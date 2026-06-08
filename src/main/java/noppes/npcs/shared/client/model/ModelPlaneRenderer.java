package noppes.npcs.shared.client.model;

import net.minecraft.core.Direction;
import noppes.npcs.shared.common.util.NopVector3f;

public class ModelPlaneRenderer extends NopModelPart {
    private int xTexOffs;
    private int yTexOffs;

    public ModelPlaneRenderer(int i, int j) {
        super(64, 64, i, j);
        xTexOffs = i;
        yTexOffs = j;
    }

    public ModelPlaneRenderer(int textX, int textY, int i, int j) {
        super(textX, textY, i, j);
        xTexOffs = i;
        yTexOffs = j;
    }

    public ModelPlaneRenderer mirror(boolean bo){
        this.mirror = bo;
        return this;
    }

    public ModelPlaneRenderer addPlane(float x, float y, float z, int sizeX, int sizeY, NopVector3f scale, Direction d)
    {
        if(d == Direction.DOWN || d == Direction.UP){
            addPlane(x, y, z, sizeX, 0, sizeY, scale, d);
        }
        if(d == Direction.EAST || d == Direction.WEST){
            addPlane(x, y, z, 0, sizeX, sizeY, scale, d);
        }
        if(d == Direction.SOUTH || d == Direction.NORTH){
            addPlane(x, y, z, sizeX, sizeY, 0, scale, d);
        }
        return this;
    }

    public void addBackPlane(float f, float f1, float f2, int i, int j)
    {
    	addPlane(f, f1, f2, i, j, 0, NopVector3f.ONE, Direction.SOUTH);
    }

    public ModelPlaneRenderer addSidePlane(float f, float f1, float f2, int j, int k)
    {
    	addPlane(f, f1, f2, 0, j, k, NopVector3f.ONE, Direction.WEST);
        return this;
    }

    public void addTopPlane(float f, float f1, float f2, int i, int k)
    {
    	addPlane(f, f1, f2, i, 0, k, NopVector3f.ONE, Direction.UP);
    }

    private void addPlane(float x, float y, float z, int dx, int dy, int dz, NopVector3f scale, Direction pos){
        addBox(x, y, z, dx, dy, dz);

        float xx = x + dx;
        float yy = y + dy;
        float zz = z + dz;
        xx *= scale.x;
        yy *= scale.y;
        zz *= scale.z;
        if (mirror)
        {
            float var14 = xx;
            xx = x;
            x = var14;
        }
        PositionTextureVertex lvt_18_2_ = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
        PositionTextureVertex lvt_19_1_ = new PositionTextureVertex(xx, y, z, 0.0F, 8.0F);
        PositionTextureVertex lvt_20_1_ = new PositionTextureVertex(xx, yy, z, 8.0F, 8.0F);
        PositionTextureVertex lvt_21_1_ = new PositionTextureVertex(x, yy, z, 8.0F, 0.0F);
        PositionTextureVertex lvt_22_1_ = new PositionTextureVertex(x, y, zz, 0.0F, 0.0F);
        PositionTextureVertex lvt_23_1_ = new PositionTextureVertex(xx, y, zz, 0.0F, 8.0F);
        PositionTextureVertex lvt_24_1_ = new PositionTextureVertex(xx, yy, zz, 8.0F, 8.0F);
        PositionTextureVertex lvt_25_1_ = new PositionTextureVertex(x, yy, zz, 8.0F, 0.0F);


        ModelBox box = this.cubes.get(this.cubes.size() - 1);
        if(pos == Direction.EAST){
            box.polygons = new TexturedQuad[]{new TexturedQuad(new PositionTextureVertex[]{lvt_23_1_, lvt_19_1_, lvt_20_1_, lvt_24_1_}, xTexOffs, yTexOffs, xTexOffs + dz, yTexOffs + dy, xTexSize, yTexSize, mirror, Direction.WEST)};
        }
        if(pos == Direction.DOWN){
            box.polygons = new TexturedQuad[]{new TexturedQuad(new PositionTextureVertex[]{lvt_23_1_, lvt_22_1_, lvt_18_2_, lvt_19_1_}, xTexOffs, yTexOffs, xTexOffs + dx, yTexOffs + dz, xTexSize, yTexSize, mirror, Direction.DOWN)};
        }
        if(pos == Direction.NORTH){
            box.polygons = new TexturedQuad[]{new TexturedQuad(new PositionTextureVertex[]{lvt_19_1_, lvt_18_2_, lvt_21_1_, lvt_20_1_}, xTexOffs, yTexOffs, xTexOffs + dx, yTexOffs + dy, xTexSize, yTexSize, mirror, Direction.NORTH)};
        }
        if(pos == Direction.UP){
            box.polygons = new TexturedQuad[]{new TexturedQuad(new PositionTextureVertex[]{lvt_20_1_, lvt_21_1_, lvt_25_1_, lvt_24_1_}, xTexOffs + dx, yTexOffs + dz, xTexOffs, yTexOffs, xTexSize, yTexSize, mirror, Direction.UP)};
        }
        if(pos == Direction.WEST){
            box.polygons = new TexturedQuad[]{new TexturedQuad(new PositionTextureVertex[]{lvt_18_2_, lvt_22_1_, lvt_25_1_, lvt_21_1_}, xTexOffs, yTexOffs, xTexOffs + dz, yTexOffs + dy, xTexSize, yTexSize, mirror, Direction.WEST)};
        }
        if(pos == Direction.SOUTH){
            box.polygons = new TexturedQuad[]{new TexturedQuad(new PositionTextureVertex[]{lvt_22_1_, lvt_23_1_, lvt_24_1_, lvt_25_1_}, xTexOffs, yTexOffs, xTexOffs + dx, yTexOffs + dy, xTexSize, yTexSize, mirror, Direction.SOUTH)};
        }
    }
}
