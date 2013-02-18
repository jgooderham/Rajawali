package rajawali.materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.TextureManager.TextureType;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;

public abstract class AMaterial {
	public static final int NONE				= 0;
	public static final int VERTEX_ANIMATION 	= 1 << 0;

	public static final String ATTR_POSITION			= "aPosition";
	public static final String ATTR_NORMAL				= "aNormal";
	public static final String ATTR_TEXTURECOORD		= "aTextureCoord";
	public static final String ATTR_COLOR				= "aColor";
	public static final String ATTR_NEXT_FRAME_POSITION	= "aNextFramePosition";
	public static final String ATTR_NEXT_FRAME_NORMAL	= "aNextFrameNormal";

	public static final String UNI_CAMERA_POSITION	= "uCameraPosition";
	public static final String UNI_MVP_MATRIX		= "uMVPMatrix";
	public static final String UNI_MODEL_MATRIX		= "uMMatrix";
	public static final String UNI_VIEW_MATRIX		= "uVMatrix";
	public static final String UNI_INTERPOATION		= "uInterpolation";

	public static final String UNI_DIFFUSE_TEX		= "uDiffuseTexture";
	public static final String UNI_NORMAL_TEX		= "uNormalTexture";
	public static final String UNI_FRAMEBUFFER_TEX	= "uFrameBufferTexture";
	public static final String UNI_DEPTHBUFFER_TEX	= "uDepthBufferTexture";
	public static final String UNI_LOOKUP_TEX		= "uLookupTexture";
	public static final String UNI_CUBEMAP_TEX		= "uCubeMapTexture";
	public static final String UNI_SPHEREMAP_TEX	= "uSphereMapTexture";

	protected String mUntouchedVertexShader;
	protected String mUntouchedFragmentShader;
	protected String mVertexShader;
	protected String mFragmentShader;

	protected int mProgram;
	protected int mVShaderHandle;
	protected int mFShaderHandle;

	protected final HashMap<String, Integer> mAttributes = new HashMap<String, Integer>();
	protected final HashMap<String, Integer> mUniforms = new HashMap<String, Integer>();

	protected Stack<ALight> mLights;
	protected boolean mUseColor = false;

	protected int mNumTextures = 0;
	protected float[] mModelViewMatrix;
	protected float[] mViewMatrix;
	protected float[] mCameraPosArray;
	protected ArrayList<TextureInfo> mTextureInfoList;
	protected boolean usesCubeMap = false;
	
	/**
	 * The maximum number of available textures for this device.
	 */
	private int mMaxTextures;
	private boolean mProgramCreated = false;
	
	protected boolean mVertexAnimationEnabled;
	
	public AMaterial() {
		mTextureInfoList = new ArrayList<TextureInfo>();
		mCameraPosArray = new float[3];
		mLights = new Stack<ALight>();
		mMaxTextures = queryMaxTextures();
	}
	
	public AMaterial(String vertexShader, String fragmentShader, boolean vertexAnimationEnabled) {
		this(vertexShader, fragmentShader, vertexAnimationEnabled ? VERTEX_ANIMATION : NONE);
	}
	
	public AMaterial(String vertexShader, String fragmentShader, int parameters) {
		this();
		mUntouchedVertexShader = vertexShader;
		mUntouchedFragmentShader = fragmentShader;
		mVertexAnimationEnabled = (parameters & VERTEX_ANIMATION) != 0;
	}
	
	public AMaterial(int parameters) {
		this();
		mVertexAnimationEnabled = (parameters & VERTEX_ANIMATION) != 0;
	}
	
	protected int queryMaxTextures() {
		int numTexUnits[] = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, numTexUnits, 0);
		return numTexUnits[0];
	}
	
	public void reload() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		
		mNumTextures = mTextureInfoList.size();

		for(int i=0; i<mNumTextures; i++) {
			if(mTextureInfoList.get(i).getTexture() != null)
				addTexture(mTextureInfoList.get(i), true, true);
		}
	}

	public void setShaders() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		mVertexShader = mVertexAnimationEnabled ? "#define VERTEX_ANIM\n" + vertexShader : vertexShader;
		mVertexShader = mUseColor ? mVertexShader : "#define TEXTURED\n" + mVertexShader;
		mFragmentShader = mUseColor ? fragmentShader : "#define TEXTURED\n" + fragmentShader;

		if(RajawaliRenderer.isFogEnabled())
		{
			mVertexShader = "#define FOG_ENABLED\n" + mVertexShader;
			mFragmentShader = "#define FOG_ENABLED\n" + mFragmentShader;
		}
		
		mProgram = createProgram(mVertexShader, mFragmentShader);
		if (mProgram == 0)
			return;

		registerAttributes(ATTR_POSITION, ATTR_NORMAL, ATTR_TEXTURECOORD, ATTR_COLOR);
		registerUniforms(UNI_CAMERA_POSITION, UNI_MVP_MATRIX, UNI_MODEL_MATRIX, UNI_VIEW_MATRIX);
		
		if(mVertexAnimationEnabled == true) {
			registerAttributes(ATTR_NEXT_FRAME_POSITION, ATTR_NEXT_FRAME_NORMAL);
			registerUniforms(UNI_INTERPOATION);
		}
		
		mProgramCreated = true;

		checkTextureHandles();
	}

	protected int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				RajLog.e("[" +getClass().getName()+ "] Could not compile " + (shaderType == GLES20.GL_FRAGMENT_SHADER ? "fragment" : "vertex") + " shader:");
				RajLog.e("Shader log: " + GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	protected int createProgram(String vertexSource, String fragmentSource) {
		mVShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (mVShaderHandle == 0) {
			return 0;
		}

		mFShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (mFShaderHandle == 0) {
			return 0;
		}

		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, mVShaderHandle);
			GLES20.glAttachShader(program, mFShaderHandle);
			GLES20.glLinkProgram(program);

			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				RajLog.e("Could not link program in " + getClass().getCanonicalName() +": ");
				RajLog.e(GLES20.glGetProgramInfoLog(program));
				RajLog.d("-=-=-= VERTEX SHADER =-=-=-");
				RajLog.d(mVertexShader);
				RajLog.d("-=-=-= FRAGMENT SHADER =-=-=-");
				RajLog.d(mFragmentShader);
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

	protected int getUniformLocation(String name) {
		return GLES20.glGetUniformLocation(mProgram, name);
	}

	protected int getAttribLocation(String name) {
		return GLES20.glGetAttribLocation(mProgram, name);
	}
	
	public void registerUniforms( String... uniforms) {
		for (String name : uniforms) {
			int handle = getUniformLocation(name);
//			if(handle == -1)
//				Log.d(Wallpaper.TAG, "Could not get uniform location for " + name);
//			else
				mUniforms.put(name, handle);
		}
	}

	public void registerAttributes( String... attributes) {
		for (String name : attributes) {
			int handle = getAttribLocation(name);
//			if(handle == -1)
//				Log.d(Wallpaper.TAG, "Could not get attribute location for " + name);
//			else
				mAttributes.put(name, handle);
		}
	}

	public int getAttributeHandle(String name) {
		Integer attr = mAttributes.get( name );
		if (attr == null)
			return -1;
		else
			return attr;
	}

	public int getUniformHandle(String name) {
		Integer uni = mUniforms.get( name );
		if (uni == null)
			return -1;
		else
			return uni;
	}

	public void unload() {
		GLES20.glDeleteShader(mVShaderHandle);
		GLES20.glDeleteShader(mFShaderHandle);
		GLES20.glDeleteProgram(mProgram);
	}
	
	public void destroy() {
		mModelViewMatrix = null;
		mViewMatrix = null;
		mCameraPosArray = null;
		if(mLights != null) mLights.clear();
		if(mTextureInfoList != null) mTextureInfoList.clear();
		unload();
	}

	public void useProgram() {
		if(!mProgramCreated) {
			mMaxTextures = queryMaxTextures();
			reload();
		}
		GLES20.glUseProgram(mProgram);
	}

	public void bindTextures() {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; i++) {
			TextureInfo ti = mTextureInfoList.get(i);
			int type = ti.isCubeMap() ? GLES20.GL_TEXTURE_CUBE_MAP : GLES20.GL_TEXTURE_2D;
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
			GLES20.glBindTexture(type, ti.getTextureId());
			GLES20.glUniform1i(ti.getUniformHandle(), i);
		}
	}

	public void unbindTextures() {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; i++) {
			int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP
					: GLES20.GL_TEXTURE_2D;
			GLES20.glBindTexture(type, 0);
		}
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}

	public ArrayList<TextureInfo> getTextureInfoList() {
		return mTextureInfoList;
	}
	
	public void setTextureInfoList(ArrayList<TextureInfo> textureInfoList) {
		mTextureInfoList = textureInfoList;
	}

	public void addTexture(TextureInfo textureInfo) {
		addTexture(textureInfo, false);
	}
	
	public void addTexture(TextureInfo textureInfo, boolean isExistingTexture) {
		addTexture(textureInfo, isExistingTexture, false);
	}
	
	public void removeTexture(TextureInfo textureInfo) {
		mTextureInfoList.remove(textureInfo);
	}
	
	public void addTexture(TextureInfo textureInfo, boolean isExistingTexture, boolean reload) {
		// -- check if this texture is already in the list
		if(mTextureInfoList.indexOf(textureInfo) > -1 && !reload) return;		
		
		if(mTextureInfoList.size() > mMaxTextures) {
			RajLog.e("[" +getClass().getCanonicalName()+ "] Maximum number of textures for this material has been reached. Maximum number of textures is " + mMaxTextures + ".");
		}
		
		String textureName = "uTexture";

		switch (textureInfo.getTextureType()) {
		case DIFFUSE:
		case VIDEO_TEXTURE:
			textureName = UNI_DIFFUSE_TEX;
			break;
		case BUMP:
			textureName = UNI_NORMAL_TEX;
			break;
		case FRAME_BUFFER:
			textureName = UNI_FRAMEBUFFER_TEX;
			break;
		case DEPTH_BUFFER:
			textureName = UNI_DEPTHBUFFER_TEX;
			break;
		case LOOKUP:
			textureName = UNI_LOOKUP_TEX;
			break;
		case CUBE_MAP:
			textureName = UNI_CUBEMAP_TEX;
			break;
		case SPHERE_MAP:
			textureName = UNI_SPHEREMAP_TEX;
			break;
		}

		// -- check if there are already diffuse texture in the list
		int num = mTextureInfoList.size();
		int numDiffuse = 0;
		for(int i=0; i<num; ++i) {
			TextureInfo ti = mTextureInfoList.get(i);
			if(ti.getTextureType() == TextureType.DIFFUSE)
				numDiffuse++;
		}
		
		// -- if there are already diffuse textures in the list then append a
		//    number (ie the second texture in the list will be called 
		//    "uDiffuseTexture1", the third "uDiffuseTexture2", etc.
		if(numDiffuse > 0 && textureInfo.getTextureType() == TextureType.DIFFUSE)
			textureName += numDiffuse;

		if(isExistingTexture)
			textureName = textureInfo.getTextureName();
		
		if(mProgramCreated) {
			int textureHandle = GLES20.glGetUniformLocation(mProgram, textureName);
			if (textureHandle == -1) {
				RajLog.d("Could not get attrib location for "
						+ textureName + ", " + textureInfo.getTextureType());
			}
			textureInfo.setUniformHandle(textureHandle);
		}
		
		if(!isExistingTexture)
			textureInfo.setTextureName(textureName);
		
		if(textureInfo.getTextureType() != TextureType.SPHERE_MAP) mUseColor = false;
		if(!isExistingTexture) {
			mTextureInfoList.add(textureInfo);
			mNumTextures++;
		}
	}
	
	protected void checkTextureHandles() {
		int num = mTextureInfoList.size();
		for(int i=0; i<num; ++i) {
			TextureInfo ti = mTextureInfoList.get(i);
			if(ti.getUniformHandle() == -1) {
				addTexture(ti, true, true);
			}
		}
	}
	
	public void setBuffer(final BufferInfo bufferInfo) {
		if(checkValidHandle(bufferInfo.bufferHandle, "vertex data: " + bufferInfo.attributeName)){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferInfo.bufferHandle);
			Integer attr = mAttributes.get( bufferInfo.attributeName );
			if (attr == null)
				RajLog.e("[" +getClass().getCanonicalName()+ "] Missing material attribute: "+bufferInfo.attributeName);
			GLES20.glEnableVertexAttribArray(attr);
			fix.android.opengl.GLES20.glVertexAttribPointer(attr, bufferInfo.attributeSize, bufferInfo.packed ? GLES20.GL_UNSIGNED_BYTE : GLES20.GL_FLOAT,
					false, bufferInfo.vertexSize, bufferInfo.attributeOffset);
		}
	}

	public void setVertices(final int vertexBufferHandle) {
		if(checkValidHandle(vertexBufferHandle, "vertex data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
			int attr = mAttributes.get( ATTR_POSITION );
			GLES20.glEnableVertexAttribArray(attr);
			fix.android.opengl.GLES20.glVertexAttribPointer(attr, 3, GLES20.GL_FLOAT, false, 0, 0);
		}
	}

	public void setTextureCoords(int textureCoordBufferHandle) {
		if(checkValidHandle(textureCoordBufferHandle, "texture coordinates"))
			setTextureCoords(textureCoordBufferHandle, false);
	}

	public void setTextureCoords(final int textureCoordBufferHandle,
			boolean hasCubemapTexture) {
		if(checkValidHandle(textureCoordBufferHandle, "texture coordinates"))
		{
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordBufferHandle);
			int attr = mAttributes.get( ATTR_TEXTURECOORD );
			GLES20.glEnableVertexAttribArray(attr);
			fix.android.opengl.GLES20.glVertexAttribPointer(attr, hasCubemapTexture ? 3 : 2, GLES20.GL_FLOAT, false, 0, 0);
		}
	}

	public void setColors(final int colorBufferHandle) {
		if(checkValidHandle(colorBufferHandle, "color data"))
		{
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBufferHandle);
			int attr = mAttributes.get( ATTR_COLOR );
			GLES20.glEnableVertexAttribArray(attr);
			// TODO: check for packed colors here..
			fix.android.opengl.GLES20.glVertexAttribPointer(attr, 4, GLES20.GL_FLOAT, false, 0, 0);
		}
	}

	public void setNormals(final int normalBufferHandle) {
		if(checkValidHandle(normalBufferHandle, "normal data")) {
			int attr = mAttributes.get( ATTR_NORMAL );
			if(checkValidHandle(attr, null)) {
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
				GLES20.glEnableVertexAttribArray(attr);
				fix.android.opengl.GLES20.glVertexAttribPointer(attr, 3, GLES20.GL_FLOAT, false, 0, 0);
			}
		}
	}

	public void setMVPMatrix(float[] mvpMatrix) {
		int uni = getUniformHandle( UNI_MVP_MATRIX );
		if(checkValidHandle(uni, null))//"mvp matrix"))
			GLES20.glUniformMatrix4fv(uni, 1, false, mvpMatrix, 0);
	}

	public void setModelMatrix(float[] modelMatrix) {
		mModelViewMatrix = modelMatrix;
		int uni = getUniformHandle( UNI_MODEL_MATRIX );
		if(checkValidHandle(uni, null))
			GLES20.glUniformMatrix4fv(uni, 1, false, modelMatrix, 0);
	}

	public void setViewMatrix(float[] viewMatrix) {
		mViewMatrix = viewMatrix;
		int uni = getUniformHandle( UNI_VIEW_MATRIX );
		if(checkValidHandle(uni, null))
			GLES20.glUniformMatrix4fv(uni, 1, false, viewMatrix, 0);
	}
	
	public void setInterpolation(float interpolation) {
		int uni = getUniformHandle( UNI_INTERPOATION );
		if(checkValidHandle(uni, "interpolation"))
			GLES20.glUniform1f(uni, interpolation);
	}
	
	public void setNextFrameVertices(final int vertexBufferHandle) {
		if(checkValidHandle(vertexBufferHandle, "NextFrameVertices")){
			int attr = mAttributes.get( ATTR_NEXT_FRAME_POSITION );
			if(checkValidHandle(attr, "maNextFramePositionHandle")){
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
				GLES20.glEnableVertexAttribArray(attr);
				fix.android.opengl.GLES20.glVertexAttribPointer(attr, 3, GLES20.GL_FLOAT, false, 0, 0);
			}
		}
	}
	
	public void setNextFrameNormals(final int normalBufferHandle) {
		if(checkValidHandle(normalBufferHandle, "NextFrameNormals")){
			int attr = mAttributes.get( ATTR_NEXT_FRAME_NORMAL );
			if(checkValidHandle(attr, "maNextFrameNormalHandle")){
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
				GLES20.glEnableVertexAttribArray(attr);
				fix.android.opengl.GLES20.glVertexAttribPointer(attr, 3, GLES20.GL_FLOAT, false, 0, 0);
			}
		}
	}
	
	public boolean checkValidHandle(int handle, String message){
		if(handle >= 0)
			return true;
		if(message != null)
			RajLog.e("[" +getClass().getCanonicalName()+ "] Trying to set "+message+
				" without a valid handle.");
		return false;					
	}
	
	public void setLightParams() {
		
	}
	
	public void setLights(Stack<ALight> lights) {
		if(lights == null || lights.size() == 0)
			return;
		for(int i=0; i<lights.size(); ++i) {
			if(i>=mLights.size()) 
				mLights.add(lights.get(i));
			else
				mLights.set(i, lights.get(i));
		}
	}
	
	public void setCamera(Camera camera) {
		Number3D camPos = camera.getPosition();
		mCameraPosArray[0] = camPos.x;
		mCameraPosArray[1] = camPos.y;
		mCameraPosArray[2] = camPos.z;
		int uni = getUniformHandle( UNI_CAMERA_POSITION );
		if (uni > -1)
			GLES20.glUniform3fv(uni, 1, mCameraPosArray, 0);
	}

	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("[" +getClass().getName()+ "]\n");
		out.append("program: ").append(mProgram).append("\n");
		out.append("vshader handle: ").append(mVShaderHandle).append("\n");
		out.append("fshader handle: ").append(mFShaderHandle).append("\n");
		out.append("program created: ").append(mProgramCreated).append("\n");
		return out.toString();
	}
	
	

	public float[] getModelViewMatrix() {
		return mModelViewMatrix;
	}

	public void copyTexturesTo(AMaterial shader) {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; ++i)
			shader.addTexture(mTextureInfoList.get(i));
	}

	public void setUseColor(boolean value) {
		if(value != mUseColor) {
			mUseColor = value;
			if(mLights.size() > 0 || !(this instanceof AAdvancedMaterial))
				setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		}
		mUseColor = value;
	}
	
	public boolean getUseColor() {
		return mUseColor;
	}
}
