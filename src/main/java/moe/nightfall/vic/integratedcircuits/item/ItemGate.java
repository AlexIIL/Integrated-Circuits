package moe.nightfall.vic.integratedcircuits.item;

import moe.nightfall.vic.integratedcircuits.api.gate.IGateItem;
//import moe.nightfall.vic.integratedcircuits.compat.buildcraft.GatePipePluggable;
import net.minecraft.item.ItemStack;
/*
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.PipePluggable;*/
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "buildcraft.api.transport.pluggable.IPipePluggableItem", modid = "BuildCraft|Core")
public abstract class ItemGate extends ItemBase /*implements IGateItem, IPipePluggableItem*/ {

	public ItemGate(String name) {
		super(name);
	}
/* FIXME reimplement this
	@Override
	@Method(modid = "BuildCraft|Core")
	public PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack) {
		IPipeTile tile = pipe.getTile();
		return new GatePipePluggable(stack, new BlockCoord(tile.x(), tile.y(), tile.z()), tile.getWorld(), side);
	}*/

}
