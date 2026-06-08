package noppes.npcs.api.event;

public class Event {
    boolean canceled = false;
    public boolean isCanceled(){
        return canceled;
    }

    public void setCanceled(boolean val){
        canceled = val;
    }
}
