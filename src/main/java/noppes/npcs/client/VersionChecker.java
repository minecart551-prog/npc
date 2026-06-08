package noppes.npcs.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class VersionChecker extends Thread{
	
	public void run(){
		String name = '\u00A7'+ "2CustomNpcs" + '\u00A7' + "f";
		String link = '\u00A7'+"9"+'\u00A7' + "nClick here"; 
		String text =  name +" installed. For more info " + link;
		
        Player player;
		try{
			player = Minecraft.getInstance().player;
		}
		catch(NoSuchMethodError e){
			return;
		}
        while((player = Minecraft.getInstance().player) == null){
        	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

		MutableComponent message = Component.translatable(text);
		message.setStyle(message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://www.kodevelopment.nl/minecraft/customnpcs/")));
        player.sendSystemMessage(message);
	}
}
