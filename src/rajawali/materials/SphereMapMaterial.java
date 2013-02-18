package rajawali.materials;

import rajawali.lights.ALight;
import android.opengl.GLES20;

public class SphereMapMaterial extends AAdvancedMaterial {

	public static final String UNI_SPHEREMAP_STRENGTH = "uSphereMapStrength";

	protected static final String mVShader = 
		"precision mediump float;\n" +

		"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +
		"uniform mat4 " + UNI_MODEL_MATRIX + ";\n" +
		"uniform mat3 " + UNI_NORMAL_MATRIX + ";\n" +
//		"uniform vec3 uLightPos;\n" +
		"uniform vec3 " + UNI_CAMERA_POSITION + ";\n" +
		"attribute vec4 " + ATTR_POSITION + ";\n" +
		"attribute vec2 " + ATTR_TEXTURECOORD + ";\n" +
		"attribute vec3 " + ATTR_NORMAL + ";\n" +
		"attribute vec4 " + ATTR_COLOR + ";\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec2 vReflectTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"	float dist = 0.0;\n" +
		"	gl_Position = " + UNI_MVP_MATRIX + " * " + ATTR_POSITION + ";\n" +
		"	V = uMMatrix * " + ATTR_POSITION + ";\n" +
		"	vec3 eyeDir = normalize(V.xyz - " + UNI_CAMERA_POSITION + ".xyz);\n" +
		"	N = normalize(" + UNI_NORMAL_MATRIX + " * " + ATTR_NORMAL + ");\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	float m = 2.0 * sqrt(vReflectDir.x*vReflectDir.x + vReflectDir.y*vReflectDir.y + (vReflectDir.z+1.0)*(vReflectDir.z+1.0));\n" +
		"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
		"	vReflectTextureCoord.s = vReflectDir.x/m + 0.5;\n" +
		"	vReflectTextureCoord.t = vReflectDir.y/m + 0.5;\n" +
		"	vNormal = " + ATTR_NORMAL + ";\n" +
		"#ifndef TEXTURED\n" +
		"	vColor = " + ATTR_COLOR + ";\n" +
		"#endif\n" +
		"%LIGHT_CODE%" +
		M_FOG_VERTEX_DENSITY +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"uniform sampler2D " + UNI_DIFFUSE_TEX + ";\n" +
		"uniform sampler2D " + UNI_SPHEREMAP_TEX + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" +
		"uniform float " + UNI_SPHEREMAP_STRENGTH + ";\n" +

		"varying vec2 vReflectTextureCoord;\n" +
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_FRAGMENT_VARS +
		"%LIGHT_VARS%" +

		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"%LIGHT_CODE%" +
		"	vec4 reflColor = texture2D(" + UNI_SPHEREMAP_TEX + ", vReflectTextureCoord);\n" +
		"#ifdef TEXTURED\n" +		
		"	vec4 diffColor = texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
		"#else\n" +
	    "	vec4 diffColor = vColor;\n" +
	    "#endif\n" +
		"	gl_FragColor = diffColor + reflColor * " + UNI_SPHEREMAP_STRENGTH + ";\n" +
		"	gl_FragColor += " + UNI_AMBIENT_INTENSITY + " * " + UNI_AMBIENT_COLOR + ";" +
		"	gl_FragColor.rgb *= intensity;\n" +
		M_FOG_FRAGMENT_COLOR +	
		"}\n";
	
	private float mSphereMapStrength = .4f;
	
	public SphereMapMaterial() {
		super(mVShader, mFShader);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		int uni = getUniformHandle(UNI_SPHEREMAP_STRENGTH);
		if (uni > -1)
			GLES20.glUniform1f(uni, mSphereMapStrength);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer sb = new StringBuffer();
		StringBuffer vc = new StringBuffer();

		sb.append("vec3 L = vec3(0.0);\n");

		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				sb.append("L = normalize(").append(UNI_LIGHT_POSITION).append(i).append(" - V.xyz);\n");
				vc.append("dist = distance(V.xyz, ").append(UNI_LIGHT_POSITION).append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (").append(UNI_LIGHT_ATTENUATION).append(i).append("[1] + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[2] * dist + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				sb.append("L = -normalize(").append(UNI_LIGHT_DIRECTION).append(i).append(");");				
			}
			sb.append("intensity += ").append(UNI_LIGHT_POWER).append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", sb.toString()));

		registerUniforms(UNI_SPHEREMAP_STRENGTH);
	}

	public float getSphereMapStrength() {
		return mSphereMapStrength;
	}

	public void setSphereMapStrength(float sphereMapStrength) {
		this.mSphereMapStrength = sphereMapStrength;
	}
}
