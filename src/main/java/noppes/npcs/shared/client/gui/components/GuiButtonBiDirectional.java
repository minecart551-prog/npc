package noppes.npcs.shared.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.shared.client.gui.listeners.IGuiInterface;

public class GuiButtonBiDirectional extends GuiButtonNop {
	public static final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/arrowbuttons.png");
	
	private int color = 0xffffff;
    public static final int UNSET_FG_COLOR = -1;
    protected int packedFGColor = UNSET_FG_COLOR;
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;
        return this.active ? 16777215 : 10526880; // White : Light Grey
    }
    public void setFGColor(int color) {
        this.packedFGColor = color;
    }
    public void clearFGColor() {
        this.packedFGColor = UNSET_FG_COLOR;
    }

    public GuiButtonBiDirectional(IGuiInterface gui, int id, int x, int y, int width, int height, String[] arr, int current) {
        super(gui, id, x, y, width, height, arr, current);
    }

    public GuiButtonBiDirectional(IGuiInterface gui, int id, int x, int y, int width, int height, int current, String... arr) {
        super(gui, id, x, y, width, height, arr, current);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible)
        	return;

        boolean hover = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        
        boolean hoverL = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + 14 && mouseY < this.getY() + this.height;

        boolean hoverR = !hoverL && mouseX >= this.getX() + width - 14 && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);

        graphics.blit( resource,this.getX(), this.getY(), 0, hoverL?40:20, 11, 20);
        graphics.blit( resource,this.getX() + width - 11, this.getY(), 11, hover && !hoverL || hoverR?40:20, 11, 20);
        
        int l = color;
        if (packedFGColor != 0){
            l = packedFGColor;
        }
        else if (!this.active){
            l = 10526880;
        }
        else if (hover){
            l = 16777120;
        }
        String text = "";
        float maxWidth = this.width - 36;
        String displayString = getMessage().getString();
        if(mc.font.width(displayString) > maxWidth){
        	for(int h = 0; h < displayString.length(); h++){
        		char c = displayString.charAt(h);
        		text += c;
        		if(mc.font.width(text) > maxWidth)
        			break;
        	}
        	text += "...";
        }
        else
        	text = displayString;
        if(hover)
        	text = (char)167 + "n" + text;

        graphics.drawString(mc.font, text, this.getX() + this.width / 2 - mc.font.width(text)/2, this.getY() + (this.height - 8) / 2, l);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
    	int value = getValue();
    	if(this.isMouseOver(mouseX, mouseY) && display != null && display.length != 0){
            boolean hoverL = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + 14 && mouseY < this.getY() + this.height;

            boolean hoverR = !hoverL && mouseX >= this.getX() + 14 && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

            if(hoverR)
            	value = (value+1) % display.length;
            if(hoverL){
            	if(value <= 0)
            		value = display.length;
            	value--;
            }
    		this.setDisplay(value);
    	}
    	return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public void onClick(double x, double y){
        if(gui.hasSubGui())
            return;
        gui.buttonEvent(this);
    }
}
