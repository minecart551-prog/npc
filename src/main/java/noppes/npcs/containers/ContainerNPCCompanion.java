package noppes.npcs.containers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.CustomContainer;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

public class ContainerNPCCompanion extends ContainerNpcInterface{
    public InventoryNPC currencyMatrix;
	public RoleCompanion role;

    public ContainerNPCCompanion(int containerId, Inventory playerInventory, int entityId){
    	super(CustomContainer.container_companion, containerId, playerInventory);
    	EntityNPCInterface npc = (EntityNPCInterface) player.level().getEntity(entityId);
        role = (RoleCompanion) npc.role;        
        for (int k = 0; k < 3; k++){
            for (int j1 = 0; j1 < 9; j1++){
            	addSlot(new Slot(playerInventory, j1 + k * 9 + 9, 6 + j1 * 18, 87 + k * 18));
            }
        }

        for (int l = 0; l < 9; l++){
        	addSlot(new Slot(playerInventory, l, 6 + l * 18, 145));
        }
        
		if(role.talents.containsKey(EnumCompanionTalent.INVENTORY)){
			int size = (role.getTalentLevel(EnumCompanionTalent.INVENTORY) + 1) * 2;
			for(int i = 0; i < size; i++){
				addSlot(new Slot(role.inventory, i, 114 + i % 3 * 18, 8 + i / 3 * 18));
			}
		}
		if(role.getTalentLevel(EnumCompanionTalent.ARMOR) > 0){
        	addSlot(new SlotCompanionArmor(role, npc.inventory, 0, 6, 8, EquipmentSlot.HEAD));
        	addSlot(new SlotCompanionArmor(role, npc.inventory, 1, 6, 26, EquipmentSlot.CHEST));
        	addSlot(new SlotCompanionArmor(role, npc.inventory, 2, 6, 44 , EquipmentSlot.LEGS));
        	addSlot(new SlotCompanionArmor(role, npc.inventory, 3, 6, 62,  EquipmentSlot.FEET));
		}
		if(role.getTalentLevel(EnumCompanionTalent.SWORD) > 0){
        	addSlot(new SlotCompanionWeapon(role, npc.inventory, 4, 79, 17));
		}
    }

    @Override
    public ItemStack quickMoveStack(Player par1Player, int i){
        return ItemStack.EMPTY;
    }
    
    @Override
    public void removed(Player entityplayer){
        super.removed(entityplayer);
        role.setStats();
    }
}
