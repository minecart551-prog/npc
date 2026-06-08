package noppes.npcs.client.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;

public class ModelClassicPlayer<T extends LivingEntity> extends PlayerModel<T> {
    public ModelClassicPlayer(ModelPart p_170821_, float scale) {
        super(p_170821_, false);
    }

    @Override
    public void setupAnim(T entity, float par1, float limbSwingAmount, float par3, float par4, float par5) {
        super.setupAnim(entity, par1, limbSwingAmount, par3, par4, par5);

        float j = 2.0f;

        if (entity.isSprinting()) {
            j = 1.0f;
        }

        rightArm.xRot += Mth.cos(par1 * 0.6662F + (float) Math.PI) * j * limbSwingAmount;
        leftArm.xRot += Mth.cos(par1 * 0.6662F) * j * limbSwingAmount;
        leftArm.zRot += (Mth.cos(par1 * 0.2812F) - 1.0F) * limbSwingAmount;
        rightArm.zRot += (Mth.cos(par1 * 0.2312F) + 1.0F) * limbSwingAmount;

        leftSleeve.xRot = leftArm.xRot;
        leftSleeve.yRot = leftArm.yRot;
        leftSleeve.zRot = leftArm.zRot;
        rightSleeve.xRot = rightArm.xRot;
        rightSleeve.yRot = rightArm.yRot;
        rightSleeve.zRot = rightArm.zRot;
    }
}
