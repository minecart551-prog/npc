package noppes.npcs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class NpcGroundPathNavigator extends GroundPathNavigation {

    public NpcGroundPathNavigator(Mob p_26448_, Level p_26449_) {
        super(p_26448_, p_26449_);
    }

    protected PathFinder createPathFinder(int p_26453_) {
        this.nodeEvaluator = new NpcWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, p_26453_);
    }


    protected void followThePath()
    {
        Vec3 vec3d = this.getTempMobPos();
        int i = this.path.getNodeCount();

        for (int j = this.path.getNextNodeIndex(); j < this.path.getNodeCount(); ++j)
        {
            if ((double)this.path.getNode(j).y != Math.floor(vec3d.y))
            {
                i = j;
                break;
            }
        }

        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i vec3i = this.path.getNextNodePos();

        if (Mth.abs((float)(this.mob.getX() - (vec3i.getX() + 0.5D))) < this.maxDistanceToWaypoint && Mth.abs((float)(this.mob.getZ() - (vec3i.getZ() + 0.5D))) < this.maxDistanceToWaypoint && Math.abs(this.mob.getY() - vec3i.getY()) < 1.0D)
        {
            this.path.advance();
        }

        int k = Mth.ceil(this.mob.getBbWidth());
        int l = Mth.ceil(this.mob.getBbHeight());
        int i1 = k;

        for (int j1 = i - 1; j1 >= this.path.getNextNodeIndex(); --j1)
        {
            if (this.isDirectPathBetweenPoints(vec3d, this.path.getEntityPosAtNode(this.mob, j1), k, l, i1))
            {
                this.path.setNextNodeIndex(j1);
                break;
            }
        }

        this.doStuckDetection(vec3d);
    }


    protected boolean isDirectPathBetweenPoints(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ)
    {
        int i = Mth.floor(posVec31.x);
        int j = Mth.floor(posVec31.z);
        double d0 = posVec32.x - posVec31.x;
        double d1 = posVec32.z - posVec31.z;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8D)
        {
            return false;
        }
        else
        {
            double d3 = 1.0D / Math.sqrt(d2);
            d0 = d0 * d3;
            d1 = d1 * d3;
            sizeX = sizeX + 2;
            sizeZ = sizeZ + 2;

            if (!this.isSafeToStandAt(i, (int)posVec31.y, j, sizeX, sizeY, sizeZ, posVec31, d0, d1))
            {
                return false;
            }
            else
            {
                sizeX = sizeX - 2;
                sizeZ = sizeZ - 2;
                double d4 = 1.0D / Math.abs(d0);
                double d5 = 1.0D / Math.abs(d1);
                double d6 = (double)i - posVec31.x;
                double d7 = (double)j - posVec31.z;

                if (d0 >= 0.0D)
                {
                    ++d6;
                }

                if (d1 >= 0.0D)
                {
                    ++d7;
                }

                d6 = d6 / d0;
                d7 = d7 / d1;
                int k = d0 < 0.0D ? -1 : 1;
                int l = d1 < 0.0D ? -1 : 1;
                int i1 = Mth.floor(posVec32.x);
                int j1 = Mth.floor(posVec32.z);
                int k1 = i1 - i;
                int l1 = j1 - j;

                while (k1 * k > 0 || l1 * l > 0)
                {
                    if (d6 < d7)
                    {
                        d6 += d4;
                        i += k;
                        k1 = i1 - i;
                    }
                    else
                    {
                        d7 += d5;
                        j += l;
                        l1 = j1 - j;
                    }

                    if (!this.isSafeToStandAt(i, (int)posVec31.y, j, sizeX, sizeY, sizeZ, posVec31, d0, d1))
                    {
                        return false;
                    }
                }

                return true;
            }
        }
    }


    private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 p_179692_7_, double p_179692_8_, double p_179692_10_)
    {
        for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1)))
        {
            double d0 = (double)blockpos.getX() + 0.5D - p_179692_7_.x;
            double d1 = (double)blockpos.getZ() + 0.5D - p_179692_7_.z;

            if (d0 * p_179692_8_ + d1 * p_179692_10_ >= 0.0D)
            {
                if (!this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathComputationType.LAND))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 vec31, double p_179683_8_, double p_179683_10_)
    {
        int i = x - sizeX / 2;
        int j = z - sizeZ / 2;

        if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, p_179683_8_, p_179683_10_))
        {
            return false;
        }
        else
        {
            for (int k = i; k < i + sizeX; ++k)
            {
                for (int l = j; l < j + sizeZ; ++l)
                {
                    double d0 = (double)k + 0.5D - vec31.x;
                    double d1 = (double)l + 0.5D - vec31.z;

                    if (d0 * p_179683_8_ + d1 * p_179683_10_ >= 0.0D)
                    {
                        BlockPathTypes pathnodetype = ((NpcWalkNodeEvaluator)nodeEvaluator).getCachedBlockType(this.mob, k, y - 1, l);

                        if (pathnodetype == BlockPathTypes.WATER)
                        {
                            return false;
                        }

                        if (pathnodetype == BlockPathTypes.LAVA)
                        {
                            return false;
                        }

                        if (pathnodetype == BlockPathTypes.OPEN)
                        {
                            return false;
                        }

                        pathnodetype = ((NpcWalkNodeEvaluator)nodeEvaluator).getCachedBlockType(this.mob, k, y, l);
                        float f = this.mob.getPathfindingMalus(pathnodetype);

                        if (f < 0.0F || f >= 8.0F)
                        {
                            return false;
                        }

                        if (pathnodetype == BlockPathTypes.DAMAGE_FIRE || pathnodetype == BlockPathTypes.DANGER_FIRE || pathnodetype == BlockPathTypes.DAMAGE_OTHER)
                        {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }
}
