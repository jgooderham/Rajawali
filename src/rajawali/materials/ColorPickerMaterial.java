package rajawali.materials;

import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;

public class ColorPickerMaterial extends AMaterial {

	public static final String UNI_PICKING_COLOR = "uPickingColor";

	protected static final String mVShader = 
		"uniform mat4 " + UNI_MVP_MATRIX + ";\n" +
		"uniform vec4 " + UNI_PICKING_COLOR + ";\n" +

		"attribute vec4 " + ATTR_POSITION + ";\n" +

		"varying vec4 vColor;\n" +		

		"void main() {\n" +
		"	gl_Position = " + UNI_MVP_MATRIX + " * " + ATTR_POSITION + ";\n" +
		"	vColor = " + UNI_PICKING_COLOR + ";\n" +
		"}\n";
	
	protected static final String mFShader = 
		"precision mediump float;\n" +

		"varying vec4 vColor;\n" +

		"void main() {\n" +
		"	gl_FragColor = vColor;\n" +
		"}\n";
	
	protected float[] mPickingColor;
	
	public ColorPickerMaterial() {
		super(mVShader, mFShader, false);		
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public ColorPickerMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		int uni = getUniformHandle(UNI_PICKING_COLOR);
		if (uni > -1)
			GLES20.glUniform4fv(uni, 1, mPickingColor, 0);
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		registerUniforms(UNI_PICKING_COLOR);
	}
	
	public void setPickingColor(float[] color) {
		mPickingColor = color;
	}
}
