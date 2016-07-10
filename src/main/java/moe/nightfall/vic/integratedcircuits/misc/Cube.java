package moe.nightfall.vic.integratedcircuits.misc;

import net.minecraft.util.math.Vec3d;

public class Cube {

    Vec3d low;
    Vec3d high;

    public Cube(double x, double y, double z, double w, double h, double d) {
        low = new Vec3d(x, y, z);
        high = new Vec3d(x + w, y + h, z + d);
    }
}
