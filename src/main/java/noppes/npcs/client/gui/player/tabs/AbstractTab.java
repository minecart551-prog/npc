package noppes.npcs.client.gui.player.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.CustomNpcs;

public abstract class AbstractTab extends AbstractButton {
	public int id;
	ResourceLocation texture = new ResourceLocation("customnpcs", "textures/gui/tabs.png");
	ItemStack renderStack;

	public AbstractTab(int id, int posX, int posY, ItemStack renderStack) {
		super(posX, posY, 28, 32, Component.translatable(""));
		this.renderStack = renderStack;
		this.id = id;
	}

	public AbstractTab init(Screen s){
		int guiLeft = (s.width - 176) / 2;
		int guiTop = (s.height - 166) / 2;
		setX(guiLeft + id * 28);
		setY(guiTop - 28);
		return this;
	}


	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft mc = Minecraft.getInstance();
			int yTexPos = this.active ? 3 : 32;
			int ySize = this.active ? 25 : 32;
			int xOffset = this.id == 2 ? 0 : 1;
			int yPos = this.getY() + (this.active ? 3 : 0);
			ItemRenderer itemRender = mc.getItemRenderer();
			RenderSystem.setShaderTexture(0, this.texture);
			graphics.blit(this.texture, this.getX(), yPos, xOffset * 28, yTexPos, 28, ySize);

			graphics.pose().pushPose();
			graphics.pose().translate(0,0,30);
			graphics.renderItem(this.renderStack, this.getX() + 6, this.getY() + 8);
			graphics.renderItemDecorations(mc.font, this.renderStack, this.getX()+ 6, this.getY() + 8, null);
			graphics.pose().popPose();
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		this.onTabClicked();
	}

	@Override
	public void onPress() {

	}

	public abstract void onTabClicked();

	public abstract boolean shouldAddToList();
}
