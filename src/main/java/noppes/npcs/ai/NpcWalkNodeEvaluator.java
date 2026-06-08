package noppes.npcs.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class NpcWalkNodeEvaluator extends WalkNodeEvaluator {
    public BlockPathTypes getCachedBlockType(Mob p_77568_, int p_77569_, int p_77570_, int p_77571_) {
        return super.getCachedBlockType(p_77568_, p_77569_, p_77570_, p_77571_);
    }

    public void done() {
        PathNavigationRegion level = this.level;
        Mob mob = this.mob;
        super.done();
        this.level = level;
        this.mob = mob;
    }
}
