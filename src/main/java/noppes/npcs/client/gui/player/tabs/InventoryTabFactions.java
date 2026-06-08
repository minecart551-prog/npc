package noppes.npcs.client.gui.player.tabs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import noppes.npcs.client.gui.player.GuiFaction;
import noppes.npcs.util.CustomNPCsScheduler;

import java.util.Arrays;
import java.util.List;

public class InventoryTabFactions extends AbstractTab {

    private Component displayString;

    public InventoryTabFactions() {
        super(1, 0, 0, new ItemStack(Items.RED_BANNER, 1));
        displayString = Component.translatable("menu.factions");
    }

    @Override
    public void onTabClicked() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new GuiFaction());
    }

    @Override
    public boolean shouldAddToList() {
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        if (hovered) {
            ;
            graphics.pose().translate(mouseX, getY() + 2, 0);
            drawHoveringText(graphics, Arrays.asList(displayString), -mc.font.width(displayString), 0, mc.font);
            graphics.pose().translate(-mouseX, -(getY() + 2), 0);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    protected void drawHoveringText(GuiGraphics graphics, List<Component> list, int x, int y, Font font) {
        if (list.isEmpty())
            return;

        //RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        int k = 0;

        for (Component o : list) {
            int l = font.width(o);

            if (l > k) {
                k = l;
            }
        }

        int j2 = x;
        int k2 = y;
        int i1 = 8;

        if (list.size() > 1) {
            i1 += 2 + (list.size() - 1) * 10;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0,0,300);
        int j1 = -267386864;
        graphics.fillGradient(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
        graphics.fillGradient( j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
        graphics.fillGradient(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
        graphics.fillGradient(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
        graphics.fillGradient( j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
        int k1 = 1347420415;
        int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
        graphics.fillGradient( j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
        graphics.fillGradient( j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
        graphics.fillGradient(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
        graphics.fillGradient(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

        for (int i2 = 0; i2 < list.size(); ++i2) {
            Component s1 = list.get(i2);
            graphics.drawString(font, s1, j2, k2, -1);

            if (i2 == 0) {
                k2 += 2;
            }

            k2 += 10;
        }

        graphics.pose().popPose();
        RenderSystem.enableDepthTest();
        //RenderSystem.enableRescaleNormal();
    }

}