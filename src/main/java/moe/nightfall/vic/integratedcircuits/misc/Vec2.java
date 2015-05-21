package moe.nightfall.vic.integratedcircuits.misc;

import java.util.Objects;

import net.minecraftforge.common.util.ForgeDirection;

/** An int value pair **/
public class Vec2 implements Cloneable {
	public final int x, y;

	public Vec2(int a, int b) {
		this.x = a;
		this.y = b;
	}

	@Override
	public Vec2 clone() {
		return new Vec2(x, y);
	}

	public Vec2 offset(ForgeDirection dir) {
		return new Vec2(x + dir.offsetX, y + dir.offsetZ);
	}

	public double distanceTo(Vec2 other) {
		return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vec2 other = (Vec2) obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString() {
		return "Vec2[" + x + ", " + y + "]";
	}
}
