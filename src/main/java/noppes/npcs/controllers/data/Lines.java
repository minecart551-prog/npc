package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;



public class Lines {
	private static final Random random = new Random();
	private int lastLine = -1;

	public HashMap<Integer,Line> lines = new HashMap<Integer,Line>();

	public CompoundTag save() {
		CompoundTag compound = new CompoundTag();

        ListTag nbttaglist = new ListTag();
        for(int slot : lines.keySet())
        {
        	Line line = lines.get(slot);
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putInt("Slot", slot);
            nbttagcompound.putString("Line", line.getText());
            nbttagcompound.putString("Song", line.getSound());
            
            nbttaglist.add(nbttagcompound);
        }
        
		compound.put("Lines", nbttaglist);
		return compound;
	}

	public void readNBT(CompoundTag compound) {
		ListTag nbttaglist = compound.getList("Lines", 10);

		HashMap<Integer, Line> map = new HashMap<Integer, Line>();
        for(int i = 0; i < nbttaglist.size(); i++)
        {
            CompoundTag nbttagcompound = nbttaglist.getCompound(i);
            Line line = new Line();
            line.setText(nbttagcompound.getString("Line"));
            line.setSound(nbttagcompound.getString("Song"));
            
            map.put(nbttagcompound.getInt("Slot"), line);
        }
        lines = map;
	}

	public Line getLine(boolean isRandom) {
		if(lines.isEmpty())
			return null;
		if(isRandom){
			int i = random.nextInt(this.lines.size());
			for(Entry<Integer,Line> e: this.lines.entrySet()){
				if (--i < 0) 
					return e.getValue().copy();
			}
		}
		lastLine++;
		while(true){
			lastLine %= 8;
			Line line = lines.get(lastLine);
			if(line != null)
				return line.copy();
			lastLine++;
		}
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}
}
