package vic.mod.integratedcircuits.net;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import vic.mod.integratedcircuits.client.gui.Gui7Segment;
import vic.mod.integratedcircuits.gate.Part7Segment;
import vic.mod.integratedcircuits.gate.GateProvider.IGateProvider;
import cpw.mods.fml.relauncher.Side;

public class Packet7SegmentOpenGui extends PacketGate<Packet7SegmentOpenGui>
{
	public Packet7SegmentOpenGui() {}
	
	public Packet7SegmentOpenGui(IGateProvider part) 
	{
		super(part);
	}
	
	@Override
	public void process(EntityPlayer player, Side side) 
	{
		Part7Segment part = (Part7Segment)getPart(player.worldObj);
		if(part == null) return;
		Minecraft.getMinecraft().displayGuiScreen(new Gui7Segment(part));
	}
}
