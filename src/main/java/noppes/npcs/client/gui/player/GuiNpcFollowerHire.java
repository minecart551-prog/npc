package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketFollowerHire;
import noppes.npcs.roles.RoleFollower;



public class GuiNpcFollowerHire extends GuiContainerNPCInterface<ContainerNPCFollowerHire>
{
	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/followerhire.png");
	private ContainerNPCFollowerHire container;
	private RoleFollower role;

    public GuiNpcFollowerHire(ContainerNPCFollowerHire container, Inventory inv, Component titleIn) {
        super(NoppesUtil.getLastNpc(), container, inv, titleIn);
        this.container = container;
        role = (RoleFollower) npc.role;
    }

    @Override
    public void init() {
    	super.init();
        addButton(new GuiButtonNop(this, 5, guiLeft + 26, guiTop+ 60, 50, 20, I18n.get("follower.hire")));
    }

    @Override
    public void buttonEvent(GuiButtonNop guibutton) {
        if(guibutton.id == 5) {
        	Packets.sendServer(new SPacketFollowerHire());
        	close();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        super.renderLabels(p_281635_, p_282681_, p_283686_);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float f, int i, int j)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        int l = (width - imageWidth) / 2;
        int i1 = (height - imageHeight) / 2;
        graphics.blit(resource, l, i1, 0, 0, imageWidth, imageHeight);
		int index = 0;
		for(int slot = 0; slot < role.inventory.items.size(); slot++){
			ItemStack itemstack = role.inventory.items.get(slot);
			if(NoppesUtilServer.IsItemStackNull(itemstack))
				continue;
			int days = 1;
			if(role.rates.containsKey(slot))
	            days = role.rates.get(slot);
				
			
			int yOffset = index * 26;
			
			int x = guiLeft +  78;
			int y = guiTop + yOffset + 10;
            //RenderHelper.enableGUIStandardItemLighting();
            graphics.renderItem(itemstack, x + 11,y);
	        graphics.renderItemDecorations(font, itemstack, x+11,y);
            //RenderHelper.disableStandardItemLighting();

            String daysS = days + " " + ((days == 1)?I18n.get("follower.day"):I18n.get("follower.days"));
            graphics.drawString(font, " = "+daysS, x + 27, y + 4, CustomNpcResourceListener.DefaultTextColor);

	        if (this.isHovering(x - guiLeft  + 11, y - guiTop, 16, 16, mouseX, mouseY))
	        {
	            graphics.renderTooltip(font, itemstack, mouseX, mouseY);
	        }
	        //font.draw(quantity, x + 0 + (12-font.width(quantity))/2, y + 4, 0x404040);
	        
	        index++;
    	}
    	
    }
	@Override
	public void save() {
		return;
	}
}
