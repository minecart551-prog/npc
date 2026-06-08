package noppes.npcs.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;

import java.util.List;

public class ItemSoulstoneFilled extends Item {
	public ItemSoulstoneFilled(){
		super(new Item.Properties().stacksTo(1));
	}

    @Override
	@Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
    	CompoundTag compound = stack.getTag();
    	if(compound == null || !compound.contains("Entity", 10)){
    		list.add(Component.literal(ChatFormatting.RED + "Error"));
    		return;
    	}    	
    	Component name = Component.translatable(compound.getString("Name"));
    	if(compound.contains("DisplayName"))
    		name = Component.translatable(compound.getString("DisplayName")).append(" (").append(name).append(")");
    	list.add(Component.literal(ChatFormatting.BLUE + "").append(name));

    	if(stack.getTag().contains("ExtraText")){
			MutableComponent text = Component.literal("");
    		String[] split = compound.getString("ExtraText").split(",");
    		for(String s : split)
    			text.append(Component.translatable(s));
    		list.add(text);
    	}
    }
    
    @Override
	public InteractionResult useOn(UseOnContext context){
    	if(context.getLevel().isClientSide)
    		return InteractionResult.SUCCESS;
		ItemStack stack = context.getItemInHand();
    	if(Spawn(context.getPlayer(), stack, context.getLevel(), context.getClickedPos()) == null)
    		return InteractionResult.FAIL;
		
		if(!context.getPlayer().getAbilities().instabuild)
			stack.split(1);
    	return InteractionResult.SUCCESS;
    }
    
    public static Entity Spawn(Player player, ItemStack stack, Level level, BlockPos pos){
        if(level.isClientSide)
        	return null;
    	if(stack.getTag() == null || !stack.getTag().contains("Entity", 10))
    		return null;
    	CompoundTag compound = stack.getTag().getCompound("Entity");
    	Entity entity = EntityType.create(compound, level).orElse(null);
    	if(entity == null)
    		return null;
    	entity.setPos(pos.getX() + 0.5, pos.getY() + 1 + 0.2F, pos.getZ() + 0.5);
    	if(entity instanceof EntityNPCInterface){
    		EntityNPCInterface npc = (EntityNPCInterface) entity;
    		npc.ais.setStartPos(pos);
    		npc.setHealth(npc.getMaxHealth());
    		npc.setPos((float)pos.getX() + 0.5F, npc.getStartYPos(), (float)pos.getZ() + 0.5F);
    		
    		if(npc.role.getType() == RoleType.COMPANION && player != null){
    			PlayerData data = PlayerData.get(player);
    			if(data.hasCompanion())
    				return null;
    			((RoleCompanion)npc.role).setOwner(player);
    			data.setCompanion(npc);
    		}
    		if(npc.role.getType() == RoleType.FOLLOWER && player != null){
    			((RoleFollower)npc.role).setOwner(player);
    		}
    	}
		if(!level.addFreshEntity(entity)) {
			player.sendSystemMessage(Component.translatable("error.failedToSpawn"));
			return null;
		}
		return entity;
    }
}
