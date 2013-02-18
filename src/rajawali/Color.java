package rajawali;

import rajawali.math.Matrix4;
import rajawali.math.Number3D;


public class Color {

	private static final double GAMMA_TO_LINEAR = 2.2f;
	private static final double LINEAR_TO_GAMMA = 0.455f;

	public static final Matrix4 RGB_TO_YIQ = new Matrix4(	0.299f,	0.587f,		0.114f,		0,
																												0.596f,	-0.274f,	-0.321f,	0,
																												0.211f,	-0.523f,	0.311f,		0,
																												0,			0,				0,				1 );

	public static final Matrix4 YIQ_TO_RGB = new Matrix4(	1,			0.956f,		0.621f,		0,
																												1,			-0.272f,	-0.647f,	0,
																												1,			-1.107f,	1.705f,		0,
																												0,			0,				0,				1 );

	public float r, g, b, a;

	private static Number3D tmpVec = new Number3D();
	private static Number3D tmpVec2 = new Number3D();

	public Color() {
		r = g = b = a = 1;
	}

	public Color( float r, float g, float b, float a ) {

		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public void setAll( float r, float g, float b, float a ) {

		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public void setAllFrom( Color color ) {

		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
	}

	public void randomize() {

		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
		a = (float)Math.random();
	}

	public void randomizeRGB() {

		r = (float)Math.random();
		g = (float)Math.random();
		b = (float)Math.random();
	}

	@Override
	public String toString() {
	
		return r + ", " + g + ", " + b + ", " + a;
	}

	public void rotateHue( float radians ) {
//		tmpVec.setAll( Math.pow( r, GAMMA_TO_LINEAR ), Math.pow( g, GAMMA_TO_LINEAR ), Math.pow( b, GAMMA_TO_LINEAR ) );
		tmpVec.setAll( r, g, b );
		RGB_TO_YIQ.multiply( tmpVec, tmpVec2 );
		tmpVec2.rotateX( radians );
		YIQ_TO_RGB.multiply( tmpVec2, tmpVec );
//		this.r = (float) Math.pow( tmpVec.x, LINEAR_TO_GAMMA );
//		this.g = (float) Math.pow( tmpVec.y, LINEAR_TO_GAMMA );
//		this.b = (float) Math.pow( tmpVec.z, LINEAR_TO_GAMMA );
		this.r = tmpVec.x;
		this.g = tmpVec.y;
		this.b = tmpVec.z;
	}

	/**
	 * Transforms RGB color values using Hue-Saturation-Value space
	 * @param color
	 *  color to transform
	 * @param H
	 *  amount to rotate hue
	 * @param S
	 *  saturation multiplier
	 * @param V
	 *  value multiplier
	 */
	public static void transformHSV( Color color, float h, float s, float v ) {

	  float vsu = v * s * (float)Math.cos( h );
	  float vsw = v * s * (float)Math.sin( h );

	  tmpVec.x =	( 0.299f * v + 0.701f * vsu + 0.168f * vsw ) * color.r +
					      ( 0.587f * v - 0.587f * vsu + 0.330f * vsw ) * color.g +
					      ( 0.114f * v - 0.114f * vsu - 0.497f * vsw ) * color.b;
	  tmpVec.y =	( 0.299f * v - 0.299f * vsu - 0.328f * vsw ) * color.r +
	      				( 0.587f * v + 0.413f * vsu + 0.035f * vsw ) * color.g +
	      				( 0.114f * v - 0.114f * vsu + 0.292f * vsw ) * color.b;
	  tmpVec.z =	( 0.299f * v - 0.300f * vsu + 1.250f * vsw ) * color.r +
	  						( 0.587f * v - 0.588f * vsu - 1.050f * vsw ) * color.g +
	  						( 0.114f * v + 0.886f * vsu - 0.203f * vsw ) * color.b;

	  color.r = tmpVec.x;
	  color.g = tmpVec.y;
	  color.b = tmpVec.z;
	}

	/**
	 * Rotates RGB color values using Hue-Saturation-Value space
	 * @param color
	 *  color to transform
	 * @param radians
	 *  amount to rotate hue
	 */
	public static void rotateHue( Color color, float radians ) {

	  float vsu = (float)Math.cos( radians );
	  float vsw = (float)Math.sin( radians );

	  tmpVec.x =	( 0.299f + 0.701f * vsu + 0.168f * vsw ) * color.r +
					      ( 0.587f - 0.587f * vsu + 0.330f * vsw ) * color.g +
					      ( 0.114f - 0.114f * vsu - 0.497f * vsw ) * color.b;
	  tmpVec.y =	( 0.299f - 0.299f * vsu - 0.328f * vsw ) * color.r +
	      				( 0.587f + 0.413f * vsu + 0.035f * vsw ) * color.g +
	      				( 0.114f - 0.114f * vsu + 0.292f * vsw ) * color.b;
	  tmpVec.z =	( 0.299f - 0.300f * vsu + 1.250f * vsw ) * color.r +
	  						( 0.587f - 0.588f * vsu - 1.050f * vsw ) * color.g +
	  						( 0.114f + 0.886f * vsu - 0.203f * vsw ) * color.b;

	  color.r = tmpVec.x;
	  color.g = tmpVec.y;
	  color.b = tmpVec.z;
	}

	/**
	 * Encodes the ABGR int color as a float.
	 * The high bits are masked to avoid using floats in the NaN range, which unfortunately means the full range of alpha cannot be used.
	 * See Float.intBitsToFloat(int) javadocs.
	 * @param value int color in ABGR format
	 * @return float representation of color
	 */
	public static float intToFloatColor(int value) {
		return Float.intBitsToFloat(value & 0xfeffffff);
	}
}
