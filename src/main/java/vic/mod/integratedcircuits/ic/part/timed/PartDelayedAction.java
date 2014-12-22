package vic.mod.integratedcircuits.ic.part.timed;

import vic.mod.integratedcircuits.ic.ICircuit;
import vic.mod.integratedcircuits.ic.part.PartCPGate;
import vic.mod.integratedcircuits.misc.PropertyStitcher.BooleanProperty;
import vic.mod.integratedcircuits.misc.PropertyStitcher.IntProperty;
import vic.mod.integratedcircuits.misc.Vec2;

/** Uses 8 bits for the delay. 255 ticks = 12.75 seconds*/
public abstract class PartDelayedAction extends PartCPGate
{
	public final BooleanProperty PROP_ACTIVE = new BooleanProperty(stitcher);
	public final IntProperty PROP_CURRENT_DELAY = new IntProperty(stitcher, 255);
	
	protected abstract int getDelay(Vec2 pos, ICircuit parent);
	
	public int getCurrentDelay(Vec2 pos, ICircuit parent)
	{
		return getProperty(pos, parent, PROP_CURRENT_DELAY);
	}
	
	@Override
	public void onScheduledTick(Vec2 pos, ICircuit parent) 
	{
		if(getProperty(pos, parent, PROP_ACTIVE))
		{
			int counter = getCurrentDelay(pos, parent);
			counter--;
			if(counter == 0)
			{
				setProperty(pos, parent, PROP_ACTIVE, false);
				setProperty(pos, parent, PROP_CURRENT_DELAY, 0);
				onDelay(pos, parent);
			}
			else 
			{
				setProperty(pos, parent, PROP_CURRENT_DELAY, counter);
				scheduleTick(pos, parent);
			}
		}	
	}
	
	public void onDelay(Vec2 pos, ICircuit parent)
	{
		notifyNeighbours(pos, parent);
	}

	protected void setDelay(Vec2 pos, ICircuit parent, boolean delay)
	{
		setProperty(pos, parent, PROP_CURRENT_DELAY, getDelay(pos, parent));
		setProperty(pos, parent, PROP_ACTIVE, delay);
		if(delay) scheduleTick(pos, parent);
	}
}