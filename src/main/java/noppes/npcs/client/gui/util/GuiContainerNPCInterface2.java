package noppes.npcs.client.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import noppes.npcs.entity.EntityNPCInterface;




public abstract class GuiContainerNPCInterface2<T extends AbstractContainerMenu> extends GuiContainerNPCInterface<T>
{
	private ResourceLocation background = new ResourceLocation("customnpcs","textures/gui/menubg.png");
	private final ResourceLocation defaultBackground = new ResourceLocation("customnpcs","textures/gui/menubg.png");
	private GuiNpcMenu menu;
	public int menuYOffset = 0;

    public GuiContainerNPCInterface2(EntityNPCInterface npc, T cont, Inventory inv, Component titleIn){
    	this(npc, cont, inv, titleIn, -1);
    }
    public GuiContainerNPCInterface2(EntityNPCInterface npc, T cont, Inventory inv, Component titleIn, int activeMenu){
    	super(npc, cont, inv, titleIn);
    	this.imageWidth = 420;
    	this.menu = new GuiNpcMenu(this,activeMenu,npc);
    	title = "";
    }
    
    public void setBackground(String texture){
    	background = new ResourceLocation("customnpcs","textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture){
    	return new ResourceLocation("customnpcs","textures/gui/" + texture);
    }
    
	@Override
    public void init(){
    	super.init();
        menu.initGui(guiLeft, guiTop + menuYOffset, imageWidth);
    }   

    @Override
    public boolean mouseClicked(double i, double j, int k){
    	if(!hasSubGui())
	    	menu.mouseClicked(i, j, k);
    	return super.mouseClicked(i, j, k);
    }
    
    public void delete(){
    	npc.delete();
        setScreen(null);
        minecraft.mouseHandler.grabMouse();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y){
        PoseStack matrixStack = graphics.pose();
    	renderBackground(graphics);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, background);
        graphics.blit(background, guiLeft, guiTop, 0, 0, 256, 256);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, defaultBackground);
        graphics.blit(defaultBackground,  guiLeft + imageWidth-200, guiTop, 26, 0, 200, 220);
        
        menu.drawElements(graphics, font, x, y, minecraft, partialTicks);
        super.renderBg(graphics, partialTicks, x, y);
    }

//    @Override
//    protected void drawSlotInventory(Slot par1Slot)
//    {this.dra
//        if(subgui == null)
//        	super.drawSlotInventory(par1Slot);
//    } //TODO fix
}
