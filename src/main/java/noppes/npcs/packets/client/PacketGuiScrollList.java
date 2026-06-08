package noppes.npcs.packets.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.shared.client.gui.listeners.IScrollData;
import noppes.npcs.shared.common.PacketBasic;

import java.util.Vector;


public class PacketGuiScrollList extends PacketBasic {
	private final Vector<String> data;

    public PacketGuiScrollList(Vector<String> data) {
    	this.data = data;
    }

    public static void encode(PacketGuiScrollList msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.data.size());
        for(String s : msg.data){
            buf.writeUtf(s);
        }
    }

    public static PacketGuiScrollList decode(FriendlyByteBuf buf) {
        Vector<String> data = new Vector<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++){
            data.add(buf.readUtf(32767));
        }
        return new PacketGuiScrollList(data);
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void handle() {
        Screen gui = Minecraft.getInstance().screen;
        if(gui instanceof GuiNPCInterface && ((GuiNPCInterface)gui).hasSubGui())
            gui = ((GuiNPCInterface)gui).getSubGui();
        if(gui == null || !(gui instanceof IScrollData))
            return;
        ((IScrollData)gui).setData(data,null);
	}
}