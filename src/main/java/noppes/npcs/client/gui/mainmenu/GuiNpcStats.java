package noppes.npcs.client.gui.mainmenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Difficulty;
import noppes.npcs.client.gui.*;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.constants.EnumMenuType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataStats;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketMenuGet;
import noppes.npcs.packets.server.SPacketMenuSave;
import noppes.npcs.shared.client.gui.components.GuiButtonYesNo;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.gui.components.GuiTextFieldNop;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.listeners.IGuiData;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;

public class GuiNpcStats extends GuiNPCInterface2 implements ITextfieldListener, IGuiData {
	private DataStats stats;
	public GuiNpcStats(EntityNPCInterface npc) {
		super(npc,2);
		stats = npc.stats;
		Packets.sendServer(new SPacketMenuGet(EnumMenuType.STATS));
	}

	@Override
    public void init(){
        super.init();
        int y = guiTop + 10;
        addLabel(new GuiLabel(0,"stats.health", guiLeft + 4, y + 5,"guihint.npchealth"));
        addTextField(new GuiTextFieldNop(0,this, guiLeft + 85, y, 50, 18, stats.maxHealth+""));
        getTextField(0).numbersOnly = true;
        getTextField(0).setMinMaxDefault(0, Integer.MAX_VALUE, 20);
        addLabel(new GuiLabel(1,"stats.aggro", guiLeft + 140, y + 5, "guihint.npcaggrorange"));
        addTextField(new GuiTextFieldNop(1, this,  guiLeft + 220, y, 50, 18, stats.aggroRange+""));
        getTextField(1).numbersOnly = true;
        getTextField(1).setMinMaxDefault(1, 512, 2);
        addLabel(new GuiLabel(34,"stats.creaturetype", guiLeft + 275, y + 5, "guihint.npccreaturetype"));
    	addButton(new GuiButtonNop(this, 8,guiLeft + 355, y, 56, 20, new String[]{"stats.normal","stats.undead","stats.arthropod"} ,stats.getCreatureType()));

    	addButton(new GuiButtonNop(this, 0,guiLeft + 82, y+=22, 56, 20, "selectServer.edit"));
    	addLabel(new GuiLabel(2,"stats.respawn", guiLeft + 4, y + 5));


    	this.addButton(new GuiButtonNop(this, 2, guiLeft + 82, y+=22, 56, 20, "selectServer.edit"));
		GuiLabel meleePropsLabel = new GuiLabel(5,"stats.meleeproperties", guiLeft + 4, y + 5, "guihint.npcmeleeprops");
		if(minecraft!=null && minecraft.level.getDifficulty()== Difficulty.PEACEFUL) {
			((MutableComponent) meleePropsLabel.getMessage()).append(Component.literal("!").setStyle(Style.EMPTY.withColor(0xfc0345).withBold(true)));
			meleePropsLabel.setTooltip(Tooltip.create(Component.translatable("guihint.npcmeleeprops").setStyle(Style.EMPTY.withColor(0xffc65c))
					.append(Component.translatable("guihint.npcmeleeprops.peaceful").setStyle(Style.EMPTY.withColor(0xfc0345)))));
			meleePropsLabel.setWidth(Minecraft.getInstance().font.width(meleePropsLabel.getMessage()));
		}
    	addLabel(meleePropsLabel);
    	this.addButton(new GuiButtonNop(this, 3, guiLeft + 82, y+=22, 56, 20, "selectServer.edit"));
		GuiLabel rangedPropsLabel = new GuiLabel(6,"stats.rangedproperties", guiLeft + 4, y + 5, "guihint.npcrangedprops");
		if(minecraft!=null && minecraft.level.getDifficulty()== Difficulty.PEACEFUL) {
			((MutableComponent) rangedPropsLabel.getMessage()).append(Component.literal("!").setStyle(Style.EMPTY.withColor(0xfc0345).withBold(true)));
			rangedPropsLabel.setTooltip(Tooltip.create(Component.translatable("guihint.npcrangedprops").setStyle(Style.EMPTY.withColor(0xffc65c))
					.append(Component.translatable("guihint.npcrangedprops.peaceful").setStyle(Style.EMPTY.withColor(0xfc0345)))));
			rangedPropsLabel.setWidth(Minecraft.getInstance().font.width(rangedPropsLabel.getMessage()));
		}
		addLabel(rangedPropsLabel);
    	this.addButton(new GuiButtonNop(this, 9, guiLeft + 217, y, 56, 20, "selectServer.edit"));
    	addLabel(new GuiLabel(7,"stats.projectileproperties", guiLeft + 140, y + 5, "guihint.npcprojectiletype"));

    	this.addButton(new GuiButtonNop(this, 15, guiLeft + 82, y+=34, 56, 20, "selectServer.edit"));
    	addLabel(new GuiLabel(15,"effect.minecraft.resistance", guiLeft + 4, y + 5, "guihint.npcresistance"));


    	addButton(new GuiButtonNop(this, 4,guiLeft + 82, y+=34, 56, 20, new String[]{"gui.no","gui.yes"} ,npc.fireImmune()? 1:0));
    	addLabel(new GuiLabel(10,"stats.fireimmune", guiLeft + 4, y + 5, "guihint.npcimmunetofire"));
    	addButton(new GuiButtonNop(this, 5,guiLeft + 217, y, 56, 20, new String[]{"gui.no","gui.yes"} ,stats.canDrown? 1:0));
    	addLabel(new GuiLabel(11,"stats.candrown", guiLeft + 140, y + 5, "guihint.npccandrown"));
    	addTextField(new GuiTextFieldNop(14, this, guiLeft + 355, y, 56, 20, stats.healthRegen + "").setNumbersOnly());
    	addLabel(new GuiLabel(14,"stats.regenhealth", guiLeft + 275, y + 5, "guihint.npchealthregen"));

    	addTextField(new GuiTextFieldNop(16, this, guiLeft + 355, y+=22, 56, 20, stats.combatRegen + "").setNumbersOnly());
    	addLabel(new GuiLabel(16,"stats.combatregen", guiLeft + 275, y + 5, "guihint.npccombatregen"));
    	addButton(new GuiButtonNop(this, 6,guiLeft + 82, y, 56, 20, new String[]{"gui.no","gui.yes"} ,stats.burnInSun? 1:0));
    	addLabel(new GuiLabel(12,"stats.burninsun", guiLeft + 4, y + 5, "guihint.npcburnsinsun"));
    	addButton(new GuiButtonNop(this, 7,guiLeft + 217, y, 56, 20, new String[]{"gui.no","gui.yes"} ,stats.noFallDamage? 1:0));
    	addLabel(new GuiLabel(13,"stats.nofalldamage", guiLeft + 140, y + 5, "guihint.npcnofalldamage"));

    	addButton(new GuiButtonYesNo(this, 17, guiLeft + 82, y+=22, 56, 20, stats.potionImmune));
    	addLabel(new GuiLabel(17,"stats.potionImmune", guiLeft + 4, y + 5, "guihint.npcpotionimmune"));

		addLabel(new GuiLabel(22,"ai.cobwebAffected", guiLeft + 140, y + 5, "guihint.npccobwebaffected"));
    	addButton(new GuiButtonNop(this, 22 ,guiLeft + 217, y, 56, 20,  new String[]{"gui.no", "gui.yes"}, stats.ignoreCobweb ? 0:1));
    }	
    
	@Override
	public void unFocused(GuiTextFieldNop textfield){
		if(textfield.id == 0){
			stats.maxHealth = textfield.getInteger();
			npc.heal(stats.maxHealth);
		}
		else if(textfield.id == 1){
			stats.aggroRange = textfield.getInteger();
		}
		else if(textfield.id == 14){
			stats.healthRegen = textfield.getInteger();
		}
		else if(textfield.id == 16){
			stats.combatRegen = textfield.getInteger();
		}
	}
	
    @Override
	public void buttonEvent(GuiButtonNop guibutton){
		GuiButtonNop button = (GuiButtonNop) guibutton;
		if (button.id == 0){
			setSubGui(new SubGuiNpcRespawn(this.stats));
		}
		else if (button.id == 2){
			setSubGui(new SubGuiNpcMeleeProperties(this.stats.melee));
		}
		else if (button.id == 3){
			setSubGui(new SubGuiNpcRangeProperties(this.stats));
		}
		else if(button.id == 4){
			npc.setImmuneToFire(button.getValue() == 1);
		}
		else if(button.id == 5){
			stats.canDrown = button.getValue() == 1;
		}
		else if(button.id == 6){
			stats.burnInSun = button.getValue() == 1;
		}
		else if(button.id == 7){
			stats.noFallDamage = button.getValue() == 1;
		}
		else if (button.id == 8) {
			stats.setCreatureType(button.getValue());
		}
		else if (button.id == 9) {
			setSubGui(new SubGuiNpcProjectiles(this.stats.ranged));
		}
		else if (button.id == 15) {
			setSubGui(new SubGuiNpcResistanceProperties(this.stats.resistances));
		}
		else if (button.id == 17) {
			stats.potionImmune = ((GuiButtonYesNo)guibutton).getBoolean();
		}
		else if (button.id == 22) {
			stats.ignoreCobweb = (button.getValue() == 0);
		}
    }
    
	@Override
	public void save() {
		Packets.sendServer(new SPacketMenuSave(EnumMenuType.STATS, stats.save(new CompoundTag())));
	}

	@Override
	public void setGuiData(CompoundTag compound) {
		stats.readToNBT(compound);
		init();
	}
}
