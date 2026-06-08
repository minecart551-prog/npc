package noppes.npcs;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;

import java.util.*;

public class NBTTags {

	public static void getItemStackList(ListTag tagList, NonNullList<ItemStack> items) {
		items.clear();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            try{
            	items.set(nbttagcompound.getByte("Slot") & 0xff, ItemStack.of(nbttagcompound));
            }
            catch(ClassCastException e){
            	items.set(nbttagcompound.getInt("Slot"), ItemStack.of(nbttagcompound));
            }
        }
	}

	public static Map<Integer, IItemStack> getIItemStackMap(ListTag tagList) {
		Map<Integer, IItemStack> list = new HashMap<Integer, IItemStack>();
        for(int i = 0; i < tagList.size(); i++){
            CompoundTag nbttagcompound = tagList.getCompound(i);
            ItemStack item = ItemStack.of(nbttagcompound);
            if(item.isEmpty())
            	continue;
            try{
            	list.put(nbttagcompound.getByte("Slot") & 0xff, NpcAPI.Instance().getIItemStack(item));
            }
            catch(ClassCastException e){
            	list.put(nbttagcompound.getInt("Slot"), NpcAPI.Instance().getIItemStack(item));
            }
        }
		return list;
	}
	
	public static ItemStack[] getItemStackArray(
			ListTag tagList) {
		ItemStack[] list = new ItemStack[tagList.size()];
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
        	list[nbttagcompound.getByte("Slot") & 0xff] = ItemStack.of(nbttagcompound);
        }
		return list;
	}
	
	public static NonNullList<Ingredient> getIngredientList(ListTag tagList) {
		NonNullList<Ingredient> list = NonNullList.create();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.add(nbttagcompound.getByte("Slot") & 0xff, Ingredient.of(ItemStack.of(nbttagcompound)));
        }
		return list;
	}
	
	public static ArrayList<int[]> getIntegerArraySet(ListTag tagList) {
		ArrayList<int[]> set = new ArrayList<int[]>();
        for(int i = 0; i < tagList.size(); i++)
        {
        	CompoundTag compound = tagList.getCompound(i);
        	set.add(compound.getIntArray("Array"));
        }
		return set;
	}

	public static HashMap<Integer, Boolean> getBooleanList(ListTag tagList) {
		HashMap<Integer, Boolean> list = new HashMap<Integer, Boolean>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInt("Slot"), nbttagcompound.getBoolean("Boolean"));
        }
		return list;
	}

	public static HashMap<Integer, Integer> getIntegerIntegerMap(
			ListTag tagList) {
		HashMap<Integer, Integer> list = new HashMap<Integer, Integer>();
		for(int i = 0; i < tagList.size(); i++)
		{
			CompoundTag nbttagcompound = tagList.getCompound(i);
			list.put(nbttagcompound.getInt("Slot"), nbttagcompound.getInt("Integer"));
		}
		return list;
	}

	public static HashMap<Integer, Float> getFloatIntegerMap(
			ListTag tagList) {
		HashMap<Integer, Float> list = new HashMap<Integer, Float>();
		for(int i = 0; i < tagList.size(); i++)
		{
			CompoundTag nbttagcompound = tagList.getCompound(i);
			list.put(nbttagcompound.getInt("Slot"), nbttagcompound.getFloat("Integer"));
		}
		return list;
	}
	
	public static HashMap<Integer, Long> getIntegerLongMap(
			ListTag tagList) {
		HashMap<Integer, Long> list = new HashMap<Integer, Long>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInt("Slot"), nbttagcompound.getLong("Long"));
        }
		return list;
	}
	
	public static HashSet<Integer> getIntegerSet(ListTag tagList) {
		HashSet<Integer> list = new HashSet<Integer>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.add(nbttagcompound.getInt("Integer"));
        }
		return list;
	}
	
	public static List<Integer> getIntegerList(ListTag tagList) {
		List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.add(nbttagcompound.getInt("Integer"));
        }
		return list;
	}

	public static HashMap<String, String> getStringStringMap(ListTag tagList) {
		HashMap<String, String> list = new HashMap<String, String>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getString("Slot"), nbttagcompound.getString("Value"));
        }
		return list;
	}

	public static HashMap<Integer, String> getIntegerStringMap(ListTag tagList) {
		HashMap<Integer, String> list = new HashMap<Integer, String>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInt("Slot"), nbttagcompound.getString("Value"));
        }
		return list;
	}
	
	public static HashMap<String, Integer> getStringIntegerMap(ListTag tagList) {
		HashMap<String, Integer> list = new HashMap<String, Integer>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getString("Slot"), nbttagcompound.getInt("Value"));
        }
		return list;
	}
	public static HashMap<String, Vector<String>> getVectorMap(ListTag tagList) {
		HashMap<String, Vector<String>> map = new HashMap<String, Vector<String>>();
        for(int i = 0; i < tagList.size(); i++)
        {
        	Vector<String> values = new Vector<String>();
            CompoundTag nbttagcompound = tagList.getCompound(i);
            ListTag list = nbttagcompound.getList("Values", 10);
            for(int j = 0; j < list.size(); j++)
            {
                CompoundTag value = list.getCompound(j);
                values.add(value.getString("Value"));
            }
            
            map.put(nbttagcompound.getString("Key"), values);
        }
		return map;
	}


	public static List<String> getStringList(ListTag tagList) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag nbttagcompound = tagList.getCompound(i);
			String line = nbttagcompound.getString("Line");
			list.add(line);
		}
		return list;
	}

	public static List<ResourceLocation> getResourceLocationList(ListTag tagList) {
		List<ResourceLocation> list = new ArrayList<ResourceLocation>();
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag nbttagcompound = tagList.getCompound(i);
			ResourceLocation line = new ResourceLocation(nbttagcompound.getString("Line"));
			list.add(line);
		}
		return list;
	}
	
	public static String[] getStringArray(ListTag tagList, int size) {
		String[] arr = new String[size];
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag nbttagcompound = tagList.getCompound(i);
			String line = nbttagcompound.getString("Value");
			int slot = nbttagcompound.getInt("Slot");
			arr[slot] = line;
		}
		return arr;
	}
	
    public static ListTag nbtIntegerArraySet(List<int[]> set) {
        ListTag nbttaglist = new ListTag();
    	if(set == null)
    		return nbttaglist;
        for(int[] arr : set)
        {
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putIntArray("Array", arr);
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}
	
    public static ListTag nbtItemStackList(NonNullList<ItemStack> inventory) {
        ListTag nbttaglist = new ListTag();
        for(int slot = 0; slot < inventory.size(); slot++){
        	ItemStack item = inventory.get(slot);
        	if(item.isEmpty())
        		continue;
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putByte("Slot", (byte)slot);
            
            item.save(nbttagcompound);
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}
	
    public static ListTag nbtIItemStackMap(Map<Integer, IItemStack> inventory) {
        ListTag nbttaglist = new ListTag();
    	if(inventory == null)
    		return nbttaglist;
        for(int slot : inventory.keySet()){
        	IItemStack item = inventory.get(slot);
        	if(item == null)
        		continue;
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putByte("Slot", (byte)slot);
            
            item.getMCItemStack().save(nbttagcompound);
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}
	
    public static ListTag nbtItemStackArray(ItemStack[] inventory) {
        ListTag nbttaglist = new ListTag();
    	if(inventory == null)
    		return nbttaglist;
        for(int slot = 0 ; slot < inventory.length; slot++)
        {
        	ItemStack item = inventory[slot];
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putByte("Slot", (byte)slot);

        	if(item != null)
        		item.save(nbttagcompound);
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}
	
    public static ListTag nbtIngredientList(NonNullList<Ingredient> inventory) {
        ListTag nbttaglist = new ListTag();
    	if(inventory == null)
    		return nbttaglist;
        for(int slot = 0 ; slot < inventory.size(); slot++)
        {
        	Ingredient ingredient = inventory.get(slot);
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putByte("Slot", (byte)slot);

        	if(ingredient != null && ingredient.getItems().length > 0)
        		ingredient.getItems()[0].save(nbttagcompound);
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}

	public static ListTag nbtBooleanList(HashMap<Integer, Boolean> updatedSlots) {
        ListTag nbttaglist = new ListTag();
    	if(updatedSlots == null)
    		return nbttaglist;
        HashMap<Integer,Boolean> inventory2 = updatedSlots;
        for(Integer slot : inventory2.keySet())
        {
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putInt("Slot", slot);
            nbttagcompound.putBoolean("Boolean", inventory2.get(slot));
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}

	public static ListTag nbtIntegerIntegerMap(Map<Integer, Integer> lines) {
		ListTag nbttaglist = new ListTag();
		if(lines == null)
			return nbttaglist;
		for (int slot : lines.keySet()) {
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Slot", slot);
			nbttagcompound.putInt("Integer", lines.get(slot));
			nbttaglist.add(nbttagcompound);
		}
		return nbttaglist;
	}

	public static ListTag nbtFloatMap(Map<Integer, Float> lines) {
		ListTag nbttaglist = new ListTag();
		if(lines == null)
			return nbttaglist;
		for (int slot : lines.keySet()) {
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Slot", slot);
			nbttagcompound.putFloat("Integer", lines.get(slot));
			nbttaglist.add(nbttagcompound);
		}
		return nbttaglist;
	}

	public static ListTag nbtIntegerLongMap(HashMap<Integer, Long> lines) {
		ListTag nbttaglist = new ListTag();
		if(lines == null)
			return nbttaglist;
		for (int slot : lines.keySet()) {
			CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putInt("Slot", slot);
            nbttagcompound.putLong("Long", lines.get(slot));
			nbttaglist.add(nbttagcompound);
		}
		return nbttaglist;
	}

	public static ListTag nbtIntegerCollection(Collection<Integer> set) {
		ListTag nbttaglist = new ListTag();
		if(set == null)
			return nbttaglist;
		for (int slot : set) {
			CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putInt("Integer", slot);
			nbttaglist.add(nbttagcompound);
		}
		return nbttaglist;
	}

	public static ListTag nbtVectorMap(HashMap<String, Vector<String>> map) {
        ListTag list = new ListTag();
        if(map == null)
        	return list;
        for(String key : map.keySet()){
        	CompoundTag compound = new CompoundTag();
        	compound.putString("Key", key);
            ListTag values = new ListTag();
        	for(String value : map.get(key)){
            	CompoundTag comp = new CompoundTag();
            	comp.putString("Value", value);
            	values.add(comp);
        	}
            compound.put("Values", values);
        	list.add(compound);
        }
		return list;
	}
	
	public static ListTag nbtStringStringMap(HashMap<String, String> map) {
        ListTag nbttaglist = new ListTag();
    	if(map == null)
    		return nbttaglist;
        for(String slot : map.keySet())
        {
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putString("Slot", slot);
            nbttagcompound.putString("Value", map.get(slot));
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}
	
	public static ListTag nbtStringIntegerMap(Map<String, Integer> map) {
        ListTag nbttaglist = new ListTag();
    	if(map == null)
    		return nbttaglist;
        for(String slot : map.keySet())
        {
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putString("Slot", slot);
            nbttagcompound.putInt("Value", map.get(slot));
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}

	public static Tag nbtIntegerStringMap(Map<Integer, String> map) {
        ListTag nbttaglist = new ListTag();
    	if(map == null)
    		return nbttaglist;
        for(int slot : map.keySet())
        {
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putInt("Slot", slot);
            nbttagcompound.putString("Value", map.get(slot));
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}
	
	public static ListTag nbtStringArray(String[] list) {
        ListTag nbttaglist = new ListTag();
    	if(list == null)
    		return nbttaglist;
        for(int i = 0; i < list.length; i++){ 
        	if(list[i] == null)
        		continue;
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putString("Value", list[i]);
			nbttagcompound.putInt("Slot", i);
			nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}

	public static ListTag nbtStringList(List<String> list) {
		ListTag nbttaglist = new ListTag();
		for (String s : list) {
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putString("Line", s);
			nbttaglist.add(nbttagcompound);
		}
		return nbttaglist;
	}

	public static ListTag nbtResourceLocationList(List<ResourceLocation> list) {
		ListTag nbttaglist = new ListTag();
		for (ResourceLocation s : list) {
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putString("Line", s.toString());
			nbttaglist.add(nbttagcompound);
		}
		return nbttaglist;
	}

	public static ListTag nbtDoubleList(double ... par1ArrayOfDouble){
        ListTag nbttaglist = new ListTag();
        double[] adouble = par1ArrayOfDouble;
        int i = par1ArrayOfDouble.length;

        for (int j = 0; j < i; ++j)
        {
            double d1 = adouble[j];
            nbttaglist.add(DoubleTag.valueOf(d1));
        }

        return nbttaglist;
    }

	public static CompoundTag NBTMerge(CompoundTag data, CompoundTag merge) {
		CompoundTag compound = (CompoundTag) data.copy();
		Set<String> names = merge.getAllKeys();
		for(String name : names){
			Tag base = merge.get(name);
			if(base.getId() == 10)
				base = NBTMerge(compound.getCompound(name), (CompoundTag) base);
			compound.put(name, base);
		}
		return compound;
	}
	
	public static List<ScriptContainer> GetScript(ListTag list, IScriptHandler handler){
		List<ScriptContainer> scripts = new ArrayList<ScriptContainer>();
		for(int i = 0; i < list.size(); i++){
			CompoundTag compoundd = list.getCompound(i);
			ScriptContainer script = new ScriptContainer(handler);
			script.load(compoundd);
			scripts.add(script);			
		}
		return scripts;
	}
	
	public static ListTag NBTScript(List<ScriptContainer> scripts){
		ListTag list = new ListTag();
		for(ScriptContainer script : scripts){
			CompoundTag compound = new CompoundTag();
			script.save(compound);
			list.add(compound);
		}
		return list;
	}

	public static TreeMap<Long, String> GetLongStringMap(ListTag tagList) {
		TreeMap<Long, String> list = new TreeMap<Long, String>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getLong("Long"), nbttagcompound.getString("String"));
        }
		return list;
	}

	public static ListTag NBTLongStringMap(Map<Long, String> map) {
        ListTag nbttaglist = new ListTag();
    	if(map == null)
    		return nbttaglist;
        for(long slot : map.keySet())
        {
            CompoundTag nbttagcompound = new CompoundTag();
            nbttagcompound.putLong("Long", slot);
            nbttagcompound.putString("String", map.get(slot));
            
            nbttaglist.add(nbttagcompound);
        }
        return nbttaglist;
	}

}
