package noppes.npcs.client.gui.custom;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelEyeData;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.subgui.AssetsGui;
import noppes.npcs.api.wrapper.gui.*;
import noppes.npcs.client.gui.custom.components.*;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.model.GuiModelColor;
import noppes.npcs.client.layer.LayerParts;
import noppes.npcs.client.parts.*;
import noppes.npcs.client.parts.PartBehaviorType;
import noppes.npcs.client.parts.PartRenderType;
import noppes.npcs.constants.BodyPart;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.server.SPacketCustomGuiParts;
import noppes.npcs.shared.client.gui.components.GuiColorButton;
import noppes.npcs.shared.client.gui.components.GuiCustomScrollNop;
import noppes.npcs.shared.client.gui.listeners.ICustomScrollListener;
import noppes.npcs.shared.common.util.ColorUtil;
import noppes.npcs.shared.common.util.NaturalOrderComparator;
import noppes.npcs.shared.common.util.NopVector2i;
import noppes.npcs.shared.common.util.NopVector3f;

import java.util.*;
import java.util.stream.Collectors;

public class GuiCreationNewParts extends AbstractWidget implements IGuiComponent {
	private CustomGuiScroll scroll;
	private CustomGuiSlider slider;
	private CustomGuiEntityDisplay entity;
	private ModelData data;
	private ModelData renderData = new ModelData(null);
	private static String active = "";
	private static PlayerModel biped;
	private GuiCustom parent;
	private EntityCustomNpc npc;
	private Minecraft minecraft;
	private List<GuiMpmPart> guiParts = new ArrayList<>();

	public static final ResourceLocation buttonsResource = new ResourceLocation("moreplayermodels", "textures/gui/arrowbuttons.png");
	private static final ResourceLocation colorWheel = new ResourceLocation("moreplayermodels", "textures/gui/colorwheel.png");

	public GuiCreationNewParts(GuiCustom parent, EntityCustomNpc npc){
		super(0, 0, 420, 200, Component.empty());
		this.npc = npc;
		this.parent = parent;
		this.data = npc.modelData;
		minecraft = Minecraft.getInstance();

		String[] menus = MpmPartReader.PARTS.values().stream().map((p) -> p.menu).sorted(new NaturalOrderComparator()).distinct().toArray(String[]::new);
		if(active.isEmpty()){
			active = menus[0];
		}
		scroll = new CustomGuiScroll(parent, new CustomGuiScrollWrapper(-4, 4, 24, 100, 210, menus));
		scroll.disabledSearch();
		scroll.listener = new ICustomScrollListener(){
			@Override
			public void scrollClicked(double i, double j, int k, GuiCustomScrollNop scroll) {
				if(scroll.getSelectedIndex() >= 0){
					active = scroll.getSelected();
					for(GuiMpmPart part : guiParts){
						parent.scrollingPanel.comps.removeComponent(part.getID());
					}
					guiParts.clear();
					parent.init();
				}
			}
			@Override
			public void scrollDoubleClicked(String selection, GuiCustomScrollNop scroll) {}
		};

		CustomGuiEntityDisplayWrapper wrapper = new CustomGuiEntityDisplayWrapper(-2, npc.wrappedNPC, 106, 90);
		wrapper.setSize(68, 90);
		entity = new CustomGuiEntityDisplay(parent, wrapper);

		slider = new CustomGuiSlider(parent, (CustomGuiSliderWrapper) new CustomGuiSliderWrapper(- 3, "", 106, 186, 68, 20).setMax(360).setDecimals(0).setValue(180).setOnChange((gui, slider) -> {
			entity.component.setRotation((int)slider.getValue() - 180);
			entity.init();
		})).disablePackets();

		biped = new PlayerModel(minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), true);
	}

	public void openSubgui(GuiCustom parent, GuiCustom subgui){

		subgui.init(this.minecraft, parent.width, parent.height);
		subgui.parent = parent;
		parent.subgui = subgui;
		if(subgui.guiWrapper != null){
			subgui.background = new CustomGuiTexturedRect(subgui, (CustomGuiTexturedRectWrapper) subgui.guiWrapper.getBackgroundRect());
		}
		if(subgui.scrollingPanel.comps == null){
			subgui.scrollingPanel.comps = new GuiComponentsScrollableWrapper(subgui.guiWrapper, null);
		}
	}

	@Override
	public int getID() {
		return -10;
	}

	@Override
	public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		entity.visible = parent.subgui==null;
		render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
    public void init() {
		parent.add(scroll);
		parent.add(entity);
		parent.add(slider);

		List<MpmPart> list = new ArrayList<MpmPart>(MpmPartReader.PARTS.values().stream().sorted(Comparator.comparing(t -> t.id))
				.filter(t -> t.menu.equals(active) && t.parentId == null).collect(Collectors.toList()));

		scroll.setSelected(active);
		entity.setEntity(npc);

		if(guiParts.isEmpty()){
			for(int i = 0; i < list.size(); i++){
				int column = (i % 3);
				MpmPart part = list.get(i);
				GuiMpmPart gui = new GuiMpmPart(parent, 80 + i, column * GuiMpmPart.SIZE + column, (i / 3)  * GuiMpmPart.SIZE, part);
				guiParts.add(gui);
				parent.addPanel(gui);
				parent.scrollingPanel.comps.addComponent(new PartsWrapper(gui));
			}
		}
		else{
			for(int i = 0; i < guiParts.size(); i++){
				int column = (i % 3);
				GuiMpmPart gui = guiParts.get(i);
				gui.setX(column * GuiMpmPart.SIZE + column);
				gui.setY((i / 3)  * GuiMpmPart.SIZE);
				parent.addPanel(gui);
			}
		}
		parent.scrollingPanel.setMaxSize(guiParts.stream()
				.mapToInt(v -> v.getY() + v.getHeight())
				.max().orElse(0));

//		addButton(new GuiNpcButton(66, guiLeft + 396, guiTop + 2, 20, 20, "X", (b) -> {
//			close();
//		}));
    }

	@Override
	public ICustomGuiComponent component() {
		return null;
	}

	@Override
	protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {
		
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)  {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int i, int j, float f){
		//super.render(poseStack, i, j, f);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

	}

	public void save(){
		Packets.sendServer(new SPacketCustomGuiParts(data.save()));
	}

	class GuiMpmPart extends AbstractWidget implements IGuiComponent {
		public static final int SIZE = 70;
		public boolean basic = false;
		private List<MpmPart> all = new ArrayList<>();
		private MpmPart part;
		private MpmPartData data;
		private boolean selected = true;
		private GuiCustom parent;

		boolean colorPickerHovered = false;
		boolean infoHovered = false;
		boolean settingsHovered = false;
		boolean hoverL = false;
		boolean hoverR = false;

		int zPos = 0;

		int id;

		public GuiMpmPart(GuiCustom parent, int id, int x, int y, MpmPart part) {
			super(x, y, SIZE, SIZE, Component.empty());
			this.parent = parent;
			this.id = id;
			this.part = part;
			this.all.add(part);
			for(Map.Entry<ResourceLocation, MpmPart> entry : MpmPartReader.PARTS.entrySet()){
				if(entry.getValue().parentId != null && entry.getValue().parentId.equals(part.id)){
					all.add(entry.getValue());
				}
			}
			for(MpmPart p : all){
				data = GuiCreationNewParts.this.data.mpmParts.stream().filter(t -> t.partId.equals(p.id)).findFirst().orElse(null);
				if(data != null){
					this.part = p;
					break;
				}
			}
			all = all.stream().sorted(Comparator.comparing(t -> t.id)).collect(Collectors.toList());
			if(data == null){
				if(part.id.equals(ModelEyeData.RESOURCE) || part.id.equals(ModelEyeData.RESOURCE_RIGHT) || part.id.equals(ModelEyeData.RESOURCE_LEFT)){
					this.data = new ModelEyeData();
				}
				else{
					this.data = new MpmPartData();
				}
				this.data.partId = part.id;
				this.data.usePlayerSkin = part.defaultUsePlayerSkins;
				selected = false;
			}
		}
		

		public void renderModel(GuiGraphics graphics, int xMouse, int yMouse, float tick){
			int x1 = getX();
			int x2 = getX() + SIZE;
			int y1 = getY();
			int y2 = getY() + SIZE - 1;
			graphics.fill(x1, y1, x2, y2, 0xffC6C6C6);

			renderData.mpmParts = GuiCreationNewParts.this.data.mpmParts;

			PoseStack posestack = RenderSystem.getModelViewStack();
			posestack.pushPose();
			posestack.translate(0,0, 100.0D + zPos);
			posestack.scale(1.0F, 1.0F, -1.0F);
			RenderSystem.applyModelViewMatrix();
			PoseStack matrixstack = new PoseStack();
			matrixstack.translate(getX(), getY() - parent.scrollingPanel.comps.scrollAmount, 1);
			matrixstack.pushPose();
			EntityRenderDispatcher entityrenderermanager = minecraft.getEntityRenderDispatcher();
			entityrenderermanager.setRenderShadow(false);
			//entityrenderermanager.overrideCameraOrientation(Vector3f.YP.rotationDegrees(180.0F));
			MultiBufferSource.BufferSource irendertypebuffer$impl = minecraft.renderBuffers().bufferSource();
			VertexConsumer ivertex = irendertypebuffer$impl.getBuffer(RenderType.entityCutoutNoCull(npc.textureLocation));
			Lighting.setupForEntityInInventory();
			RenderSystem.runAsFancy(() -> {

				biped.leftLeg.visible = !part.hiddenParts.contains(BodyPart.LEFT_LEG) && !part.hiddenParts.contains(BodyPart.LEGS);
				biped.leftPants.visible = biped.leftPants.visible && biped.leftLeg.visible;
				biped.rightLeg.visible = !part.hiddenParts.contains(BodyPart.RIGHT_LEG) && !part.hiddenParts.contains(BodyPart.LEGS);
				biped.rightPants.visible = biped.rightPants.visible && biped.rightLeg.visible;

				biped.leftArm.visible = !part.hiddenParts.contains(BodyPart.LEFT_ARM) && !part.hiddenParts.contains(BodyPart.ARMS);
				biped.leftSleeve.visible = biped.leftSleeve.visible && biped.leftArm.visible;
				biped.rightArm.visible = !part.hiddenParts.contains(BodyPart.RIGHT_ARM) && !part.hiddenParts.contains(BodyPart.ARMS);
				biped.rightSleeve.visible = biped.rightSleeve.visible && biped.rightArm.visible;

				biped.body.visible = !part.hiddenParts.contains(BodyPart.BODY);
				biped.jacket.visible = biped.jacket.visible && biped.body.visible;
				biped.head.visible = !part.hiddenParts.contains(BodyPart.HEAD);
				biped.hat.visible = biped.hat.visible && biped.head.visible;

				if(part.bodyPart == BodyPart.HEAD){
					matrixstack.translate(32, (float)46, 25.0F);
					matrixstack.scale(36, 36, 36);
					matrixstack.mulPose(Axis.XP.rotation((float)(Math.PI / 8)));
					matrixstack.mulPose(Axis.YP.rotation(part.previewRotation * ((float)Math.PI / 180F)));
					biped.head.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
				}
				if(part.bodyPart == BodyPart.LEGS){
					matrixstack.translate(18, (float)12, 25.0F);
					matrixstack.scale(36, 36, 36);
					matrixstack.mulPose(Axis.XP.rotation((float)(Math.PI / 8)));
					matrixstack.mulPose(Axis.YP.rotation(part.previewRotation * ((float)Math.PI / 180F)));
					biped.body.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);

					if(part.animationType == PartBehaviorType.LEGS){
						ModelPartWrapper modelPart = part.getPart("right_leg");
						if(modelPart != null){
							modelPart.setRot(new NopVector3f(biped.rightLeg.xRot, biped.rightLeg.yRot, biped.rightLeg.zRot));
							modelPart.setPos(new NopVector3f(biped.rightLeg.x, biped.rightLeg.y, biped.rightLeg.z));
						}

						modelPart = part.getPart("left_leg");
						if(modelPart != null){
							modelPart.setRot(new NopVector3f(biped.leftLeg.xRot, biped.leftLeg.yRot, biped.leftLeg.zRot));
							modelPart.setPos(new NopVector3f(biped.leftLeg.x, biped.leftLeg.y, biped.leftLeg.z));
						}
					}

					biped.rightLeg.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
					biped.leftLeg.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
				}
				if(part.bodyPart == BodyPart.ARMS){
					matrixstack.translate(18, (float)12, 25.0F);
					matrixstack.scale(36, 36, 36);
					matrixstack.mulPose(Axis.XP.rotation((float)(Math.PI / 8)));
					matrixstack.mulPose(Axis.YP.rotation(part.previewRotation * ((float)Math.PI / 180F)));
					biped.body.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);

					if(part.animationType == PartBehaviorType.ARMS){
						ModelPartWrapper modelPart = part.getPart("right_arm");
						if(modelPart != null){
							modelPart.setRot(new NopVector3f(biped.rightArm.xRot, biped.rightArm.yRot, biped.rightArm.zRot));
							modelPart.setPos(new NopVector3f(biped.rightArm.x, biped.rightArm.y, biped.rightArm.z));
						}

						modelPart = part.getPart("left_arm");
						if(modelPart != null){
							modelPart.setRot(new NopVector3f(biped.leftArm.xRot, biped.leftArm.yRot, biped.leftArm.zRot));
							modelPart.setPos(new NopVector3f(biped.leftArm.x, biped.leftArm.y, biped.leftArm.z));
						}
					}

					biped.leftArm.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
					biped.rightArm.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
				}
				if(part.bodyPart == BodyPart.BODY){
					matrixstack.translate(18, (float)18, 25.0F);
					matrixstack.scale(36, 36, 36);
					matrixstack.mulPose(Axis.XP.rotation((float)(Math.PI / 8)));
					matrixstack.mulPose(Axis.YP.rotation(part.previewRotation * ((float)Math.PI / 180F)));
					biped.body.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
				}
				if(part.renderType != PartRenderType.NONE){
					MpmPartAbstractClient partc = (MpmPartAbstractClient) part;
					partc.pos = NopVector3f.ZERO;
					partc.rot = NopVector3f.ZERO;
					LayerParts.renderPart(data, partc, matrixstack, irendertypebuffer$impl, 15728880, npc, biped, renderData);
				}
			});

			irendertypebuffer$impl.endBatch();
			matrixstack.popPose();
			posestack.popPose();
			entityrenderermanager.setRenderShadow(true);
			RenderSystem.applyModelViewMatrix();
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int xMouse, int yMouse, float tick) {
			if(parent.subgui == null){
				//renderModel(poseStack, xMouse, yMouse, tick);
				//renderIcons(poseStack, xMouse, yMouse, tick);
			}
		}

		public void renderIcons(GuiGraphics graphics, int xMouse, int yMouse, float tick) {
			int color = 0xffffffff;

			if(!basic){
				if(isHovered){
					color = 0xffff0000;
				}
				int x1 = getX();
				int x2 = getX() + SIZE;
				int y1 = getY();
				int y2 = getY() + SIZE - 1;
				graphics.hLine(x1, x2, y1, color);
				graphics.hLine(x1, x2, y2, color);
				graphics.hLine(x1, y1, y2, color);
				graphics.hLine(x2, y1, y2, color);

				x1 = getX() + SIZE - 16;
				x2 = getX() + SIZE;
				y1 = getY() + 1;
				y2 = getY() + SIZE - 1;
				graphics.fill(x1, y1, x2, y2, 0xffC6C6C6);

				color = 0xffffffff;
				x1 = getX() + SIZE - 14;
				x2 = getX() + SIZE - 2;
				y1 = getY() + 2;
				y2 = getY() + 14;
				graphics.fill(x1, y1, x2, y2, 0xff000000);
				graphics.hLine(x1, x2, y1, color);
				graphics.hLine(x1, x2, y2, color);
				graphics.hLine(x1, y1, y2, color);
				graphics.hLine(x2, y1, y2, color);
				if(!part.isEnabled){
					graphics.drawString(minecraft.font, Component.literal("X").withStyle(ChatFormatting.BOLD), x1 + 4, y1 + 3, 0xFF0000);
				}
				else if(selected){
					char c = (char)Integer.parseInt("2713", 16);
					graphics.drawString(minecraft.font, Component.literal(c + "").withStyle(ChatFormatting.BOLD), x1 + 3, y1 + 2, 0x00FF00);
				}
			}
			int guiY = getY() + 16;

			RenderSystem.setShaderTexture(0, colorWheel);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int size = 14;
			int x1 = getX() + SIZE - 15;
			int x2 = x1 + size;
			int y1 = guiY;
			int y2 = y1 + size;
			colorPickerHovered = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
			if(colorPickerHovered){
				x1--;
				y1--;
				size = 16;
			}
			graphics.blit(colorWheel, x1, y1, 0, 0, 0, size, size,size, size);
			guiY += 15;

			RenderSystem.setShaderTexture(0, buttonsResource);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			if(all.size() > 1){
				x1 = getX() + SIZE - 17;
				x2 = x1 + 6;
				y1 = guiY;
				y2 = y1 + 8;

				RenderSystem.setShaderTexture(0, buttonsResource);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				hoverL = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
				graphics.blit(buttonsResource, x1, y1, 0, hoverL?76:60, 6, 8);

				String s = all.indexOf(part) + "";
				graphics.drawString(minecraft.font, s, (int) (x1 + 9.5f - minecraft.font.width(s) / 2f), (int) (y1 + 0.5f), 0x000000);

				RenderSystem.setShaderTexture(0, buttonsResource);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				x1 = getX() + SIZE - 5;
				x2 = x1 + 6;
				y1 = guiY;
				y2 = y1 + 8;
				hoverR = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
				graphics.blit(buttonsResource, x1, y1, 6, hoverR?76:60, 6, 8);
				guiY += 11;
			}

			if(!basic){
				if(selected) {
					x1 = getX() + SIZE - 15;
					x2 = x1 + 14;
					y1 = guiY;
					y2 = y1 + 14;
					settingsHovered = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
					graphics.blit(buttonsResource, x1, y1, 0, settingsHovered ? 140 : 126, 14, 14);
				}

				size = 8;
				x1 = getX() + SIZE - 10;
				x2 = x1 + size;
				y1 = getY() + SIZE - 12;
				y2 = y1 + size;
				infoHovered = xMouse >= x1 && yMouse >= y1 && xMouse < x2 && yMouse < y2;
				MutableComponent text = Component.literal("i").withStyle(ChatFormatting.BOLD);
				if(infoHovered){
					text = text.withStyle(ChatFormatting.UNDERLINE);
				}
				graphics.drawString(minecraft.font, text, x1 + 3, y1 + 2, 0x000000);
			}

		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)  {
			if(!super.clicked(mouseX, mouseY)) {
				return false;
			}
			if(colorPickerHovered){
				openSubgui(parent, new GuiModelColor(parent, data.getColor(), color -> {data.setColor(color);save();}));
			}
			else if(hoverL){
				int index = (all.indexOf(part) + all.size() - 1) % all.size();
				this.part = all.get(index);
				this.data.partId = part.id;
			}
			else if(hoverR){
				int index = (all.indexOf(part) + 1) % all.size();
				this.part = all.get(index);
				this.data.partId = part.id;
			}
			else if(settingsHovered){
				if(data instanceof ModelEyeData){
					openEyesSubgui(parent, (ModelEyeData) data, (MpmPartEyes)part);
				}
				else{
					openTextureSubgui(parent, data, part);
				}
			}
			else if(part.isEnabled && !basic){
				selected = !selected;
				if(selected){
					GuiCreationNewParts.this.data.mpmParts.add(data);
				}
				else{
					GuiCreationNewParts.this.data.mpmParts.removeIf(t -> t.partId.equals(data.partId));
				}
			}
			GuiCreationNewParts.this.data.refreshParts();
			save();
			return true;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

		}

		@Override
		public int getID() {
			return id;
		}

		@Override
		public void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			if(parent.subgui == null){
				renderModel(graphics, mouseX, mouseY, partialTicks);
			}
		}

		@Override
		public void onRenderPost(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			if(parent.subgui == null){
				renderIcons(graphics, mouseX, mouseY, partialTicks);
				if (infoHovered) {
					List<Component> text = Arrays.asList(Component.translatable(part.name), Component.translatable("message.madeby", part.author));
					if (!part.isEnabled) {
						text = new ArrayList<>();
						text.add(Component.translatable("gui.disabled", part.author));
					}
					parent.hoverText = text;
				}
			}
		}

		@Override
		public void init() {

		}

		@Override
		public ICustomGuiComponent component() {
			return null;
		}
	}
	public void openTextureSubgui(GuiCustom parent, MpmPartData data, MpmPart part){
		TexturePart screen = new TexturePart(data, part);
		CustomGuiWrapper gui = screen.guiWrapper;
		gui.setBackgroundTexture("customnpcs:textures/gui/components.png");
		gui.setSize(310, 200);
		gui.getBackgroundRect().setTextureOffset(0, 0);
		gui.getBackgroundRect().setRepeatingTexture(64, 64, 4);

		gui.addButton(66, "x", 276, 4, 20, 20).setOnPress((gui2, button) -> screen.onClose()).setDisablePackets();

		if(!part.disableCustomTextures) {
			gui.addLabel(21, "gui.playerskin", 4, 110, 10, 100);
			gui.addButtonList(22, 76, 105, 50, 20).setValues("gui.no", "gui.yes").setSelected(data.usePlayerSkin? 1: 0)
					.setOnPress((gui2, button) -> {
						data.usePlayerSkin = ((CustomGuiButtonListWrapper) button).getSelected() == 1;
						gui2.getComponent(23).setVisible(!data.usePlayerSkin);
						gui2.getComponent(24).setVisible(!data.usePlayerSkin);
						gui2.getComponent(25).setVisible(!data.usePlayerSkin);
						gui2.getComponent(26).setVisible(!data.usePlayerSkin);
						gui2.getComponent(27).setVisible(!data.usePlayerSkin);
						GuiCreationNewParts.this.data.refreshParts();
						save();
						screen.init();
					}).setDisablePackets();

			gui.addLabel(23, "gui.texture", 4, 130, 10, 100).setVisible(!data.usePlayerSkin);
			ResourceLocation loc = data.getDefaultTexture();
			CustomGuiTextFieldWrapper tf = (CustomGuiTextFieldWrapper)gui.addTextField(24, 4, 140, 220, 20).setText(loc == null ? "" : loc.toString())
					.setOnFocusLost((gui2, text) -> data.setTexture(text.getText())).setVisible(!data.usePlayerSkin).setDisablePackets();
			gui.addButton(25, "gui.select", 226, 140, 80, 20)
					.setOnPress((gui2, button) -> {
						openSubgui(screen, openTextureBasic(data.getDefaultTexture() == null ? "" : data.getDefaultTexture().toString(), (resource) -> {
							data.setTexture(resource);
							tf.setText(resource);
							GuiCreationNewParts.this.data.refreshParts();
							save();
							screen.init();
						}));
					}).setVisible(!data.usePlayerSkin).setDisablePackets();

			gui.addLabel(26, "config.skinurl", 4, 168, 10, 100).setVisible(!data.usePlayerSkin);
			gui.addTextField(27, 4, 178, 220, 20).setText(data.url).setOnFocusLost((gui2, text) -> {
				data.setUrl(text.getText());
				GuiCreationNewParts.this.data.refreshParts();
				save();
				screen.init();
			}).setVisible(!data.usePlayerSkin).setDisablePackets();
		}

		screen.setGuiWrapper(gui);
		openSubgui(parent, screen);
	}

	public GuiCustom openTextureBasic(String resource, AssetsGui.SelectionCallback callback){
		GuiCustom screen = new GuiCustom(this.parent.getMenu(), this.parent.inv, Component.empty());
		CustomGuiWrapper gui = screen.guiWrapper = new CustomGuiWrapper(null);
		gui.setBackgroundTexture("customnpcs:textures/gui/components.png");
		gui.setSize(308, 214);
		gui.getBackgroundRect().setTextureOffset(0, 0);
		gui.getBackgroundRect().setRepeatingTexture(64, 64, 4);

		CustomGuiButtonWrapper b = gui.addTexturedButton(666, "X", 290, -4, 14, 14, "customnpcs:textures/gui/components.png", 0, 64);
		b.getTextureRect().setRepeatingTexture(64, 22, 3).setHoverText("gui.close");
		b.setTextureHoverOffset(22).setOnPress((guii, bb) -> screen.onClose());
		b.setDisablePackets();

		gui.addAssetsSelector(11, 4, 4, 300, 204).setSelected(resource).setOnPress((gui2, assets) -> screen.onClose())
				.setOnChange((gui2, assets) -> callback.call(assets.getSelected())).setDisablePackets();
		screen.setGuiWrapper(gui);

		return screen;
	}

	class TexturePart extends GuiCustom{
		private MpmPart part;
		private MpmPartData data;
		private GuiMpmPart partGui;

		public TexturePart(MpmPartData data, MpmPart part){
			super(GuiCreationNewParts.this.parent.getMenu(), GuiCreationNewParts.this.parent.inv, Component.empty());
			this.data = data;
			this.part = part;

			partGui = new GuiMpmPart(this, 70, 2,  2, part);
			partGui.zPos = 250;
			partGui.basic = true;

			guiWrapper = new CustomGuiWrapper(null);
			guiWrapper.addComponent(new PartsWrapper(partGui));
		}

		@Override
		public void init(){
			super.init();
			add(partGui);
		}

		@Override
		public void onClose(){
			super.onClose();
			save();
		}
	}
	public void openEyesSubgui(GuiCustom parent, ModelEyeData data, MpmPartEyes part){
		EyesPart screen = new EyesPart(data, part);
		CustomGuiWrapper gui = screen.guiWrapper = new CustomGuiWrapper(null);
		gui.setBackgroundTexture("customnpcs:textures/gui/components.png");
		gui.setSize(310, 200);
		gui.getBackgroundRect().setTextureOffset(0, 0);
		gui.getBackgroundRect().setRepeatingTexture(64, 64, 4);

		int y = 8;
		gui.addLabel(21, "part.eyes", 56, y + 5, 10, 100);
		gui.addButtonList(22, 110, y, 110, 20).setValues("gui.playerskin", "gui.normal", "gui.texture").setSelected(data.skinType)
				.setOnPress((gui2, button) -> {
					data.skinType = ((CustomGuiButtonListWrapper)button).getSelected();
					gui2.getComponent(23).setVisible(data.skinType == 1);
					gui2.getComponent(24).setVisible(data.skinType == 2);
					gui2.getComponent(25).setVisible(data.skinType == 2);
					gui2.getComponent(27).setVisible(data.glint || data.skinType == 1 || data.skinType == 2);
					screen.init();
				}).setDisablePackets();

		gui.addButton(23, "", 230, y, 50, 20)
				.setOnPress((gui2, button) -> {
					openSubgui(screen, new GuiModelColor(screen, ColorUtil.rgbToColor(data.color), color -> {
						data.color = ColorUtil.colorToRgb(color);
						screen.init();
					}));
				}).setVisible(data.skinType == 1).setDisablePackets();

		y += 25;
		gui.addLabel(24, "config.skinurl", 56, y + 5, 10, 100).setVisible(data.skinType == 2);
		gui.addTextField(25, 110, y, 195, 20).setText(data.url).setOnFocusLost((gui2, text) -> {
			data.setUrl(text.getText());
		}).setVisible(data.skinType == 2).setDisablePackets();

		gui.addButtonList(26, 54, y += 25, 100, 20).setValues("gui.normal", "gui.big").setSelected(data.eyeSize)
				.setOnPress((gui2, button) -> {
					data.eyeSize = ((CustomGuiButtonListWrapper)button).getSelected();
					screen.init();
				}).setDisablePackets();

		gui.addButtonList(27, 156, y, 100, 20).setValues("gui.normal", "gui.mirror").setSelected(data.mirror ? 1 : 0)
				.setOnPress((gui2, button) -> {
					data.mirror = ((CustomGuiButtonListWrapper)button).getSelected() == 1;
					screen.init();
				}).setVisible(data.glint || data.skinType == 1 || data.skinType == 2).setDisablePackets();
		gui.addLabel(28, "eye.pupil", 4 , y + 5, 10, 100);

		gui.addButtonList(29, 54, y += 25, 100, 20)
				.setValues(I18n.get("gui.down") + "x2", "gui.down", "gui.normal", "gui.up", I18n.get("gui.up") + "x2")
				.setSelected(data.eyePos.y + 2)
				.setOnPress((gui2, button) -> data.eyePos = new NopVector2i(data.eyePos.x, ((CustomGuiButtonListWrapper)button).getSelected() - 2))
				.setDisablePackets();
		gui.addButtonList(30, 156, y, 100, 20)
				.setValues("gui.inward", "gui.normal", "gui.outward").setSelected(data.eyePos.x + 1)
				.setOnPress((gui2, button) -> data.eyePos = new NopVector2i(((CustomGuiButtonListWrapper)button).getSelected() - 1, data.eyePos.y))
				.setDisablePackets();
		gui.addLabel(31, "gui.position", 4 , y + 5, 10, 100);

		gui.addButtonList(32, 54, y += 25, 50, 20)
				.setValues("gui.no", "gui.yes").setSelected(data.glint? 1 : 0)
				.setOnPress((gui2, button) -> {
					data.glint = ((CustomGuiButtonListWrapper)button).getSelected() == 1;
					gui2.getComponent(27).setVisible(data.glint || data.skinType == 1 || data.skinType == 2);
				}).setDisablePackets();
		gui.addLabel(33, "eye.glint", 4 , y + 5, 10, 100);

		gui.addButton(34, "", 162, y, 50, 20)
				.setOnPress((gui2, button) -> {
					openSubgui(screen, new GuiModelColor(screen, ColorUtil.rgbToColor(data.browColor), color -> {
						data.browColor = ColorUtil.colorToRgb(color);
						screen.init();
					}));
				}).setDisablePackets();

		gui.addButtonList(35, 214, y, 70, 20)
				.setValues("gui.disabled", "1", "2", "3", "4", "5", "6", "7", "8").setSelected((int)(data.browThickness.y * 10))
				.setOnPress((gui2, button) -> {
					data.browThickness = new NopVector3f(1, ((CustomGuiButtonListWrapper)button).getSelected() / 10f, 1);
				}).setDisablePackets();
		gui.addLabel(36, "eye.lash", 112 , y + 5, 10, 100);

		gui.addButtonList(37, 54, y += 25, 50, 20)
				.setValues("gui.no", "gui.yes").setSelected(data.disableBlink? 0 : 1)
				.setOnPress((gui2, button) -> {
					data.disableBlink = ((CustomGuiButtonListWrapper)button).getSelected() == 0;
					gui2.getComponent(39).setVisible(!data.disableBlink);
					gui2.getComponent(40).setVisible(!data.disableBlink);
					screen.init();
				}).setDisablePackets();
		gui.addLabel(38, "eye.blink", 4 , y + 5, 10, 100);


		gui.addLabel(39, "eye.lid", 112 , y + 5, 10, 100).setVisible(!data.disableBlink);
		gui.addButton(40, "", 162, y, 50, 20)
				.setOnPress((gui2, button) -> {
					openSubgui(screen, new GuiModelColor(screen, ColorUtil.rgbToColor(data.lidColor), color -> {
						data.lidColor = ColorUtil.colorToRgb(color);
						screen.init();
					}));
				}).setVisible(!data.disableBlink).setDisablePackets();

		gui.addButton(66, "x", 288, 4, 20, 20)
				.setOnPress((gui2, button) -> screen.onClose()).setDisablePackets();

		screen.setGuiWrapper(gui);
		openSubgui(parent, screen);

	}
	class EyesPart extends GuiCustom{
		private MpmPartEyes part;
		private ModelEyeData data;


		public EyesPart(ModelEyeData data, MpmPartEyes part){
			super(GuiCreationNewParts.this.parent.getMenu(), GuiCreationNewParts.this.parent.inv, Component.empty());
			this.data = data;
			this.part = part;
			//this.closeOnEsc = true;
		}

		@Override
		public void init(){
			super.init();

			this.components.components.put(23, new GuiColorButton(this, (CustomGuiButtonWrapper)guiWrapper.getComponent(23), ColorUtil.rgbToColor(data.color)));
			this.components.components.put(34, new GuiColorButton(this, (CustomGuiButtonWrapper)guiWrapper.getComponent(34), ColorUtil.rgbToColor(data.browColor)));
			this.components.components.put(40, new GuiColorButton(this, (CustomGuiButtonWrapper)guiWrapper.getComponent(40), ColorUtil.rgbToColor(data.lidColor)));
		}

		@Override
		public void renderBackground(GuiGraphics graphics) {
			super.renderBackground(graphics);
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.render(graphics, mouseX, mouseY, partialTicks);

			PoseStack posestack = RenderSystem.getModelViewStack();
			posestack.pushPose();
			posestack.translate(leftPos + 10, topPos + 10, 150.0D);
			posestack.scale(1.0F, 1.0F, -1.0F);
			RenderSystem.applyModelViewMatrix();
			PoseStack matrixstack = new PoseStack();
			matrixstack.pushPose();
			EntityRenderDispatcher entityrenderermanager = minecraft.getEntityRenderDispatcher();
			entityrenderermanager.setRenderShadow(false);
			//entityrenderermanager.overrideCameraOrientation(Vector3f.YP.rotationDegrees(180.0F));
			MultiBufferSource.BufferSource irendertypebuffer$impl = minecraft.renderBuffers().bufferSource();
			VertexConsumer ivertex = irendertypebuffer$impl.getBuffer(RenderType.entityCutoutNoCull(npc.textureLocation));
			Lighting.setupForEntityInInventory();
			RenderSystem.runAsFancy(() -> {
				biped.body.visible = !part.hiddenParts.contains(BodyPart.BODY);
				biped.jacket.visible = biped.jacket.visible && biped.body.visible;
				biped.head.visible = !part.hiddenParts.contains(BodyPart.HEAD);
				biped.hat.visible = biped.hat.visible && biped.head.visible;
				matrixstack.translate(19, (float)43, 25.0F);
				matrixstack.scale(100, 100, 100);
				//matrixstack.mulPose(Vector3f.XP.rotation((float)(Math.PI / 8)));
				//matrixstack.mulPose(Vector3f.YP.rotation(part.previewRotation * ((float)Math.PI / 180F)));
				biped.head.render(matrixstack, ivertex, 15728880, OverlayTexture.NO_OVERLAY);
				part.pos = NopVector3f.ZERO;
				part.rot = NopVector3f.ZERO;
				LayerParts.renderPart(data, part, matrixstack, irendertypebuffer$impl, 15728880, npc, biped, renderData);
			});

			irendertypebuffer$impl.endBatch();
			matrixstack.popPose();
			posestack.popPose();
			entityrenderermanager.setRenderShadow(true);
			RenderSystem.applyModelViewMatrix();
		}

		@Override
		public void onClose(){
			super.onClose();
			save();
		}
	}
	public class PartsWrapper implements ICustomGuiComponent{
		private GuiMpmPart part;
		public PartsWrapper(GuiMpmPart part){
			this.part = part;
		}
		@Override
		public int getID() {
			return part.getID();
		}

		@Override
		public ICustomGuiComponent setID(int id) {
			return this;
		}

		@Override
		public UUID getUniqueID() {
			return null;
		}

		@Override
		public int getPosX() {
			return part.getX();
		}

		@Override
		public int getPosY() {
			return part.getY();
		}

		@Override
		public ICustomGuiComponent setPos(int x, int y) {
			return this;
		}

		@Override
		public int getWidth() {
			return part.getWidth();
		}

		@Override
		public int getHeight() {
			return part.getHeight();
		}

		@Override
		public ICustomGuiComponent setSize(int width, int height) {
			return null;
		}

		@Override
		public boolean hasHoverText() {
			return false;
		}

		@Override
		public String[] getHoverText() {
			return new String[0];
		}

		@Override
		public ICustomGuiComponent setHoverText(String text) {
			return null;
		}

		@Override
		public ICustomGuiComponent setHoverText(String[] text) {
			return null;
		}

		@Override
		public ICustomGuiComponent setEnabled(boolean bo) {
			return this;
		}

		@Override
		public boolean getVisible() {
			return true;
		}

		@Override
		public ICustomGuiComponent setVisible(boolean bo) {
			return null;
		}

		@Override
		public boolean getEnabled() {
			return true;
		}

		@Override
		public int getType() {
			return -1;
		}
	}
}
