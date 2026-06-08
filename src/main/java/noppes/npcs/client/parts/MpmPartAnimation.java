package noppes.npcs.client.parts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.shared.common.util.NopVector3f;

import java.util.*;

public class MpmPartAnimation {
    private Map<Integer, ModelPartWrapper[]> animations = new HashMap<>();

    public void load(List<AnimationContainer> animationsList, MpmPart part){
        if(animationsList == null || animationsList.size() == 0){
            return;
        }
        animationsList.forEach(container -> {
            ModelPartWrapper[] list = animations.computeIfAbsent(container.animation, k -> new ModelPartWrapper[0]);
            ModelPartWrapper model = part.getPart(container.part);
            if(model != null){
                if(container.additional){
                    container = container.copy();
                    for(int i = 0; i < container.actualLength; i++){
                        if(i < container.length) {
                            if(container.hasTranslate){
                                container.translates[i] = container.translates[i].add(model.oriPos);
                            }
                            if(container.hasRotation){
                                container.rotations[i] = container.rotations[i].add(model.oriRot);
                            }
                        }
                        else{
                            if(container.hasTranslate){
                                container.translates[i] = container.translates[container.length - i % container.length - 2];
                            }
                            if(container.hasRotation){
                                container.rotations[i] = container.rotations[container.length - i % container.length - 2];
                            }
                        }
                    }
                }
                model.animations.put(container.animation, container);
                list = Arrays.copyOf(list, list.length + 1);
                list[list.length - 1] = model;
                animations.put(container.animation, list);
            }
        });
    }

    public static List<AnimationContainer> loadAnimations(JsonObject json){
        List<AnimationContainer> list = new ArrayList<>();
        if(json == null || json.size() == 0){
            return list;
        }
        for(Map.Entry<String, JsonElement> entry : json.entrySet()){
            try{
                int animation = AnimationType.valueOf(entry.getKey().toUpperCase());
                JsonObject animationData = entry.getValue().getAsJsonObject();
                int length = animationData.get("animation_length").getAsInt();
                float speed = animationData.get("animation_speed").getAsFloat();
                boolean loop = animationData.has("loop") && animationData.get("loop").getAsBoolean();
                boolean additional = animationData.has("additional") && animationData.get("additional").getAsBoolean();
                for(Map.Entry<String, JsonElement> bone : animationData.get("bones").getAsJsonObject().entrySet()){
                    list.removeIf(c -> c.animation == animation && c.part.equals(bone.getKey()));
                    AnimationContainer con = new AnimationContainer(animation, bone.getKey(), length, speed, additional, loop);
                    list.add(con);
                    JsonObject boneAnimation = bone.getValue().getAsJsonObject();
                    if(boneAnimation.has("rotation")){
                        con.hasRotation = true;
                        JsonArray rotArray = boneAnimation.get("rotation").getAsJsonArray();
                        for(int i = 0; i < con.actualLength; i++){
                            if(i < length){
                                con.rotations[i] = MpmPartReader.jsonVector3f(rotArray.get(i)).mul((float)Math.PI / 180F);
                            }
                            else{
                                con.rotations[i] = con.rotations[length - i % length - 2];
                            }
                        }
                    }
                    if(boneAnimation.has("translate")){
                        con.hasTranslate = true;
                        JsonArray tranArray = boneAnimation.get("translate").getAsJsonArray();
                        for(int i = 0; i < con.actualLength; i++){
                            if(i < length){
                                con.translates[i] = MpmPartReader.jsonVector3f(tranArray.get(i));
                            }
                            else{
                                con.translates[i] = con.translates[length - i % length - 2];
                            }
                        }
                    }
                    else{
                        for(int i = 0; i < con.actualLength; i++){
                            con.translates[i] = NopVector3f.ZERO;
                        }
                    }
                    if(boneAnimation.has("scale")){
                        con.hasScale = true;
                        JsonArray tranArray = boneAnimation.get("scale").getAsJsonArray();
                        for(int i = 0; i < con.actualLength; i++){
                            if(i < length){
                                con.scale[i] = MpmPartReader.jsonVector3f(tranArray.get(i));
                            }
                            else{
                                con.scale[i] = con.scale[length - i % length - 2];
                            }
                        }
                    }
                    else{
                        for(int i = 0; i < con.actualLength; i++){
                            con.scale[i] = NopVector3f.ZERO;
                        }
                    }
                }
            }
            catch(Exception e){
                throw new CustomNPCsException(e, "Error in animation: " + entry.getKey());
            }
        }
        return list;
    }

    public void start(int animation){
        ModelPartWrapper[] models = animations.get(animation);
        if(models != null){
            for(int i = 0; i < models.length; i++){
                models[i].animations.get(animation).start();
            }
        }
    }

    public boolean animation(int animation, int step, float partialTick){
        ModelPartWrapper[] models = animations.get(animation);
        if(models != null){
            for(int i = 0; i < models.length; i++){
                ModelPartWrapper m = models[i];
                m.animations.get(animation).animation(m, step, partialTick);
            }
            return true;
        }
        return false;
    }

    public boolean animation(int animation, float step){
        ModelPartWrapper[] models = animations.get(animation);
        if(models != null){
            for(int i = 0; i < models.length; i++){
                ModelPartWrapper m = models[i];
                m.animations.get(animation).animation(m, step);
            }
            return true;
        }
        return false;
    }
}
