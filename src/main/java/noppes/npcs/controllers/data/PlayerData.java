package noppes.npcs.controllers.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomEntities;
import noppes.npcs.CustomNpcs;
import noppes.npcs.shared.common.util.LogWriter;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
	private static Map<Integer, PlayerData> dataMap = new HashMap<>();
    public BlockPos scriptBlockPos = BlockPos.ZERO;
	
	public PlayerDialogData dialogData = new PlayerDialogData();
	public PlayerBankData bankData = new PlayerBankData();
	public PlayerQuestData questData = new PlayerQuestData();
	public PlayerTransportData transportData = new PlayerTransportData();
	public PlayerFactionData factionData = new PlayerFactionData();
	public PlayerItemGiverData itemgiverData = new PlayerItemGiverData();
	public PlayerMailData mailData = new PlayerMailData();
	public PlayerSkinData skinData = new PlayerSkinData();
	public PlayerScriptData scriptData;

	public CompoundTag scriptStoreddata = new CompoundTag();

	public DataTimers timers = new DataTimers(this);
	
	public EntityNPCInterface editingNpc;
	public CompoundTag cloned;
	
	public Player player;

	public String playername = "";
	public String uuid = "";
	
	private EntityNPCInterface activeCompanion = null;
	public int companionID = 0;
	
	public int playerLevel = 0;
	
	public boolean updateClient = false;

	public int dialogId = -1;
	public ItemStack prevHeldItem = ItemStack.EMPTY;

	public Entity mounted;

	public UUID iAmStealingYourDatas = UUID.randomUUID();

	public void setNBT(CompoundTag data){
		dialogData.loadNBTData(data);
		bankData.loadNBTData(data);
		questData.loadNBTData(data);
		transportData.loadNBTData(data);
		factionData.loadNBTData(data);
		itemgiverData.loadNBTData(data);
		mailData.loadNBTData(data);
		skinData.loadNBTData(data);
		timers.load(data);

		if(player != null){
			playername = player.getName().getString();
			uuid = player.getUUID().toString();
		}
		else{
			playername = data.getString("PlayerName");
			uuid = data.getString("UUID");
		}
		companionID = data.getInt("PlayerCompanionId");
		
		if(data.contains("PlayerCompanion") && !hasCompanion() && player != null){
			EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, player.level());
			npc.readAdditionalSaveData(data.getCompound("PlayerCompanion"));
			npc.setPos(player.getX(), player.getY(), player.getZ());
			if(npc.role.getType() == RoleType.COMPANION){
				((RoleCompanion)npc.role).setSitting(false);
				player.level().addFreshEntity(npc);
				setCompanion(npc);
			}
		}
		scriptStoreddata = data.getCompound("ScriptStoreddata");
	}
	
	public CompoundTag getSyncNBT(){
		CompoundTag compound = new CompoundTag();
		dialogData.saveNBTData(compound);
		questData.saveNBTData(compound);
		factionData.saveNBTData(compound);
		
		return compound;
	}
	
	public CompoundTag getNBT() {
		if(player != null){
			playername = player.getName().getString();
			uuid = player.getUUID().toString();
		}
		CompoundTag compound = new CompoundTag();
		dialogData.saveNBTData(compound);
		bankData.saveNBTData(compound);
		questData.saveNBTData(compound);
		transportData.saveNBTData(compound);
		factionData.saveNBTData(compound);
		itemgiverData.saveNBTData(compound);
		mailData.saveNBTData(compound);
		skinData.saveNBTData(compound);
		timers.save(compound);
		
		compound.putString("PlayerName", playername);
		compound.putString("UUID", uuid);
		compound.putInt("PlayerCompanionId", companionID);
		compound.put("ScriptStoreddata", scriptStoreddata);
		
		if(hasCompanion()){
			CompoundTag nbt = new CompoundTag();
			if(activeCompanion.saveAsPassenger(nbt))
				compound.put("PlayerCompanion", nbt);
		}
		return compound;
	}
	
	public boolean hasCompanion(){
		return activeCompanion != null && !activeCompanion.isRemoved();
	}

	public void setCompanion(EntityNPCInterface npc) {
		if(npc != null && npc.role.getType() != RoleType.COMPANION)//shouldnt happen
			return;
		companionID++;
		activeCompanion = npc;
		if(npc != null)
			((RoleCompanion)npc.role).companionID = companionID;
		save(false);
	}


	public void updateCompanion(Level level) {
		if(!hasCompanion() || level == activeCompanion.level())
			return;
		RoleCompanion role = (RoleCompanion) activeCompanion.role;
		role.owner = player;
		if(!role.isFollowing())
			return;
		CompoundTag nbt = new CompoundTag();
		activeCompanion.saveAsPassenger(nbt);
		activeCompanion.discard();

		EntityCustomNpc npc = new EntityCustomNpc(CustomEntities.entityCustomNpc, level);
		npc.readAdditionalSaveData(nbt);
		npc.setPos(player.getX(), player.getY(), player.getZ());
		setCompanion(npc);
		((RoleCompanion)npc.role).setSitting(false);
		level.addFreshEntity(npc);
	}


	private static final ResourceLocation key = new ResourceLocation("customnpcs", "playerdata");

	public synchronized void save(boolean update) {
		final CompoundTag compound = getNBT();
		final String filename = uuid + ".json";

        CustomNPCsScheduler.runTack(() -> {
            try {
                File saveDir = CustomNpcs.getLevelSaveDirectory("playerdata");
                File file = new File(saveDir, filename + "_new");
                File file1 = new File(saveDir, filename);
                NBTJsonUtil.SaveFile(file, compound);
                if (file1.exists()) {
                    file1.delete();
                }
                file.renameTo(file1);
            } catch (Exception e) {
                LogWriter.except(e);
            }
        });

        if(update)
			updateClient = true;
	}

	public static CompoundTag loadPlayerData(String player){
		File saveDir = CustomNpcs.getLevelSaveDirectory("playerdata");
		String filename = player;
		if(filename.isEmpty())
			filename = "noplayername";
		filename += ".json";
		File file = null;
		try {
	        file = new File(saveDir, filename);
	        if(file.exists()){
		        return NBTJsonUtil.LoadFile(file);
	        }
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
		}

		return new CompoundTag();
	}

	public static PlayerData get(Player player) {
		if(player.level().isClientSide)
			return CustomNpcs.proxy.getPlayerData(player);
		PlayerData data = dataMap.computeIfAbsent(player.getId(), (i)->new PlayerData());
		if(data.player == null){
			data.player = player;
			data.playerLevel = player.experienceLevel;
			data.scriptData = new PlayerScriptData(player);

			CompoundTag compound = loadPlayerData(player.getUUID().toString());
			data.setNBT(compound);
		}
		return data;
	}
}
