package rajawali.materials;

import rajawali.lights.ALight;
import android.graphics.Color;
import android.opengl.GLES20;


public class GouraudMaterial extends AAdvancedMaterial {

	public static final String UNI_SPECULAR_COLOR		= "uSpecularColor";
	public static final String UNI_SPECULAR_INTENSITY	= "uSpecularIntensity";

	protected static final String mVShader = 
		"precision mediump float;\n" +

		"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +
		"uniform mat3 " + UNI_NORMAL_MATRIX + ";\n" +
		"uniform mat4 " + UNI_MODEL_MATRIX + ";\n" +
		"uniform mat4 " + UNI_VIEW_MATRIX + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" +
		
		"attribute vec4 " + ATTR_POSITION + ";\n" +
		"attribute vec3 " + ATTR_NORMAL + ";\n" +
		"attribute vec2 " + ATTR_TEXTURECOORD + ";\n" +
		"attribute vec4 " + ATTR_COLOR + ";\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying float vSpecularIntensity;\n" +
		"varying float vDiffuseIntensity;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"\n#ifdef VERTEX_ANIM\n" +
		"attribute vec4 " + ATTR_NEXT_FRAME_POSITION + ";\n" +
		"attribute vec3 " + ATTR_NEXT_FRAME_NORMAL + ";\n" +
		"uniform float " + UNI_INTERPOATION + ";\n" +
		"#endif\n\n" +
		
		"void main() {\n" +
		"	vSpecularIntensity = 0.0;\n" +
		"	vDiffuseIntensity = 0.0;\n" +
		"	vec4 position = " + ATTR_POSITION + ";\n" +
		"	vec3 normal = " + ATTR_NORMAL + ";\n" +
		"	#ifdef VERTEX_ANIM\n" +
		"	position = " + ATTR_POSITION + " + " + UNI_INTERPOATION + " * (" + ATTR_NEXT_FRAME_POSITION + " - " + ATTR_POSITION + ");\n" +
		"	normal = " + ATTR_NORMAL + " + " + UNI_INTERPOATION + " * (" + ATTR_NEXT_FRAME_NORMAL + " - " + ATTR_NORMAL + ");\n" +
		"	#endif\n" +
		
		"	gl_Position = " + UNI_MVP_MATRIX + " * position;\n" +
		"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
		
		"	vec3 E = -vec3(" + UNI_MODEL_MATRIX + " * position);\n" +
		"	vec3 N = normalize(" + UNI_NORMAL_MATRIX + " * normal);\n" +
		"	vec3 L = vec3(0.0);\n" +
		"	float dist = 0.0;\n" +
		"	float attenuation = 1.0;\n" +
		"	float NdotL = 0.0;\n" +

		"%LIGHT_CODE%" +
		"	vSpecularIntensity = clamp(vSpecularIntensity, 0.0, 1.0);\n" +
		"#ifndef TEXTURED\n" +
		"	vColor = " + ATTR_COLOR + ";\n" +
		"#endif\n" +
		M_FOG_VERTEX_DENSITY +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"uniform sampler2D " + UNI_DIFFUSE_TEX + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" + 
		"uniform vec4 " + UNI_SPECULAR_COLOR + ";\n" +
		"uniform vec4 " + UNI_SPECULAR_INTENSITY + ";\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying float vSpecularIntensity;\n" +
		"varying float vDiffuseIntensity;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_FRAGMENT_VARS +	
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"#ifdef TEXTURED\n" +
		"	vec4 texColor = texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
		"#else\n" +
	    "	vec4 texColor = vColor;\n" +
	    "#endif\n" +
		"	gl_FragColor = texColor * vDiffuseIntensity + " + UNI_SPECULAR_COLOR + " * vSpecularIntensity * " + UNI_SPECULAR_INTENSITY + ";\n" +
		"	gl_FragColor.a = texColor.a;\n" +
		"	gl_FragColor += " + UNI_AMBIENT_INTENSITY + " * " + UNI_AMBIENT_COLOR + ";\n" +
		M_FOG_FRAGMENT_COLOR +
		"}";
	
	protected float[] mSpecularColor;
	protected float[] mSpecularIntensity;
	
	public GouraudMaterial() {
		this(false);
	}
	
	public GouraudMaterial(boolean isAnimated) {
		super(mVShader, mFShader, isAnimated);
		mSpecularColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		mSpecularIntensity = new float[] { 1f, 1f, 1f, 1.0f };
	}
	
	public GouraudMaterial(float[] specularColor) {
		this();
		mSpecularColor = specularColor;
	}

	@Override
	public void useProgram() {
		super.useProgram();
		int uni = getUniformHandle(UNI_SPECULAR_COLOR);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mSpecularColor, 0);
		uni = getUniformHandle(UNI_SPECULAR_INTENSITY);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mSpecularIntensity, 0);
	}
	
	public void setSpecularColor(float[] color) {
		mSpecularColor = color;
	}
	
	public void setSpecularColor(float r, float g, float b, float a) {
		mSpecularColor[0] = r;
		mSpecularColor[1] = g;
		mSpecularColor[2] = b;
		mSpecularColor[3] = a;
	}
	
	public void setSpecularColor(int color) {
		setSpecularColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f);
	}
	
	public void setSpecularIntensity(float[] intensity) {
		mSpecularIntensity = intensity;
	}
	
	public void setSpecularIntensity(float r, float g, float b, float a) {
		mSpecularIntensity[0] = r;
		mSpecularIntensity[1] = g;
		mSpecularIntensity[2] = b;
		mSpecularIntensity[3] = a;
	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
System.out.println("gouraud setShaders ***************");		
		StringBuffer sb = new StringBuffer();

		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);

			if(light.getLightType() == ALight.POINT_LIGHT) {
				sb.append("L = normalize(").append(UNI_LIGHT_POSITION).append(i).append(" + E);\n");
				sb.append("dist = distance(-E, ").append(UNI_LIGHT_POSITION).append(i).append(");\n");
				sb.append("attenuation = 1.0 / (").append(UNI_LIGHT_ATTENUATION).append(i).append("[1] + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[2] * dist + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				sb.append("L = normalize(-").append(UNI_LIGHT_DIRECTION).append(i).append(");\n");
			}
			sb.append("NdotL = max(dot(N, L), 0.1);\n");
			sb.append("vDiffuseIntensity += NdotL * attenuation * ").append(UNI_LIGHT_POWER).append(i).append(";\n");
			sb.append("vSpecularIntensity += pow(NdotL, 6.0) * attenuation * ").append(UNI_LIGHT_POWER).append(i).append(";\n");
		}
System.out.println("gouraud material *****************");
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", sb.toString()), fragmentShader);

		registerUniforms(UNI_SPECULAR_COLOR, UNI_SPECULAR_INTENSITY);
System.out.println("gouraud material *****************");		
	}
}