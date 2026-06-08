package noppes.npcs.client.gui.custom.components;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.InventoryMenu;
import noppes.npcs.api.wrapper.gui.CustomGuiEntityDisplayWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiSliderWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CustomGuiEntityDisplay extends AbstractWidget implements IGuiComponent {

    private GuiCustom parent;
    public CustomGuiEntityDisplayWrapper component;
    private Entity entity;

    public int id;

    public CustomGuiEntityDisplay(GuiCustom parent, CustomGuiEntityDisplayWrapper component) {
        super(component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), Component.empty());
        this.component = component;
        this.parent = parent;
        this.init();
    }

    @Override
    public void init() {
        this.id = component.getID();
        this.setX(component.getPosX());
        this.setY(component.getPosY());
        this.setWidth(component.getWidth());
        this.height = (component.getHeight());
        if(component.entityId!=-1){
            this.entity = Minecraft.getInstance().player.getCommandSenderWorld().getEntity(component.entityId);
        }else if(!component.getEntityData().isEmpty()){
            entity = EntityType.create(component.getEntityData().getMCNBT(), Minecraft.getInstance().level).orElse(null);
        }
        this.active = component.getEnabled() && component.getVisible();
        this.visible = component.getVisible();
    }

    public void setEntity(Entity entity){
        this.entity = entity;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(!visible)
            return;
        if(component.getBackground()){
            graphics.fillGradient(getX(), getY(), width + getX(), height + getY(), -1072689136, -804253680);
        }
        if(entity != null){
            //InventoryScreen.renderEntityInInventory(x + width / 2, y + height, (int)(this.component.getScale() * 10), mouseX, mouseY, (LivingEntity) entity);
            drawEntity(graphics, entity, getX(), getY(), this.component.getScale(), this.component.getRotation() / 2 + 180, mouseX, mouseY, width / 2f, height * 0.9f, component.isFollowingCursor);
        }
        boolean hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        if(hovered && component.hasHoverText()) {
            this.parent.hoverText = component.getHoverTextList();
        }
    }


    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)  {
        return true;
    }

    public static CustomGuiEntityDisplay fromComponent(GuiCustom parent, CustomGuiEntityDisplayWrapper component) {
        CustomGuiEntityDisplay btn = new CustomGuiEntityDisplay(parent, component);
        //btn.active = component.getEnabled();
        return btn;
    }

    public static void drawEntity(GuiGraphics graphics, Entity entity, int x, int y, float zoomed, int rotation, int xMouse, int yMouse, float guiLeft, float guiTop){
        drawEntity(graphics, entity, x,y, zoomed, rotation, xMouse, yMouse, guiLeft, guiTop, true);
    }

    public static void drawEntity(GuiGraphics graphics, Entity entity, int x, int y, float zoomed, int rotation, int xMouse, int yMouse, float guiLeft, float guiTop, boolean followCursor){
        EntityNPCInterface npc = null;
        if(entity instanceof EntityNPCInterface) {
            npc = (EntityNPCInterface) entity;
        }

        LivingEntity livingEntity = null;
        if(entity instanceof LivingEntity) {
            livingEntity = (LivingEntity) entity;
        }
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f2 = 0, f5 = 0, f6 = 0;
        if(livingEntity != null){
            f2 = livingEntity.yBodyRot;
            f5 = livingEntity.yHeadRotO;
            f6 = livingEntity.yHeadRot;
        }

        float scale = 1;
        if(entity.getBbHeight() > 2.4) {
            scale = 2 / entity.getBbHeight();
        }
        float f7 = guiLeft + x - xMouse;
        float f8 = (guiTop + y - (50 * scale * zoomed)) * (entity.getBbHeight() / entity.getEyeHeight()) - yMouse;
        //f8 *= (entity.getEyeHeight() / entity.getBbHeight());
        if(followCursor) {
            entity.setYRot((float) Math.atan(f7 / 80F) * 40F + rotation);
            entity.setXRot(-(float) Math.atan(f8 / 40F) * 20F);
        }else{
            entity.setYRot((float) rotation);
            entity.setXRot(0);
        }

        if(livingEntity != null) {
            livingEntity.yHeadRotO = livingEntity.yHeadRot = livingEntity.yBodyRot = entity.getYRot();
        }
        int orientation = 0;
        int showname = 0;
        if(npc != null){
            orientation = npc.ais.orientation;
            npc.ais.orientation = (int)entity.getYRot();
            showname = npc.display.getShowName();
            npc.display.setShowName(1);
        }
        //entity.yBodyRot = rotation;
        //entity.yRot = (float)Math.atan(f5 / 80F) * 40F + rotation;
        //entity.xRot = -(float) Math.atan(f6 / 40F) * 20F;
        float fs = 30 * scale * zoomed;

        PoseStack posestack = RenderSystem.getModelViewStack();
        //posestack.pushPose();
        //posestack.translate(entity.getBbWidth() / -2 * 40, entity.getBbHeight() / 2 * 10, 0);
        posestack.translate(0,0, 1050.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);

        RenderSystem.applyModelViewMatrix();

        PoseStack matrixStack = new PoseStack();
        matrixStack.translate(guiLeft + x, guiTop + y,0);
        matrixStack.scale(fs, fs, fs);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        matrixStack.mulPose(Axis.YN.rotationDegrees(rotation));

        Lighting.setupForEntityInInventory();

        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderdispatcher.setRenderShadow(false);

        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, graphics.bufferSource(), 15728880);
        });
        graphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        //posestack.popPose();
        posestack.scale(1.0F, 1.0F, -1.0F);
        posestack.translate(0,0, -1050.0F);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();

        matrixStack.popPose();


        entity.setYRot(f3);
        entity.setXRot(f4);
        if(livingEntity != null) {
            livingEntity.yBodyRot = f2;
            livingEntity.yHeadRotO = f5;
            livingEntity.yHeadRot = f6;
        }

        if(npc != null){
            npc.ais.orientation = orientation;
            npc.display.setShowName(showname);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_169152_) {}

    @Override
    public ICustomGuiComponent component() {
        return component;
    }
}
