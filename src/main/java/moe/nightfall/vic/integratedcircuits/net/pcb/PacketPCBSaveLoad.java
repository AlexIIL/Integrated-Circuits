package moe.nightfall.vic.integratedcircuits.net.pcb;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.net.PacketTileEntity;
import moe.nightfall.vic.integratedcircuits.proxy.CommonProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class PacketPCBSaveLoad extends PacketTileEntity<PacketPCBSaveLoad> {
	private boolean write;

	public PacketPCBSaveLoad() {
	}

	public PacketPCBSaveLoad(boolean write, int xCoord, int yCoord, int zCoord) {
		super(xCoord, yCoord, zCoord);
		this.write = write;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		this.write = buffer.readBoolean();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeBoolean(write);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(new BlockPos(xCoord, yCoord, zCoord));
		if (te != null) {
			if (write) {
				ItemStack floppy = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
				if (floppy != null) {
					NBTTagCompound comp = floppy.getTagCompound();
					if (comp == null)
						comp = new NBTTagCompound();
					te.getCircuitData().getProperties().setAuthor(player.getCommandSenderEntity().getName());
					comp.setTag("circuit", te.getCircuitData().writeToNBT(new NBTTagCompound()));
					floppy.setTagCompound(comp);
					((IItemHandlerModifiable)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)).setStackInSlot(0, floppy);
				}
			} else {
				ItemStack floppy = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(0);
				if (floppy != null) {
					NBTTagCompound comp = floppy.getTagCompound();
					if (comp == null)
						return;
					if (comp.hasKey("circuit"))
						te.setCircuitData(CircuitData.readFromNBT((NBTTagCompound) comp.getCompoundTag("circuit")
							.copy(), te));
					else
						te.getCircuitData().clearAllAndSetup(te.getCircuitData().getSize());
					CommonProxy.networkWrapper.sendToAllAround(new PacketPCBLoad(te.getCircuitData(), xCoord, yCoord,
							zCoord), new TargetPoint(te.getWorld().provider.getDimension(), xCoord, yCoord, zCoord, 8));
				}
			}
		}
	}
}
