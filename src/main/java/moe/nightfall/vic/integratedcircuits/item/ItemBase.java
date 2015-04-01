package moe.nightfall.vic.integratedcircuits.item;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemBase extends Item
{
	private boolean hasIcon = true;

	public ItemBase(String name)
	{
		setCreativeTab(IntegratedCircuits.creativeTab);
		setUnlocalizedName(Constants.MOD_ID + "." + name);
		setTextureName(Constants.MOD_ID + ":" + name);
		GameRegistry.registerItem(this, Constants.MOD_ID + "_" + name, Constants.MOD_ID);
	}
	
	public ItemBase setHasIcon(boolean hasIcon)
	{
		this.hasIcon = hasIcon;
		return this;
	}

	@Override
	public void registerIcons(IIconRegister ir) 
	{
		if(hasIcon) super.registerIcons(ir);
	}
}
