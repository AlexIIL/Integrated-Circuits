package vic.mod.integratedcircuits.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.misc.MiscUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;

public class ItemSocket extends ItemBase
{
	public ItemSocket()
	{
		super("socket");
		setCreativeTab(null);
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) 
	{
		BlockCoord pos = new BlockCoord(x, y, z);
		Vector3 vhit = new Vector3(hitX, hitY, hitZ);
		double d = getHitDepth(vhit, side);

		if(d < 1 && place(stack, player, world, pos, side, vhit))
			return true;
	
		pos.offset(side);
		return place(stack, player, world, pos, side, vhit);
	}
	
	private boolean place(ItemStack stack, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit)
	{
		BlockCoord pos2 = pos.copy().offset(side ^ 1);
		if(!MiscUtils.canPlaceGateOnSide(world, pos2.x, pos2.y, pos2.z, side))
			return false;
		
		if(world.getBlock(pos.x, pos.y, pos.z).isReplaceable(world, pos.x, pos.y, pos.z))
			world.setBlock(pos.x, pos.y, pos.z, IntegratedCircuits.blockGate);

		if(!player.capabilities.isCreativeMode)
			stack.stackSize--;
		
		return true;
	}
	
	public double getHitDepth(Vector3 vhit, int side)
	{
		return vhit.copy().scalarProject(Rotation.axes[side]) + (side % 2 ^ 1);
	}
}
