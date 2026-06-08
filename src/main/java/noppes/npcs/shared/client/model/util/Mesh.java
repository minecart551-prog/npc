package noppes.npcs.shared.client.model.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import noppes.npcs.shared.common.util.NopVector3f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL21;

public class Mesh {
	public int[] indices;
	public Vertex[] vertices;
	public NopVector3f[] normals;
	private int vbo = -1;

//	public int getVbo() {
//		if (this.vbo == -1) {
//			if (this.normals == null) {
//				this.normals = new NopVector3f[this.indices.length];
//				for (int i = 0; i < this.normals.length / 3; i++) {
//					Vector3f v1 = this.vertices[this.indices[i * 3]].pos;
//					Vector3f v2 = this.vertices[this.indices[i * 3 + 1]].pos;
//					Vector3f v3 = this.vertices[this.indices[i * 3 + 2]].pos;
//					NopVector3f normal = calcNormal(v1, v2, v3);
//					this.normals[i * 3] = normal;
//					this.normals[i * 3 + 1] = normal;
//					this.normals[i * 3 + 2] = normal;
//				}
//			}
//
//			Tesselator tesselator = Tesselator.getInstance();
//			BufferBuilder bufferBuilder = tesselator.getBuilder();
//
//			bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);
//			for (int i = 0; i < this.indices.length; i++) {
//				Vertex vertex = this.vertices[this.indices[i]];
//				NopVector3f normal = this.normals[i];
//				bufferBuilder.vertex(vertex.pos.x(), vertex.pos.y(), vertex.pos.z());
//				bufferBuilder.uv(vertex.texCoords.x, 1.0F - vertex.texCoords.y);
//				bufferBuilder.normal(normal.x, normal.y, normal.z);
//				bufferBuilder.endVertex();
//			}
//			bufferBuilder.end();
//
//			this.vbo = GL21.glGenBuffers();
//			GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, vbo);
//			//GL21.glBufferData(GL21.GL_ARRAY_BUFFER, bufferBuilder.popNextBuffer().getSecond(), GL21.GL_STATIC_DRAW); TODO method unused
//			GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, 0);
//		}
//
//		return this.vbo;
//	}

	private static NopVector3f calcNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		// Calculate Edges:
		NopVector3f calU = new NopVector3f(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
		NopVector3f calV = new NopVector3f(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());

		NopVector3f output = new NopVector3f(calU.y * calV.z - calU.z * calV.y, calU.z * calV.x - calU.x * calV.z, calU.x * calV.y - calU.y * calV.x);

		return output.normalize();
	}

	public void delete() {
		if (vbo != -1) {
			GL21.glDeleteBuffers(vbo);
			vbo = -1;
		}
	}

}
