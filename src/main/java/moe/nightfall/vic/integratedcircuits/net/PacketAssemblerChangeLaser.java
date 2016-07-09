package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;

public class PacketAssemblerChangeLaser extends PacketTileEntity<PacketAssemblerChangeLaser> {
	private int id;
	private ItemStack laser;

	public PacketAssemblerChangeLaser() {
	}

	public PacketAssemblerChangeLaser(int xCoord, int yCoord, int zCoord, int id, ItemStack laser) {
		super(xCoord, yCoord, zCoord);
		this.id = id;
		this.laser = laser;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		id = buffer.readInt();
		laser = buffer.readItemStackFromBuffer();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeInt(id);
		buffer.writeItemStackToBuffer(laser);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityAssembler te = (TileEntityAssembler) player.worldObj.getTileEntity(new BlockPos(xCoord, yCoord, zCoord));
		if (te == null)
			return;
		te.laserHelper.createLaser(id, laser);
	}
}
