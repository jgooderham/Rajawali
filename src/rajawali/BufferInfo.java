package rajawali;

import java.nio.Buffer;

import android.opengl.GLES20;

import rajawali.AGeometry3D.BufferType;

public class BufferInfo {
	public String attributeName;
	public int bufferHandle = -1;
	public BufferType bufferType;
	public Buffer buffer;
	public int target;
	public int byteSize;
	public int usage;
	public int attributeSize;
	public int attributeOffset;
	public int vertexSize;
	public boolean packed = false;
	
	public BufferInfo() {
		this.usage = GLES20.GL_STATIC_DRAW;
	}
	
	public BufferInfo(BufferType bufferType, Buffer buffer) {
		this.bufferType = bufferType;
		this.buffer = buffer;
	}

	public BufferInfo(String attributeName, BufferType bufferType, Buffer buffer) {
		this.attributeName = attributeName;
		this.bufferType = bufferType;
		this.buffer = buffer;
	}

	public BufferInfo(String attributeName, int attributeSize ) {
		this( attributeName, attributeSize, GLES20.GL_STATIC_DRAW );
	}

	public BufferInfo(String attributeName, int attributeSize, boolean packed ) {
		this( attributeName, attributeSize, GLES20.GL_STATIC_DRAW );
		this.packed = packed;
	}

	public BufferInfo(String attributeName, int attributeSize, int usage) {
		this.attributeName = attributeName;
		this.attributeSize = attributeSize;
		this.usage = usage;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb
			.append("Handle: ").append(bufferHandle)
			.append(" type: ").append(bufferType)
			.append(" target: ").append(target)
			.append(" byteSize: ").append(byteSize)
			.append(" usage: ").append(usage)
			.append(" attributeSize: ").append(attributeSize)
			.append(" attributeOffset: ").append(attributeOffset)
			.append(" vertexSize: ").append(vertexSize);
		return sb.toString();
	}
}