package noppes.npcs.controllers.data;

import java.util.HashSet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class PlayerDialogData{
	public HashSet<Integer> dialogsRead = new HashSet<Integer>();
	
	public void loadNBTData(CompoundTag compound) {
		HashSet<Integer> dialogsRead = new HashSet<Integer>();
		if(compound == null)
			return;
        ListTag list = compound.getList("DialogData", 10);
        if(list == null){
        	return;
        }

        for(int i = 0; i < list.size(); i++)
        {
            CompoundTag nbttagcompound = list.getCompound(i);
            dialogsRead.add(nbttagcompound.getInt("Dialog"));
        }
        this.dialogsRead = dialogsRead;
	}

	public void saveNBTData(CompoundTag compound) {
		ListTag list = new ListTag();
		for(int dia : dialogsRead){
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Dialog", dia);
			list.add(nbttagcompound);
		}
		
		compound.put("DialogData", list);
	}
	
}
