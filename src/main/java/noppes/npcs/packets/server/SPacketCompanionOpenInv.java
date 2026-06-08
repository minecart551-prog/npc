package noppes.npcs.packets.server;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.packets.PacketServerBasic;

public class SPacketCompanionOpenInv extends PacketServerBasic {

    public SPacketCompanionOpenInv() {
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return true;
    }
    
    @Override
    public boolean requiresNpc(){
        return true;
    }

    public static void encode(SPacketCompanionOpenInv msg, FriendlyByteBuf buf) {
    }

    public static SPacketCompanionOpenInv decode(FriendlyByteBuf buf) {
        return new SPacketCompanionOpenInv();
    }

    @Override
    protected void handle() {
        if(npc.role.getType() != RoleType.COMPANION || player != npc.getOwner())
            return;
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionInv, npc);
    }
}