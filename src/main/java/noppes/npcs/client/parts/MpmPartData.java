package noppes.npcs.client.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.shared.client.util.ImageDownloadAlt;
import noppes.npcs.shared.client.util.NoppesStringUtils;
import noppes.npcs.shared.client.util.ResourceDownloader;
import noppes.npcs.shared.common.util.NopVector3f;

import java.io.File;

public class MpmPartData {
    public static final NopVector3f WHITE = new NopVector3f(1.0F, 1.0F, 1.0F);
    public ResourceLocation partId;
    public boolean usePlayerSkin = false;
    public NopVector3f color = WHITE;

    public ResourceLocation texture = null;
    private ResourceLocation textureUrl = null;

    public String url = "";

    public MpmPart getPart(){
        return MpmPartReader.PARTS.get(partId);
    }

    public ResourceLocation getTexture(){
        if(getUrlTexture() != null){
            return getUrlTexture();
        }
        if(texture != null){
            return texture;
        }
        MpmPart part = getPart();
        if(part != null && part.texture != null){
            return getPart().texture;
        }
        return MissingTextureAtlasSprite.getLocation();
    }

    public ResourceLocation getUrlTexture(){
        if(textureUrl != null){
            return textureUrl;
        }

        if(!url.isEmpty()){
            ResourceLocation resource = ResourceDownloader.getUrlResourceLocation(url, false);
            File file = ResourceDownloader.getUrlFile(url, false);
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            AbstractTexture object = texturemanager.getTexture(resource, null);
            if(object == null){
                textureUrl = getDefaultTexture();
                ResourceDownloader.load(new ImageDownloadAlt(file, url, resource, getDefaultTexture(), false, () -> textureUrl = resource));
            }
            else{
                textureUrl = resource;
            }
        }
        return textureUrl;
    }

    public void setTexture(String s){
        if(s == null || s.isEmpty()){
            this.texture = null;
        }
        else{
            this.texture = new ResourceLocation(s);
        }
    }

    public void setUrl(String url){
        if(NoppesStringUtils.areEqual(this.url, url)){
            return;
        }
        this.url = url;
        textureUrl = null;
    }

    public ResourceLocation getDefaultTexture(){
        if(texture != null){
            return texture;
        }
        return getPart().texture;
    }

    public int getColor(){
        int r = (int)(color.x * 255) << 16;
        int g = (int)(color.y * 255) << 8;
        int b = (int)(color.z * 255);
        return r + g + b;
    }

    public void setColor(int color){
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        this.color = new NopVector3f(r, g, b);
    }

    public CompoundTag getNbt() {
        CompoundTag item = new CompoundTag();
        item.putString("Id", partId.toString());
        item.putBoolean("UsePlayerSkin", usePlayerSkin);
        item.putString("Url", url);
        item.putString("Texture", texture == null ? "" : texture.toString());
        item.putFloat("ColorR", color.x);
        item.putFloat("ColorG", color.y);
        item.putFloat("ColorB", color.z);
        return item;
    }

    public void setNbt(CompoundTag compound) {
        partId = new ResourceLocation(compound.getString("Id"));
        usePlayerSkin = compound.getBoolean("UsePlayerSkin");
        setUrl(compound.getString("Url"));
        setTexture(compound.getString("Texture"));
        color = new NopVector3f(compound.getFloat("ColorR"), compound.getFloat("ColorG"), compound.getFloat("ColorB"));
    }
}
