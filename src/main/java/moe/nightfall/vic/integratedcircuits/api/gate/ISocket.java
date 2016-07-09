package moe.nightfall.vic.integratedcircuits.api.gate;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import codechicken.lib.data.MCDataInput;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * Contains all methods that have to be called by an {@link ISocketWrapper} and
 * documentation.
 * 
 * @author Vic Nightfall
 */
public interface ISocket extends ISocketBase {
	void update();

	void readFromNBT(NBTTagCompound compound);

	void writeToNBT(NBTTagCompound compound);

	void writeDesc(NBTTagCompound compound);

	void readDesc(NBTTagCompound compound);

	void read(ByteBuf packet);

	/**
	 * Only has to be called when the orientation needs to be set automatically,
	 * relative to the player's position and look vector. If you want to set the
	 * orientation manually, use the two methods {@link #setSide(EnumFacing)} and
	 * {@link #setRotation(EnumFacing)}
	 * 
	 * @param player
	 * @param pos
	 * @param side
	 * @param stack
	 */
	void preparePlacement(EntityPlayer player, BlockPos pos, EnumFacing side, ItemStack stack);

	boolean activate(EntityPlayer player, RayTraceResult hit, ItemStack stack);

	void onNeighborChanged();

	void addDrops(List<ItemStack> list);
	
	/**
	 * Called when a gate (circuit) is placed onto the socket.
	 * This is to allow a socket, if it wants, to allow a player to place something on it, without using it up.
	 * A creative mode check should happen in other code, presumably the code calling this method.
	 * 
	 * This is also used to determine if the gate should be dropped when removed.
	 * 
	 * @return If the gate is to be used up (removed from inventory)
	 */
	boolean usesUpPlacedGate();

	ItemStack pickItem(RayTraceResult target);

	void scheduledTick();

	void onAdded();

	void onMoved();

	void onRemoved();

	enum EnumConnectionType {
		SIMPLE(1), ANALOG(16), BUNDLED(16), NONE(0);

		EnumConnectionType(int size) { this.size = size; }
		public final int size;

		public boolean isBundled() { return this == BUNDLED; }
		public boolean isRedstone() { return this == SIMPLE || this == ANALOG; }
		public boolean isDisabled() { return this.size == 0; }
		public boolean isSingle() { return this.size == 1; }
		public boolean isFull() { return this.size == -1; }
		public boolean isAnalog() { return this == ANALOG; }
		
		/** Get single character (as a string) that uniquely identifies this connection type. **/
		public String singleID() { return Character.toString(singleCharID()); }
		/** Get single character (as a character) that uniquely identifies this connection type. **/
		public char singleCharID() { return name().charAt(0); }
		
		/** Get a List of supported connection types based on the maximum size supported. **/
		public static List<EnumConnectionType> getSupportedList(int size) {
			ArrayList<EnumConnectionType> list = new ArrayList<EnumConnectionType>();
			for (EnumConnectionType connectionType : EnumConnectionType.values()) {
				if (connectionType.size <= size) list.add(connectionType);
			}
			return list;
		}
	}

	/**
	 * Rotates the socket (or gate in the socket)
	 * @return true if successful.
	 */
	boolean rotate();
}
