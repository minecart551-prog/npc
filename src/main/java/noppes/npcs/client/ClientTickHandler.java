package noppes.npcs.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.*;
import org.lwjgl.glfw.GLFW;

public class ClientTickHandler implements ClientTickEvents.StartTick {

	private Level prevLevel;
	private boolean otherContainer = false;


	@Override
	public void onStartTick(Minecraft client) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && mc.player.containerMenu instanceof InventoryMenu){
			if(otherContainer){
		    	Packets.sendServer(new SPacketQuestCompletionCheckAll());
				otherContainer = false;
			}
		}
		else
			otherContainer = true;
		CustomNpcs.ticks++;
		RenderNPCInterface.LastTextureTick++;
		if(prevLevel != mc.level){
			prevLevel = mc.level;
			MusicController.Instance.stopMusic();
		}
	}

	public static void onKey(int key, int scancode, int modifiers, int action){
		Minecraft mc = Minecraft.getInstance();

		if(mc == null || mc.level == null || mc.getConnection() == null){
			return;
		}
		if(CustomNpcs.SceneButtonsEnabled){
			if(ClientProxy.Scene1.isDown()){
				Packets.sendServer(new SPacketSceneStart(1));
			}
			if(ClientProxy.Scene2.isDown()){
				Packets.sendServer(new SPacketSceneStart(2));
			}
			if(ClientProxy.Scene3.isDown()){
				Packets.sendServer(new SPacketSceneStart(3));
			}
			if(ClientProxy.SceneReset.isDown()){
				Packets.sendServer(new SPacketSceneReset());
			}
		}
		if(ClientProxy.QuestLog.isDown()){
			if(mc.screen == null)
				NoppesUtil.openGUI(mc.player, new GuiQuestLog(mc.player));
			else if(mc.screen instanceof GuiQuestLog)
				mc.mouseHandler.grabMouse();
		}

		if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE) {
			boolean isCtrlPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
			boolean isShiftPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
			boolean isAltPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
			boolean isMetaPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
			String openGui = mc.screen == null ? "" : mc.screen.getClass().getName();
			Packets.sendServer(new SPacketPlayerKeyPressed(key, isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed, action == GLFW.GLFW_RELEASE, openGui));
		}
	}

//	@SubscribeEvent TODO FABRIC
//	public void invoke(PlayerInteractEvent.LeftClickEmpty event) {
//		if(event.getHand() != InteractionHand.MAIN_HAND)
//			return;
//		Packets.sendServer(new SPacketPlayerLeftClicked());
//	}

	private final int[] ignoreKeys = new int[]{341, 340, 342, 343, 345, 344, 346, 347};
	private boolean isIgnoredKey(int key) {
		for(int i : ignoreKeys) {
			if(i == key)
				return true;
		}
		return false;
	}
}
