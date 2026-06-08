package noppes.npcs.controllers.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.ICompatibilty;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.QuestType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.db.DatabaseColumn;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketQuestCompletion;
import noppes.npcs.quests.QuestDialog;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.quests.QuestKill;
import noppes.npcs.quests.QuestLocation;
import noppes.npcs.quests.QuestManual;



public class Quest implements ICompatibilty, IQuest {
	public int version = VersionCompatibility.ModRev;

	@DatabaseColumn(name = "id", type = DatabaseColumn.Type.INT)
	public int id = -1;

	@DatabaseColumn(name = "title", type = DatabaseColumn.Type.VARCHAR)
	public String title = "default";

	@DatabaseColumn(name = "type", type = DatabaseColumn.Type.SMALLINT)
	public int type = QuestType.ITEM;

	@DatabaseColumn(name = "repeat_type", type = DatabaseColumn.Type.ENUM)
	public EnumQuestRepeat repeat = EnumQuestRepeat.NONE;

	@DatabaseColumn(name = "completion_type", type = DatabaseColumn.Type.ENUM)
	public EnumQuestCompletion completion = EnumQuestCompletion.Npc;

	@DatabaseColumn(name = "category", type = DatabaseColumn.Type.VARCHAR)
	public String categoryName;
	public QuestCategory category;

	@DatabaseColumn(name = "log_text", type = DatabaseColumn.Type.TEXT)
	public String logText = "";
	@DatabaseColumn(name = "complete_text", type = DatabaseColumn.Type.TEXT)
	public String completeText = "";
	@DatabaseColumn(name = "complete_npc", type = DatabaseColumn.Type.VARCHAR)
	public String completerNpc = "";
	@DatabaseColumn(name = "next_quest", type = DatabaseColumn.Type.INT)
	public int nextQuestid = -1;

	@DatabaseColumn(name = "command", type = DatabaseColumn.Type.TEXT)
	public String command = "";

	@DatabaseColumn(name = "mail_data", type = DatabaseColumn.Type.JSON)
	public CompoundTag mailData = new CompoundTag();
	public PlayerMail mail = new PlayerMail();

	@DatabaseColumn(name = "quest_data", type = DatabaseColumn.Type.JSON)
	public CompoundTag questData = new CompoundTag();
	public QuestInterface questInterface = new QuestItem();

	@DatabaseColumn(name = "reward_exp", type = DatabaseColumn.Type.INT)
	public int rewardExp = 0;

	@DatabaseColumn(name = "reward_items", type = DatabaseColumn.Type.JSON)
	public CompoundTag rewardItemsData = new CompoundTag();
	public NpcMiscInventory rewardItems = new NpcMiscInventory(9);

	@DatabaseColumn(name = "reward_randomized", type = DatabaseColumn.Type.BOOLEAN)
	public boolean randomReward = false;

	@DatabaseColumn(name = "faction_options", type = DatabaseColumn.Type.JSON)
	public FactionOptions factionOptions = new FactionOptions();
	
	public Quest(QuestCategory category){
		this.category = category;
	}

	public void readNBT(CompoundTag compound) {
		id = compound.getInt("Id");
		readNBTPartial(compound);
	}
	public void readNBTPartial(CompoundTag compound) {
    	version = compound.getInt("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);
		
		setType(compound.getInt("Type"));
		title = compound.getString("Title");
		logText = compound.getString("Text");
		completeText = compound.getString("CompleteText");
		completerNpc = compound.getString("CompleterNpc");
		command = compound.getString("QuestCommand");
		nextQuestid = compound.getInt("NextQuestId");

		randomReward = compound.getBoolean("RandomReward");
		rewardExp = compound.getInt("RewardExp");
		rewardItems.setFromNBT(compound.getCompound("Rewards"));
		
		completion = EnumQuestCompletion.values()[compound.getInt("QuestCompletion")];
		repeat = EnumQuestRepeat.values()[compound.getInt("QuestRepeat")];
		
		questInterface.readAdditionalSaveData(compound);
		
		factionOptions.load(compound.getCompound("QuestFactionPoints"));
		
		mail.readNBT(compound.getCompound("QuestMail"));
	}

	@Override
	public void setType(int questType) {
		type = questType;
		if(type == QuestType.ITEM)
			questInterface = new QuestItem();
		else if(type == QuestType.DIALOG)
			questInterface = new QuestDialog();
		else if(type == QuestType.KILL || type == QuestType.AREA_KILL)
			questInterface = new QuestKill();
		else if(type == QuestType.LOCATION)
			questInterface = new QuestLocation();
		else if(type == QuestType.MANUAL)
			questInterface = new QuestManual();
		
		if(questInterface != null)
			questInterface.questId = id;
	}

	public CompoundTag save(CompoundTag compound) {
		compound.putInt("Id", id);
		return writeToNBTPartial(compound);
	}
	public CompoundTag writeToNBTPartial(CompoundTag compound) {
		compound.putInt("ModRev", version);
		compound.putInt("Type", type);
		compound.putString("Title", title);
		compound.putString("Text", logText);
		compound.putString("CompleteText", completeText);
		compound.putString("CompleterNpc", completerNpc);
		compound.putInt("NextQuestId", nextQuestid);
		compound.putInt("RewardExp", rewardExp);
		compound.put("Rewards", rewardItems.getToNBT());
		compound.putString("QuestCommand", command);
		compound.putBoolean("RandomReward", randomReward);

		compound.putInt("QuestCompletion", completion.ordinal());
		compound.putInt("QuestRepeat", repeat.ordinal());
		
		this.questInterface.addAdditionalSaveData(compound);
		compound.put("QuestFactionPoints", factionOptions.save(new CompoundTag()));
		compound.put("QuestMail", mail.writeNBT());
		
		return compound;
	}
	
	public boolean hasNewQuest()
	{
		return getNextQuest() != null;
	}
	public Quest getNextQuest()
	{
		return QuestController.instance == null?null:QuestController.instance.quests.get(nextQuestid);
	}

	public boolean complete(Player player, QuestData data) {
		if(completion == EnumQuestCompletion.Instant){
			Packets.send((ServerPlayer)player, new PacketQuestCompletion(data.quest.id));
			return true;
		}
		return false;
	}
	public Quest copy(){
		Quest quest = new Quest(category);
		quest.readNBT(this.save(new CompoundTag()));
		return quest;
	}
	
	@Override
	public int getVersion() {
		return version;
	}
	@Override
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return title;
	}
	
	@Override
	public int getType() {
		return type;
	}
	
	@Override
	public IQuestCategory getCategory() {
		return category;
	}
	
	@Override
	public void save() {
		QuestController.instance.saveQuest(category, this);
	}
	
	@Override
	public void setName(String name) {
		this.title = name;
	}
	
	@Override
	public String getLogText() {
		return logText;
	}
	
	@Override
	public void setLogText(String text) {
		this.logText = text;
	}
	
	@Override
	public String getCompleteText() {
		return completeText;
	}
	
	@Override
	public void setCompleteText(String text) {
		this.completeText = text;
	}
	
	@Override
	public void setNextQuest(IQuest quest) {
		if(quest == null){
			nextQuestid = -1;
		}
		else{
			if(quest.getId() < 0)
				throw new CustomNPCsException("Quest id is lower than 0");
			nextQuestid = quest.getId();
		}
	}
	@Override
	public String getNpcName() {
		return this.completerNpc;
	}
	@Override
	public void setNpcName(String name) {
		this.completerNpc = name;
	}

	@Override
	public IQuestObjective[] getObjectives(IPlayer player) {
		if(!player.hasActiveQuest(id)) {
			throw new CustomNPCsException("Player doesnt have this quest active.");
		}
		return questInterface.getObjectives(player.getMCEntity());
	}

	@Override
	public boolean getIsRepeatable() {
		return repeat != EnumQuestRepeat.NONE;
	}

	@Override
	public IContainer getRewards() {
		return NpcAPI.Instance().getIContainer(rewardItems);
	}
}
