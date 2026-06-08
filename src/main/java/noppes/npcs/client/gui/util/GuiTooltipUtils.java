package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.mixin.ClientTextTooltipMixin;
import org.joml.Vector2ic;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiTooltipUtils {
    private static ItemStack tooltipStack = ItemStack.EMPTY;

    public static void renderTooltip(GuiGraphics graphics, Font p_282308_, ItemStack p_282781_, int p_282687_, int p_282292_) {
        tooltipStack = p_282781_;
        renderTooltip(graphics, p_282308_, Screen.getTooltipFromItem(Minecraft.getInstance(), p_282781_), p_282781_.getTooltipImage(), p_282687_, p_282292_);
        tooltipStack = ItemStack.EMPTY;
    }

    public static void renderTooltip(GuiGraphics graphics, Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, ItemStack stack, int mouseX, int mouseY) {
        tooltipStack = stack;
        renderTooltip(graphics, font, textComponents, tooltipComponent, mouseX, mouseY);
        tooltipStack = ItemStack.EMPTY;
    }

    public static void renderTooltip(GuiGraphics graphics, Font p_283128_, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int p_283678_, int p_281696_) {
        List<ClientTooltipComponent> list = (List)tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        visualTooltipComponent.ifPresent((tooltipComponent) -> {
            list.add(1, ClientTooltipComponent.create(tooltipComponent));
        });
        renderTooltipInternal(graphics, p_283128_, list, p_283678_, p_281696_, DefaultTooltipPositioner.INSTANCE);
    }

    public static void renderTooltip(GuiGraphics graphics, Font p_282269_, Component p_282572_, int p_282044_, int p_282545_) {
        renderTooltip(graphics, p_282269_, List.of(p_282572_.getVisualOrderText()), p_282044_, p_282545_);
    }

    public static void renderTooltip(GuiGraphics graphics, Font p_282192_, List<? extends FormattedCharSequence> p_282297_, int p_281680_, int p_283325_) {
        renderTooltipInternal(graphics, p_282192_, p_282297_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_281680_, p_283325_, DefaultTooltipPositioner.INSTANCE);
    }

    public static void renderTooltip(GuiGraphics graphics, Font p_281627_, List<FormattedCharSequence> p_283313_, ClientTooltipPositioner p_283571_, int p_282367_, int p_282806_) {
        renderTooltipInternal(graphics, p_281627_, p_283313_.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), p_282367_, p_282806_, p_283571_);
    }

    private static void renderTooltipInternal(GuiGraphics graphics, Font p_282675_, List<ClientTooltipComponent> p_282615_, int p_283230_, int p_283417_, ClientTooltipPositioner p_282442_) {
        if (!p_282615_.isEmpty()) {
            int i = 0;
            int j = p_282615_.size() == 1 ? -2 : 0;

            for(ClientTooltipComponent clienttooltipcomponent : p_282615_) {
                int k = clienttooltipcomponent.getWidth(p_282675_);
                if (k > i) {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight();
            }

            int i2 = i;
            int j2 = j;
            Vector2ic vector2ic = p_282442_.positionTooltip(graphics.guiWidth(), graphics.guiHeight(),  p_283230_, p_283417_, i2, j2);
            int l = vector2ic.x();
            int i1 = vector2ic.y();
            graphics.pose().pushPose();
            int j1 = 400;
            graphics.drawManaged(() -> {
                TooltipRenderUtil.renderTooltipBackground(graphics, l, i1, i2, j2, 400);
            });
            graphics.pose().translate(0.0F, 0.0F, 400.0F);
            int k1 = i1;

            for(int l1 = 0; l1 < p_282615_.size(); ++l1) {
                ClientTooltipComponent clienttooltipcomponent1 = p_282615_.get(l1);
                if(clienttooltipcomponent1 instanceof ClientTextTooltip) {
                    graphics.drawString(p_282675_, ((ClientTextTooltipMixin)clienttooltipcomponent1).getText(), l, k1, 16777215, false);
                }else {
                    clienttooltipcomponent1.renderText(p_282675_, l, k1, graphics.pose().last().pose(), graphics.bufferSource());
                }
                k1 += clienttooltipcomponent1.getHeight() + (l1 == 0 ? 2 : 0);
            }

            k1 = i1;

            for(int k2 = 0; k2 < p_282615_.size(); ++k2) {
                ClientTooltipComponent clienttooltipcomponent2 = p_282615_.get(k2);
                clienttooltipcomponent2.renderImage(p_282675_, l, k1, graphics);
                k1 += clienttooltipcomponent2.getHeight() + (k2 == 0 ? 2 : 0);
            }

            graphics.pose().popPose();
        }
    }

}
