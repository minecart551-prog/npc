package noppes.npcs.client.gui.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketPlayerTransport;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.components.GuiLabel;
import noppes.npcs.shared.client.gui.components.GuiButtonNop;
import noppes.npcs.shared.client.gui.listeners.IScrollData;
import noppes.npcs.shared.client.gui.listeners.ITopButtonListener;

import java.util.Map;
import java.util.Vector;



public class GuiTransportSelection extends GuiNPCInterface implements ITopButtonListener, IScrollData {

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/smallbg.png");
    protected int imageWidth;
    protected int guiLeft;
    protected int guiTop;
    
    private GuiCustomScrollNop scroll;
    
	public GuiTransportSelection(EntityNPCInterface npc) {
		super(npc);
        imageWidth = 176;
        this.drawDefaultBackground = false;
        title = "";
	}

	@Override
    public void init() {
        super.init();
        guiLeft = (width - imageWidth) / 2;
        guiTop = (height - 222) / 2;
        //String name = "Location: " + npc.getDataWatcher().getWatchableObjectString(11);
        String name = "";
        addLabel(new GuiLabel(0,name, guiLeft + (this.imageWidth - this.font.width(name))/2, guiTop + 10));
        addButton(new GuiButtonNop(this, 0, guiLeft+ 10, guiTop + 192,156,20, I18n.get("transporter.travel")));
        if(scroll == null)
        	scroll = new GuiCustomScrollNop(this,0);
        scroll.setSize(156, 165);
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = guiTop + 20;
        addScroll(scroll);
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    	renderBackground(graphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resource);
        graphics.blit(resource, guiLeft, guiTop, 0, 0, 176, 222);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
	public void buttonEvent(GuiButtonNop guibutton)
    {
    	GuiButtonNop button = (GuiButtonNop) guibutton;
    	String sel = scroll.getSelected();
    	if(button.id == 0 && sel != null){
            Packets.sendServer(new SPacketPlayerTransport(sel));
            close();
    	}
    }
//    @Override
//    protected void renderBg(PoseStack matrixStack, float f, int i, int j)
//    {
//    }
    @Override
    public boolean mouseClicked(double i, double j, int k) {
    	super.mouseClicked(i, j, k);
    	scroll.mouseClicked(i, j, k);
        return true;
    }
    
	@Override
	public void save() {
	}

	@Override
	public void setData(Vector<String> list, Map<String, Integer> data) {
		scroll.setList(list);
	}

	@Override
	public void setSelected(String selected) {
		// TODO Auto-generated method stub
		
	}

}
