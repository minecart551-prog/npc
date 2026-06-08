package noppes.npcs.roles;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import noppes.npcs.CustomNpcs;
import noppes.npcs.mixin.MusicManagerMixin;
import noppes.npcs.shared.client.util.NoppesStringUtils;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobBard;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.entity.EntityNPCInterface;



public class JobBard extends JobInterface implements IJobBard{
	public int minRange = 2;
	public int maxRange = 64;

	public boolean isStreamer = true;
	public boolean isLooping = false;
	public boolean hasOffRange = true;

	public String song = "";

	public JobBard(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public CompoundTag save(CompoundTag nbttagcompound) {
		nbttagcompound.putString("BardSong", song);
		nbttagcompound.putInt("BardMinRange", minRange);
		nbttagcompound.putInt("BardMaxRange", maxRange);
		nbttagcompound.putBoolean("BardStreamer", isStreamer);
		nbttagcompound.putBoolean("BardLoops", isLooping);
		nbttagcompound.putBoolean("BardHasOff", hasOffRange);
		
		return nbttagcompound;
	}

	@Override
	public void load(CompoundTag nbttagcompound) {
		song = nbttagcompound.getString("BardSong");
		minRange = nbttagcompound.getInt("BardMinRange");
		maxRange = nbttagcompound.getInt("BardMaxRange");
		isStreamer = nbttagcompound.getBoolean("BardStreamer");
		isLooping = nbttagcompound.getBoolean("BardLoops");
		hasOffRange = nbttagcompound.getBoolean("BardHasOff");
	}

	public void aiStep() {
		if(!npc.isClientSide() || song.isEmpty())
			return;
		
		if(!MusicController.Instance.isPlaying(song)){
			List<Player> list = npc.level().getEntitiesOfClass(Player.class, npc.getBoundingBox().inflate(minRange, minRange/2, minRange));
			if(!list.contains(CustomNpcs.proxy.getPlayer()))
				return;
			if(isStreamer)
				MusicController.Instance.playStreaming(song, npc, isLooping);
			else
				MusicController.Instance.playMusic(song, npc, isLooping);
		}
		else if(MusicController.Instance.playingEntity != npc){
			Player player = CustomNpcs.proxy.getPlayer();
			if(npc.distanceToSqr(player) < MusicController.Instance.playingEntity.distanceToSqr(player)){
				MusicController.Instance.playingEntity = npc;
			}
			
		}
		else if(hasOffRange){
			List<Player> list = npc.level().getEntitiesOfClass(Player.class, npc.getBoundingBox().inflate(maxRange, maxRange/2, maxRange));
			if(!list.contains(CustomNpcs.proxy.getPlayer()))
				MusicController.Instance.stopMusic();
		}
		
		if(MusicController.Instance.isPlaying(song)) {
			((MusicManagerMixin)Minecraft.getInstance().getMusicManager()).nextSongDelay(12000);
		}
		
	}

	@Override
	public void killed() {
		delete();
	}

	@Override
	public void delete() {
		if(npc.level().isClientSide && hasOffRange){
			if(MusicController.Instance.isPlaying(song)){
				MusicController.Instance.stopMusic();
			}
		}
	}

	@Override
	public String getSong(){
		return NoppesStringUtils.cleanResource(this.song);
	}

	@Override
	public void setSong(String song){
		this.song = NoppesStringUtils.cleanResource(song);
		npc.updateClient = true;
	}

	@Override
	public int getType() {
		return JobType.BARD;
	}
}
