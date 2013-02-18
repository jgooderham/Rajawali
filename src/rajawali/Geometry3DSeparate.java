package rajawali;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.materials.AMaterial;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.BufferUtil;
import rajawali.util.RajLog;
import android.graphics.Color;
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
public class Geometry3DSeparate extends AGeometry3D {
	/**
	 * FloatBuffer containing vertex data (x, y, z)
	 */
	protected FloatBuffer mVertices;
	/**
	 * FloatBuffer containing normal data (x, y, z) 
	 */
	protected FloatBuffer mNormals;
	/**
	 * FloatBuffer containing texture coordinates (u, v)
	 */
	protected FloatBuffer mTextureCoords;
	protected int mSizeTextureCoords;
	/**
	 * FloatBuffer containing color data (r, g, b, a)
	 */
	protected FloatBuffer mColors;
	/**
	 * A pointer to the original geometry. This is not null when the object has been cloned.
	 * When cloning a BaseObject3D the data isn't copied over, only the handle to the OpenGL
	 * buffers are used.
	 */
	protected Geometry3DSeparate mOriginalGeometry;
	
	public Geometry3DSeparate() {
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
		if ( !(geom instanceof Geometry3DSeparate) ) {
			RajLog.e("[" + this.getClass().getName()
					+ "] Can't copy buffer information from separate to interleaved.");
			throw new RuntimeException(
					"Can't copy buffer information from separate to interleaved.");
		}
		Geometry3DSeparate g = (Geometry3DSeparate)geom;
		this.mNumIndices = g.getNumIndices();
		this.mNumVertices = g.getNumVertices();
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
	public void setData(BufferInfo vertexBufferInfo, BufferInfo normalBufferInfo,
			float[] textureCoords, int sizeTexCoords, float[] colors, int[] indices) {
		if(textureCoords == null || textureCoords.length == 0)
			textureCoords = new float[(mNumVertices / 3) * 2];
		setTextureCoords(textureCoords, sizeTexCoords);
		if(colors == null || colors.length == 0)
			setColors(0xff000000 + (int)(Math.random() * 0xffffff));
		else
			setColors(colors);	
		setIndices(indices);
		
		registerBuffer( vertexBufferInfo );
		registerBuffer( normalBufferInfo );
		
		mOriginalGeometry = null;
		
		createBuffers();
	}
	
	/**
	 * Sets the data. Assumes that the data will never be changed and passes GLES20.GL_STATIC_DRAW
	 * to the OpenGL context when the buffers are created. 
	 * 
	 * @param vertices
	 * @param normals
	 * @param textureCoords
	 * @param colors
	 * @param indices
	 * @see GLES20.GL_STATIC_DRAW
	 */
	public void setData(float[] vertices, float[] normals,
			float[] textureCoords, int sizeTexCoords, float[] colors, int[] indices) {
		setData(vertices, GLES20.GL_STATIC_DRAW, normals, GLES20.GL_STATIC_DRAW, textureCoords, sizeTexCoords, GLES20.GL_STATIC_DRAW, colors, GLES20.GL_STATIC_DRAW, indices, GLES20.GL_STATIC_DRAW);
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
	 * @param verticesUsage
	 * @param normals
	 * @param normalsUsage
	 * @param textureCoords
	 * @param textureCoordsUsage
	 * @param colors
	 * @param colorsUsage
	 * @param indices
	 * @param indicesUsage
	 */
	public void setData(float[] vertices, int verticesUsage, float[] normals, int normalsUsage,
			float[] textureCoords, int sizeTexCoords, int textureCoordsUsage, float[] colors, int colorsUsage, 
			int[] indices, int indicesUsage) {
		mIndexBufferInfo.usage = indicesUsage;
		setVertices(vertices);
		setNormals(normals);
		if(textureCoords == null || textureCoords.length == 0)
			textureCoords = new float[(vertices.length / 3) * 2];
		
		setTextureCoords(textureCoords, sizeTexCoords);
		if(colors == null || colors.length == 0)
			setColors(0xff000000 + (int)(Math.random() * 0xffffff));
		else
			setColors(colors);	
		setIndices(indices);

		// prime buffer info objects
		registerBuffer(new BufferInfo(AMaterial.ATTR_POSITION, 0, verticesUsage));
		registerBuffer(new BufferInfo(AMaterial.ATTR_NORMAL, 0, normalsUsage));
		registerBuffer(new BufferInfo(AMaterial.ATTR_TEXTURECOORD, 0, textureCoordsUsage));
		registerBuffer(new BufferInfo(AMaterial.ATTR_COLOR, 0, colorsUsage));

		// create buffers from info objects
		createBuffers();
	}

	/**
	 * Creates the actual Buffer objects. 
	 */
	@Override
	public void createBuffers() {

		// creates index buffers
		super.createBuffers();

		BufferInfo bufferInfo;
		if(mVertices != null) {
			mVertices.compact().position(0);
			bufferInfo = getBuffer(AMaterial.ATTR_POSITION);
			if ( bufferInfo == null ) {
				bufferInfo = new BufferInfo(AMaterial.ATTR_POSITION, 0, GLES20.GL_STATIC_DRAW);
				registerBuffer(bufferInfo);
			}
			createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mVertices, GLES20.GL_ARRAY_BUFFER, 3);
		}
		if(mNormals != null) {
			mNormals.compact().position(0);
			bufferInfo = getBuffer(AMaterial.ATTR_NORMAL);
			if ( bufferInfo == null ) {
				bufferInfo = new BufferInfo(AMaterial.ATTR_NORMAL, 0, GLES20.GL_STATIC_DRAW);
				registerBuffer(bufferInfo);
			}
			createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mNormals, GLES20.GL_ARRAY_BUFFER, 3);
		}
		if(mTextureCoords != null) {
			mTextureCoords.compact().position(0);
			bufferInfo = getBuffer(AMaterial.ATTR_TEXTURECOORD);
			if ( bufferInfo == null ) {
				bufferInfo = new BufferInfo(AMaterial.ATTR_TEXTURECOORD, 0, GLES20.GL_STATIC_DRAW);
				registerBuffer(bufferInfo);
			}
			createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mTextureCoords, GLES20.GL_ARRAY_BUFFER, mSizeTextureCoords);
		}
		if(mColors != null) {
			mColors.compact().position(0);
			bufferInfo = getBuffer(AMaterial.ATTR_COLOR);
			if ( bufferInfo == null ) {
				bufferInfo = new BufferInfo(AMaterial.ATTR_COLOR, 0, GLES20.GL_STATIC_DRAW);
				registerBuffer(bufferInfo);
			}
			createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mColors, GLES20.GL_ARRAY_BUFFER, 4);
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
		mNormals.compact().position(0);

		BufferInfo bufferInfo = new BufferInfo();
		createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mVertices, GLES20.GL_ARRAY_BUFFER, 3);
		bufferInfo.attributeName = AMaterial.ATTR_POSITION;
		registerBuffer(bufferInfo);

		bufferInfo = new BufferInfo();
		createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mNormals, GLES20.GL_ARRAY_BUFFER, 3);
		bufferInfo.attributeName = AMaterial.ATTR_NORMAL;
		registerBuffer(bufferInfo);

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
		if(mVertices == null || override == true) {
			if(mVertices != null) {
				mVertices.clear();
			}
			mVertices = ByteBuffer
					.allocateDirect(vertices.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			
			BufferUtil.copy(vertices, mVertices, vertices.length, 0);
			mVertices.position(0);
			mNumVertices = vertices.length / 3;
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
	
	public void setNormals(float[] normals) {
		if(mNormals == null) {
			mNormals = ByteBuffer.allocateDirect(normals.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			BufferUtil.copy(normals, mNormals, normals.length, 0);
			mNormals.position(0);
		} else {
			mNormals.position(0);
			BufferUtil.copy(normals, mNormals, normals.length, 0);
			mNormals.position(0);
		}
	}
	
	public void setNormals(FloatBuffer normals) {
		normals.position(0);
		float[] n = new float[normals.capacity()];
		normals.get(n);
		setNormals(n);
	}
	
	public FloatBuffer getNormals() {
		if(mOriginalGeometry != null)
			return mOriginalGeometry.getNormals();
		return mNormals;
	}
	
	public void setTextureCoords(float[] textureCoords, int sizeTexCoords) {
		mSizeTextureCoords = sizeTexCoords;
		if(mTextureCoords == null) {
			mTextureCoords = ByteBuffer
					.allocateDirect(textureCoords.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			BufferUtil.copy(textureCoords, mTextureCoords, textureCoords.length, 0);
			mTextureCoords.position(0);
		} else {
			BufferUtil.copy(textureCoords, mTextureCoords, textureCoords.length, 0);
		}
	}
	
	public FloatBuffer getTextureCoords() {
		if(mTextureCoords == null && mOriginalGeometry != null)
			return mOriginalGeometry.getTextureCoords();
		return mTextureCoords;
	}
	
	public void setColors(int color) {
		setColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
	}
	
	public void setColors(float[] colors) {
		if(mColors == null) {
			mColors = ByteBuffer
					.allocateDirect(colors.length * FLOAT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			BufferUtil.copy(colors, mColors, colors.length, 0);
			mColors.position(0);
		} else {
			BufferUtil.copy(colors, mColors, colors.length, 0);
			mColors.position(0);
		}
	}
	
	public FloatBuffer getColors() {
		if(mColors == null && mOriginalGeometry != null)
			return mOriginalGeometry.getColors();
		return mColors;
	}

	@Override
	public void setColor(float r, float g, float b, float a, boolean createNewBuffer) {
		BufferInfo bufferInfo = getBuffer(AMaterial.ATTR_COLOR);
		if (bufferInfo == null) {
			bufferInfo = new BufferInfo();
			createNewBuffer = true;
		}
		if(mColors == null || mColors.limit() == 0)
		{
			mColors = ByteBuffer.allocateDirect(mNumVertices * 4 * FLOAT_SIZE_BYTES)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
			createNewBuffer = true;
		}
		
		mColors.position(0);
		
		while(mColors.remaining() > 3) {
			mColors.put(r);
			mColors.put(g);
			mColors.put(b);
			mColors.put(a);
		}
		mColors.position(0);
		
		if(createNewBuffer == true) {
			createBuffer(bufferInfo, BufferType.FLOAT_BUFFER, mColors, GLES20.GL_ARRAY_BUFFER, 4);
			bufferInfo.attributeName = AMaterial.ATTR_COLOR;
			registerBuffer(bufferInfo);
		} else {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferInfo.bufferHandle);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mColors.limit() * FLOAT_SIZE_BYTES, mColors, GLES20.GL_STATIC_DRAW);
		}
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();

		if(mIndicesInt != null) buff.append("Geometry3DSeparate indices: ").append(mIndicesInt.capacity());
		if(mVertices != null) buff.append(", vertices: ").append(mVertices.capacity());
		if(mNormals != null) buff.append(", normals: ").append(mNormals.capacity());
		if(mTextureCoords != null) buff.append(", uvs: ").append(mTextureCoords.capacity()).append("\n");

		return buff.append(super.toString()).toString();
	}
	
	public void destroy() {
		super.destroy();

	    if(mVertices != null) mVertices.clear();
	    if(mNormals != null) mNormals.clear();
	    if(mTextureCoords != null) mTextureCoords.clear();
	    if(mColors != null) mColors.clear();

	    mVertices=null;
	    mNormals=null;
	    mTextureCoords=null;
	    mColors=null;
	}

	@Override
	public int getNumTriangles() {
		return mVertices != null ? mVertices.limit() / 9 : 0;
	}

	public SerializedObject3D toSerializedObject3D() {

		SerializedObject3D ser = new SerializedObject3D(
				getVertices() != null ? getVertices().capacity() : 0,
				getNormals() != null ? getNormals().capacity() : 0,
				getTextureCoords() != null ? getTextureCoords().capacity() : 0,
				getColors() != null ? getColors().capacity() : 0,
				getIndices() != null ? getIndices().capacity() : 0);

		int i;

		if (getVertices() != null)
			for (i = 0; i < getVertices().capacity(); i++)
				ser.getVertices()[i] = getVertices().get(i);
		if (getNormals() != null)
			for (i = 0; i < getNormals().capacity(); i++)
				ser.getNormals()[i] = getNormals().get(i);
		if (getTextureCoords() != null)
			for (i = 0; i < getTextureCoords().capacity(); i++)
				ser.getTextureCoords()[i] = getTextureCoords().get(i);
		if (getColors() != null)
			for (i = 0; i < getColors().capacity(); i++)
				ser.getColors()[i] = getColors().get(i);
		if (!areOnlyShortBuffersSupported()) {
			IntBuffer buff = (IntBuffer) getIndices();
			for (i = 0; i < getIndices().capacity(); i++)
				ser.getIndices()[i] = buff.get(i);
		} else {
			ShortBuffer buff = (ShortBuffer) getIndices();
			for (i = 0; i < getIndices().capacity(); i++)
				ser.getIndices()[i] = buff.get(i);
		}

		return ser;
	}
}
