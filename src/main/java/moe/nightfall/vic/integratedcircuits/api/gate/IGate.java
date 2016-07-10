package moe.nightfall.vic.integratedcircuits.api.gate;

import io.netty.buffer.ByteBuf;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketBridge.ISocketBase;
import moe.nightfall.vic.integratedcircuits.misc.Cube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

public interface IGate {
	public ISocketBase getProvider();

	public void setProvider(ISocketBase provider);

	public void preparePlacement(EntityPlayer player, ItemStack stack);

	public void load(NBTTagCompound tag);

	public void save(NBTTagCompound tag);

	public void readDesc(NBTTagCompound tag);

	public void writeDesc(NBTTagCompound tag);

	public void read(byte discr, ByteBuf packet);

	public boolean activate(EntityPlayer player, RayTraceResult hit, ItemStack item);

	public void onActivatedWithScrewdriver(EntityPlayer player, RayTraceResult hit, ItemStack item);

	public void onRotated();

	public void onAdded();

	public void onRemoved();

	public void onMoved();

	public ItemStack getItemStack();

	public ItemStack pickItem(RayTraceResult hit);

	public Cube getDimension();

	public void onNeighborChanged();

	public void update();

	public void scheduledTick();

	public void updateInputPre();

	public void updateInputPost();

	public EnumConnectionType getConnectionTypeAtSide(EnumFacing side);

	public boolean hasComparatorInputAtSide(EnumFacing side);
}
