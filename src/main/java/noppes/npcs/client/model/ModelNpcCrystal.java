package noppes.npcs.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import noppes.npcs.shared.client.model.NopModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ModelNpcCrystal extends EntityModel
{
    private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
    private NopModelPart field_41057_g;
    private NopModelPart field_41058_h;
    private NopModelPart field_41059_i;

    public ModelNpcCrystal()
    {
        field_41058_h = new NopModelPart(64, 32, 0, 0);
        field_41058_h.addBox(-4F, -4F, -4F, 8, 8, 8);
        field_41057_g = new NopModelPart(64, 32, 32, 0);
        field_41057_g.addBox(-4F, -4F, -4F, 8, 8, 8);
        field_41059_i = new NopModelPart(64, 32, 0, 16);
        field_41059_i.addBox(-6F, 16.0F, -6F, 12, 4, 12);
    }
    float ticks, tickCount;

    @Override
    public void setupAnim(Entity p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {

    }

    @Override
    public void prepareMobModel(Entity par1EntityLiving, float f6, float f5, float par9){
    	ticks = par9;
        tickCount = par1EntityLiving.tickCount;
    }

    @Override
    public void renderToBuffer(PoseStack mStack, VertexConsumer ivertex, int lightmapUV, int packedOverlayIn, float red, float green, float blue, float alpha) {
        mStack.pushPose();
        mStack.scale(2.0F, 2.0F, 2.0F);
        mStack.translate(0.0F, -0.5F, 0.0F);
        field_41059_i.render(mStack, ivertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);

        float f = (float)tickCount + ticks;
        float f1 = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = f1 * f1 + f1;
        
        float par3 = f * 3F;
        float par4 = f1 * 0.2F;
        mStack.mulPose(Axis.YP.rotationDegrees(par3));
        mStack.translate(0.0F, 0.1F + par4, 0.0F);
        mStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        field_41058_h.render(mStack, ivertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
        float sca = 0.875F;
        mStack.scale(sca, sca, sca);
        mStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        mStack.mulPose(Axis.YP.rotationDegrees(par3));
        field_41058_h.render(mStack, ivertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
        mStack.scale(sca, sca, sca);
        mStack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
        mStack.mulPose(Axis.YP.rotationDegrees(par3));
        field_41057_g.render(mStack, ivertex, lightmapUV, packedOverlayIn, red, green, blue, alpha);
        mStack.popPose();
    }

}
