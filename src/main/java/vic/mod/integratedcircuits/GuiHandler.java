package vic.mod.integratedcircuits;

import vic.mod.integratedcircuits.client.GuiPCBLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		if(ID == 0)
		{
			return new ContainerPCBLayout((TileEntityPCBLayout)world.getTileEntity(x, y, z));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		if(ID == 0)
		{
			return new GuiPCBLayout(new ContainerPCBLayout((TileEntityPCBLayout)world.getTileEntity(x, y, z)));
		}
		return null;
	}
}
