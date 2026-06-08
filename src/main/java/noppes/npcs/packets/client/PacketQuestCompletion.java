package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.player.GuiQuestCompletion;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.shared.common.PacketBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketQuestCompletionCheck;

public class PacketQuestCompletion extends PacketBasic {
	private final int id;

    public PacketQuestCompletion(int id) {
    	this.id = id;
    }

    public static void encode(PacketQuestCompletion msg, FriendlyByteBuf buf) {
    	buf.writeInt(msg.id);
    }

    public static PacketQuestCompletion decode(FriendlyByteBuf buf) {
        return new PacketQuestCompletion(buf.readInt());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        IQuest quest = QuestController.instance.get(id);
        if (!quest.getCompleteText().isEmpty())
            NoppesUtil.openGUI(player, new GuiQuestCompletion(quest));
        else
            Packets.sendServer(new SPacketQuestCompletionCheck(id));
	}
}