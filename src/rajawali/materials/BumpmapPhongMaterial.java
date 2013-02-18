package rajawali.materials;

import rajawali.lights.ALight;

public class BumpmapPhongMaterial extends PhongMaterial {
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
			"uniform sampler2D " + UNI_NORMAL_TEX + ";\n" +
			"uniform float " + UNI_SHININESS + ";\n" +

			"void main() {\n" +
			"	float Kd = 0.0;" +
			"	float Ks = 0.0;" +
			"	float NdotL = 0.0;\n" +
			"	vec3 N = normalize(vNormal);\n" +
			"	vec3 E = normalize(vEyeVec);\n" +
			"	vec3 L = vec3(0.0);\n" +
			"	vec3 bumpnormal = normalize(texture2D(" + UNI_NORMAL_TEX + ", vTextureCoord).rgb * 2.0 - 1.0);" +
			"	bumpnormal.z = -bumpnormal.z;" +
			"	bumpnormal = normalize(bumpnormal + N);" +
			
			"%LIGHT_CODE%" +
			
			"#ifdef TEXTURED\n" +
		    "	vec4 diffuse  = Kd * texture2D(" + UNI_DIFFUSE_TEX + ", vTextureCoord);\n" +
			"#else\n" +
		    "	vec4 diffuse  = Kd * vColor;\n" +
		    "#endif\n" +
		    "	vec4 specular = Ks * " + UNI_SPECULAR_COLOR + ";\n" + 
		    "	vec4 ambient  = " + UNI_AMBIENT_INTENSITY + " * " + UNI_AMBIENT_COLOR + ";\n" + 
		    "	gl_FragColor = ambient + diffuse + specular;\n" + 
		    M_FOG_FRAGMENT_COLOR +	
			"}";
	
	public BumpmapPhongMaterial() {
		this(false);
	}
	
	public BumpmapPhongMaterial(boolean isAnimated) {
		super(PhongMaterial.mVShader, mFShader, isAnimated);
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
				fc.append("L = normalize(-").append(UNI_LIGHT_DIRECTION).append(i).append(");\n");
			}
		
			fc.append("NdotL = max(dot(bumpnormal, L), 0.1);\n");
			fc.append("Kd += NdotL * vAttenuation").append(i).append(" * ").append(UNI_LIGHT_POWER).append(i).append(";\n"); 
			fc.append("Ks += pow(NdotL, ").append(UNI_SHININESS).append(") * vAttenuation").append(i).append(" * ").append(UNI_LIGHT_POWER).append(i).append(";\n");
		}
		
		super.setShaders(
				vertexShader.replace("%LIGHT_CODE%", vc.toString()), 
				fragmentShader.replace("%LIGHT_CODE%", fc.toString())
				);		
	}
}
