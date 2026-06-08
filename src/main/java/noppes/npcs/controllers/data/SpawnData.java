package noppes.npcs.controllers.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import noppes.npcs.NBTTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnData implements WeightedEntry {
	public List<ResourceLocation> biomes = new ArrayList<ResourceLocation>();
	public int id = -1;
	public String name = "";
	public Map<Integer, CloneSpawnData> data = new HashMap<>();
	public boolean liquid = false;
	public int type = 0; //0:any, 1:dark only

	private Weight weight = Weight.of(10);

	public void readNBT(CompoundTag compound) {
		id = compound.getInt("SpawnId");
		name = compound.getString("SpawnName");
		setWeight(compound.getInt("SpawnWeight"));


		biomes = NBTTags.getResourceLocationList(compound.getList("SpawnBiomes", 10));
		data = CloneSpawnData.load(compound.getList("SpawnData", 10));

		type = compound.getInt("SpawnType");
	}

	public CompoundTag writeNBT(CompoundTag compound) {
		compound.putInt("SpawnId", id);
		compound.putString("SpawnName", name);
		compound.putInt("SpawnWeight", weight.asInt());
		
		compound.put("SpawnBiomes", NBTTags.nbtResourceLocationList(biomes));

		compound.put("SpawnData", CloneSpawnData.save(data));

		compound.putInt("SpawnType", type);
		return compound;
	}
	public void setWeight(int weight){
		if(weight == 0)
			weight = 1;
		this.weight = Weight.of(weight);
	}

	public void setClone(int slot, int tab, String name) {
		data.put(slot, new CloneSpawnData(tab, name));
	}

	public CompoundTag getCompound(int slot) {
		CloneSpawnData sd = data.get(slot);
		if(sd == null){
			return null;
		}
		return sd.getCompound();
	}

	@Override
	public Weight getWeight() {
		return weight;
	}
}
