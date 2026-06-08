package noppes.npcs.client.parts;

import net.minecraft.client.model.geom.ModelPart;
import noppes.npcs.shared.client.model.NopModelPart;
import noppes.npcs.shared.common.util.NopVector3f;

import java.util.HashMap;
import java.util.Map;

public class ModelPartWrapper {
    protected ModelPart mcPart = null;
    protected NopModelPart mpmPart = null;
    public final NopVector3f oriPos;
    public final NopVector3f oriRot;

    public final NopVector3f oriScale;

    public Map<Integer, AnimationContainer> animations = new HashMap<>();

    public ModelPartWrapper(ModelPart mcPart, NopVector3f oriPos, NopVector3f oriRot){
        this.mcPart = mcPart;
        this.oriRot = oriRot;
        this.oriPos = oriPos;
        this.oriScale = new NopVector3f(1,1,1);
    }

    public ModelPartWrapper(NopModelPart mpmPart, NopVector3f oriPos, NopVector3f oriRot){
        this.mpmPart = mpmPart;
        this.oriRot = oriRot;
        this.oriPos = oriPos;
        this.oriScale = new NopVector3f(1,1,1);
    }

    public NopVector3f getPos(){
        if(mcPart != null){
            return new NopVector3f(mcPart.x, mcPart.y, mcPart.z);
        }
        return new NopVector3f(mpmPart.x, mpmPart.y, mpmPart.z);
    }

    public void setPos(NopVector3f pos){
        if(mcPart != null){
            mcPart.setPos(pos.x, pos.y, pos.z);
        }
        else{
            mpmPart.setPos(pos.x, pos.y, pos.z);
        }
    }

    public NopVector3f getRot(){
        if(mcPart != null){
            return new NopVector3f(mcPart.xRot, mcPart.yRot, mcPart.zRot);
        }
        return new NopVector3f(mpmPart.xRot, mpmPart.yRot, mpmPart.zRot);
    }

    public void setRot(NopVector3f rot){
        if(mcPart != null){
            mcPart.setRotation(rot.x, rot.y, rot.z);
        }
        else{
            mpmPart.setRotation(rot);
        }
    }

    public NopVector3f getScale(){
        if(mcPart != null){
            return new NopVector3f(mcPart.xScale, mcPart.yScale, mcPart.zScale);
        }
        return mpmPart.scale;
    }

    public void setScale(NopVector3f scale){
        if(mcPart != null){
            mcPart.xScale = scale.x;
            mcPart.yScale = scale.y;
            mcPart.zScale = scale.z;
        }
        else{
            mpmPart.scale = scale;
        }
    }


    public void setVisible(boolean b) {
        if(mcPart != null){
            mcPart.visible = b;
        }
        else{
            mpmPart.visible = b;
        }
    }
}
