package noppes.npcs.shared.client.model.util;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import noppes.npcs.shared.common.util.NopVector2i;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BatchRenderer {

    private static final FloatBuffer MATRIX_BUFFER = MemoryUtil.memAllocFloat(16);
    public static RenderType lastType = null;

    private static final BatchRenderer instance = new BatchRenderer();
    public static BatchRenderer getInstance() {
        return instance;
    }


    private final Map<RenderType, List<Batch>> queue = new LinkedHashMap<>();

    public void add(RenderType renderType, ResourceLocation resource, int id, VertexFormat format, Matrix4f matrix, int vertexCount, NopVector2i texPos, int light, int overlay, float red, float green, float blue, float alpha){
        if(renderType == null){
            renderType = lastType;
        }
        queue.computeIfAbsent(renderType, k -> new LinkedList<>()).add(new Batch(resource, id, format, matrix, vertexCount, texPos, light, overlay, red, green, blue, alpha));
    }

    public void draw(){
        queue.forEach((renderType, batches) -> {
            if (batches.isEmpty()) {
                return;
            }
            RenderSystem.assertOnRenderThread();
            renderType.setupRenderState();
            //RenderSystem.enableTexture();
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            ShaderInstance shaderinstance = RenderSystem.getShader();

            for (Batch b : batches) {
                //Minecraft.getInstance().getTextureManager().bindForSetup(b.resource);
                RenderSystem.setShaderTexture(0, b.resource);
                shaderinstance.COLOR_MODULATOR.set(new float[] {b.red, b.green, b.blue, b.alpha});
                //GL11.glColor4f(b.red, b.green, b.blue, b.alpha);
//                if (textureOffsetX != 0.0F || textureOffsetY != 0.0F) {
//                    RenderSystem.matrixMode(GL21.GL_TEXTURE);
//                    RenderSystem.pushMatrix();
//                    RenderSystem.translatef(textureOffsetX, textureOffsetY, 0.0F);
//                    RenderSystem.matrixMode(GL21.GL_MODELVIEW);
//                }
                //GlStateManager._texParameter(GL21.GL_TEXTURE1, b.overlay1, b.overlay2);
                //GlStateManager._texParameter(GL21.GL_TEXTURE2, b.light1, b.light2);
                //RenderSystem.setTextureMatrix(Matrix4f.createTranslateMatrix(b.texPos.x, b.texPos.y, 0.0F));

                if (shaderinstance.LIGHT0_DIRECTION != null) {
                    shaderinstance.LIGHT0_DIRECTION.set(b.light1);
                }

                if (shaderinstance.LIGHT1_DIRECTION != null) {
                    shaderinstance.LIGHT1_DIRECTION.set(b.light2);
                }
                shaderinstance.MODEL_VIEW_MATRIX.set(b.matrix);

                if (shaderinstance.INVERSE_VIEW_ROTATION_MATRIX != null) {
                    shaderinstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
                }

                if (shaderinstance.FOG_START != null) {
                    shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
                }

                if (shaderinstance.FOG_END != null) {
                    shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
                }

                if (shaderinstance.FOG_COLOR != null) {
                    shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
                }

                if (shaderinstance.FOG_SHAPE != null) {
                    shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
                }

                if (shaderinstance.TEXTURE_MATRIX != null) {
                    shaderinstance.TEXTURE_MATRIX.set(createTranslateMatrix(b.texPos.x, b.texPos.y, 0.0F));
                }

                if (shaderinstance.GAME_TIME != null) {
                    shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
                }

                if (shaderinstance.SCREEN_SIZE != null) {
                    Window window = Minecraft.getInstance().getWindow();
                    shaderinstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
                }

                //shaderinstance.TEXTURE_MATRIX.set(Matrix4f.createTranslateMatrix(b.texPos.x, b.texPos.y, 0.0F));

                RenderSystem.glBindBuffer(GL21.GL_ARRAY_BUFFER, () -> b.id);
                b.format.setupBufferState();
                shaderinstance.apply();
                RenderSystem.drawElements(GL21.GL_TRIANGLES, 0, b.vertexCount);
                shaderinstance.clear();
                b.format.clearBufferState();
                RenderSystem.glBindBuffer(GL21.GL_ARRAY_BUFFER, () -> 0);

//                if (textureOffsetX != 0.0F || textureOffsetY != 0.0F) {
//                    RenderSystem.matrixMode(GL21.GL_TEXTURE);
//                    RenderSystem.popMatrix();
//                    RenderSystem.matrixMode(GL21.GL_MODELVIEW);
//                }
            }

            renderType.clearRenderState();
        });

        queue.clear();
    }

    public static Matrix4f createTranslateMatrix(float p_27654_, float p_27655_, float p_27656_) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.m00(1.0F);
        matrix4f.m11(1.0F);
        matrix4f.m22(1.0F);
        matrix4f.m33(1.0F);
        matrix4f.m03(p_27654_);
        matrix4f.m13(p_27655_);
        matrix4f.m23(p_27656_);
        return matrix4f;
    }

    class Batch{
        final Matrix4f matrix;
        final int vertexCount;
        final ResourceLocation resource;
        final int id;
        final VertexFormat format;
        final int light1, light2;
        final int overlay1, overlay2;
        final float red, green, blue, alpha;
        final NopVector2i texPos;
        public Batch(ResourceLocation resource, int id, VertexFormat format, Matrix4f matrix, int vertexCount, NopVector2i texPos, int light, int overlay, float red, float green, float blue, float alpha){
            this.resource = resource;
            this.id = id;
            this.format = format;
            this.matrix = matrix;
            this.vertexCount = vertexCount;
            this.texPos = texPos;
            this.light1 = light & '\uffff';
            this.light2 = light >> 16 & '\uffff';
            this.overlay1 = overlay & '\uffff';
            this.overlay2 = overlay >> 16 & '\uffff';
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }
}
