package noppes.npcs.client.parts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.shared.client.model.Model2DRenderer;
import noppes.npcs.shared.client.model.ModelPlaneRenderer;
import noppes.npcs.shared.client.model.NopModelPart;
import noppes.npcs.shared.common.util.NopVector2i;
import noppes.npcs.shared.common.util.NopVector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MpmPartSimple extends MpmPartAbstractClient{

    private NopModelPart model;

    public NopVector2i textureSize = NopVector2i.ZERO;

    @Override
    public void render(MpmPartData data, PoseStack mStack, VertexConsumer c, int lightmapUV, LivingEntity player) {
        mStack.pushPose();
        if(data.usePlayerSkin){
            Model2DRenderer.textureOverride = ((EntityCustomNpc)player).textureLocation;
        }
        if(model != null){
            translateAndRotate(mStack);
            model.render(mStack, c, lightmapUV, OverlayTexture.NO_OVERLAY, data.color.x, data.color.y, data.color.z, 1);
        }
        mStack.popPose();
        Model2DRenderer.textureOverride = null;
    }

    public void translateAndRotate(PoseStack pose) {
        pose.scale(scale.x, scale.y, scale.z);
        pose.translate(pos.x / 16.0F, pos.y / 16.0F, pos.z / 16.0F);
        if (rot.z != 0.0F) {
            pose.mulPose(Axis.ZP.rotation(rot.z));
        }

        if (rot.y != 0.0F) {
            pose.mulPose(Axis.YP.rotation(rot.y));
        }

        if (rot.x != 0.0F) {
            pose.mulPose(Axis.XP.rotation(rot.x));
        }
        float f = 0.0625f;
        pose.translate(rotatePoint.x * f, rotatePoint.y * f, rotatePoint.z * f);

    }

    @Override
    public void load(JsonObject renderData){
        if(renderData != null && renderData.size() > 0){
            textureSize = MpmPartReader.jsonVector2i(renderData.get("texture_size"));
            model = new NopModelPart(textureSize.x, textureSize.y, 0, 0);
            JsonArray parts = renderData.get("parts").getAsJsonArray();
            Map<String, NopModelPart> allParts = new HashMap<>();
            for(int i = 0; i < parts.size(); i++){
                JsonObject part = parts.get(i).getAsJsonObject();
                String name = part.has("name") ? part.get("name").getAsString() : UUID.randomUUID().toString();
                NopVector2i texturePosition = MpmPartReader.jsonVector2i(part.get("texture_position"));
                NopVector2i partSize = MpmPartReader.jsonVector2i(part.get("part_size"));
                NopVector3f translate = MpmPartReader.jsonVector3f(part.get("translate"));
                NopVector3f rotate = MpmPartReader.jsonVector3f(part.get("rotate")).mul((float)Math.PI / 180F);
                NopVector3f scale = MpmPartReader.jsonVector3fOrOne(part.get("scale"));
                NopVector3f rotatePoint = MpmPartReader.jsonVector3f(part.get("rotate_offset"));

                NopModelPart mr;
                if(part.has("empty") && part.get("empty").getAsBoolean()){
                    mr = new NopModelPart(textureSize.x, textureSize.y, 0, 0);
                    mr.scale = scale;
                }
                else if(part.has("plane") && part.get("plane").getAsBoolean()){
                    Direction direction = part.has("direction") ? Direction.valueOf(part.get("direction").getAsString().toUpperCase()) : Direction.NORTH;
                    mr = new ModelPlaneRenderer(textureSize.x, textureSize.y, texturePosition.x, texturePosition.y).mirror(part.has("mirror") && part.get("mirror").getAsBoolean())
                            .addPlane(rotatePoint.x, rotatePoint.y, rotatePoint.z, partSize.x, partSize.y, scale, direction);
                }
                else{
                    mr = new Model2DRenderer(textureSize.x, textureSize.y, texturePosition.x, texturePosition.y, partSize.x, partSize.y, texture)
                            .setScale(scale).setRotationOffset(rotatePoint);
                }
                mr.setRotation(rotate).setPos(translate);
                if(part.has("mirror") && part.get("mirror").getAsBoolean()){
                    mr.mirror = true;
                }
                defaultPose.put(name, new ModelPartWrapper(mr, translate, rotate));
                allParts.put(name, mr);

                String parent = part.has("parent") ? part.get("parent").getAsString() : null;
                if(allParts.containsKey(parent)){
                    allParts.get(parent).addChild(name, mr);
                }
                else{
                    model.addChild(name, mr);
                }
            }
            defaultPose.put(null, new ModelPartWrapper(model, translate, rotate.mul((float)Math.PI / 180F)));
            model.setRotation(this.rotate.mul((float)Math.PI / 180F));
            model.setPos(translate);
        }
    }
}
