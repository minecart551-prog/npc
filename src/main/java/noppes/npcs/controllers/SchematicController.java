package noppes.npcs.controllers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomNpcs;
import noppes.npcs.schematics.*;
import noppes.npcs.shared.common.CommonUtil;
import noppes.npcs.shared.common.util.LogWriter;
import noppes.npcs.util.ValueUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SchematicController {
	public static SchematicController Instance = new SchematicController();

	private SchematicWrapper building = null;
	private CommandSourceStack buildStarter = null;
	private int buildingPercentage = 0;

	public List<String> included = Arrays.asList("archery_range.schematic", "bakery.schematic", "barn.schematic", "building_site.schematic", "chapel.schematic", "church.schematic", "gate.schematic", "glassworks.schematic", "guard_tower.schematic", "guild_house.schematic",
			"house.schematic", "house_small.schematic", "inn.schematic", "library.schematic", "lighthouse.schematic", "mill.schematic", "observatory.schematic", "ship.schematic", "shop.schematic", "stall.schematic", "stall2.schematic", "stall3.schematic",
			"tier_house1.schematic", "tier_house2.schematic", "tier_house3.schematic", "tower.schematic", "wall.schematic", "wall_corner.schematic");
	
	
	public List<String> list(){
		List<String> list = new ArrayList<String>();
		list.addAll(included);
		for(File file : getDir().listFiles()){
			String name = file.getName();
			if(ValueUtil.isValidPath(name) && (name.toLowerCase().endsWith(".schematic") || name.toLowerCase().endsWith(".schem") || name.toLowerCase().endsWith(".blueprint"))){
				list.add(name);
			}
		}
		Collections.sort(list);
		return list;
	}
	
	public File getDir(){
		File dir = new File(CustomNpcs.getLevelSaveDirectory(), "schematics");
		if(!dir.exists())
			dir.mkdir();
		
		return dir;
	}
	
	public void info(CommandSourceStack sender){
		if(building == null){
			sendMessage(sender, "Nothing is being build");
		}
		else{
			sendMessage(sender, "Already building: " + building.schema.getName() + " - " + building.getPercentage() + "%");
			if(buildStarter != null)
				sendMessage(sender, "Build started by: " + buildStarter.getDisplayName().getString());
		}
	}
	
	private void sendMessage(CommandSourceStack sender, String message){
		if(sender == null)
			return;
		sender.sendSuccess(()->Component.literal(message), false);
	}


	public void stop(CommandSourceStack sender) {
		if(building == null || !building.isBuilding){
			sendMessage(sender, "Not building");
		}
		else{
			sendMessage(sender, "Stopped building: " + building.schema.getName());
			building = null;
		}
		
	}
	
	public void build(SchematicWrapper schem, CommandSourceStack sender){
		if(building != null && building.isBuilding){
			info(sender);
			return;
		}
		buildingPercentage = 0;
		building = schem;
		building.isBuilding = true;
		
		buildStarter = sender;
	}
	
	public void updateBuilding(){
		if(building == null)
			return;
		building.build();
		if(buildStarter != null && building.getPercentage() - buildingPercentage >= 10){
			sendMessage(buildStarter, "Building at " + building.getPercentage() + "%");
			buildingPercentage = building.getPercentage();
		}
		if(!building.isBuilding){
			if(buildStarter != null)
				sendMessage(buildStarter, "Building finished");
			building = null;
		}
	}

	public SchematicWrapper load(String name) {

		InputStream stream = null;

		if(included.contains(name)){
			ResourceLocation resource = new ResourceLocation("customnpcs","schematics/" + name);
			Resource ir = CustomNpcs.Server.getResourceManager().getResource(resource).orElse(null);
			if(ir!=null)
			try{
				stream = ir.open();
			} catch (IOException e) {
			}
		}
		if(stream == null){
			File file = new File(getDir(), name);
			if(!file.exists()){
				return null;
			}
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		try {
			CompoundTag compound = NbtIo.readCompressed(stream);
			stream.close();
			if(name.toLowerCase().endsWith(".schem")){
				SpongeSchem bp = new SpongeSchem(name);
				bp.load(compound);
				return new SchematicWrapper(bp);
			}
			if(name.toLowerCase().endsWith(".blueprint")){
				Blueprint bp = BlueprintUtil.readBlueprintFromNBT(compound);
				bp.setName(name);
				return new SchematicWrapper(bp);
			}
			Schematic schema = new Schematic(name);
			schema.load(compound);
			return new SchematicWrapper(schema);
		} catch (IOException e) {
			LogWriter.except(e);
		}
		return null;
	}

	public void save(CommandSourceStack sender, String name, BlockPos pos, short height, short width, short length) {
		name = name.replace(" ", "_");
		if(included.contains(name))
			return;

		Level level = sender.getLevel();
		File file = new File(getDir(), name + ".schem");
		ISchematic schema = SpongeSchem.Create(level, name, pos, height, width, length);

		CommonUtil.NotifyOPs(sender.getServer(), "Schematic " + name + " succesfully created");
		try {
			NbtIo.writeCompressed(schema.getNBT(), new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
}
