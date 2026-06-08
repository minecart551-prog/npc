package noppes.npcs.api.overlay;

public interface ILabel extends IOverlayComponent
{
    String getText();
    
    ILabel setText(final String text);
    
    ILabel setCentered(final boolean centered);
    
    boolean isCentered();
    
    float getScale();
    
    void setScale(final float scale);
}
