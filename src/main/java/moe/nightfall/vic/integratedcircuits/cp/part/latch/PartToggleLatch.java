package moe.nightfall.vic.integratedcircuits.cp.part.latch;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPartRenderer;
import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

public class PartToggleLatch extends PartLatch {
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) {
		super.onClick(pos, parent, button, ctrl);
		if (button == 0 && ctrl) {
			invertProperty(pos, parent, PROP_TMP);
			notifyNeighbours(pos, parent);
		}
	}

	@Override
	public void onInputChange(Vec2 pos, ICircuit parent, ForgeDirection side) {
		super.onInputChange(pos, parent, side);
		ForgeDirection s2 = toInternal(pos, parent, side);
		if ((s2 == ForgeDirection.NORTH || s2 == ForgeDirection.SOUTH)) {
			if (getInputFromSide(pos, parent, side))
				invertProperty(pos, parent, PROP_OUT);
			scheduleTick(pos, parent);
		}
	}

	@Override
	public boolean getOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection side) {
		ForgeDirection s2 = toInternal(pos, parent, side);
		if (s2 == ForgeDirection.EAST)
			return getProperty(pos, parent, PROP_TMP);
		if (s2 == ForgeDirection.WEST)
			return !getProperty(pos, parent, PROP_TMP);
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec2 getTextureOffset(Vec2 pos, ICircuit parent, double x, double y, CircuitPartRenderer.EnumRenderType type) {
		return new Vec2(8, 1);
	}

	@Override
	public ArrayList<String> getInformation(Vec2 pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.toggle"));
		return text;
	}
}