package noppes.npcs.shared.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import noppes.npcs.CustomNpcs;
import noppes.npcs.shared.common.util.LRUHashMap;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.*;

public class TrueTypeFont {
	enum GlyphType{ NORMAL, COLOR, RANDOM, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET, OTHER }
	
	private final static int MaxWidth = 512;
    private static final List<Font> allFonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
	private List<Font> usedFonts = new ArrayList<Font>();
    
	private LinkedHashMap<String, GlyphCache> textcache = new LRUHashMap<String, GlyphCache>(100);
	private Map<Character, Glyph> glyphcache = new HashMap<Character, Glyph>();
	private List<TextureCache> textures = new ArrayList<TextureCache>();
    
	private Font font;
	private int lineHeight = 1;
	
	private Graphics2D globalG = (Graphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
		
	public float scale = 1;
	
	private int specialChar = 167;
	
	public TrueTypeFont(Font font, float scale){
		this.font = font;
		this.scale = scale;
		globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		lineHeight = globalG.getFontMetrics(font).getHeight();
	}
	
	public TrueTypeFont(ResourceLocation resource, int fontSize, float scale) throws IOException, FontFormatException{
		Resource r = Minecraft.getInstance().getResourceManager().getResource(resource).orElse(null);
		if(r!=null) {
			try {
				InputStream stream = r.open();
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
				ge.registerFont(font);
				this.font = font.deriveFont(Font.PLAIN, fontSize);
				this.scale = scale;
				globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				lineHeight = globalG.getFontMetrics(font).getHeight();
			} catch (IOException e) {
				throw e;
			}
		}
	}
	
	public void setSpecial(char c){
		specialChar = c;
	}
	
	public void draw(PoseStack posestack, String text, float x, float y, int color){
		GlyphCache cache = getOrCreateCache(text);

        int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;

		RenderSystem.enableBlend();
		posestack.pushPose();
		posestack.translate(x, y, 0);
		posestack.scale(scale, scale, 1);
		int rr = r;
		int gg = g;
		int bb = b;
		float i = 0;
		for(Glyph gl : cache.glyphs){
			if(gl.type != GlyphType.NORMAL){
				if(gl.type == GlyphType.RESET){
					rr = r;
					gg = g;
					bb = b;
				}
				else if(gl.type == GlyphType.COLOR){
					rr = gl.color >> 16 & 255;
					gg = gl.color >> 8 & 255;
					bb = gl.color & 255;
				}
			}
			else{
				RenderSystem.setShaderTexture(0, gl.texture);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		        fillGradient(posestack.last().pose(), i, 0, gl.x * textureScale(), gl.y * textureScale(), gl.width * textureScale(), gl.height * textureScale(), rr, gg, bb);
		        i += gl.width * textureScale();
			}
		}
		RenderSystem.disableBlend();
		posestack.popPose();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	public void fillGradient(Matrix4f m, float x, float y, float textureX, float textureY, float width, float height, int r, int g, int b){
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		int zLevel = 0;
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		BufferBuilder tessellator = Tesselator.getInstance().getBuilder();
		tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		//tessellator.noColor();
		tessellator.vertex(m, x, y + height, zLevel).uv(textureX * f, (textureY + height) * f1).color(r, g, b, 255).endVertex();
		tessellator.vertex(m, x + width, y + height, zLevel).uv((textureX + width) * f, (textureY + height) * f1).color(r, g, b, 255).endVertex();
		tessellator.vertex(m, x + width, y, zLevel).uv((textureX + width) * f, textureY * f1).color(r, g, b, 255).endVertex();
		tessellator.vertex(m, x, y, zLevel).uv(textureX * f, textureY * f1).color(r, g, b, 255).endVertex();
		BufferUploader.drawWithShader(tessellator.end());
	}
	
	private GlyphCache getOrCreateCache(String text){
		GlyphCache cache = textcache.get(text);
		if(cache != null)
			return cache;
		cache = new GlyphCache();
		for(int i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if(c == specialChar && i + 1 < text.length()){
				char next = text.toLowerCase(Locale.ENGLISH).charAt(i + 1);
                int index = "0123456789abcdefklmnor".indexOf(next);
                if(index >= 0){
                	Glyph g = new Glyph();

                    if (index < 16){
                    	g.type = GlyphType.COLOR; 
                    	g.color = ChatFormatting.getByCode(next).getColor();
                    }
                    else if(index == 16){
                    	g.type = GlyphType.RANDOM;
                    }
                    else if(index == 17){
                    	g.type = GlyphType.BOLD;
                    }
                    else if(index == 18){
                    	g.type = GlyphType.STRIKETHROUGH;
                    }
                    else if(index == 19){
                    	g.type = GlyphType.UNDERLINE;
                    }
                    else if(index == 20){
                    	g.type = GlyphType.ITALIC;
                    }
                    else{
                    	g.type = GlyphType.RESET;
                    }
        			cache.glyphs.add(g);
                	i++;
                	continue;
                }
			}
			Glyph g = getOrCreateGlyph(c);
			cache.glyphs.add(g);
			cache.width += g.width;
			cache.height = Math.max(cache.height, g.height);
		}
		textcache.put(text, cache);
		return cache;
	}
	
	private Glyph getOrCreateGlyph(char c){
		Glyph g = glyphcache.get(c);
		if(g != null)
			return g;
		
		TextureCache cache = getCurrentTexture();
		Font font = getFontForChar(c);
		FontMetrics metrics = globalG.getFontMetrics(font);
		g = new Glyph();
		g.width = Math.max(metrics.charWidth(c), 1);
		g.height = Math.max(metrics.getHeight(), 1);
		
		if(cache.x + g.width >= MaxWidth){
			cache.x = 0;
			cache.y += lineHeight + 1;
			if(cache.y >= MaxWidth){
				cache.full = true;
				cache = getCurrentTexture();
			}
		}
		
		g.x = cache.x;
		g.y = cache.y;		
		cache.x += g.width + 3;
		lineHeight = Math.max(lineHeight, g.height);

		cache.g.setFont(font);
		cache.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		cache.g.drawString(c + "", g.x, g.y + metrics.getAscent());
		g.texture = cache.textureId;
		int[] aint = new int[MaxWidth * MaxWidth];
		cache.bufferedImage.getRGB(0, 0, MaxWidth, MaxWidth, aint, 0, MaxWidth);
		IntBuffer intbuffer = BufferUtils.createIntBuffer(MaxWidth * MaxWidth);
		intbuffer.put(aint);
		intbuffer.flip();

		//RenderSystem.activeTexture(33984);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.bindTextureForSetup(cache.textureId);

		GL11.glPixelStorei(3312, 0);
		GL11.glPixelStorei(3313, 0);
		GL11.glPixelStorei(3314, 0);
		GL11.glPixelStorei(3315, 0);
		GL11.glPixelStorei(3316, 0);
		GL11.glPixelStorei(3317, 4);
		GL11.glTexImage2D(3553, 0, 6408, MaxWidth, MaxWidth, 0, 32993, 33639, intbuffer);
		GL11.glTexParameteri(3553, 10240, 9728);
		GL11.glTexParameteri(3553, 10241, 9729);

		//CTextureUtil.uploadTextureImage(cache.textureId, bufferedImage);
        glyphcache.put(c,  g);
        
		return g;
	}
	
	private TextureCache getCurrentTexture(){
		TextureCache cache = null;
		for(TextureCache t : textures){
			if(!t.full){
				cache = t;
				break;
			}
		}
		if(cache == null){
			textures.add(cache = new TextureCache());
		}
		return cache;
	}

	
	private Font getFontForChar(char c){
		if(font.canDisplay(c))
			return font;

		for(Font f : usedFonts){
			if(f.canDisplay(c)){
				return f;
			}
		}
		
		Font fa = new Font("Arial Unicode MS", Font.PLAIN, this.font.getSize());
		if(fa.canDisplay(c))
			return fa;
		
		for(Font f : allFonts){
			if(f.canDisplay(c)){
				usedFonts.add(f = f.deriveFont(Font.PLAIN, this.font.getSize()));
				return f;
			}
		}
		return font;
	}
    
    public int width(String text){
		GlyphCache cache = getOrCreateCache(text);
		return (int) (cache.width * scale * textureScale());
    }
    
    public int height(String text){
    	if(text == null || text.trim().isEmpty())
    		return (int) (lineHeight * scale * textureScale());
		GlyphCache cache = getOrCreateCache(text);
		return Math.max(1,(int) (cache.height * scale * textureScale()));
    }
    
    private float textureScale(){
    	return 0.5f;
    }
	
	public void dispose(){
		for(TextureCache cache : textures){
			RenderSystem.deleteTexture(cache.textureId);
		}
		textcache.clear();
	}
	
	class TextureCache{
		int x, y;	
		int textureId = GL11.glGenTextures();
		BufferedImage bufferedImage = new BufferedImage(MaxWidth, MaxWidth, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		boolean full;
	}
	
	class Glyph{
		GlyphType type = GlyphType.NORMAL;
		int color = -1;
		int x, y, height, width, texture;
	}
	
	class GlyphCache{
		public int width, height;
		List<Glyph> glyphs = new ArrayList<Glyph>();
	}

	public String getFontName() {
		return font.getFontName();
	}
}
