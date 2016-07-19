package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public abstract class TileEntityContainer extends TileEntity {
	public EnumFacing rotation = EnumFacing.NORTH;
	public int playersUsing;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		rotation = EnumFacing.values()[compound.getInteger("rotation")];
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("rotation", rotation.getIndex());
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		return new SPacketUpdateTileEntity(pos, 0, compound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound compound = pkt.getNbtCompound();
		readFromNBT(compound);
	}

	@Override
	public boolean receiveClientEvent(int id, int par) {
		if (id == 0) {
			playersUsing = par;
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			return true;
		}
		return false;
	}

	public void onSlotChange(int id) {

	}

	public boolean rotate() {

		this.rotation = MiscUtils.rot(rotation);
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); // FIXME flags, states
		return true;
	}
}
