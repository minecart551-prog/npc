package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.gui.IEntityDisplay;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.shared.common.util.NopVector2f;


public class CustomGuiEntityDisplayWrapper extends CustomGuiComponentWrapper implements IEntityDisplay {

    private IEntity entity;
    private INbt entityData = new NBTWrapper(new CompoundTag());
    public int entityId = -1;
    private int rotation;
    public boolean isFollowingCursor = true;
    private float scale = 1.0f;
    private boolean showBackground = true;
    private float offsetX = 0, offsetY = 0;

    public CustomGuiEntityDisplayWrapper(){}

    public CustomGuiEntityDisplayWrapper(int id, IEntity entity, int x, int y) {
        setID(id);
        setEntity(entity);
        setPos(x,y);
    }

    @Override
    public IEntity getEntity() {
        return entity;
    }

    public INbt getEntityData(){
        return this.entityData;
    }

    @Override
    public IEntityDisplay setEntity(IEntity entity) {
        this.entity = entity;
        if(entity == null){
            this.entityData = new NBTWrapper(new CompoundTag());
        }
        else{
            this.entityData = entity.getEntityNbt();
        }
        if(entity!=null && entity.getMCEntity() instanceof Player){
            entityId = entity.getMCEntity().getId();
        }
        return this;
    }

    @Override
    public int getRotation() {
        return rotation;
    }

    @Override
    public IEntityDisplay setRotation(int rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public boolean isFollowingCursor() {
        return isFollowingCursor;
    }

    @Override
    public IEntityDisplay setFollowingCursor(boolean state) {
        this.isFollowingCursor = state;
        return this;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public IEntityDisplay setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public boolean getBackground() {
        return showBackground;
    }

    @Override
    public IEntityDisplay setBackground(boolean bo) {
        showBackground = bo;
        return this;
    }

    @Override
    public int getType() {
        return GuiComponentType.ENTITY_DISPLAY;
    }

    @Override
    public CompoundTag toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.put("entity", entityData.getMCNBT());
        compound.putInt("entityId", entityId);
        compound.putInt("rotation", rotation);
        compound.putFloat("scale", scale);
        compound.putBoolean("followCursor", isFollowingCursor);
        compound.putBoolean("background", showBackground);
        return compound;
    }

    @Override
    public CustomGuiComponentWrapper fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        this.entityData = NpcAPI.Instance().getINbt(compound.getCompound("entity"));
        this.entityId = compound.getInt("entityId");
        setRotation(compound.getInt("rotation"));
        setScale(compound.getFloat("scale"));
        setFollowingCursor(compound.getBoolean("followCursor"));
        setBackground(compound.getBoolean("background"));
        return this;
    }

}
