package rajawali;

import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.HashMap;

import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.bounds.BoundingBox;
import rajawali.bounds.BoundingSphere;
import rajawali.renderer.RajawaliRenderer;
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
public abstract class AGeometry3D {
	public static final int FLOAT_SIZE_BYTES = 4;
	public static final int INT_SIZE_BYTES = 4;
	public static final int SHORT_SIZE_BYTES = 2;
	
	protected AGeometry3D mOriginalGeometry;
	/**
	 * Vertex buffer info objects.
	 */
	protected HashMap<String, BufferInfo> mVertexBufferNames;
	protected Collection<BufferInfo> mVertexBufferInfos;

	/**
	 * IntBuffer containing index data. Whether this buffer is used or not depends
	 * on the hardware capabilities. If int buffers aren't supported then short
	 * buffers will be used.
	 * @see RajawaliRenderer.supportsUIntBuffers
	 */
	protected IntBuffer mIndicesInt;
	/**
	 * ShortBuffer containing index data. Whether this buffer is used or not depends
	 * on the hardware capabilities. If int buffers aren't supported then short
	 * buffers will be used.
	 * @see RajawaliRenderer.supportsUIntBuffers
	 */
	protected ShortBuffer mIndicesShort;
	/**
	 * The number of indices currently stored in the index buffer.
	 */
	protected int mNumIndices;
	/**
	 * The number of vertices currently stored in the vertex buffer.
	 */
	protected int mNumVertices;
	/**
	 * Index buffer info object.
	 */
	protected BufferInfo mIndexBufferInfo;
	
	protected boolean mOnlyShortBufferSupported = false;
	/**
	 * The bounding box for this geometry. This is used for collision detection. 
	 */
	protected BoundingBox mBoundingBox;
	/**
	 * The bounding sphere for this geometry. This is used for collision detection.
	 */
	protected BoundingSphere mBoundingSphere;
	public enum BufferType {
		FLOAT_BUFFER,
		INT_BUFFER,
		SHORT_BUFFER
	}

	
	public AGeometry3D() {
		mVertexBufferNames = new HashMap<String, BufferInfo>();
		mVertexBufferInfos = mVertexBufferNames.values();
	}

	/**
	 * Copies another Geometry3D's BufferInfo objects. This means that it
	 * doesn't copy or clone the actual data. It will just use the pointers
	 * to the other Geometry3D's buffers.
	 * @param geom
	 * @see BufferInfo
	 */
	abstract public void copyFromGeometry3D(AGeometry3D geom);

	/**
	 * Creates the actual Buffer objects. 
	 */
	public void createBuffers() {
		boolean supportsUIntBuffers = RajawaliRenderer.supportsUIntBuffers;
		if(mIndicesInt != null && !mOnlyShortBufferSupported && supportsUIntBuffers) {
			mIndicesInt.compact().position(0);
			createBuffer(mIndexBufferInfo, BufferType.INT_BUFFER, mIndicesInt, GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		
		if(mOnlyShortBufferSupported || !supportsUIntBuffers) {
			mOnlyShortBufferSupported = true;
			
			if(mIndicesShort == null && mIndicesInt != null) {
				mIndicesInt.position(0);
				mIndicesShort = ByteBuffer
						.allocateDirect(mNumIndices * SHORT_SIZE_BYTES)
						.order(ByteOrder.nativeOrder()).asShortBuffer();
				
				try {
					for(int i=0; i<mNumIndices; ++i) {
						mIndicesShort.put((short)mIndicesInt.get(i));
					}
				} catch(BufferOverflowException e) {
					RajLog.e("Buffer overflow. Unfortunately your device doesn't supported int type index buffers. The mesh is too big.");
					throw(e);
				}
				
				mIndicesInt.clear();
				mIndicesInt.limit();
				mIndicesInt = null;
			}
			if(mIndicesShort != null) {
				mIndicesShort.compact().position(0);
				createBuffer(mIndexBufferInfo, BufferType.SHORT_BUFFER, mIndicesShort, GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			}
		}

		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Reload is typically called whenever the OpenGL context needs to be restored.
	 * All buffer data is re-uploaded and a new handle is obtained.
	 * It is not recommended to call this function manually.
	 */
	public void reload() {
		if(mOriginalGeometry != null) {
			if(!mOriginalGeometry.isValid()) {
				mOriginalGeometry.reload();
			}
			copyFromGeometry3D(mOriginalGeometry);
		}
		createBuffers();
	}
	
	/**
	 * Checks whether the handle to the vertex buffer is still valid or not.
	 * The handle typically becomes invalid whenever the OpenGL context is lost.
	 * This usually happens when the application regains focus.
	 * @return
	 */
	abstract public boolean isValid();
	
	/**
	 * Creates the vertex and normal buffers only. This is typically used for a 
	 * VertexAnimationObject3D's frames.
	 * 
	 * @see VertexAnimationObject3D
	 */
	abstract public void createVertexAndNormalBuffersOnly();
	
	/**
	 * Creates a buffer and assumes the buffer will be used for static drawing only.
	 * 
	 * @param bufferInfo
	 * @param type
	 * @param buffer
	 * @param target
	 * @param attributeSize
	 *            Number of components for this buffer's attribute
	 */
	public void createBuffer(BufferInfo bufferInfo, BufferType type, Buffer buffer, int target, int attributeSize) {
		createBuffer(bufferInfo, type, buffer, target, bufferInfo.usage, attributeSize, 0, 0);
	}
	
	/**
	 * Creates a buffer and uploads it to the GPU.
	 * 
	 * @param bufferInfo
	 * @param type
	 * @param buffer
	 * @param target
	 * @param usage
	 * @param attributeSize
	 *            Number of components for this buffer's attribute
	 * @param attributeOffset
	 *            If interleaved vertex data, offset into vertex data for this attribute, else 0
	 * @param vertexSize
	 *            If interleaved vertex data, size of vertex for setting stride, else 0
	 */
	public void createBuffer(BufferInfo bufferInfo, BufferType type, Buffer buffer, int target, int usage, int attributeSize, int attributeOffset, int vertexSize) {
		int buff[] = new int[1];
		GLES20.glGenBuffers(1, buff, 0);
		int handle = buff[0];
		int byteSize = FLOAT_SIZE_BYTES;
		if(type == BufferType.SHORT_BUFFER)
			byteSize = SHORT_SIZE_BYTES;
		
		GLES20.glBindBuffer(target, handle);
		GLES20.glBufferData(target, buffer.limit() * byteSize, buffer, usage);
		GLES20.glBindBuffer(target, 0);
		
		bufferInfo.buffer = buffer;
		bufferInfo.bufferHandle = handle;
		bufferInfo.bufferType = type;
		bufferInfo.target = target;
		bufferInfo.byteSize = byteSize;
		bufferInfo.usage = usage;
		bufferInfo.attributeSize = attributeSize;
		bufferInfo.attributeOffset = attributeOffset;
		bufferInfo.vertexSize = vertexSize;
	}
	
	public void createBuffer(BufferInfo bufferInfo) {
		createBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.buffer, bufferInfo.target, bufferInfo.usage, bufferInfo.attributeSize, bufferInfo.attributeOffset, bufferInfo.vertexSize);
	}
	
	abstract public void validateBuffers();
	
	/**
	 * Specifies the expected usage pattern of the data store. The symbolic constant must be GLES20.GL_STREAM_DRAW, GLES20.GL_STREAM_READ, GLES20.GL_STREAM_COPY, GLES20.GL_STATIC_DRAW, GLES20.GL_STATIC_READ, GLES20.GL_STATIC_COPY, GLES20.GL_DYNAMIC_DRAW, GLES20.GL_DYNAMIC_READ, or GLES20.GL_DYNAMIC_COPY.
	 * 
	 * usage is a hint to the GL implementation as to how a buffer object's data store will be accessed. This enables the GL implementation to make more intelligent decisions that may significantly impact buffer object performance. It does not, however, constrain the actual usage of the data store. usage can be broken down into two parts: first, the frequency of access (modification and usage), and second, the nature of that access. The frequency of access may be one of these:
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
	 * @param bufferHandle
	 * @param usage
	 */
	public void changeBufferUsage(BufferInfo bufferInfo, final int usage) {
		GLES20.glDeleteBuffers(1, new int[] { bufferInfo.bufferHandle }, 0);
		createBuffer(bufferInfo, bufferInfo.bufferType, bufferInfo.buffer, bufferInfo.target, bufferInfo.attributeSize);
	}
	
	/**
	 * Change a specific buffer's data at the given offset to the end of the passed buffer.
	 * 
	 * @param bufferInfo
	 * @param newData
	 * @param index
	 */
	public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index) {
	    changeBufferData(bufferInfo, newData, index, newData.capacity());
	}

	/**
	 * Change a specific subset of the buffer's data at the given offset to the given length.
	 * 
	 * @param bufferInfo
	 * @param newData
	 * @param index
	 * @param length
	 */
	public void changeBufferData(BufferInfo bufferInfo, Buffer newData, int index, int length) {
		newData.rewind();
	    GLES20.glBindBuffer(bufferInfo.target, bufferInfo.bufferHandle);
	    GLES20.glBufferSubData(bufferInfo.target, index * bufferInfo.byteSize, length * FLOAT_SIZE_BYTES, newData);
	    GLES20.glBindBuffer(bufferInfo.target, 0);
	}

	public void addBuffer(BufferInfo bufferInfo) {
		registerBuffer(bufferInfo);
		createBuffer(bufferInfo);
	}

	public void registerBuffer(BufferInfo bufferInfo) {
		mVertexBufferNames.put(bufferInfo.attributeName, bufferInfo);
	}

	public BufferInfo getBuffer(String name) {
		return mVertexBufferNames.get(name);
	}

	public HashMap<String, BufferInfo> getVertexBufferNames() {
		return mVertexBufferNames;
	}

	public Collection<BufferInfo> getVertexBufferInfos() {
		return mVertexBufferInfos;
	}

	public void setIndices(int[] indices) {
		if (indices == null) {
			mNumIndices = 0;
			return;
		}
		if(mIndicesInt == null) {
			mIndicesInt = ByteBuffer.allocateDirect(indices.length * INT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asIntBuffer();
			mIndicesInt.put(indices).position(0);
	
			mNumIndices = indices.length;
		} else {
			mIndicesInt.put(indices);
		}
	}
	
	public Buffer getIndices() {
		if(mIndicesInt == null && mOriginalGeometry != null)
			return mOriginalGeometry.getIndices();
		return mOnlyShortBufferSupported ? mIndicesShort : mIndicesInt;
	}
	
	abstract public FloatBuffer getVertices();

	public int getNumIndices() {
		return mNumIndices;
	}

	public int getNumVertices() {
		return mNumVertices;
	}

	public void setColor(float r, float g, float b, float a) {
		setColor(r, g, b, a, false);
	}
	
	abstract public void setColor(float r, float g, float b, float a, boolean createNewBuffer);
	
	public String toString() {
		StringBuffer buff = new StringBuffer(super.toString());

	    if(mIndexBufferInfo != null) buff.append("Index buffer handle: ").append(mIndexBufferInfo.bufferHandle).append("\n");
		for (BufferInfo bufferInfo : mVertexBufferInfos) {
			if (bufferInfo != null)
				buff.append("Attribute \"").append(bufferInfo.attributeName).append("\" buffer handle: ").append(bufferInfo.bufferHandle).append("\n");
		}

		return buff.toString();
	}
	
	public void destroy() {
		int i = 0;
		int[] buffers;
	    if(mIndexBufferInfo != null) {
	    	buffers = new int[mVertexBufferInfos.size()+1];
	    	buffers[0] = mIndexBufferInfo.bufferHandle;
		    if(mIndexBufferInfo.buffer != null) {
		    	mIndexBufferInfo.buffer.clear();
		    	mIndexBufferInfo.buffer = null;
		    }
	    } else
	    	buffers = new int[mVertexBufferInfos.size()];

		for (BufferInfo bufferInfo : mVertexBufferInfos) {
			if (bufferInfo != null) {
				buffers[i+1] = mIndexBufferInfo.bufferHandle;
		    	if(bufferInfo.buffer != null) {
		    		bufferInfo.buffer.clear();
		    		bufferInfo.buffer=null;
		    	}
		    	bufferInfo = null;
		    }
			i++;
		}
	    GLES20.glDeleteBuffers(buffers.length, buffers, 0);

	    if(mIndicesInt != null) { mIndicesInt.clear(); mIndicesInt = null; }
	    if(mIndicesShort != null) { mIndicesShort.clear(); mIndicesShort = null; }
	    if(mOriginalGeometry != null) { mOriginalGeometry.destroy(); mOriginalGeometry = null; }
	}
	
	public boolean hasBoundingBox() {
		return mBoundingBox != null;
	}
	
	/**
	 * Gets the bounding box for this geometry. If there is no current bounding
	 * box it will be calculated. 
	 * 
	 * @return
	 */
	public BoundingBox getBoundingBox() {
		if(mBoundingBox == null)
			mBoundingBox = new BoundingBox(this);
		return mBoundingBox;
	}

	public boolean hasBoundingSphere() {
		return mBoundingSphere != null;
	}
	
	/**
	 * Gets the bounding sphere for this geometry. If there is not current bounding
	 * sphere it will be calculated.
	 * @return
	 */
	public BoundingSphere getBoundingSphere() {
		if(mBoundingSphere == null)
			mBoundingSphere = new BoundingSphere(this);
		return mBoundingSphere;
	}

	/**
	 * Indices whether only short buffers are supported. Not all devices support
	 * integer buffers.
	 * @see RajawaliRenderer.supportsUIntBuffers
	 */
	public boolean areOnlyShortBuffersSupported() {
		return mOnlyShortBufferSupported;
	}
	
	public BufferInfo getIndexBufferInfo() {
		return mIndexBufferInfo;
	}

	public void setIndexBufferInfo(BufferInfo indexBufferInfo) {
		this.mIndexBufferInfo = indexBufferInfo;
	}
	
	public int getNumTriangles() {
		return 0;
	}

	abstract public SerializedObject3D toSerializedObject3D();
}
