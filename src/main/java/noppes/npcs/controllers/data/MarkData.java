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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MarkData {

	private static final String NBTKEY = "cnpcmarkdata";
	private WeakReference<LivingEntity> entityRef;

	public List<Mark> marks = new ArrayList<Mark>();

	/**
	 * Keyed weakly by the entity itself instead of by the entity id.
	 *
	 * Entity ids come from a static counter and are never reused, so an entry could only
	 * ever be looked up again through a live entity. The old id-keyed map therefore grew by
	 * one entry (and one hard reference to the entity, and to everything the entity owns:
	 * script container, script engine, ...) for every entity that ever spawned, and never
	 * shrank. Keying weakly keeps the exact same lookup semantics while letting the entry
	 * disappear as soon as the entity is garbage collected (chunk unload, discard, death).
	 *
	 * The value must not reference the key strongly, hence {@link #entityRef}.
	 */
	private static final Map<LivingEntity, MarkData> dataMap =
			Collections.synchronizedMap(new WeakHashMap<LivingEntity, MarkData>());

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
		LivingEntity e = entity();
		if(e == null)
			return;
		((IEntityPersistentData)e).getPersistentData().put(NBTKEY, getNBT());
	}

	public IMark addMark(int type) {
		Mark m = new Mark();
		m.type = type;
		marks.add(m);
		syncIfServer();
		return m;
	}

	public IMark addMark(int type, int color) {
		Mark m = new Mark();
		m.type = type;
		m.color = color;
		marks.add(m);
		syncIfServer();
		return m;
	}

	public static MarkData get(LivingEntity entity) {
		MarkData data;
		// Synchronised on the map wrapper so the weak cleanup can never race with a lookup.
		synchronized (dataMap) {
			data = dataMap.get(entity);
			if (data == null) {
				data = new MarkData();
				dataMap.put(entity, data);
			}
		}
		if (data.entity() == null) {
			data.entityRef = new WeakReference<LivingEntity>(entity);
			data.setNBT(((IEntityPersistentData)entity).getPersistentData().getCompound(NBTKEY));
		}
		return data;
	}

	/**
	 * @return the owning entity, or null when it has already been garbage collected.
	 */
	private LivingEntity entity() {
		return entityRef == null ? null : entityRef.get();
	}

	private void syncIfServer() {
		LivingEntity e = entity();
		if(e != null && !e.level().isClientSide)
			syncClients();
	}

	public void syncClients() {
		LivingEntity e = entity();
		if(e == null)
			return;
		Packets.sendAll( new PacketMarkData(e.getId(), getNBT()));
	}

	/**
	 * Number of live entries, for debugging/diagnostics.
	 */
	public static int size() {
		return dataMap.size();
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
