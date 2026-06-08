package noppes.npcs.client.gui.script;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketScriptGet;
import noppes.npcs.packets.server.SPacketScriptSave;

public class GuiScriptBlock extends GuiScriptInterface {
	
	private TileScripted script;
		
	public GuiScriptBlock(BlockPos pos) {
		handler = script = (TileScripted) player.level().getBlockEntity(pos);
		Packets.sendServer(new SPacketScriptGet(1));
	}

	@Override
	public void setGuiData(CompoundTag compound) {
		script.setNBT(compound);
		super.setGuiData(compound);
	}

	@Override
	public void save() {
		super.save();
        Packets.sendServer(new SPacketScriptSave(1, script.getNBT(new CompoundTag())));
	}
}
