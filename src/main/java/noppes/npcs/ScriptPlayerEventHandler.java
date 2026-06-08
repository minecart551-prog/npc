package noppes.npcs;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemNbtBook;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketItemUpdate;
import org.jetbrains.annotations.Nullable;

public class ScriptPlayerEventHandler implements ServerTickEvents.StartTick, AttackBlockCallback, UseBlockCallback,
		UseEntityCallback, UseItemCallback, PlayerBlockBreakEvents.Before, ServerMessageEvents.AllowChatMessage,
		ServerLivingEntityEvents.AllowDamage, ServerLivingEntityEvents.AllowDeath, ServerPlayConnectionEvents.Join,
		ServerPlayConnectionEvents.Disconnect {
	@Override
	public void onStartTick(MinecraftServer server) {
		for(Player player : server.getPlayerList().getPlayers()) {
			PlayerData data = PlayerData.get(player);

			if (player.tickCount % 10 == 0) {
				EventHooks.onPlayerTick(data.scriptData);
				for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
					ItemStack item = player.getInventory().getItem(i);
					if (!item.isEmpty() && item.getItem() == CustomItems.scripted_item) {
						ItemScriptedWrapper isw = (ItemScriptedWrapper) NpcAPI.Instance().getIItemStack(item);
						EventHooks.onScriptItemUpdate(isw, player);
						if (isw.updateClient) {
							isw.updateClient = false;
							Packets.send((ServerPlayer) player, new PacketItemUpdate(i, isw.getMCNbt()));
						}
					}
				}
			}
			if (data.playerLevel != player.experienceLevel) {
				EventHooks.onPlayerLevelUp(data.scriptData, data.playerLevel - player.experienceLevel);
				data.playerLevel = player.experienceLevel;
			}
			data.timers.update();
		}
	}

	@Override
	public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		if (world.isClientSide || hand != InteractionHand.MAIN_HAND || !(world instanceof ServerLevel))
			return InteractionResult.PASS;
		if (player.getItemInHand(hand).getItem() == CustomItems.teleporter) {
			return InteractionResult.FAIL;
		}
		boolean isCancelled = false;

		PlayerScriptData handler = PlayerData.get(player).scriptData;
		PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(world, pos));
		isCancelled = (EventHooks.onPlayerAttack(handler, ev));

		if (player.getItemInHand(hand).getItem() == CustomItems.scripted_item && !isCancelled) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(player.getItemInHand(hand));
			ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(world, pos));
			eve.setCanceled(isCancelled);
			isCancelled = (EventHooks.onScriptItemAttack(isw, eve));
		}
		return isCancelled ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	@Override
	public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if(player.level().isClientSide || hand != InteractionHand.MAIN_HAND || !(world instanceof ServerLevel))
			return InteractionResult.PASS;
		if(player.getItemInHand(hand).getItem() == CustomItems.nbt_book) {
			((ItemNbtBook)player.getItemInHand(hand).getItem()).blockEvent(player, world, hand, hitResult);
			return InteractionResult.FAIL;
		}
		if(player.getItemInHand(hand).getItem() == CustomItems.teleporter) {
			return InteractionResult.FAIL;
		}
		boolean isCancelled = false;

		PlayerScriptData handler = PlayerData.get(player).scriptData;
		handler.hadInteract = true;
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(world, hitResult.getBlockPos()));
		isCancelled = EventHooks.onPlayerInteract(handler, ev);

		if(player.getItemInHand(hand).getItem() == CustomItems.scripted_item && !isCancelled) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(player.getItemInHand(hand));
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(world, hitResult.getBlockPos()));
			isCancelled = (EventHooks.onScriptItemInteract(isw, eve));
		}
		return isCancelled ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	@Override
	public InteractionResult interact(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		if(world.isClientSide || hand != InteractionHand.MAIN_HAND || !(world instanceof ServerLevel))
			return InteractionResult.PASS;
		if(player.getItemInHand(hand).getItem() == CustomItems.nbt_book) {
			((ItemNbtBook)player.getItemInHand(hand).getItem()).entityEvent(player, world, hand, entity, hitResult);
			return InteractionResult.FAIL;
		}
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 1, NpcAPI.Instance().getIEntity(entity));
		boolean isCancelled = false;
		isCancelled = EventHooks.onPlayerInteract(handler, ev);

		if(player.getItemInHand(hand).getItem() == CustomItems.scripted_item && !isCancelled) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(player.getItemInHand(hand));
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 1, NpcAPI.Instance().getIEntity(entity));
			isCancelled = (EventHooks.onScriptItemInteract(isw, eve));
		}
		return isCancelled ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> interact(Player player, Level world, InteractionHand hand) {
		if(world.isClientSide || hand != InteractionHand.MAIN_HAND || !(world instanceof ServerLevel))
			return InteractionResultHolder.pass(player.getItemInHand(hand));

		if(player.isCreative() && player.isCrouching() && player.getItemInHand(hand).getItem() == CustomItems.scripted_item){
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.ScriptItem, null);
			return InteractionResultHolder.pass(player.getItemInHand(hand));
		}
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		if(handler.hadInteract) {
			handler.hadInteract = false;
			return InteractionResultHolder.pass(player.getItemInHand(hand));
		}
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 0, null);
		boolean isCancelled = (EventHooks.onPlayerInteract(handler, ev));

		if(player.getItemInHand(hand).getItem() == CustomItems.scripted_item && !isCancelled) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(player.getItemInHand(hand));
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 0, null);
			isCancelled = (EventHooks.onScriptItemInteract(isw, eve));
		}
		return isCancelled ? InteractionResultHolder.fail(player.getItemInHand(hand)) : InteractionResultHolder.pass(player.getItemInHand(hand));
	}

//	@SubscribeEvent
//	public void invoke(ArrowLooseEvent event){
//		if(event.getEntity().level().isClientSide || !(event.getLevel() instanceof ServerLevel))
//			return;
//		PlayerScriptData handler = PlayerData.get(event.getEntity()).scriptData;
//		PlayerEvent.RangedLaunchedEvent ev = new PlayerEvent.RangedLaunchedEvent(handler.getPlayer());
//		event.setCanceled(EventHooks.onPlayerRanged(handler, ev));
//	}

	@Override
	public boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		if(player.level().isClientSide || !(world instanceof ServerLevel))
			return true;
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		PlayerEvent.BreakEvent ev = new PlayerEvent.BreakEvent(handler.getPlayer(),
				NpcAPI.Instance().getIBlock((ServerLevel)world, pos));
		return !(EventHooks.onPlayerBreak(handler, ev));
	}


//	@SubscribeEvent
//	public void invoke(ItemTossEvent event) {
//		if(!(event.getPlayer().level() instanceof ServerLevel))
//			return;
//		PlayerScriptData handler = PlayerData.get(event.getPlayer()).scriptData;
//		event.setCanceled(EventHooks.onPlayerToss(handler, event.getEntity()));
//	}
//
//	@SubscribeEvent
//	public void invoke(EntityItemPickupEvent event) {
//		if(!(event.getEntity().level() instanceof ServerLevel))
//			return;
//		PlayerScriptData handler = PlayerData.get(event.getEntity()).scriptData;
//		event.setCanceled(EventHooks.onPlayerPickUp(handler, event.getItem()));
//	}
//
//	@SubscribeEvent
//	public void invoke(PlayerContainerEvent.Open event) {
//		if(!(event.getEntity().level() instanceof ServerLevel))
//			return;
//		PlayerScriptData handler = PlayerData.get(event.getEntity()).scriptData;
//		EventHooks.onPlayerContainerOpen(handler, event.getContainer());
//	}
//
//	@SubscribeEvent
//	public void invoke(PlayerContainerEvent.Close event) {
//		if(!(event.getEntity().level() instanceof ServerLevel))
//			return;
//		PlayerScriptData handler = PlayerData.get(event.getEntity()).scriptData;
//		EventHooks.onPlayerContainerClose(handler, event.getContainer());
//	}

	@Override
	public boolean allowDeath(LivingEntity entity, DamageSource damageSource, float damageAmount) {
		if(!(entity.level() instanceof ServerLevel))
			return true;
		Entity sourceEnt = NoppesUtilServer.GetDamageSourcee(damageSource);
		if(entity instanceof Player){
			PlayerScriptData handler = PlayerData.get((Player) entity).scriptData;
			EventHooks.onPlayerDeath(handler, damageSource, sourceEnt);
		}
		if(sourceEnt instanceof Player){
			PlayerScriptData handler = PlayerData.get((Player) sourceEnt).scriptData;
			EventHooks.onPlayerKills(handler, entity);
		}
		return true;
	}

	@Override
	public boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if(!(entity.level() instanceof ServerLevel))
			return true;
		Entity sourceEnt = NoppesUtilServer.GetDamageSourcee(source);
		boolean isCanceled = false;
		if(entity instanceof Player){
			PlayerScriptData handler = PlayerData.get((Player) entity).scriptData;
			PlayerEvent.DamagedEvent pevent = new PlayerEvent.DamagedEvent(handler.getPlayer(), sourceEnt, amount, source);
			isCanceled = (EventHooks.onPlayerDamaged(handler, pevent));
			//event.setAmount(pevent.damage); TODO
		}

		if(sourceEnt instanceof Player){
			PlayerScriptData handler = PlayerData.get((Player) sourceEnt).scriptData;
			PlayerEvent.DamagedEntityEvent pevent = new PlayerEvent.DamagedEntityEvent(handler.getPlayer(), entity, amount, source);
			isCanceled = (EventHooks.onPlayerDamagedEntity(handler, pevent));
			//event.setAmount(pevent.damage);
		}
		return !isCanceled;
	}

//	@SubscribeEvent(priority = EventPriority.LOW)
//	public void invoke(LivingAttackEvent event) {
//		if(!(event.getEntity().level() instanceof ServerLevel))
//			return;
//		Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
//		if(source instanceof Player){
//			PlayerScriptData handler = PlayerData.get((Player) source).scriptData;
//			ItemStack item = ((Player) source).getMainHandItem();
//			IEntity target = NpcAPI.Instance().getIEntity(event.getEntity());
//			IDamageSource damageSource = NpcAPI.Instance().getIDamageSource(event.getSource());
//			PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), target, damageSource);
//			event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
//			if(item.getItem() == CustomItems.scripted_item && !event.isCanceled()) {
//				ItemScriptedWrapper isw = ItemScripted.GetWrapper(item);
//				ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), target, damageSource);
//				eve.setCanceled(event.isCanceled());
//				event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
//			}
//		}
//	}

	@Override
	public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		if(!(handler.player.level() instanceof ServerLevel))
			return;
		PlayerScriptData scriptData = PlayerData.get(handler.player).scriptData;
		EventHooks.onPlayerLogin(scriptData);
	}

	@Override
	public void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
		if(!(handler.player.level() instanceof ServerLevel))
			return;
		PlayerScriptData scriptData = PlayerData.get(handler.player).scriptData;
		EventHooks.onPlayerLogout(scriptData);
	}

	//TODO priority highest
	@Override
	public boolean allowChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
		if(!(sender.level() instanceof ServerLevel) || sender == EntityNPCInterface.ChatEventPlayer)
			return true;
		PlayerScriptData handler = PlayerData.get(sender).scriptData;
		String messageStr = message.decoratedContent().getString();
		PlayerEvent.ChatEvent ev = new PlayerEvent.ChatEvent(handler.getPlayer(), message.decoratedContent().getString());
		EventHooks.onPlayerChat(handler, ev);
		boolean isCanceled =  (ev.isCanceled());
//		if(!messageStr.equals(ev.message)) { TODO
//			MutableComponent chat = Component.translatable("");
//			chat.append(ForgeHooks.newChatWithLinks(ev.message));
//			event.setMessage(chat);
//		}
		return !isCanceled;
	}




//	private Set<Class> getClasses(String packageName){
//		packageName = packageName.replace('.','/');
//		HashSet<String> urls = new HashSet<>();
//		try{
//			Module module = net.minecraftforge.event.entity.EntityEvent.class.getModule();
//			Enumeration<URL> resources = module.getClassLoader().getResources(packageName);
//			while (resources.hasMoreElements()) {
//				URL url = resources.nextElement();
//				String path = url.getPath();
//				int i = path.indexOf(".jar");
//				if(i > 0) {
//					urls.add(path.substring(0, i + 4));
//				}
//			}
//		}
//		catch(Throwable ignored){}
//		try{
//			Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources(packageName);
//			while (resources.hasMoreElements()) {
//				URL url = resources.nextElement();
//				String path = url.getPath();
//				int i = path.indexOf(".jar");
//				if(i > 0) {
//					urls.add(path.substring(0, i + 4));
//				}
//			}
//		}
//		catch(Throwable ignored){}
//		HashSet<Class> classes = new HashSet<>();
//		for (String path : urls) {
//			try{
//				JarFile file = new JarFile(new File(path));
//				Enumeration<JarEntry> entries = file.entries();
//				while (entries.hasMoreElements()) {
//					JarEntry entry = entries.nextElement();
//					if (entry.isDirectory() || !entry.getName().startsWith(packageName)) {
//						continue;
//					}
//					String name = entry.getName().replace('/', '.');
//					try{
//						Class c = Class.forName(name.substring(0, name.length() - 6));
//						if(Event.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) && Modifier.isPublic(c.getModifiers())) {
//							if(c.getDeclaredClasses().length > 0){
//								classes.addAll(Arrays.asList(c.getDeclaredClasses()));
//							}
//							else {
//								classes.add(c);
//							}
//						}
//						classes.add(c);
//					}
//					catch(Throwable ignored){}
//				}
//			}
//			catch(Throwable ignored){
//
//			}
//		}
//		return classes;
//	}


//	public ScriptPlayerEventHandler registerForgeEvents() {
//		ForgeEventHandler.eventNames.clear();
//		ForgeEventHandler handler = new ForgeEventHandler();
//		try {
//			Method m = handler.getClass().getMethod("forgeEntity", Event.class);
//			Method register = MinecraftForge.EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class);
//			register.setAccessible(true);
//
//			for(Class c : getClasses("net.minecraftforge.event.")) {
//				try{
//					if(GenericEvent.class.isAssignableFrom(c) || EntityEvent.EntityConstructing.class.isAssignableFrom(c) || LevelEvent.CreateSpawnPosition.class.isAssignableFrom(c)
//							|| TickEvent.RenderTickEvent.class.isAssignableFrom(c) || TickEvent.ClientTickEvent.class.isAssignableFrom(c)
//							|| NetworkEvent.ClientCustomPayloadEvent.class.isAssignableFrom(c) || ItemTooltipEvent.class.isAssignableFrom(c)) {
//						continue;
//					}
//					String eventName = ForgeEventHandler.getEventName(c);
//					if(!ForgeEventHandler.eventNames.contains(eventName)){
//						register.invoke(MinecraftForge.EVENT_BUS, c, handler, m);
//						ForgeEventHandler.eventNames.add(eventName);
//					}
//				}
//				catch(Throwable ignored){}
//			}
//			if(PixelmonHelper.Enabled) {
//		        try {
//		            for(Class c : getClasses("com.pixelmonmod.pixelmon.api.events.")){
//						ForgeEventHandler.eventNames.add(ForgeEventHandler.getEventName(c));
//						register.invoke(PixelmonHelper.EVENT_BUS, c, handler, m);
//		            }
//		        } catch (Throwable e) {
//					LogWriter.except(e);
//		        }
//			}
//		} catch (Throwable e) {
//			LogWriter.except(e);
//		}
//		return this;
//	}
}
