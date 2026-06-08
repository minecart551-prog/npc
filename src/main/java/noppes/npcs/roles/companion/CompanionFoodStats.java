package noppes.npcs.roles.companion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import noppes.npcs.entity.EntityNPCInterface;

public class CompanionFoodStats {
    /** The player's food level. */
    private int foodLevel = 20;
    /** The player's food saturation. */
    private float foodSaturationLevel = 5.0F;
    /** The player's food exhaustion. */
    private float foodExhaustionLevel;
    /** The player's food timer value. */
    private int foodTimer;
    private int prevFoodLevel = 20;

    /**
     * Args: int foodLevel, float foodSaturationModifier
     */
    private void addStats(int p_75122_1_, float p_75122_2_){
        this.foodLevel = Math.min(p_75122_1_ + this.foodLevel, 20);
        this.foodSaturationLevel = Math.min(this.foodSaturationLevel + (float)p_75122_1_ * p_75122_2_ * 2.0F, (float)this.foodLevel);
    }

    public void onFoodEaten(FoodProperties food, ItemStack itemstack){
        this.addStats(food.getNutrition(), food.getSaturationModifier());
    }

    /**
     * Handles the food game logic.
     */
    public void onUpdate(EntityNPCInterface npc){
        Difficulty enumdifficulty = npc.level().getDifficulty();
        this.prevFoodLevel = this.foodLevel;

        if (this.foodExhaustionLevel > 4.0F){
            this.foodExhaustionLevel -= 4.0F;

            if (this.foodSaturationLevel > 0.0F)
            {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            }
            else if (enumdifficulty != Difficulty.PEACEFUL)
            {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        if (npc.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION) && this.foodLevel >= 18 && npc.getHealth() > 0.0F && npc.getHealth() < npc.getMaxHealth())
        {
            ++this.foodTimer;

            if (this.foodTimer >= 80){
                npc.heal(1.0F);
                this.addExhaustion(3.0F);
                this.foodTimer = 0;
            }
        }
        else if (this.foodLevel <= 0){
            ++this.foodTimer;

            if (this.foodTimer >= 80){
                if (npc.getHealth() > 10.0F || enumdifficulty == Difficulty.HARD || npc.getHealth() > 1.0F && enumdifficulty == Difficulty.NORMAL)
                {
                    npc.hurt(npc.damageSources().starve(), 1.0F);
                }

                this.foodTimer = 0;
            }
        }
        else
        {
            this.foodTimer = 0;
        }
    }

    /**
     * Reads food stats from an NBT object.
     */
    public void readNBT(CompoundTag compound)
    {
        if (compound.contains("foodLevel", 99))
        {
            this.foodLevel = compound.getInt("foodLevel");
            this.foodTimer = compound.getInt("foodTickTimer");
            this.foodSaturationLevel = compound.getFloat("foodSaturationLevel");
            this.foodExhaustionLevel = compound.getFloat("foodExhaustionLevel");
        }
    }

    /**
     * Writes food stats to an NBT object.
     */
    public void writeNBT(CompoundTag compound)
    {
        compound.putInt("foodLevel", this.foodLevel);
        compound.putInt("foodTickTimer", this.foodTimer);
        compound.putFloat("foodSaturationLevel", this.foodSaturationLevel);
        compound.putFloat("foodExhaustionLevel", this.foodExhaustionLevel);
    }

    /**
     * Get the player's food level.
     */
    public int getFoodLevel(){
        return this.foodLevel;
    }

    @Environment(EnvType.CLIENT)
    public int getPrevFoodLevel(){
        return this.prevFoodLevel;
    }

    /**
     * If foodLevel is not max.
     */
    public boolean needFood(){
        return this.foodLevel < 20;
    }

    /**
     * adds input to foodExhaustionLevel to a max of 40
     */
    public void addExhaustion(float p_75113_1_){
        this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + p_75113_1_, 40.0F);
    }

    /**
     * Get the player's food saturation level.
     */
    public float getSaturationLevel(){
        return this.foodSaturationLevel;
    }

    @Environment(EnvType.CLIENT)
    public void setFoodLevel(int p_75114_1_){
        this.foodLevel = p_75114_1_;
    }

    @Environment(EnvType.CLIENT)
    public void setFoodSaturationLevel(float p_75119_1_){
        this.foodSaturationLevel = p_75119_1_;
    }
}
