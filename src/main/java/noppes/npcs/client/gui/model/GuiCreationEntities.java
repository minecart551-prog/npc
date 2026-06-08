package noppes.npcs.client.gui.model;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiButtonYesNo;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuiCreationEntities extends GuiCreationScreenInterface implements ICustomScrollListener {
	private List<EntityType<? extends Entity>> types;
	private GuiCustomScrollNop scroll;
	private boolean resetToSelected = true;
	
	public GuiCreationEntities(EntityNPCInterface npc){
		super(npc);
		types = getAllEntities(npc.level());
		Collections.sort(types, Comparator.comparing(t -> t.getDescriptionId().toLowerCase()));
		active = 1;
		xOffset = 60;
	}

	private static List<EntityType<? extends Entity>> getAllEntities(Level level){
		List<EntityType<? extends Entity>> data = new ArrayList<>();

		for(EntityType<? extends Entity> ent : BuiltInRegistries.ENTITY_TYPE){
			try {
				Entity e = ent.create(level);
				if(e != null){
					if(LivingEntity.class.isAssignableFrom(e.getClass()) && !EnderDragon.class.isAssignableFrom(e.getClass())){
						data.add(ent);
					}
					e.discard();
				}
			}
			catch(Exception e){

			}
		}

		return data;
	}

    @Override
    public void init() {
    	super.init();
    	addButton(new GuiButtonNop(this, 10, guiLeft, guiTop + 46, 120, 20, "Reset To NPC", button -> {
			playerdata.setEntity(null);
			npc.display.setSkinTexture("customnpcs:textures/entity/humanmale/steve.png");
			resetToSelected = true;
			init();
		}));
    	if(scroll == null){
    		scroll = new GuiCustomScrollNop(this, 0);
    		scroll.setUnsortedList(types.stream().map(EntityType::getDescriptionId).collect(Collectors.toList()));
    	}
    	scroll.guiLeft = guiLeft;
    	scroll.guiTop = guiTop + 68;
    	scroll.setSize(120, imageHeight - 96);

		int index = -1;
		EntityType selectedType = CustomEntities.entityCustomNpc;
    	if(entity != null){
	    	for(int i = 0; i < types.size(); i++){
				EntityType type = types.get(i);
	    		if(type == entity.getType()){
					index = i;
					selectedType = type;
					break;
	    		}
	    	}
    	}
		if(index >= 0){
			scroll.setSelectedIndex(index);
		}
		else{
			scroll.setSelected("entity.customnpcs.customnpc");
		}
    	
    	if(resetToSelected){
    		scroll.scrollTo(scroll.getSelected());
    		resetToSelected = false;
    	}
    	addScroll(scroll);

		addLabel(new GuiLabel(110, "gui.simpleRenderer", guiLeft + 124, guiTop + 5, 0xff0000));
		addButton(new GuiButtonYesNo(this, 110, guiLeft + 260, guiTop, playerdata.simpleRender, b -> playerdata.simpleRender = ((GuiButtonYesNo)b).getBoolean()));
    }

	@Override
	public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
		String selected = scroll.getSelected();
		if(selected.equals("entity.customnpcs.customnpc")){
			playerdata.setEntity(null);
		}
		else{
			playerdata.setEntity(BuiltInRegistries.ENTITY_TYPE.getKey(types.get(scroll.getSelectedIndex())));
		}
		Entity entity = playerdata.getEntity(npc);
		if(entity != null){
			EntityRenderer render = minecraft.getEntityRenderDispatcher().getRenderer(entity);
			try {
				if (render instanceof LivingEntityRenderer && !render.getTextureLocation(entity).equals("minecraft:missingno")) {
					npc.display.setSkinTexture(render.getTextureLocation(entity).toString());
				}
			}catch(Exception e){
				npc.display.setSkinTexture("customnpcs:textures/entity/humanmale/steve.png");
			}
		}
		else{
			npc.display.setSkinTexture("customnpcs:textures/entity/humanmale/steve.png");
		}
		init();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {}

}
