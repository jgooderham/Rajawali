package rajawali.materials;


public class SimpleMaterial extends AMaterial {
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
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"uniform sampler2D " + UNI_DIFFUSE_TEX + ";\n" +
		"varying vec4 vColor;\n" +

		"void main() {\n" +
		"#ifdef TEXTURED\n" +
		"	gl_FragColor = texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
		"#else\n" +
	    "	gl_FragColor = vColor;\n" +
	    "#endif\n" +
		"}\n";
	
	public SimpleMaterial() {
		super(mVShader, mFShader, false);
		setShaders();
	}
	
	public SimpleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
		setShaders();
	}
}
