package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import noppes.npcs.api.function.gui.GuiComponentClicked;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiScrollClick;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;

import java.util.Arrays;
import java.util.List;

public class CustomGuiScroll extends GuiCustomScrollNop implements IGuiComponent {
    private GuiCustom parent;
    public CustomGuiScrollWrapper component;

    public CustomGuiScroll(GuiCustom parent, CustomGuiScrollWrapper component) {
        super(parent, component.getID(), component.isMultiSelect());
        this.component = component;
        this.listener = new ICustomScrollListener(){
            @Override
            public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
                Packets.sendServer(new SPacketCustomGuiScrollClick(component.uniqueId, scroll.getSelectedIndex(), false));
            }

            @Override
            public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {
                Packets.sendServer(new SPacketCustomGuiScrollClick(component.uniqueId, scroll.getSelectedIndex(), true));
            }
        };

        this.minecraft = Minecraft.getInstance();
        this.font = minecraft.font;

        this.parent = parent;
        init();
    }

    @Override
    public void init(){
        this.guiLeft = component.getPosX();
        this.guiTop = component.getPosY();
        this.hasSearch = component.getHasSearch();
        this.setSize(component.getWidth(), component.getHeight());

        this.setUnsortedList(Arrays.asList(component.getList()));

        if(component.getDefaultSelection() >= 0) {
            int defaultSelect = component.getDefaultSelection();
            if(defaultSelect < this.getList().size())
                setSelected(list.get(defaultSelect));
        }
        //this.active = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        boolean hovered = mouseX >= this.guiLeft && mouseY >= this.guiTop && mouseX < this.guiLeft + this.getWidth() && mouseY < this.guiTop + this.getHeight();
        super.render(graphics, mouseX, mouseY, partialTicks);
        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
        matrixStack.popPose();
    }

    @Override
    public ICustomGuiComponent component() {
        return component;
    }
}
