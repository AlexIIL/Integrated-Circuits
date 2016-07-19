package moe.nightfall.vic.integratedcircuits.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import net.minecraft.item.Item;

public class ItemBase extends Item {
	private boolean hasIcon = true;

	public ItemBase(String name) {
		register(this, name);
	}

	static void register(Item item, String name) {
		item.setCreativeTab(IntegratedCircuits.creativeTab);
		item.setUnlocalizedName(Constants.MOD_ID + "." + name);
		item.setRegistryName(name);

		GameRegistry.register(item);
	}

	public ItemBase setHasIcon(boolean hasIcon) {
		this.hasIcon = hasIcon;
		return this;
	}

	public void registerIcons() {
		if (hasIcon)
			ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName().toString()));
	}
}
