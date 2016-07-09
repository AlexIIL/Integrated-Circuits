package moe.nightfall.vic.integratedcircuits.api.gate;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class GateIOProvider {
	public ISocket socket;

	public byte[] calculateBundledInput(EnumFacing side, EnumFacing rotation, EnumFacing abs, BlockPos offset) {
		return null;
	}

	public int calculateRedstoneInput(EnumFacing side, EnumFacing rotation, EnumFacing abs, BlockPos offset) {
		return 0;
	}
}
