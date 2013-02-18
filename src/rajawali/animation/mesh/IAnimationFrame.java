package rajawali.animation.mesh;

import rajawali.Geometry3DSeparate;

public interface IAnimationFrame {
	public Geometry3DSeparate getGeometry();
	public void setGeometry(Geometry3DSeparate geometry);
	public String getName();
	public void setName(String name);
}
