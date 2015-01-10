package vic.mod.integratedcircuits.tile;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import vic.mod.integratedcircuits.gate.PartGate;
import vic.mod.integratedcircuits.proxy.ClientProxy;
import codechicken.lib.vec.Cuboid6;

public class BlockGate extends BlockContainer
{
	private PartGate gate;
	
	public BlockGate(PartGate gate) 
	{
		super(Material.circuits);
		setBlockName(gate.getType());
		this.gate = gate;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		Cuboid6 bounds = PartGate.box.copy().apply(te.getGate().getRotationTransformation());
		bounds.setBlockBounds(this);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().scheduledTick();
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onNeighborChanged();
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int meta) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onAdded();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		return te.getGate().activate(player, new MovingObjectPosition(x, y, z, side, Vec3.createVectorHelper(hitX, hitY, hitZ)), player.getHeldItem());
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onWorldJoin();
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		te.getGate().onRemoved();
	}

	@Override
	public boolean renderAsNormalBlock() 
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube() 
	{
		return false;
	}

	@Override
	public int getRenderType() 
	{
		return ClientProxy.GATE_RENDER_ID;
	}

	@Override
	public void registerBlockIcons(IIconRegister ir) {}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) 
	{
		return new TileEntityGate(gate.newInstance());
	}

	@Override
	public boolean canProvidePower() 
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		if((side & 6) == (gate.getSide() & 6)) return false;
		int rel = gate.getSideRel(side);
		System.out.println(rel);
		
		/*if(IntegratedCircuits.isFMPLoaded)
		{
			//Check if ANY multipart from that side can connect.
			BlockCoord pos = new BlockCoord(te).offset(side);
			TileEntity t = world.getTileEntity(pos.x, pos.y, pos.z);
			System.out.println(t);
			
			if(t instanceof TileMultipart)
			{
				TMultiPart mp = ((TileMultipart)t).partMap(gate.getSide());
				if(!(mp instanceof IRedstoneConnector)) return false;
			}
		}*/
		
		return gate.canConnectRedstoneImpl(rel);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) 
	{
		return isProvidingStrongPower(world, x, y, z, side);
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) 
	{
		TileEntityGate te = (TileEntityGate)world.getTileEntity(x, y, z);
		PartGate gate = te.getGate();
		
		if((side & 6) == (gate.getSide() & 6)) return 0;
		int rot = gate.getSideRel(side);
		if(!gate.canConnectRedstoneImpl(rot)) return 0;
		return gate.getRedstoneOutput(rot);
	}
}
