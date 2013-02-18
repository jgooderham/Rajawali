package rajawali.materials;

import rajawali.lights.ALight;

public class DiffuseMaterial extends AAdvancedMaterial {
	protected static final String mVShader = 
		"precision mediump float;\n" +
		"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +
		"uniform mat3 " + UNI_NORMAL_MATRIX + ";\n" +
		"uniform mat4 " + UNI_MODEL_MATRIX + ";\n" +
		"uniform mat4 " + UNI_VIEW_MATRIX + ";\n" +
		
		"attribute vec4 " + ATTR_POSITION + ";\n" +
		"attribute vec3 " + ATTR_NORMAL + ";\n" +
		"attribute vec2 " + ATTR_TEXTURECOORD + ";\n" +
		"attribute vec4 " + ATTR_COLOR + ";\n" +
		
		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
		
		M_FOG_VERTEX_VARS +
		"%LIGHT_VARS%" +
		
		"\n#ifdef VERTEX_ANIM\n" +
		"attribute vec4 " + ATTR_NEXT_FRAME_POSITION + ";\n" +
		"attribute vec3 " + ATTR_NEXT_FRAME_NORMAL + ";\n" +
		"uniform float " + UNI_INTERPOATION + ";\n" +
		"#endif\n\n" +
		
		"void main() {\n" +
		"	vec4 position = " + ATTR_POSITION + ";\n" +
		"	float dist = 0.0;\n" +
		"	vec3 normal = " + ATTR_NORMAL + ";\n" +
		"	#ifdef VERTEX_ANIM\n" +
		"	position = " + ATTR_POSITION + " + " + UNI_INTERPOATION + " * (" + ATTR_NEXT_FRAME_POSITION + " - " + ATTR_POSITION + ");\n" +
		"	normal = " + ATTR_NORMAL + " + " + UNI_INTERPOATION + " * (" + ATTR_NEXT_FRAME_NORMAL + " - " + ATTR_NORMAL + ");\n" +
		"	#endif\n" +

		"	gl_Position = " + UNI_MVP_MATRIX + " * position;\n" +
		"	vTextureCoord = " + ATTR_TEXTURECOORD + ";\n" +
		"	N = normalize(" + UNI_NORMAL_MATRIX + " * normal);\n" +
		"	V = " + UNI_MODEL_MATRIX + " * position;\n" +
		"#ifndef TEXTURED\n" +
		"	vColor = " + ATTR_COLOR + ";\n" +
		"#endif\n" +
		
		"%LIGHT_CODE%" +
		
		M_FOG_VERTEX_DENSITY +
		"}";
		
	protected static final String mFShader =
		"precision mediump float;\n" +

		"varying vec2 vTextureCoord;\n" +
		"varying vec3 N;\n" +
		"varying vec4 V;\n" +
		"varying vec4 vColor;\n" +
 
		"uniform sampler2D " + UNI_DIFFUSE_TEX + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_COLOR + ";\n" +
		"uniform vec4 " + UNI_AMBIENT_INTENSITY + ";\n" +
		
		M_FOG_FRAGMENT_VARS +		
		"%LIGHT_VARS%" +
		
		"void main() {\n" +
		"	float intensity = 0.0;\n" +
		"	float dist = 0.0;\n" +
		"	vec3 L = vec3(0.0);\n" +
		"#ifdef TEXTURED\n" +
		"	gl_FragColor = texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
		"#else\n" +
	    "	gl_FragColor = vColor;\n" +
	    "#endif\n" +

	    "%LIGHT_CODE%" +

		"	gl_FragColor.rgb = " + UNI_AMBIENT_INTENSITY + ".rgb * " + UNI_AMBIENT_COLOR + ".rgb + intensity * gl_FragColor.rgb;\n" +
		M_FOG_FRAGMENT_COLOR +		
		"}";
	
	public DiffuseMaterial() {
		this(false);
	}
	
	public DiffuseMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
	}
	
	public DiffuseMaterial(boolean isAnimated) {
		this(mVShader, mFShader, isAnimated);
	}
	
	public DiffuseMaterial(int parameters) {
		super(mVShader, mFShader, parameters);
	}
	
	public DiffuseMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		StringBuffer sb = new StringBuffer();
		StringBuffer vc = new StringBuffer();
		
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			
			if(light.getLightType() == ALight.POINT_LIGHT) {
				sb.append("L = normalize(").append(UNI_LIGHT_POSITION).append(i).append(" - V.xyz);\n");
				vc.append("dist = distance(V.xyz, ").append(UNI_LIGHT_POSITION).append(i).append(");\n");
				vc.append("vAttenuation").append(i).append(" = 1.0 / (").append(UNI_LIGHT_ATTENUATION).append(i).append("[1] + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[2] * dist + ").append(UNI_LIGHT_ATTENUATION).append(i).append("[3] * dist * dist);\n");
			} else if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				vc.append("vAttenuation").append(i).append(" = 1.0;\n");
				sb.append("L = -normalize(").append(UNI_LIGHT_DIRECTION).append(i).append(");\n");
			}
			//sb.append("gl_FragColor.rgb += uLightColor").append(i).append(";\n");
			sb.append("intensity += ").append(UNI_LIGHT_POWER).append(i).append(" * max(dot(N, L), 0.1) * vAttenuation").append(i).append(";\n");
		}
		
		super.setShaders(vertexShader.replace("%LIGHT_CODE%", vc.toString()), fragmentShader.replace("%LIGHT_CODE%", sb.toString()));
	}
}