package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import noppes.npcs.client.gui.player.tabs.AbstractTab;
import noppes.npcs.client.gui.player.tabs.InventoryTabFactions;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.player.tabs.InventoryTabQuests;
import noppes.npcs.client.gui.player.tabs.InventoryTabVanilla;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.shared.client.gui.components.GuiButtonNextPage;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;

import java.util.ArrayList;



public class GuiFaction extends GuiNPCInterface {
	private ArrayList<Faction> playerFactions = new ArrayList<Faction>();
	private PlayerFactionData data;

	private int page = 0;
	private int pages = 1;

	private GuiButtonNextPage buttonNextPage;
	private GuiButtonNextPage buttonPreviousPage;
	private ResourceLocation indicator;

	public GuiFaction() {
		super();
		imageWidth = 200;
		imageHeight = 195;
		this.drawDefaultBackground = false;
		title = "";
		//Packets.sendServer(new SPacketPlayerFactionsGet());
		indicator = getResource("standardbg.png");
	}

	@Override
	public void init() {
		super.init();
		data = PlayerData.get(player).factionData;
		playerFactions = new ArrayList<Faction>();
		for (int id : data.factionData.keySet()) {
			Faction faction = FactionController.instance.getFaction(id);
			if (faction == null || faction.hideFaction)
				continue;
			playerFactions.add(faction);
		}

		pages = (playerFactions.size() - 1) / 5;
		pages++;

		page = 1;

		guiLeft = (width - imageWidth) / 2;
		guiTop += 12;

		addRenderableWidget(new InventoryTabVanilla().init(this));
		addRenderableWidget(new InventoryTabFactions().init(this));
		addRenderableWidget(new InventoryTabQuests().init(this));

		this.addButton(buttonNextPage = new GuiButtonNextPage(this, 1, guiLeft + imageWidth - 43, guiTop + 180, true, (button) -> {
			page++;
			updateButtons();
		}));
		this.addButton(buttonPreviousPage = new GuiButtonNextPage(this, 2, guiLeft + 20, guiTop + 180, false, (button) -> {
			page--;
			updateButtons();
		}));
		updateButtons();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, indicator);
		graphics.blit(indicator, guiLeft, guiTop + 8, 0, 0, imageWidth, imageHeight);
		graphics.blit(indicator,guiLeft + 4, guiTop + 8, 56, 0, 200, imageHeight);

		if (playerFactions.isEmpty()) {
			Component noFaction = Component.translatable("faction.nostanding");
			Font font = Minecraft.getInstance().font;
			graphics.drawString(font, noFaction, guiLeft + (this.imageWidth - font.width(noFaction)) / 2, guiTop + 80, CustomNpcResourceListener.DefaultTextColor);
		} else
			renderScreen(graphics);

		super.render(graphics, mouseX, mouseY, partialTicks);

	}

	private void renderScreen(GuiGraphics graphics) {
		int size = 5;
		if (playerFactions.size() % 5 != 0 && page == pages)
			size = playerFactions.size() % 5;

		for (int id = 0; id < size; id++) {
			graphics.hLine(guiLeft + 2, guiLeft + imageWidth, guiTop + 14 + id * 30, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);

			Faction faction = playerFactions.get((page - 1) * 5 + id);
			Component name = Component.translatable(faction.name);
			int current = data.factionData.get(faction.id);
			String points = " : " + current;

			Component standing = Component.translatable("faction.friendly");
			int color = 0x00FF00;
			if (current < faction.neutralPoints) {
				standing = Component.translatable("faction.unfriendly");
				color = 0xFF0000;
				points += "/" + faction.neutralPoints;
			} else if (current < faction.friendlyPoints) {
				standing = Component.translatable("faction.neutral");
				color = 0xF2FF00;
				points += "/" + faction.friendlyPoints;
			} else {
				points += "/-";
			}

			graphics.drawString(font, name, guiLeft + (this.imageWidth - font.width(name)) / 2, guiTop + 19 + id * 30, faction.color);

			graphics.drawString(font, standing, width / 2 - font.width(standing) - 1, guiTop + 33 + id * 30, color);
			graphics.drawString(font, points, width / 2, guiTop + 33 + id * 30, CustomNpcResourceListener.DefaultTextColor);
		}
		graphics.hLine( guiLeft + 2, guiLeft + imageWidth, guiTop + 14 + size * 30, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);

		if (pages > 1) {
			String s = page + "/" + pages;
			graphics.drawString(font, s, guiLeft + (this.imageWidth - font.width(s)) / 2, guiTop + 203, CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	public void buttonEvent(GuiButtonNop guibutton) {
		if (!(guibutton instanceof GuiButtonNextPage))
			return;
		int id = guibutton.id;
		if (id == 1) {
			page++;
		}
		if (id == 2) {
			page--;
		}
		updateButtons();
	}

	private void updateButtons() {
		buttonNextPage.visible = page < pages;
		buttonPreviousPage.visible = page > 1;
	}

	@Override
	public void save() {
	}
}