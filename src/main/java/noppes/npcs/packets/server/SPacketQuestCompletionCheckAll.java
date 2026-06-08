package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.CustomItems;
import noppes.npcs.controllers.data.*;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketQuestCompletionCheckAll extends PacketServerBasic {

    public SPacketQuestCompletionCheckAll() {

    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }

    public static void encode(SPacketQuestCompletionCheckAll msg, FriendlyByteBuf buf) {

    }

    public static SPacketQuestCompletionCheckAll decode(FriendlyByteBuf buf) {
        return new SPacketQuestCompletionCheckAll();
    }

    @Override
    protected void handle() {
        PlayerQuestData playerdata = PlayerData.get(player).questData;
        playerdata.checkQuestCompletion(player, -1);
    }
}