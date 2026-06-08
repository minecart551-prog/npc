package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.CustomNpcs;

public class GlobalDataController {
	public static GlobalDataController instance;
	
	private int itemGiverId = 0;
	
	public GlobalDataController(){
		instance = this;
		load();
	}
	
	private void load(){
		File saveDir = CustomNpcs.getLevelSaveDirectory();
		try {
	        File file = new File(saveDir, "global.dat");
	        if(file.exists()){
	        	loadData(file);
	        }
		} catch (Exception e) {
			try {
		        File file = new File(saveDir, "global.dat_old");
		        if(file.exists()){
		        	loadData(file);
		        }
		        
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}
	private void loadData(File file) throws Exception{		
        CompoundTag nbttagcompound1 = NbtIo.readCompressed(new FileInputStream(file));
        itemGiverId = nbttagcompound1.getInt("itemGiverId");
	}
	public void saveData(){
		try {
			File saveDir = CustomNpcs.getLevelSaveDirectory();

	        CompoundTag nbttagcompound = new CompoundTag();
	        nbttagcompound.putInt("itemGiverId", itemGiverId);
	        
            File file = new File(saveDir, "global.dat_new");
            File file1 = new File(saveDir, "global.dat_old");
            File file2 = new File(saveDir, "global.dat");
            NbtIo.writeCompressed(nbttagcompound, new FileOutputStream(file));
            if(file1.exists())
            {
                file1.delete();
            }
            file2.renameTo(file1);
            if(file2.exists())
            {
                file2.delete();
            }
            file.renameTo(file2);
            if(file.exists())
            {
                file.delete();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int incrementItemGiverId(){
		itemGiverId++;
		saveData();
		return itemGiverId;
	}
}
