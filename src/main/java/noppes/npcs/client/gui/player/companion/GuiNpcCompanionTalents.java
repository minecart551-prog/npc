package noppes.npcs.client.gui.player.companion;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCompanionOpenInv;
import noppes.npcs.packets.server.SPacketCompanionTalentExp;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.util.NoppesStringUtils;

import java.util.HashMap;
import java.util.Map;

import static noppes.npcs.client.gui.player.companion.GuiNpcCompanionStats.GUI_ICONS_LOCATION;

public class GuiNpcCompanionTalents extends GuiNPCInterface{
	private RoleCompanion role;
	private Map<Integer,GuiTalent> talents = new HashMap<Integer,GuiTalent>();	
	private GuiButtonNop selected;

	public GuiNpcCompanionTalents(EntityNPCInterface npc) {
		super(npc);
		role = (RoleCompanion) npc.role;
		setBackground("companion_empty.png");
		imageWidth = 171;
		imageHeight = 166;
	}

	@Override
	public void init() {
		super.init();
		talents = new HashMap<Integer,GuiTalent>();	
		int y = guiTop + 12;
		
		addLabel(new GuiLabel(0, NoppesStringUtils.translate("quest.exp", ": "), guiLeft + 4, guiTop + 10));

		GuiNpcCompanionStats.addTopMenu(role, this, 2);
		int i = 0;
		for(EnumCompanionTalent e : role.talents.keySet()){
			addTalent(i++, e);
		}
	}
	
	private void addTalent(int i, EnumCompanionTalent talent){
		int y = guiTop + 28 + i/2 * 26;
		int x = guiLeft + 4 + i % 2 * 84;
		GuiTalent gui = new GuiTalent(role, talent, x, y);
		gui.init(minecraft, width, height);
		talents.put(i, gui);
		if(role.getTalentLevel(talent) < 5){
			addButton(new GuiButtonNop(this, i + 10, x + 26, y, 14, 14, "+"));
			y += 8;
		}
		addLabel(new GuiLabel(i, role.talents.get(talent) + "/" + role.getNextLevel(talent), x + 26, y + 8));
	}

	@Override
	public void buttonEvent(GuiButtonNop guibutton) {

		int id = guibutton.id;
		if(id == 1){
			CustomNpcs.proxy.openGui(npc, EnumGuiType.Companion);
		}
		if(id == 3){
			Packets.sendServer(new SPacketCompanionOpenInv());
		}
		if(id >= 10){
			selected = (GuiButtonNop) guibutton;
			lastPressedTime = startPressedTime = minecraft.level.getDayTime();
			addExperience(1);
		}
	}
	
	private void addExperience(int exp){
		EnumCompanionTalent talent = talents.get(selected.id - 10).talent;
		if(!role.canAddExp(-exp) && role.currentExp <= 0)
			return;
		if(exp > role.currentExp)
			exp = role.currentExp;
		Packets.sendServer(new SPacketCompanionTalentExp(talent, exp));
		role.talents.put(talent, role.talents.get(talent) + exp);
		role.addExp(-exp);
		getLabel(selected.id - 10).setMessage(Component.literal(role.talents.get(talent) + "/" + role.getNextLevel(talent)));
	}
	
	private long lastPressedTime = 0;
	private long startPressedTime = 0;
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		if(selected != null && minecraft.level.getDayTime() - startPressedTime > 4 && lastPressedTime < minecraft.level.getDayTime() && minecraft.level.getDayTime() % 4 == 0){
			if(selected.mouseClicked(mouseX, mouseY, 0)){
				lastPressedTime = minecraft.level.getDayTime();
				if(lastPressedTime - startPressedTime < 20)
					addExperience(1);
				else if(lastPressedTime - startPressedTime < 40)
					addExperience(2);
				else if(lastPressedTime - startPressedTime < 60)
					addExperience(4);
				else if(lastPressedTime - startPressedTime < 90)
					addExperience(8);
				else if(lastPressedTime - startPressedTime < 140)
					addExperience(14);
				else
					addExperience(28);
			}
			else{
				lastPressedTime = 0;
				selected = null;
			}
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
        graphics.blit(GUI_ICONS_LOCATION,guiLeft + 4, guiTop + 20, 10, 64, 162, 5);

        if (role.currentExp > 0){
        	float v = 1f * role.currentExp / role.getMaxExp();
        	if(v > 1)
        		v = 1;
            graphics.blit(GUI_ICONS_LOCATION,guiLeft + 4, guiTop + 20, 10, 69, (int)(v * 162), 5);
        }
        String s = role.currentExp + "\\" + role.getMaxExp();
		graphics.drawString(minecraft.font, s, guiLeft + imageWidth / 2 - minecraft.font.width(s) / 2, guiTop + 10, CustomNpcResourceListener.DefaultTextColor);

		for(GuiTalent talent : talents.values()){
			talent.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void save() {
		
	}
	
	public static class GuiTalent extends Screen {
		private EnumCompanionTalent talent;
		private int x, y;
		private RoleCompanion role;
		private static final ResourceLocation resource = new ResourceLocation("customnpcs:textures/gui/talent.png");
		public GuiTalent(RoleCompanion role, EnumCompanionTalent talent, int x, int y){
			super(Component.empty());
			this.talent = talent;
			this.x = x;
			this.y = y;
			this.role = role;
		}
		
		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			Minecraft mc = Minecraft.getInstance();
			PoseStack matrixStack = graphics.pose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, resource);
	        
			ItemStack item = talent.item;
			if(item.getItem() == null)
				item = new ItemStack(Blocks.DIRT);
			matrixStack.pushPose();
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.enableBlend();
	        boolean hover = x < mouseX && x + 24 > mouseX && y < mouseY && y + 24 > mouseY;
	        graphics.blit(resource, x, y, 0, hover?24:0, 24, 24);
			graphics.pose().pushPose();
			graphics.pose().translate(0,0,100);
	        graphics.renderItem(item, x + 4, y + 4);
	        graphics.renderItemDecorations(mc.font, item, x + 4, y + 4);
	        //RenderHelper.disableStandardItemLighting();
			matrixStack.translate(0, 0, 200);
            graphics.drawCenteredString(mc.font, role.getTalentLevel(talent) + "", x + 20, y + 16, 0xFFFFFF);
	        graphics.pose().popPose();
			matrixStack.popPose();
		}
	}
}
