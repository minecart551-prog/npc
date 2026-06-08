package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

public class DialogCategory implements IDialogCategory{

	public int id = -1;
	public String title = "";
	public HashMap<Integer,Dialog> dialogs;
	
	public DialogCategory(){
		dialogs = new HashMap<Integer, Dialog>();
	}
	
	public void readNBT(CompoundTag compound){
        id = compound.getInt("Slot");
        title = compound.getString("Title");
        
        ListTag dialogsList = compound.getList("Dialogs", 10);
        if(dialogsList != null){
            for(int ii = 0; ii < dialogsList.size(); ii++)
            {
            	Dialog dialog = new Dialog(this);
            	CompoundTag comp = dialogsList.getCompound(ii);
            	dialog.readNBT(comp);
            	dialog.id = comp.getInt("DialogId");
            	dialogs.put(dialog.id, dialog);
            }
        }
	}

	public CompoundTag writeNBT(CompoundTag compound) {
		compound.putInt("Slot", id);
		compound.putString("Title", title);
        ListTag dialogs = new ListTag();
        for(Dialog dialog : this.dialogs.values()){
        	dialogs.add(dialog.save(new CompoundTag()));
        }
		compound.put("Dialogs", dialogs);
        return compound;
	}

	@Override
	public List<IDialog> dialogs() {
		return new ArrayList<IDialog>(dialogs.values());
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public IDialog create() {
		return new Dialog(this);
	}
}
