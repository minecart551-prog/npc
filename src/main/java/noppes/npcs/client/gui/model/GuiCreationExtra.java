package noppes.npcs.client.gui.model;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.controllers.CobblemonHelper;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityFakeLiving;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.client.gui.components.*;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;

import java.lang.reflect.Method;
import java.util.*;

public class GuiCreationExtra extends GuiCreationScreenInterface implements ICustomScrollListener, ITextfieldListener {

	private final String[] ignoredTags = {"CanBreakDoors", "Bred", "PlayerCreated", "HasReproduced"};
	private final String[] grimmsTags = {"DataSkin", "DataHair", "DataFace", "DataUniform", "DataGemstone", "DataVisor", "DataGloves", "DataCape"};
	private final String[] booleanTags = {};
	
	private GuiCustomScrollNop scroll;
	private Map<String, GuiType> data = new HashMap<String, GuiType>();
	
	private GuiType selected;
	public int nextAvailableFieldId = 0;
	
	public GuiCreationExtra(EntityNPCInterface npc){
		super(npc);
		active = 2;
	}

    @Override
    public void init() {
    	super.init();
    	if(entity == null){
    		//openGui(new GuiCreationParts(npc));
    		return;
    	}

		data = getData(entity);

    	if(scroll == null){
    		scroll = new GuiCustomScrollNop(this, 0);
    		List<String> list = new ArrayList<String>(data.keySet()); 
    		scroll.setList(list);
    		if(list.isEmpty())
    			return;
    		scroll.setSelected(list.get(0));
    	}
    	selected = data.get(scroll.getSelected());
    	if(selected == null)
    		return;
    	scroll.guiLeft = guiLeft;
    	scroll.guiTop = guiTop + 46;
    	scroll.setSize(100, imageHeight - 74);
    	addScroll(scroll);
    	selected.init();
    }
    
    public Map<String, GuiType> getData(LivingEntity entity){
    	Map<String, GuiType> data = new HashMap<String, GuiType>();
		CompoundTag compound = getExtras(entity);
		Set<String> keys = compound.getAllKeys();
		for(String name : keys){
			if(isIgnored(name))
				continue;
			Tag base = compound.get(name);
			if(name.equals("Age")){
				data.put("Child", new GuiTypeBoolean("Child", entity.isBaby()));
			}
			else if(name.equals("Color") && base.getId() == 1){
				data.put("Color", new GuiTypeByte("Color", compound.getByte("Color")));
			}
			else if(base.getId() == 3){
				data.put(name, new GuiTypeInt(name, compound.getInt(name)));
			}
			else if(base.getId() == 1){
				byte b = ((ByteTag)base).getAsByte();
				if(b != 0 && b != 1)
					continue;
				if(playerdata.extra.contains(name))
					b = playerdata.extra.getByte(name);
				data.put(name, new GuiTypeBoolean(name, b == 1));
			}
		}
		if(PixelmonHelper.isPixelmon(entity)){
			data.put("Model", new GuiTypePixelmon("Model"));
		}
		if(CobblemonHelper.isPokemon(entity)){
			data.put("CobblemonModel", new GuiTypeCobblemon("CobblemonModel"));
		}
		if(entity.getEncodeId().equals("tgvstyle.Dog")){
			data.put("Breed", new GuiTypeDoggyStyle("Breed"));
		}
		return data;
    }

	private boolean isIgnored(String tag){
		for(String s : ignoredTags)
			if(s.equals(tag))
				return true;
		return false;
	}

	private boolean isGrimms(String tag){
		for(String s : grimmsTags)
			if(s.equals(tag))
				return true;
		return false;
	}
	
	private void updateTexture(){
		LivingEntity entity = playerdata.getEntity(npc);
		EntityRenderer render = minecraft.getEntityRenderDispatcher().getRenderer(entity);
		npc.display.setSkinTexture(render.getTextureLocation(entity).toString());
	}

	private CompoundTag getExtras(LivingEntity entity) {
		CompoundTag fake = new CompoundTag();
		new EntityFakeLiving(entity.level()).addAdditionalSaveData(fake);
		
		CompoundTag compound = new CompoundTag();
		try {
			entity.addAdditionalSaveData(compound);
		}
		catch(Throwable e) {
			
		}
		Set<String> keys = fake.getAllKeys();
		for(String name : keys)
			compound.remove(name);
		
		return compound;
	}
	@Override
	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
		if(scroll.id == 0)
			init();
		else if(selected != null){
			selected.scrollClicked(i, j, k, scroll);
		}
	}

    @Override
    public void buttonEvent(GuiButtonNop btn) {
    	if(selected != null)
    		selected.buttonEvent(btn);
    }

	@Override
	public void unFocused(GuiTextFieldNop textfield) {
		if(selected != null)
			selected.unFocused(textfield);
	}


	abstract class GuiType{
    	public String name;
    	public GuiType(String name){
    		this.name = name;
    	}
    	public void init(){};
    	public void buttonEvent(GuiButtonNop button){};
    	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll){};
		public void unFocused(GuiTextFieldNop textfield){}
    }

    class GuiTypeBoolean extends GuiType{
    	private boolean bo;
    	public GuiTypeBoolean(String name, boolean bo){
    		super(name);
    		this.bo = bo;
    	}
		@Override
		public void init() {
			addButton(new GuiButtonYesNo(GuiCreationExtra.this, 11, guiLeft + 120, guiTop + 50, 60, 20, bo));
		}
		@Override
		public void buttonEvent(GuiButtonNop button) {
			if(button.id != 11)
				return;
			bo = ((GuiButtonYesNo)button).getBoolean();
			if(name.equals("Child")){
	    		playerdata.extra.putInt("Age",bo?-24000:0);
	    		playerdata.clearEntity();
			}
			else{
	    		playerdata.extra.putBoolean(name, bo);
	    		playerdata.clearEntity();
				updateTexture();
			}
		}
    	
    }
    class GuiTypeByte extends GuiType{
    	private byte b;
    	public GuiTypeByte(String name, byte b){
    		super(name);
    		this.b = b;
    	}
    	
    	@Override
    	public void init(){
    		addButton(new GuiButtonBiDirectional(GuiCreationExtra.this,11, guiLeft + 120, guiTop + 45, 50, 20, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"}, b));
    	}
		@Override
		public void buttonEvent(GuiButtonNop button) {
			if(button.id != 11)
				return;
			playerdata.extra.putByte(name, (byte)((GuiButtonNop)button).getValue());
    		playerdata.clearEntity();
			updateTexture();
		}
    	
    }

	class GuiTypeInt extends GuiType{
		private int initVal;
		private int fieldId;
		public GuiTypeInt(String name, int b){
			super(name);
			this.initVal = b;
			fieldId = nextAvailableFieldId++;
		}

		@Override
		public void init(){
			GuiTextFieldNop field = new GuiTextFieldNop(11, GuiCreationExtra.this,guiLeft + 120, guiTop + 45, 50, 20, ""+ initVal);
			field.setNumbersOnly();
			addTextField(field);
		}
		@Override
		public void unFocused(GuiTextFieldNop textfield) {
			if(textfield.id != 11)
				return;
			playerdata.extra.putInt(name, textfield.getInteger());
			playerdata.clearEntity();
			updateTexture();
		}
	}

    class GuiTypePixelmon extends GuiType{
    	
		public GuiTypePixelmon(String name) {
			super(name);
		}

		@Override
		public void init() {
			GuiCustomScrollNop scroll = new GuiCustomScrollNop(GuiCreationExtra.this, 1);
			scroll.setSize(120, 200);
			scroll.guiLeft = guiLeft + 120;
			scroll.guiTop = guiTop + 20;
			addScroll(scroll);
			
			scroll.setList(PixelmonHelper.getPixelmonList());
			scroll.setSelected(PixelmonHelper.getName(entity));
		}

		@Override
    	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll){
			String name = scroll.getSelected();
	    	playerdata.setExtra(entity, "name", name);
			updateTexture();
    	};
    	
    }

	class GuiTypeCobblemon extends GuiType{

		public GuiTypeCobblemon(String name) {
			super(name);
		}

		@Override
		public void init() {
			GuiCustomScrollNop scroll = new GuiCustomScrollNop(GuiCreationExtra.this, 1);
			scroll.setSize(120, 200);
			scroll.guiLeft = guiLeft + 120;
			scroll.guiTop = guiTop + 20;
			addScroll(scroll);

			scroll.setList(CobblemonHelper.getTypes());
			scroll.setSelected(CobblemonHelper.getType(entity).toString());
		}

		@Override
		public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll){
			String name = scroll.getSelected();
			playerdata.setExtra(entity, "CobblemonModel", name);
			updateTexture();
		};
	}
    
    class GuiTypeDoggyStyle extends GuiType{
		public GuiTypeDoggyStyle(String name) {
			super(name);
		}

		@Override
		public void init() {
			Enum breed = null;
			try {
				Method method = entity.getClass().getMethod("getBreedID");
				breed = (Enum) method.invoke(entity);
			} catch (Exception e) {
				
			}
	    	addButton(new GuiButtonBiDirectional(GuiCreationExtra.this,11, guiLeft + 120, guiTop + 45, 50, 20, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"}, breed.ordinal()));
		}
		
		@Override
		public void buttonEvent(GuiButtonNop button) {
			if(button.id != 11)
				return;
			int breed = ((GuiButtonNop)button).getValue();
	    	LivingEntity entity = playerdata.getEntity(npc);
	    	playerdata.setExtra(entity, "breed", ((GuiButtonNop)button).getValue() + "");
			updateTexture();
		}
    }

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {}
}
