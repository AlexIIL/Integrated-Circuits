package vic.mod.integratedcircuits.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import vic.mod.integratedcircuits.gate.PartGate;

public class ItemCircuit extends ItemPartGate
{
	public ItemCircuit(PartGate part, boolean isMultiPart)
	{
		super("circuit", part, isMultiPart);
		setMaxStackSize(1);
		setCreativeTab(null);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) 
	{
		if(stack.getTagCompound() == null) I18n.format(getUnlocalizedName() + ".name", "INVALID!");
		String name = stack.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name");
		return I18n.format(getUnlocalizedName() + ".name", name);
	}
}
