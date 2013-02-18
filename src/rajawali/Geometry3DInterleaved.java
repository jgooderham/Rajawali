package rajawali;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.materials.AMaterial;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.BufferUtil;
import rajawali.util.RajLog;
import android.opengl.GLES20;

/**
 * This is where the vertex, normal, texture coordinate, color and index data is stored.
 * The data is stored in FloatBuffers, IntBuffers and ShortBuffers. The data is uploaded
 * to the graphics card using Vertex Buffer Objects (VBOs). The data in the FloatBuffers
 * is kept in memory in order to restore the VBOs when the OpenGL context needs to be
 * restored (typically when the application regains focus).
 * <p>
 * An object's Geometry3D and its data can be accessed by calling the getGeometry() and its methods:
 * <pre><code> // Get the geometry instance
 * Geometry3D geom = mMyObject3D.getGeometry();
 * // Get vertices (x, y, z)
 * FloatBuffer verts = geom.getVertices();
 * // Get normals (x, y, z)
 * FloatBuffer normals = geom.getNormals();
 * // Get texture coordinates (u, v)
 * FloatBuffer texCoords = geom.getTextureCoords();
 * // Get colors (r, g, b, a)
 * FloatBuffer colors = geom.getColors();
 * // Get indices. This can be either a ShortBuffer or a FloatBuffer. This depends
 * // on the device it runs on. (See RajawaliRenderer.supportsUIntBuffers)
 * FloatBuffer indices = geom.getIndices();
 * ShortBuffer indices = geom.getIndices();
 * </pre></code>
 * 
 * @see RajawaliRenderer.supportsUIntBuffers
 * @author dennis.ippel
 *
 */
public class Geometry3DInterleaved extends AGeometry3D {
	/**
	 * FloatBuffer containing vertex data (x, y, z)
	 */
	protected FloatBuffer mVertices;
	/**
	 * Size of each vertex in bytes
	 */
	private int mVertexSize;
	/**
	 * A pointer to the original geometry. This is not null when the object has been cloned.
	 * When cloning a BaseObject3D the data isn't copied over, only the handle to the OpenGL
	 * buffers are used.
	 */
	protected Geometry3DInterleaved mOriginalGeometry;
	
	public Geometry3DInterleaved() {
		super();
		mIndexBufferInfo = new BufferInfo();
	}
	
	/**
	 * Copies another Geometry3D's BufferInfo objects. This means that it
	 * doesn't copy or clone the actual data. It will just use the pointers
	 * to the other Geometry3D's buffers.
	 * @param geom
	 * @see BufferInfo
	 */
	@Override
	public void copyFromGeometry3D(AGeometry3D geom) {
		if ( !(geom instanceof Geometry3DInterleaved) ) {
			RajLog.e("[" + this.getClass().getName()
					+ "] Can't copy buffer information from interleaved to separate.");
			throw new RuntimeException(
					"Can't copy buffer information from interleaved to separate.");
		}
		Geometry3DInterleaved g = (Geometry3DInterleaved)geom;
		this.mNumIndices = g.getNumIndices();
		this.mNumVertices = g.getNumVertices();
		this.mVertexBufferNames = g.getVertexBufferNames();
		this.mVertexBufferInfos = g.getVertexBufferInfos();
		this.mIndexBufferInfo = g.getIndexBufferInfo();
		this.mOriginalGeometry = g;
	}
	
	/**
	 * Sets the data. This methods takes two BufferInfo objects which means it'll use another
	 * Geometry3D instance's data (vertices and normals). The remaining parameters are arrays
	 * which will be used to create buffers that are unique to this instance.
	 * <p>
	 * This is typically used with VertexAnimationObject3D instances.
	 * 
	 * @param vertexBufferInfo
	 * @param normalBufferInfo
	 * @param textureCoords
	 * @param colors
	 * @param indices
	 * @see VertexAnimationObject3D
	 */
//	public void setData(BufferInfo vertexBufferInfo, BufferInfo normalBufferInfo,
//			float[] textureCoords, float[] colors, int[] indices) {
//		if(textureCoords == null || textureCoords.length == 0)
//			textureCoords = new float[(mNumVertices / 3) * 2];
//		setTextureCoords(textureCoords);
//		if(colors == null || colors.length == 0)
//			setColors(0xff000000 + (int)(Math.random() * 0xffffff));
//		else
//			setColors(colors);	
//		setIndices(indices);
//		
//		addBuffer( vertexBufferInfo );
//		addBuffer( normalBufferInfo );
//		
//		mOriginalGeometry = null;
//		
//		createBuffers();
//	}
	
	/**
	 * Sets the data. Assumes that the data will never be changed and passes GLES20.GL_STATIC_DRAW
	 * to the OpenGL context when the buffers are created. 
	 * 
	 * @param vertices
	 * @param indices
	 * @see GLES20.GL_STATIC_DRAW
	 */
	public void setData(float[] vertices, int[] indices, BufferInfo... bufferInfos) {
		setData(vertices, indices, GLES20.GL_STATIC_DRAW, bufferInfos);
	}
	
	/**
	 * Sets the data. This method takes an additional parameters that specifies the data used for each buffer.
	 * <p>
	 * Usage is a hint to the GL implementation as to how a buffer object's data store will be accessed. This enables the GL implementation to make more intelligent decisions that may significantly impact buffer object performance. It does not, however, constrain the actual usage of the data store. 
	 * <p>
	 * Usage can be broken down into two parts: first, the frequency of access (modification and usage), and second, the nature of that access. The frequency of access may be one of these:
	 * <p>
	 * STREAM
	 * The data store contents will be modified once and used at most a few times.
	 * <p>
	 * STATIC
	 * The data store contents will be modified once and used many times.
	 * <p>
	 * DYNAMIC
	 * The data store contents will be modified repeatedly and used many times.
	 * <p>
	 * The nature of access may be one of these:
	 * <p>
	 * DRAW
	 * The data store contents are modified by the application, and used as the source for GL drawing and image specification commands.
	 * <p>
	 * READ
	 * The data store contents are modified by reading data from the GL, and used to return that data when queried by the application.
	 * <p>
	 * COPY
	 * The data store contents are modified by reading data from the GL, and used as the source for GL drawing and image specification commands.
	 * 
	 * @param vertices
	 * @param indices
	 * @param indicesUsage
	 * @param bufferInfos 
	 */
	public void setData(float[] vertices, int[] indices, int indicesUsage, BufferInfo... bufferInfos) {
		mIndexBufferInfo.usage = indicesUsage;

		// prime buffer info objects
		mVertexSize = 0;
		for (int i = 0; i < bufferInfos.length; i++) {
			BufferInfo bufferInfo = bufferInfos[i];
			registerBuffer(bufferInfo);
			bufferInfo.attributeOffset = mVertexSize;
			mVertexSize += AGeometry3D.FLOAT_SIZE_BYTES * bufferInfo.attributeSize;
		}
		setVertices(vertices);
		setIndices(indices);
		createBuffers();
	}

	/**
	 * Replace vertex data. Object must already be setup and vertex array must be the same size.
	 * If intending to replace vertex data, BufferInfo.usage should have been defined as DYNAMIC.
	 * 
	 * @param vertices
	 * the new vertex data
	 */
	public void replaceData( float[] vertices ) {
		setVertices(vertices);
		mVertices.compact().position(0);

		// all buffers share the same handle and data store, so grab the first buffer and upload data to GPU
		BufferInfo bufferInfo = mVertexBufferInfos.iterator().next();
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferInfo.bufferHandle);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertices.limit() * FLOAT_SIZE_BYTES, mVertices, bufferInfo.usage);
	}

	/**
	 * Creates the actual Buffer objects. 
	 */
	@Override
	public void createBuffers() {
		// creates index buffers
		super.createBuffers();

		if(mVertices != null) {

			// create a base buffer info object to upload data to GPU
			mVertices.compact().position(0);
			BufferInfo baseBufferInfo = new BufferInfo();
			createBuffer(baseBufferInfo, BufferType.FLOAT_BUFFER, mVertices, GLES20.GL_ARRAY_BUFFER, 0);

			// populate attribute BufferInfo objects with base BufferInfo object data
			for (BufferInfo bufferInfo : mVertexBufferInfos) {
				bufferInfo.buffer = baseBufferInfo.buffer;
				bufferInfo.bufferHandle = baseBufferInfo.bufferHandle;
				bufferInfo.byteSize = baseBufferInfo.byteSize;
				bufferInfo.target = baseBufferInfo.target;
				bufferInfo.vertexSize = mVertexSize;
			}
		}
	}
	
	/**
	 * Checks whether the handle to the vertex buffer is still valid or not.
	 * The handle typically becomes invalid whenever the OpenGL context is lost.
	 * This usually happens when the application regains focus.
	 * @return
	 */
	@Override
	public boolean isValid() {
		return GLES20.glIsBuffer(getBuffer(AMaterial.ATTR_POSITION).bufferHandle);
	}
	
	/**
	 * Creates the vertex and normal buffers only. This is typically used for a 
	 * VertexAnimationObject3D's frames.
	 * 
	 * @see VertexAnimationObject3D
	 */
	@Override
	public void createVertexAndNormalBuffersOnly() {

		mVertices.compact().position(0);

		BufferInfo bufferInfo = new BufferInfo();
		createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mVertices, GLES20.GL_ARRAY_BUFFER, 3);
		bufferInfo.attributeName = AMaterial.ATTR_POSITION;
		bufferInfo.attributeOffset = 0;
		bufferInfo.vertexSize = 6;
		registerBuffer(bufferInfo);

		BufferInfo nBufferInfo = new BufferInfo();
		nBufferInfo.attributeName = AMaterial.ATTR_NORMAL;
		nBufferInfo.buffer = bufferInfo.buffer;
		nBufferInfo.bufferHandle = bufferInfo.bufferHandle;
		nBufferInfo.attributeOffset = 3;
		nBufferInfo.vertexSize = 6;
		registerBuffer(nBufferInfo);

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void validateBuffers() {
		if(mOriginalGeometry != null) return;
		for (BufferInfo bufferInfo : mVertexBufferInfos) {
			if (bufferInfo != null && bufferInfo.bufferHandle == 0)
				createBuffer(bufferInfo);
		}
	}
	
	public void setVertices(float[] vertices) {
		setVertices(vertices, false);
	}
	
	public void setVertices(float[] vertices, boolean override) {
		if(mVertices == null || override) {
			if(mVertices != null) {
				mVertices.clear();
			}
			mVertices = ByteBuffer
					.allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			
			BufferUtil.copy(vertices, mVertices, vertices.length, 0);
			mVertices.position(0);
			mNumVertices = vertices.length / (mVertexSize/FLOAT_SIZE_BYTES);
		} else {
			BufferUtil.copy(vertices, mVertices, vertices.length, 0);
		}
	}
	
	public void setVertices(FloatBuffer vertices) {
		vertices.position(0);
		float[] v = new float[vertices.capacity()];
		vertices.get(v);
		setVertices(v);
	}
	
	@Override
	public FloatBuffer getVertices() {
		if(mOriginalGeometry != null)
			return mOriginalGeometry.getVertices();
		return mVertices;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();

		if(mIndicesInt != null) buff.append("Geometry3DInterleaved indices: ").append(mIndicesInt.capacity());
		if(mVertices != null) buff.append(", vertices: ").append(mVertices.capacity());
		
		return buff.append(super.toString()).toString();
	}
	
	public void destroy() {
		super.destroy();

		if(mVertices != null) mVertices.clear();

	    mVertices=null;
	}

	@Override
	public int getNumTriangles() {
		return mVertices != null ? mVertices.limit() / (3*mVertexSize) : 0;
	}

// TODO: jp - fixme!
	public SerializedObject3D toSerializedObject3D() {
		return null;
//
//		SerializedObject3D ser = new SerializedObject3D(
//				getVertices() != null ? getVertices().capacity() : 0,
//				getNormals() != null ? getNormals().capacity() : 0,
//				getTextureCoords() != null ? getTextureCoords().capacity() : 0,
//				getColors() != null ? getColors().capacity() : 0,
//				getIndices() != null ? getIndices().capacity() : 0);
//
//		int i;
//
//		if (getVertices() != null)
//			for (i = 0; i < getVertices().capacity(); i++)
//				ser.getVertices()[i] = getVertices().get(i);
//		if (getNormals() != null)
//			for (i = 0; i < getNormals().capacity(); i++)
//				ser.getNormals()[i] = getNormals().get(i);
//		if (getTextureCoords() != null)
//			for (i = 0; i < getTextureCoords().capacity(); i++)
//				ser.getTextureCoords()[i] = getTextureCoords().get(i);
//		if (getColors() != null)
//			for (i = 0; i < getColors().capacity(); i++)
//				ser.getColors()[i] = getColors().get(i);
//		if (!areOnlyShortBuffersSupported()) {
//			IntBuffer buff = (IntBuffer) getIndices();
//			for (i = 0; i < getIndices().capacity(); i++)
//				ser.getIndices()[i] = buff.get(i);
//		} else {
//			ShortBuffer buff = (ShortBuffer) getIndices();
//			for (i = 0; i < getIndices().capacity(); i++)
//				ser.getIndices()[i] = buff.get(i);
//		}
//
//		return ser;
	}

	@Override
	public void setColor(float r, float g, float b, float a, boolean createNewBuffer) {
		// TODO Auto-generated method stub
		
	}
}
