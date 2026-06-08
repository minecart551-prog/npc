package noppes.npcs.client.gui.questtypes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiButtonYesNo;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.gui.components.GuiTextFieldNop;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;



public class GuiNpcQuestTypeItem extends GuiContainerNPCInterface<ContainerNpcQuestTypeItem> implements ITextfieldListener
{
	private Quest quest;
	private static final ResourceLocation field_110422_t = new ResourceLocation("customnpcs","textures/gui/followersetup.png");

    public GuiNpcQuestTypeItem(ContainerNpcQuestTypeItem container, Inventory inv, Component titleIn) {
        super(NoppesUtil.getLastNpc(), container, inv, titleIn);
        this.quest = NoppesUtilServer.getEditingQuest(player);
        title = "";
        imageHeight = 202;
        closeOnEsc = false;
    }
    
    @Override
    public void init(){
        super.init();
        addLabel(new GuiLabel(0, "quest.takeitems", guiLeft + 4, guiTop + 8));
        addButton(new GuiButtonNop(this, 0, guiLeft + 90, guiTop + 3, 60, 20, new String[]{ "gui.yes","gui.no"}, ((QuestItem)quest.questInterface).leaveItems?1:0));

        addLabel(new GuiLabel(1, "gui.ignoreDamage", guiLeft + 4, guiTop + 29));
        addButton(new GuiButtonYesNo(this, 1, guiLeft + 90, guiTop + 24, 50, 20, ((QuestItem)quest.questInterface).ignoreDamage));

        addLabel(new GuiLabel(2, "gui.ignoreNBT", guiLeft + 62, guiTop + 51));
        addButton(new GuiButtonYesNo(this, 2, guiLeft + 120, guiTop + 46, 50, 20, ((QuestItem)quest.questInterface).ignoreNBT));
        
        addButton(new GuiButtonNop(this, 5, guiLeft, guiTop + imageHeight, 98, 20, "gui.back"));
    }

    @Override
    public void buttonEvent(GuiButtonNop guibutton){
        if(guibutton.id == 0){
        	((QuestItem)quest.questInterface).leaveItems = ((GuiButtonNop)guibutton).getValue() == 1;
        }
        if(guibutton.id == 1){
        	((QuestItem)quest.questInterface).ignoreDamage = ((GuiButtonYesNo)guibutton).getBoolean();
        }
        if(guibutton.id == 2){
        	((QuestItem)quest.questInterface).ignoreNBT = ((GuiButtonYesNo)guibutton).getBoolean();
        }
        if(guibutton.id == 5){
        	NoppesUtil.openGUI(player,GuiNPCManageQuest.Instance);
        }
    }


    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y)
    {
        super.renderBackground(graphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, field_110422_t);
        int l = (width - imageWidth) / 2;
        int i1 = (height - imageHeight) / 2;
        graphics.blit(field_110422_t, l, i1, 0, 0, imageWidth, imageHeight);

    }
    
	@Override
	public void save() {
//    	HashMap<Integer,ItemStack> map = new HashMap<Integer,ItemStack>();
//    	for(int i= 0;i < container.invMatrix.getContainerSize();i++){
//    		ItemStack item = container.invMatrix.getItem(i);
//    		if(item != null)
//    			map.put(i, item.copy());
//        }
//    	((QuestItem)quest.questInterface).items = map;
//    	QuestController.saveQuest(quest);
	}
	@Override
	public void unFocused(GuiTextFieldNop textfield) {
		quest.rewardExp = textfield.getInteger();
	}
}
