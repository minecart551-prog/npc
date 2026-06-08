package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;



public class GuiNPCTrader extends GuiContainerNPCInterface<ContainerNPCTrader>{
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/trader.png");
	private final ResourceLocation slot = new ResourceLocation("customnpcs","textures/gui/slot.png");
	private RoleTrader role;
	private ContainerNPCTrader container;

	public GuiNPCTrader(ContainerNPCTrader container, Inventory inv, Component titleIn) {
        super(NoppesUtil.getLastNpc(), container, inv, titleIn);
        this.container = container;
        role = (RoleTrader) npc.role;
        imageHeight = 224;
        imageWidth = 223;
        this.title = "role.trader";
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBackground(graphics);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, resource);
        graphics.blit(resource, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, slot);
		for(int slot = 0; slot < 18; slot++){
			int i = guiLeft + slot%3 * 72 + 10;
			int j = guiTop + slot/3 * 21 + 6;
			
			ItemStack item = role.inventoryCurrency.items.get(slot);
			ItemStack item2 = role.inventoryCurrency.items.get(slot + 18);
			if(NoppesUtilServer.IsItemStackNull(item)){
				item = item2;
				item2 = ItemStack.EMPTY;
			}
			if(NoppesUtilPlayer.compareItems(item, item2, false, false)){
				item = item.copy();
				item.setCount(item.getCount() + item2.getCount());
				item2 = ItemStack.EMPTY;
			}

			ItemStack sold = role.inventorySold.items.get(slot);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, this.slot);
			graphics.blit(this.slot, i + 42, j, 0, 0, 18, 18);
			if(!NoppesUtilServer.IsItemStackNull(item) && !NoppesUtilServer.IsItemStackNull(sold)){
	            //RenderHelper.enableGUIStandardItemLighting();
	            if(!NoppesUtilServer.IsItemStackNull(item2)){
	            	graphics.renderItem(item2, i, j + 1);
		        	graphics.renderItemDecorations(font, item2, i, j + 1);
	            }
		        graphics.renderItem(item, i + 18, j + 1);
		        graphics.renderItemDecorations(font, item, i + 18, j + 1);
	            //RenderHelper.disableStandardItemLighting();
	
	            graphics.drawString(font, "=", i + 36, j + 5, CustomNpcResourceListener.DefaultTextColor);

			}
    	}

    }
    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
		for(int slot = 0; slot < 18; slot++){
			int i = slot%3 * 72 + 10;
			int j = slot/3 * 21 + 6;
			
			ItemStack item = role.inventoryCurrency.items.get(slot);
			ItemStack item2 = role.inventoryCurrency.items.get(slot + 18);
			if(NoppesUtilServer.IsItemStackNull(item)){
				item = item2;
				item2 = ItemStack.EMPTY;
			}
			if(NoppesUtilPlayer.compareItems(item, item2, role.ignoreDamage, role.ignoreNBT)){
				item = item.copy();
				item.setCount(item.getCount() + item2.getCount());
				item2 = ItemStack.EMPTY;
			}
			ItemStack sold = role.inventorySold.items.get(slot);
			if(NoppesUtilServer.IsItemStackNull(sold))
				continue;
			
			if(this.isHovering(i + 43, j + 1, 16, 16, x, y)){
				if(!container.canBuy(item, item2, player)){
					graphics.pose().translate(0, 0, 300);
					if(!item.isEmpty() && !NoppesUtilPlayer.compareItems(player, item, role.ignoreDamage, role.ignoreNBT))
						graphics.fillGradient(i + 17, j, i + 35, j + 18, 0x70771010, 0x70771010);
					if(!item2.isEmpty() && !NoppesUtilPlayer.compareItems(player, item2, role.ignoreDamage, role.ignoreNBT))
						graphics.fillGradient(i - 1, j, i + 17, j + 18, 0x70771010, 0x70771010);
					
		        	String title = I18n.get("trader.insufficient");
					graphics.drawString(font, title, (imageWidth - font.width(title))/2, 131, 0xDD0000);
					graphics.pose().translate(0, 0, -300);
				}
				else{
		        	String title = I18n.get("trader.sufficient");
					graphics.drawString(font, title, (imageWidth - font.width(title))/2, 131, 0x00DD00);
				}
			}

            if (this.isHovering(i, j, 16, 16, x, y) && !NoppesUtilServer.IsItemStackNull(item2)){
                graphics.renderTooltip(font, item2, x - guiLeft, y - guiTop);
            }
            if (this.isHovering(i + 18, j, 16, 16, x, y)){
                graphics.renderTooltip(font, item, x - guiLeft, y - guiTop);
            }
    	}
    }

	@Override
	public void buttonEvent(GuiButtonNop button) {

	}

	@Override
	public void save() {
	}
}
