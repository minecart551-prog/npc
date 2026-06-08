package noppes.npcs.command;

import java.util.Map.Entry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.entity.data.DataScenes.SceneState;

public class CmdScene {


	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("scene").requires((source) -> {
			return source.hasPermission(2);
		}).then(Commands.literal("time")
				.executes(context -> {
					context.getSource().sendSuccess(()->Component.literal("Active scenes:"), false);
					for(Entry<String, SceneState> entry : DataScenes.StartedScenes.entrySet())
						context.getSource().sendSuccess(()->Component.translatable("Scene %s time is %s", entry.getKey(), entry.getValue().ticks), false);
					return 1;
				}).then(Commands.argument("time", IntegerArgumentType.integer(0)).executes(context -> {
					int ticks = IntegerArgumentType.getInteger(context, "time");
					for(SceneState state : DataScenes.StartedScenes.values())
						state.ticks = ticks;
					return 1;
				}).then(Commands.argument("name", StringArgumentType.string()).executes(context -> {
					String name = StringArgumentType.getString(context, "name");
					SceneState state = DataScenes.StartedScenes.get(name.toLowerCase());
					if(state == null)
						throw new CommandRuntimeException(Component.translatable("Unknown scene name %s", name));
					state.ticks = IntegerArgumentType.getInteger(context, "time");
					context.getSource().sendSuccess(()->Component.translatable("Scene %s set to %s", name, state.ticks), false);
					return 1;
				})))
		).then(Commands.literal("reset")
				.executes(context -> {
					DataScenes.Reset(context.getSource(), null);
					return 1;
				}).then(Commands.argument("name", StringArgumentType.string()).executes(context -> {
					DataScenes.Reset(context.getSource(), StringArgumentType.getString(context, "name"));
					return 1;
				}))
		).then(Commands.literal("start").then(Commands.argument("name", StringArgumentType.string()).executes(context -> {
			DataScenes.Start(context.getSource().getServer(), StringArgumentType.getString(context, "name"));
			return 1;
		})))
		.then(Commands.literal("pause")
				.executes(context -> {
					DataScenes.Pause(context.getSource(), null);
					return 1;
				}).then(Commands.argument("name", StringArgumentType.string()).executes(context -> {
					DataScenes.Pause(context.getSource(), StringArgumentType.getString(context, "name"));
					return 1;
				}))
		)
				;
		return command;
	}
}
