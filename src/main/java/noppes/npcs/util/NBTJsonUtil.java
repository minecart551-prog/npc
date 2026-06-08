package noppes.npcs.util;

import com.google.common.io.Files;
import net.minecraft.nbt.*;
import noppes.npcs.mixin.ListNBTMixin;
import org.apache.commons.io.Charsets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class NBTJsonUtil {
	public static String Convert(CompoundTag compound){
		List<JsonLine> list = new ArrayList<JsonLine>();
		JsonLine line = ReadTag("", compound, list);
		line.removeComma();
		return ConvertList(list);
	}

	public static CompoundTag Convert(String json) throws JsonException{
		json = json.trim();
		JsonFile file = new JsonFile(json);
		if(!json.startsWith("{") || !json.endsWith("}"))
			throw new JsonException("Not properly incapsulated between { }", file);
		
		CompoundTag compound = new CompoundTag();
		FillCompound(compound, file);
		return compound;
	}
	
	public static void FillCompound(CompoundTag compound, JsonFile json) throws JsonException{
		if(json.startsWith("{") || json.startsWith(","))
			json.cut(1);
		if(json.startsWith("}"))
			return;
		
		int index = json.keyIndex();
		if(index < 1)
			throw new JsonException("Expected key after ," ,json);
		
		String key = json.substring(0, index);
		json.cut(index + 1);
		
		Tag base = ReadValue(json);
		
		if(base == null)
			base = StringTag.valueOf("");

		
		if(key.startsWith("\""))
			key = key.substring(1);
		if(key.endsWith("\""))
			key = key.substring(0, key.length() - 1);
		
		compound.put(key, base);
		if(json.startsWith(","))
			FillCompound(compound, json);
		
	}
	
	public static Tag ReadValue(JsonFile json) throws JsonException{
		if(json.startsWith("{")){
			CompoundTag compound = new CompoundTag();
			FillCompound(compound, json);
			if(!json.startsWith("}")){
				throw new JsonException("Expected }", json);
			}
			json.cut(1);
			
			return compound;
		}
		if(json.startsWith("[")){
			json.cut(1);
			ListTag list = new ListTag();
			if(json.startsWith("B;") || json.startsWith("I;") || json.startsWith("L;"))
				json.cut(2);
			
			Tag value = ReadValue(json);
			while(value != null){
				list.add(value);
				if(!json.startsWith(","))
					break;
				json.cut(1);
				value = ReadValue(json);
			}
			if(!json.startsWith("]")){
				throw new JsonException("Expected ]", json);
			}
			json.cut(1);

			if(list.getElementType() == 3){
				int[] arr = new int[list.size()];
				for(int i = 0; list.size() > 0 ; i++){
					arr[i] = ((IntTag)list.remove(0)).getAsInt();
				}
				return new IntArrayTag(arr);
			}
			if(list.getElementType() == 1){
				byte[] arr = new byte[list.size()];
				for(int i = 0; list.size() > 0 ; i++){
					arr[i] = ((ByteTag)list.remove(0)).getAsByte();
				}
				return new ByteArrayTag(arr);
			}
			if(list.getElementType() == 4){
				long[] arr = new long[list.size()];
				for(int i = 0; list.size() > 0 ; i++){
					arr[i] = ((LongTag)list.remove(0)).getAsByte();
				}
				return new LongArrayTag(arr);
			}
			
			return list;
		}
		if(json.startsWith("\"")){
			json.cut(1);
			String s = "";
			boolean ignore = false;
			while(!json.startsWith("\"") || ignore){
				String cut = json.cutDirty(1);
				ignore = cut.equals("\\");
				s += cut;
			}
			json.cut(1);
			return StringTag.valueOf(s.replace("\\\\", "\\").replace("\\\"", "\""));
		}
		String s = "";
		while(!json.startsWith(",", "]", "}")){
			s += json.cut(1);
		}
		s = s.trim().toLowerCase();
		if(s.isEmpty())
			return null;
		try{
			if(s.endsWith("d")){
				return DoubleTag.valueOf(Double.parseDouble(s.substring(0, s.length() - 1)));
			}
			if(s.endsWith("f")){
				return FloatTag.valueOf(Float.parseFloat(s.substring(0, s.length() - 1)));
			}
			if(s.endsWith("b")){
				return ByteTag.valueOf(Byte.parseByte(s.substring(0, s.length() - 1)));
			}
			if(s.endsWith("s")){
				return ShortTag.valueOf(Short.parseShort(s.substring(0, s.length() - 1)));
			}
			if(s.endsWith("l")){
				return LongTag.valueOf(Long.parseLong(s.substring(0, s.length() - 1)));
			}
			if(s.contains("."))
				return DoubleTag.valueOf(Double.parseDouble(s));
			else
				return IntTag.valueOf(Integer.parseInt(s));
		}
		catch(NumberFormatException ex){
			throw new JsonException("Unable to convert: "+ s +" to a number", json);
		}
	}
	
	private static JsonLine ReadTag(String name, Tag base, List<JsonLine> list){
		if(!name.isEmpty()){
			name = "\"" + name + "\": ";
		}
//		if(base.getId() == 8){//StringTag
//			String data = base.getAsString();
//			data = data.replace("\"", "\\\""); //replace " with \"
//			list.add(new JsonLine(name + "\"" + data + "\""));
//		}
		if(base.getId() == 9){//ListTag
			list.add(new JsonLine(name + "["));
			ListTag tags = (ListTag) base;
			JsonLine line = null;
			List<Tag> data = ((ListNBTMixin)tags).getList();
			for(Tag b : data)
				line = ReadTag("", b, list);
			if(line != null)
				line.removeComma();
			list.add(new JsonLine("]"));
		}
		else if(base.getId() == 10){ //CompoundTag
			list.add(new JsonLine(name + "{"));
			CompoundTag compound = (CompoundTag)base;
			JsonLine line = null;
			for(Object key : compound.getAllKeys())
				line = ReadTag(key.toString(), compound.get(key.toString()), list);
			
			if(line != null)
				line.removeComma();
				
			list.add(new JsonLine("}"));
		}
		else if(base.getId() == 11){//ListTag
			list.add(new JsonLine(name + base.toString().replaceFirst(",]", "]")));
		}
		else if(base.getId() == 8){//StringTag
			list.add(new JsonLine(name + quoteAndEscape(base.getAsString())));
		}
		else{
			list.add(new JsonLine(name + base));
		}
		JsonLine line = list.get(list.size() - 1);
		line.line += ",";
		return line;
	}

	private static String ConvertList(List<JsonLine> list) {
		String json = "";
		int tab = 0;
		for(JsonLine tag : list){
			if(tag.reduceTab())
				tab--;
			for(int i = 0; i < tab; i++){
				json += "    ";
			}
			json += tag + "\n";

			if(tag.increaseTab())
				tab++;
		}
		return json;
	}
	static class JsonLine{
		private String line;
		public JsonLine(String line){
			this.line = line;
		}
		
		public void removeComma(){
			if(line.endsWith(","))
				line = line.substring(0, line.length() - 1);
		}
		
		public boolean reduceTab(){
			int length = line.length();
			return length == 1 && (line.endsWith("}") || line.endsWith("]"))
					|| length == 2 && (line.endsWith("},") ||line.endsWith("],"));
		}
		
		public boolean increaseTab(){
			return line.endsWith("{") || line.endsWith("[");
		}
		
		@Override
		public String toString(){
			return line;
		}
	}
	static class JsonFile{
		private String original;
		private String text;
		
		public JsonFile(String text){
			this.text = text;
			this.original = text;
		}

		public int keyIndex() {
			boolean hasQuote = false;
			for(int i = 0; i < text.length(); i++){
				char c = text.charAt(i);
				if(i == 0 && c == '"'){
					hasQuote = true;
				}
				else if(hasQuote && c == '"'){
					hasQuote = false;
				}
				if(!hasQuote && c == ':'){
					return i;
				}
			}
			return -1;
		}

		public String cutDirty(int i) {
			String s = text.substring(0, i);
			text = text.substring(i);
			return s;
		}

		public String cut(int i) {
			String s = text.substring(0, i);
			text = text.substring(i).trim();
			return s;
		}

		public String substring(int beginIndex, int endIndex) {
			return text.substring(beginIndex, endIndex);
		}

		public int indexOf(String s) {
			return text.indexOf(s);
		}
		
		public String getCurrentPos(){
			int lengthOr = original.length();
			int lengthCur = text.length();
			int currentPos = lengthOr - lengthCur;
			String done = original.substring(0, currentPos);
			String[] lines = done.split("\r\n|\r|\n");
			
			int pos = 0;
			String line = "";
			if(lines.length > 0){
				pos = lines[lines.length - 1].length();
				line = original.split("\r\n|\r|\n")[lines.length - 1].trim();
			}
			
			return "Line: " + lines.length + ", Pos: " + pos + ", Text: " + line;
		}

		public boolean startsWith(String... ss) {
			for(String s : ss)
				if(text.startsWith(s))
					return true;
			return false;
		}
		public boolean endsWith(String s) {
			return text.endsWith(s);
		}
	}

	
	public static CompoundTag LoadFile(File file) throws IOException, JsonException {
		return Convert(Files.toString(file, Charsets.UTF_8));
	}
	
	public static void SaveFile(File file, CompoundTag compound) throws IOException, JsonException {
		String json = Convert(compound);
		OutputStreamWriter writer = null;
		try{
			writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
			writer.write(json);
			
		}
		finally{
			if(writer != null)
				writer.close();
		}
	}
	public static class JsonException extends Exception{
		public JsonException(String message, JsonFile json){
			super(message + ": " + json.getCurrentPos());
		}
	}
	
	public static void main(String[] args){
		CompoundTag comp = new CompoundTag();
		CompoundTag comp2 = new CompoundTag();
		comp2.putByteArray("test", new byte[]{0,0,1,1,0});
		comp.put("comp", comp2);
		System.out.println(Convert(comp));
	}

	public static String quoteAndEscape(String p_193588_0_)
	{
		StringBuilder stringbuilder = new StringBuilder("\"");

		for (int i = 0; i < p_193588_0_.length(); ++i)
		{
			char c0 = p_193588_0_.charAt(i);

			if (c0 == '\\' || c0 == '"')
			{
				stringbuilder.append('\\');
			}

			stringbuilder.append(c0);
		}

		return stringbuilder.append('"').toString();
	}
}
