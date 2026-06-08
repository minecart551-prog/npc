package noppes.npcs.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketConfigFont;

import java.util.Set;

public class CmdConfig {


	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("config");

		command.then(Commands.literal("leavesdecay").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("LeavesDecay: " + CustomNpcs.LeavesDecayEnabled), false);
			return 1;
		}).then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context -> {
			CustomNpcs.LeavesDecayEnabled = BoolArgumentType.getBool(context, "boolean");
			CustomNpcs.Config.updateConfig();
			context.getSource().sendSuccess(()->Component.literal("LeavesDecay: " + CustomNpcs.LeavesDecayEnabled), false);
			return 1;
		})));

		command.then(Commands.literal("vineinflateth").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("VineGrowth: " + CustomNpcs.VineGrowthEnabled), false);
			return 1;
		}).then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context -> {
			CustomNpcs.VineGrowthEnabled = BoolArgumentType.getBool(context, "boolean");
			CustomNpcs.Config.updateConfig();
			context.getSource().sendSuccess(()->Component.literal("VineGrowth: " + CustomNpcs.VineGrowthEnabled), false);
			return 1;
		})));

		command.then(Commands.literal("icemelts").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("IceMelts: " + CustomNpcs.IceMeltsEnabled), false);
			return 1;
		}).then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context -> {
			CustomNpcs.IceMeltsEnabled = BoolArgumentType.getBool(context, "boolean");
			CustomNpcs.Config.updateConfig();
			context.getSource().sendSuccess(()->Component.literal("IceMelts: " + CustomNpcs.IceMeltsEnabled), false);
			return 1;
		})));

		command.then(Commands.literal("freezenpcs").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("Frozen NPCs: " + CustomNpcs.FreezeNPCs), false);
			return 1;
		}).then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context -> {
			CustomNpcs.FreezeNPCs = BoolArgumentType.getBool(context, "boolean");
			context.getSource().sendSuccess(()->Component.literal("Frozen NPCs: " + CustomNpcs.FreezeNPCs), false);
			return 1;
		})));

		command.then(Commands.literal("debug").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("Verbose debug is " + CustomNpcs.VerboseDebug), false);
			return 1;
		}).then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context -> {
			CustomNpcs.VerboseDebug = BoolArgumentType.getBool(context, "boolean");
			context.getSource().sendSuccess(()->Component.literal("Verbose debug is now" + CustomNpcs.VerboseDebug), false);
			return 1;
		})));

		command.then(Commands.literal("scripting").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("Scripting is " + CustomNpcs.EnableScripting), false);
			return 1;
		}).then(Commands.argument("boolean", BoolArgumentType.bool()).executes(context -> {
			CustomNpcs.EnableScripting = BoolArgumentType.getBool(context, "boolean");
			CustomNpcs.Config.updateConfig();
			context.getSource().sendSuccess(()->Component.literal("Scripting is now" + CustomNpcs.EnableScripting), false);
			return 1;
		})));

		command.then(Commands.literal("chunkloaders").requires((source) -> {
			return source.hasPermission(4);
		}).executes(context -> {
			context.getSource().sendSuccess(()->Component.literal("ChunkLoaders: " + ChunkController.instance.size() + "/" + CustomNpcs.ChuckLoaders), false);
			return 1;
		}).then(Commands.argument("number", IntegerArgumentType.integer(0)).executes(context -> {
			CustomNpcs.ChuckLoaders = IntegerArgumentType.getInteger(context, "number");
			CustomNpcs.Config.updateConfig();
			context.getSource().sendSuccess(()->Component.literal("Max ChunkLoaders: " + CustomNpcs.ChuckLoaders), false);
			return 1;
		})));

		command.then(Commands.literal("font").requires((source) -> {
			return source.hasPermission(2);
		}).executes(context -> {
			Packets.send(context.getSource().getPlayerOrException(), new PacketConfigFont("", 0));
			return 1;
		}).then(Commands.argument("font", StringArgumentType.string()).executes(context -> {
			Packets.send(context.getSource().getPlayerOrException(), new PacketConfigFont(StringArgumentType.getString(context, "font"), 18));
			return 1;
		}).then(Commands.argument("size", IntegerArgumentType.integer(0)).executes(context -> {
			Packets.send(context.getSource().getPlayerOrException(),
					new PacketConfigFont(StringArgumentType.getString(context, "font"), IntegerArgumentType.getInteger(context, "size")));
			return 1;
		}))));

		return command;
	}
}
