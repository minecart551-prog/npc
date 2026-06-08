package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import org.joml.Matrix4f;

import java.util.List;

public class CustomGuiTexturedRect extends AbstractWidget implements IGuiComponent {

    private CustomGuiTexturedRectWrapper component = null;
    GuiCustom parent;
    ResourceLocation texture;

    public int id,x,y,width,height,textureX,textureY;
    float scale = 1.0f;
    List<Component> hoverText;
    public boolean hasRepeatingTexture = false;
    public int texRepWidth, texRepHeight, texRepBorderSize = 0;

    public CustomGuiTexturedRect(GuiCustom parent, CustomGuiTexturedRectWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.empty());
        this.component = component;
        this.parent = parent;
        init();
    }

    @Override
    public void init() {
        this.id = component.getID();
        this.texture = new ResourceLocation(component.getTexture());
        this.x = component.getPosX();
        this.y = component.getPosY();
        this.width = component.getWidth();
        this.height = component.getHeight();
        this.textureX = component.getTextureX();
        this.textureY = component.getTextureY();

        scale = component.getScale();
        hasRepeatingTexture = component.hasRepeatingTexture;
        texRepWidth = component.texRepWidth;
        texRepHeight = component.texRepHeight;
        texRepBorderSize = component.texRepBorderSize;

        if(component.hasHoverText()) {
            hoverText = component.getHoverTextList();
        }
    }

    public CustomGuiTexturedRect setRep(int texRepWidth, int texRepHeight, int texRepBorderSize ){
        this.texRepWidth = texRepWidth;
        this.texRepHeight = texRepHeight;
        this.texRepBorderSize = texRepBorderSize;
        this.hasRepeatingTexture = true;
        return this;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(component.getTexture().isEmpty() || !component.getVisible()){
            return;
        }
        boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        graphics.pose().pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        //blit(matrixStack, x, y, textureX, textureY, width, height);

        Matrix4f m = graphics.pose().last().pose();
        if(!hasRepeatingTexture){
            draw(m, x, y, textureX, textureY, width, height);
        }
        else{
            if(texRepBorderSize > 0){ //Draw Corners
                draw(m, x, y, textureX, textureY, texRepBorderSize, texRepBorderSize); //NW
                draw(m, x + width - texRepBorderSize, y, textureX + texRepWidth - texRepBorderSize, textureY, texRepBorderSize, texRepBorderSize); //NE
                draw(m, x, y + height - texRepBorderSize, textureX, textureY + texRepHeight - texRepBorderSize, texRepBorderSize, texRepBorderSize); //SW
                draw(m, x + width - texRepBorderSize, y + height - texRepBorderSize, textureX + texRepWidth - texRepBorderSize, textureY + texRepHeight - texRepBorderSize, texRepBorderSize, texRepBorderSize); //SW
            }
            float w = width - texRepBorderSize * 2f;
            float h = height - texRepBorderSize * 2f;
            float tw = texRepWidth - texRepBorderSize * 2f;
            float th = texRepHeight - texRepBorderSize * 2f;
            float mx = w / tw;
            float my = h / th;
            for(int i = 0; i < my; i++){
                float dh = th * Math.min(1f, my - i);
                draw(m, x, y + texRepBorderSize + th * i, textureX, textureY + texRepBorderSize, texRepBorderSize, dh);
                draw(m, x + width - texRepBorderSize, y + texRepBorderSize + th * i, textureX + texRepWidth - texRepBorderSize, textureY + texRepBorderSize, texRepBorderSize, dh);
                for(int j = 0; j < mx; j++){
                    float dw = tw * Math.min(1f, mx - j);
                    draw(m, x + texRepBorderSize + tw * j, y, textureX + texRepBorderSize, textureY, dw, texRepBorderSize);
                    draw(m, x + texRepBorderSize + tw * j, y + height - texRepBorderSize, textureX + texRepBorderSize, textureY + texRepHeight - texRepBorderSize, dw, texRepBorderSize);

                    draw(m, x + texRepBorderSize + tw * j, y + texRepBorderSize + th * i, textureX + texRepBorderSize, textureY + texRepBorderSize, dw, dh); //NW
                }
            }
        }
        if(hovered && this.hoverText!=null && this.hoverText.size() > 0) {
            this.parent.hoverText = hoverText;
        }
        graphics.pose().popPose();
    }

    private void draw(Matrix4f m, float x, float y, float texX, float texY, float width, float height){
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        int blitLevel = Math.max(0, this.id);
        bufferbuilder.vertex(m, x, y + height*scale, blitLevel).uv(texX * 0.00390625F, (texY + height) * 0.00390625F).endVertex();
        bufferbuilder.vertex(m, x + width*scale, (y + height*scale), blitLevel).uv((texX + width) * 0.00390625F, (texY + height) * 0.00390625F).endVertex();
        bufferbuilder.vertex(m, x + width*scale, y, blitLevel).uv((texX + width) * 0.00390625F, texY * 0.00390625F).endVertex();
        bufferbuilder.vertex(m, x, y, blitLevel).uv(texX * 0.00390625F, texY * 0.00390625F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public void setTexture(ResourceLocation texture){
        this.texture = texture;
    }

    @Override
    public ICustomGuiComponent component() {
        return component;
    }

    @Override
    public void playDownSound(SoundManager p_93665_) { }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}
