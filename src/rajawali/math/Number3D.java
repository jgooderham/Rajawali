package rajawali.math;

import rajawali.util.RajLog;


/**
 * @author dennis.ippel
 *
 */
public class Number3D {

	private static final Number3D AXIS_X = new Number3D(1, 0, 0);
	private static final Number3D AXIS_Y = new Number3D(0, 1, 0);
	private static final Number3D AXIS_Z = new Number3D(0, 0, 1);

	public float x;
	public float y;
	public float z;
	
	public static final int M00 = 0;// 0;
	public static final int M01 = 4;// 1;
	public static final int M02 = 8;// 2;
	public static final int M03 = 12;// 3;
	public static final int M10 = 1;// 4;
	public static final int M11 = 5;// 5;
	public static final int M12 = 9;// 6;
	public static final int M13 = 13;// 7;
	public static final int M20 = 2;// 8;
	public static final int M21 = 6;// 9;
	public static final int M22 = 10;// 10;
	public static final int M23 = 14;// 11;
	public static final int M30 = 3;// 12;
	public static final int M31 = 7;// 13;
	public static final int M32 = 11;// 14;
	public static final int M33 = 15;// 15;

	private static Quaternion tmpQuat = new Quaternion();

	private static Number3D tmp = new Number3D();
	private static Number3D tmp2 = new Number3D();
	private static Number3D tmp3 = new Number3D();

	public enum Axis {
		X, Y, Z
	}

	public Number3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Number3D(Number3D from) {
		this.x = from.x;
		this.y = from.y;
		this.z = from.z;
	}
	
	public Number3D(String[] values) {
		if(values.length != 3) RajLog.e("Number3D should be initialized with 3 values");
		this.x = Float.parseFloat(values[0]);
		this.y = Float.parseFloat(values[1]);
		this.z = Float.parseFloat(values[2]);
	}

	public Number3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Number3D(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	public boolean equals(Number3D obj) {
		return obj.x == this.x && obj.y == this.y && obj.z == this.z;
	}

	public void setAll(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setAll(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}
	
	public void project(float[] mat){
			
          float l_w = x * mat[M30] + y * mat[M31] + z * mat[M32] + mat[M33];
          
          this.setAll(
        		  (x * mat[M00] + y * mat[M01] + z * mat[M02] + mat[M03]) / l_w, 
        		  (x * mat[M10] + y * mat[M11] + z * mat[M12] + mat[M13]) / l_w, 
        		  (x * mat[M20] + y * mat[M21] + z * mat[M22] + mat[M23]) / l_w);
          
	}

	public void setAllFrom(Number3D other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public float normalize() {
		float mod = (float)Math.sqrt(x * x + y * y + z * z);

		if (mod != 0 && mod != 1) {
			mod = 1 / mod;
			this.x *= mod;
			this.y *= mod;
			this.z *= mod;
		}
		
		return mod;
	}

	public Number3D inverse() {
		return new Number3D(-x, -y, -z);
	}
	
	public Number3D add(Number3D n) {
		this.x += n.x;
		this.y += n.y;
		this.z += n.z;
		return this;
	}

	public Number3D add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Number3D subtract(Number3D n) {
		this.x -= n.x;
		this.y -= n.y;
		this.z -= n.z;
		return this;
	}

	public Number3D multiply(float f) {
		this.x *= f;
		this.y *= f;
		this.z *= f;
		return this;
	}

	public void multiply(Number3D n) {
		this.x *= n.x;
		this.y *= n.y;
		this.z *= n.z;
	}

	public void multiply(final float[] matrix) {
		float vx = x, vy = y, vz = z;
		this.x = vx * matrix[0] + vy * matrix[4] + vz * matrix[8] + matrix[12];
		this.y = vx * matrix[1] + vy * matrix[5] + vz * matrix[9] + matrix[13];
		this.z = vx * matrix[2] + vy * matrix[6] + vz * matrix[10] + matrix[14];
	}

	public float distanceTo(Number3D other) {
		return (float)Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z));
	}

	public float distanceSquaredTo(Number3D other) {
		return (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z);
	}

	public float length() {
		return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public float lengthSquared() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	public Number3D clone() {
		return new Number3D(x, y, z);
	}

	public void rotateX(float angle) {
		float cosRY = (float)Math.cos(angle);
		float sinRY = (float)Math.sin(angle);

		tmp.setAll(this.x, this.y, this.z);

		this.y = (tmp.y * cosRY) - (tmp.z * sinRY);
		this.z = (tmp.y * sinRY) + (tmp.z * cosRY);
	}

	public void rotateY(float angle) {
		float cosRY = (float)Math.cos(angle);
		float sinRY = (float)Math.sin(angle);

		tmp.setAll(this.x, this.y, this.z);

		this.x = (tmp.x * cosRY) + (tmp.z * sinRY);
		this.z = (tmp.x * -sinRY) + (tmp.z * cosRY);
	}

	public void rotateZ(float angle) {
		float cosRY = (float)Math.cos(angle);
		float sinRY = (float)Math.sin(angle);

		tmp.setAll(this.x, this.y, this.z);

		this.x = (tmp.x * cosRY) - (tmp.y * sinRY);
		this.y = (tmp.x * sinRY) + (tmp.y * cosRY);
	}

	/** Rotates this vector by the given angle around the given axis.
	 * 
	 * @param axisX the x-component of the axis
	 * @param axisY the y-component of the axis
	 * @param axisZ the z-component of the axis
	 */
	public void rotate (float angle, float axisX, float axisY, float axisZ) {
		tmp.setAll(axisX, axisY, axisZ);
		rotate(tmp, angle);
	}
	
	/** Rotates this vector by the given angle around the given axis.
	 * 
	 * @param axis
	 * @param angle the angle
	 */
	public void rotate (Number3D axis, float angle) {
		tmpQuat.fromAngleAxis( angle, axis );
		tmp.setAllFrom( this );
		tmpQuat.multiply( tmp, this );
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(x);
		sb.append(", ");
		sb.append(y);
		sb.append(", ");
		sb.append(z);
		return sb.toString();
	}

	//

	public static void add(Number3D a, Number3D b, Number3D result) {
		result.setAll(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static void subtract(Number3D a, Number3D b, Number3D result) {
		result.setAll(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static void multiply(Number3D a, Number3D b, Number3D result) {
		result.setAll(a.x * b.x, a.y * b.y, a.z * b.z);
	}

	public static void multiply(Number3D a, float b, Number3D result) {
		result.setAll(a.x * b, a.y * b, a.z * b);
	}

	public static void cross(Number3D v, Number3D w, Number3D result) {
		result.setAll(w.y * v.z - w.z * v.y, w.z * v.x - w.x * v.z, w.x * v.y - w.y * v.x);
	}
	
	public Number3D cross(Number3D w) {
		tmp.setAllFrom(this);
		x = w.y * tmp.z - w.z * tmp.y;
		y = w.z * tmp.x - w.x * tmp.z;
		z = w.x * tmp.y - w.y * tmp.x;
		return this;
	}

	public static float dot(Number3D v, Number3D w) {
		return v.x * w.x + v.y * w.y + v.z * w.z;
	}
	
	public float dot(Number3D w) {
		return x * w.x + y * w.y + z * w.z;
	}

	public static Number3D getAxisVector(Axis axis) {
		switch (axis) {
		case X:
			return AXIS_X;
		case Y:
			return AXIS_Y;
		case Z:
			return AXIS_Z;
		}
		return AXIS_Z;
	}
	
	/**
	 * http://ogre.sourcearchive.com/documentation/1.4.5/classOgre_1_1Vector3_eeef4472ad0c4d5f34a038a9f2faa819.html#eeef4472ad0c4d5f34a038a9f2faa819
	 * 
	 * @param direction
	 * @return
	 */
	public void getRotationTo(Number3D direction, Quaternion q) {
		// Based on Stan Melax's article in Game Programming Gems
		// Copy, since cannot modify local
		tmp.setAllFrom(this);
		tmp2.setAllFrom(direction);
		tmp.normalize();
		tmp2.normalize();

		float d = dot(tmp, tmp2);
		// If dot == 1, vectors are the same
		if (d >= 1.0f) {
			q.setIdentity();
		}
		if (d < 0.000001f - 1.0f) {
			// Generate an axis
			tmp.setAllFrom(getAxisVector(Axis.X));
			cross(tmp, this, tmp2);
			if (tmp2.length() == 0) { // pick another if colinear
				tmp.setAllFrom(getAxisVector(Axis.Y));
				cross(tmp, this, tmp2);
			}
			tmp2.normalize();
			q.fromAngleAxis(MathUtil.radiansToDegrees(MathUtil.PI), tmp2);
		} else {
			float s = (float)Math.sqrt((1 + d) * 2);
			float invs = 1f / s;

			cross(tmp, tmp2, tmp3);

			q.x = tmp3.x * invs;
			q.y = tmp3.y * invs;
			q.z = tmp3.z * invs;
			q.w = s * 0.5f;
			q.normalize();
		}
	}
	
	public static Number3D getUpVector() {
		return new Number3D(0, 1, 0);
	}
	
	public static void lerp(Number3D from, Number3D to, float amount, Number3D result)
	{
		result.setAll(	from.x + (to.x - from.x) * amount,
						from.y + (to.y - from.y) * amount,
						from.z + (to.z - from.z) * amount);
	}
	
	/**
	 * Performs a linear interpolation between from and to by the specified amount.
	 * The result will be stored in the current object which means that the current
	 * x, y, z values will be overridden.
	 * 
	 * @param from
	 * @param to
	 * @param amount
	 */
	public void lerpSelf(Number3D from, Number3D to, float amount)
	{
	  this.x = from.x + (to.x - from.x) * amount;
	  this.y = from.y + (to.y - from.y) * amount;
	  this.z = from.z + (to.z - from.z) * amount;
	}

	/**
	 * Access to the static Number3D instance used internally for calculations. Can be useful for quick calculations where instantiation would be unnecessarily costly.
	 * Care must be taken not to keep a reference to this value and to use it only for temporary scratch!
	 * @return
	 */
	public static Number3D tmp() {
		return tmp;
	}

	/**
	 * Access to the static Number3D instance used internally for calculations. Can be useful for quick calculations where instantiation would be unnecessarily costly.
	 * Care must be taken not to keep a reference to this value and to use it only for temporary scratch!
	 * @return
	 */
	public static Number3D tmp2() {
		return tmp2;
	}

	/**
	 * Access to the static Number3D instance used internally for calculations. Can be useful for quick calculations where instantiation would be unnecessarily costly.
	 * Care must be taken not to keep a reference to this value and to use it only for temporary scratch!
	 * @return
	 */
	public static Number3D tmp3() {
		return tmp3;
	}
}
