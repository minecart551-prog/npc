package noppes.npcs;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import nikedemos.markovnames.generators.MarkovGenerator;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.command.CmdNoppes;
import noppes.npcs.command.CmdSchematics;
import noppes.npcs.config.ConfigLoader;
import noppes.npcs.config.ConfigProp;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.ScoreBoardMixin;
import noppes.npcs.packets.Packets;
import noppes.npcs.shared.common.util.LogWriter;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class CustomNpcs implements ModInitializer, CommandRegistrationCallback, ServerLifecycleEvents.ServerStarting, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopping {

    public static final String MODID = "customnpcs";
    public static final String VERSION = "1.20.1";

	@ConfigProp(info = "Whether scripting is enabled or not")
    public static boolean EnableScripting = true;
    
    @ConfigProp(info = "Arguments given to the Nashorn scripting library")
    public static String NashorArguments = "-strict";

	@ConfigProp(info = "Disable Chat Bubbles")
    public static boolean EnableChatBubbles = true;

    @ConfigProp(info = "Navigation search range for NPCs. Not recommended to increase if you have a slow pc or on a server")
    public static int NpcNavRange = 32;

    @ConfigProp(info = "Limit too how many npcs can be in one chunk for natural spawning")
    public static int NpcNaturalSpawningChunkLimit = 4;

    @ConfigProp(info = "Set to true if you want the dialog command option to be able to use op commands like tp etc")
    public static boolean NpcUseOpCommands = false;

    @ConfigProp(info = "If set to true only opped people can use the /noppes command")
    public static boolean NoppesCommandOpOnly = false;

    @ConfigProp
    public static boolean InventoryGuiEnabled = true;

    //@ConfigProp 1.13 might have broken this needs testing
    public static boolean FixUpdateFromPre_1_12 = false;
    
    //@ConfigProp(info = "If you are running sponge and you want to disable the permissions set this to true")
    public static boolean DisablePermissions = false;

    @ConfigProp
    public static boolean SceneButtonsEnabled = true;

    public static long ticks;


    @ConfigProp(info = "Enables CustomNpcs startup update message")
    public static boolean EnableUpdateChecker = true;

    public static CustomNpcs instance;

    public static boolean FreezeNPCs = false;

    //@ConfigProp(info = "Only ops can create and edit npcs")
    public static boolean OpsOnly = true;
    
    @ConfigProp(info = "Default interact line. Leave empty to not have one")
    public static String DefaultInteractLine = "Hello @p";

    @ConfigProp(info = "Number of chunk loading npcs that can be active at the same time")
    public static int ChuckLoaders = 20;

    public static File Dir;

    @ConfigProp(info = "Enables leaves decay")
    public static boolean LeavesDecayEnabled = true;
    
    @ConfigProp(info = "Enables Vine Growth")
    public static boolean VineGrowthEnabled = true;

    @ConfigProp(info = "Enables Ice Melting")
    public static boolean IceMeltsEnabled = true;
    
    @ConfigProp(info = "Normal players can use soulstone on animals")
	public static boolean SoulStoneAnimals = true;
    
    @ConfigProp(info = "Normal players can use soulstone on all npcs")
	public static boolean SoulStoneNPCs = false;

	@ConfigProp(info="Type 0 = Normal, Type 1 = Solid")
	public static int HeadWearType = 1;

	@ConfigProp(info="When set to Minecraft it will use minecrafts font, when Default it will use OpenSans. Can only use fonts installed on your PC")
	public static String FontType = "Default";

	@ConfigProp(info="Font size for custom fonts (doesn't work with minecrafts font)")
	public static int FontSize = 18;

    @ConfigProp(info="On some servers or with certain plugins, it doesnt work, so you can disable it here")
    public static boolean EnableInvisibleNpcs = true;

	@ConfigProp
	public static boolean NpcSpeachTriggersChatEvent = false;
    
    public static ConfigLoader Config;
    
    public static boolean VerboseDebug = false;
    
    public static MinecraftServer Server;

    public static CommonProxy proxy = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? new ClientProxy() : new CommonProxy();

    static {
        File dir = new File(Paths.get("config").toFile(), "..");
        Dir = new File(dir, "customnpcs");
        if(!Dir.exists()){
            Dir.mkdir();
        }
    }

    public CustomNpcs() {

    }


    public static File getLevelSaveDirectory() {
        return getLevelSaveDirectory(null);
    }

    public static File getLevelSaveDirectory(String s) {
    	try{
	        File dir = new File(".");
	        if (Server != null) {
	        	if(!Server.isDedicatedServer()) {
                    dir = new File(Minecraft.getInstance().gameDirectory, "saves");
                }
	        	dir = Server.getWorldPath(new LevelResource("customnpcs")).toFile();

	        }
            if(s != null){
            	dir = new File(dir, s);
            }
            if (!dir.exists()) {
            	dir.mkdirs();
            }
            return dir;
	        
    	}
    	catch(Exception e){
    		LogWriter.error("Error getting worldsave", e);
    	}
        return null;
    }

    public static <T, E> void setPrivateValue(Class<? super T> classToAccess, T instance, E value, int fieldIndex)
    {
        try
        {
            Field f = classToAccess.getDeclaredFields()[fieldIndex];
            f.setAccessible(true);
            f.set(instance, value);
        }
        catch (IllegalAccessException e)
        {
            LogWriter.error("setPrivateValue error", e);
        }
    }

    @Override
    public void onInitialize() {
        instance = this;
        File dir = new File(Paths.get("config").toFile(), "..");

        Config = new ConfigLoader(this.getClass(), new File(dir, "config"), "CustomNpcs");
        Config.loadConfig();


        if (NpcNavRange < 16) {
            NpcNavRange = 16;
        }
        CustomBlocks.registerBlocks();
        CustomItems.registerItems();
        CustomTabs.registerCreativeTab();
        CustomEntities.registerEntities();
        CustomEntities.attribute();
        CustomContainer.registerContainers();

        Packets.register();
        ServerLifecycleEvents.SERVER_STARTING.register(this);
        ServerLifecycleEvents.SERVER_STARTED.register(this);
        ServerLifecycleEvents.SERVER_STOPPING.register(this);
        ServerTickEvents.END_SERVER_TICK.register(new SkinEventHandler());
        ServerPlayConnectionEvents.JOIN.register(new SkinEventHandler());
        UseEntityCallback.EVENT.register(new ServerEventsHandler());
        ServerLivingEntityEvents.AFTER_DEATH.register(new ServerEventsHandler());
        ServerTickEvents.START_SERVER_TICK.register(new ServerTickHandler());
        ServerPlayConnectionEvents.JOIN.register(new ServerTickHandler());
        CommandRegistrationCallback.EVENT.register(this);
        //MinecraftForge.EVENT_BUS.register(new CustomEntities());

        //ForgeChunkMap.setForcedChunkLoadingCallback(this, new ChunkController());
        proxy.load();

        PixelmonHelper.load();
        ScriptController controller = new ScriptController();
        if(EnableScripting && controller.languages.size() > 0) {
            ServerTickEvents.START_SERVER_TICK.register(new ScriptPlayerEventHandler());
            AttackBlockCallback.EVENT.register(new ScriptPlayerEventHandler());
            UseBlockCallback.EVENT.register(new ScriptPlayerEventHandler());
            UseEntityCallback.EVENT.register(new ScriptPlayerEventHandler());
            UseItemCallback.EVENT.register(new ScriptPlayerEventHandler());
            PlayerBlockBreakEvents.BEFORE.register(new ScriptPlayerEventHandler());
            ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(new ScriptPlayerEventHandler());
            ServerLivingEntityEvents.ALLOW_DAMAGE.register(new ScriptPlayerEventHandler());
            ServerLivingEntityEvents.ALLOW_DEATH.register(new ScriptPlayerEventHandler());
            ServerPlayConnectionEvents.JOIN.register(new ScriptPlayerEventHandler());
            ServerPlayConnectionEvents.DISCONNECT.register(new ScriptPlayerEventHandler());
        }
        setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.MAX_HEALTH, Double.MAX_VALUE, 1);

        //dont remember why I changed this, in 1.13 can only be set using config it seems like
        //ForgeConfig.SERVER.fullBoundingBoxLadders = true;
        //ForgeModContainer.fullBoundingBoxLadders = true;

        new RecipeController();

        //ScreenManager.registerFactory();
        //ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> ClientProxy::openGui);

        proxy.postload();
        CustomItems.registerDispenser();
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        CmdNoppes.register(dispatcher);
    }



    @Override
    public void onServerStarted(MinecraftServer server) {

        EntityNPCInterface.ChatEventPlayer = FakePlayer.get(server.getLevel(Level.OVERWORLD), EntityNPCInterface.ChatEventProfile);
        EntityNPCInterface.CommandPlayer = FakePlayer.get(server.getLevel(Level.OVERWORLD), EntityNPCInterface.CommandProfile);
        EntityNPCInterface.GenericPlayer = FakePlayer.get(server.getLevel(Level.OVERWORLD), EntityNPCInterface.GenericProfile);


        for(ServerLevel level : Server.getAllLevels()) {
            ServerScoreboard board = level.getScoreboard();
            board.addDirtyListener(() ->
            {
                for(String objective : Availability.scores) {
                    Objective so = board.getObjective(objective);
                    if(so != null) {
                        for(ServerPlayer player : Server.getPlayerList().getPlayers()) {
                            if(!board.hasPlayerScore(player.getScoreboardName(), so) && board.getObjectiveDisplaySlotCount(so) == 0) {
                                player.connection.send(new ClientboundSetObjectivePacket(so, 0));
                            }
                            ScoreBoardMixin mixin = (ScoreBoardMixin) board;

                            Map<Objective, Score> map = mixin.getScores().computeIfAbsent(player.getScoreboardName(), (p_197898_0_) -> Maps.newHashMap());
                            Score sco = map.computeIfAbsent(so, (ob) ->  new Score(board, ob, player.getScoreboardName()));

                            player.connection.send(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, so.getName(), sco.getOwner(), sco.getScore()));
                        }
                    }
                }
            });
            board.addDirtyListener(() -> {
                List<ServerPlayer> players = Server.getPlayerList().getPlayers();
                for(ServerPlayer playerMP : players){
                    VisibilityController.instance.onUpdate(playerMP);
                }
            });

            // Restore any cached NPCs from disk for this dimension
            NaturalSpawnCache.instance.loadAndRestore(level);
        }

        RecipeController.instance.load();
        new BankController();
        ServerCloneController.Instance = new ServerCloneController();
        DialogController.instance.load();
        QuestController.instance.load();
        ScriptController.HasStart = true;
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        Availability.scores.clear();
        Server = server;
        MarkovGenerator.load();
        ChunkController.instance.clear();
        FactionController.instance.load();
        new PlayerDataController();
        new TransportController();
        new GlobalDataController();
        new SpawnController();
        new LinkedNpcController();
        new MassBlockController();
        VisibilityController.instance = new VisibilityController();
        ScriptController.Instance.loadCategories();
        ScriptController.Instance.loadStoredData();
        ScriptController.Instance.loadPlayerScripts();
        ScriptController.Instance.loadForgeScripts();
        ScriptController.HasStart = false;

        WrapperNpcAPI.clearCache();

        CmdSchematics.names.clear();
        CmdSchematics.names.addAll(SchematicController.Instance.list());
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        // Save all cached NPC data to disk so it survives server restart
        NaturalSpawnCache.instance.save();
    }
}