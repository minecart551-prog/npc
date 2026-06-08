package noppes.npcs.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.Util;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketAchievement;
import noppes.npcs.packets.client.PacketChat;

import java.util.Collection;

public class CmdQuest{

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("quest");

        command.then(Commands.literal("start").then(Commands.argument("players", EntityArgument.players()).requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.argument("quest", IntegerArgumentType.integer(0)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                QuestData questdata = new QuestData(quest);
                data.questData.activeQuests.put(quest.id, questdata);
                data.save(true);
                Packets.send(player, new PacketAchievement(Component.translatable("quest.newquest"), Component.translatable(quest.title), 2));
                Packets.send(player, new PacketChat(Component.translatable("quest.newquest").append(":").append(Component.translatable(quest.title))));
            }
            return 1;
        }))));

        command.then(Commands.literal("finish").then(Commands.argument("players", EntityArgument.players()).requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.argument("quest", IntegerArgumentType.integer(0)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                data.questData.finishedQuests.put(quest.id, System.currentTimeMillis());
                data.save(true);
            }
            return 1;
        }))));

        command.then(Commands.literal("stop").then(Commands.argument("players", EntityArgument.players()).requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.argument("quest", IntegerArgumentType.integer(0)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                data.questData.activeQuests.remove(quest.id);
                data.save(true);
            }
            return 1;
        }))));

        command.then(Commands.literal("remove").then(Commands.argument("players", EntityArgument.players()).requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.argument("quest", IntegerArgumentType.integer(0)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                data.questData.activeQuests.remove(quest.id);
                data.questData.finishedQuests.remove(quest.id);
                data.save(true);
            }
            return 1;
        }))));

        command.then(Commands.literal("objective").then(Commands.argument("players", EntityArgument.players()).requires((source) -> {
            return source.hasPermission(2);
        }).then(Commands.argument("quest", IntegerArgumentType.integer(0)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                if(data.questData.activeQuests.containsKey(quest.id)) {
                    IQuestObjective[] objectives = quest.questInterface.getObjectives(player);
                    for(IQuestObjective ob : objectives) {
                        player.sendSystemMessage(ob.getMCText());
                    }
                }
            }
            return 1;
        }).then(Commands.argument("objective", IntegerArgumentType.integer(0, 3)).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }

            int objective = IntegerArgumentType.getInteger(context, "objective");
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                if(data.questData.activeQuests.containsKey(quest.id)) {
                    IQuestObjective[] objectives = quest.questInterface.getObjectives(player);
                    if(objective < objectives.length){
                        player.sendSystemMessage(objectives[objective].getMCText());
                    }
                }
            }

            return 1;
        }).then(Commands.argument("value", IntegerArgumentType.integer()).executes(context -> {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
            if(players.isEmpty())
                return 1;

            Quest quest = QuestController.instance.quests.get(IntegerArgumentType.getInteger(context, "quest"));
            if (quest == null){
                throw new CommandRuntimeException(Component.literal("Unknown QuestID"));
            }

            int objective = IntegerArgumentType.getInteger(context, "objective");
            int value = IntegerArgumentType.getInteger(context, "value");
            for(ServerPlayer player : players){
                PlayerData data = PlayerData.get(player);
                if(data.questData.activeQuests.containsKey(quest.id)) {
                    IQuestObjective[] objectives = quest.questInterface.getObjectives(player);
                    if(objective < objectives.length){
                        objectives[objective].setProgress(value);
                    }
                }
            }

            return 1;
        }))))));

        command.requires((source) -> {
            return source.hasPermission(4);
        }).then(Commands.literal("reload").executes(context -> {
            new QuestController().load();
            SyncController.syncAllQuests();
            return 1;
        }));

        return command;
    }
}













