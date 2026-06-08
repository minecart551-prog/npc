package noppes.npcs.client.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.ServerCloneController;

public class ClientCloneController extends ServerCloneController{
	public static ClientCloneController Instance = new ClientCloneController();

	public File getDir(){
		File dir = new File(CustomNpcs.Dir,"clones");
		if(!dir.exists())
			dir.mkdir();
		return dir;
	}
}
