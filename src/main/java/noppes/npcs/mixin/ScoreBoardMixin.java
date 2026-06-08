package noppes.npcs.mixin;

import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(Scoreboard.class)
public interface ScoreBoardMixin {

    @Accessor(value="playerScores")
    Map<String, Map<Objective, Score>> getScores();

}
