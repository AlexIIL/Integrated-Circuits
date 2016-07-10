package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCAD extends Container {
	public TileEntityCAD tileentity;

	public ContainerCAD(EntityPlayer player, TileEntityCAD tileentity) {
		this.tileentity = tileentity;
        SimpleSlotItemHandler slotHandler = (SimpleSlotItemHandler)tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        this.addSlotToContainer(slotHandler);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		//this.tileentity.closeInventory(player);
		super.onContainerClosed(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public boolean canDragIntoSlot(Slot slot) {
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		return null;
	}

    public static class SimpleSlotItemHandler extends SlotItemHandler {

        public SimpleSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return false;
        }
    }
}
