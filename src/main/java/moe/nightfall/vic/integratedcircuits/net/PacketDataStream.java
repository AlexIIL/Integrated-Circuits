package moe.nightfall.vic.integratedcircuits.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.api.IntegratedCircuitsAPI;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

public class PacketDataStream extends PacketTileEntity<PacketDataStream> {
	private EnumFacing side;
	private byte[] data;

	public PacketDataStream() {
	}

	public PacketDataStream(byte[] data, int x, int y, int z, EnumFacing side) {
		super(x, y, z);
		this.data = data;
		this.side = side;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		side = EnumFacing.getFront(buffer.readInt());
		data = buffer.readByteArray();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeInt(side.getIndex());
		buffer.writeByteArray(data);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		ISocket socket = IntegratedCircuitsAPI.getSocketAt(player.worldObj, new BlockPos(xCoord, yCoord, zCoord),
				this.side);
		if (socket == null)
			return;
		socket.read(Unpooled.wrappedBuffer(data));
	}
}
