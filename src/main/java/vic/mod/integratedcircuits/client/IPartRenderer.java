package vic.mod.integratedcircuits.client;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import vic.mod.integratedcircuits.client.model.IComponentModel;
import codechicken.lib.vec.Transformation;

public interface IPartRenderer<T> extends IItemRenderer
{
	public List<IComponentModel> getModels();
	
	public void prepare(T part);
	
	public void prepareInv(ItemStack stack);
	
	public void prepareDynamic(T part, float partialTicks);
	
	public void renderStatic(Transformation t, int orient);
	
	public void renderDynamic(Transformation t);
}
