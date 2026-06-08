package noppes.npcs.api.wrapper;

import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import noppes.npcs.ItemStackEmptyWrapper;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.entity.IMob;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemScripted;

import java.util.*;
import java.util.Map.Entry;

public class ItemStackWrapper implements IItemStack {
	private Map<String, Object> tempData = new HashMap<String, Object>();

	private static Map<Integer, ItemStackWrapper> dataMap = new HashMap<>();

    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	public ItemStack item;
	
	private CompoundTag storedData = new CompoundTag();

	public static ItemStackWrapper AIR = new ItemStackEmptyWrapper();

	private final IData tempdata = new IData() {

		@Override
		public void put(String key, Object value) {
			tempData.put(key, value);
		}

		@Override
		public Object get(String key) {
			return tempData.get(key);
		}

		@Override
		public void remove(String key) {
			tempData.remove(key);
		}

		@Override
		public boolean has(String key) {
			return tempData.containsKey(key);
		}

		@Override
		public void clear() {
			tempData.clear();
		}

		@Override
		public String[] getKeys() {
			return tempData.keySet().toArray(new String[tempData.size()]);
		}
		
	};
	

	private final IData storeddata = new IData() {

		@Override
		public void put(String key, Object value) {
			if(value instanceof Number){
				storedData.putDouble(key, ((Number) value).doubleValue());
			}
			else if(value instanceof String)
				storedData.putString(key, (String)value);
		}

		@Override
		public Object get(String key) {
			if(!storedData.contains(key))
				return null;
			Tag base = storedData.get(key);
			if(base instanceof NumericTag)
				return ((NumericTag)base).getAsDouble();
			return base.getAsString();
		}

		@Override
		public void remove(String key) {
			storedData.remove(key);
		}

		@Override
		public boolean has(String key) {
			return storedData.contains(key);
		}

		@Override
		public void clear() {
			storedData = new CompoundTag();
		}

		@Override
		public String[] getKeys() {
			return storedData.getAllKeys().toArray(new String[storedData.getAllKeys().size()]);
		}
	};
	
	public ItemStackWrapper(ItemStack item){
		this.item = item;
	}


	@Override
	public IData getTempdata(){
		return tempdata;
	}

	@Override
	public IData getStoreddata(){
		return storeddata;
	}

	@Override
	public int getStackSize(){
		return item.getCount();
	}

	@Override
	public void setStackSize(int size){
		if(size > getMaxStackSize())
			throw new CustomNPCsException("Can't set the stacksize bigger than MaxStacksize");
		item.setCount(size);
	}
	
	@Override
	public void setAttribute(String name, double value) {
		setAttribute(name, value, -1);
	}
	
	@Override
	public void setAttribute(String name, double value, int slot) {
		if(slot < -1 || slot > 5) {
			throw new CustomNPCsException("Slot has to be between -1 and 5, given was: " + slot);
		}
		
		CompoundTag compound = item.getTag();
		if(compound == null)
			item.setTag(compound = new CompoundTag());

		
        ListTag nbttaglist = compound.getList("AttributeModifiers", 10);
        ListTag newList = new ListTag();
		UUID uuid = null;
        for(int i = 0; i < nbttaglist.size(); i++) {
        	CompoundTag c = nbttaglist.getCompound(i);
        	if(!c.getString("AttributeName").equals(name) ) {
        		newList.add(c);
        	}else{
				uuid = c.getUUID("UUID");
			}
        }
        if(value != 0) {
            CompoundTag nbttagcompound = new AttributeModifier(name, value, AttributeModifier.Operation.ADDITION).save();
            nbttagcompound.putString("AttributeName", name);
            if(slot >= 0) {
                nbttagcompound.putString("Slot", EquipmentSlot.values()[slot].getName());
            }
			if(uuid!=null){
				nbttagcompound.putUUID("UUID", uuid);
			}
    		newList.add(nbttagcompound);
        }
        compound.put("AttributeModifiers", newList);
	}
	
	@Override
	public double getAttribute(String name) {
		CompoundTag compound = item.getTag();
		if(compound == null)
			return 0;
		Multimap<Attribute, AttributeModifier> map = item.getAttributeModifiers(EquipmentSlot.MAINHAND);
        for (Entry<Attribute, AttributeModifier> entry : map.entries()){
            if(entry.getKey().getDescriptionId().equals(name)){
                AttributeModifier mod = entry.getValue();
                return mod.getAmount();
            }
        }
        return 0;
	}
	
	@Override
	public boolean hasAttribute(String name) {
		CompoundTag compound = item.getTag();
		if(compound == null)
			return false;
        ListTag nbttaglist = compound.getList("AttributeModifiers", 10);
        for(int i = 0; i < nbttaglist.size(); i++) {
        	CompoundTag c = nbttaglist.getCompound(i);
        	if(c.getString("AttributeName").equals(name)) {
        		return true;
        	}
        }
        return false;
	}

	@Override
	public void addEnchantment(String id, int strenght){
		Enchantment ench = BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(id));
		if(ench == null)
			throw new CustomNPCsException("Unknown enchant id:" + id);
		item.enchant(ench, strenght);
	}

	@Override
	public boolean isEnchanted(){
		return item.isEnchanted();
	}

	@Override
	public boolean hasEnchant(String id){
		Enchantment ench = BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(id));
		if(ench == null)
			throw new CustomNPCsException("Unknown enchant id:" + id);
		if(!isEnchanted())
			return false;
		ListTag list = item.getEnchantmentTags();
		for(int i = 0; i < list.size(); i++){
			CompoundTag compound = list.getCompound(i);
			if(compound.getString("id").equalsIgnoreCase(id))
				return true;
		}
		return false;
	}

	@Override
	public boolean removeEnchant(String id){
		Enchantment ench = BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(id));
		if(ench == null)
			throw new CustomNPCsException("Unknown enchant id:" + id);
		if(!isEnchanted())
			return false;
		ListTag list = item.getEnchantmentTags();
		ListTag newList = new ListTag();
		for(int i = 0; i < list.size(); i++){
			CompoundTag compound = list.getCompound(i);
			if(!compound.getString("id").equalsIgnoreCase(id))
				newList.add(compound);
		}
		if(list.size() == newList.size()) {
			return false;
		}
		item.getTag().put("ench", newList);
		return true;
	}

	@Override
	public boolean isBlock(){
		Block block = Block.byItem(item.getItem());
		if(block == null || block == Blocks.AIR)
			return false;
		return true;
	}

	@Override
	public boolean hasCustomName(){
		return item.hasCustomHoverName();
	}

	@Override
	public void setCustomName(String name){
		item.setHoverName(Component.translatable(name));
	}

	@Override
	public String getDisplayName(){
		return item.getHoverName().getString();
	}

	@Override
	public String getItemName(){
		return item.getItem().getName(item).getString();
	}
	
	@Override
	public String getName(){
		return BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
	}

	@Override
	public INbt getNbt(){
		CompoundTag compound = item.getTag();
		if(compound == null)
			item.setTag(compound = new CompoundTag());
		return NpcAPI.Instance().getINbt(compound);
	}

	@Override
	public boolean hasNbt(){
		CompoundTag compound = item.getTag();
		return compound != null && !compound.isEmpty();
	}

	@Override
	public ItemStack getMCItemStack() {
		return item;
	}

	public static ItemStack MCItem(IItemStack item) {
		if(item == null)
			return ItemStack.EMPTY;
		return item.getMCItemStack();
	}

	@Override
	public void damageItem(int damage, IMob living) {
		if(living != null){
			item.hurtAndBreak(damage, living == null? null:living.getMCEntity(), (e) -> {
				e.broadcastBreakEvent(EquipmentSlot.MAINHAND);
			});
		}
		else if(item.isDamageableItem()){
			if(item.getDamageValue() <= damage){
				item.shrink(1);
				item.setDamageValue(0);
			}
			else{
				item.setDamageValue(item.getDamageValue() - damage);
			}

		}
	}

	@Override
	public boolean isBook(){
		return false;
	}

	@Override
	public int getFoodLevel() {
		if(item.getItem().getFoodProperties() != null) {
			return item.getItem().getFoodProperties().getNutrition();
		}
		return 0;
	}

	@Override
	public IItemStack copy() {
		return createNew(item.copy());
	}

	@Override
	public int getMaxStackSize(){ 
		return item.getMaxStackSize();
	}

	@Override
	public boolean isDamageable() {
		return item.isDamageableItem();
	}

	@Override
	public int getDamage() {
		return item.getDamageValue();
	}

	@Override
	public void setDamage(int value) {
		item.setDamageValue(value);
	}

	@Deprecated
	public int getItemDamage() {
		return item.getDamageValue();
	}
	@Deprecated
	public void setItemDamage(int value) {
		item.setDamageValue(value);
	}

	@Override
	public int getMaxDamage(){
		return item.getMaxDamage();
	}

	@Override
	public INbt getItemNbt() {
		CompoundTag compound = new CompoundTag();
		item.save(compound);
		return NpcAPI.Instance().getINbt(compound);
	}

	@Override
	public double getAttackDamage() {
		Multimap<Attribute, AttributeModifier> map = item.getAttributeModifiers(EquipmentSlot.MAINHAND);
        double damage = 0;
		for (Entry<Attribute, AttributeModifier> entry : map.entries()) {
            if(entry.getKey() == Attributes.ATTACK_DAMAGE){
                AttributeModifier mod = entry.getValue();
                damage = mod.getAmount();
            }
        }
		return damage + EnchantmentHelper.getDamageBonus(item, MobType.UNDEFINED);
	}

	@Override
	public boolean isEmpty(){
		return item.isEmpty();
	}

	@Override
	public int getType(){
//		if(item.getItem() instanceof IPlantable)
//			return ItemType.SEEDS;
		if(item.getItem() instanceof SwordItem)
			return ItemType.SWORD;
		return ItemType.NORMAL;
	}

	@Override
	public boolean isWearable() {
		return item.getItem() instanceof Equipable;
	}

	private static final ResourceLocation key = new ResourceLocation("customnpcs", "itemscripteddata");
	
	private static ItemStackWrapper createNew(ItemStack item){
		if(item == null || item.isEmpty())
			return AIR;
				
		if(item.getItem() instanceof ItemScripted) {
			return new ItemScriptedWrapper(item);		
		}
		if(item.getItem() == Items.WRITTEN_BOOK || item.getItem() == Items.WRITABLE_BOOK || item.getItem() instanceof WritableBookItem || item.getItem() instanceof WrittenBookItem)
			return new ItemBookWrapper(item);
		if(item.getItem() instanceof ArmorItem)
			return new ItemArmorWrapper(item);

		Block block = Block.byItem(item.getItem());
		if(block != Blocks.AIR)
			return new ItemBlockWrapper(item);
		
		return new ItemStackWrapper(item);
	}

	@Override
	public String[] getLore() {
		CompoundTag compound = item.getTagElement("display");
		if(compound == null || compound.getTagType("Lore") != 9)
			return new String[0];

        ListTag nbttaglist = compound.getList("Lore", 8);
        if(nbttaglist.isEmpty())
			return new String[0];

        List<String> lore = new ArrayList<String>();
        for (int i = 0; i < nbttaglist.size(); ++i){
        	lore.add(nbttaglist.getString(i));
        }
        
		return lore.toArray(new String[lore.size()]);
	}

	@Override
	public void setLore(String[] lore) {
		CompoundTag compound = item.getOrCreateTagElement("display");
		if(lore == null || lore.length == 0) {
			compound.remove("Lore");
			return;
		}
		
		ListTag nbtlist = new ListTag();
		for(String s : lore) {
			try {
				Component.Serializer.fromJson(s);
			} catch (JsonParseException jsonparseexception) {
				s = Component.Serializer.toJson(Component.translatable(s));
			}
			nbtlist.add(StringTag.valueOf(s));
		}
		compound.put("Lore", nbtlist);
	}

	public CompoundTag serializeNBT() {
		return getMCNbt();
	}

	public void deserializeNBT(CompoundTag nbt) {
		setMCNbt(nbt);
	}
	
	public CompoundTag getMCNbt() {
		CompoundTag compound = new CompoundTag();
		if(!storedData.isEmpty()) {
			compound.put("StoredData", storedData);
		}
		return compound;
	}
	
	public void setMCNbt(CompoundTag compound) {
		if(compound==null){
			storedData = new CompoundTag();
		}else {
			storedData = compound.getCompound("StoredData");
		}
	}

	@Override
	public void removeNbt() {
		this.item.setTag(null);
	}

	@Override
	public boolean compare(IItemStack item, boolean ignoreNBT) {
		if(item == null)
			item = AIR;
		return NoppesUtilPlayer.compareItems(getMCItemStack(), item.getMCItemStack(), false, ignoreNBT);
	}
	
}
