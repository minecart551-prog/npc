package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.shared.client.gui.components.GuiBasic;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;
import noppes.npcs.shared.client.util.NoppesStringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;



public class GuiSoundSelection extends GuiBasic implements ICustomScrollListener{

	private GuiCustomScrollNop scrollCategories;
	private GuiCustomScrollNop scrollQuests;
	
	private String selectedDomain;
	public ResourceLocation selectedResource;
		
	private HashMap<String,List<String>> domains = new HashMap<String,List<String>>();
		
    public GuiSoundSelection(String sound){
    	drawDefaultBackground = false;
		title = "";
		setBackground("menubg.png");
		imageWidth = 366;
		imageHeight = 226;
		
    	SoundManager handler = Minecraft.getInstance().getSoundManager();
    	Collection<ResourceLocation> set = handler.getAvailableSounds();
    	for(ResourceLocation location : set){
    		List<String> list = domains.get(location.getNamespace());
    		if(list == null)
    			domains.put(location.getNamespace(), list = new ArrayList<String>());
    		list.add(location.getPath());
    		domains.put(location.getNamespace(), list);
    	}
    	if(sound != null && !sound.isEmpty()){
    		selectedResource = new ResourceLocation(sound);
    		selectedDomain = selectedResource.getNamespace();
    		if(!domains.containsKey(selectedDomain)) {
    			selectedDomain = null;
    		}
    	}
    }

    @Override
    public void init(){
        super.init();
    	this.addButton(new GuiButtonNop(this, 2, guiLeft + imageWidth - 26, guiTop + 4, 20, 20, "X"));
    	this.addButton(new GuiButtonNop(this, 1, guiLeft + 125, guiTop + 212, 70, 20, "gui.play", selectedResource != null));
		this.addButton(new GuiButtonNop(this, 3, guiLeft + 195, guiTop + 212, 70, 20, "gui.copy", selectedResource != null));
        
        if(scrollCategories == null){
	        scrollCategories = new GuiCustomScrollNop(this,0);
	        scrollCategories.setSize(90, 200);
        }
        scrollCategories.setList(Lists.newArrayList(domains.keySet()));
        if(selectedDomain != null) {
        	scrollCategories.setSelected(selectedDomain);
        }
        
        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 14;
        this.addScroll(scrollCategories);
        
        if(scrollQuests == null){
        	scrollQuests = new GuiCustomScrollNop(this,1);
        	scrollQuests.setSize(250, 200);
        }        
        if(selectedDomain != null) {
        	scrollQuests.setList(domains.get(selectedDomain));
        }
        if(selectedResource != null) {
        	scrollQuests.setSelected(selectedResource.getPath());
        }
        scrollQuests.guiLeft = guiLeft + 95;
        scrollQuests.guiTop = guiTop + 14;
        this.addScroll(scrollQuests);

    }

	@Override
	public void buttonEvent(GuiButtonNop guibutton){

        if(guibutton.id == 1){
        	MusicController.Instance.stopMusic();
        	BlockPos pos = player.blockPosition();
        	MusicController.Instance.playSound(SoundSource.NEUTRAL, selectedResource.toString(), pos, 1, 1);
        }
		if(guibutton.id == 3){
			NoppesStringUtils.setClipboardContents(selectedResource.toString());
		}
        if(guibutton.id == 2){
    		close();
        }
    }

	@Override
	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
		if(scroll.id == 0){
			selectedDomain = scroll.getSelected();
			selectedResource = null;
			scrollQuests.clearSelection();
		}
		if(scroll.id == 1){
			selectedResource = new ResourceLocation(selectedDomain, scroll.getSelected());	
		}		
		init();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {
		if(selectedResource == null)
			return;
		close();
	}

}
