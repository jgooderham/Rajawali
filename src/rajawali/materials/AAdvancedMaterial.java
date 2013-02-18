package rajawali.materials;

import java.util.Stack;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.lights.DirectionalLight;
import rajawali.lights.PointLight;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;

public abstract class AAdvancedMaterial extends AMaterial {
	protected static final int MAX_LIGHTS = RajawaliRenderer.getMaxLights(); 

	public static final String UNI_NORMAL_MATRIX		= "uNMatrix";
	public static final String UNI_AMBIENT_COLOR		= "uAmbientColor";
	public static final String UNI_AMBIENT_INTENSITY	= "uAmbientIntensity";

	public static final String UNI_FOG_COLOR			= "uFogColor";
	public static final String UNI_FOG_NEAR				= "uFogNear";
	public static final String UNI_FOG_FAR				= "uFogFar";
	public static final String UNI_FOG_ENABLED			= "uFogEnabled";

	public static final String UNI_LIGHT_COLOR			= "uLightColor";
	public static final String UNI_LIGHT_POWER			= "uLightPower";
	public static final String UNI_LIGHT_POSITION		= "uLightPosition";
	public static final String UNI_LIGHT_DIRECTION		= "uLightDirection";
	public static final String UNI_LIGHT_ATTENUATION	= "uLightAttenuation";

	public static final String M_FOG_VERTEX_VARS =
			"\n#ifdef FOG_ENABLED\n" +
			"	uniform float " + UNI_FOG_NEAR + ";\n" +
			"	uniform float " + UNI_FOG_FAR + ";\n" +
			"	uniform bool " + UNI_FOG_ENABLED + ";\n" +
			"	varying float vFogDensity;\n" +
			"#endif\n";
	public static final String M_FOG_VERTEX_DENSITY = 
			"\n#ifdef FOG_ENABLED\n" +
			"	vFogDensity = 0.0;\n" +
			"	if (" + UNI_FOG_ENABLED + " == true){\n" +
			"		vFogDensity = (gl_Position.z - " + UNI_FOG_NEAR + ") / (" + UNI_FOG_FAR + " - " + UNI_FOG_NEAR + ");\n" +
			"		vFogDensity = clamp(vFogDensity, 0.0, 1.0);\n" +
			"	}\n" +
			"#endif\n";
	public static final String M_FOG_FRAGMENT_VARS =
			"\n#ifdef FOG_ENABLED\n" +
			"	uniform vec3 " + UNI_FOG_COLOR + ";\n" +
			"	varying float vFogDensity;\n" +
			"#endif\n";
	public static final String M_FOG_FRAGMENT_COLOR =
			"\n#ifdef FOG_ENABLED\n" +
			"	gl_FragColor.rgb = mix(gl_FragColor.rgb, " + UNI_FOG_COLOR + ", vFogDensity);\n" +
			"#endif\n";

	/**
	 * @deprecated Replaced by {@link #M_FOG_VERTEX_DENSITY}
	 */
	@Deprecated
	public static final String M_FOG_VERTEX_DEPTH = M_FOG_VERTEX_DENSITY;

	/**
	 * @deprecated No longer needed (density calculation moved to
	 *             {@link #M_FOG_VERTEX_DENSITY} in the vertex shader)
	 */
	@Deprecated
	public static final String M_FOG_FRAGMENT_CALC = "";

	protected float[] mNormalMatrix;
	protected float[] mTmp, mTmp2;
	protected float[] mAmbientColor, mAmbientIntensity;
	protected float[] mFogColor;
	protected float mFogNear, mFogFar;
	protected boolean mFogEnabled;
	
	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	public AAdvancedMaterial() {
		super();
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader) {
		this(vertexShader, fragmentShader, AMaterial.NONE);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		this(vertexShader, fragmentShader,
				isAnimated ? AMaterial.VERTEX_ANIMATION : AMaterial.NONE);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, int parameters) {
		super(vertexShader, fragmentShader, parameters);
		mNormalMatrix = new float[9];
		mTmp = new float[9];
		mTmp2 = new float[9];
		mAmbientColor = new float[] {.2f, .2f, .2f, 1};
		mAmbientIntensity = new float[] { .3f, .3f, .3f, 1 };		

		if(RajawaliRenderer.isFogEnabled())
			mFogColor = new float[] { .8f, .8f, .8f };
	}
	
	@Override
	public void setLights(Stack<ALight> lights) {
		if(lights.size() != mLights.size() && lights.size() != 0) {
			super.setLights(lights);
			setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		} else if(lights.size() != 0) {
			boolean same = true;
			for(int i=0; i<lights.size(); ++i)
				if(lights.get(i) != mLights.get(i))
					same = false;
			if(!same)
			{
				super.setLights(lights);
				setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
			}
		} else {
			super.setLights(lights);
		}
	}
	
	@Override
	public void setLightParams() {
		int uni;
		for(int i=0; i<mLights.size(); ++i) {
			ALight light = mLights.get(i);
			uni = getUniformHandle(UNI_LIGHT_COLOR+i);
			if (uni > -1)
				GLES20.glUniform3fv(uni, 1, light.getColor(), 0);
			uni = getUniformHandle(UNI_LIGHT_POWER+i);
			if (uni > -1)
				GLES20.glUniform1f(uni, light.getPower());
			uni = getUniformHandle(UNI_LIGHT_POSITION+i);
			if (uni > -1)
				GLES20.glUniform3fv(uni, 1, light.getPositionArray(), 0);
			if(light.getLightType() == ALight.DIRECTIONAL_LIGHT) {
				uni = getUniformHandle(UNI_LIGHT_DIRECTION+i);
				if (uni > -1)
					GLES20.glUniform3fv(uni, 1, ((DirectionalLight)light).getDirection(), 0);
			} else {
				uni = getUniformHandle(UNI_LIGHT_ATTENUATION+i);
				if (uni > -1)
					GLES20.glUniform4fv(uni, 1, ((PointLight)light).getAttenuation(), 0);
			}
		}
	}
	
	public void setAmbientColor(float[] color) {
		mAmbientColor = color;
	}
	
	public void setAmbientColor(Number3D color) {
		setAmbientColor(color.x, color.y, color.z, 1);
	}
	
	public void setAmbientColor(float r, float g, float b, float a) {
		setAmbientColor(new float[] { r, g, b, a });
	}
	
	public void setAmbientColor(int color) {
		setAmbientColor(new float[] { Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f });
	}
	
	public void setAmbientIntensity(float[] intensity) {
		mAmbientIntensity = intensity;
	}
	
	public void setAmbientIntensity(float intensity) {
		mAmbientIntensity[0] = intensity;
		mAmbientIntensity[1] = intensity;
		mAmbientIntensity[2] = intensity;
		mAmbientIntensity[3] = 1;
	}
	
	public void setAmbientIntensity(float r, float g, float b, float a) {
		setAmbientIntensity(new float[] { r, g, b, a });
	}
	
	public void setFogColor(int color) {
		mFogColor[0] = Color.red(color) / 255f;
		mFogColor[1] = Color.green(color) / 255f;
		mFogColor[2] = Color.blue(color) / 255f;
	}
	
	public void setFogNear(float near) {
		mFogNear = near;
	}
	
	public void setFogFar(float far) {
		mFogFar = far;
	}
	
	public void setFogEnabled(boolean enabled) {
		mFogEnabled = enabled;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();

		int uni = getUniformHandle(UNI_AMBIENT_COLOR);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mAmbientColor, 0);
		uni = getUniformHandle(UNI_AMBIENT_INTENSITY);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mAmbientIntensity, 0);

		if(mFogEnabled) {
			uni = getUniformHandle(UNI_FOG_COLOR);
			if (uni > -1)
				GLES20.glUniform3fv(uni, 1, mFogColor, 0);

			uni = getUniformHandle(UNI_FOG_NEAR);
			if (uni > -1)
				GLES20.glUniform1f(uni, mFogNear);

			uni = getUniformHandle(UNI_FOG_FAR);
			if (uni > -1)
				GLES20.glUniform1f(uni, mFogFar);

			uni = getUniformHandle(UNI_FOG_ENABLED);
			if (uni > -1)
				GLES20.glUniform1i(uni, mFogEnabled == true ? GLES20.GL_TRUE : GLES20.GL_FALSE);
		}
	}
	
	@Override
	public void setCamera(Camera camera) {
		super.setCamera(camera);
		if(camera.isFogEnabled()) {
			setFogColor(camera.getFogColor());
			setFogNear(camera.getFogNear());
			setFogFar(camera.getFogFar());
			setFogEnabled(true);
		} else {
			setFogEnabled(false);
		}
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		StringBuffer lightVars = new StringBuffer();
		int numLights = mLights.size();
		
		for(int i=0; i<numLights; ++i) {
			lightVars.append("uniform vec3 ").append(UNI_LIGHT_COLOR).append(i).append(";\n");
			lightVars.append("uniform float ").append(UNI_LIGHT_POWER).append(i).append(";\n");
			lightVars.append("uniform int uLightType").append(i).append(";\n");
			lightVars.append("uniform vec3 ").append(UNI_LIGHT_POSITION).append(i).append(";\n");
			lightVars.append("uniform vec3 ").append(UNI_LIGHT_DIRECTION).append(i).append(";\n");
			lightVars.append("uniform vec4 ").append(UNI_LIGHT_ATTENUATION).append(i).append(";\n");
			lightVars.append("varying float vAttenuation").append(i).append(";\n");
		}
		vertexShader = vertexShader.replace("%LIGHT_VARS%", lightVars.toString());
		fragmentShader = fragmentShader.replace("%LIGHT_VARS%", lightVars.toString());
System.out.println(vertexShader);
System.out.println("\n**********************\n\n");
System.out.println(fragmentShader);
		super.setShaders(vertexShader, fragmentShader);

		registerUniforms(UNI_NORMAL_MATRIX, UNI_AMBIENT_COLOR, UNI_AMBIENT_INTENSITY);
		
		for(int i=0; i<mLights.size(); ++i) {
			registerUniforms(UNI_LIGHT_COLOR+i, UNI_LIGHT_POWER+i, UNI_LIGHT_POSITION+i, UNI_LIGHT_DIRECTION+i, UNI_LIGHT_ATTENUATION+i);
		}
		
		if(RajawaliRenderer.isFogEnabled()) {
			registerUniforms(UNI_FOG_COLOR, UNI_FOG_NEAR, UNI_FOG_FAR, UNI_FOG_ENABLED);
		}
	}
	
	@Override
	public void setModelMatrix(float[] modelMatrix) {
		super.setModelMatrix(modelMatrix);
		
		mTmp2[0] = modelMatrix[0]; mTmp2[1] = modelMatrix[1]; mTmp2[2] = modelMatrix[2]; 
		mTmp2[3] = modelMatrix[4]; mTmp2[4] = modelMatrix[5]; mTmp2[5] = modelMatrix[6];
		mTmp2[6] = modelMatrix[8]; mTmp2[7] = modelMatrix[9]; mTmp2[8] = modelMatrix[10];
		
		mTmpMvMatrix.setValues(mTmp2);
		
		mTmpNormalMatrix.reset();
		mTmpMvMatrix.invert(mTmpNormalMatrix);

		mTmpNormalMatrix.getValues(mTmp);
		mTmp2[0] = mTmp[0]; mTmp2[1] = mTmp[3]; mTmp2[2] = mTmp[6]; 
		mTmp2[3] = mTmp[1]; mTmp2[4] = mTmp[4]; mTmp2[5] = mTmp[7];
		mTmp2[6] = mTmp[2]; mTmp2[7] = mTmp[5]; mTmp2[8] = mTmp[8];
		mTmpNormalMatrix.setValues(mTmp2);
		mTmpNormalMatrix.getValues(mNormalMatrix);

		int uni = getUniformHandle(UNI_NORMAL_MATRIX);
		if (uni > -1)
			GLES20.glUniformMatrix3fv(uni, 1, false, mNormalMatrix, 0);
	}
	
	public void destroy() {
		super.destroy();
		mNormalMatrix = null;
		mTmp = null;
		mTmp2 = null;
		mAmbientColor = null;
		mAmbientIntensity = null;
		mFogColor = null;
		mTmpNormalMatrix = null;
		mTmpMvMatrix = null;
	}
}
