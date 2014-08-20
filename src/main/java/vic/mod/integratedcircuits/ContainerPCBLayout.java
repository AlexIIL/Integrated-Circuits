package vic.mod.integratedcircuits;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerPCBLayout extends Container
{
	public TileEntityPCBLayout tileentity;
	
	public ContainerPCBLayout(TileEntityPCBLayout tileentity)
	{
		this.tileentity = tileentity;
		this.tileentity.onContainerOpened();
	}

	@Override
	public void onContainerClosed(EntityPlayer player) 
	{
		this.tileentity.onContainerClosed();
		super.onContainerClosed(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return player.getDistanceSq(tileentity.xCoord + 0.5, tileentity.yCoord + 0.5, tileentity.zCoord + 0.5) < 64;
	}
}
