package moe.nightfall.vic.integratedcircuits.misc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.collect.HashBiMap;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class MiscUtils {
	private static EnumFacing[] order = { EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST };
	private static int[] index = { -1, -1, 0, 2, 3, 1, -1 };
	public static HashBiMap<String, Integer> colors = HashBiMap.create();

	static {
		colors.put("dyeBlack", 15);
		colors.put("dyeRed", 14);
		colors.put("dyeGreen", 13);
		colors.put("dyeBrown", 12);
		colors.put("dyeBlue", 11);
		colors.put("dyePurple", 10);
		colors.put("dyeCyan", 9);
		colors.put("dyeLightGray", 8);
		colors.put("dyeGray", 7);
		colors.put("dyePink", 6);
		colors.put("dyeLime", 5);
		colors.put("dyeYellow", 4);
		colors.put("dyeLightBlue", 3);
		colors.put("dyeMagenta", 2);
		colors.put("dyeOrange", 1);
		colors.put("dyeWhite", 0);
	}

	public static int getColor(ItemStack stack) {
		for (int id : OreDictionary.getOreIDs(stack)) {
			Integer color = colors.get(OreDictionary.getOreName(id));
			if (color != null)
				return color;
		}
		return -1;
	}

	public static String getLocalizedColor(int color) {
		if (color == 8)
			return I18n.translateToLocal("item.fireworksCharge.silver");
		else
			return I18n.translateToLocal("item.fireworksCharge."
					+ WordUtils.uncapitalize(colors.inverse().get(color).substring(3)));
	}

	public static void playPlaceSound(EntityPlayer player, World world, BlockPos pos) {
		SoundType sound = world.getBlockState(pos).getBlock().getSoundType();
		world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer thePlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	/*
	public static EntityPlayerMP getPlayerByUUID(UUID uuid) {
		for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {

			EntityPlayerMP player = (EntityPlayerMP) o;
			if (uuid.equals(player.getGameProfile().getId()))
				return player;
		}
		return null;
	}

	public static EntityPlayerMP getPlayerByUsername(String username) {
		for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			EntityPlayerMP player = (EntityPlayerMP) o;
			if (player.getCommandSenderEntity().getName().equalsIgnoreCase(username))
				return player;
		}
		return null;
	}*/

	public static EnumFacing rotn(EnumFacing fd, int offset) {
		int pos = index[fd.ordinal()];
		int newPos = pos + offset;
		pos = newPos > 3 ? newPos - 4 : newPos < 0 ? newPos + 4 : newPos;
		return order[pos];
	}

	public static EnumFacing rot(EnumFacing fd) {
		return rotn(fd, 1);
	}

	public static EnumFacing getDirection(int side) {
		return order[side];
	}

	public static int getSide(EnumFacing side) {
		return index[side.getIndex()];
	}

	public static String getLocalizedDirection(EnumFacing fd) {
		return I18n.translateToLocalFormatted("fdirection." + fd.name().toLowerCase() + ".name");
	}

	public static AxisAlignedBB getRotatedInstance(AxisAlignedBB def, EnumFacing rotation) {
		def = def.offset(-0.5, -0.5, -0.5);
		switch (rotation) { // FIXME fix to be the right dirs
			case WEST:
				def = new AxisAlignedBB(def.minZ, def.minY, -def.maxX, def.maxZ, def.maxY, -def.minX);
			case SOUTH:
				def = new AxisAlignedBB(-def.maxX, def.minY, -def.maxZ, -def.minX, def.maxY, -def.minZ);
			case EAST:
				def = new AxisAlignedBB(-def.maxZ, def.minY, def.minX, -def.minZ, def.maxY, def.maxX);
		}
		def = def.offset(0.5, 0.5, 0.5);
		return def;
	}

	public static boolean canPlaceGateOnSide(World world, BlockPos pos, EnumFacing side) {
		IBlockState blockState = world.getBlockState(pos);
		if (blockState == null)
			return false;
		return blockState.isSideSolid(world, pos, side);
	}

	public static void dropItem(World world, ItemStack stack, int x, int y, int z) {
		if (stack == null)
			return;
		EntityItem entityItem = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack);
		world.spawnEntityInWorld(entityItem);
	}

	public static boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	public static boolean isServer() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	public static float toBinary16Float(int bits) {
		int mant = bits & 0x03FF;
		int exp = bits & 0x7C00;

		if (exp == 0x7C00)
			exp = 0x3FC00;
		else if (exp != 0) {
			exp += 0x1C000;
			if (mant == 0 && exp > 0x1C400)
				return Float.intBitsToFloat((bits & 0x8000) << 16 | exp << 13 | 0x3FF);
		} else if (mant != 0) {
			exp = 0x1C400;
			do {
				mant <<= 1;
				exp -= 0x400;
			} while ((mant & 0x400) == 0);
			mant &= 0x3FF;
		}
		return Float.intBitsToFloat((bits & 0x8000) << 16 | (exp | mant) << 13);
	}

	public static String[] stringNewlineSplit(String toSplit) {
		// ASCII is strange.
		return toSplit.split("\\r\\n|\\n\\r|\\r|\\n");
	}

	public static String stringNormalizeLinefeed(String normalize) {
		return normalize.replaceAll("\\r\\n", "\n");
	}

	public static String[] stringSplitFormat(String toFormat, Object... toInsert) {
		return stringNewlineSplit(String.format(toFormat, toInsert));
	}

	public static List<String> splitTranslateToLocalFormatted(String toTranslate, Object... toInsert) {
		return Arrays.asList(stringNewlineSplit(I18n.translateToLocalFormatted(toTranslate, toInsert)));
	}

	public static List<String> appendToAll(Object toAppend, List<String> list) {
		for (int i = list.size() - 1; i >= 0; i--) {
			list.set(i, toAppend + list.get(i));
		}
		return list;
	}

	public static String translate(String unlocalizedName) {
		String localizedName = I18n.translateToLocal(unlocalizedName);
		if (localizedName.equals(unlocalizedName)) {
			localizedName = I18n.translateToFallback(unlocalizedName);
			if (localizedName.equals(unlocalizedName))
				return null;
		}
		return localizedName;
	}

	public static String translateFormatted(String unlocalizedName, Object... toInsert) {
		String localizedName = I18n.translateToLocalFormatted(unlocalizedName, toInsert);
		if (localizedName.equals(unlocalizedName)) {
			localizedName = I18n.translateToFallback(unlocalizedName);
			if (localizedName.equals(unlocalizedName))
				return null;
		}
		return localizedName;
	}

}
