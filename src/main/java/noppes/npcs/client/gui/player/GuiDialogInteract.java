package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.MouseHelperMixin;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketDialogSelected;
import noppes.npcs.packets.server.SPacketQuestCompletionCheckAll;
import noppes.npcs.shared.client.gui.listeners.IGuiClose;
import noppes.npcs.shared.client.util.NoppesStringUtils;

import java.util.ArrayList;
import java.util.List;

public class GuiDialogInteract extends GuiNPCInterface implements IGuiClose
{
	private Dialog dialog;
    private int selected = 0;
    private List<TextBlockClient> lines = new ArrayList<TextBlockClient>();
    private List<Integer> options = new ArrayList<Integer>();
    private int rowStart = 0;
    private int rowTotal = 0;
    private int dialogHeight = 180;

	private ResourceLocation wheel;
	private ResourceLocation[] wheelparts;
	private ResourceLocation indicator;
	
	private boolean isGrabbed = false;
	
    public GuiDialogInteract(EntityNPCInterface npc, Dialog dialog){
    	super(npc);
		this.dialog = dialog;
    	appendDialog(dialog);
    	imageHeight = 238;

    	wheel = this.getResource("wheel.png");
    	indicator = this.getResource("indicator.png");
    	wheelparts = new ResourceLocation[]{getResource("wheel1.png"),getResource("wheel2.png"),getResource("wheel3.png"),
    			getResource("wheel4.png"),getResource("wheel5.png"),getResource("wheel6.png")};
    }
    
    public void init(){
    	super.init();
    	isGrabbed = false;
    	grabMouse(dialog.showWheel);
    	guiTop = (height - imageHeight);
    	calculateRowHeight();
    }
    
    public void grabMouse(boolean grab){
		 if(grab && !isGrabbed){
			 MouseHelperMixin mouse = (MouseHelperMixin) Minecraft.getInstance().mouseHandler;
			 mouse.setGrabbed(false);
			 double xpos = 0;
			 double ypos = 0;
			 mouse.setX(xpos);
			 mouse.setY(ypos);
			 InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, xpos, ypos);
			 isGrabbed = true;
		 }
		 else if(!grab && isGrabbed){
			 Minecraft.getInstance().mouseHandler.releaseMouse();
			 isGrabbed = false;
		 }
    }
     
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    	//this.renderBackground(matrixStack);
        //graphics.fillGradient(0, 0, this.width, this.height, 0xDD000000, 0xDD000000);
    	
        if(!dialog.hideNPC){
	    	int l = (-70);
	    	int i1 =  (imageHeight);
	    	drawNpc(graphics, npc, l, i1, 1.4f, 0);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);

		graphics.pose().pushPose();
		graphics.pose().translate(0.0F, 0.5f, 100.065F);
        int count = 0;
        for(TextBlockClient block : new ArrayList<TextBlockClient>(lines)){
        	int size = ClientProxy.Font.width(block.getName() + ": ");
    		drawString(graphics, block.getName() + ": ",  -4 - size, block.color, count);
        	for(Component line : block.lines){
        		drawString(graphics, line.getString(), 0, block.color, count);
                count++;
        	}
        	count++;
        }

        if(!options.isEmpty()){
        	if(!dialog.showWheel)
        		drawLinedOptions(graphics, mouseY);
        	else
        		drawWheel(graphics);
        }
		graphics.pose().popPose();
    }

    private double selectedX = 0;
    private double selectedY = 0;
    private void drawWheel(GuiGraphics graphics){
    	int yoffset = guiTop + dialogHeight + 14;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, wheel);

		graphics.blit(wheel,(width/2) - 31, yoffset, 0, 0, 63, 40);
        selectedX = minecraft.mouseHandler.xpos() * 0.5D;
        selectedY = -minecraft.mouseHandler.ypos() * 0.5D;
        int limit = 80;
        if(selectedX > limit)
        	selectedX = limit;
        if(selectedX < -limit)
        	selectedX = -limit;
        
        if(selectedY > limit)
        	selectedY = limit;
        if(selectedY < -limit)
        	selectedY = -limit;

        selected = 1;
    	if(selectedY < -20)
    		selected++;
    	if(selectedY > 54)
    		selected--;
    	
    	if(selectedX < 0)
    		selected += 3;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, wheelparts[selected]);
		graphics.blit(wheelparts[selected],(width/2) - 31, yoffset, 0, 0, 85, 55);
        for(int slot:dialog.options.keySet()){
        	DialogOption option = dialog.options.get(slot);
        	if(option == null || option.optionType == OptionType.DISABLED || option.hasDialog() && !option.getDialog().availability.isAvailable(player))
        		continue;
            int color = option.optionColor;            	
        	if(slot == (selected))
        		color = 0x838FD8;
    		//drawString(font, option.title, width/2 -50 ,yoffset+ 162 + slot * 13 , color);
        	int height = ClientProxy.Font.height(option.title);
        	if(slot == 0)
        		graphics.drawString(font, option.title, width/2 + 13, yoffset - height, color);
        	if(slot == 1)
				graphics.drawString(font, option.title, width/2 + 33, yoffset - height / 2 + 14, color);
        	if(slot == 2)
				graphics.drawString(font, option.title, width/2 + 27, yoffset + 27, color);
        	if(slot == 3)
				graphics.drawString(font, option.title, width/2 - 13 - ClientProxy.Font.width(option.title), yoffset - height, color);
        	if(slot == 4)
				graphics.drawString(font, option.title, width/2 - 33 - ClientProxy.Font.width(option.title), yoffset - height / 2 + 14, color);
        	if(slot == 5)
				graphics.drawString(font, option.title, width/2 - 27 - ClientProxy.Font.width(option.title), yoffset + 27, color);
        	
        }
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, indicator);
		graphics.blit(indicator,width/2 + (int)selectedX/4  - 2,yoffset + 16 - (int)selectedY/6, 0, 0, 8, 8);
    }
    private void drawLinedOptions(GuiGraphics graphics, int j){
    	graphics.hLine( guiLeft - 60, guiLeft + imageWidth + 120, guiTop + dialogHeight - ClientProxy.Font.height(null) / 3, 0xFFFFFFFF);
        int offset = dialogHeight;
        if(j >= (int)((guiTop + offset))){
        	int selected = (int) ((j - (guiTop + offset)) / (ClientProxy.Font.height(null)));
	        if(selected < options.size())
		        this.selected = selected;
        }
        if(selected >= options.size())
        	selected = 0;
        if(selected < 0)
        	selected = 0;
        
        for(int k = 0; k < options.size(); k++){
        	int id = options.get(k);
        	DialogOption option = dialog.options.get(id);
        	int y = (int)((guiTop + offset  + (k * ClientProxy.Font.height(null))));
        	if(selected == k){
        		graphics.drawString(font, ">", guiLeft - 60, y, 0xe0e0e0);
        	}
			graphics.drawString(font, NoppesStringUtils.formatText(option.title, player, npc), guiLeft - 30, y, option.optionColor);
        }
    }
    
    private void drawString(GuiGraphics graphics, String text, int left, int color, int count){
    	int height = count - rowStart;
		ClientProxy.Font.draw(graphics, text, guiLeft + left, guiTop + (height * ClientProxy.Font.height(null)), color);
    }
    
    private int getSelected(){
    	if(selected <= 0)
    		return 0;
    	
    	if(selected < options.size())
    		return selected;
    	
    	return options.size() - 1;
    }

	@Override
	public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
    	if(key == minecraft.options.keyUp.key.getValue() || key == InputConstants.getKey("key.keyboard.up").getValue()){
			selected--;
    	}
    	if(key == minecraft.options.keyDown.key.getValue() || key == InputConstants.getKey("key.keyboard.down").getValue()){
    		selected++;
    	}
    	if(key == InputConstants.getKey("key.keyboard.enter").getValue() || key == InputConstants.getKey("key.keyboard.keypad.enter").getValue()){
        	handleDialogSelection();
    	}
        if (closeOnEsc && (key == InputConstants.getKey("key.keyboard.escape").getValue() || isInventoryKey(key))){
        	Packets.sendServer(new SPacketDialogSelected(dialog.id, -1));
        	closed();
            onClose();
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double i, double j, int  k){
    	if((selected == -1 && options.isEmpty() || selected >= 0) && k == 0) {
    		handleDialogSelection();
    	}
    	return true;
    }

    private void handleDialogSelection(){
    	int optionId = -1;
    	if(dialog.showWheel)
    		optionId = selected;
    	else if(!options.isEmpty())
    		optionId = options.get(selected);
		Packets.sendServer(new SPacketDialogSelected(dialog.id, optionId));
    	if(dialog == null || !dialog.hasOtherOptions() || options.isEmpty()){
    		if(closeOnEsc) {
        		closed();
        		onClose();
    		}
        	return;
    	}
    	DialogOption option = dialog.options.get(optionId);
    	if(option == null || option.optionType != OptionType.DIALOG_OPTION){
    		if(closeOnEsc) {
        		closed();
				onClose();
    		}
    		return;
    	}
    	
    	lines.add(new TextBlockClient(player.getDisplayName().getString(), option.title, 280, option.optionColor, player, npc));
		calculateRowHeight();
    	
    	NoppesUtil.clickSound();
    	
    }
    private void closed(){
    	grabMouse(false);
    	Packets.sendServer(new SPacketQuestCompletionCheckAll());
    }

	public void appendDialog(Dialog dialog) {
		closeOnEsc = !dialog.disableEsc;
		this.dialog = dialog;
		this.options = new ArrayList<Integer>();

    	if(dialog.sound != null && !dialog.sound.isEmpty()){
    		MusicController.Instance.stopMusic();
    		BlockPos pos = npc.blockPosition();
    		MusicController.Instance.playSound(SoundSource.VOICE, dialog.sound, pos, 1, 1);
    	}
    	
    	lines.add(new TextBlockClient(npc.createCommandSourceStack(), dialog.text, 280, 0xe0e0e0, player, npc));
    	
		 for(int slot:dialog.options.keySet()){
			DialogOption option = dialog.options.get(slot);
			if(option == null || !option.isAvailable(player))
				continue;
			options.add(slot);
		 }
		 calculateRowHeight();
		 
		 grabMouse(dialog.showWheel);
	}
	private void calculateRowHeight(){
		if(dialog.showWheel){
	    	dialogHeight = imageHeight - 58;
		}
		else{
	    	dialogHeight = imageHeight - 3 * ClientProxy.Font.height(null) - 4;
	    	if(dialog.options.size() > 3){
	    		dialogHeight -= (dialog.options.size() - 3) * ClientProxy.Font.height(null);
	    	}
		}
        rowTotal = 0;
        for(TextBlockClient block : lines){
        	rowTotal += block.lines.size() + 1;
        }
        int max = dialogHeight / ClientProxy.Font.height(null);
        
        rowStart = rowTotal - max;
        if(rowStart < 0)
        	rowStart = 0;
	}

	@Override
	public void setClose( CompoundTag data) {
    	grabMouse(false);
	}

	@Override
	public void save() {
		
	}
}

