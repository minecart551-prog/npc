package noppes.npcs.shared.common.util;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class NopVector3f implements Comparable<NopVector3f> {
    public static final NopVector3f ZERO = new NopVector3f(0.0F, 0.0F, 0.0F);
    public static final NopVector3f ONE = new NopVector3f(1.0F, 1.0F, 1.0F);
    public static final NopVector3f ROTATION = new NopVector3f((float)Math.PI * 2, (float)Math.PI * 2, (float)Math.PI * 2);
    public final float x;
    public final float y;
    public final float z;

    public NopVector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public NopVector3f(float[] values) {
        this(values[0], values[1], values[2]);
    }

    public NopVector3f mul(float mul) {
        return new NopVector3f(x * mul, y * mul, z * mul);
    }

    public NopVector3f add(float x, float y, float z) {
        return new NopVector3f(this.x + x, this.y + y, this.z + z);
    }

    public NopVector3f add(NopVector3f vec) {
        return new NopVector3f(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    public NopVector3f subtract(NopVector3f vec) {
        return new NopVector3f(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    public NopVector3f modulo(NopVector3f vec) {
        return new NopVector3f(this.x % vec.x, this.y % vec.y, this.z % vec.z);
    }

    public NopVector3f set(float x, float y, float z) {
        return new NopVector3f(x, y, z);
    }

    public NopVector3f normalize() {
        float f = this.x * this.x + this.y * this.y + this.z * this.z;
        if (f < Float.MIN_NORMAL) {
            return this;
        }
        return mul((float) Mth.fastInvSqrt(f));
    }

    public NopVector3f lerp(NopVector3f vec, float f) {
        if(vec == this){
            return this;
        }
        float dif = 1.0F - f;
        return new NopVector3f(
                this.x * dif + vec.x * f,
                this.y * dif + vec.y * f,
                this.z * dif + vec.z * f);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        } else if (!(ob instanceof NopVector3f)) {
            return false;
        } else {
            NopVector3f o = (NopVector3f)ob;
            return this.x == o.x && this.y == o.y && this.z == o.z;
        }
    }

    @Override
    public int compareTo(@NotNull NopVector3f o) {
        if(this.x == o.x && this.y == o.y && this.z == o.z){
            return 0;
        }
        if(this.x != o.x){
            return this.x < o.x ? -1 : 1;
        }
        if(this.y != o.y){
            return this.y < o.y ? -1 : 1;
        }
        if(this.z != o.z){
            return this.z < o.z ? -1 : 1;
        }
        return 0;
    }
}
