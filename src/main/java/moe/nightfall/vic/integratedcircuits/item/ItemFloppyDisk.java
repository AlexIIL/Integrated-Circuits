package moe.nightfall.vic.integratedcircuits.item;

import java.util.List;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.misc.MiscUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.util.StatCollector;

public class ItemFloppyDisk extends ItemBase
{
	public ItemFloppyDisk()
	{
		super("floppy");
		setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) 
	{
		NBTTagCompound comp = stack.getTagCompound();
		if(comp != null && comp.hasKey("circuit"))
		{
			comp = comp.getCompoundTag("circuit");
			Integer size = comp.getInteger("size");
			itemInformation.add(ChatFormatting.GRAY + StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".tooltip.name", ChatFormatting.WHITE + comp.getCompoundTag("properties").getString("name")));
			itemInformation.add(ChatFormatting.GRAY + StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".tooltip.size", "" + ChatFormatting.WHITE + size + "x" + size));
			itemInformation.add(ChatFormatting.GRAY + StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".tooltip.author", ChatFormatting.WHITE + comp.getCompoundTag("properties").getString("author")));
		} else if (Config.enableTooltips) {
			itemInformation.addAll(MiscUtils.appendToAll(ChatFormatting.GRAY + "" + ChatFormatting.ITALIC, MiscUtils.splitTranslateToLocalFormatted(getUnlocalizedName() + ".tooltip.info")));
		}
	}
}
