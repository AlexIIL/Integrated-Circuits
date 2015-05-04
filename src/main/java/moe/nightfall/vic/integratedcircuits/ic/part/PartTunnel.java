package moe.nightfall.vic.integratedcircuits.ic.part;

import moe.nightfall.vic.integratedcircuits.ic.CircuitPart;
import moe.nightfall.vic.integratedcircuits.ic.CircuitPartRenderer.EnumRenderType;
import moe.nightfall.vic.integratedcircuits.ic.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraftforge.common.util.ForgeDirection;

public class PartTunnel extends CircuitPart {

	public final IntProperty PROP_POS_X = new IntProperty("PROP_POS_X", stitcher, 255);
	public final IntProperty PROP_POS_Y = new IntProperty("PROP_POS_Y", stitcher, 255);

	private Vec2 getConnectedPos(Vec2 pos, ICircuit parent) {
		return new Vec2(getProperty(pos, parent, PROP_POS_X), getProperty(pos, parent, PROP_POS_Y));
	}

	private boolean isConnected(Vec2 pos) {
		return pos.x != 255 && pos.y != 255;
	}

	private PartTunnel getConnectedPart(Vec2 pos, ICircuit parent) {
		if (isConnected(pos)) {
			CircuitPart cp = parent.getCircuitData().getPart(pos);
			if (cp instanceof PartTunnel) {
				return (PartTunnel) cp;
			} else {
				// Reset back to default, unlinked state
				setProperty(pos, parent, PROP_POS_X, 255);
				setProperty(pos, parent, PROP_POS_Y, 255);
			}
		}
		return null;
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		Vec2 pos2 = getConnectedPos(pos, parent);
		if (isConnected(pos2) && getInput(pos, parent)) {
			PartTunnel part = getConnectedPart(pos2, parent);
			if (part != null) {
				part.onInputChange(pos2, parent, ForgeDirection.UNKNOWN);
			}
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		boolean output = getInput(pos, parent);
		if (!output) {
			Vec2 pos2 = getConnectedPos(pos, parent);
			PartTunnel part = getConnectedPart(pos2, parent);
			if (part != null) {
				output = part.getInput(pos2, parent);
			}
		}
		return output && !getInputFromSide(pos, parent, side.getOpposite());
	}

	@Override
	public void onPlaced(Vec2 pos, ICircuit parent) {
		setProperty(pos, parent, PROP_POS_X, 255);
		setProperty(pos, parent, PROP_POS_Y, 255);
		super.onPlaced(pos, parent);
	}

	@Override
	public void renderPart(Vec2 pos, ICircuit parent, double x, double y, EnumRenderType type) {

	}
}
