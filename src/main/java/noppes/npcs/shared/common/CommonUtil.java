package noppes.npcs.shared.common;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import noppes.npcs.shared.common.util.LogWriter;

import java.util.Iterator;

public class CommonUtil {

    public static void NotifyOPs(MinecraftServer server, String message, Object... obs){
        NotifyOPs(server, Component.translatable(message, obs));
    }

    public static void NotifyOPs(MinecraftServer server, Component message){
        MutableComponent chatcomponenttranslation = Component.literal("").append(message).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

        Iterator iterator = server.getPlayerList().getPlayers().iterator();

        while (iterator.hasNext()){
            Player entityplayer = (Player)iterator.next();

            if (entityplayer.shouldInformAdmins() && isOp(entityplayer)){
                entityplayer.sendSystemMessage(chatcomponenttranslation);
            }
        }

        if (server.getLevel(Level.OVERWORLD).getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)){
            LogWriter.info(chatcomponenttranslation.getString());
        }
    }
    public static boolean isOp(Player player) {
        return player.getServer().getPlayerList().isOp(player.getGameProfile());
    }
}
