package vic.mod.integratedcircuits.client;

import java.util.Random;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vic.mod.integratedcircuits.DiskDriveUtils;
import vic.mod.integratedcircuits.DiskDriveUtils.ModelFloppy;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.LaserHelper;
import vic.mod.integratedcircuits.LaserHelper.Laser;
import vic.mod.integratedcircuits.TileEntityAssembler;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import vic.mod.integratedcircuits.util.RenderUtils;

public class TileEntityAssemblerRenderer extends TileEntitySemiTransparentRenderer
{
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);
	
	private ResourceLocation safetyRegulationsTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_safety.png");
	private ResourceLocation bottomTex = new ResourceLocation(IntegratedCircuits.modID, "textures/blocks/assembler_bottom.png");
	
	public void renderTileEntityAt(TileEntityAssembler te, double x, double y, double z, float partialTicks)
	{	
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(-90 * te.rotation, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		
		if(getCurrentRenderPass() == 0)
		{
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPushMatrix();
			Tessellator tes = Tessellator.instance;
			this.bindTexture(bottomTex);
			tes.startDrawingQuads();
			tes.addVertexWithUV(0, 8 / 16F, 0, 0, 0);
			tes.addVertexWithUV(0, 8 / 16F, 1, 0, 1);
			tes.addVertexWithUV(1, 8 / 16F, 1, 1, 1);
			tes.addVertexWithUV(1, 8 / 16F, 0, 1, 0);
			tes.draw();
			
			if(te.refMatrix != null && te.verts != null)
			{
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glPushMatrix();
				
				GL11.glTranslatef(0.5F, 0, 0.5F);
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glTranslatef(-0.5F, 0, -0.5F);
				
				GL11.glTranslatef(-1 + 3 / 16F, 0, -1 + 3 / 16F);
				GL11.glTranslatef(1, 8 / 16F, 1);
				float s = 10 / 16F / te.size;
				GL11.glScalef(s, 1 / 80F, s);
				Tessellator verts = te.verts;
				TesselatorVertexState state = verts.getVertexState(0, 0, 0);
				verts.draw();
				verts.startDrawingQuads();
				verts.setVertexState(state);
				GL11.glPopMatrix();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
			}
			
			GL11.glRotatef(180, 0, 0, 1);
			GL11.glTranslatef(-1.005F, -1, 0);
			this.bindTexture(safetyRegulationsTex);
			tes.startDrawingQuads();
			tes.addVertexWithUV(0, 9 / 16F, 1 - 7 / 16F, 0, 0);
			tes.addVertexWithUV(0, 1 - 3 / 16F, 1 - 7 / 16F, 0, 1);
			tes.addVertexWithUV(0, 1 - 3 / 16F, 0 + 1 / 16F, 1, 1);
			tes.addVertexWithUV(0, 9 / 16F, 0 + 1 / 16F, 1, 0);
			tes.draw();
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 14 / 16F, 0.5F);
		GL11.glRotatef(45, 0, 1, 0);

		LaserHelper laserHelper = te.laserHelper;		
		for(int i = 0; i < 4; i++)
		{
			Laser laser = laserHelper.getLaser(i);
			if(laser == null) continue;
			if(getCurrentRenderPass() == 0 && te.refMatrix != null) 
				laser.update(partialTicks);
			
			GL11.glPushMatrix();
			GL11.glRotatef(90 * i, 0, 1, 0);
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);

			if(getCurrentRenderPass() > 0)
			{
				if(laser.isActive && laser.isRunning) renderLaser(1 / 64F, -laser.iY, laser.iZ, laser.length, te, partialTicks);
			}
			else 
			{
				boolean active = laser.isActive && laser.isRunning;
				if(active) laser.iX = (float)Math.toRadians((float)ClientProxy.clientTicks * 4 + partialTicks * 4);
				ModelLaser.instance.render(1 / 64F, -laser.iY, laser.iZ, active, laser.iX, partialTicks, te);
			}
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
		if(getCurrentRenderPass() == 0) addToRenderQueue(te.xCoord, te.yCoord, te.zCoord);	
		GL11.glPopMatrix();
		
		if(getCurrentRenderPass() == 0) DiskDriveUtils.renderFloppy(te, model, x, y, z, partialTicks, te.rotation);
	}
	
	private Random rand = new Random();
	
	private void renderLaser(float scale, float aZ, float aY, float length, TileEntityAssembler te, float partialTicks)
	{
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		
		GL11.glPushMatrix();
		if(length > 0) 
		{
			GL11.glRotatef(aY, 0, 1, 0);
			GL11.glRotatef(aZ, 0, 0, 1);
			
			double offset = Math.abs(rand.nextGaussian());
			
			Tessellator tes = Tessellator.instance;
			GL11.glScalef(scale, scale, scale);	
			for(int j = 0; j < 4; j++)
			{
				float color = (float)(0.75 + offset);
				tes.startDrawing(GL11.GL_QUADS);
				tes.setColorRGBA_F(color, 0, 0, 1);
				tes.addVertex(0, 0, 0);
				tes.addVertex(length / scale, 0, 0);	
				tes.setColorRGBA_F(0, 0, 0, 1);
				tes.addVertex(length / scale, 0.5, 0.5);
				tes.addVertex(0, 0.5, 0.5);
				tes.draw();
				GL11.glRotatef(90, 1, 0, 0);
			}
			GL11.glScalef(1 / scale, 1 / scale, 1 / scale);
		}
		RenderUtils.resetBrightness(te);
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks)
	{
		this.renderTileEntityAt((TileEntityAssembler)te, x, y, z, partialTicks);
	}
}
