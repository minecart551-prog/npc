package noppes.npcs.api.wrapper;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import nikedemos.markovnames.generators.MarkovGenerator;
import noppes.npcs.CustomEntities;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.*;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.*;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.overlay.IOverlay;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.EntityIMixin;
import noppes.npcs.shared.common.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.NBTJsonUtil.JsonException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WrapperNpcAPI extends NpcAPI{
	private static final Map<DimensionType, WorldWrapper> worldCache = new LRUHashMap<DimensionType, WorldWrapper>(10);
	
	private static NpcAPI instance = null;
	
	public static void clearCache(){
		worldCache.clear();
		BlockWrapper.clearCache();
	}

	@Override
	public IEntity getIEntity(Entity entity) {
		if(entity == null || entity.level().isClientSide)
			return null;
		if(entity instanceof EntityNPCInterface)
			return ((EntityNPCInterface) entity).wrappedNPC;
		else{
			return WrapperEntityData.get(entity);
		}
	}

	@Override
	public ICustomNpc createNPC(Level level){
		if(level.isClientSide)
			return null;
		EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, level);
		return npc.wrappedNPC;
	}

	public void registerPermissionNode(String permission, int defaultType) {
		throw new CustomNPCsException("registerPermissionNode is nolonger supported");
	}

	@Override
	public boolean hasPermissionNode(String permission) {
//		for(PermissionNode<?> node : PermissionAPI.getRegisteredNodes()){ TODO
//			if(node.getNodeName().equals(permission)){
//				return true;
//			}
//		}
		return false;
	}

	@Override
	public ICustomNpc spawnNPC(Level level, int x, int y, int z){
		if(level.isClientSide)
			return null;
		EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, level);
        npc.absMoveTo(x + 0.5, y, z + 0.5, 0, 0);
        npc.ais.setStartPos(x, y, z);
        npc.setHealth(npc.getMaxHealth());
		level.addFreshEntity(npc);
		return npc.wrappedNPC;
	}
	
	
	public static NpcAPI Instance(){
		if(instance == null)
			instance = new WrapperNpcAPI();
		return instance;
	}

	@Override
	public IBlock getIBlock(Level level, BlockPos pos) {
		return BlockWrapper.createNew(level, pos, level.getBlockState(pos));
	}

	@Override
	public IItemStack getIItemStack(ItemStack itemstack) {
		if(itemstack == null || itemstack.isEmpty())
			return ItemStackWrapper.AIR;
		if(itemstack.getItem()== CustomItems.scripted_item){
			return new ItemScriptedWrapper(itemstack);
		}
		return new ItemStackWrapper(itemstack);
	}

	@Override
	public IWorld getIWorld(ServerLevel level) {
		WorldWrapper w = worldCache.get(level.dimensionType());
		if(w != null) {
			w.level = level;
			return w;
		}
		worldCache.put(level.dimensionType(), w = WorldWrapper.createNew(level));
		return w;
	}

	@Override
	public IWorld getIWorld(DimensionType dimension) {
		for(ServerLevel level : CustomNpcs.Server.getAllLevels()){
			if(level.dimensionType() == dimension)
				return getIWorld(level);
		}
		throw new CustomNPCsException("Unknown dimension: " + dimension);
	}

	@Override
	public IWorld getIWorld(String dimension) {
		ResourceLocation loc = new ResourceLocation(dimension);
		for(ServerLevel level : CustomNpcs.Server.getAllLevels()){
			if(level.dimension().location().equals(loc))
				return getIWorld(level);
		}
		throw new CustomNPCsException("Unknown dimension: " + loc);
	}

	@Override
	public IContainer getIContainer(AbstractContainerMenu inventory) {
		return new ContainerWrapper(inventory);
	}

	@Override
	public IContainer getIContainer(Container container) {
		if(container instanceof ContainerNpcInterface) {
			return ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface)container);
		}
		return new ContainerWrapper(container);
	}

	@Override
	public IFactionHandler getFactions() {
		checkLevel();
		return FactionController.instance;
	}

	private void checkLevel() {
		if(CustomNpcs.Server == null || CustomNpcs.Server.isStopped())
			throw new CustomNPCsException("No world is loaded right now");
	}

	@Override
	public IRecipeHandler getRecipes() {
		checkLevel();
		return RecipeController.instance;
	}

	@Override
	public IQuestHandler getQuests() {
		checkLevel();
		return QuestController.instance;
	}

	@Override
	public IWorld[] getIWorlds(){
		checkLevel();
		List<IWorld> list = new ArrayList<IWorld>();
		for(ServerLevel level : CustomNpcs.Server.getAllLevels()){
			list.add(getIWorld(level));
		}
		return list.toArray(new IWorld[list.size()]);
	}

	@Override
	public IPos getIPos(double x, double y, double z) {
		return new BlockPosWrapper(new BlockPos((int) x, (int) y, (int) z));
	}

	@Override
	public File getGlobalDir() {
		return CustomNpcs.Dir;
	}

	@Override
	public File getLevelDir() {
		return CustomNpcs.getLevelSaveDirectory();
	}

	@Override
	public INbt getINbt(CompoundTag compound) {
		if(compound == null)
			return new NBTWrapper(new CompoundTag());
		return new NBTWrapper(compound);
	}

	@Override
	public INbt stringToNbt(String str){
		if(str == null || str.isEmpty())
			throw new CustomNPCsException("Cant cast empty string to nbt");
		try {
			return getINbt(NBTJsonUtil.Convert(str));
		} catch (JsonException e) {
			throw new CustomNPCsException(e, "Failed converting " + str);
		}
	}

	@Override
	public IDamageSource getIDamageSource(DamageSource damagesource) {
		return new DamageSourceWrapper(damagesource);
	}

	@Override
	public IDialogHandler getDialogs() {
		return DialogController.instance;
	}

	@Override
	public ICloneHandler getClones() {
		return ServerCloneController.Instance;
	}

	@Override
	public String executeCommand(IWorld level, String command) {
		FakePlayer player = EntityNPCInterface.CommandPlayer;
		((EntityIMixin)player).setLevel(level.getMCLevel());
		player.setPos(0, 0, 0);
		return NoppesUtilServer.runCommand(level.getMCLevel(), BlockPos.ZERO, "API", command, null, player);
	}

	@Override
	public INbt getRawPlayerData(String uuid) {
		return getINbt(PlayerData.loadPlayerData(uuid));
	}

	@Override
	public IPlayerMail createMail(String sender, String subject) {
		PlayerMail mail = new PlayerMail();
		mail.sender = sender;
		mail.subject = subject;
		return mail;
	}

	@Override
	public ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame, IPlayer player) {
		return new CustomGuiWrapper(player, id, width, height, pauseGame);
	}

	@Override
	public IOverlay createOverlay(final int id) {
		return new OverlayWrapper(id);
	}

	@Override
	public String getRandomName(int dictionary, int gender) {
		return MarkovGenerator.fetch(dictionary, gender);
	}
}
