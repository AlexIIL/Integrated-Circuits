package moe.nightfall.vic.integratedcircuits.client;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import moe.nightfall.vic.integratedcircuits.DiskDrive;
import moe.nightfall.vic.integratedcircuits.LaserHelper;
import moe.nightfall.vic.integratedcircuits.DiskDrive.ModelFloppy;
import moe.nightfall.vic.integratedcircuits.LaserHelper.Laser;
import moe.nightfall.vic.integratedcircuits.client.model.ModelLaser;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.misc.RenderUtils;
import moe.nightfall.vic.integratedcircuits.proxy.ClientProxy;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class TileEntityAssemblerRenderer extends TileEntitySemiTransparentRenderer {
	private static ModelFloppy model = new ModelFloppy(-7, -7, -9, 12, 2, 1);

	// Used to unload the FBOs when the world does. If there is a better way to
	// do this, tell me.
	public static List<Integer> textureList = Lists.newLinkedList();
	private static List<TileEntityAssembler> schedule = Lists.newLinkedList();
	private static Framebuffer fbo;

	public TileEntityAssemblerRenderer() {
		FMLCommonHandler.instance().bus().register(this);
	}

	public void renderTileEntityAt(TileEntityAssembler te, double x, double y, double z, float partialTicks) {
		if (getCurrentRenderPass() == 0)
			DiskDrive.renderFloppy(te, model, x, y, z, partialTicks, te.rotation);

		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(-90 * te.rotation, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		GL11.glColor3f(1, 1, 1);

		if (getCurrentRenderPass() == 0) {
			GL11.glPushMatrix();
			Tessellator tes = Tessellator.instance;
			this.bindTexture(Resources.RESOURCE_ASSEMBLER_BOTTOM);
			tes.startDrawingQuads();
			tes.setNormal(0, 1, 0);
			tes.addVertexWithUV(0, 8 / 16F, 0, 0, 0);
			tes.addVertexWithUV(0, 8 / 16F, 1, 0, 1);
			tes.addVertexWithUV(1, 8 / 16F, 1, 1, 1);
			tes.addVertexWithUV(1, 8 / 16F, 0, 1, 0);
			tes.draw();

			if (te.circuitTexture != -1 && te.isOccupied) {
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glColor3f(0, 0.1F, 0);

				tes.startDrawingQuads();
				// NORTH
				tes.setNormal(0, 0, -1);
				tes.addVertex(3 / 16F, 8 / 16F, 3 / 16F);
				tes.addVertex(3 / 16F, 8 / 16F + 1 / 40F, 3 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F + 1 / 40F, 3 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F, 3 / 16F);

				// SOUTH
				tes.setNormal(0, 0, 1);
				tes.addVertex(3 / 16F, 8 / 16F, 13 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F, 13 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F + 1 / 40F, 13 / 16F);
				tes.addVertex(3 / 16F, 8 / 16F + 1 / 40F, 13 / 16F);

				// EAST
				tes.setNormal(-1, 0, 0);
				tes.addVertex(3 / 16F, 8 / 16F, 13 / 16F);
				tes.addVertex(3 / 16F, 8 / 16F + 1 / 40F, 13 / 16F);
				tes.addVertex(3 / 16F, 8 / 16F + 1 / 40F, 3 / 16F);
				tes.addVertex(3 / 16F, 8 / 16F, 3 / 16F);

				// WEST
				tes.setNormal(1, 0, 0);
				tes.addVertex(13 / 16F, 8 / 16F, 3 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F + 1 / 40F, 3 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F + 1 / 40F, 13 / 16F);
				tes.addVertex(13 / 16F, 8 / 16F, 13 / 16F);
				tes.draw();

				GL11.glColor3f(1, 1, 1);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, te.circuitTexture);

				tes.startDrawingQuads();
				tes.setNormal(0, 1, 0);
				tes.addVertexWithUV(3 / 16F, 8 / 16F + 1 / 40F, 3 / 16F, 1, 0);
				tes.addVertexWithUV(3 / 16F, 8 / 16F + 1 / 40F, 13 / 16F, 1, 1);
				tes.addVertexWithUV(13 / 16F, 8 / 16F + 1 / 40F, 13 / 16F, 0, 1);
				tes.addVertexWithUV(13 / 16F, 8 / 16F + 1 / 40F, 3 / 16F, 0, 0);
				tes.draw();
			}

			GL11.glRotatef(180, 0, 0, 1);
			GL11.glTranslatef(-1.005F, -1, 0);
			this.bindTexture(Resources.RESOURCE_ASSEMBLER_SAFETY);
			tes.startDrawingQuads();
			tes.setNormal(1, 0, 0);
			tes.addVertexWithUV(0, 9 / 16F, 1 - 7 / 16F, 0, 0);
			tes.addVertexWithUV(0, 1 - 3 / 16F, 1 - 7 / 16F, 0, 1);
			tes.addVertexWithUV(0, 1 - 3 / 16F, 0 + 1 / 16F, 1, 1);
			tes.addVertexWithUV(0, 9 / 16F, 0 + 1 / 16F, 1, 0);
			tes.draw();
			GL11.glPopMatrix();
		}

		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 14 / 16F, 0.5F);
		GL11.glRotatef(45, 0, 1, 0);

		LaserHelper laserHelper = te.laserHelper;
		for (int i = 0; i < 4; i++) {
			Laser laser = laserHelper.getLaser(i);
			if (laser == null)
				continue;
			if (getCurrentRenderPass() == 0 && te.refMatrix != null)
				laser.update(partialTicks);

			GL11.glPushMatrix();
			GL11.glRotatef(90 * i, 0, 1, 0);
			GL11.glTranslatef(0, 0, -0.5F);
			GL11.glRotatef(-90, 0, 1, 0);

			boolean active = laser.isActive && laser.isRunning;
			if (getCurrentRenderPass() > 0) {
				if (active && te.getStatus() == te.RUNNING)
					renderLaser(1 / 64F, -laser.iY, laser.iZ, laser.length, te, partialTicks);
			} else {
				if (active)
					laser.iX = (float) Math.toRadians((float) ClientProxy.clientTicks * 4 + partialTicks * 4);
				ModelLaser.instance.render(1 / 64F, -laser.iY, laser.iZ, active, laser.iX, partialTicks, te);
			}
			GL11.glPopMatrix();
		}

		GL11.glPopMatrix();
		if (getCurrentRenderPass() == 0)
			addToRenderQueue(te.xCoord, te.yCoord, te.zCoord);
		GL11.glPopMatrix();
	}

	private Random rand = new Random();

	private void renderLaser(float scale, float aZ, float aY, float length, TileEntityAssembler te, float partialTicks) {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		RenderUtils.setBrightness(240, 240);

		GL11.glPushMatrix();
		if (length > 0) {
			GL11.glRotatef(aY, 0, 1, 0);
			GL11.glRotatef(aZ, 0, 0, 1);

			double offset = Math.abs(rand.nextGaussian());

			Tessellator tes = Tessellator.instance;
			GL11.glScalef(scale, scale, scale);
			for (int j = 0; j < 4; j++) {
				float color = (float) (0.75 + offset);
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
		RenderUtils.resetBrightness();

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
		this.renderTileEntityAt((TileEntityAssembler) te, x, y, z, partialTicks);
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == Phase.START && schedule.size() > 0) {
			int currentFBO = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
			if (fbo == null)
				fbo = new Framebuffer(256, 256, false);
			fbo.unbindFramebuffer();

			fbo.bindFramebuffer(false);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glViewport(0, 0, 256, 256);
			GL11.glOrtho(0, 256, 256, 0, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			for (TileEntityAssembler te : schedule)
				updateFramebuffer(te);
			schedule.clear();

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();

			fbo.unbindFramebuffer();
			OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, currentFBO);
		}
	}

	private void updateFramebuffer(TileEntityAssembler te) {
		if (te.circuitTexture == -1) {
			te.circuitTexture = fbo.framebufferTexture = TextureUtil.glGenTextures();
			textureList.add(te.circuitTexture);
			fbo.setFramebufferFilter(GL11.GL_NEAREST);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.framebufferTexture);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, fbo.framebufferTextureWidth,
					fbo.framebufferTextureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, fbo.framebufferObject);
		}

		fbo.framebufferTexture = te.circuitTexture;
		OpenGlHelper.func_153188_a(OpenGlHelper.field_153198_e, OpenGlHelper.field_153200_g, GL11.GL_TEXTURE_2D,
				fbo.framebufferTexture, 0);

		GL11.glColor3f(0, 0.1F, 0);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.addVertex(0, 0, 0);
		tes.addVertex(0, 256, 0);
		tes.addVertex(256, 256, 0);
		tes.addVertex(256, 0, 0);
		tes.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);

		if (te.excMatrix != null && te.cdata != null) {
			GL11.glScalef(16 / (float) te.cdata.getSize(), 16 / (float) te.cdata.getSize(), 1);
			CircuitPartRenderer.renderParts(new CircuitRenderWrapper(te.cdata), 0, 0, te.excMatrix, te.size > 16 ? CircuitPartRenderer.EnumRenderType.WORLD_16x
					: CircuitPartRenderer.EnumRenderType.WORLD);
		}
	}

	public static void scheduleFramebuffer(TileEntityAssembler te) {
		if (!schedule.contains(te))
			schedule.add(te);
	}
}
