package noppes.npcs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;


public class CustomNpcsPermissions{
	public static final PermissionNode<Boolean> NPC_DELETE = new PermissionNode<Boolean>("customnpcs", "npc.delete", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_CREATE = new PermissionNode<Boolean>("customnpcs", "npc.create", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_GUI = new PermissionNode<Boolean>("customnpcs", "npc.gui", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_FREEZE = new PermissionNode<Boolean>("customnpcs", "npc.freeze", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_RESET = new PermissionNode<Boolean>("customnpcs", "npc.reset", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_AI = new PermissionNode<Boolean>("customnpcs", "npc.ai", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_ADVANCED = new PermissionNode<Boolean>("customnpcs", "npc.advanced", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_DISPLAY = new PermissionNode<Boolean>("customnpcs", "npc.display", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_INVENTORY = new PermissionNode<Boolean>("customnpcs", "npc.inventory", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_STATS = new PermissionNode<Boolean>("customnpcs", "npc.stats", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> NPC_CLONE = new PermissionNode<Boolean>("customnpcs", "npc.clone", PermissionTypes.BOOLEAN, (player, id, context) -> true);

	public static final PermissionNode<Boolean> GLOBAL_LINKED = new PermissionNode<Boolean>("customnpcs", "global.linked", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_PLAYERDATA = new PermissionNode<Boolean>("customnpcs", "global.playerdata", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_BANK = new PermissionNode<Boolean>("customnpcs", "global.bank", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_DIALOG = new PermissionNode<Boolean>("customnpcs", "global.dialog", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_QUEST = new PermissionNode<Boolean>("customnpcs", "global.quest", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_FACTION = new PermissionNode<Boolean>("customnpcs", "global.faction", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_TRANSPORT = new PermissionNode<Boolean>("customnpcs", "global.transport", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_RECIPE = new PermissionNode<Boolean>("customnpcs", "global.recipe", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> GLOBAL_NATURALSPAWN = new PermissionNode<Boolean>("customnpcs", "global.naturalspawn", PermissionTypes.BOOLEAN, (player, id, context) -> true);

	public static final PermissionNode<Boolean> SPAWNER_MOB = new PermissionNode<Boolean>("customnpcs", "spawner.mob", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> SPAWNER_CREATE = new PermissionNode<Boolean>("customnpcs", "spawner.create", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	
	public static final PermissionNode<Boolean> TOOL_MOUNTER = new PermissionNode<Boolean>("customnpcs", "tool.mounter", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> TOOL_PATHER = new PermissionNode<Boolean>("customnpcs", "tool.pather", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> TOOL_SCRIPTER = new PermissionNode<Boolean>("customnpcs", "tool.scripter", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> TOOL_NBTBOOK = new PermissionNode<Boolean>("customnpcs", "tool.nbtbook", PermissionTypes.BOOLEAN, (player, id, context) -> true);

	public static final PermissionNode<Boolean> EDIT_VILLAGER = new PermissionNode<Boolean>("customnpcs", "edit.villager", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	public static final PermissionNode<Boolean> EDIT_BLOCKS = new PermissionNode<Boolean>("customnpcs", "edit.blocks", PermissionTypes.BOOLEAN, (player, id, context) -> true);
	
	public static final PermissionNode<Boolean> SOULSTONE_ALL = new PermissionNode<Boolean>("customnpcs", "soulstone.all", PermissionTypes.BOOLEAN, (player, id, context) -> false);

	public static final PermissionNode<Boolean> SCENES = new PermissionNode<Boolean>("customnpcs", "scenes", PermissionTypes.BOOLEAN, (player, id, context) -> true);

//	@SubscribeEvent
//	public void registerNodes(PermissionGatherEvent.Nodes event){
//		if(!CustomNpcs.DisablePermissions){
//			List<PermissionNode<Boolean>> nodes = Arrays.asList(
//					NPC_DELETE, NPC_CREATE, NPC_GUI, NPC_FREEZE, NPC_RESET, NPC_AI, NPC_ADVANCED, NPC_DISPLAY, NPC_INVENTORY, NPC_STATS, NPC_CLONE,
//					GLOBAL_LINKED, GLOBAL_PLAYERDATA, GLOBAL_BANK, GLOBAL_DIALOG, GLOBAL_QUEST, GLOBAL_FACTION, GLOBAL_TRANSPORT, GLOBAL_RECIPE, GLOBAL_NATURALSPAWN,
//					SPAWNER_MOB, SPAWNER_CREATE,
//					TOOL_MOUNTER, TOOL_PATHER, TOOL_SCRIPTER, TOOL_NBTBOOK,
//					EDIT_VILLAGER, EDIT_BLOCKS,
//					SOULSTONE_ALL, SCENES);
//
//			LogManager.getLogger(CustomNpcs.class).info("CustomNPC PermissionNode<Boolean>s available:");
//			Collections.sort(nodes, (o1, o2) -> o1.getNodeName().compareToIgnoreCase(o2.getNodeName()));
//			for(PermissionNode<Boolean> p : nodes){
//				event.addNodes(p);
//				LogManager.getLogger(CustomNpcs.class).info(p.getNodeName());
//			}
//		}
//	}
	
	public static boolean hasPermission(ServerPlayer player, PermissionNode<Boolean> permission){
		return player.hasPermissions(4);
//		if(CustomNpcs.OpsOnly){
//			return player.hasPermissions(4);
//		}
//		if(CustomNpcs.DisablePermissions) {
//			return permission.getDefaultResolver().resolve(player, player.getUUID());
//		}
//		return true; //PermissionAPI.getPermission(player, permission);
	}
}
