package moe.nightfall.vic.integratedcircuits.tile;

//import buildcraft.api.tiles.IControllable;
//import buildcraft.api.tiles.IHasWork;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.DiskDrive.IDiskDrive;
import moe.nightfall.vic.integratedcircuits.LaserHelper;
import moe.nightfall.vic.integratedcircuits.client.TextureRenderer;
import moe.nightfall.vic.integratedcircuits.client.gui.GuiAssembler;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.misc.CraftingSupply;
import moe.nightfall.vic.integratedcircuits.misc.IOptionsProvider;
import moe.nightfall.vic.integratedcircuits.misc.InventoryUtils;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import moe.nightfall.vic.integratedcircuits.net.PacketAssemblerStart;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemStackHandler;

@InterfaceList({ @Interface(iface = "buildcraft.api.tiles.IControllable", modid = "BuildCraft|Core"),
		@Interface(iface = "buildcraft.api.tiles.IHasWork", modid = "BuildCraft|Core") })
public class TileEntityAssembler extends TileEntityInventory implements IDiskDrive, IOptionsProvider, ITickable //FIXME use capabilities
		/* FIXME reimplement IHasWork, IControllable */ {
	public static final int IDLE = 0, RUNNING = 1, OUT_OF_MATERIALS = 2,
			OUT_OF_PCB = 3;
	public static final int SETTING_PULL = 0, SETTING_REDSTONE = 1;
	public static final int RS_ENABLED = 0, RS_INVERTED = 1, RS_DISABLED = 2;

	private static final ItemStack STACK_PCB = new ItemStack(Content.itemPCB, 1);

	public int[][] refMatrix;
	private int statusCode;

	// Client
	public TextureRenderer.Entry texture;
	public boolean isOccupied;
	public byte request = 1;

	private byte queue, position;
	public int size;
	private int power = -1;
	private int output = 0;
	private boolean powerOverride;

	public boolean[][] excMatrix;
	public CircuitData cdata;
	public LaserHelper laserHelper = new LaserHelper(this, 9);

	public CraftingSupply craftingSupply = new CraftingSupply(this, 2, 9);
	private OptionSet<TileEntityAssembler> optionSet = new OptionSet<TileEntityAssembler>(this);

	@Override
	public void update() {
		//if (worldObj.isRemote && texture != null)
			//TileEntityAssemblerRenderer.scheduleFramebuffer(this);
		if (worldObj.isRemote)
			return;

		if (power == -1)
			onNeighborBlockChange();

		if (refMatrix != null)
			laserHelper.update();

		if (output == 0)
			worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); // FIXME flags
		if (output >= 0)
			output--;

		if (statusCode == OUT_OF_PCB && queue != 0)
			requestCircuitPlayload();
		else if (statusCode == IDLE && getOptionSet().getBoolean(SETTING_PULL)) {
			if ((isPowered() || powerOverride) && output < 0) {
				ItemStack stack = tryFetchPCB();
				if (stack != null)
					inventory.setStackInSlot(1, stack);
				requestCircuit((byte) 1);
			}
		}
	}

	private boolean isPowered() {
		int rs = getOptionSet().getInt(SETTING_REDSTONE);
		return (rs == RS_ENABLED && power > 0) || (rs == RS_INVERTED && power == 0);
	}

	public int isProvidingPower() {
		return output > 0 && getOptionSet().getInt(SETTING_REDSTONE) != RS_DISABLED ? 15 : 0;
	}

	public void onNeighborBlockChange() {
		int nPower = worldObj.isBlockIndirectlyGettingPowered(pos);
		if (nPower != power) {
			boolean o = power > 0, n = nPower > 0;
			int rsmode = getOptionSet().getInt(SETTING_REDSTONE);
			if (o != n && ((n && rsmode == RS_ENABLED) || (!n && rsmode == RS_INVERTED)) && output < 0) {
				if (getStatus() == IDLE) {
					ItemStack stack = tryFetchPCB();
					if (stack != null)
						inventory.setStackInSlot(1, stack);
					requestCircuit((byte) 1);
				}
			}
			power = nPower;
		}
	}

	public void updateStatus(int status) {
		if (statusCode != status) {
			statusCode = status;
			if (!worldObj.isRemote)
				worldObj.addBlockEvent(pos, getBlockType(), 1, status);
		}
	}

	public int getStatus() {
		return statusCode;
	}

	@Override
	public OptionSet getOptionSet() {
		return optionSet;
	}

	@Override
	public void onSettingChanged(int setting) {
		if (worldObj.isRemote && Minecraft.getMinecraft().currentScreen instanceof GuiAssembler)
			((GuiAssembler) Minecraft.getMinecraft().currentScreen).refreshUI();
	}

	@Override
	public boolean receiveClientEvent(int id, int par) {
		if (id == 1) {
			if (worldObj.isRemote)
				statusCode = par;
			return true;
		} else if (id == 2) {
			if (worldObj.isRemote)
				position = (byte) par;
			return true;
		}

		return super.receiveClientEvent(id, par);
	}

	public void requestCircuit(byte amount) {
		if (queue != 0)
			return;
		setQueueSize(amount);
		if (!requestCircuitPlayload())
			setQueueSize((byte) 0);
		position = 0;
		worldObj.addBlockEvent(pos, getBlockType(), 2, position);
	}

	private boolean requestCircuitPlayload() {
		if (cdata != null && laserHelper.getLaserAmount() > 0) {
			craftingSupply.clear();
			if (InventoryUtils.tryFetchItem(this, STACK_PCB.copy(), 1, 1) == null) {
				if (!(optionSet.getBoolean(SETTING_PULL) && tryFetchPCB() != null)) {
					updateStatus(OUT_OF_PCB);
					return true;
				}
			}
			laserHelper.reset();
			laserHelper.start();
			updateStatus(RUNNING);
			CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerStart(pos.getX(), pos.getY(), pos.getZ(), queue),
					worldObj.provider.getDimension());
			return true;
		}
		return false;
	}

	/** Clears the PCB slot and returns an empty PCB if one was found **/
	public ItemStack tryFetchPCB() {
		ItemStack pcb = inventory.getStackInSlot(1);
		if (pcb != null) {
			if (STACK_PCB.isItemEqual(pcb))
				return pcb;
			if (!InventoryUtils.tryPutItem(this, pcb, 2, 9))
				return null;
			inventory.setStackInSlot(1, null);
		}
		pcb = InventoryUtils.tryFetchItem(this, STACK_PCB.copy(), 2, 9);
		return pcb;
	}

	public void setQueueSize(byte queue) {
		this.queue = queue;
	}

	public byte getQueueSize() {
		return queue;
	}

	public void clearQueue() {
		position = 0;
		queue = 0;
		if (getStatus() != RUNNING && getStatus() != OUT_OF_MATERIALS) {
			laserHelper.reset();
			updateStatus(IDLE);
			CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerStart(pos.getX(), pos.getY(), pos.getZ(), queue),
					worldObj.provider.getDimension());
		}
	}

	public byte getQueuePosition() {
		return position;
	}

	public void onCircuitFinished() {
		if (inventory.getStackInSlot(1) == null) {
			inventory.setStackInSlot(1, new ItemStack(Content.itemPCB, 1, 1));
			NBTTagCompound comp = new NBTTagCompound();
			comp.setTag("circuit", cdata.writeToNBTRaw(new NBTTagCompound()));
			inventory.getStackInSlot(1).setTagCompound(comp);
			worldObj.addBlockEvent(pos, getBlockType(), 2, ++position);
			// Give off a redstone pulse
			output = 2;
			worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); //FIXME flags and stuff
		}
		if (position == queue || queue == 0) {
			queue = 0;
			updateStatus(IDLE);
		} else
			requestCircuitPlayload();

		markDirty();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (worldObj.isRemote && texture != null) {
			ClientProxy.textureRenderer.delete(texture);
			texture = null;
		}
	}

	@Override
	public void onSlotChange(int id) {
		if (worldObj == null) return;
		if (worldObj.isRemote)
			return;
		if (id > 8 && id < 13)
			laserHelper.createLaser(id - 9, inventory.getStackInSlot(id));
		else if (id == 1)
			CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerStart(pos.getX(), pos.getY(), pos.getZ(), queue),
					worldObj.provider.getDimension());
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

	public void dropContents() {
		for (int i = 0; i < inventory.getSlots(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null)
				continue;
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX(), pos.getY(), pos.getZ(), stack));
		}
	}

	@Override
	public void setDisk(ItemStack stack) {
		inventory.setStackInSlot(0, stack);
		if (!worldObj.isRemote)
			CommonProxy.networkWrapper.sendToDimension(new PacketAssemblerStart(pos.getX(), pos.getY(), pos.getZ(), queue),
					worldObj.provider.getDimension());
		loadMatrixFromDisk();
		if (worldObj.isRemote && Minecraft.getMinecraft().currentScreen instanceof GuiAssembler) {
			cdata.calculateCost();
			((GuiAssembler) Minecraft.getMinecraft().currentScreen).refreshUI();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		powerOverride = compound.getBoolean("powerOverride");
		output = compound.getInteger("output");
		queue = compound.getByte("queue");
		position = compound.getByte("positon");
		statusCode = compound.getInteger("status");
		optionSet = OptionSet.readFromNBT(compound, this);

		loadMatrix(compound);
		if (compound.hasKey("tmp")) {
			excMatrix = new boolean[size][size];
			byte[] temp = compound.getByteArray("tmp");
			for (int i = 0; i < temp.length; i++)
				excMatrix[i / size][i % size] = temp[i] != 0;
		}

		laserHelper.readFromNBT(compound);

		if (MiscUtils.isClient() && (inventory.getStackInSlot(1) != null || laserHelper.isRunning)) {
			isOccupied = true;
			//TileEntityAssemblerRenderer.scheduleFramebuffer(this);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);

		compound.setBoolean("powerOverride", powerOverride);
		compound.setInteger("output", output);
		compound.setByte("queue", queue);
		compound.setByte("positon", position);
		compound.setInteger("status", statusCode);
		optionSet.writeToNBT(compound);

		saveMatrix(compound);
		if (excMatrix != null) {
			byte[] temp = new byte[size * size];
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++)
					temp[x + y * size] = (byte) (excMatrix[x][y] ? 1 : 0);
			compound.setByteArray("tmp", temp);
		}

		laserHelper.writeToNBT(compound);
	}

	@Override
	public int getSlots() {
		return 13;
	}

	private void loadMatrix(NBTTagCompound compound) {
		if (compound.hasKey("circuit")) {
			NBTTagCompound circuit = compound.getCompoundTag("circuit");
			cdata = CircuitData.readFromNBT(circuit);
			craftingSupply = CraftingSupply.readFromNBT(compound, this, 2, 9);
			size = cdata.getSize();

			refMatrix = new int[size][size];

			NBTTagList idlist = circuit.getTagList("id", NBT.TAG_INT_ARRAY);
			for (int i = 0; i < idlist.tagCount(); i++)
				refMatrix[i] = idlist.getIntArrayAt(i);
		}
	}

	private void saveMatrix(NBTTagCompound compound) {
		if (refMatrix != null) {
			NBTTagCompound circuit = new NBTTagCompound();
			cdata.writeToNBTRaw(circuit);
			craftingSupply.writeToNBT(compound);
			compound.setTag("circuit", circuit);
		}
	}

	public void loadMatrixFromDisk() {
		if (getDisk() != null) {
			ItemStack stack = getDisk();
			NBTTagCompound comp = stack.getTagCompound();
			if (comp != null && comp.hasKey("circuit"))
				loadMatrix(comp);
			else
				refMatrix = null;
		}
	}

	private static final int[] accessibleSlots = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	public int[] getSlotsForFace(EnumFacing side) {
		if (getConnectionOnSide(side) > -1)
			return accessibleSlots;
		return new int[0];
	}

	private int getConnectionOnSide(EnumFacing side) {
		if (side == EnumFacing.UP)
			return -1;
		else if (side == EnumFacing.DOWN)
			return 0;
		side = side.rotateAround(rotation.getAxis()); // FIXME check this
		if (side == EnumFacing.SOUTH)
			return 1;
		else if (side != EnumFacing.NORTH)
			return 0;
		return -1;
	}

	public boolean isItemValidForSlot(int id, ItemStack stack) {
		if (id < 1 || id > 9)
			return false;
		if (id == 1 && inventory.getStackInSlot(1) == null && STACK_PCB.isItemEqual(stack))
			return true;
		return id != 1;
	}

	/*public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		int con = getConnectionOnSide(direction);
		return con == 0;
	}

	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		int con = getConnectionOnSide(direction);
		boolean isPCB = stack.getItem() == Content.itemPCB && stack.getItemDamage() == 1;
		if (con == 0)
			return index != 1 && !isPCB;
		else if (con == 1)
			return isPCB;
		return false;
	}*/ // FIXME reimplement sidedness

	/* FIXME reimplement
	@Override
	public Mode getControlMode() {
		if (powerOverride || isPowered())
			return Mode.On;
		return Mode.Off;
	}

	@Override
	public void setControlMode(Mode mode) {
		if (mode == Mode.On) {
			powerOverride = true;
			getOptionSet().changeSetting(SETTING_PULL, true);
		} else if (mode == Mode.Off)
			powerOverride = false;
	}

	@Override
	public boolean acceptsControlMode(Mode mode) {
		return mode == Mode.Off || mode == Mode.On;
	}

	@Override
	public boolean hasWork() {
		return getStatus() != IDLE;
	}*/

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {


		return super.getCapability(capability, facing);
	}

	@Override
	protected void createStackHandler() {
		inventory = new ItemStackHandler(getSlots()) {
			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				if (!isItemValidForSlot(slot, stack)) return stack;

				return super.insertItem(slot, stack, simulate);
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				return super.extractItem(slot, amount, simulate);
			}

			@Override
			protected void onContentsChanged(int slot) {
				onSlotChange(slot);
			}
		};
	}

	public boolean rotate() {
		rotation = rotation.rotateY();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); //FIXME flags
		return true;
	}
}
