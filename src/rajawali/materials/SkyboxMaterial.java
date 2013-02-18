package rajawali.materials;



public class SkyboxMaterial extends AMaterial {
	protected static final String mVShader = 
			"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +
					
			"attribute vec4 " + ATTR_POSITION + ";\n" +
			"attribute vec3 " + ATTR_TEXTURECOORD + ";\n" +
			"attribute vec4 " + ATTR_COLOR + ";\n" +
			"attribute vec3 " + ATTR_NORMAL + ";\n" +

			"varying vec3 vTextureCoord;\n" +
			"varying vec4 vColor;\n" +		

			"void main() {\n" +
			"	gl_Position = " + UNI_MVP_MATRIX + " * " + ATTR_POSITION + ";\n" +
			"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
			"	vColor = " + ATTR_COLOR + ";\n" +
			"}\n";
		
	protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec3 vTextureCoord;\n" +
			"uniform samplerCube " + UNI_CUBEMAP_TEX + ";\n" +
			"varying vec4 vColor;\n" +

			"void main() {\n" +
			"	gl_FragColor = textureCube(" + UNI_CUBEMAP_TEX + ", vTextureCoord);\n" +
			"}\n";
		
	public SkyboxMaterial() {
		super(mVShader, mFShader, false);
//		mUntouchedVertexShader = new String(mVShader);
//		mUntouchedFragmentShader = new String(mFShader);
		usesCubeMap = true;
//		setShaders(mVShader, mFShader);
	}
}
