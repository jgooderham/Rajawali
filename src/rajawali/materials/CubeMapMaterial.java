package rajawali.materials;

import rajawali.lights.ALight;

public class CubeMapMaterial extends AAdvancedMaterial {
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
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"varying vec3 vNormal;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		
		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"	float dist = 0.0;\n" +
		"	gl_Position = " + UNI_MVP_MATRIX + " * " + ATTR_POSITION + ";\n" +
		"	V = " + UNI_MODEL_MATRIX + " * " + ATTR_POSITION + ";\n" +
		"	vec3 eyeDir = normalize(V.xyz - " + UNI_CAMERA_POSITION + ".xyz);\n" +
		"	N = normalize(" + UNI_NORMAL_MATRIX + " * " + ATTR_NORMAL + ");\n" +
		"	vReflectDir = reflect(eyeDir, N);\n" +
		"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
		"	vNormal = " + ATTR_NORMAL + ";\n" +
		"%LIGHT_CODE%" +
		M_FOG_VERTEX_DENSITY +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 vReflectDir;\n" +
		"uniform samplerCube " + UNI_CUBEMAP_TEX + ";\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec3 vNormal;\n" +
		"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" +
		
		M_FOG_FRAGMENT_VARS +
		"%LIGHT_VARS%" +

		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"%LIGHT_CODE%" +
		"	gl_FragColor = textureCube(" + UNI_CUBEMAP_TEX + ", vReflectDir);\n" +
		"	gl_FragColor += " + UNI_AMBIENT_INTENSITY + " * " + UNI_AMBIENT_COLOR + ";" +
		"	gl_FragColor.rgb *= intensity;\n" +
		M_FOG_FRAGMENT_COLOR +	
		"}\n";
	
	public CubeMapMaterial() {
		super(mVShader, mFShader);
		usesCubeMap = true;
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
	}
}
