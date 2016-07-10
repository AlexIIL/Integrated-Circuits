package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.item.ItemPCBPrint;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityPrinter extends TileEntityInventory {

	private int field = 0;
	private float inkLevel = 0F;

	public boolean hasInk() {
		return inkLevel() > 0F;
	}

	public float inkLevel() {
		return inkLevel;
	}

	public boolean hasPaper() {
		return paperCount() > 0F;
	}

	public int paperCount() {
		return inventory.getStackInSlot(0) != null ? inventory.getStackInSlot(0).stackSize : 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		inkLevel = compound.getFloat("inkLevel");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setFloat("inkLevel", inkLevel);
	}

	@Override
	public int getSlots() {
		return 1;
	}

	public boolean addInk(ItemStack stack) {
		// TODO If you ever need this again, move it to the utility functions
		if (inkLevel < 1F && stack != null && stack.getItem() == Items.DYE && stack.getItemDamage() == 0) {
			inkLevel += 0.2F;
			inkLevel = Math.min(inkLevel, 1F);
			stack.stackSize--;

			markDirty();
			return true;
		}
		return false;
	}

	public boolean addPaper(ItemStack stack) {
		// TODO Same as above
		if (stack != null && stack.getItem() == Items.PAPER && paperCount() < 16) {
			if (inventory.getStackInSlot(0) == null) {
				inventory.setStackInSlot(0, new ItemStack(Items.PAPER, stack.stackSize));
			} else {
				inventory.getStackInSlot(0).stackSize += stack.stackSize;
			}

			markDirty();

			stack.stackSize = 0;
			int over = inventory.getStackInSlot(0).stackSize - 16;
			inventory.getStackInSlot(0).stackSize = Math.min(inventory.getStackInSlot(0).stackSize, 16);
			if (over > 0) {
				stack.stackSize = over;
			}
			return true;
		}
		return false;
	}

	public void print(CircuitData cdata) {
		if (hasPaper() && inkLevel() >= 0.1F) {
			inventory.getStackInSlot(0).stackSize--;
			inkLevel -= 0.1F;
			markDirty();
			MiscUtils.dropItem(worldObj, ItemPCBPrint.create(cdata), pos.getX(), pos.getY(), pos.getZ());
			worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 0); //FIXME states, flags
		}
	}
}
