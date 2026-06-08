package noppes.npcs.controllers.data;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.IPlayerSkin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerSkinData implements IPlayerSkin {
    private boolean isMale = true;
    private int body;
    private int bodyColor;
    private int hair;
    private int hairColor;
    private int face;
    private int eyesColor;
    private int leg;
    private int jacket;
    private int shoes;
    private List<Integer> peculiarities;
    private boolean isActive;

    private ResourceLocation cacheResLoc = null;
    private boolean hasChanged;
    private static boolean skinsNeedResync;

    @Override
    public boolean isMale() {
        return isMale;
    }

    @Override
    public PlayerSkinData setMale(boolean male) {
        isMale = male;
        markChanged();
        return this;
    }

    public String getGender(){
        return isMale ? "male":"female";
    }

    @Override
    public int getBodyType() {
        return body;
    }

    @Override
    public PlayerSkinData setBodyType(int body) {
        this.body = body;
        markChanged();
        return this;
    }

    @Override
    public int getBodyColor() {
        return bodyColor;
    }

    @Override
    public PlayerSkinData setBodyColor(int bodyColor) {
        this.bodyColor = bodyColor;
        markChanged();
        return this;
    }

    @Override
    public int getHairType() {
        return hair;
    }

    @Override
    public PlayerSkinData setHairType(int hair) {
        this.hair = hair;
        markChanged();
        return this;
    }

    @Override
    public int getHairColor() {
        return hairColor;
    }

    @Override
    public PlayerSkinData setHairColor(int hairColor) {
        this.hairColor = hairColor;
        markChanged();
        return this;
    }

    @Override
    public int getFaceType() {
        return face;
    }

    @Override
    public PlayerSkinData setFaceType(int face) {
        this.face = face;
        markChanged();
        return this;
    }

    @Override
    public int getEyesColor() {
        return eyesColor;
    }

    @Override
    public PlayerSkinData setEyesColor(int eyesColor) {
        this.eyesColor = eyesColor;
        markChanged();
        return this;
    }

    @Override
    public int getPantsType() {
        return leg;
    }

    @Override
    public PlayerSkinData setPantsType(int leg) {
        this.leg = leg;
        markChanged();
        return this;
    }

    @Override
    public int getJacketType() {
        return jacket;
    }

    @Override
    public PlayerSkinData setJacketType(int jacket) {
        this.jacket = jacket;
        markChanged();
        return this;
    }

    @Override
    public int getShoesType() {
        return shoes;
    }

    @Override
    public PlayerSkinData setShoesType(int shoes) {
        this.shoes = shoes;
        markChanged();
        return this;
    }

    @Override
    public List<Integer> getPeculiarities() {
        return peculiarities;
    }

    @Override
    public PlayerSkinData setPeculiarities(List<Integer> peculiarities) {
        this.peculiarities = peculiarities;
        markChanged();
        return this;
    }

    public void markChanged() {
        calculateResLoc();
        skinsNeedResync = true;
        hasChanged = true;
        isActive = true;
    }

    public boolean hasChanged(){
        return hasChanged;
    }

    public void markSynced(){
        hasChanged = false;
    }

    public boolean isActive(){
        return isActive;
    }

    private void calculateResLoc() {
        StringBuilder path = new StringBuilder("textures/entity/custom/");
        path.append(getGender()).append("_");
        path.append(getBodyType()).append("_");
        path.append(getBodyColor()).append("_");
        path.append(getHairType()).append("_");
        path.append(getHairColor()).append("_");
        path.append(getFaceType()).append("_");
        path.append(getEyesColor()).append("_");
        path.append(getPantsType()).append("_");
        path.append(getJacketType()).append("_");
        path.append(getShoesType());

        for (int id : peculiarities) {
            path.append("_").append(id);
        }
        path.append(".png");
        cacheResLoc = new ResourceLocation(CustomNpcs.MODID, path.toString());
    }

    public ResourceLocation getResLoc() {
        if (cacheResLoc == null) calculateResLoc();
        return cacheResLoc;
    }

    @Environment(EnvType.CLIENT)
    public ResourceLocation getPartResLocByNumber(ResourceManager textureManager, String name, int partNum){
        ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + getGender() + "/"+name+"/" + partNum + ".png");
        if (textureManager.getResource(loc).isEmpty()) {
            loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" +  getGender() + "/"+name+"/0.png");
        }
        if (!textureManager.getResource(loc).isEmpty()) {
            return loc;
        }
        return null;
    }

    public CompoundTag saveNBTData(CompoundTag tag){
        tag.putBoolean("isMale", isMale);
        tag.putInt("body",body);
        tag.putInt("bodyColor",bodyColor);
        tag.putInt("hair",hair);
        tag.putInt("hairColor",hairColor);
        tag.putInt("face",face);
        tag.putInt("eyesColor",eyesColor);
        tag.putInt("leg",leg);
        tag.putInt("jacket",jacket);
        tag.putInt("shoes",shoes);
        tag.putIntArray("peculiarities",peculiarities);
        tag.putBoolean("isActive", isActive);
        return tag;
    }

    public void loadNBTData(CompoundTag tag){
        isMale = tag.getBoolean("isMale");
        body = tag.getInt("body");
        bodyColor = tag.getInt("bodyColor");
        hair = tag.getInt("hair");
        hairColor = tag.getInt("hairColor");
        face = tag.getInt("face");
        eyesColor = tag.getInt("eyesColor");
        leg = tag.getInt("leg");
        jacket = tag.getInt("jacket");
        shoes = tag.getInt("shoes");
        peculiarities = Arrays.stream(tag.getIntArray("peculiarities")).boxed().collect(Collectors.toList());
        isActive = tag.getBoolean("isActive");
    }

    public static boolean needsAnyResync(){
        return skinsNeedResync;
    }

    public static void resyncPerformed(){
        skinsNeedResync = false;
    }
}
