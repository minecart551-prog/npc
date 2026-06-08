package noppes.npcs.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import noppes.npcs.entity.EntityNpcSlime;
import noppes.npcs.shared.client.model.NopModelPart;

@Environment(EnvType.CLIENT)
public class ModelNpcSlime<T extends EntityNpcSlime> extends EntityModel<T>
{
    NopModelPart outerBody;
    NopModelPart innerBody;

    /** The slime's right eye */
    NopModelPart slimeRightEye;

    /** The slime's left eye */
    NopModelPart slimeLeftEye;

    /** The slime's mouth */
    NopModelPart slimeMouth;

    public ModelNpcSlime(int par1) {
        outerBody = new NopModelPart(64, 64, 0, 0);
        this.outerBody = new NopModelPart(64, 64, 0, 0);
        this.outerBody.addBox(-8.0F, 32.0F, -8.0F, 16, 16, 16);
        //this.outerBody.addBox(-8.0F, 32.0F, -8.0F, 16, 16, 16);

        if (par1 > 0)
        {
            this.innerBody = new NopModelPart(64, 64, 0, 32);
            this.innerBody.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
            
            this.slimeRightEye = new NopModelPart(64, 64, 0, 0);
            this.slimeRightEye.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
            this.slimeLeftEye = new NopModelPart(64, 64, 0, 4);
            this.slimeLeftEye.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
            this.slimeMouth = new NopModelPart(64, 64, 0, 8);
            this.slimeMouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
        }
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack mStack, VertexConsumer iVertex, int lightmapUV, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if(innerBody != null){
            this.innerBody.render(mStack, iVertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
        }
        else{
            mStack.pushPose();
            mStack.scale(0.5f, 0.5f, 0.5f);
            this.outerBody.render(mStack, iVertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
            mStack.popPose();
        }

        if (this.slimeRightEye != null)
        {
            this.slimeRightEye.render(mStack, iVertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
            this.slimeLeftEye.render(mStack, iVertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
            this.slimeMouth.render(mStack, iVertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
        }
    }
}
