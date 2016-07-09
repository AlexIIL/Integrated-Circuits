package moe.nightfall.vic.integratedcircuits.tile;

import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.client.Resources;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAssembler extends Block {
	public BlockAssembler() {
		super(Material.IRON);
		setUnlocalizedName(Constants.MOD_ID + ".assembler");
		setRegistryName("assembler");
		setCreativeTab(IntegratedCircuits.creativeTab);
		setHardness(2F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing side, float p_onBlockActivated_8_, float p_onBlockActivated_9_, float p_onBlockActivated_10_) {
		if (!world.isRemote)
			player.openGui(IntegratedCircuits.instance, 1, world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityAssembler();
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	/*
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return getIcon(null, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int s) {
		return getIcon((TileEntityAssembler) world.getTileEntity(x, y, z), s);
	}

	@SideOnly(Side.CLIENT)
	private IIcon getIcon(TileEntityAssembler te, int s) {
		int rotation = te != null ? te.rotation : 0;

		// TODO Maybe... just maybe use ForgeDirection's rotating here?
		if (s == 0)
			return Resources.ICON_ASSEMBLER_BOTTOM;
		else if (s == 1)
			return Resources.ICON_ASSEMBLER_TOP;
		else if (s == 2 && rotation == 0 || s == 5 && rotation == 1 || s == 3 && rotation == 2 || s == 4
				&& rotation == 3)
			return te != null && te.laserHelper.isRunning ? Resources.ICON_ASSEMBLER_FRONT_ON
					: Resources.ICON_ASSEMBLER_FRONT_OFF;
		else if (s == 3 && rotation == 0 || s == 4 && rotation == 1 || s == 2 && rotation == 2 || s == 5
				&& rotation == 3)
			return Resources.ICON_ASSEMBLER_BACK;
		else
			return Resources.ICON_ASSEMBLER_SIDE;
	}*/

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entity, ItemStack stack) {
		int rotation = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(blockPos);
		if (te != null)
			te.rotation = rotation;
	}

	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(blockPos);
		te.dropContents();
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos blockPos, IBlockState blockState, Block block) {
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(blockPos);
		te.onNeighborBlockChange();
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		return getStrongPower(blockState, world, blockPos, side);
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		TileEntityAssembler te = (TileEntityAssembler) world.getTileEntity(blockPos);
		return te.isProvidingPower();
	}

	@Override
	public boolean isSideSolid(IBlockState blockState, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean canProvidePower(IBlockState blockState) {
		return true;
	}

	@Override
	public boolean canConnectRedstone(IBlockState blockState, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
		return true; // TODO check was side != -1... find out what -1 meant previosuly and fix it
	}

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing axis) {
		return ((TileEntityAssembler) world.getTileEntity(blockPos)).rotate();
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}
/*
	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_) {
	}*/
}
