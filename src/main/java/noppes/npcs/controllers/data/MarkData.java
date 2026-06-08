package noppes.npcs.controllers.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.entity.data.IEntityPersistentData;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketMarkData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkData {

	private static final String NBTKEY = "cnpcmarkdata";
	private LivingEntity entity;

	public List<Mark> marks = new ArrayList<Mark>();

	private static Map<Integer, MarkData> dataMap = new HashMap<>();

	public void setNBT(CompoundTag compound){
		List<Mark> marks = new ArrayList<Mark>();
		ListTag list = compound.getList("marks", 10);
		for(int i = 0; i < list.size(); i++){
			CompoundTag c = list.getCompound(i);
			Mark m = new Mark();
			m.type = c.getInt("type");
			m.color = c.getInt("color");
			m.availability.load(c.getCompound("availability"));
			marks.add(m);
		}
		this.marks = marks;
	}
	
	public CompoundTag getNBT() {
		CompoundTag compound = new CompoundTag();
		ListTag list = new ListTag();
		for(Mark m : marks){
			CompoundTag c = new CompoundTag();
			c.putInt("type", m.type);
			c.putInt("color", m.color);
			c.put("availability", m.availability.save(new CompoundTag()));
			list.add(c);
		}
		compound.put("marks", list);
		return compound;
	}

	public void save() {
		((IEntityPersistentData)entity).getPersistentData().put(NBTKEY, getNBT());
	}

	public IMark addMark(int type) {
		Mark m = new Mark();
		m.type = type;
		marks.add(m);
		if(!entity.level().isClientSide)
			syncClients();
		return m;
	}

	public IMark addMark(int type, int color) {
		Mark m = new Mark();
		m.type = type;
		m.color = color;
		marks.add(m);
		if(!entity.level().isClientSide)
			syncClients();
		return m;
	}

	public static MarkData get(LivingEntity entity) {
		MarkData data = dataMap.computeIfAbsent(entity.getId(), (i)->new MarkData());
		if(data.entity == null){
			data.entity = entity;
			data.setNBT(((IEntityPersistentData)entity).getPersistentData().getCompound(NBTKEY));
		}
		return data;
	}

	public void syncClients() {
		Packets.sendAll( new PacketMarkData(entity.getId(), getNBT()));
	}
	
	public class Mark implements IMark{	
		public int type = MarkType.NONE;
		
		public Availability availability = new Availability();
		
		public int color = 0xFFED51;

		@Override
		public IAvailability getAvailability() {
			return availability;
		}

		@Override
		public int getColor() {
			return color;
		}

		@Override
		public void setColor(int color) {
			this.color = color;
		}

		@Override
		public int getType() {
			return type;
		}

		@Override
		public void setType(int type) {
			this.type = type;
		}

		@Override
		public void update() {
			MarkData.this.syncClients();
		}
	}
}
