package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;


public class MusicController {
	//public TreeSet<String> sounds = new TreeSet<String>();
	//public TreeSet<String> music = new TreeSet<String>();
	
	public static MusicController Instance;
    public SoundInstance playing;
    public ResourceLocation playingResource;
    public Entity playingEntity;

	//private SoundPoolEntry musicPool;
	
	public MusicController(){
		Instance = this;
	}


	public void stopMusic(){
		SoundManager handler = Minecraft.getInstance().getSoundManager();
		if(playing != null)
			handler.stop(playing);
		handler.stop(null, SoundSource.MUSIC);
		handler.stop(null, SoundSource.AMBIENT);
		handler.stop(null, SoundSource.RECORDS);
		playingResource = null;
		playingEntity = null;
		playing = null;
	}
	
	public void playStreaming(String music, Entity entity, boolean isLooping){
		if(isPlaying(music)){			
			return;
		}
		stopMusic();
		playingEntity = entity;
		playingResource = new ResourceLocation(music);
		SoundManager handler = Minecraft.getInstance().getSoundManager();
        playing = new SimpleSoundInstance(playingResource, SoundSource.RECORDS, 4.0F, 1.0F, SoundInstance.createUnseededRandom(), isLooping, 0, SoundInstance.Attenuation.LINEAR, entity.getX(), entity.getY(), entity.getZ(), false);

        handler.play(playing);
	}

	public void playMusic(String music, Entity entity, boolean isLooping) {
		if(isPlaying(music))
			return;
		stopMusic();
		playingResource = new ResourceLocation(music);

		playingEntity = entity;

		SoundManager handler = Minecraft.getInstance().getSoundManager();
        playing = new SimpleSoundInstance(playingResource, SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), isLooping, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, false);
        handler.play(playing);
	}
	

	public boolean isPlaying(String music) {
		ResourceLocation resource = new ResourceLocation(music);
		if(playingResource == null || !playingResource.equals(resource)){
			return false;
		}
    	return Minecraft.getInstance().getSoundManager().isActive(playing);
	}
	
	public void playSound(SoundSource cat, String music, BlockPos pos, float volume, float pitch) {
		SimpleSoundInstance rec = new SimpleSoundInstance(new ResourceLocation(music), cat, volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f, false);
		Minecraft.getInstance().getSoundManager().play(rec);
	}
}
