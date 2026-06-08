package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.controllers.ClientSkinController;
import noppes.npcs.controllers.data.PlayerSkinData;
import noppes.npcs.shared.common.PacketBasic;

public class PacketSyncSkin extends PacketBasic {
    private final String name;
    private final PlayerSkinData skinData;

    public PacketSyncSkin(String name, PlayerSkinData skinData) {
        this.name = name;
        this.skinData = skinData;
    }

    public static void encode(PacketSyncSkin msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name);
        CompoundTag tag = new CompoundTag();
        msg.skinData.saveNBTData(tag);
        buf.writeNbt(tag);
    }

    public static PacketSyncSkin decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        CompoundTag tag = buf.readNbt();
        PlayerSkinData skinData = new PlayerSkinData();
        skinData.loadNBTData(tag);
        return new PacketSyncSkin(name, skinData);
    }

    @Environment(EnvType.CLIENT)
    protected void handle() {
        ClientSkinController.addSkinForPlayer(name, skinData);
    }
}
