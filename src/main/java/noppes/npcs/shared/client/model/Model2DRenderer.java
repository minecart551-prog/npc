package noppes.npcs.shared.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import noppes.npcs.shared.client.model.util.BatchRenderer;
import noppes.npcs.shared.client.model.util.CustomRenderStates;
import noppes.npcs.shared.client.model.util.Polygon;
import noppes.npcs.shared.client.model.util.Vertex;
import noppes.npcs.shared.client.util.ImageDownloadAlt;
import noppes.npcs.shared.client.util.ResourceDownloader;
import noppes.npcs.shared.common.util.NopVector2i;
import noppes.npcs.shared.common.util.NopVector3f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model2DRenderer extends NopModelPart {

    private final float x1, x2, y1, y2;
    private final int width, height;
    private final NopVector2i texPos;
    private float rotationOffsetX, rotationOffsetY, rotationOffsetZ;
    public static ResourceLocation textureOverride = null;
    private ResourceLocation location;

    private float scaleX = 1, scaleY = 1, thickness = 1;

    private final Map<ResourceLocation, Polygon[]> compiled = new HashMap<>();

    public Model2DRenderer(int texWidth, int texHeight, int x, int y, int width, int height, ResourceLocation location){
        super(texWidth, texHeight, 0, 0);
        this.width = width;
        this.height = height;
        this.texPos = new NopVector2i(x, y);

        setTexSize(texWidth, texHeight);

        this.location = location;

        x1 = (float)x / texWidth;
        y1 = (float)y / texHeight;

        x2 = ((float)x + width) / texWidth;
        y2 = ((float)y + height) / texHeight;
        init(location);
    }

    public Polygon[] init(ResourceLocation location){
        Polygon[] polygons = compiled.get(location);
        if(polygons != null || location == null || location.toString().isEmpty()){
            return polygons;
        }

        if(ResourceDownloader.contains(location)){
            return null;
        }
        BufferedImage image = null;
        Resource resource = Minecraft.getInstance().getResourceManager().getResource(location).orElse(null);
        if(resource!=null) {
            try {
                image = ImageIO.read(resource.open());
            } catch (Exception e) {
                AbstractTexture text = Minecraft.getInstance().getTextureManager().getTexture(location, null);
                if (text != null && text instanceof ImageDownloadAlt) {
                    try (FileInputStream input = new FileInputStream(((ImageDownloadAlt) text).cacheFile)) {
                        image = ImageIO.read(input);
                    } catch (Exception ee) {
                    }
                }
            }
        }
        int scaleW = 1;
        int scaleH = 1;
        if(image != null){
            scaleW = Math.max(1, (int)(image.getWidth() / this.xTexSize));
            scaleH = Math.max(1, (int)(image.getHeight() / this.yTexSize));
        }

        int width = this.width * scaleW;
        int height = this.height * scaleH;

        NopVector2i texPos = this.texPos.mul(scaleW, scaleH);

//        float x1 = this.x1 * (image.getWidth() / 64f);
//        float y1 = this.y1 * (image.getHeight() / 64f);
//        float x2 = this.x2 * (image.getWidth() / 64f);
//        float y2 = this.y2 * (image.getHeight() / 64f);

        polygons = new Polygon[6];

        polygons[0] = new Polygon(new Vector3f(0.0F, 0.0F, 1.0F),
                new Vertex(0f, 0f, 0f, x1, y2),
                new Vertex(1f, 0f, 0f, x2, y2),
                new Vertex(1f, 1f, 0f, x2, y1),
                new Vertex(0f, 1f, 0f, x1, y1));

        polygons[1] = new Polygon(new Vector3f(0.0F, 0.0F, -1.0F),
                new Vertex(0f, 1f, -0.0625f, x1, y1),
                new Vertex(1f, 1f, -0.0625f, x2, y1),
                new Vertex(1f, 0f, -0.0625f, x2, y2),
                new Vertex(0f, 0f, -0.0625f, x1, y2));

        List<Vertex> list = new ArrayList<>();
        List<Vertex> list2 = new ArrayList<>();
        for (int k = 0; k < width; ++k)
        {
            float f7 = (float)k / (float)width;
            float f8 = x1 + (x2 - x1) * f7 - 0.5F * (x1 - x2) / (float)width;
            float f9 = f7 + 1.0F / (float)width;


            boolean left = false, right = false;
            if(image == null){
                left = true;
                right = true;
            }
            else{
                try{
                    for(int n = 0; n < height; n++){
                        if((image.getRGB(texPos.x + k, texPos.y + n) >> 24 & 255) < 128){
                            continue;
                        }
                        if(k + 1 < width && (image.getRGB(texPos.x + k + 1, texPos.y + n) >> 24 & 255) < 128){
                            right = true;
                        }
                        else if(k + 1 == width){
                            right = true;
                        }
                        if(k > 0 && (image.getRGB(texPos.x + k - 1, texPos.y + n) >> 24 & 255) < 128){
                            left = true;
                        }
                        else if(k == 0){
                            left = true;
                        }
                    }
                }
                catch(Exception e) {}
            }
            if(left){
                list.add(new Vertex(f7, 0f, -0.0625f, f8, y2));
                list.add(new Vertex(f7, 0f, 0f, f8, y2));
                list.add(new Vertex(f7, 1f, 0f, f8, y1));
                list.add(new Vertex(f7, 1f, -0.0625f, f8, y1));
            }

            if(right){
                list2.add(new Vertex(f9, 1f, -0.0625f, f8, y1));
                list2.add(new Vertex(f9, 1f, 0f, f8, y1));
                list2.add(new Vertex(f9, 0f, 0f, f8, y2));
                list2.add(new Vertex(f9, 0f, -0.0625f, f8, y2));
            }
        }
        polygons[2] = new Polygon(new Vector3f(-1.0F, 0.0F, 0.0F), list.toArray(new Vertex[0]));
        polygons[3] = new Polygon(new Vector3f(1.0F, 0.0F, 0.0F), list2.toArray(new Vertex[0]));

        list = new ArrayList<>();
        list2 = new ArrayList<>();
        for (int k = 0; k < height; ++k)
        {
            float f7 = (float)k / (float)height;
            float f8 = y2 + (y1 - y2) * f7 - 0.5F * (y2 - y1) / (float)height;
            float f9 = f7 + 1.0F / (float)height;

            boolean top = false, bottom = false;
            if(image == null){
                top = true;
                bottom = true;
            }
            else {
                try {
                    for (int n = 0; n < width; n++) {
                        int m = height - k - 1;
                        if ((image.getRGB(texPos.x + n, texPos.y + m) >> 24 & 255) < 128) {
                            continue;
                        }
                        if (m > 0 && (image.getRGB(texPos.x + n, texPos.y + m - 1) >> 24 & 255) < 128) {
                            top = true;
                        } else if (m == 0) {
                            top = true;
                        }
                        if (m + 1 < height && (image.getRGB(texPos.x + n, texPos.y + m + 1) >> 24 & 255) < 128) {
                            bottom = true;
                        } else if (m + 1 == height) {
                            bottom = true;
                        }
                    }
                }
                catch(Exception e){}
            }
            if(bottom){
                list2.add(new Vertex(1f, f7, 0f, x2, f8));
                list2.add(new Vertex(0f, f7, 0f, x1, f8));
                list2.add(new Vertex(0f, f7, -0.0625f, x1, f8));
                list2.add(new Vertex(1f, f7, -0.0625f, x2, f8));
            }

            if(top){
                list.add(new Vertex(0f, f9, 0f, x1, f8));
                list.add(new Vertex(1f, f9, 0f, x2, f8));
                list.add(new Vertex(1f, f9, -0.0625f, x2, f8));
                list.add(new Vertex(0f, f9, -0.0625f, x1, f8));
            }
        }
        polygons[4] = new Polygon(new Vector3f(0.0F, 1.0F, 0.0F), list.toArray(new Vertex[0]));
        polygons[5] = new Polygon(new Vector3f(0.0F, -1.0F, 0.0F), list2.toArray(new Vertex[0]));

        compiled.put(location, polygons);
        return polygons;
    }

    @Override
    public void render(PoseStack mstack, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        this.render(textureOverride != null ? textureOverride : location, mstack, builder, light, overlay, red, green, blue, alpha);
    }

    public void render(ResourceLocation location, PoseStack mstack, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        if(!visible || location == null || location.toString().isEmpty())
            return;
        mstack.pushPose();
        this.translateAndRotate(mstack);
        float f = 0.0625f;
        mstack.translate(rotationOffsetX * f, rotationOffsetY * f, rotationOffsetZ * f);
        mstack.scale(scaleX * width / height, scaleY, thickness);
        mstack.mulPose(Axis.XP.rotationDegrees(180));
        if(mirror){
            mstack.translate(0, 0, -1f * f);
            mstack.mulPose(Axis.YP.rotationDegrees(180));
        }
        renderModel(location, mstack.last().normal(), mstack.last().pose(), builder, light, overlay, red, green, blue, alpha);
        //render(location, mstack, light, overlay,red, green, blue, alpha);
        mstack.popPose();
    }

    VertexBuffer cache;
    public void render(ResourceLocation resource, PoseStack mstack, int light, int overlay, float red, float green, float blue, float alpha) {
        if(!visible || resource == null)
            return;
        Minecraft.getInstance().getTextureManager().bindForSetup(resource);
        RenderType rType = CustomRenderStates.entityCutout(resource);
        RenderSystem.setShader(() -> CustomRenderStates.posTexNormalShader);
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setTextureMatrix(BatchRenderer.createTranslateMatrix(texPos.x, texPos.y, 0.0F));

        if(cache == null){
            cache = new VertexBuffer(VertexBuffer.Usage.STATIC);
            PoseStack mmstack = new PoseStack();
            mmstack.pushPose();
            //MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
            //BufferBuilder bufferbuilder = (BufferBuilder)irendertypebuffer$impl.getBuffer(rType);
            Tesselator t = Tesselator.getInstance();
            BufferBuilder bufferbuilder = t.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);
            //renderModel(mmstack.last().pose(), bufferbuilder, light, overlay, red, green, blue, alpha);
            renderModel(resource, mmstack.last().normal(), mmstack.last().pose(), bufferbuilder, light, overlay, 1, 1, 1, 1);
            this.cache.upload(bufferbuilder.end());
            mmstack.popPose();
            //irendertypebuffer$impl.endBatch();

        }
        mstack.pushPose();
        this.translateAndRotate(mstack);

        float f = 0.0625f;
        mstack.translate(rotationOffsetX * f, rotationOffsetY * f, rotationOffsetZ * f);
        mstack.scale(scaleX * width / height, scaleY, thickness);
        mstack.mulPose(Axis.XP.rotationDegrees(180));
        if(mirror){
            mstack.translate(0, 0, -1f * f);
            mstack.mulPose(Axis.YP.rotationDegrees(180));
        }
        PoseStack.Pose entry = mstack.last();
        Matrix4f matrix = entry.pose();
        //rType.setupRenderState();
        //RenderSystem.enableTexture();

        //this.cache.bind();
        //CustomRenderStates.POS_TEX_NORMAL.setupBufferState();
        //RenderSystem.color4f(1, 0, 0, 1);
        this.cache.drawWithShader(matrix, new Matrix4f(), RenderSystem.getShader());

        //CustomRenderStates.POS_TEX_NORMAL.clearBufferState();
        //rType.clearRenderState();

        mstack.popPose();
    }

    public void renderModel(ResourceLocation resource, Matrix3f matrix3f, Matrix4f matrix4f, VertexConsumer builder, int light, int overlay, float red, float green, float blue, float alpha) {
        Polygon[] polygons = init(resource);
        if(polygons == null){
            return;
        }
        for(int i = 0; i < polygons.length; i++){
            Polygon p = polygons[i];
            Vector3f vector3f = new Vector3f(p.normal.x,p.normal.y,p.normal.z);
            vector3f.mul(matrix3f);

            float nX = vector3f.x();
            float nY = vector3f.y();
            float nZ = vector3f.z();
            for(int j = 0; j < p.vertexes.length; j++){
                Vertex vec = p.vertexes[j];
                Vector4f vector4f = new Vector4f(vec.pos.x,vec.pos.y,vec.pos.z,1.0f);
                vector4f.mul(matrix4f);

                builder.vertex(vector4f.x(), vector4f.y(), vector4f.z());
                builder.color(red, green, blue, alpha);
                builder.uv(vec.texCoords.x, vec.texCoords.y);
                builder.overlayCoords(overlay);
                builder.uv2(light);
                builder.normal(nX, nY, nZ);
                builder.endVertex();
            }
        }

    }
    private void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        Vector4f v = new Vector4f(x, y, z, 1.0F);
        v.mul(matrix);
        builder.vertex(v.x(), v.y(), v.z());
        builder.color(red, green, blue, alpha);
        builder.uv(texU, texV);
        builder.overlayCoords(overlayUV);
        builder.uv2(lightmapUV);
        builder.normal(normalX, normalY, normalZ);
        builder.endVertex();
        //builder.addVertex(v.getX(), v.getY(), v.getZ(), red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
    }

    public Model2DRenderer setRotationOffset(float x, float y, float z){
        rotationOffsetX = x;
        rotationOffsetY = y;
        rotationOffsetZ = z;
        return this;
    }

    public Model2DRenderer setRotationOffset(NopVector3f scale){
        rotationOffsetX = scale.x;
        rotationOffsetY = scale.y;
        rotationOffsetZ = scale.z;
        return this;
    }

    public void setScale(float scale){
        this.scaleX = scale;
        this.scaleY = scale;
    }
    public void setScale(float x, float y){
        this.scaleX = x;
        this.scaleY = y;
    }
    public Model2DRenderer setScale(NopVector3f scale){
        this.scaleX = scale.x;
        this.scaleY = scale.y;
        this.thickness = scale.z;
        return this;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }


//    private void compile(float par1)
//    {
//        GlStateManager.pushMatrix();
//        this.displayList = GL11.glGenLists(1);
//        GL11.glNewList(this.displayList, GL11.GL_COMPILE);
//        GlStateManager.translatef(rotationOffsetX * par1, rotationOffsetY * par1, rotationOffsetZ * par1);
//        GlStateManager.scalef(scaleX * width / height, scaleY, thickness);
//        GlStateManager.rotatef(180, 1F, 0F, 0F);
//        if(mirror){
//            GlStateManager.translatef(0, 0, -1f * par1);
//            GlStateManager.rotatef(180, 0, 1F, 0F);
//        }
//
//        renderItemIn2D(Tesselator.getInstance().getBuilder(), x1, y1, x2, y2, width, height, par1);
//        GL11.glEndList();
//        this.isCompiled = true;
//        GlStateManager.popMatrix();
//    }

    //    @OnlyIn(Dist.CLIENT)
//    private void compile(float par1) {
//        PoseStack mstack = new PoseStack();
//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder bufferbuilder = tessellator.getBuilder();
//
//		renderItemIn2D(Tesselator.getInstance().getBuilder(), x1, y1, x2, y2, width, height, par1);
//        bufferbuilder.finishDrawing();
//        this.cache.upload(bufferbuilder);
//    }
//
//    public static void renderItemIn2D(BufferBuilder worldrenderer, float p_78439_1_, float p_78439_2_, float p_78439_3_, float p_78439_4_, int p_78439_5_, int p_78439_6_, float p_78439_7_){
//        Tesselator tessellator = Tesselator.getInstance();
//        worldrenderer.begin(Mode.QUADS, POSITION_TEX_NORMAL);
//        worldrenderer.vertex(0.0D, 0.0D, 0.0D).uv(p_78439_1_, p_78439_4_).normal(0.0F, 0.0F, 1.0F).endVertex();
//        worldrenderer.vertex(1.0D, 0.0D, 0.0D).uv(p_78439_3_, p_78439_4_).normal(0.0F, 0.0F, 1.0F).endVertex();
//        worldrenderer.vertex(1.0D, 1.0D, 0.0D).uv(p_78439_3_, p_78439_2_).normal(0.0F, 0.0F, 1.0F).endVertex();
//        worldrenderer.vertex(0.0D, 1.0D, 0.0D).uv(p_78439_1_, p_78439_2_).normal(0.0F, 0.0F, 1.0F).endVertex();
//
//        tessellator.end();
//
//        worldrenderer.begin(Mode.QUADS, POSITION_TEX_NORMAL);
//        worldrenderer.vertex(0.0D, 1.0D, 0.0F - p_78439_7_).uv(p_78439_1_, p_78439_2_).normal(0.0F, 0.0F, -1.0F).endVertex();
//        worldrenderer.vertex(1.0D, 1.0D, 0.0F - p_78439_7_).uv(p_78439_3_, p_78439_2_).normal(0.0F, 0.0F, -1.0F).endVertex();
//        worldrenderer.vertex(1.0D, 0.0D, 0.0F - p_78439_7_).uv(p_78439_3_, p_78439_4_).normal(0.0F, 0.0F, -1.0F).endVertex();
//        worldrenderer.vertex(0.0D, 0.0D, 0.0F - p_78439_7_).uv(p_78439_1_, p_78439_4_).normal(0.0F, 0.0F, -1.0F).endVertex();
//        tessellator.end();
//        float f5 = 0.5F * (p_78439_1_ - p_78439_3_) / (float)p_78439_5_;
//        float f6 = 0.5F * (p_78439_4_ - p_78439_2_) / (float)p_78439_6_;
//        worldrenderer.begin(Mode.QUADS, POSITION_TEX_NORMAL);
//
//        int k;
//        float f7;
//        float f8;
//
//        for (k = 0; k < p_78439_5_; ++k)
//        {
//            f7 = (float)k / (float)p_78439_5_;
//            f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
//            worldrenderer.vertex(f7, 0.0D, 0.0F - p_78439_7_).uv(f8, p_78439_4_).normal(-1.0F, 0.0F, 0.0F).endVertex();
//            worldrenderer.vertex(f7, 0.0D, 0.0D).uv(f8, p_78439_4_).normal(-1.0F, 0.0F, 0.0F).endVertex();
//            worldrenderer.vertex(f7, 1.0D, 0.0D).uv(f8, p_78439_2_).normal(-1.0F, 0.0F, 0.0F).endVertex();
//            worldrenderer.vertex(f7, 1.0D, 0.0F - p_78439_7_).uv(f8, p_78439_2_).normal(-1.0F, 0.0F, 0.0F).endVertex();
//        }
//
//        tessellator.end();
//        worldrenderer.begin(Mode.QUADS, POSITION_TEX_NORMAL);
//        float f9;
//
//        for (k = 0; k < p_78439_5_; ++k)
//        {
//            f7 = (float)k / (float)p_78439_5_;
//            f8 = p_78439_1_ + (p_78439_3_ - p_78439_1_) * f7 - f5;
//            f9 = f7 + 1.0F / (float)p_78439_5_;
//            worldrenderer.vertex(f9, 1.0D, 0.0F - p_78439_7_).uv(f8, p_78439_2_).normal(1.0F, 0.0F, 0.0F).endVertex();
//            worldrenderer.vertex(f9, 1.0D, 0.0D).uv(f8, p_78439_2_).normal(1.0F, 0.0F, 0.0F).endVertex();
//            worldrenderer.vertex(f9, 0.0D, 0.0D).uv(f8, p_78439_4_).normal(1.0F, 0.0F, 0.0F).endVertex();
//            worldrenderer.vertex(f9, 0.0D, 0.0F - p_78439_7_).uv(f8, p_78439_4_).normal(1.0F, 0.0F, 0.0F).endVertex();
//        }
//
//        tessellator.end();
//        worldrenderer.begin(Mode.QUADS, POSITION_TEX_NORMAL);
//
//        for (k = 0; k < p_78439_6_; ++k)
//        {
//            f7 = (float)k / (float)p_78439_6_;
//            f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
//            f9 = f7 + 1.0F / (float)p_78439_6_;
//            worldrenderer.vertex(0.0D, f9, 0.0D).uv(p_78439_1_, f8).normal(0.0F, 1.0F, 0.0F).endVertex();
//            worldrenderer.vertex(1.0D, f9, 0.0D).uv(p_78439_3_, f8).normal(0.0F, 1.0F, 0.0F).endVertex();
//            worldrenderer.vertex(1.0D, f9, 0.0F - p_78439_7_).uv(p_78439_3_, f8).normal(0.0F, 1.0F, 0.0F).endVertex();
//            worldrenderer.vertex(0.0D, f9, 0.0F - p_78439_7_).uv(p_78439_1_, f8).normal(0.0F, 1.0F, 0.0F).endVertex();
//        }
//
//        tessellator.end();
//        worldrenderer.begin(Mode.QUADS, POSITION_TEX_NORMAL);
//
//        for (k = 0; k < p_78439_6_; ++k)
//        {
//            f7 = (float)k / (float)p_78439_6_;
//            f8 = p_78439_4_ + (p_78439_2_ - p_78439_4_) * f7 - f6;
//            worldrenderer.vertex(1.0D, f7, 0.0D).uv(p_78439_3_, f8).normal(0.0F, -1.0F, 0.0F).endVertex();
//            worldrenderer.vertex(0.0D, f7, 0.0D).uv(p_78439_1_, f8).normal(0.0F, -1.0F, 0.0F).endVertex();
//            worldrenderer.vertex(0.0D, f7, 0.0F - p_78439_7_).uv(p_78439_1_, f8).normal(0.0F, -1.0F, 0.0F).endVertex();
//            worldrenderer.vertex(1.0D, f7, 0.0F - p_78439_7_).uv(p_78439_3_, f8).normal(0.0F, -1.0F, 0.0F).endVertex();
//        }
//
//        tessellator.end();
//    }

}
