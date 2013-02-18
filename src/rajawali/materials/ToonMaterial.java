package rajawali.materials;

import android.graphics.Color;
import android.opengl.GLES20;

public class ToonMaterial extends DiffuseMaterial {
	
	public static final String UNI_TOON_COLOR0 = "uToonColor0";
	public static final String UNI_TOON_COLOR1 = "uToonColor1";
	public static final String UNI_TOON_COLOR2 = "uToonColor2";
	public static final String UNI_TOON_COLOR3 = "uToonColor3";

	protected static final String mFShader = 
			"precision mediump float;\n" +

			"varying vec2 vTextureCoord;\n" +
			"varying vec3 N;\n" +
			"varying vec4 V;\n" +
			"varying vec4 vColor;\n" +
	 
			"uniform sampler2D " + UNI_DIFFUSE_TEX + ";\n" +
			"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
			"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" + 
			"uniform vec4 " + UNI_TOON_COLOR0 + ";\n" +
			"uniform vec4 " + UNI_TOON_COLOR1 + ";\n" +
			"uniform vec4 " + UNI_TOON_COLOR2 + ";\n" +
			"uniform vec4 " + UNI_TOON_COLOR3 + ";\n" +
			
			M_FOG_FRAGMENT_VARS +
			"%LIGHT_VARS%" +

			"void main() {\n" +
			"	float intensity = 0.0;\n" +
			"	float dist = 0.0;\n" +
			"	vec3 L = vec3(0.0);\n" +
			
			"%LIGHT_CODE%" +
			
			"#ifndef TEXTURED\n" +
			"	vec4 color = vColor;\n" +
			"#else\n" +
			"	vec4 color = vec4(1.0);\n" +
			"#endif\n" +
			"   if(intensity > .95) color = " + UNI_TOON_COLOR0 + ";\n" +
			"   else if(intensity > .5) color = " + UNI_TOON_COLOR1 + ";\n" +
			"   else if(intensity > .25) color = " + UNI_TOON_COLOR2 + ";\n" +
			"   else color = " + UNI_TOON_COLOR3 + ";\n" +
			"	color.rgb *= intensity;\n" +	
			"	color += " + UNI_AMBIENT_INTENSITY + " * " + UNI_AMBIENT_COLOR + ";\n" +
			"	gl_FragColor = color;\n" +
			M_FOG_FRAGMENT_COLOR +	
			"}\n";
	
	protected float[] mToonColor0, mToonColor1, mToonColor2, mToonColor3;
	
	public ToonMaterial() {
		this(false);
	}
	
	public ToonMaterial(boolean isAnimated) {
		super(DiffuseMaterial.mVShader, mFShader, isAnimated);
		mToonColor0 = new float[] { 1, .5f, .5f, 1 };
		mToonColor1 = new float[] { .6f, .3f, .3f, 1 };
		mToonColor2 = new float[] { .4f, .2f, .2f, 1 };
		mToonColor3 = new float[] { .2f, .1f, .1f, 1 };
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		int uni = getUniformHandle(UNI_TOON_COLOR0);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mToonColor0, 0);
		uni = getUniformHandle(UNI_TOON_COLOR1);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mToonColor1, 0);
		uni = getUniformHandle(UNI_TOON_COLOR2);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mToonColor2, 0);
		uni = getUniformHandle(UNI_TOON_COLOR3);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mToonColor3, 0);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		registerUniforms(UNI_TOON_COLOR0, UNI_TOON_COLOR1, UNI_TOON_COLOR2, UNI_TOON_COLOR3);
	}
	
	public void setToonColors(int color0, int color1, int color2, int color3) {
		mToonColor0[0] = Color.red(color0); 
		mToonColor0[1] = Color.green(color0);
		mToonColor0[2] = Color.blue(color0); 
		mToonColor0[3] = Color.alpha(color0); 

		mToonColor1[0] = Color.red(color1); 
		mToonColor1[1] = Color.green(color1);
		mToonColor1[2] = Color.blue(color1); 
		mToonColor1[3] = Color.alpha(color1); 

		mToonColor2[0] = Color.red(color2); 
		mToonColor2[1] = Color.green(color2);
		mToonColor2[2] = Color.blue(color2); 
		mToonColor2[3] = Color.alpha(color2); 
		
		mToonColor3[0] = Color.red(color3); 
		mToonColor3[1] = Color.green(color3);
		mToonColor3[2] = Color.blue(color3); 
		mToonColor3[3] = Color.alpha(color3); 
	}
}
