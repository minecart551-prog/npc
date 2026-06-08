package noppes.npcs.client;

import net.minecraft.network.syncher.SynchedEntityData;

import java.util.List;

public interface ISynchedEntityData {
    List<SynchedEntityData.DataItem<?>> getAll();
}
