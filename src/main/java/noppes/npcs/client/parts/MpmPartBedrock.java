package noppes.npcs.client.parts;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.mixin.ModelPartMixin;
import noppes.npcs.shared.client.model.Model2DRenderer;
import noppes.npcs.shared.common.util.NopVector2i;
import noppes.npcs.shared.common.util.NopVector3f;

import java.util.HashMap;
import java.util.Map;

public class MpmPartBedrock extends MpmPartAbstractClient{
    public final Map<ResourceLocation, Model2DRenderer> playerModels = new HashMap<>();

    private ModelPart model;
    private ModelPartMixin modelMixin;
    public NopVector2i textureSize = NopVector2i.ZERO;

    @Override
    public void render(MpmPartData data, PoseStack mStack, VertexConsumer c, int lightmapUV, LivingEntity player) {
        mStack.pushPose();
        if(model != null){
            Map<String, ModelPart> children = modelMixin.getChildren();
            model.translateAndRotate(mStack);
            float f = 0.0625f;
            mStack.translate(-rotatePoint.x * f, -rotatePoint.y * f, -rotatePoint.z * f);
            mStack.scale(scale.x, scale.y, scale.z);

            for(ModelPart modelpart : children.values()) {
                modelpart.render(mStack, c, lightmapUV, OverlayTexture.NO_OVERLAY, data.color.x, data.color.y, data.color.z, 1);
            }
        }
        mStack.popPose();
    }

    @Override
    public void load(JsonObject renderData){
        if(renderData != null && renderData.size() > 0){
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition root = meshdefinition.getRoot();

            JsonObject ob = renderData.get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject();
            JsonObject description = ob.get("description").getAsJsonObject();
            textureSize = new NopVector2i(description.get("texture_width").getAsInt(), description.get("texture_height").getAsInt());
            JsonArray bones = ob.get("bones").getAsJsonArray();
            Map<String, PartDefinition> namedParts = new HashMap<>();
            Map<String, NopVector3f> parentPivots = new HashMap<>();
            Map<String, ModelPartWrapper> defaultPose = new HashMap<>();
            for(int i = 0; i < bones.size(); i++){
                JsonObject bone = bones.get(i).getAsJsonObject();
                String name = bone.get("name").getAsString();
                String pName = bone.has("parent") ? bone.get("parent").getAsString() : null;
                PartDefinition parent = pName != null && namedParts.containsKey(pName) ? namedParts.get(pName) : root;
                NopVector3f ppivot = parentPivots.containsKey(pName) ? parentPivots.get(pName) : NopVector3f.ZERO;
                //PartDefinitionMixin parentM = (PartDefinitionMixin)parent;

                NopVector3f pivot = MpmPartReader.jsonVector3f(bone.get("pivot"));
                parentPivots.put(name, pivot);
                NopVector3f rotation = MpmPartReader.jsonVector3f(bone.get("rotation")).mul((float)Math.PI / 180F);
                PartPose pose = PartPose.offsetAndRotation(pivot.x - ppivot.x, ppivot.y - pivot.y, pivot.z - ppivot.z, rotation.x, rotation.y, rotation.z);

                PartDefinition partDef = parent.addOrReplaceChild(name, CubeListBuilder.create(), pose);
                defaultPose.put(name, new ModelPartWrapper((ModelPart)null, new NopVector3f(pose.x, pose.y, pose.z), rotation));

                if(bone.has("cubes")){
                    JsonArray cubes = bone.get("cubes").getAsJsonArray();
                    for(int j = 0; j < cubes.size(); j++){
                        CubeListBuilder builder = CubeListBuilder.create();
                        JsonObject cube = cubes.get(j).getAsJsonObject();
                        NopVector2i uv = MpmPartReader.jsonVector2i(cube.get("uv"));
                        boolean mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
                        NopVector3f cpivot = MpmPartReader.jsonVector3f(cube.get("pivot"));
                        rotation = MpmPartReader.jsonVector3f(cube.get("rotation")).mul((float)Math.PI / 180F);
                        NopVector3f origin = MpmPartReader.jsonVector3f(cube.get("origin"));
                        NopVector3f size = MpmPartReader.jsonVector3f(cube.get("size"));
                        CubeDeformation deform = cube.has("inflate") ? new CubeDeformation(cube.get("inflate").getAsFloat()) : CubeDeformation.NONE;
                        builder.texOffs(uv.x, uv.y).mirror(mirror).addBox(origin.x - cpivot.x, cpivot.y - size.y - origin.y, origin.z - cpivot.z, size.x, size.y, size.z, deform);
                        partDef.addOrReplaceChild("cube_" + name + j, builder, PartPose.offsetAndRotation(cpivot.x - pivot.x, pivot.y -cpivot.y, cpivot.z - pivot.z, rotation.x, rotation.y, rotation.z));
                    }
                }
                namedParts.put(name, partDef);
            }
            model = LayerDefinition.create(meshdefinition, description.get("texture_width").getAsInt(), description.get("texture_height").getAsInt()).bakeRoot();
            model.setPos(translate.x, translate.y, translate.z);
            for(Map.Entry<String, ModelPartWrapper> entry : defaultPose.entrySet()){
                entry.getValue().mcPart = getChild(model, entry.getKey());
            }
            defaultPose.put(null, new ModelPartWrapper(model, translate, rotate));

            this.defaultPose = defaultPose;
            NopVector3f rotation = rotate.mul((float)Math.PI / 180F);
            model.setRotation(rotation.x, rotation.y, rotation.z);
            modelMixin = (ModelPartMixin)(Object)model;
        }
    }

    private ModelPart getChild(ModelPart root, String name){
        Map<String, ModelPart> children = ((ModelPartMixin)(Object)root).getChildren();
        if(children.containsKey(name)){
            return children.get(name);
        }
        for(ModelPart child : children.values()){
            ModelPart p = getChild(child, name);
            if(p != null){
                return p;
            }
        }
        return null;
    }
}
