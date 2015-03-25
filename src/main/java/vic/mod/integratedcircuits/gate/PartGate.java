package vic.mod.integratedcircuits.gate;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;

import org.apache.commons.lang3.ArrayUtils;

import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.PartGateRenderer;
import vic.mod.integratedcircuits.gate.GateProvider.IGateProvider;
import vic.mod.integratedcircuits.misc.MiscUtils;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class PartGate
{
	//Collision box
	public static Cuboid6 box = new Cuboid6(0, 0, 0, 1, 2 / 16D, 1);
	
	private String name;
	protected IGateProvider provider;
	
	//Used by the client, redstone IO
	public byte io;
	protected byte[][] output = new byte[4][16];
	protected byte[][] input = new byte[4][16];
	
	public byte orientation;
	
	public PartGate(String name)
	{
		this.name = name;
	}
	
	public IGateProvider getProvider()
	{
		return provider;
	}
	
	public void setProvider(IGateProvider provider)
	{
		this.provider = provider;
	}
	
	public String getType() 
	{
		return Constants.MOD_ID + "_" + name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
	{
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
	}
	
	public void load(NBTTagCompound tag)
	{
		orientation = tag.getByte("orientation");
		io = tag.getByte("io");
		
		byte[] input = tag.getByteArray("input");
		byte[] output = tag.getByteArray("output");
		
		try {
    		for(int i = 0; i < 4; i++)
    		{
    			this.input[i] = Arrays.copyOfRange(input, i * 16, (i + 1) * 16);
    			this.output[i] = Arrays.copyOfRange(output, i * 16, (i + 1) * 16);
    		}
		} catch (ArrayIndexOutOfBoundsException e) {
			//--Legacy code--
			//We couldn't get a proper array, so output a warning.
			IntegratedCircuits.logger.warn("Couldn't retrieve gate io for circuit at " + getProvider().getPos() + ". "
				+ "This can be caused by a version update, if this happens on the next world load please report it!");
			
			//Reset to old configuration
			this.input = new byte[4][16];
			this.output = new byte[4][16];
		}
	}
	
	public void save(NBTTagCompound tag)
	{
		tag.setByte("orientation", orientation);
		tag.setByte("io", io);
		
		byte[] input = null;
		byte[] output = null;
		
		for(int i = 0; i < 4; i++)
		{
			input = ArrayUtils.addAll(input, this.input[i]);
			output = ArrayUtils.addAll(output, this.output[i]);
		}
		
		tag.setByteArray("input", input);
		tag.setByteArray("output", output);
	}

	public void readDesc(MCDataInput packet)
	{
		orientation = packet.readByte();
		io = packet.readByte();
	}
	
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(orientation);
		packet.writeByte(io);
	}

	public void read(byte discr, MCDataInput packet)
	{
		switch (discr) {
		case 0 :
			orientation = packet.readByte();
			provider.markRender();
			break;
		case 1 :
			io = packet.readByte();
			provider.markRender();
			break;
		}
	}

	public int getSide()
	{
		return orientation >> 2;
	}
	
	public int getSideRel(int side)
	{
		return getRotationRel(Rotation.rotationTo(getSide(), side));
	}
	
	public void setSide(int s)
	{
		orientation = (byte)(orientation & 3 | s << 2);
	}
	
	public int getRotation()
	{
		return orientation & 3;
	}
	
	public int getRotationAbs(int rel)
	{
		return (rel + getRotation() + 2) % 4;
	}
	
	public int getRotationRel(int abs)
	{
		return (abs + 6 - getRotation()) % 4;
	}
	
	public void setRotation(int r)
	{
		orientation = (byte)(orientation & 252 | r);
	}
	
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		if(item != null)
		{
			//TODO This is really ugly and doesn't use P:R's API.
			String name = item.getItem().getUnlocalizedName();
			if(item.getItem() == IntegratedCircuits.itemScrewdriver || name.equals("item.redlogic.screwdriver") || name.equals("item.bluepower:screwdriver") || name.equals("item.projectred.core.screwdriver"))
			{
				if(!provider.getWorld().isRemote) onActivatedWithScrewdriver(player, hit, item);
				item.damageItem(1, player);
				return true;
			}
		}
		return false;
	}

	public void onActivatedWithScrewdriver(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		rotate();
	}
	
	public void rotate()
	{
		setRotation((getRotation() + 1) % 4);
		provider.getWriteStream(0).writeByte(orientation);
		provider.notifyBlocksAndChanges();
		updateInput();
	}

	public void onAdded() 
	{
		notifyChanges();
	}

	public void onRemoved()
	{
		provider.notifyBlocksAndChanges();
	}

	public void onMoved() 
	{
		notifyChanges();
	}

	public void notifyChanges()
	{
		if(!provider.getWorld().isRemote) updateInput();
		provider.notifyBlocksAndChanges();
	}
	
	public abstract ItemStack getItemStack();

	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return getItemStack();
	}

	public Transformation getRotationTransformation()
	{
		return Rotation.sideOrientation(getSide(), getRotation()).at(Vector3.center);
	}
	
	@SideOnly(Side.CLIENT)
	public abstract <T extends PartGate> PartGateRenderer<T> getRenderer();
	
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			BlockCoord blockPos = provider.getPos();
			CCRenderState.setBrightness(provider.getWorld(), blockPos.x, blockPos.y, blockPos.z);
			getRenderer().prepare(this);
			getRenderer().renderStatic(pos.translation(), orientation & 255);
			return true;
		}	
		else return false;
	}
	
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			getRenderer().prepareDynamic(this, frame);
			getRenderer().renderDynamic(this.getRotationTransformation().with(pos.translation()));
		}	
	}
	
	public void onNeighborChanged() 
	{
		if(!provider.getWorld().isRemote) 
		{
			BlockCoord pos = provider.getPos().offset(getSide());
			if(!MiscUtils.canPlaceGateOnSide(provider.getWorld(), pos.x, pos.y, pos.z, getSide() ^ 1))
			{
				MiscUtils.dropItem(provider.getWorld(), provider.getItemStack(), pos.x, pos.y, pos.z);
				provider.destroy();
			}
			else updateInput();
		}
	}
	
	public byte getRedstoneInput(int side)
	{
		return getBundledInput(side, 0);
	}
	
	public byte[] getBundledInput(int side) 
	{
		return input[side];
	}
	
	public byte getBundledInput(int side, int frequency)
	{
		return input[side][frequency];
	}
	
	public byte getRedstoneOutput(int side) 
	{
		return getBundledOutput(side, 0);
	}
	
	public byte[] getBundledOutput(int side) 
	{
		return output[side];
	}

	public byte getBundledOutput(int side, int frequency) 
	{
		return output[side][frequency];
	}
	
	public void setInput(byte[][] input) 
	{
		this.input = input;
	}
	
	public void setOutput(byte[][] output)
	{
		this.output = output;
	}
	
	public void setInput(int side, byte[] input) 
	{
		this.input[side] = input;
	}
	
	public void setOutput(int side, byte[] output)
	{
		this.output[side] = output;
	}
	
	public void setInput(int side, int frequency, byte input) 
	{
		this.input[side][frequency] = input;
	}
	
	public void setOutput(int side, int frequency, byte output) 
	{
		this.output[side][frequency] = output;
	}
	
	public void update() {}
	
	public void scheduledTick() {}
	
	public final void updateInput()
	{
		updateInputPre();
		for(int i = 0; i < 4; i++)
		{
			if(canConnectRedstoneImpl(i)) input[i][0] = (byte)updateRedstoneInput(i);
			else if(canConnectBundledImpl(i)) input[i] = updateBundledInput(i);
		}
		updateInputPost();
	}
	
	public void updateInputPre() {}
	
	public void updateInputPost() 
	{
		updateRedstoneIO();
	}
	
	public int updateRedstoneInput(int side)
	{
		return provider.updateRedstoneInput(side);
	}
	
	public byte[] updateBundledInput(int side)
	{
		return provider.updateBundledInput(side);
	}
	
	public void updateRedstoneIO()
	{
		byte oio = io;
		io = 0;
		for(int i = 0; i < 4; i++)
			io |= (getRedstoneInput(i) != 0 || getRedstoneOutput(i) != 0) ? 1 << i: 0;
		
		if(oio != io) provider.getWriteStream(1).writeByte(io);
	}

	public abstract boolean canConnectRedstoneImpl(int arg0);
	public abstract boolean canConnectBundledImpl(int arg0);

	public abstract PartGate newInstance();
}
