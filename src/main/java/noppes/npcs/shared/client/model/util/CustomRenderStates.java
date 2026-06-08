package noppes.npcs.shared.client.model.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CustomRenderStates extends RenderStateShard {
	public static final Vector4f WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);

	public static VertexFormat POS_COL_TEX_LIGHT_FADE_NORMAL;
	public static VertexFormat POS_COL_TEX_NORMAL;
	public static final VertexFormat POS_TEX_NORMAL = new VertexFormat(ImmutableMap.of(
			"Position", DefaultVertexFormat.ELEMENT_POSITION,
			"UV0", DefaultVertexFormat.ELEMENT_UV0,
			"Normal", DefaultVertexFormat.ELEMENT_NORMAL,
			"Padding", DefaultVertexFormat.ELEMENT_PADDING));

	public enum BLEND {
		NORMAL(0), ADD(1), SUB(2);
		public final int id;
		BLEND(int value) { this.id = value; }
		public int getValue() { return id; }
	}

	protected static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard("lm_additive_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});

	protected static final TransparencyStateShard SUBTRACTIVE_TRANSPARENCY = new TransparencyStateShard("lm_subtractive_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});

	private static final RenderType[] OBJ_RENDER_TYPES = new RenderType[BLEND.values().length * 2];
	static {
		for (BLEND blend : BLEND.values()) {
			for (int glow = 0; glow < 2; glow++) {
				OBJ_RENDER_TYPES[blend.id * 2 + glow] = RenderType.create("lm_obj_" + blend.toString() + (glow == 1 ? "_glow" : ""), POS_TEX_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, RenderType.CompositeState.builder()
						.setTransparencyState(blend == BLEND.ADD ? ADDITIVE_TRANSPARENCY : (blend == BLEND.SUB ? SUBTRACTIVE_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY))
						//.setDiffuseLightingState(glow == 1 ? NO_DIFFUSE_LIGHTING : DIFFUSE_LIGHTING)
						//.setAlphaState(DEFAULT_ALPHA)
						.setCullState(NO_CULL)
						.setLightmapState(LIGHTMAP)
						.setOverlayState(OVERLAY)
						.createCompositeState(false));
			}
		}
	}
	public static final RenderType OBJ_OUTLINE_RENDER_TYPE = RenderType.create("lm_obj_outline_no_cull", POS_TEX_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, RenderType.CompositeState.builder()
			//.setAlphaState(DEFAULT_ALPHA)
			.setDepthTestState(NO_DEPTH_TEST)
			.setCullState(NO_CULL)
			//.setFogState(NO_FOG)
			.setOutputState(OUTLINE_TARGET)
			//.setTexturingState(new RenderStateShard.TextureStateShard(p_173272_, false, false))
			.createCompositeState(false));

	public CustomRenderStates(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
		super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
	}

	public static RenderType getObjVBORenderType(int blending, boolean glow) {
		return OBJ_RENDER_TYPES[(blending << 1) | (glow ? 1 : 0)];
	}
	protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityCutoutShader);

	public static ShaderInstance posTexNormalShader = null;

	private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize((p_173202_) -> {
		RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(new ShaderStateShard(() -> posTexNormalShader)).setTextureState(new TextureStateShard(p_173202_, false, false)).setTransparencyState(NO_TRANSPARENCY).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
		return RenderType.create("nop_entity_cutout", POS_TEX_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, rendertype$compositestate);
	});
	public static RenderType entityCutout(ResourceLocation p_110444_) {
		return ENTITY_CUTOUT.apply(p_110444_);
	}

	public static RenderType getObjRenderType(ResourceLocation texture, int blending, boolean glow) {
		if(POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
			Map<String, VertexFormatElement> vertexFormatValues = new HashMap<>();
			vertexFormatValues.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
			vertexFormatValues.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
			vertexFormatValues.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
			vertexFormatValues.put("UV1", DefaultVertexFormat.ELEMENT_UV1);
			vertexFormatValues.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
			vertexFormatValues.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
			vertexFormatValues.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
			POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(ImmutableMap.copyOf(vertexFormatValues));
		}

		TransparencyStateShard TransparencyStateShard = TRANSLUCENT_TRANSPARENCY;
		if(blending == BLEND.ADD.getValue()) {
			TransparencyStateShard = ADDITIVE_TRANSPARENCY;
		}
		else if(blending == BLEND.SUB.getValue()) {
			TransparencyStateShard = SUBTRACTIVE_TRANSPARENCY;
		}
//		RenderStateShard.DiffuseLightingState lightingState = DIFFUSE_LIGHTING;
//		if(glow) {
//			lightingState = NO_DIFFUSE_LIGHTING;
//		}
		RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
				.setTextureState(new TextureStateShard(texture, false, false)) // Texture
				.setTransparencyState(TransparencyStateShard)
				//.setDiffuseLightingState(lightingState)
				//.setAlphaState(DEFAULT_ALPHA)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("lm_obj_translucent_no_cull", POS_COL_TEX_LIGHT_FADE_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, renderTypeState);
	}

	public static RenderType getObjColorOnlyRenderType(ResourceLocation texture, int blending, boolean glow) {
		if(POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
			Map<String, VertexFormatElement> vertexFormatValues = new HashMap<>();
			vertexFormatValues.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
			vertexFormatValues.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
			vertexFormatValues.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
			vertexFormatValues.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
			POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(ImmutableMap.copyOf(vertexFormatValues));
		}

		TransparencyStateShard TransparencyStateShard = TRANSLUCENT_TRANSPARENCY;
		if(blending == BLEND.ADD.getValue()) {
			TransparencyStateShard = ADDITIVE_TRANSPARENCY;
		}
		else if(blending == BLEND.SUB.getValue()) {
			TransparencyStateShard = SUBTRACTIVE_TRANSPARENCY;
		}
//		RenderStateShard.DiffuseLightingState lightingState = DIFFUSE_LIGHTING;
//		if(glow) {
//			lightingState = NO_DIFFUSE_LIGHTING;
//		}
		RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
				.setTextureState(new TextureStateShard(texture, false, false)) // Texture
				.setTransparencyState(TransparencyStateShard)
				//.setDiffuseLightingState(lightingState)
				//.setAlphaState(DEFAULT_ALPHA)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("lm_obj_translucent_no_cull", POS_COL_TEX_LIGHT_FADE_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, renderTypeState);
	}

	public static RenderType getObjOutlineRenderType(ResourceLocation texture) {
		if(POS_COL_TEX_LIGHT_FADE_NORMAL == null) {
			Map<String, VertexFormatElement> vertexFormatValues = new HashMap<>();
			vertexFormatValues.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
			vertexFormatValues.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
			vertexFormatValues.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
			vertexFormatValues.put("UV1", DefaultVertexFormat.ELEMENT_UV1);
			vertexFormatValues.put("UV2", DefaultVertexFormat.ELEMENT_UV2);
			vertexFormatValues.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
			vertexFormatValues.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
			POS_COL_TEX_LIGHT_FADE_NORMAL = new VertexFormat(ImmutableMap.copyOf(vertexFormatValues));
		}

		RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
				.setTextureState(new TextureStateShard(texture, false, false))
				.setCullState(NO_CULL)
				.setDepthTestState(NO_DEPTH_TEST)
				//.setAlphaState(DEFAULT_ALPHA)
				//.setTexturingState(OUTLINE_TEXTURING)
				//.setFogState(NO_FOG)
				.setOutputState(OUTLINE_TARGET)
				.createCompositeState(false);
		return RenderType.create("lm_obj_outline_no_cull", POS_COL_TEX_LIGHT_FADE_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, renderTypeState);
	}

	public static RenderType getSpriteRenderType(ResourceLocation texture) {
		if(POS_COL_TEX_NORMAL == null) {
			Map<String, VertexFormatElement> vertexFormatValues = new HashMap<>();
			vertexFormatValues.put("Position", DefaultVertexFormat.ELEMENT_POSITION);
			vertexFormatValues.put("Color", DefaultVertexFormat.ELEMENT_COLOR);
			vertexFormatValues.put("UV0", DefaultVertexFormat.ELEMENT_UV0);
			vertexFormatValues.put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);
			vertexFormatValues.put("Padding", DefaultVertexFormat.ELEMENT_PADDING);
			POS_COL_TEX_NORMAL = new VertexFormat(ImmutableMap.copyOf(vertexFormatValues));
		}

		RenderType.CompositeState renderTypeState = RenderType.CompositeState.builder()
				.setTextureState(new TextureStateShard(texture, false, false))
				//.setAlphaState(DEFAULT_ALPHA)
				.createCompositeState(true);
		return RenderType.create("lm_sprite", POS_COL_TEX_NORMAL, VertexFormat.Mode.QUADS, 256, true, false, renderTypeState);
	}
}
