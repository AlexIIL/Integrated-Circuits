package moe.nightfall.vic.integratedcircuits.tile;

import mcmultipart.client.multipart.AdvancedEffectRenderer;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.Multipart;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocketWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class MCMPartSocket extends Multipart implements ISocketWrapper {

    @Override
    public ISocket getSocket() {
        return null;
    }

    @Override
    public void markRender() {

    }

    @Override
    public MCDataOutput getWriteStream(int disc) {
        return null;
    }

    @Override
    public void notifyBlocksAndChanges() {

    }

    @Override
    public void notifyPartChange() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void updateInput() {

    }

    @Override
    public int updateRedstoneInput(int side) {
        return 0;
    }

    @Override
    public byte[] updateBundledInput(int side) {
        return new byte[0];
    }

    @Override
    public void scheduleTick(int delay) {

    }

    @Override
    public void sendDescription() {

    }
}
