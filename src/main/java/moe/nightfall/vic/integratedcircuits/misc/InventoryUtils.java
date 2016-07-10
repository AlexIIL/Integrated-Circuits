package moe.nightfall.vic.integratedcircuits.misc;

import moe.nightfall.vic.integratedcircuits.tile.TileEntityContainer;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryUtils {
	public static ItemStack tryFetchItem(TileEntityInventory te, ItemStack stack, int from, int to) {
		for (int i = from; i <= to; i++) {
			ItemStack stack2 = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(i);
			if (stack2 == null)
				continue;
			if (stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2)) {
				if (stack2.stackSize >= stack.stackSize) {
					te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(i, stack.stackSize, false);
					return stack;
				}
			}
		}
		return null;
	}

	public static boolean tryPutItem(TileEntityInventory te, ItemStack stack, int from, int to) {
		if (stack == null)
			return true;
		for (int i = from; i <= to; i++) {
			ItemStack stack2 = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(i);
			if (stack2 != null && stack.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack, stack2)) {
				if (stack2.getMaxStackSize() >= stack2.stackSize + stack.stackSize) {
					stack2.stackSize += stack.stackSize;
					te.onSlotChange(i);
					return true;
				}
			}
		}
		for (int i = from; i <= to; i++) {
			ItemStack stack2 = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(i);
			if (stack2 == null) {
				((ItemStackHandler)te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)).setStackInSlot(i, stack);
				return true;
			}
		}
		return false;
	}

	public static ItemStack getFirstItem(Item item, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() == item)
				return stack;
		}
		return null;
	}

	public static int getSlotIndex(ItemStack stack, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			if (inventory.getStackInSlot(i) == stack)
				return i;
		return -1;
	}
}
