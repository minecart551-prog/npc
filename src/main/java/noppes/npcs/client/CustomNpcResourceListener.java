package noppes.npcs.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.parts.MpmPartReader;
import noppes.npcs.shared.client.model.util.CustomRenderStates;
import noppes.npcs.shared.client.util.TextureCache;

import java.io.IOException;

public class CustomNpcResourceListener implements ResourceManagerReloadListener {

	public static int DefaultTextColor = 0x404040;
	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		try{
			DefaultTextColor = Integer.parseInt(I18n.get("customnpcs.defaultTextColor"),16);
		}
		catch(NumberFormatException e){
			DefaultTextColor = 0x404040;
		}
		GuiTextureSelection.clear();
		MpmPartReader.reload();

		RenderSystem.recordRenderCall(() -> {
			try {
				CustomRenderStates.posTexNormalShader = new ShaderInstance(manager, "position_tex_normal", CustomRenderStates.POS_TEX_NORMAL);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		//createTextureCache();

		//EntityEnderFX.portalSprite = ((ParticleManagerMixin)Minecraft.getInstance().particleEngine).getPacks().get(Registry.PARTICLE_TYPE.getKey(ParticleTypes.PORTAL));

	}

	private void createTextureCache(){
		enlargeTexture("acacia_planks");
		enlargeTexture("birch_planks");
		enlargeTexture("crimson_planks");
		enlargeTexture("dark_oak_planks");
		enlargeTexture("jungle_planks");
		enlargeTexture("oak_planks");
		enlargeTexture("spruce_planks");
		enlargeTexture("warped_planks");
		enlargeTexture("iron_block");
		enlargeTexture("diamond_block");
		enlargeTexture("stone");
		enlargeTexture("gold_block");
		enlargeTexture("white_wool");
	}
	
	private void enlargeTexture(String texture){
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        ResourceLocation location = new ResourceLocation("customnpcs:textures/cache/" + texture + ".png");
		AbstractTexture ob = manager.getTexture(location);
    	if(!(ob instanceof TextureCache)){
    		ob = new TextureCache(location, new ResourceLocation("textures/block/" + texture + ".png"));
    		manager.register(location, ob);
    	}
	}
}
