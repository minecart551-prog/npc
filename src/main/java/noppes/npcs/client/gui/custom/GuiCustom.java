package noppes.npcs.client.gui.custom;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.util.GuiTooltipUtils;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiSubGuiClosed;
import noppes.npcs.shared.client.gui.listeners.IGuiData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GuiCustom extends AbstractContainerScreen<ContainerCustomGui> implements IGuiData {

    protected CustomGuiTexturedRect background;
    public CustomGuiWrapper guiWrapper;

    public List<Component> hoverText;

    protected GuiCustomComponents components = new GuiCustomComponents();
    protected GuiCustomScrollingPanel scrollingPanel = new GuiCustomScrollingPanel();

    public GuiCustom subgui = null;
    public GuiCustom parent = null;

    public Inventory inv;

    public InitCallback initCallback;

    public GuiCustom(ContainerCustomGui container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
        this.inv = inv;
    }

    @Override
    public void init() {
        super.init();
        if(guiWrapper != null) {
            scrollingPanel.setComponents(this, guiWrapper.getScrollingPanel());
            components.setComponents(this, guiWrapper);
        }
        if(initCallback != null){
            initCallback.init();
        }
        if(subgui != null){
            subgui.init();
        }
    }

    @Override
    public void containerTick() {
        if(subgui != null){
            subgui.containerTick();
        }
        else{
            components.containerTick();
            scrollingPanel.containerTick();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        hoverText = null;
        PoseStack matrixStack = graphics.pose();
        renderBackground(graphics);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(leftPos, topPos, 0);
        RenderSystem.applyModelViewMatrix();
        matrixStack.pushPose();
        //matrixStack.translate(leftPos, topPos, 0);
        if(background != null){
            background.onRender(graphics, mouseX, mouseY, partialTicks);
        }
        components.render(graphics, mouseX - leftPos, mouseY - topPos, partialTicks);
        scrollingPanel.render(graphics, mouseX - leftPos, mouseY - topPos, partialTicks);

        if(hoverText!=null && !hoverText.isEmpty() && subgui == null) {
            GuiTooltipUtils.renderTooltip(graphics, this.font,this.hoverText, Optional.empty(), mouseX - leftPos, mouseY - topPos);
        }
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        super.render(graphics, mouseX, mouseY, partialTicks);
        if(subgui == null){
            this.renderTooltip(graphics, mouseX, mouseY);
        }
        matrixStack.popPose();

        if(subgui != null){
            matrixStack.pushPose();
            posestack.pushPose();
            posestack.translate(0, 0, 40);
            RenderSystem.applyModelViewMatrix();
            matrixStack.translate(0, 0, 40);
            subgui.render(graphics, mouseX, mouseY, partialTicks);
            matrixStack.popPose();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

    }

    @Override
    protected void renderBg(GuiGraphics p_283065_, float p_97788_, int p_97789_, int p_97790_) {

    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(subgui != null){
            return subgui.charTyped(typedChar, keyCode);
        }
        if(components.charTyped(typedChar, keyCode)){
            return true;
        }
        if(scrollingPanel.charTyped(typedChar, keyCode)){
            return true;
        }
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if(subgui != null){
            return subgui.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
        if(components.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_)){
            return true;
        }
        if(scrollingPanel.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_)){
            return true;
        }
//        if(this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(key, p_keyPressed_2_))){ TODO FABRIC
//            return true;
//        }
        return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)  {
        if(subgui != null){
            return subgui.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if(components.mouseClicked(mouseX - leftPos, mouseY - topPos, mouseButton)){
            return true;
        }
        if(scrollingPanel.mouseClicked(mouseX - leftPos, mouseY - topPos, mouseButton)){
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseScrolled) {
        if(subgui != null){
            return subgui.mouseScrolled(mouseX, mouseY, mouseScrolled);
        }
        if(super.mouseScrolled(mouseX, mouseY, mouseScrolled)){
            return true;
        }
        if(scrollingPanel.mouseScrolled(mouseX - leftPos, mouseY - topPos, mouseScrolled)){
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)  {
        if(subgui != null){
            return subgui.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
        }
        if(components.mouseDragged(mouseX - leftPos, mouseY - topPos, mouseButton, dx, dy)){
            return true;
        }
        if(scrollingPanel.mouseDragged(mouseX - leftPos, mouseY - topPos, mouseButton, dx, dy)){
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton)  {
        if(subgui != null){
            return subgui.mouseReleased(mouseX, mouseY, mouseButton);
        }
        if(components.mouseReleased(mouseX - leftPos, mouseY - topPos, mouseButton)){
            return true;
        }
        if(scrollingPanel.mouseReleased(mouseX - leftPos, mouseY - topPos, mouseButton)){
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isPauseScreen() {
        if(guiWrapper != null) {
            return guiWrapper.getDoesPauseGame();
        }
        return true;
    }

    @Override
    public void onClose() {
        if(subgui == null){
            if(parent == null){
                super.onClose();
            }
            else{
                Packets.sendServer(new SPacketCustomGuiSubGuiClosed());
                parent.subgui = null;
            }
        }
        else{
            subgui.onClose();
        }
    }

	@Override
	public void setGuiData(CompoundTag compound) {
        setGuiWrapper((CustomGuiWrapper) new CustomGuiWrapper((IPlayer)NpcAPI.Instance().getIEntity(Minecraft.getInstance().player)).fromNBT(compound));
        init();
	}

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if(subgui != null){
            subgui.resize(minecraft, width, height);
        }
    }

    public void setGuiWrapper(CustomGuiWrapper guiWrapper){
        this.guiWrapper = guiWrapper;
        imageWidth = guiWrapper.getWidth();
        imageHeight = guiWrapper.getHeight();
        background = new CustomGuiTexturedRect(this, (CustomGuiTexturedRectWrapper) guiWrapper.getBackgroundRect());

        if(guiWrapper.hasSubGui()){
            if(subgui == null){
                subgui = new GuiCustom(this.menu, Minecraft.getInstance().player.getInventory(), Component.empty());
                subgui.init(this.minecraft, width, height);
            }
            subgui.parent = this;
            subgui.setGuiWrapper(guiWrapper.getSubGui());
        }
        else{
            menu.setGui(guiWrapper, Minecraft.getInstance().player);
            subgui = null;
            if(parent == null){
                init();
            }
        }
    }

    public IGuiComponent getComponent(UUID id){
        Optional<IGuiComponent> c = components.components.values().stream().filter(t -> t.component() != null && t.component().getUniqueID().equals(id)).findFirst();
        if(c.isPresent()){
            return c.get();
        }
        c = scrollingPanel.components.values().stream().filter(t -> t.component() != null && t.component().getUniqueID().equals(id)).findFirst();
        if(c.isPresent()){
            return c.get();
        }
        if(subgui != null){
            return subgui.getComponent(id);
        }
        return null;
    }

    public int getTotalGuiLeft(){
        if(parent != null){
            return parent.getTotalGuiLeft() + leftPos;
        }
        return leftPos;
    }

    public int getTotalGuiTop(){
        if(parent != null){
            return parent.getTotalGuiTop() + topPos;
        }
        return topPos;
    }

    public void add(IGuiComponent component) {
        components.components.put(component.getID(), component);
    }

    public void addPanel(IGuiComponent component) {
        scrollingPanel.components.put(component.getID(), component);
    }

    public interface InitCallback{
        void init();
    }
}
