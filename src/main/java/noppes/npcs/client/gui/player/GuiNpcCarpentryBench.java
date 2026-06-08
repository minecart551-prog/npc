package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;

public class GuiNpcCarpentryBench extends GuiContainerNPCInterface<ContainerCarpentryBench> {
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/carpentry.png");
    private ContainerCarpentryBench container;
    private GuiButtonNop button;
    
    public GuiNpcCarpentryBench(ContainerCarpentryBench container, Inventory inv, Component titleIn) {
        super(null, container, inv, titleIn);
        this.container = container;
        this.title = "";
        imageHeight = 180;
    }

    @Override
    public void init(){
    	super.init();
    	addButton(button = new GuiButtonNop(this, 0, guiLeft + 158, guiTop + 4, 12, 20, "..."));
    }

    public void buttonEvent(GuiButtonNop guibutton){
    	setScreen(new GuiRecipes());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
    	button.active = RecipeController.instance != null && !RecipeController.instance.anvilRecipes.isEmpty();
    	RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        int l = (width - imageWidth) / 2;
        int i1 = (height - imageHeight) / 2;
        String title = I18n.get("block.customnpcs.npccarpentybench");
        graphics.blit(resource, l, i1, 0, 0, imageWidth, imageHeight);

        graphics.drawString(font, title, guiLeft + 4 , guiTop + 4 , CustomNpcResourceListener.DefaultTextColor, false);
        graphics.drawString(font, I18n.get("container.inventory"), guiLeft + 4, guiTop + 87, CustomNpcResourceListener.DefaultTextColor, false);
    }

	@Override
	public void save() {
		return;
	}
}
