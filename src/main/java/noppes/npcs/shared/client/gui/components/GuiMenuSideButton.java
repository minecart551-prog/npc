package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;

public class GuiMenuSideButton extends GuiButtonNop {
	public static final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/menusidebutton.png");

    public boolean active;

    public GuiMenuSideButton(IGuiInterface gui, int i, int j, int k, String s){
        this(gui, i, j, k, 200, 20, s);
    }

    public GuiMenuSideButton(IGuiInterface gui, int i, int j, int k, int l, int i1, String s){
    	super(gui, i, j, k, l, i1, s);
    }

    @Override
    public void render(GuiGraphics graphics, int i, int j, float partialTicks) {
        if (!visible){
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        int width = this.width + (active?2:0);
        isHovered = i >= getX() && j >= getY() && i < getX() + width && j < getY() + height;
        int k = this.active ? 0 : (isHovered ? 2 : 1);
        graphics.blit(resource, getX(), getY(), 0,  k * 22, width, height);
        //mouseDragged(i, j);
        
        String text = "";
        float maxWidth = width * 0.75f;
        String displayString = getMessage().getString();
        if(fontrenderer.width(displayString) > maxWidth){
        	for(int h = 0; h < displayString.length(); h++){
        		char c = displayString.charAt(h);
        		if(fontrenderer.width(text + c) > maxWidth)
        			break;
        		text += c;
        	}
        	text += "...";
        }
        else
        	text = displayString;
        if (active){
            graphics.drawCenteredString(fontrenderer, text, getX() + width / 2, getY() + (height - 8) / 2, 0xffffa0);
        }
        else if (isHovered){
            graphics.drawCenteredString(fontrenderer, text, getX() + width / 2, getY() + (height - 8) / 2, 0xffffa0);
        }
        else{
            graphics.drawCenteredString(fontrenderer, text, getX() + width / 2, getY() + (height - 8) / 2, 0xe0e0e0);
        }
    }

    @Override
    public boolean mouseClicked(double i, double j, int button){
        if(!active){
            return super.mouseClicked(i, j, button);
        }
        return false;
    }
}
