package vic.mod.integratedcircuits;

import java.util.Arrays;
import java.util.Random;

import mrtjp.projectred.integration.BundledGateLogic;
import mrtjp.projectred.integration.BundledGatePart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import vic.mod.integratedcircuits.ic.CircuitData;
import vic.mod.integratedcircuits.ic.ICircuit;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartCircuit extends BundledGatePart implements ICircuit
{
	public byte tier;
	public String name;
	public byte[][] output;
	public CircuitData circuitData;
	
	@Override
	public String getType() 
	{
		return IntegratedCircuits.partCircuit;
	}
	
	@Override
    public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
    {
		subID = (byte)meta;
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
		logic = new CircuitLogic(this);
		
		ItemStack stack = player.getCurrentEquippedItem();
		NBTTagCompound comp = stack.stackTagCompound;
		if(comp == null) return;
		
		state = comp.getByte("con");
		tier = comp.getByte("tier");
		name = comp.getString("name");
		circuitData = CircuitData.readFromNBT(comp.getCompoundTag("circuit"), this);
		
		genOutput();
		genMatrix();
		scheduleTick(0);
    }

	@Override
	public void load(NBTTagCompound tag) 
	{
		orientation = tag.getByte("orient");
		subID = tag.getByte("subID");
		shape = tag.getByte("shape");
		connMap = tag.getShort("connMap") & 0xFFFF;
		schedTime = tag.getLong("schedTime");
		state = tag.getByte("state");
		logic = new CircuitLogic(this);
		logic.load(tag);
		
		//My part
		tier = tag.getByte("tier");
		name = tag.getString("name");
		genMatrix();
		circuitData = CircuitData.readFromNBT(tag.getCompoundTag("circuit"), this);
		genOutput();
	}
	
	@Override
	public void save(NBTTagCompound tag) 
	{
		super.save(tag);
		
		tag.setShort("tier", tier);
		tag.setString("name", name);
		tag.setTag("circuit", circuitData.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readDesc(MCDataInput packet) 
	{
		orientation = packet.readByte();
		subID = packet.readByte();
		shape = packet.readByte();
		state = packet.readByte();
		if(logic == null) logic = new CircuitLogic(this);
		logic.readDesc(packet);
		
		//My part
		tier = packet.readByte();
		name = packet.readString();
		circuitData = CircuitData.readFromNBT(packet.readNBTTagCompound(), this);
		genOutput();
	}
	
	@Override
	public ItemStack getItem() 
	{
		ItemStack stack = new ItemStack(IntegratedCircuits.itemCircuit);
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("circuit", getCircuitData().writeToNBT(new NBTTagCompound()));
		comp.setInteger("con", state);
		comp.setString("name", name);
		stack.stackTagCompound = comp;
		return stack;
	}

	@Override
	public void writeDesc(MCDataOutput packet) 
	{
		super.writeDesc(packet);
		
		packet.writeByte(tier);
		packet.writeString(name);
		packet.writeNBTTagCompound(circuitData.writeToNBT(new NBTTagCompound()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(Random rand) 
	{
		//Nothing fancy in here...
	}
	
	@Override
	public int getLightValue() 
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			CCRenderState.setBrightness(world(), x(), y(), z());
			ItemCircuit.renderer.prepare(this);
			ItemCircuit.renderer.renderStatic(pos.translation(), orientation & 0xFF);
			return true;
		}	
		else return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			ItemCircuit.renderer.prepareDynamic(this, frame);
			ItemCircuit.renderer.renderDynamic(this.rotationT().with(pos.translation()));
		}	
	}

	public class CircuitLogic extends BundledGateLogic
	{	
		public CircuitLogic(BundledGatePart gate) 
		{
			super(gate);
		}

		@Override
		public boolean canConnectBundled(BundledGatePart gate, int r) 
		{
			return isBundeledAtSide(r);
		}

		@Override
		public boolean canConnect(int shape, int r) 
		{
			return !isBundeledAtSide(r);
		}

		@Override
		public int getOutput(BundledGatePart gate, int r)
		{
			return isBundeledAtSide(r) ? 0 : output[r][0];
		}

		@Override
		public void onChange(BundledGatePart gate) 
		{
			
		}

		@Override
		public byte[] getBundledOutput(BundledGatePart gate, int r) 
		{
			return isBundeledAtSide(r) ? output[r] : null;
		}

		@Override
		public void scheduledTick(BundledGatePart gate) 
		{
			super.scheduledTick(gate);
		}
	}
	
	@Override
	public byte[] getBundledSignal(int r) 
	{
		//Why would I need a mask?
		return getLogic().getBundledOutput(this, toInternal(r));
	}

	private boolean isBundeledAtSide(int s)
	{
		int mask = (int)Math.pow(2, s);
		return (state & mask) > 0;
	}
	
	private void genMatrix()
	{
		int s = tier == 1 ? 18 : tier == 2 ? 34 : 68;
		circuitData = new CircuitData(s, this);
	}
	
	private void genOutput()
	{
		output = new byte[4][];
		for(int i = 0; i < 4; i++)
		{
			if(isBundeledAtSide(i))
			{
				byte[] b = new byte[16];
				Arrays.fill(b, (byte)255);
				output[i] = b;
			}
			else output[i] = new byte[]{15};
		}
	}

	@Override
	public CircuitData getCircuitData() 
	{
		return circuitData;
	}
	
	@Override
	public void setCircuitData(CircuitData data) 
	{
		this.circuitData = data;
	}

	@Override
	public boolean getInputFromSide(ForgeDirection dir, int frequency) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOutputToSide(ForgeDirection dir, int frequency, boolean output) 
	{
		// TODO Auto-generated method stub
	}
}
