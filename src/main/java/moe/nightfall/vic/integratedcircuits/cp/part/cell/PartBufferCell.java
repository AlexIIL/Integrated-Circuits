package moe.nightfall.vic.integratedcircuits.cp.part.cell;

import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartSimpleGate;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PartBufferCell extends PartSimpleGate {
	@Override
	public Category getCategory() {
		return Category.CELL;
	}

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		super.onInputChange(pos, parent);
		notifyNeighbours(pos, parent);
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		// A-la NullCell
		if (getInputFromSide(pos, parent, side.getOpposite()) && !getInputFromSide(pos, parent, side))
			return true;
		
		// But also works as BufferGate
		return super.getOutputToSide(pos, parent, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPart(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		CircuitPartRenderer.renderPartCell(pos, parent, this, x, y, type);

		Vec2i textureOffset = getTextureOffset(pos, parent, x, y, type);
		CircuitPartRenderer.addQuad(x, y, textureOffset.x * 16, textureOffset.y * 16, 16, 16,  this.getRotation(pos, parent));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2i getTextureOffset(Vec2i pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2i(6, 2);
	}

	@Override
	protected void calcOutput(Vec2i pos, ICircuit parent) {
		EnumFacing east = toExternal(pos, parent, EnumFacing.EAST);
		setOutput(pos, parent,
			getInputFromSide(pos, parent, east) || getInputFromSide(pos, parent, east.getOpposite()));
	}

	@Override
	protected boolean hasOutputToSide(Vec2i pos, ICircuit parent, EnumFacing fd) {
		return fd == EnumFacing.NORTH || fd == EnumFacing.SOUTH;
	}
}
