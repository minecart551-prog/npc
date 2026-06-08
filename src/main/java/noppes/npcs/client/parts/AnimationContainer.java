package noppes.npcs.client.parts;

import noppes.npcs.shared.common.util.NopVector3f;

public class AnimationContainer {
    public final int animation;
    public final String part;
    public final int length;
    public final int actualLength;
    public final float speed;
    public final boolean additional;
    public final boolean loop;
    public boolean hasRotation = false;
    public boolean hasTranslate = false;
    public boolean hasScale = false;
    public final NopVector3f[] rotations;
    public final NopVector3f[] translates;
    public final NopVector3f[] scale;

    public int startupTicks = Integer.MAX_VALUE;

    public AnimationContainer(int animation, String part, int length, float speed, boolean additional, boolean loop){
        this.animation = animation;
        this.part = part;
        this.length = length;
        this.speed = speed;
        this.additional = additional;
        this.loop = loop;

        if(loop){
            this.actualLength = length;
        }
        else{
            this.actualLength = length > 2 ? length * 2 - 2 : length;
        }
        this.rotations = new NopVector3f[actualLength];
        this.translates = new NopVector3f[actualLength];
        this.scale = new NopVector3f[actualLength];
    }

    public void start(){
        startupTicks = 0;
    }

    public void animation(ModelPartWrapper part, int step, float partialTick){
        float f = step / 20f * speed;
        int i = (int)f;
        float pf = (step - 1) / 20f * speed;
        int pi = (int)pf;
        if(pi != i){
            this.step(part, i, (f - i) * partialTick);
        }
        else{
            this.step(part, i, (pf - pi) + (f - pf) * partialTick);
        }
        if(startupTicks < actualLength && i != pi){
            startupTicks++;
        }
    }

    // where step is between 0 and 1
    public void animation(ModelPartWrapper part, float step){
        float f = step * speed * (length - 1);
        int i = (int)f;
        this.step(part, i, f - i);
    }

    public void step(ModelPartWrapper part, int step, float progress){
        int i = step % actualLength;
        int j = (step + 1) % actualLength;
        if(startupTicks < 60){
            if(hasRotation){
                part.setRot(part.getRot().lerp(rotations[i].lerp(rotations[j], progress), 0.15f));
            }
            else{
                part.setRot(part.getRot().lerp(part.oriRot, 0.15f));
            }
            if(hasTranslate){
                part.setPos(part.getPos().lerp(translates[i].lerp(translates[j], progress), 0.15f));
            }
            else{
                part.setPos(part.getPos().lerp(part.oriPos, 0.15f));
            }
            if(hasScale){
                part.setScale(part.getScale().lerp(scale[i].lerp(scale[j], progress), 0.15f));
            }
            else{
                part.setScale(part.getScale().lerp(part.oriScale, 0.15f));
            }
        }
        else{
            if(hasRotation){
                if(loop && j < i){
                    part.setRot(rotations[i].subtract(NopVector3f.ROTATION).modulo(NopVector3f.ROTATION).lerp(rotations[j], progress));
                }
                else{
                    part.setRot(rotations[i].lerp(rotations[j], progress));
                }
            }
            if(hasTranslate){
                part.setPos(translates[i].lerp(translates[j], progress));
            }
            if(hasScale){
                part.setScale(scale[i].lerp(scale[j], progress));
            }
        }

    }

    public AnimationContainer copy() {
        AnimationContainer container = new AnimationContainer(animation, part, length, speed, additional, loop);
        container.hasRotation = hasRotation;
        container.hasTranslate = hasTranslate;
        container.hasScale = hasScale;
        for(int i = 0; i < actualLength; i++){
            if(i < length){
                if(hasTranslate){
                    container.translates[i] = translates[i];
                }
                if(hasRotation){
                    container.rotations[i] = rotations[i];
                }
                if(hasScale){
                    container.scale[i] = scale[i];
                }
            }
            else{
                if(hasTranslate) {
                    container.translates[i] = container.translates[length - i % length - 2];
                }
                if(hasRotation) {
                    container.rotations[i] = container.rotations[length - i % length - 2];
                }
                if(hasScale) {
                    container.scale[i] = container.scale[length - i % length - 2];
                }
            }
        }
        return container;
    }
}
