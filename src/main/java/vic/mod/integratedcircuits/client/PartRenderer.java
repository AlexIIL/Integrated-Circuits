package vic.mod.integratedcircuits.client;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.Resources;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.Vertex5;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PartRenderer <T extends TMultiPart> implements IItemRenderer
{	
	protected List<IComponentModel> models = new LinkedList<IComponentModel>();
	
	private ModelBundledConnection[] bundledModels = new ModelBundledConnection[4];
	private ModelRedstoneConnection[] redstoneModels = new ModelRedstoneConnection[4];
	
	public PartRenderer()
	{
		models.add(new BaseModel());
	}
	
	public void addBundledConnections(int flag1, int... size)
	{
		if((flag1 & 1) != 0) bundledModels[0] = new ModelBundledConnection(0, size[0]);
		if((flag1 & 2) != 0) bundledModels[1] = new ModelBundledConnection(1, size[1]);
		if((flag1 & 4) != 0) bundledModels[2] = new ModelBundledConnection(2, size[2]);
		if((flag1 & 8) != 0) bundledModels[3] = new ModelBundledConnection(3, size[3]);
		
		for(IComponentModel m : bundledModels)
			if(m != null) models.add(m);
	}
	
	public void addRedstoneConnections(int flag1, int... size)
	{
		if((flag1 & 1) != 0) redstoneModels[3] = new ModelRedstoneConnection(0, size[0]);
		if((flag1 & 2) != 0) redstoneModels[0] = new ModelRedstoneConnection(1, size[1]);
		if((flag1 & 4) != 0) redstoneModels[1] = new ModelRedstoneConnection(2, size[2]);
		if((flag1 & 8) != 0) redstoneModels[2] = new ModelRedstoneConnection(3, size[3]);
		
		for(IComponentModel m : redstoneModels)
			if(m != null) models.add(m);
	}
	
	public void prepareBundled(int flag)
	{
		for(int i = 0; i < 4; i++)
		{
			if(bundledModels[i] == null) continue;
			bundledModels[i].rendered = (flag & 1) != 0;
			flag >>= 1;
		}
	}
	
	public void prepareRedstone(int flag1, int flag2)
	{
		for(int i = 0; i < 4; i++)
		{
			if(redstoneModels[i] == null) continue;
			redstoneModels[i].rendered = (flag1 & 1) != 0;
			redstoneModels[i].active = (flag2 & 1) != 0;
			flag1 >>= 1;
			flag2 >>= 1;
		}
	}
	
	public static interface IComponentModel
	{
		public void renderModel(Transformation t, int orient);
	}
	
	public static class BaseModel implements IComponentModel
	{
		public static CCModel[] models = new CCModel[24];
		private static CCModel base = generateModel();
		
		static
		{
			for(int i = 0; i < 24; i++) models[i] = bakeCopy(base, i);
		}

		@Override
		public void renderModel(Transformation t, int orient)
		{
			models[orient % 24].render(t, new IconTransformation(Resources.ICON_IC_BASE));
		}
		
		private static CCModel generateModel()
		{
			CCModel m1 = CCModel.quadModel(24);
			m1.generateBlock(0, 0, 0, 0, 1, 2 / 16D, 1);
			m1.computeNormals();
			shrink(m1, 0.0002);
			return m1;
		}
	}
	
	public static class ModelBundledConnection implements IComponentModel
	{
		private CCModel[] conModels = new CCModel[24];
		private final int rotation;
		private boolean rendered = true;
		
		ModelBundledConnection(int rotation, int size)
		{
			this.rotation = rotation;
			CCModel model = generateModel(size);
			for(int i = 0; i < 24; i++)
				conModels[i] = bakeCopy(model, i).shrinkUVs(0.002);
		}
	
		@Override
		public void renderModel(Transformation t, int arg1)
		{
			if(!rendered) return;
			arg1 = arg1 & 28 | ((arg1 + rotation) & 3);
			ForgeDirection dir = ForgeDirection.getOrientation((arg1 & 3) + 2);
			ForgeDirection dir1 = ForgeDirection.getOrientation((arg1 & 28) >> 2).getRotation(ForgeDirection.UP);
			boolean b = ((dir1.ordinal() % 2 == 0 ? 12 : 9) & dir.flag >> 2) > 0;
			conModels[arg1 % 24].render(t, new IconTransformation(b ? Resources.ICON_IC_WIRE_FLIPPED : Resources.ICON_IC_WIRE));
		}
		
		private CCModel generateModel(int size)
		{
			CCModel m1 = CCModel.quadModel(24);
			m1.generateBlock(0, 5 / 16D, 0.0003, 0, 11 / 16D, 4 / 16D, size / 16D);
			m1.computeNormals();
			return m1;
		}
	}

	public static class ModelRedstoneConnection implements IComponentModel
	{
		private CCModel[] conModels = new CCModel[24];
		private final int rotation;
		private boolean rendered = true;
		private boolean active = false;
		private int small;
		
		ModelRedstoneConnection(int rotation, int size)
		{
			this.rotation = rotation;
			CCModel model = generateModel(size);
			for(int i = 0; i < 24; i++)
				conModels[i] = bakeCopy(model, i).shrinkUVs(0.002);
		}
	
		@Override
		public void renderModel(Transformation t, int arg1)
		{	
			if(!rendered) return;
			arg1 = arg1 & 28 | ((arg1 + rotation) & 3);
			conModels[arg1 % 24].render(t, new IconTransformation(active ? Resources.ICON_IC_RSWIRE_ON : Resources.ICON_IC_RSWIRE_OFF));
		}
		
		private static CCModel generateModel(int size)
		{
			CCModel m1 = CCModel.quadModel(72);
			m1.generateBox(0, 0, 2, 7, size, 0.32, 2, 0, 0, 16, 16, 16);
			m1.generateBox(24, 0, 2, 6, size, 0.16, 1, 9, 0, 16, 16, 16);
			m1.generateBox(48, 0, 2, 9, size, 0.16, 1, 9, 0, 16, 16, 16);
			m1.computeNormals();
			return m1;
		}
	}
	
	/** https://github.com/MrTJP/ProjectRed/blob/master/src/mrtjp/projectred/integration/ComponentStore.java **/
	public static CCModel bakeCopy(CCModel base, int orient)
	{
		CCModel m = base.copy();
		if(orient >= 24)
		{
			for(int i = 0; i < m.verts.length; i += 4)
			{
				Vertex5 vtmp = m.verts[i + 1];
				Vector3 ntmp = m.normals()[i + 1];
				m.verts[i + 1] = m.verts[i + 3];
				m.normals()[i + 1] = m.normals()[i + 3];
				m.verts[i + 3] = vtmp;
				m.normals()[i + 3] = ntmp;
			}
		}
		
		Transformation t = Rotation.sideOrientation(orient % 24 >> 2, orient & 3);
		if(orient >= 24) t = new Scale(-1, 1, 1).with(t);
		
		m.apply(t.at(Vector3.center)).computeLighting(LightModel.standardLightModel);
		return m;
	}
	
	public static CCModel shrink(CCModel model, double inset)
	{
		for(int i = 0; i < model.verts.length; i++)
			model.verts[i].vec.subtract(model.normals()[i].copy().multiply(inset));
		return model;
	}

	public void prepare(T part) 
	{
		
	}
	
	public void prepareInv(ItemStack stack)
	{
		
	}
	
	public void prepareDynamic(T part, float partialTicks) 
	{
		
	}
	
	public void renderStatic(Transformation t, int orient)
	{
		for(IComponentModel m : models) m.renderModel(t, orient);
	}
	
	public void renderDynamic(Transformation t)
	{
		
	}
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) 
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) 
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) 
	{
		switch (type) {
		case ENTITY : renderPart(item, -0.3F, 0F, -0.3F, 0.6F); break;
		case EQUIPPED : renderPart(item, 0.0F, 0.15F, 0.0F, 1.0F); break;
		case EQUIPPED_FIRST_PERSON : renderPart(item, 1.0F, -0.2F, -0.4f, 2.0F); break;
		case INVENTORY : renderPart(item, 0.0F, 0.2F, 0.0F, 1.0F); break;
		default: break;
		}
	}
	
	private void renderPart(ItemStack stack, float x, float y, float z, float scale)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.setDynamic();
		CCRenderState.pullLightmap();
		prepareInv(stack);
		CCRenderState.startDrawing();
		renderStatic(new Scale(scale).with(new Translation(x, y, z)), 0);
		CCRenderState.draw();
		renderDynamic(new Scale(scale).with(new Translation(x, y, z)));
	}
}
