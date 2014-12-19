package vic.mod.integratedcircuits.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.ic.CircuitProperties;
import vic.mod.integratedcircuits.part.PartCircuit;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** https://github.com/MrTJP/ProjectRed/ **/
@SideOnly(Side.CLIENT)
public class PartCircuitRenderer extends PartRenderer<PartCircuit>
{
	public static IIcon iconIC;
	
	public PartCircuitRenderer()
	{
		models.add(new ChipModel());
		addBundledConnections(15, 2, 2, 2, 2);
		addRedstoneConnections(15, 2, 2, 2, 2);
	}

	public static class ChipModel implements IComponentModel
	{
		private static CCModel[] models = new CCModel[24];
		private static CCModel base = generateModel();
		
		static
		{
			for(int i = 0; i < 24; i++) models[i] = bakeCopy(base, i);
		}

		@Override
		public void renderModel(Transformation arg0, int arg1)
		{
			models[arg1 % 24].render(arg0, new IconTransformation(iconIC));
		}
		
		private static CCModel generateModel()
		{
			CCModel m1 = CCModel.quadModel(24);
			m1.generateBlock(0, 0, 0, 0, 12 / 16D, 3 / 16D, 12 / 16D);
			m1.apply(new Translation(2 / 16D, 2 / 16D, 2 / 16D));
			m1.computeNormals();
			return m1;
		}
	}
	
	private byte tier;
	private String name = "NO_NAME";

	@Override
	public void prepare(PartCircuit part) 
	{
		CircuitProperties prop = part.getCircuitData().getProperties();
		int bundled = 0;
		for(int i = 0; i < 4; i++)
			bundled |= prop.getModeAtSide((i + 2) % 4) == CircuitProperties.BUNDLED ? 1 << i : 0;
		prepareBundled(bundled);
		prepareRedstone(~bundled, part.io);
	}
	
	@Override
	public void prepareInv(ItemStack stack)
	{
		NBTTagCompound comp = stack.getTagCompound();	
		if(comp == null) return;
		NBTTagCompound comp2 = comp.getCompoundTag("circuit").getCompoundTag("properties");
		byte con = comp2.getByte("con");
		
		int bundled = 0;
		for(int i = 0; i < 4; i++)
			bundled |= (con >> ((i + 2) % 4) * 2 & 3) == CircuitProperties.BUNDLED ? 1 << i : 0;
		prepareBundled(bundled);
		prepareRedstone(~bundled, 0);
		
		name = comp2.getString("name");
		tier = (byte) (comp.getCompoundTag("circuit").getInteger("size") / 16);
	}
	
	@Override
	public void prepareDynamic(PartCircuit part, float partialTicks) 
	{
		tier = (byte)(part.circuitData.getSize() / 16);
		name = part.circuitData.getProperties().getName();
	}

	@Override
	public void renderDynamic(Transformation t)
	{
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPushMatrix();
		t.glApply();
		GL11.glRotatef(90, 1, 0, 0);
		GL11.glRotatef(180, 0, 0, 1);
		GL11.glTranslated(-13 / 16D, -5 / 16D, -5.005 / 16D);
		
		FontRenderer fr = RenderManager.instance.getFontRenderer();
		if(fr == null) return;
		
		GL11.glPushMatrix();
		GL11.glScaled(1 / 64D, 1 / 64D, 1 / 64D);
		fr.drawString("T" + tier, 0, 0, 0xFFFFFF);
		GL11.glPopMatrix();
		
		GL11.glTranslated(0, -4 / 16D, 0);
		GL11.glScaled(1 / 64D, 1 / 64D, 1 / 64D);
		
		int w = fr.getStringWidth(name);
		int mw = 42;
		fr.drawString(name, (int)(mw / 2F - w / 2F), 0, 0xFFFFFF);
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	public void registerIcons(IIconRegister arg0) 
	{
		super.registerIcons(arg0);
		iconIC = arg0.registerIcon(IntegratedCircuits.modID + ":ic");
	}
}
