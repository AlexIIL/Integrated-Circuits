package moe.nightfall.vic.integratedcircuits;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//TODO Rewrite!
public class DiskDrive {
	public static AxisAlignedBB getDiskDriveBoundingBox(IDiskDrive drive, Vec3d hitVec) {
		AxisAlignedBB box = drive.getBoundingBox();
		if (!box.isVecInside(hitVec))
			return null;
		return box;
	}

	public static void dropFloppy(IDiskDrive drive, World world, BlockPos pos) {
		if (drive.getDisk() != null)
			world.spawnEntityInWorld(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), drive.getDisk()));
	}

	@SideOnly(Side.CLIENT)
	public static void renderFloppy(IDiskDrive drive, double x, double y, double z,
			float partialTicks, EnumFacing rotation) {
		if (drive.getDisk() != null) {
			ItemStack floppy = drive.getDisk();
			String name = floppy.getTagCompound() != null && floppy.getTagCompound().hasKey("circuit") ? floppy
				.getTagCompound().getCompoundTag("circuit").getCompoundTag("properties").getString("name") : null;

			if (name != null) {
				FontRenderer fr = FMLClientHandler.instance().getClient().fontRendererObj;
				GL11.glPushMatrix();
				GL11.glTranslated(x, y, z);
				GL11.glRotatef(180, 0, 0, 1);
				GL11.glTranslatef(-0.5F, -0.5F, 0.5F);
				GL11.glRotatef(90 * (rotation.getHorizontalIndex() - 2), 0, 1, 0);
				GL11.glTranslatef(0.5F, 0.5F, -0.5F);
				GL11.glTranslatef(-1, -1, 0);
				float scale = 1 / 128F;
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslated(32, 110, -8.005);
				GL11.glDisable(GL11.GL_LIGHTING);
				fr.drawString(name, 0, 0, 0xFFFFFF);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			}
		}
	}

	public interface IDiskDrive {
		public AxisAlignedBB getBoundingBox();

		public ItemStack getDisk();

		public void setDisk(ItemStack stack);

		default public boolean useDisk(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
			ItemStack stack = player.getHeldItem(hand);
			AxisAlignedBB box = DiskDrive.getDiskDriveBoundingBox(this, new Vec3d(hitX, hitY, hitZ));
			if (box != null) {
				if (!world.isRemote) {
					if (stack == null) {
						ItemStack floppy = getDisk();
						setDisk(null);
						player.setHeldItem(hand, floppy);
						return true;
					} else if (stack.getItem() != null && stack.getItem() == Content.itemFloppyDisk
							&& getDisk() == null) {
						setDisk(stack);
						player.setHeldItem(hand, null);
						return true;
					}
				}
				return true;
			}
			return false;
		}
	}
}
