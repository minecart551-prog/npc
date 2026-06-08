package noppes.npcs.client.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.entity.EntityNPCInterface;



public abstract class GuiNPCInterface2 extends GuiNPCInterface
{
	private ResourceLocation background = new ResourceLocation("customnpcs:textures/gui/menubg.png");
	private GuiNpcMenu menu;
	
    public GuiNPCInterface2(EntityNPCInterface npc){
    	this(npc, -1);
    }
    public GuiNPCInterface2(EntityNPCInterface npc, int activeMenu){
    	super(npc);
    	imageWidth = 420;
    	imageHeight = 200;
    	menu = new GuiNpcMenu(this, activeMenu, npc);
    	
    }
    @Override
    public void init(){
    	super.init();
        menu.initGui(guiLeft, guiTop, imageWidth);
    }
    

    @Override
    public boolean mouseClicked(double i, double j, int k){
    	if(!hasSubGui() && menu.mouseClicked(i, j, k))
	    	return true;
    	return super.mouseClicked(i, j, k);
    }   
	     
    public abstract void save();
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    	if(drawDefaultBackground) {
            renderBackground(graphics);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, background);
        graphics.blit(background, guiLeft, guiTop, 0, 0, 200, 220);
        graphics.blit(background, guiLeft + imageWidth-230, guiTop, 26, 0, 230, 220);

        int x = mouseX;
        int y = mouseY;
        if(hasSubGui())
        	x = y = 0;
        
        menu.drawElements(graphics, getFontRenderer(), x, y, minecraft, partialTicks);
        
        boolean bo = drawDefaultBackground;
        drawDefaultBackground = false;
        super.render(graphics, mouseX, mouseY, partialTicks);
        drawDefaultBackground = bo;
    }
}
