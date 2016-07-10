package moe.nightfall.vic.integratedcircuits.tile;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.DiskDrive.IDiskDrive;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.client.gui.cad.GuiCAD;
import moe.nightfall.vic.integratedcircuits.cp.CircuitCache;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.net.PacketFloppyDisk;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBChangeInput;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBSimulation;
import moe.nightfall.vic.integratedcircuits.net.pcb.PacketPCBUpdate;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityCAD extends TileEntityInventory implements ICircuit, IDiskDrive, ITickable {

	private CircuitData circuitData;
	public CircuitCache cache = new CircuitCache(this);

	// Used for the GUI.
	public float scale = 0.33F;
	public double offX = 0;
	public double offY = 0;

	public int[] in = new int[4];
	public int[] out = new int[4];
	private boolean updateIO;

	// Simulation settings
	private boolean pausing = false;
	private boolean step = false;

	// Shows if there is a printer connected
	private EnumFacing printerLocation;

	public boolean isPausing() {
		return pausing;
	}

	public void setPausing(boolean pausing) {
		this.pausing = pausing;
		this.step = false;
	}

	public void step() {
		this.step = true;
	}

	public void sendSimulationState() {
		CommonProxy.networkWrapper.sendToServer(new PacketPCBSimulation(step, pausing, getPos().getX(), getPos().getY(), getPos().getZ()));
	}

	public void setup(int size) {
		circuitData = new CircuitData(size, this);
		circuitData.clearAllAndSetup(size);
	}

	@Override
	public void update() {
		// Update the matrix in case there is at least one player watching.
		if (!worldObj.isRemote && playersUsing > 0) {
			if (step || !pausing) {
				getCircuitData().updateMatrix();
				step = false;
			}

			if (getCircuitData().checkUpdate()) {
				CommonProxy.networkWrapper.sendToAllAround(
						new PacketPCBUpdate(getCircuitData(), getPos().getX(), getPos().getY(), getPos().getZ()), new TargetPoint(
								worldObj.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 8));
			}
			if (updateIO) {
				updateIO = false;
				CommonProxy.networkWrapper.sendToAllAround(new PacketPCBChangeInput(false, out, circuitData
					.getProperties().getCon(), this), new TargetPoint(
						worldObj.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 8));
			}
			markDirty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		circuitData = CircuitData.readFromNBT(compound.getCompoundTag("circuit"), this);
		in = compound.getIntArray("in");
		out = compound.getIntArray("out");
		pausing = compound.getBoolean("pausing");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
		compound.setIntArray("in", in);
		compound.setIntArray("out", out);
		compound.setBoolean("pausing", pausing);
	}

	@Override
	public int getSlots() {
		return 1;
	}

	public final boolean getExternalInputFromSide(EnumFacing dir, int frequency) {
		return (in[MiscUtils.getSide(dir)] & 1 << frequency) != 0;
	}

	@Override
	public boolean getInputFromSide(EnumFacing dir, int frequency) {
		return getExternalInputFromSide(dir, frequency) && !getOutputToSide(dir, frequency);
	}

	public boolean getOutputToSide(EnumFacing dir, int frequency) {
		return (out[MiscUtils.getSide(dir)] & 1 << frequency) != 0;
	}

	@SideOnly(Side.CLIENT)
	public final void setExternalInputFromSide(EnumFacing dir, int frequency, boolean output) {
		EnumConnectionType mode = circuitData.getProperties().getModeAtSide(dir);
		if (mode != EnumConnectionType.SIMPLE || frequency == 0) {
			int[] i = this.in.clone();
			if (mode == EnumConnectionType.ANALOG)
				i[MiscUtils.getSide(dir)] = 0;
			if (output)
				i[MiscUtils.getSide(dir)] |= 1 << frequency;
			else {
				if (mode == EnumConnectionType.ANALOG)
					i[MiscUtils.getSide(dir)] = 1;
				else
					i[MiscUtils.getSide(dir)] &= ~(1 << frequency);
			}
			CommonProxy.networkWrapper.sendToServer(new PacketPCBChangeInput(true, i, circuitData.getProperties()
				.getCon(), this));
		}
	}

	@SideOnly(Side.CLIENT)
	public void setInputMode(int side, EnumConnectionType mode) {
		int con = circuitData.getProperties().setModeAtSide(side, mode);
		int i[] = this.in.clone();
		i[side] = mode == EnumConnectionType.ANALOG ? 1 : 0;
		CommonProxy.networkWrapper.sendToServer(new PacketPCBChangeInput(true, i, con, this));
	}

	@Override
	public void setOutputToSide(EnumFacing dir, int frequency, boolean output) {
		if (output)
			out[MiscUtils.getSide(dir)] |= 1 << frequency;
		else
			out[MiscUtils.getSide(dir)] &= ~(1 << frequency);
		updateIO = true;
	}

	@Override
	public boolean receiveClientEvent(int id, int par) {
		if (id == 1) {
			if (worldObj.isRemote) {
				// Update GUI
				printerLocation = EnumFacing.getFront(par);
				GuiScreen gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiCAD) {
					((GuiCAD) gui).refreshPrinter();
				}
			}
			return true;
		} else {
			return super.receiveClientEvent(id, par);
		}
	}

	public void onNeighborBlockChange() {
		BlockPos bc = new BlockPos(this.getPos());
		EnumFacing oldPrinterLocation = printerLocation;
		printerLocation = null;
		for (EnumFacing fd : EnumFacing.VALUES) {
			BlockPos bcs = bc.offset(fd);
			if (worldObj.getBlockState(bcs) == Content.blockPrinter) {
				printerLocation = fd;
			}
		}
		if (!worldObj.isRemote && oldPrinterLocation != printerLocation) {
			worldObj.addBlockEvent(getPos(), getBlockType(), 1, printerLocation.ordinal());
		}
	}

	public EnumFacing printerLocation() {
		if (printerLocation == null) {
			onNeighborBlockChange();
		}
		return printerLocation;
	}

	public boolean isPrinterConnected() {
		return printerLocation() != null;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return MiscUtils.getRotatedInstance(
				new AxisAlignedBB(1 / 16F, 1 / 16F, -1 / 16F, 13 / 16F, 3 / 16F, 1 / 16F), rotation);
	}

	@Override
	public ItemStack getDisk() {
		return inventory.getStackInSlot(0);
	}

	@Override
	public void setDisk(ItemStack stack) {
		inventory.
		setStackInSlot(0, stack);
		if (!worldObj.isRemote)
			CommonProxy.networkWrapper.sendToDimension(new PacketFloppyDisk(getPos().getX(), getPos().getY(), getPos().getZ(), stack),
					worldObj.provider.getDimension());
	}

	@Override
	public CircuitData getCircuitData() {
		return circuitData;
	}

	@Override
	public void setCircuitData(CircuitData data) {
		this.circuitData = data;
	}
}
