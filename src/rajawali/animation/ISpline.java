package rajawali.animation;

import rajawali.math.Number3D;

public interface ISpline {
	public void calculatePoint(float t, Number3D result);
	public void getCurrentTangent(Number3D result);
	public void setCalculateTangents(boolean calculateTangents);
}
