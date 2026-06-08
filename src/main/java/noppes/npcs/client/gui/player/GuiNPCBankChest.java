package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketBankUnlock;
import noppes.npcs.packets.server.SPacketBankUpgrade;
import noppes.npcs.packets.server.SPacketBanksSlotOpen;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.listeners.IGuiData;



public class GuiNPCBankChest extends GuiContainerNPCInterface<ContainerNPCBankInterface> implements IGuiData
{
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/bankchest.png");
    private ContainerNPCBankInterface container;
    private int availableSlots = 0;
    private int maxSlots = 1;
    private int unlockedSlots = 1;
    private ItemStack currency;

	public GuiNPCBankChest(ContainerNPCBankInterface container, Inventory inv, Component titleIn) {
        super(NoppesUtil.getLastNpc(), container, inv, titleIn);
        this.container = container;
        this.title = "";
        imageHeight = 235;
        
    }
    
    @Override
    public void init(){
    	super.init();
    	availableSlots = 0;
    	if(maxSlots > 1){
	        for(int i = 0; i < maxSlots ; i++){
	        	GuiButtonNop button = new GuiButtonNop(this, i, guiLeft - 50, guiTop + 10 + i * 24, 50, 20, I18n.get("gui.tab") + " " + (i+1));
	        	if( i > unlockedSlots)
	        		button.setEnabled(false);
	        	addButton(button);
	        	availableSlots++;
	        }
	        if(availableSlots == 1) {
				renderables.clear();
			}
    	}
        if(!container.isAvailable()){
        	addButton(new GuiButtonNop(this, 8, guiLeft + 48, guiTop + 48,80,20, I18n.get("bank.unlock")));
        }
        else if(container.canBeUpgraded()){
        	addButton(new GuiButtonNop(this, 9, guiLeft + 48, guiTop + 48,80,20, I18n.get("bank.upgrade")));
        }
    	if(maxSlots > 1){
    		getButton(container.slot).visible = false;
    		getButton(container.slot).setEnabled(false);
    	}
    }
    
    @Override
    public void buttonEvent(GuiButtonNop guibutton){
    	int id = guibutton.id;
    	if(id < 6){
    		Packets.sendServer(new SPacketBanksSlotOpen(id, container.bankid));
    	}
    	if(id == 8){
    		Packets.sendServer(new SPacketBankUnlock());
    	}
    	if(id == 9){
    		Packets.sendServer(new SPacketBankUpgrade());
    	}
    	

    }
    
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		super.renderBg(graphics, partialTicks, x, y);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, resource);
        int l = (width - imageWidth) / 2;
        int i1 = (height - imageHeight) / 2;
		graphics.blit(resource, l, i1, 0, 0, imageWidth, 6);
        if(!container.isAvailable()){
			graphics.blit(resource,l, i1 + 6, 0, 6, imageWidth, 64);
			graphics.blit(resource,l, i1 + 70, 0, 124, imageWidth, 222-124);
	        int i = guiLeft + 30;
	        int j = guiTop + 8;
			graphics.drawString(font, I18n.get("bank.unlockCosts")+":", i , j + 4 , CustomNpcResourceListener.DefaultTextColor);
	        drawItem(graphics, i + 90,j,currency,x,y);
        }
        else if(container.isUpgraded()){
			graphics.blit(resource,l, i1 + 60, 0, 60, imageWidth, 162);
			graphics.blit(resource,l, i1 + 6, 0, 60, imageWidth, 64);
        }
        else if(container.canBeUpgraded()){
			graphics.blit(resource,l, i1 + 6, 0, 6, imageWidth, 216);
	        int i = guiLeft + 30;
	        int j = guiTop + 8;
			graphics.drawString(font, I18n.get("bank.upgradeCosts") + ":", i , j + 4 , CustomNpcResourceListener.DefaultTextColor);
	        drawItem(graphics, i + 90,j,currency,x,y);
        }
        else{
			graphics.blit(resource,l, i1 + 6, 0, 60, imageWidth, 162);
        }
        if(maxSlots > 1){
	        for(int ii = 0; ii < maxSlots ; ii++){
	        	if(availableSlots == ii)
	        		break;
				graphics.drawString(font, "Tab " + (ii+1), guiLeft - 40, guiTop + 16 + ii * 24 , 0xFFFFFF);
	        }
        }

    }
    private void drawItem(GuiGraphics graphics, int x, int y, ItemStack item, int mouseX, int mouseY){
		if(NoppesUtilServer.IsItemStackNull(item))
			return;
        //RenderHelper.enableGUIStandardItemLighting();
        graphics.renderItem(item, x,y);
        graphics.renderItemDecorations(font, item, x,y);
        //RenderHelper.disableStandardItemLighting();

        if (this.isHovering(x - guiLeft, y - guiTop, 16, 16, mouseX, mouseY)){
            graphics.renderTooltip(font, item, mouseX, mouseY);
        }
    }
	@Override
	public void save() {
	}
	@Override
	public void setGuiData(CompoundTag compound) {
		maxSlots = compound.getInt("MaxSlots");
		unlockedSlots = compound.getInt("UnlockedSlots");
		if(compound.contains("Currency"))
			currency = ItemStack.of(compound.getCompound("Currency"));
		else
			currency = ItemStack.EMPTY;
		if(container.currency != null)
			container.currency.item = currency;
		init();
	}
}
