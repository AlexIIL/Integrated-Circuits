package moe.nightfall.vic.integratedcircuits.cp.part;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Content;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.CircuitRenderWrapper;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer.EnumRenderType;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.*;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;

public class PartTunnel extends CircuitPart {

	public final IntProperty PROP_POS_X = new IntProperty("PROP_POS_X", stitcher, 255);
	public final IntProperty PROP_POS_Y = new IntProperty("PROP_POS_Y", stitcher, 255);
	public final BooleanProperty PROP_IN = new BooleanProperty("PROP_IN", stitcher);

	// pos is for CURRENT part
	public Vec2i getConnectedPos(Vec2i pos, ICircuit parent) {
		return new Vec2i(getProperty(pos, parent, PROP_POS_X), getProperty(pos, parent, PROP_POS_Y));
	}

	// pos is for CONNECTED part
	public int setConnectedPos(int data, Vec2i pos) {
		data = PROP_POS_X.set(pos.x, data);
		data = PROP_POS_Y.set(pos.y, data);
		return data;
	}

	// pos is for CONNECTED part
	public boolean isConnected(Vec2i pos) {
		return pos.x != 255 && pos.y != 255;
	}

	// pos is for CURRENT part, not connected one, like with getNeighbourOnSide
	public PartTunnel getConnectedPart(Vec2i pos, ICircuit parent) {
		Vec2i pos2 = getConnectedPos(pos, parent);
		if (isConnected(pos2)) {
			CircuitPart cp = parent.getCircuitData().getPart(pos2);
			if (cp instanceof PartTunnel) {
				return (PartTunnel) cp;
			} else {
				// Reset back to default, unlinked state
				setProperty(pos, parent, PROP_POS_X, 255);
				setProperty(pos, parent, PROP_POS_Y, 255);
				setProperty(pos, parent, PROP_IN, false);
				markForUpdate(pos, parent);
			}
		}
		return null;
	}

	@Override
	public void onInputChange(Vec2i pos, ICircuit parent) {
		// updateInput analog for paired tunnel part
		Vec2i pos2 = getConnectedPos(pos, parent);
		PartTunnel part = getConnectedPart(pos, parent);
		setProperty(pos, parent, PROP_IN, part == null ? false : part.getOutputToSide(pos2, parent, null));

		notifyNeighbours(pos, parent);
		// notifyNeighbors analog for paired tunnel
		if (part != null && getOutputToSide(pos, parent, null) != part.getProperty(pos2, parent, PROP_IN)) {
			// Unlike notifyNeighbors, nothing can be done here after disconnect from paired tunnel.
			part.scheduleInputChange(pos2, parent);
			part.markForUpdate(pos2, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		boolean in = getProperty(pos, parent, PROP_IN);
		if (side == null)
			return getInput(pos, parent) && !in;
		return (getInput(pos, parent) || in) && !getInputFromSide(pos, parent, side);
	}

	@Override
	public void onPlaced(Vec2i pos, ICircuit parent) {
		setProperty(pos, parent, PROP_POS_X, 255);
		setProperty(pos, parent, PROP_POS_Y, 255);
		setProperty(pos, parent, PROP_IN, false);
		scheduleInputChange(pos, parent);
	}

	// Used to update previously connected tunnel when required
	private void dropConnected(Vec2i pos, ICircuit parent, Vec2i oldPos2) {
		if (isConnected(oldPos2)) {
			CircuitPart cp = parent.getCircuitData().getPart(oldPos2);
			if (cp instanceof PartTunnel) {
				PartTunnel oldPart = (PartTunnel) cp;
				if (pos.equals(oldPart.getConnectedPos(oldPos2, parent))) {
					oldPart.setProperty(oldPos2, parent, PROP_POS_X, 255);
					oldPart.setProperty(oldPos2, parent, PROP_POS_Y, 255);
					// Like notifyNeighbours after disconnect from neighbour
					if (oldPart.getProperty(oldPos2, parent, PROP_IN))
						oldPart.scheduleInputChange(oldPos2, parent);
					oldPart.markForUpdate(oldPos2, parent);
				}
			}
		}
	}

	@Override
	public void onChanged(Vec2i pos, ICircuit parent, int oldMeta) {
		// Update previously connected tunnel, if required
		Vec2i pos2 = getConnectedPos(pos, parent);
		Vec2i oldPos2 = new Vec2i(PROP_POS_X.get(oldMeta), PROP_POS_Y.get(oldMeta));
		if (!pos2.equals(oldPos2))
			dropConnected(pos, parent, oldPos2);
		
		scheduleInputChange(pos, parent);
	}

	@Override
	public void onRemoved(Vec2i pos, ICircuit parent) {
		// Update connected tunnel, if required
		dropConnected(pos, parent, getConnectedPos(pos, parent));
	}

	@Override
	public void renderPart(Vec2i pos, ICircuit parent, double x, double y, EnumRenderType type) {
		RenderManager rm = RenderManager.getInstance();

		RenderUtils.applyColorIRGBA(rm, Config.colorGreen);
		rm.addQuad(x, y, 16, 4 * 16, 16, 16);
		if (getInput(pos, parent) || getProperty(pos, parent, PROP_IN)) {
			RenderUtils.applyColorIRGBA(rm, Config.colorGreen);
		} else {
			RenderUtils.applyColorIRGBA(rm, Config.colorGreen, 0.4F);
		}
		rm.addQuad(x, y, 0, 4 * 16, 16, 16);
	}

	@Override
	public Category getCategory() {
		return Category.WIRE;
	}

	@Override
	public String getLocalizedName(Vec2i pos, ICircuit parent) {
		String name = super.getLocalizedName(pos, parent);
		if (!(parent instanceof CircuitRenderWrapper) && isConnected(getConnectedPos(pos, parent))) {
			name += " (Linked)";
		}
		return name;
	}

	@Override
	public void getCraftingCost(CraftingAmount amount, CircuitData parent, Vec2i pos) {
		amount.add(new ItemAmount(Items.REDSTONE, 0.1));
		amount.add(new ItemAmount(Content.itemSiliconDrop, 0.1));

		int data = parent.getMeta(pos);
		Vec2i end = new Vec2i(PROP_POS_X.get(data), PROP_POS_Y.get(data));
		if (isConnected(end)) {
			amount.add(new ItemAmount(Items.REDSTONE, 0.1 * pos.distanceTo(end)));
		}
	}
}
