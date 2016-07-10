package moe.nightfall.vic.integratedcircuits.gate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.IGate;
import moe.nightfall.vic.integratedcircuits.api.gate.IGateItem;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.misc.Cube;
import moe.nightfall.vic.integratedcircuits.misc.InventoryUtils;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Socket implements ISocket {
	// Collision box
	public static Cube box = new Cube(0, 0, 0, 1, 2 / 16D, 1);

	protected final ISocketWrapper provider;
	protected final Map<String, Object> extendedProperties;
	protected IGate gate;

	// Used by the client, redstone IO
	protected byte io;
	protected byte[][] output = new byte[4][16];
	protected byte[][] input = new byte[4][16];

	protected EnumFacing orientation;

	public Socket(ISocketWrapper provider) {
		this.provider = provider;
		this.extendedProperties = new HashMap<String, Object>();
	}

	// Bridge methods

	@Override
	public void update() {
		if (gate != null)
			gate.update();
	}

	@Override
	public void onAdded() {
		if (gate != null)
			gate.onAdded();
	}

	@Override
	public void onMoved() {
		if (gate != null)
			gate.onMoved();
	}

	@Override
	public void scheduledTick() {
		if (gate != null)
			gate.scheduledTick();
	}

	@Override
	public void onRemoved() {
		if (gate != null)
			gate.onRemoved();
	}

	// Bridge methods, calling liked IGateProvider

	@Override
	public void markRender() {
		provider.markRender();
	}

	@Override
	public ByteBuf getWriteStream(int disc) {
		return provider.getWriteStream(disc);
	}

	@Override
	public World getWorld() {
		return provider.getWorld();
	}

	@Override
	public void notifyBlocksAndChanges() {
		notifyPartChange();
		provider.notifyBlocksAndChanges();
	}

	@Override
	public void notifyPartChange() {
		if(!provider.getWorld().isRemote)
			updateRedstoneIO();
		provider.notifyPartChange();
	}

	@Override
	public BlockPos getPos() {
		return provider.getPos();
	}

	@Override
	public void sendDescription() {
		provider.sendDescription();
	}

	@Override
	public void destroy() {
		if (gate != null) {
			BlockPos pos = getPos();
			MiscUtils.dropItem(getWorld(), gate.getItemStack(), pos.getX(), pos.getY(), pos.getZ());
		}
		provider.destroy();
	}

	@Override
	public int updateRedstoneInput(EnumFacing side) {
		return provider.updateRedstoneInput(side);
	}

	@Override
	public byte[] updateBundledInput(EnumFacing side) {
		return provider.updateBundledInput(side);
	}

	@Override
	public void scheduleTick(int delay) {
		provider.scheduleTick(delay);
	}

	@Override
	public void setGate(IGate gate) {
		this.gate = gate;
		this.gate.setProvider(this);
		this.gate.onAdded();
	}

	@Override
	public IGate getGate() {
		return gate;
	}

	// IO

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		// Read orientation and IO
		orientation = EnumFacing.getFront(compound.getInteger("orientation"));
		io = compound.getByte("io");

		byte[] input = compound.getByteArray("input");
		byte[] output = compound.getByteArray("output");

		for (int i = 0; i < 4; i++) {
			this.input[i] = Arrays.copyOfRange(input, i * 16, (i + 1) * 16);
			this.output[i] = Arrays.copyOfRange(output, i * 16, (i + 1) * 16);
		}

		// Read gate from NBT, if present
		if (compound.hasKey("gate_id")) {
			gate = IntegratedCircuitsAPI.getGateRegistry().createGateInstace(compound.getString("gate_id"));
			gate.setProvider(this);
			gate.load(compound.getCompoundTag("gate"));
		} else
			gate = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		// Write orientation and IO
		compound.setInteger("orientation", orientation.getIndex());
		compound.setByte("io", io);

		byte[] input = null;
		byte[] output = null;

		for (int i = 0; i < 4; i++) {
			input = ArrayUtils.addAll(input, this.input[i]);
			output = ArrayUtils.addAll(output, this.output[i]);
		}

		compound.setByteArray("input", input);
		compound.setByteArray("output", output);

		// Write gate to NBT, if present
		if (gate != null) {
			compound.setString("gate_id", IntegratedCircuitsAPI.getGateRegistry().getName(gate.getClass()));
			NBTTagCompound gateCompound = new NBTTagCompound();
			gate.save(gateCompound);
			compound.setTag("gate", gateCompound);
		}
	}

	@Override
	public void writeDesc(NBTTagCompound compound) {
		compound.setInteger("orientation", orientation.getIndex());
		compound.setByte("io", io);

		if (gate != null) {
			compound.setString("gate_id", IntegratedCircuitsAPI.getGateRegistry().getName(gate.getClass()));
			gate.writeDesc(compound);
		}
	}

	@Override
	public void readDesc(NBTTagCompound compound) {
		orientation = EnumFacing.getFront(compound.getInteger("orientation"));
		io = compound.getByte("io");

		if (compound.hasKey("gate_id")) {
			gate = IntegratedCircuitsAPI.getGateRegistry().createGateInstace(compound.getString("gate_id"));
			gate.setProvider(this);
			gate.readDesc(compound);
		} else
			gate = null;

		markRender();
	}

	@Override
	public void read(ByteBuf packet) {
		byte discr = packet.readByte();
		switch (discr) {
			case 0:
				orientation = EnumFacing.getFront(packet.readInt());
				markRender();
				return;
			case 1:
				io = packet.readByte();
				markRender();
				return;
		}
		if (gate != null)
			gate.read(discr, packet);
	}

	// Rotation

	@Override
	public EnumFacing getOrientation() {
		return orientation;
	}

	@Override
	public EnumFacing getSide() {
		return orientation;
	}

	@Override
	public EnumFacing getSideRel(EnumFacing side) {
		return getRotationRel(side);
	}

	@Override
	public void setSide(EnumFacing s) {
		orientation = s;
	}

	@Override
	public EnumFacing getRotation() {
		return orientation;
	}

	@Override
	public EnumFacing getRotationAbs(EnumFacing rel) {
		return orientation.rotateAround(rel.getAxis());
	}

	@Override
	public EnumFacing getRotationRel(EnumFacing abs) {
		return orientation;
	}

	@Override
	public void setRotation(EnumFacing s) {
		orientation = s;
	}

	// Redstone IO

	@Override
	public byte[][] getInput() {
		return input;
	}

	@Override
	public byte[][] getOutput() {
		return output;
	}

	@Override
	public byte getRedstoneInput(EnumFacing side) {
		return getBundledInput(side, 0);
	}

	@Override
	public byte getBundledInput(EnumFacing side, int frequency) {
		byte i = input[side.getHorizontalIndex()][frequency];
		if (getConnectionTypeAtSide(side) == EnumConnectionType.ANALOG) {
			if (i <= getRedstoneOutput(side))
				return 0;
		} else {
			if (output[side.getHorizontalIndex()][frequency] != 0)
				return 0;
		}
		return i;
	}

	@Override
	public byte getRedstoneOutput(EnumFacing side) {
		EnumConnectionType conType = getConnectionTypeAtSide(side);
		if (conType == EnumConnectionType.ANALOG) {
			// Convert digital to analog, take highest output
			byte[] out = getOutput()[side.getHorizontalIndex()];
			for (byte i = 15; i >= 0; i--) {
				if (out[i] != 0)
					return i;
			}
			return 0;
		} else if (conType == EnumConnectionType.SIMPLE) {
			return getBundledOutput(side, 0);
		}
		return 0;
	}

	@Override
	public byte getBundledOutput(EnumFacing side, int frequency) {
		return output[side.getHorizontalIndex()][frequency];
	}

	@Override
	public void setInput(byte[][] input) {
		if (input == null)
			throw new NullPointerException();
		this.input = input;
	}

	@Override
	public void setOutput(byte[][] output) {
		if (output == null)
			throw new NullPointerException();
		this.output = output;
	}

	@Override
	public void setInput(EnumFacing side, int frequency, byte input) {
		this.input[side.getHorizontalIndex()][frequency] = input;
	}

	@Override
	public void setOutput(EnumFacing side, int frequency, byte output) {
		this.output[side.getHorizontalIndex()][frequency] = output;
	}

	@Override
	public void resetInput() {
		this.input = new byte[4][16];
	}

	@Override
	public void resetOutput() {
		this.output = new byte[4][16];
	}

	@Override
	public void updateInput() {
		if (getWorld().isRemote)
			return;
		updateInputPre();
		for (int i = 0; i < 4; i++) {
			EnumConnectionType type = getConnectionTypeAtSide(EnumFacing.getHorizontal(i));
			if (type.isRedstone())
				input[i][0] = (byte) updateRedstoneInput(EnumFacing.getHorizontal(i));
			else if (type.isBundled())
				input[i] = updateBundledInput(EnumFacing.getHorizontal(i));
		}
		updateInputPost();
	}

	@Override
	public void updateInputPre() {
		if (gate != null)
			gate.updateInputPre();
	}

	@Override
	public void updateInputPost() {
		if (gate != null) {
			gate.updateInputPost();
			updateRedstoneIO();
		}
	}

	@Override
	public EnumConnectionType getConnectionTypeAtSide(EnumFacing side) {
		return gate != null ? gate.getConnectionTypeAtSide(side) : EnumConnectionType.NONE;
	}

	private void updateRedstoneIO() {
		byte oio = io;
		io = 0;
		for (int i = 0; i < 4; i++)
			io |= (getRedstoneInput(EnumFacing.getHorizontal(i)) != 0 || getRedstoneOutput(EnumFacing.getHorizontal(i)) != 0) ? 1 << i : 0;

		if (oio != io)
			provider.getWriteStream(1).writeByte(io);
	}

	// Interaction

	@Override
	public void preparePlacement(EntityPlayer player, BlockPos pos, EnumFacing side, ItemStack stack) {
		setSide(side.getOpposite());
		//setRotation(Rotation.getSidedRotation(player, side));
	}

	@Override
	public void setGate(ItemStack stack, EntityPlayer player) {
		if (stack.getItem() instanceof IGateItem) {
			String gateID = ((IGateItem) stack.getItem()).getGateID(stack, player, getPos());
			gate = IntegratedCircuitsAPI.getGateRegistry().createGateInstace(gateID);
			gate.setProvider(this);
			gate.preparePlacement(player, stack);
			gate.onAdded();
			sendDescription();
			notifyBlocksAndChanges();
		}
	}

	@Override
	public boolean activate(EntityPlayer player, RayTraceResult hit, ItemStack stack) {
		if (stack != null) {
			if (!getWorld().isRemote) {
				if (gate == null && stack.getItem() instanceof IGateItem) {
					if (!player.capabilities.isCreativeMode) {
						ItemStack solderingIron;
						if ((solderingIron = InventoryUtils.getFirstItem(Content.itemSolderingIron,
								player.inventory)) != null) {
							solderingIron.damageItem(1, player);
							if (solderingIron.getItemDamage() == solderingIron.getMaxDamage())
								player.inventory.setInventorySlotContents(
										InventoryUtils.getSlotIndex(solderingIron, player.inventory), null);
							player.inventoryContainer.detectAndSendChanges();
						} else return false;
					}

					// Set the rotation of the socket based on where the player is facing
					EnumFacing rotation = getSide();
					setRotation(rotation);
					// Put the gate in the socket
					setGate(stack, player);
					// Now remove the item from the stack if the player is not in creative mode and the item and the socket wants it
					if (!player.capabilities.isCreativeMode
							&& ((IGateItem) stack.getItem()).usedUpOnPlace(player)
							&& this.usesUpPlacedGate()) {
						stack.stackSize--;
					}
					
					MiscUtils.playPlaceSound(player, getWorld(), getPos());
					return true;
				} else if (gate != null && stack.getItem() == Content.itemSolderingIron) {
					stack.damageItem(1, player);
					if (stack.getItemDamage() == stack.getMaxDamage())
						stack.stackSize--;
					else
						((EntityPlayerMP) player).updateHeldItem();
					
					if (!player.capabilities.isCreativeMode
							&& ((IGateItem) gate.getItemStack().getItem()).usedUpOnPlace(player)
							&& this.usesUpPlacedGate()) {
						BlockPos pos = getPos();
						MiscUtils.dropItem(getWorld(), gate.getItemStack(), pos.getX(), pos.getY(), pos.getZ());
					}
					
					gate = null;
					sendDescription();
					notifyBlocksAndChanges();

					return true;
				}
			}

			Item item = stack.getItem();
			String name = item.getUnlocalizedName();

			if (checkItemIsTool(item)) {
				if (!getWorld().isRemote && gate != null) {
					if (!player.isSneaking())
						rotate();
					gate.onActivatedWithScrewdriver(player, hit, stack);
				}
				stack.damageItem(1, player);
				return true;
			}
		}
		
		if (gate != null)
			return gate.activate(player, hit, stack);
		return false;
	}

	public static boolean checkItemIsTool(Item item) {
		/*if (item != null) {*/
	/*		return (IntegratedCircuits.isPRLoaded && item instanceof mrtjp.projectred.api.IScrewdriver)
			        || item == Content.itemScrewdriver || item.getUnlocalizedName().equals("item.redlogic.screwdriver")
					|| (IntegratedCircuits.isBPAPIThere && item instanceof com.bluepowermod.api.misc.IScrewdriver)
					|| (IntegratedCircuits.isBCToolsAPIThere && item instanceof buildcraft.api.tools.IToolWrench);*/
		/*} else*/ return false;
	}

	@Override
	public boolean rotate() {
		setRotation(MiscUtils.rotn(getRotation(), 1));
		if (!getWorld().isRemote)
			getWriteStream(0).writeInt(orientation.getHorizontalIndex());
		notifyBlocksAndChanges();
		if (gate != null && !getWorld().isRemote) {
			gate.onRotated();
			updateInput();
		}
		return true;
	}

	@Override
	public void onNeighborChanged() {
		if (!getWorld().isRemote) {
			BlockPos pos = getPos().offset(getSide());
			if (!MiscUtils.canPlaceGateOnSide(getWorld(), pos, getSide().getOpposite())) {
				destroy();
			} else
				updateInput();
		}
		if (gate != null)
			gate.onNeighborChanged();
	}

	@Override
	public void addDrops(List<ItemStack> list) {
		if (gate != null)
			list.add(gate.getItemStack());
	}
	
	@Override
	public boolean usesUpPlacedGate() {
		return true;
	}
	
	@Override
	public ItemStack pickItem(RayTraceResult mop) {
		if (gate != null)
			return gate.pickItem(mop);
		return null;
	}
/*
	public static Transformation getRotationTransformation(ISocket socket) {
		return Rotation.sideOrientation(socket.getSide(), socket.getRotation()).at(Vector3.center);
	}*/

	@Override
	public ISocketWrapper getWrapper() {
		return provider;
	}

	@Override
	public <T> T get(String key) {
		return (T) extendedProperties.get(key);
	}

	@Override
	public void put(String key, Object value) {
		extendedProperties.put(key, value);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public byte getRedstoneIO() {
		return io;
	}
}
