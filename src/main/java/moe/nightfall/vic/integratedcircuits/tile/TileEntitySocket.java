package moe.nightfall.vic.integratedcircuits.tile;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntitySocket extends TileEntity implements ISocketWrapper, ITickable {
	public ISocket socket = IntegratedCircuitsAPI.getGateRegistry().createSocketInstance(this);

	public boolean isDestroyed;

	@Override
	public void markRender() {
		worldObj.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void update() {
		socket.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		socket.readFromNBT(compound);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		socket.writeToNBT(compound);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound comp = new NBTTagCompound();
		socket.writeDesc(comp);
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound comp = pkt.getNbtCompound();
		socket.readDesc(comp);
	}

	@Override
	public ByteBuf getWriteStream(int disc) {
		return IntegratedCircuitsAPI.getWriteStream(getWorld(), getPos(), socket.getSide()).writeByte(disc);
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	@Override
	public void notifyBlocksAndChanges() {
		markDirty();
		worldObj.notifyNeighborsOfStateChange(pos, getBlockType());
	}

	@Override
	public void notifyPartChange() {
		markDirty();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); // FIXME states, flags, is this the rigth function?
	}

	@Override
	public BlockPos getPos() {
		return new BlockPos(pos);
	} // TODO does this have to make a new one?

	@Override
	public void destroy() {
		MiscUtils.dropItem(worldObj, new ItemStack(Content.itemSocket), pos.getX(), pos.getY(), pos.getZ());
		isDestroyed = true;
		worldObj.setBlockToAir(pos);
	}

	@Override
	public byte[] updateBundledInput(EnumFacing side) {
		return IntegratedCircuitsAPI.updateBundledInput(getSocket(), side);
	}

	@Override
	public int updateRedstoneInput(EnumFacing side) {
		return IntegratedCircuitsAPI.updateRedstoneInput(getSocket(), side);
	}

	@Override
	public void scheduleTick(int delay) {
		worldObj.scheduleBlockUpdate(pos, getBlockType(), delay, 0);// TODO check priority
	}

	@Override
	public ISocket getSocket() {
		return socket;
	}

	@Override
	public void sendDescription() {
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); // FIXME flags, states
	}

	@Override
	public void updateInput() {
		socket.updateInput();
	}
}