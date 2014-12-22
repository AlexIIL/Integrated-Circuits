package vic.mod.integratedcircuits.ic.part;

import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

public abstract class Part3I1O extends PartSimpleGate
{
	public final IntProperty PROP_CONNECTORS = new IntProperty(stitcher, 3);
	
	@Override
	public void onClick(Vec2 pos, ICircuit parent, int button, boolean ctrl) 
	{
		if(button == 0 && ctrl)
			cycleProperty(pos, parent, PROP_CONNECTORS);
		super.onClick(pos, parent, button, ctrl);
	}
	
	@Override
	public boolean canConnectToSide(Vec2 pos, ICircuit parent, ForgeDirection side) 
	{
		ForgeDirection s2 = toInternal(pos, parent, side);
		if(s2 == ForgeDirection.NORTH) return true;
		int i = getProperty(pos, parent, PROP_CONNECTORS);
		if(s2 == ForgeDirection.EAST && i == 1) return false;
		if(s2 == ForgeDirection.SOUTH && i == 2) return false;
		if(s2 == ForgeDirection.WEST && i == 3) return false;
		return true;
	}

	@Override
	protected boolean hasOutputToSide(Vec2 pos, ICircuit parent, ForgeDirection fd) 
	{
		return fd == ForgeDirection.NORTH;
	}
}