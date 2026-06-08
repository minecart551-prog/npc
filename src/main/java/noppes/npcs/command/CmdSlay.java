package noppes.npcs.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CmdSlay{
	static Map<String, Class<?>> slayMap = new LinkedHashMap<String, Class<?>>();

	private static Map<String, Class<?>> getSlay(Level level){
		if(!slayMap.isEmpty()){
			return slayMap;
		}

		slayMap.put("all",LivingEntity.class);
		slayMap.put("mobs", Monster.class);
		slayMap.put("animals", Animal.class);
		slayMap.put("items", ItemEntity.class);
		slayMap.put("xporbs", ExperienceOrb.class);
		slayMap.put("npcs", EntityNPCInterface.class);

		for(EntityType<?> ent : BuiltInRegistries.ENTITY_TYPE){
			if(ent.getCategory() == MobCategory.MISC)
				continue;
			String name = ent.getDescriptionId();
			try{
				Entity e = ent.create(level);
				e.remove(Entity.RemovalReason.DISCARDED);
				Class<? extends Entity> cls = e.getClass();
				if(EntityNPCInterface.class.isAssignableFrom(cls))
					continue;
				if(!LivingEntity.class.isAssignableFrom(cls))
					continue;
				slayMap.put(name.toLowerCase(), cls);
			}
			catch(Throwable e){

			}
		}

		slayMap.remove("monster");
		slayMap.remove("mob");
		return slayMap;
	}

	public static LiteralArgumentBuilder<CommandSourceStack> register() {

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("slay").requires((source) -> source.hasPermission(4))
				.then(Commands.argument("type", StringArgumentType.word()).then(Commands.argument("range", IntegerArgumentType.integer(1))
						.executes(context -> {
					ArrayList<Class<?>> toDelete = new ArrayList<Class<?>>();
					boolean deleteNPCs = false;
					String delete = StringArgumentType.getString(context, "type");
					Class<?> cls = getSlay(context.getSource().getLevel()).get(delete);
					if(cls != null)
						toDelete.add(cls);
					if(delete.equals("mobs")){
						toDelete.add(Ghast.class);
						toDelete.add(EnderDragon.class);
					}
					if(delete.equals("npcs")) {
						deleteNPCs = true;
					}
					int count = 0;
					int range = IntegerArgumentType.getInteger(context, "range");

					AABB box = new AABB(context.getSource().getPosition(), context.getSource().getPosition().add(1, 1, 1)).inflate(range, range, range);
					List<? extends Entity> list = context.getSource().getLevel().getEntitiesOfClass(LivingEntity.class, box);

					for(Entity entity : list){
						if(entity instanceof Player)
							continue;
						if(entity instanceof TamableAnimal && ((TamableAnimal)entity).isTame())
							continue;
						if(entity instanceof EntityNPCInterface && !deleteNPCs)
							continue;
						if(delete(entity,toDelete))
							count++;
					}
					if(toDelete.contains(ExperienceOrb.class)){
						list = context.getSource().getLevel().getEntitiesOfClass(ExperienceOrb.class, box);
						for(Entity entity : list){
							entity.setRemoved(Entity.RemovalReason.DISCARDED);
							count++;
						}
					}
					if(toDelete.contains(ItemEntity.class)){
						list = context.getSource().getLevel().getEntitiesOfClass(ItemEntity.class, box);
						for(Entity entity : list){
							entity.setRemoved(Entity.RemovalReason.DISCARDED);
							count++;
						}
					}

					int finalCount = count;
					context.getSource().sendSuccess(()->Component.translatable(finalCount + " entities deleted"), false);
					return 1;
				})));
		return command;
	}



    private static boolean delete(Entity entity, ArrayList<Class<?>> toDelete) {
		for(Class<?> delete : toDelete){
			if(delete == Animal.class && (entity instanceof Horse)){
				continue;
			}
			if(delete.isAssignableFrom(entity.getClass())){
				entity.setRemoved(Entity.RemovalReason.DISCARDED);
				return true;
			}
		}
		return false;
	}
}
