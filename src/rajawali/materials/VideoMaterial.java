package rajawali.materials;

/**
 * This only works for API Level 15 and higher.
 * Thanks to Lubomir Panak (@drakh)
 * <p>
 * How to use:
 * <pre><code>
 * protected void initScene() {
 * 		super.initScene();
 * 		mLight = new DirectionalLight(0, 0, 1);
 * 		mCamera.setPosition(0, 0, -17);
 * 		
 * 		VideoMaterial material = new VideoMaterial();
 * 		TextureInfo tInfo = mTextureManager.addVideoTexture();
 * 		
 * 		mTexture = new SurfaceTexture(tInfo.getTextureId());
 * 		
 * 		mMediaPlayer = MediaPlayer.create(getContext(), R.raw.nemo);
 * 		mMediaPlayer.setSurface(new Surface(mTexture));
 * 		mMediaPlayer.start();
 * 		
 * 		BaseObject3D cube = new Plane(2, 2, 1, 1);
 * 		cube.setMaterial(material);
 * 		cube.addTexture(tInfo);
 * 		cube.addLight(mLight);
 * 		addChild(cube);
 * 	}
 * 
 * 	public void onDrawFrame(GL10 glUnused) {
 * 		mTexture.updateTexImage();
 * 		super.onDrawFrame(glUnused);
 * 	}
 * </code></pre>
 *  
 * @author dennis.ippel
 * @author Lubomir Panak (@drakh)
 *
 */
public class VideoMaterial extends AMaterial {
	protected static final String mVShader = 
		"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +

		"attribute vec4 " + ATTR_POSITION + ";\n" +
		"attribute vec2 " + ATTR_TEXTURECOORD + ";\n" +
		"attribute vec4 " + ATTR_COLOR + ";\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec4 vColor;\n" +		
		
		"void main() {\n" +
		"	gl_Position = " + UNI_MVP_MATRIX + " * " + ATTR_POSITION + ";\n" +
		"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
		"	vColor = " + ATTR_COLOR + ";\n" +
		"}\n";
	
	protected static final String mFShader = 
		"#extension GL_OES_EGL_image_external : require\n"+
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform samplerExternalOES " + UNI_DIFFUSE_TEX + ";\n"+
		"varying vec4 vColor;\n" +

		"void main() {\n" +
		"#ifdef TEXTURED\n" +
		"	gl_FragColor = texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
		"#else\n" +
	    "	gl_FragColor = vColor;\n" +
	    "#endif\n" +
		"}\n";
	
	public VideoMaterial() {
		super(mVShader, mFShader, false);
		setShaders();
	}
	
	public VideoMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
		setShaders();
	}
}
