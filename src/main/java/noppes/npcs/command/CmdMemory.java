package noppes.npcs.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Diagnostic command: {@code /noppes memory}
 *
 * Prints the sizes of the static caches that used to grow without bound (one entry per
 * entity id ever minted) plus the number of live script engines, so the effect of a chunk
 * load/unload cycle can be watched directly in game.
 */
public class CmdMemory {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("memory").requires((source) -> {
			return source.hasPermission(2);
		});

		command.executes(context -> {
			CommandSourceStack source = context.getSource();
			MinecraftServer server = source.getServer();

			int npcs = 0;
			for(ServerLevel level : server.getAllLevels()){
				for(net.minecraft.world.entity.Entity entity : level.getAllEntities()){
					if(entity instanceof EntityNPCInterface)
						npcs++;
				}
			}

			Runtime runtime = Runtime.getRuntime();
			long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
			long max = runtime.maxMemory() / (1024 * 1024);

			final int npcCount = npcs;
			final long usedMb = used;
			final long maxMb = max;
			source.sendSuccess(() -> Component.literal(String.format(
					"CustomNPCs memory: %d MarkData, %d tracked npcs, %d player data, %d live script engines, %d npcs loaded, heap %d/%d MB",
					MarkData.size(),
					VisibilityController.instance.size(),
					PlayerData.size(),
					ScriptContainer.getActiveEngineCount(),
					npcCount,
					usedMb,
					maxMb)), false);
			return 1;
		});

		return command;
	}
}
