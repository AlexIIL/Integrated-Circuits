package moe.nightfall.vic.integratedcircuits.cp.part;

import java.util.ArrayList;

import moe.nightfall.vic.integratedcircuits.cp.ICircuit;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IntProperty;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;

public abstract class Part2I1O extends PartSimpleGate {
	public final IntProperty PROP_CONNECTORS = new IntProperty("CONNECTORS", stitcher, 2);

	@Override
	public void onClick(Vec2i pos, ICircuit parent, int button, boolean ctrl) {
		if (button == 0 && ctrl) {
			cycleProperty(pos, parent, PROP_CONNECTORS);
			scheduleInputChange(pos, parent);
		}
		super.onClick(pos, parent, button, ctrl);
	}

	@Override
	public boolean canConnectToSide(Vec2i pos, ICircuit parent, EnumFacing side) {
		EnumFacing s2 = toInternal(pos, parent, side);
		if (s2 == EnumFacing.NORTH)
			return true;
		int i = getProperty(pos, parent, PROP_CONNECTORS);
		if (s2 == EnumFacing.EAST && i == 2)
			return false;
		if (s2 == EnumFacing.SOUTH && i == 0)
			return false;
		if (s2 == EnumFacing.WEST && i == 1)
			return false;
		return true;
	}

	@Override
	protected boolean hasOutputToSide(Vec2i pos, ICircuit parent, EnumFacing fd) {
		return fd == EnumFacing.NORTH;
	}

	@Override
	public ArrayList<String> getInformation(Vec2i pos, ICircuit parent, boolean edit, boolean ctrlDown) {
		ArrayList<String> text = super.getInformation(pos, parent, edit, ctrlDown);
		if (edit && ctrlDown)
			text.add(I18n.format("gui.integratedcircuits.cad.mode"));
		return text;
	}
}
