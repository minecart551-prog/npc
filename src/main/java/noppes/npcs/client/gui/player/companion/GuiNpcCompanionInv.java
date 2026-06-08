package noppes.npcs.client.gui.player.companion;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;



public class GuiNpcCompanionInv extends GuiContainerNPCInterface<ContainerNPCCompanion>{
	private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/companioninv.png");
	private final ResourceLocation slot = new ResourceLocation("customnpcs", "textures/gui/slot.png");
	private RoleCompanion role;

	public GuiNpcCompanionInv(ContainerNPCCompanion container, Inventory inv, Component titleIn) {
		super(NoppesUtil.getLastNpc(), container, inv, titleIn);
		role = (RoleCompanion) npc.role;
		imageWidth = 171;
		imageHeight = 166;
	}

	@Override
	public void init() {
		super.init();
		GuiNpcCompanionStats.addTopMenu(role, this, 3);
	
	}

	@Override
	public void buttonEvent(GuiButtonNop guibutton) {

		int id = guibutton.id;
		if(id == 1){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.Companion);
		}
		if(id == 2){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.CompanionTalent);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int par1, int limbSwingAmount) {
		//super.renderLabels(par1, limbSwingAmount);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float f, int xMouse, int yMouse) {
        super.renderBackground(graphics);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, resource);
		graphics.blit(resource, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, slot);
		if(role.getTalentLevel(EnumCompanionTalent.ARMOR) > 0){
			for(int i = 0; i < 4; i++){
				graphics.blit(resource, guiLeft + 5, guiTop + 7 + i * 18 , 0, 0, 18, 18);
			}
		}
		if(role.getTalentLevel(EnumCompanionTalent.SWORD) > 0){
			graphics.blit(resource, guiLeft + 78, guiTop + 16, 0, npc.inventory.weapons.get(0) == null?18:0, 18, 18);
		}
		if(role.getTalentLevel(EnumCompanionTalent.RANGED) > 0){
			
		}
		if(role.talents.containsKey(EnumCompanionTalent.INVENTORY)){
			int size = (role.getTalentLevel(EnumCompanionTalent.INVENTORY) + 1) * 2;
			for(int i = 0; i < size; i++){
				graphics.blit(resource, guiLeft + 113 + i % 3 * 18, guiTop + 7 + i / 3 * 18 , 0, 0, 18, 18);
			}
		}

	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		super.drawNpc(graphics, 52, 70);
	}

	@Override
	public void save() {
		
	}
}
