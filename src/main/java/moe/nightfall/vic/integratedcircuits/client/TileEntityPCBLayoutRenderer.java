package moe.nightfall.vic.integratedcircuits.client;

import moe.nightfall.vic.integratedcircuits.DiskDrive;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileEntityPCBLayoutRenderer extends TileEntitySpecialRenderer<TileEntityCAD> {

	public void renderTileEntityAt(TileEntityCAD te, double x, double y, double z, float partialTicks) {
		DiskDrive.renderFloppy(te, x, y, z, partialTicks, te.rotation);
	}

    @Override
    public void renderTileEntityAt(TileEntityCAD te, double x, double y, double z, float partialTicks, int destroyStage) {
        renderTileEntityAt((TileEntityCAD) te, x, y, z, partialTicks);
    }
}