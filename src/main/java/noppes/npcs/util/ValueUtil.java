package noppes.npcs.util;

import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ValueUtil {

	public static final UUID EMPTY_UUID = new UUID(0L, 0L);

	public static float correctFloat(float given, float min, float max){
		if(given < min)
			return min;
		if(given > max)
			return max;
		return given;
	}

	public static int CorrectInt(int given, int min, int max) {
		if(given < min)
			return min;
		if(given > max)
			return max;
		return given;
	}

	public static String nbtToJson(CompoundTag nbt){
		return new Gson().toJson(getJsonValue((nbt)));
	}

	private static JsonElement getJsonValue(Tag value){
		if(value.getType() == CompoundTag.TYPE){
			CompoundTag nbt = (CompoundTag) value;
			JsonObject root = new JsonObject();
			for(String key : nbt.getAllKeys()){
				Tag n = nbt.get(key);
				JsonElement ele = getJsonValue(n);
				if(ele == null)
					continue;
				JsonObject ob = new JsonObject();
				ob.addProperty("type", n.getType().getName());
				ob.addProperty("type_id", n.getId());
				ob.addProperty("pretty_type", n.getType().getPrettyName());
				ob.add("value", ele);
				root.add(key, ob);
			}
			return root;
		}
		else if(value == StringTag.TYPE){
			return new JsonPrimitive(value.getAsString());
		}
		else if(value instanceof NumericTag){
			return new JsonPrimitive(((NumericTag)value).getAsNumber());
		}
		else if(value instanceof CollectionTag){
			JsonArray jsonValue = new JsonArray();
			for(Tag n : ((CollectionTag<Tag>)value)){
				jsonValue.add(getJsonValue(n));
			}
			return jsonValue;
		}
		return null;
	}

	public static CompoundTag jsonToNbt(String json){
		JsonObject ob = new Gson().fromJson(json, JsonObject.class);
		return toNbt(ob);
	}

	private static CompoundTag toNbt(JsonObject json){
		CompoundTag nbt = new CompoundTag();
		for(Map.Entry<String, JsonElement> entry : json.entrySet()){
			String key = entry.getKey();
			JsonObject ele = (JsonObject)entry.getValue();
			TagType<? extends Tag> type = stringToType(ele.get("type").getAsString());
			if(type == StringTag.TYPE){
				nbt.putString(key, ele.get("value").getAsString());
			}
			if(type == IntTag.TYPE) {
				nbt.putInt(key, ele.get("value").getAsInt());
			}
			if(type == ByteTag.TYPE) {
				nbt.putByte(key, ele.get("value").getAsByte());
			}
			if(type == LongTag.TYPE) {
				nbt.putLong(key, ele.get("value").getAsLong());
			}
			if(type == FloatTag.TYPE) {
				nbt.putFloat(key, ele.get("value").getAsFloat());
			}
			if(type == DoubleTag.TYPE) {
				nbt.putDouble(key, ele.get("value").getAsDouble());
			}
			if(type == ShortTag.TYPE) {
				nbt.putShort(key, ele.get("value").getAsShort());
			}
			if(type == CompoundTag.TYPE) {
				nbt.put(key, toNbt((JsonObject) ele.get("value")));
			}
			if(type == IntArrayTag.TYPE) {
				JsonArray array = (JsonArray) ele.get("value");
				nbt.put(key, new IntArrayTag(StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsInt).collect(Collectors.toList())));
			}
			if(type == ByteArrayTag.TYPE) {
				JsonArray array = (JsonArray) ele.get("value");
				nbt.put(key, new ByteArrayTag(StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsByte).collect(Collectors.toList())));
			}
			if(type == LongArrayTag.TYPE) {
				JsonArray array = (JsonArray) ele.get("value");
				nbt.put(key, new LongArrayTag(StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsLong).collect(Collectors.toList())));
			}
		}
		return nbt;
	}

	private static TagType<? extends Tag> stringToType(String type){
		if(type.equals(IntTag.TYPE.getName())){
			return IntTag.TYPE;
		}
		if(type.equals(ByteTag.TYPE.getName())){
			return ByteTag.TYPE;
		}
		if(type.equals(FloatTag.TYPE.getName())){
			return FloatTag.TYPE;
		}
		if(type.equals(LongTag.TYPE.getName())){
			return LongTag.TYPE;
		}
		if(type.equals(DoubleTag.TYPE.getName())){
			return DoubleTag.TYPE;
		}
		if(type.equals(ShortTag.TYPE.getName())){
			return ShortTag.TYPE;
		}
		if(type.equals(CompoundTag.TYPE.getName())){
			return CompoundTag.TYPE;
		}
		if(type.equals(IntArrayTag.TYPE.getName())){
			return IntArrayTag.TYPE;
		}
		if(type.equals(ByteArrayTag.TYPE.getName())){
			return ByteArrayTag.TYPE;
		}
		if(type.equals(LongArrayTag.TYPE.getName())){
			return LongArrayTag.TYPE;
		}
		return StringTag.TYPE;
	}

	public static boolean isValidPath(String s) {
		for(int i = 0; i < s.length(); ++i) {
			if (!ResourceLocation.validPathChar(s.charAt(i))) {
				return false;
			}
		}

		return true;
	}
}
