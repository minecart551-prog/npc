package noppes.npcs;


import net.minecraft.server.level.ServerLevel;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.shared.common.util.LogWriter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ForgeEventHandler {

    public static List<String> eventNames = new ArrayList<>();

//    private Event lastSeenEvent;
//
//    @SubscribeEvent
//    public void forgeEntity(Event event){
//        if(CustomNpcs.Server == null || !ScriptController.Instance.forgeScripts.isEnabled()) {
//            return;
//        }
//        if(lastSeenEvent==event) return;
//        lastSeenEvent = event;
//        try{
//            if(event instanceof net.minecraftforge.event.entity.player.PlayerEvent) {
//                net.minecraftforge.event.entity.player.PlayerEvent ev = (net.minecraftforge.event.entity.player.PlayerEvent) event;
//                if(ev.getEntity()==null || !(ev.getEntity().level() instanceof ServerLevel))
//                    return;
//            }
//            if(event instanceof EntityEvent) {
//                EntityEvent ev = (EntityEvent) event;
//                if(ev.getEntity() == null || !(ev.getEntity().level() instanceof ServerLevel))
//                    return;
//                EventHooks.onForgeEntityEvent(ev);
//                return;
//            }
//            if(event instanceof LevelEvent) {
//                LevelEvent ev = (LevelEvent) event;
//                if(!(ev.getLevel() instanceof ServerLevel)) {
//                    return;
//                }
//                EventHooks.onForgeLevelEvent(ev);
//                return;
//            }
//            if(event instanceof TickEvent) {
//                if(((TickEvent)event).side == LogicalSide.CLIENT)
//                    return;
//            }
//            EventHooks.onForgeEvent(new ForgeEvent(event), event);
//        }
//        catch(Throwable t){
//            LogWriter.error("Error in " + event.getClass().getName(), t);
//        }
//    }

    public static String getEventName(Class c){
        String eventName = c.getName();
        int i = eventName.lastIndexOf(".");
        return StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
    }
}
