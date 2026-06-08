package noppes.npcs.client.gui.script;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;



public class GuiScriptGlobal extends GuiNPCInterface {

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/smallbg.png");

	public GuiScriptGlobal() {
		super();
        imageWidth = 176;
        imageHeight = 222;
        this.drawDefaultBackground = false;
        title = "";
	}
	
    @Override
    public void init(){
        super.init();

        this.addButton(new GuiButtonNop(this, 0, guiLeft + 38, guiTop + 20, 100, 20, "Players"));
        this.addButton(new GuiButtonNop(this, 1, guiLeft + 38, guiTop + 50, 100, 20, "Forge (BROKEN)"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
    	renderBackground(graphics);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        graphics.blit(resource, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }
    
    @Override
	public void buttonEvent(GuiButtonNop guibutton){
		if(guibutton.id == 0){
			setScreen(new GuiScriptPlayers());
		}
		if(guibutton.id == 1){
			setScreen(new GuiScriptForge());
		}
    }

	@Override
	public void save() {
		
	}

}
