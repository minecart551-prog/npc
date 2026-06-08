package noppes.npcs.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import noppes.npcs.CustomEntities;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

public class CmdNoppes {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("noppes").requires((p_198816_0_) -> {
                            return p_198816_0_.hasPermission(2);
                        })
                .then(CmdClone.register())
                .then(CmdConfig.register())
                .then(CmdDialog.register())
                .then(CmdFaction.register())
                .then(CmdMark.register())
                .then(CmdNPC.register())
                .then(CmdQuest.register())
                .then(CmdScene.register())
                .then(CmdSchematics.register())
                .then(CmdScript.register())
                .then(CmdSlay.register())
        );


    }

    public static List<EntityNPCInterface> getNpcsByName(ServerLevel level, String name) {
        return (List<EntityNPCInterface>)(List<?>)level.getEntities(CustomEntities.entityCustomNpc,  (npc) -> ((EntityNPCInterface)npc).display.getName().equalsIgnoreCase(name));
    }

    public static <T extends Entity> List<T> getEntities(EntityType<T> type, ServerLevel level) {
        return (List<T>)level.getEntities(type, (entity) -> true);
    }
}
