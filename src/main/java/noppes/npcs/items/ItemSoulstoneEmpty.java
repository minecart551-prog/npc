package noppes.npcs.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.*;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.shared.common.CommonUtil;

public class ItemSoulstoneEmpty extends Item {

	public ItemSoulstoneEmpty(){
		super(new Item.Properties().stacksTo(64));
	}
    
	public boolean store(LivingEntity entity, ItemStack stack, Player player) {
		if(!hasPermission(entity, player) || entity instanceof Player)
			return false;
		ItemStack stone = new ItemStack(CustomItems.soulstoneFull);
		CompoundTag compound = new CompoundTag();
		if(!entity.saveAsPassenger(compound))
			return false;
		ServerCloneController.Instance.cleanTags(compound);
		stone.addTagElement("Entity", compound);
		
        String name = entity.getEncodeId();
        if (name == null)
        	name = "generic";
		stone.addTagElement("Name", StringTag.valueOf(name));
        if(entity instanceof EntityNPCInterface){
        	EntityNPCInterface npc = (EntityNPCInterface) entity;
    		stone.addTagElement("DisplayName", StringTag.valueOf(entity.getName().getString()));
    		if(npc.role.getType() == RoleType.COMPANION){
    			RoleCompanion role = (RoleCompanion) npc.role;
        		stone.addTagElement("ExtraText", StringTag.valueOf("companion.stage,: ," + role.stage.name));
    		}
        }
        else if(entity.hasCustomName())
    		stone.addTagElement("DisplayName", StringTag.valueOf(entity.getCustomName().getString()));
		NoppesUtilServer.GivePlayerItem(player, player, stone);
		
		if(!player.getAbilities().instabuild){
			stack.split(1);
			if(stack.getCount() <= 0)
				player.getInventory().removeItem(stack);
		}
		
		entity.discard();
		return true;
	}
	
	public boolean hasPermission(LivingEntity entity, Player player){
		if(CommonUtil.isOp(player))
			return true;
		if(CustomNpcsPermissions.hasPermission((ServerPlayer) player, CustomNpcsPermissions.SOULSTONE_ALL))
			return true;
		if(entity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if(npc.role.getType() == RoleType.COMPANION){
				RoleCompanion role = (RoleCompanion) npc.role;
				if(role.getOwner() == player)
					return true;
			}
			if(npc.role.getType() == RoleType.FOLLOWER){
				RoleFollower role = (RoleFollower) npc.role;
				if(role.getOwner() == player)
					return !role.refuseSoulStone;
			}
			return CustomNpcs.SoulStoneNPCs;
		}
		if(entity instanceof Animal)
			return CustomNpcs.SoulStoneAnimals;
		
		return false;
	}
}
