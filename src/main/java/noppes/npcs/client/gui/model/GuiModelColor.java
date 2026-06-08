package noppes.npcs.client.gui.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.client.gui.custom.GuiCreationNewParts;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.components.CustomGuiButton;
import noppes.npcs.client.gui.custom.components.CustomGuiTextField;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.shared.client.gui.components.GuiBasic;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.components.GuiTextFieldNop;
import noppes.npcs.shared.client.gui.listeners.ITextfieldListener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;




public class GuiModelColor extends GuiCustom implements ITextfieldListener{

	private GuiCustom parent;
	private final static ResourceLocation colorPicker = new ResourceLocation("moreplayermodels:textures/gui/color.png");
	private final static ResourceLocation colorgui = new ResourceLocation("moreplayermodels:textures/gui/color_gui.png");
	
	private int colorX, colorY;

	public int color;

	private CustomGuiTextField textfield;
	private CustomGuiButton button;

	private ColorCallback callback;
	public GuiModelColor(GuiCustom parent, int c, ColorCallback callback){
		super(parent.getMenu(), parent.inv, Component.empty());

		this.parent = parent;
		this.callback = callback;
		imageHeight = 170;
		imageWidth = 130;
		//closeOnEsc = false;
		this.color = c;

		CustomGuiTexturedRectWrapper bg = new CustomGuiTexturedRectWrapper();
		bg.setTexture("customnpcs:textures/gui/components.png").setSize(imageWidth, imageHeight);
		bg.setTextureOffset(0, 0).setRepeatingTexture(64, 64, 4);
		background = new CustomGuiTexturedRect(this, bg);

		textfield = new CustomGuiTextField(this, (CustomGuiTextFieldWrapper) new CustomGuiTextFieldWrapper(24, 35, 25, 60, 20).setCharacterType(2).setColor(color).setText(getColor()).setOnChange((gui, text) -> {
			color = Integer.parseInt(text.getText(),16);
			callback.color(color);
			textfield.setTextColor(color);
		}));
		button = new CustomGuiButton(this, (CustomGuiButtonWrapper)new CustomGuiButtonWrapper(66, "x", 107, 8, 20, 20).setOnPress((gui, button) -> {
			parent.subgui = null;
		}).setDisablePackets());
		this.minecraft = Minecraft.getInstance();
	}

    @Override
    public void init() {
    	super.init();
		this.add(textfield);
		this.add(button);
		background.setTexture(colorgui);
    	colorX = leftPos + 4;
    	colorY = topPos + 50;
    }

    @Override
    public void render(GuiGraphics graphics, int par1, int limbSwingAmount, float par3){
    	super.render(graphics, par1, limbSwingAmount, par3);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, colorPicker);
        graphics.blit(colorPicker, colorX, colorY, 0, 0, 120, 120);
    }
    
	@Override
    public boolean mouseClicked(double i, double j, int k){
		super.mouseClicked(i, j, k);
		if( i < colorX  || i > colorX + 120 || j < colorY || j > colorY + 120)
			return false;
		Resource resource = this.minecraft.getResourceManager().getResource(colorPicker).orElse(null);
		if(resource!=null) {
			try (InputStream stream = resource.open()) {
				BufferedImage bufferedimage = ImageIO.read(stream);
				int color = bufferedimage.getRGB((int) (i - leftPos - 4) * 4, (int) (j - topPos - 50) * 4) & 16777215;
				if (color != 0) {
					this.color = color;
					callback.color(color);
					textfield.setTextColor(color);
					textfield.setValue(getColor());
				}

			} catch (IOException e) {
			}
		}
		return true;
    }

	@Override
	public void unFocused(GuiTextFieldNop textfield) {
		try{
			color = Integer.parseInt(textfield.getValue(),16);
		}
		catch(NumberFormatException e){
			color = 0;
		}
		callback.color(color);
		textfield.setTextColor(color);
	}
	
	public String getColor() {
		String str = Integer.toHexString(color);

    	while(str.length() < 6)
    		str = "0" + str;
    	
    	return str;
	}

	public interface ColorCallback{
	    void color(int color);
	}
}
