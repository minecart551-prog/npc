package noppes.npcs.client;


import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.*;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.*;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.global.*;
import noppes.npcs.client.gui.mainmenu.*;
import noppes.npcs.client.gui.player.*;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionInv;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionStats;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionTalents;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeItem;
import noppes.npcs.client.gui.roles.*;
import noppes.npcs.client.gui.script.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.ArmorLayerMixin;
import noppes.npcs.mixin.MinecraftAccessor;
import noppes.npcs.shared.client.util.TrueTypeFont;
import noppes.npcs.shared.common.util.LogWriter;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ClientProxy extends CommonProxy {
	public static PlayerData playerData = new PlayerData();
	
	public static KeyMapping QuestLog;
	
	public static KeyMapping Scene1;
	public static KeyMapping SceneReset;
	public static KeyMapping Scene2;
	public static KeyMapping Scene3;
	
	public static FontContainer Font;

	public static ModelData data;
	public static PlayerModel playerModel;
	public static ArmorLayerMixin armorLayer;

	public ClientProxy(){
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(CustomItemModels::registerModels);
	}

	@Override
	public void load() {		

	}

	@Override
	public PlayerData getPlayerData(Player player) {
		if(player.getUUID() == Minecraft.getInstance().player.getUUID()) {
			if(playerData.player != player)
				playerData.player = player;
			return playerData;
		}
		return null;
	}
	
	@Override
	public void postload() {

	}

	public static void createFolders() {
		File file = new File(CustomNpcs.Dir,"assets/customnpcs");
		if(!file.exists())
			file.mkdirs();
		
		File check = new File(file,"sounds");
		if(!check.exists())
			check.mkdir();

		File json = new File(file, "sounds.json");
		if(!json.exists()){
			try {
				json.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(json));
				writer.write("{\n\n}");
				writer.close();
			} catch (IOException e) {
			}
		}

		File meta = new File(CustomNpcs.Dir, "pack.mcmeta");
		if(!meta.exists()){
			try {
				meta.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(meta));
				writer.write("{\n" +
						"    \"pack\": {\n" +
						"        \"description\": \"customnpcs map resource pack\",\n" +
						"        \"pack_format\": 6\n" +
						"    }\n" +
						"}");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		check = new File(file,"textures");
		if(!check.exists())
			check.mkdir();
		
	}

	public static Screen getGui(EnumGuiType gui, EntityNPCInterface npc, FriendlyByteBuf buf){
		try{
			if (gui == EnumGuiType.MainMenuDisplay) {
				if (npc != null)
					return new GuiNpcDisplay(npc);
				else
					Minecraft.getInstance().player.sendSystemMessage(Component.literal("Unable to find npc"));
			}
			else if (gui == EnumGuiType.MainMenuStats)
				return new GuiNpcStats(npc);

			else if (gui == EnumGuiType.MainMenuAdvanced)
				return new GuiNpcAdvanced(npc);

			else if (gui == EnumGuiType.MovingPath)
				return new GuiNpcPather(npc);

			else if (gui == EnumGuiType.ManageFactions)
				return new GuiNPCManageFactions(npc);

			else if (gui == EnumGuiType.ManageLinked)
				return new GuiNPCManageLinkedNpc(npc);

			else if (gui == EnumGuiType.BuilderBlock)
				return new GuiBlockBuilder(buf.readBlockPos());

			else if (gui == EnumGuiType.ManageTransport)
				return new GuiNPCManageTransporters(npc);

			else if (gui == EnumGuiType.ManageDialogs)
				return new GuiNPCManageDialogs(npc);

			else if (gui == EnumGuiType.ManageQuests)
				return new GuiNPCManageQuest(npc);

			else if (gui == EnumGuiType.Companion)
				return new GuiNpcCompanionStats(npc);

			else if (gui == EnumGuiType.CompanionTalent)
				return new GuiNpcCompanionTalents(npc);

			else if (gui == EnumGuiType.MainMenuGlobal)
				return new GuiNPCGlobalMainMenu(npc);

			else if (gui == EnumGuiType.MainMenuAI)
				return new GuiNpcAI(npc);

			else if (gui == EnumGuiType.PlayerTransporter)
				return new GuiTransportSelection(npc);

			else if (gui == EnumGuiType.Script)
				return new GuiScript(npc);

			else if (gui == EnumGuiType.ScriptBlock)
				return new GuiScriptBlock(buf.readBlockPos());

			else if (gui == EnumGuiType.ScriptItem)
				return new GuiScriptItem(Minecraft.getInstance().player);

			else if (gui == EnumGuiType.ScriptDoor)
				return new GuiScriptDoor(buf.readBlockPos());

			else if (gui == EnumGuiType.ScriptPlayers)
				return new GuiScriptGlobal();

			else if (gui == EnumGuiType.SetupTransporter)
				return new GuiNpcTransporter(npc);

			else if (gui == EnumGuiType.SetupBank)
				return new GuiNpcBankSetup(npc);

			else if (gui == EnumGuiType.NpcRemote && Minecraft.getInstance().screen == null)
				return new GuiNpcRemoteEditor();

			else if (gui == EnumGuiType.PlayerMailbox)
				return new GuiMailbox();

			else if (gui == EnumGuiType.NpcDimensions)
				return new GuiNpcDimension();

			else if (gui == EnumGuiType.Border)
				return new GuiBorderBlock(buf.readBlockPos());

			else if (gui == EnumGuiType.RedstoneBlock)
				return new GuiNpcRedstoneBlock(buf.readBlockPos());

			else if (gui == EnumGuiType.MobSpawner)
				return new GuiNpcMobSpawner(buf.readBlockPos());

			else if (gui == EnumGuiType.CopyBlock)
				return new GuiBlockCopy(buf.readBlockPos());

			else if (gui == EnumGuiType.MobSpawnerMounter)
				return new GuiNpcMobSpawnerMounter();

			else if (gui == EnumGuiType.Waypoint)
				return new GuiNpcWaypoint(buf.readBlockPos());

			else if (gui == EnumGuiType.NbtBook)
				return new GuiNbtBook(buf.readBlockPos());
			return null;
		}
		finally{
			if(buf != null)
				buf.release();
		}
	}

	public void openGui(Player player, EnumGuiType gui) {
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.player != player){
			return;
		}
		Screen screen = getGui(gui, null, null);
		if(screen != null){
			minecraft.setScreen(screen);
		}
	}

	@Override
	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setScreen(getGui(gui, npc, null));
	}


	@Override
	public void openGui(Player player, Object guiscreen) {
		Minecraft minecraft = Minecraft.getInstance();
		if(!player.level().isClientSide || !(guiscreen instanceof Screen))
			return;

		if (guiscreen != null) {
			minecraft.setScreen((Screen)guiscreen);
		}
	}

	@Override
	public void spawnParticle(LivingEntity player, String string, Object... ob) {
		if(string.equals("Block")){
			BlockPos pos = (BlockPos) ob[0];
			BlockState state = (BlockState) ob[1];
            Minecraft.getInstance().particleEngine.destroy(pos, state);
		}
		else if(string.equals("ModelData")){
			ModelData data = (ModelData) ob[0];
			ModelPartData particles = (ModelPartData) ob[1];
			EntityCustomNpc npc = (EntityCustomNpc) player;
			Minecraft minecraft =  Minecraft.getInstance();
			double height = npc.getMyRidingOffset() + data.getBodyY();
			RandomSource rand = npc.getRandom();
			//if(particles.type == 0){
//				for(int i = 0; i< 2; i++){
//					EntityEnderFX fx = new EntityEnderFX(npc, (rand.nextDouble() - 0.5D) * (double)player.getBbWidth(), (rand.nextDouble() * (double)player.getBbHeight()) - height - 0.25D, (rand.nextDouble() - 0.5D) * (double)player.getBbWidth(), (rand.nextDouble() - 0.5D) * 2D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2D, particles);
//					minecraft.particleEngine.add(fx);
//				}
	    		
			//}
//			else if(particles.type == 1){
//	        	for(int i = 0; i < 2; i++){
//		            double x = player.posX + (rand.nextDouble() - 0.5D) * 0.9;
//		            double y = (player.posY + rand.nextDouble() * 1.9) - 0.25D - height;
//		            double z = player.posZ + (rand.nextDouble() - 0.5D) * 0.9;
//		
//		            
//		            double f = (rand.nextDouble() - 0.5D) * 2D;
//		            double f1 =  -rand.nextDouble();
//		            double f2 = (rand.nextDouble() - 0.5D) * 2D;
//		            
//		            minecraft.particles.addEffect(new EntityRainbowFX(player.level, x, y, z, f, f1, f2));
//	        	}
//			}
		}
	}

	public boolean hasClient() {
		return true;
	}
	
	public Player getPlayer() {
		return Minecraft.getInstance().player;
	}

	public static void bind(ResourceLocation location) {
		try{
			if(location == null)
				return;
	        TextureManager manager = Minecraft.getInstance().getTextureManager();
			AbstractTexture ob = manager.getTexture(location);
	    	if(ob == null){
	    		ob = new SimpleTexture(location);
	    		manager.register(location, ob);
	    	}
        	RenderSystem.bindTexture(ob.getId());
		}
		catch(NullPointerException ex){
			
		}
	}

	@Override
	public void spawnParticle(ParticleOptions particle, double x, double y, double z,
							  double motionX, double motionY, double motionZ, float scale) {
		Minecraft mc = Minecraft.getInstance();
        double xx = mc.getCameraEntity().getX() - x;
        double yy = mc.getCameraEntity().getY() - y;
        double zz = mc.getCameraEntity().getZ() - z;
		if(xx * xx + yy * yy + zz * zz > 256)
			return;
		
		Particle fx = mc.particleEngine.createParticle(particle, x, y, z, motionX, motionY, motionZ);
		if(fx == null)
			return;
        if (particle == ParticleTypes.FLAME){
			fx.scale(0.00001f);
        }
        else if (particle == ParticleTypes.SMOKE){
			fx.scale(0.00001f);
        }
	}

	public static class FontContainer {
		private TrueTypeFont textFont = null;
		public boolean useCustomFont = true;
		
		private FontContainer(){
			
		}
		
		public FontContainer(String fontType, int fontSize) {
	    	try {
				textFont = new TrueTypeFont(new Font(fontType, java.awt.Font.PLAIN, fontSize), 1f);
				useCustomFont = !fontType.equalsIgnoreCase("minecraft");
	    		if(!useCustomFont || fontType.isEmpty() || fontType.equalsIgnoreCase("default"))
	    			textFont = new TrueTypeFont(new ResourceLocation("customnpcs","opensans.ttf"), fontSize, 1f);
			} catch (Throwable e) {
				LogWriter.except(e);
				useCustomFont = false;
			}
		}

		public int height(String text){
			if(useCustomFont)
				return textFont.height(text);
			return Minecraft.getInstance().font.lineHeight;
		}
		
		public int width(String text){
			if(useCustomFont)
				return textFont.width(text);
			return Minecraft.getInstance().font.width(text);
		}

		public FontContainer copy() {
			FontContainer font = new FontContainer();
			font.textFont = textFont;
			font.useCustomFont = useCustomFont;
			return font;
		}

		public void draw(GuiGraphics graphics, String text, int x, int y, int color) {
			if(useCustomFont){
				textFont.draw(graphics.pose(), text, x, y, color);
			}
			else{
				graphics.drawString(Minecraft.getInstance().font, text, x, y, color);
			}
		}

		public String getName() {
			if(!useCustomFont)
				return "Minecraft";
			return textFont.getFontName();
		}

		public void clear() {
			if(textFont != null)
				textFont.dispose();
		}
	}
}
