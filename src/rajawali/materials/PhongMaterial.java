package rajawali.materials;

import rajawali.lights.ALight;
import rajawali.math.Number3D;
import android.graphics.Color;
import android.opengl.GLES20;


public class PhongMaterial extends AAdvancedMaterial {

	public static final String UNI_SPECULAR_COLOR	= "uSpecularColor";
	public static final String UNI_SHININESS		= "uShininess";

	protected static final String mVShader =
		"precision mediump float;\n" +
		"precision mediump int;\n" +
		"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +
		"uniform mat3 " + UNI_NORMAL_MATRIX + ";\n" +
		"uniform mat4 " + UNI_MODEL_MATRIX + ";\n" +
		"uniform mat4 " + UNI_VIEW_MATRIX + ";\n" +
		
		"attribute vec4 " + ATTR_POSITION + ";\n" +
		"attribute vec3 " + ATTR_NORMAL + ";\n" +
		"attribute vec2 " + ATTR_TEXTURECOORD + ";\n" +
		"attribute vec4 " + ATTR_COLOR + ";\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 vEyeVec;\n" +
		"varying vec4 vColor;\n" +

		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"\n#ifdef VERTEX_ANIM\n" +
		"attribute vec4 " + ATTR_NEXT_FRAME_POSITION + ";\n" +
		"attribute vec3 " + ATTR_NEXT_FRAME_NORMAL + ";\n" +
		"uniform float " + UNI_INTERPOATION + ";\n" +
		"#endif\n\n" +
		
		"void main() {\n" +
		"	float dist = 0.0;\n" +
		"	vec4 position = " + ATTR_POSITION + ";\n" +
		"	vec3 normal = " + ATTR_NORMAL + ";\n" +
		"	#ifdef VERTEX_ANIM\n" +
		"	position = " + ATTR_POSITION + " + " + UNI_INTERPOATION + " * (" + ATTR_NEXT_FRAME_POSITION + " - " + ATTR_POSITION + ");\n" +
		"	normal = " + ATTR_NORMAL + " + " + UNI_INTERPOATION + " * (" + ATTR_NEXT_FRAME_NORMAL + " - " + ATTR_NORMAL + ");\n" +
		"	#endif\n" +
		"	gl_Position = " + UNI_MVP_MATRIX + " * position;\n" +
		"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
		
		"	vEyeVec = -vec3(" + UNI_MODEL_MATRIX + "  * position);\n" +
		"	vNormal = " + UNI_NORMAL_MATRIX + " * normal;\n" +
		
		"%LIGHT_CODE%" +
		
		"	vColor = " + ATTR_COLOR + ";\n" +
		M_FOG_VERTEX_DENSITY +
		"}";
		
	protected static final String mFShader = 
		"precision mediump float;\n" +
		"precision mediump int;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 vEyeVec;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_FRAGMENT_VARS +
		"%LIGHT_VARS%" +
		
		"uniform vec4 " + UNI_SPECULAR_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" + 
		"uniform sampler2D " + UNI_DIFFUSE_TEX + ";\n" +
		"uniform float " + UNI_SHININESS + ";\n" +

		"void main() {\n" +
		"	float Kd = 0.0;\n" +
		"	float Ks = 0.0;\n" +
		"	float NdotL = 0.0;\n" +
		"	vec3 L = vec3(0.0);\n" +
		"	float attenuation = 1.0;\n" +
		
		"	vec3 N = normalize(vNormal);\n" +
		"	vec3 E = normalize(vEyeVec);\n" +

		"%LIGHT_CODE%" +
		
		"#ifdef TEXTURED\n" +
		"	vec4 diffuse = Kd * texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
		"#else\n" +
	    "	vec4 diffuse = Kd * vColor;\n" +
	    "#endif\n" +

	    "	vec4 specular = Ks * " + UNI_SPECULAR_COLOR + ";\n" + 
	    "	vec4 ambient  = " + UNI_AMBIENT_INTENSITY + " * " + UNI_AMBIENT_COLOR + ";\n" + 
	    "	gl_FragColor = ambient + diffuse + specular;\n" + 
	    M_FOG_FRAGMENT_COLOR +
		"}";
	
	protected float[] mSpecularColor;
	protected float mShininess;
	
	public PhongMaterial() {
		this(false);
	}
	
	public PhongMaterial(boolean isAnimated) {
		this(mVShader, mFShader, isAnimated);
	}
	
	public PhongMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
		mSpecularColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		mShininess = 96.0f;//
	}
	
	public PhongMaterial(float[] specularColor, float[] ambientColor, float shininess) {
		this();
		mSpecularColor = specularColor;
		mAmbientColor = ambientColor;
		mShininess = shininess;
	}

	@Override
	public void useProgram() {
		super.useProgram();
		int uni = getUniformHandle(UNI_SPECULAR_COLOR);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mSpecularColor, 0);
		uni = getUniformHandle(UNI_SHININESS);
		if (uni > -1)
			GLES20.glUniform1f(uni, mShininess);
	}
	
	public void setSpecularColor(float[] color) {
		mSpecularColor = color;
	}
	
	public void setSpecularColor(Number3D color) {
		mSpecularColor[0] = color.x;
		mSpecularColor[1] = color.y;
		mSpecularColor[2] = color.z;
		mSpecularColor[3] = 1;
	}
	
	public void setSpecularColor(float r, float g, float b, float a) {
		setSpecularColor(new float[] { r, g, b, a });
	}
	
	public void setSpecularColor(int color) {
		setSpecularColor(new float[] { Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color) });
	}
	
	public void setShininess(float shininess) {
		mShininess = shininess;
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		StringBuffer fc = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);

			if(light.getLightType() == ALight.POINT_LIGHT) {
				fc.append("L = normalize(").append(UNI_LIGHT_POSITION).append(i).append(" + vEyeVec);\n");
				
				vc.append("dist = distance(-vEyeVec, ").append(UNI_LIGHT_POSITION).append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (").append(UNI_LIGHT_ATTENUATION).append(i).append("[1] + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[2] * dist + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				fc.append("L = normalize(-").append(UNI_LIGHT_DIRECTION).append(i).append(");\n");
			}
		
			fc.append("NdotL = max(dot(N, L), 0.1);\n");
			fc.append("Kd += NdotL * vAttenuation").append(i).append(" * ").append(UNI_LIGHT_POWER).append(i).append(";\n"); 
			fc.append("Ks += pow(NdotL, ").append(UNI_SHININESS).append(") * vAttenuation").append(i).append(" * ").append(UNI_LIGHT_POWER).append(i).append(";\n");
		}
System.out.println("phong material *******************");		

		super.setShaders(
				vertexShader.replace("%LIGHT_CODE%", vc.toString()), 
				fragmentShader.replace("%LIGHT_CODE%", fc.toString())
				);

		registerUniforms(UNI_SPECULAR_COLOR, UNI_SHININESS);

System.out.println("phong material *******************");		
	}
}