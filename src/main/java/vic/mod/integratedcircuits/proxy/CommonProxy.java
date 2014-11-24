package vic.mod.integratedcircuits.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import vic.mod.integratedcircuits.DiskDrive;
import vic.mod.integratedcircuits.GuiHandler;
import vic.mod.integratedcircuits.IDiskDrive;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.misc.MiscUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy 
{
	public static int serverTicks;
	
	public void initialize()
	{
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		registerNetwork();
	}
	
	public void registerNetwork()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(IntegratedCircuits.instance, new GuiHandler());
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if(event.phase == Phase.END) serverTicks++;
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.action != Action.RIGHT_CLICK_BLOCK) return;
		Block block = event.world.getBlock(event.x, event.y, event.z);
		if(!(block.hasTileEntity(event.world.getBlockMetadata(event.x, event.y, event.z)))) return;
		TileEntity te = (TileEntity)event.world.getTileEntity(event.x, event.y, event.z);
		if(!(te instanceof IDiskDrive)) return;
		IDiskDrive drive = (IDiskDrive) te;
		
		ItemStack stack = event.entityPlayer.getCurrentEquippedItem();
		
		MovingObjectPosition target = MiscUtils.rayTrace(event.entityPlayer, 1F);	
		AxisAlignedBB box = DiskDrive.getDiskDriveBoundingBox(drive, event.x, event.y, event.z, target.hitVec);
		if(box == null) return;
		
		if(!event.world.isRemote)
		{
			if(stack == null)
			{
				ItemStack floppy = drive.getDisk();
				drive.setDisk(null);
				event.entityPlayer.setCurrentItemOrArmor(0, floppy);
			}
			else if(stack.getItem() != null && stack.getItem() == IntegratedCircuits.itemFloppyDisk && drive.getDisk() == null)
			{
				drive.setDisk(stack);
				event.entityPlayer.setCurrentItemOrArmor(0, null);
			}
			event.useBlock = Result.DENY;
			event.useItem = Result.DENY;
		}
	}
}
