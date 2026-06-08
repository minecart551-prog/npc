package noppes.npcs.client.gui.global;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketRecipeGet;
import noppes.npcs.packets.server.SPacketRecipeRemove;
import noppes.npcs.packets.server.SPacketRecipeSave;
import noppes.npcs.packets.server.SPacketRecipesGet;
import noppes.npcs.shared.client.gui.components.*;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;
import noppes.npcs.shared.client.gui.listeners.IGuiData;
import noppes.npcs.shared.client.gui.listeners.IScrollData;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;



public class GuiNpcManageRecipes extends GuiContainerNPCInterface2<ContainerManageRecipes> implements IScrollData, IGuiData, ICustomScrollListener,ITextfieldListener{
    private GuiCustomScrollNop scroll;
	private Map<String,Integer> data = new HashMap<String,Integer>();
	private ContainerManageRecipes container;
	private String selected = null;
	private ResourceLocation slot;

	public GuiNpcManageRecipes(ContainerManageRecipes container, Inventory inv, Component titleIn) {
    	super(NoppesUtil.getLastNpc(), container, inv, titleIn);
    	this.container = container;
    	drawDefaultBackground = false;
    	Packets.sendServer(new SPacketRecipesGet(container.width));
        setBackground("inventorymenu.png");
        slot = getResource("slot.png");
        imageHeight = 200;
    }

	@Override
    public void init(){
        super.init();
        
        if(scroll == null)
        	scroll = new GuiCustomScrollNop(this,0);
        scroll.setSize(130, 180);
        scroll.guiLeft = guiLeft + 172;
        scroll.guiTop = guiTop + 8;
        addScroll(scroll);
        
    	this.addButton(new GuiButtonNop(this, 0,guiLeft + 306, guiTop + 10, 84, 20, "menu.global"));
    	this.addButton(new GuiButtonNop(this, 1,guiLeft + 306, guiTop + 32, 84, 20, "block.customnpcs.npccarpentybench"));
    	this.getButton(0).setEnabled(container.width == 4);
    	this.getButton(1).setEnabled(container.width == 3);

    	this.addButton(new GuiButtonNop(this, 3,guiLeft + 306, guiTop + 60, 84, 20, "gui.add"));
    	this.addButton(new GuiButtonNop(this, 4,guiLeft + 306, guiTop + 82, 84, 20, "gui.remove"));
    	

    	this.addLabel(new GuiLabel(0, "gui.ignoreDamage", guiLeft + 86, guiTop + 32));
    	this.addButton(new GuiButtonYesNo(this, 5,guiLeft + 114, guiTop + 40, 50, 20, container.recipe.ignoreDamage));
    	
    	this.addLabel(new GuiLabel(1, "gui.ignoreNBT", guiLeft + 86, guiTop + 82));
    	this.addButton(new GuiButtonYesNo(this, 6,guiLeft + 114, guiTop + 90, 50, 20, container.recipe.ignoreNBT));
    	
    	this.addTextField(new GuiTextFieldNop(0, this,  guiLeft + 8, guiTop + 8, 160, 20, container.recipe.name));
    	this.getTextField(0).enabled = false;
    	this.getButton(5).setEnabled(false);
    	this.getButton(6).setEnabled(false);
    }

	@Override
	public void buttonEvent(GuiButtonNop guibutton){
		GuiButtonNop button = (GuiButtonNop) guibutton;
        if(button.id == 0){
        	save();
        	NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, new BlockPos(3,0,0));
        }
        if(button.id == 1){
        	save();
        	NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, new BlockPos(4,0,0));
        }
        if(button.id == 3){
        	save();
        	scroll.clear();
        	String name = I18n.get("gui.new");
        	while(data.containsKey(name))
        		name += "_";
        	RecipeCarpentry recipe = new RecipeCarpentry(new ResourceLocation(CustomNpcs.MODID, name), name);
        	recipe.isGlobal = container.width == 3;
        	Packets.sendServer(new SPacketRecipeSave(recipe.writeNBT()));
        }
        if(button.id == 4){
        	if(data.containsKey(scroll.getSelected())){
        		Packets.sendServer(new SPacketRecipeRemove(data.get(scroll.getSelected())));
        		scroll.clear();
        	}
        }
        if(button.id == 5){
        	container.recipe.ignoreDamage = button.getValue() == 1;
        }
        if(button.id == 6){
        	container.recipe.ignoreNBT = button.getValue() == 1;
        }
    }
	@Override
	public void setGuiData(CompoundTag compound) {
		RecipeCarpentry recipe = RecipeCarpentry.load(compound);
		getTextField(0).setValue(recipe.name);
		container.setRecipe(recipe,player.level().registryAccess());
		this.getTextField(0).enabled = true;
    	this.getButton(5).setEnabled(true);
    	this.getButton(5).setDisplay(recipe.ignoreDamage?1:0);
    	this.getButton(6).setEnabled(true);
    	this.getButton(6).setDisplay(recipe.ignoreNBT?1:0);
		setSelected(recipe.name);
	}

	@Override
    protected void renderBg(GuiGraphics graphics, float f, int x, int y){
    	super.renderBg(graphics, f, x, y);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, slot);
    	
    	for(int i = 0; i < container.width;i++){
        	for(int j = 0; j < container.width;j++){
				graphics.blit( this.slot,guiLeft + i*18 + 7, guiTop + j*18 + 34, 0, 0, 18, 18);
        	}
    	}
		graphics.blit( this.slot, guiLeft + 86, guiTop + 60, 0, 0, 18, 18);
    }
	@Override
	public void setData(Vector<String> list, Map<String, Integer> data) {
		String name = scroll.getSelected();
		this.data = data;
		scroll.setList(list);
		this.getTextField(0).enabled = name != null;
		this.getButton(5).setEnabled(name != null);
		
		if(name != null)
			scroll.setSelected(name);
	}
	
	@Override
	public void setSelected(String selected) {
		this.selected = selected;
		scroll.setSelected(selected);
	}

	@Override
	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop guiCustomScroll) {
		save();
		selected = scroll.getSelected();
		Packets.sendServer(new SPacketRecipeGet(data.get(selected)));
	}

	@Override
	public void save() {
		GuiTextFieldNop.unfocus();
		if(selected != null && data.containsKey(selected)){
			container.saveRecipe();
			Packets.sendServer(new SPacketRecipeSave(container.recipe.writeNBT()));
		}
	}

	@Override
	public void unFocused(GuiTextFieldNop guiNpcTextField) {
		String name = guiNpcTextField.getValue();
		if(!name.isEmpty() && !data.containsKey(name)){
			String old = container.recipe.name;
			data.remove(container.recipe.name);
			container.recipe.name = name;
			//data.put(container.recipe.name, container.recipe.id);
			selected = name;
			scroll.replace(old,container.recipe.name);
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {}
}
