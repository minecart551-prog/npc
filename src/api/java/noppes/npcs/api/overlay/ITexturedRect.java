package noppes.npcs.api.overlay;

public interface ITexturedRect extends IOverlayComponent
{
    String getTexture();
    
    ITexturedRect setTexture(final String texture);
    
    int getWidth();
    
    ITexturedRect setWidth(final int width);
    
    int getHeight();
    
    ITexturedRect setHeight(final int height);
    
    float[] getUV();

    ITexturedRect setUV(final float x1, final float y1, final float x2, final float y2);

    ITexturedRect setRGB(final float r, final float g, final float b, final float a);
    
    float[] getRGB();
    
    int getTextureX();
    
    int getTextureY();
    
    int getTextureMaxX();
    
    int getTextureMaxY();

    ITexturedRect setTextureOffset(final int offsetX, final int offsetY);

    ITexturedRect setTextureMaxSize(final int textureMaxX, final int textureMaxY);
}
