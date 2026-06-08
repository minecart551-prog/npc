package noppes.npcs.roles;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

public class JobChunkLoader extends JobInterface{
	
	private List<ChunkPos> chunks = new ArrayList<ChunkPos>();
	private int ticks = 20;
	private long playerLastSeen = -1;

	public JobChunkLoader(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		compound.putLong("ChunkPlayerLastSeen", playerLastSeen);
		return compound;
	}

	@Override
	public void load(CompoundTag compound) {
		playerLastSeen = compound.getLong("ChunkPlayerLastSeen");
	}

	@Override
	public boolean aiShouldExecute() {
		ticks--;
		if(ticks > 0)
			return false;
		ticks = 20;
		
		List players = npc.level().getEntitiesOfClass(Player.class, npc.getBoundingBox().inflate(48, 48, 48));
		if(!players.isEmpty())
			playerLastSeen = System.currentTimeMillis();

		if(playerLastSeen < 0){
			return false;
		}
		//unload after 10 min
		if(System.currentTimeMillis() > playerLastSeen + 600000){
			ChunkController.instance.unload((ServerLevel) npc.level(), npc.getUUID(), npc.chunkPosition().x, npc.chunkPosition().z);
			chunks.clear();
			playerLastSeen = -1;
			return false;
		}
		double x = npc.getX() / 16;
		double z = npc.getZ() / 16;

		List<ChunkPos> list = new ArrayList<ChunkPos>();
		list.add(new ChunkPos(Mth.floor(x), Mth.floor(z)));
		list.add(new ChunkPos(Mth.ceil(x), Mth.ceil(z)));
		list.add(new ChunkPos(Mth.floor(x), Mth.ceil(z)));
		list.add(new ChunkPos(Mth.ceil(x), Mth.floor(z)));

		for(ChunkPos chunk : list){
			if(!chunks.contains(chunk)){
				ChunkController.instance.load((ServerLevel) npc.level(), npc.getUUID(), chunk.x, chunk.z);
			}
			chunks.remove(chunk);
		}

		for(ChunkPos chunk : chunks){
			ChunkController.instance.unload((ServerLevel) npc.level(), npc.getUUID(), chunk.x, chunk.z);
		}

		this.chunks = list;
		return false;
	}
	
	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public void reset() {
		if(npc.level() instanceof ServerLevel){
			ChunkController.instance.unload((ServerLevel) npc.level(), npc.getUUID(), npc.chunkPosition().x, npc.chunkPosition().z);
			chunks.clear();
			playerLastSeen = 0;
		}
	}
	public void delete() {
	}

	@Override
	public int getType() {
		return JobType.CHUNKLOADER;
	}
}
