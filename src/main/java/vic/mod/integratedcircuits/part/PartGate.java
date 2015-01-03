package vic.mod.integratedcircuits.part;

import java.util.Arrays;

import mrtjp.projectred.api.IBundledEmitter;
import mrtjp.projectred.api.IConnectable;
import mrtjp.projectred.transmission.APIImpl_Transmission;
import mrtjp.projectred.transmission.IRedwireEmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import vic.mod.integratedcircuits.IntegratedCircuits;
import vic.mod.integratedcircuits.client.PartRenderer;
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
import codechicken.multipart.IFaceRedstonePart;
import codechicken.multipart.IRedstonePart;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.RedstoneInteractions;
import codechicken.multipart.TFacePart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@InterfaceList(value = {
	@Interface(iface = "mrtjp.projectred.api.IBundledEmitter", modid = "ProjRed|Core"),
	@Interface(iface = "mrtjp.projectred.api.IConnectable", modid = "ProjRed|Core"),
})
public abstract class PartGate extends JCuboidPart implements JNormalOcclusion, TFacePart, IConnectable, IFaceRedstonePart, IBundledEmitter
{
	private String name;
	public byte orientation;
	private Cuboid6 box = new Cuboid6(0, 0, 0, 1, 2 / 16D, 1);
	public byte[][] output = new byte[4][16];
	public byte[][] input = new byte[4][16];
	//Used by the client, redstone IO
	public byte io;
	
	public PartGate(String name)
	{
		this.name = IntegratedCircuits.modID + "_" + name;
		PartFactory.register(this);
	}
	
	@Override
	public String getType() 
	{
		return name;
	}
	
	public void preparePlacement(EntityPlayer player, BlockCoord pos, int side, int meta)
	{
		setSide(side ^ 1);
		setRotation(Rotation.getSidedRotation(player, side));
	}
	
	@Override
	public void load(NBTTagCompound tag)
	{
		orientation = tag.getByte("orientation");
	}
	
	@Override
	public void save(NBTTagCompound tag)
	{
		tag.setByte("orientation", orientation);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		orientation = packet.readByte();
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(orientation);
	}

	@Override
	public void read(MCDataInput packet) 
	{
		read(packet.readByte(), packet);
	}
	
	public void read(byte discr, MCDataInput packet)
	{
		switch (discr) {
		case 0 :
			orientation = packet.readByte();
			tile().markRender();
			break;
		case 1 :
			io = packet.readByte();
			tile().markRender();
			break;
		}
	}
	
	public MCDataOutput getWriteStream(int disc)
	{
		return getWriteStream().writeByte(disc);
	}

	public Transformation getRotationTransformation()
	{
		return Rotation.sideOrientation(getSide(), getRotation()).at(Vector3.center);
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
		orientation = (byte)(orientation & 252| r);
	}
	
	@Override
	public Cuboid6 getBounds()
	{
		return box.copy().apply(getRotationTransformation());
	}
	
	@Override
	public Iterable<Cuboid6> getOcclusionBoxes() 
	{
		return Arrays.asList(getBounds());
	}
	
	@Override
    public boolean occlusionTest(TMultiPart npart)
	{
		return NormalOcclusionTest.apply(this, npart);
	}
	
	@Override
	public int getSlotMask() 
	{
		return 1 << getSide();
	}

	@Override
	public int redstoneConductionMap() 
	{
		return 0;
	}

	@Override
	public boolean solid(int arg0) 
	{
		return false;
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item) 
	{
		if(item != null)
		{
			//TODO This is really ugly and doesn't use P:R's API.
			String name = item.getItem().getUnlocalizedName();
			if(item.getItem() == IntegratedCircuits.itemScrewdriver || name.equals("item.redlogic.screwdriver") || name.equals("item.bluepower:screwdriver") || name.equals("item.projectred.core.screwdriver"))
			{
				if(!world().isRemote) onActivatedWithScrewdriver(player, hit, item);
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
		getWriteStream(0).writeByte(orientation);
		tile().markDirty();
		tile().notifyPartChange(this);
		tile().notifyNeighborChange(getSide());
	}

	@Override
	public void onAdded() 
	{
		if(!world().isRemote) updateInput();
		tile().notifyPartChange(this);
		tile().notifyNeighborChange(getSide());
	}

	@Override
	public void onRemoved()
	{
		tile().notifyPartChange(this);
		tile().notifyNeighborChange(getSide());
	}

	@Override
	public void onMoved() 
	{
		tile().notifyPartChange(this);
		tile().notifyNeighborChange(getSide());
	}
	
	public abstract ItemStack getItem();

	@Override
	public Iterable<ItemStack> getDrops() 
	{
		return Arrays.asList(getItem());
	}
	
	@Override
	public ItemStack pickItem(MovingObjectPosition hit) 
	{
		return getItem();
	}
	
	@SideOnly(Side.CLIENT)
	public abstract <T extends TMultiPart> PartRenderer<T> getRenderer();
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass) 
	{
		if(pass == 0)
		{
			TextureUtils.bindAtlas(0);
			CCRenderState.setBrightness(world(), x(), y(), z());
			getRenderer().prepare(this);
			getRenderer().renderStatic(pos.translation(), orientation & 255);
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
			getRenderer().prepareDynamic(this, frame);
			getRenderer().renderDynamic(this.getRotationTransformation().with(pos.translation()));
		}	
	}
	
	@Override
	public void onNeighborChanged() 
	{
		if(!world().isRemote) 
		{
			BlockCoord pos = new BlockCoord(tile()).offset(getSide());
			if(!MiscUtils.canPlaceGateOnSide(world(), pos.x, pos.y, pos.z, getSide() ^ 1))
			{
				TileMultipart.dropItem(getItem(), world(), Vector3.fromTileEntityCenter(tile()));
				tile().remPart(this);
			}
			else updateInput();
		}
	}

	@Override
	public void onPartChanged(TMultiPart part) 
	{
		if(!world().isRemote) updateInput();
	}

	public void updateInput()
	{
		for(int i = 0; i < 4; i++)
		{
			if(canConnectRedstoneImpl(i)) input[i][0] = (byte)updateRedstoneInput(i);
			else if(IntegratedCircuits.isPRLoaded && canConnectBundledImpl(i)) input[i] = updatedBundledInput(i);
		}
		updateRedstoneIO();
	}
	
	public void updateRedstoneIO()
	{
		byte oio = io;
		io = 0;
		for(int i = 0; i < 4; i++)
			io |= (getRedstoneInput(i) != 0 || output[i][0] != 0) ? 1 << i: 0;
		
		if(oio != io) getWriteStream(1).writeByte(io);
	}
	
	public byte getRedstoneInput(int side)
	{
		return input[side][0];
	}
	
	public byte[] getBundledInput(int side)
	{
		return input[side];
	}
	
	public byte[] updatedBundledInput(int side)
	{
		int r = getRotationAbs(side);
		int abs = Rotation.rotateSide(getSide(), r);
		byte[] power = null;

		if(((abs ^ 1) & 6) != ((getSide() ^ 1) & 6))
		{
			BlockCoord pos = new BlockCoord(tile()).offset(abs).offset(getSide());
			TileEntity t = world().getTileEntity(pos.x, pos.y, pos.z);
			if(t != null && t instanceof TileMultipart) 
				power = updateBundledPartSignal(0, ((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, getSide() ^ 1));
			if(power != null) return power;
		}
		
		BlockCoord pos = new BlockCoord(tile()).offset(abs);
		TileEntity t = world().getTileEntity(pos.x, pos.y, pos.z);
		if (t instanceof IBundledEmitter)
			power = updateBundledPartSignal(1, t, abs ^ 1);
		else if (t instanceof TileMultipart)
			power = updateBundledPartSignal(1, ((TileMultipart)t).partMap(getSide()), (r + 2) % 4);
		else power = APIImpl_Transmission.getBundledSignal(world(), pos, abs ^ 1);
		if(power != null) return power;
		
		if((abs & 6) != (getSide() & 6))
		{
			TMultiPart tp = tile().partMap(abs);
			power = updateBundledPartSignal(2, tp, Rotation.rotationTo(abs, getSide()));
			if(power != null) return power;
		}
		
		return new byte[16];
	}
	
	private byte[] updateBundledPartSignal(int mode, Object part, int r)
	{
		if(part instanceof IBundledEmitter) return ((IBundledEmitter)part).getBundledSignal(r);
		return null;
	}
	
	public int updateRedstoneInput(int side)
	{
		int r = getRotationAbs(side);
		int abs = Rotation.rotateSide(getSide(), r);
		if(strongPowerLevel(abs) != 0) return 0;
		int power = 0;
		
		if(((abs ^ 1) & 6) != ((getSide() ^ 1) & 6))
		{
			BlockCoord pos = new BlockCoord(tile()).offset(abs).offset(getSide());
			TileEntity t = world().getTileEntity(pos.x, pos.y, pos.z);
			if(t != null && t instanceof TileMultipart) 
				power = updatePartSignal(((TileMultipart)t).partMap(abs ^ 1), Rotation.rotationTo(abs ^ 1, getSide() ^ 1));
			if(power > 0) return power / 17;
		}
		
		power = RedstoneInteractions.getPowerTo(this, abs);
		if(power > 0) return power;
		
		TMultiPart tp = tile().partMap(abs);
		if((abs & 6) != (getSide() & 6))
		{
			power = updatePartSignal(tp, Rotation.rotationTo(abs, getSide()));
			if(power > 0) return power / 17;
		}
		
		if(tp instanceof IRedstonePart)
		{
			IRedstonePart rp = (IRedstonePart)tp;
			power = Math.max(rp.strongPowerLevel(getSide()), rp.weakPowerLevel(getSide())) << 4;
			if(power > 0) return power;
		}	
		
		return power;
	}
	
	private int updatePartSignal(TMultiPart part, int r)
	{
		if(!IntegratedCircuits.isPRLoaded) return 0;
		if(part instanceof IRedwireEmitter) 
			return ((IRedwireEmitter)part).getRedwireSignal(r);
		return 0;
	}
	
	//ProjectRed
	
	@Override
	public boolean canConnectCorner(int arg0) 
	{
		return false;
	}

	@Override
	public boolean connectCorner(IConnectable arg0, int arg1, int arg2) 
	{
		return connectStraight(arg0, arg1, arg2);
	}

	@Override
	public boolean connectInternal(IConnectable arg0, int arg1) 
	{
		return connectStraight(arg0, arg1, 0);
	}

	@Override
	public boolean connectStraight(IConnectable arg0, int arg1, int arg2) 
	{
		int side = getRotationRel(arg1);
		if(arg0 instanceof IRedwireEmitter && canConnectRedstoneImpl(side)) return true;
		else if(arg0 instanceof IBundledEmitter && canConnectBundledImpl(side)) return true;
		return false;
	}
	
	@Override
	public byte[] getBundledSignal(int arg0) 
	{
		int rot = getRotationRel(arg0);
		if(!canConnectBundledImpl(rot)) return null;
		return output[rot];
	}
	
	//---

	@Override
	public final boolean canConnectRedstone(int arg0) 
	{
		if((arg0 & 6) == (getSide() & 6)) return false;
		return canConnectRedstoneImpl(getSideRel(arg0));
	}

	public abstract boolean canConnectRedstoneImpl(int arg0);
	public abstract boolean canConnectBundledImpl(int arg0);
	
	@Override
	public int strongPowerLevel(int arg0) 
	{
		if((arg0 & 6) == (getSide() & 6)) return 0;
		int rot = getSideRel(arg0);
		if(!canConnectRedstoneImpl(rot)) return 0;
		return output[rot][0];
	}

	@Override
	public int weakPowerLevel(int arg0) 
	{
		return strongPowerLevel(arg0);
	}

	@Override
	public int getFace() 
	{
		return getSide();
	}

	abstract PartGate newInstance();
}
