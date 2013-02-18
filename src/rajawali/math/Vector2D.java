package rajawali.math;


public class Vector2D {
	public float x;
	public float y;
	
	public Vector2D() {
		
	}
	
	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D(String[] vals) {
		this.x = Float.parseFloat(vals[0]);
		this.y = Float.parseFloat(vals[1]);
	}

	public void setAll(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void setAllFrom(Vector2D v) {
		this.x = v.x;
		this.y = v.y;
	}

	public void add(Vector2D v) {
		this.x += v.x;
		this.y += v.y;
	}

	public void multiply(float u) {
		this.x *= u;
		this.y *= u;
	}

	public float length() {
		return (float)Math.sqrt(x*x + y+y);
	}

	public float lengthSquared() {
		return (x*x + y+y);
	}

	public void normalize() {
		float l = length();
		this.x /= l;
		this.y /= l;
	}

	public float distanceTo(Vector2D v) {
		final float xd = v.x - x;
		final float yd = v.y - y;
		return (float)Math.sqrt(xd * xd + yd * yd);
	}

	public float distanceTo(float x, float y) {
		final float xd = x - this.x;
		final float yd = y - this.y;
		return (float)Math.sqrt(xd * xd + yd * yd);
	}

	public float cross( Vector2D v ) {
		return this.x * v.y - this.y * v.x;
	}
}
