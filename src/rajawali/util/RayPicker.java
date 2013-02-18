package rajawali.util;

import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import rajawali.visitors.RayPickingVisitor;

public class RayPicker implements IObjectPicker {
	private RajawaliRenderer mRenderer;
	private OnObjectPickedListener mObjectPickedListener;

	private static Number3D pointNear = new Number3D();
	private static Number3D pointFar = new Number3D();

	public RayPicker(RajawaliRenderer renderer) {
		mRenderer = renderer;
	}
	
	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
	}
	
	public void getObjectAt(float x, float y) {
		pointNear.setAll(x, y, 0);
		pointFar.setAll(x, y, 1);
		mRenderer.unproject(pointNear);
		mRenderer.unproject(pointFar);
		
		RayPickingVisitor visitor = new RayPickingVisitor(pointNear, pointFar);
		mRenderer.accept(visitor);
		
		// TODO: ray-triangle intersection test
		
		mObjectPickedListener.onObjectPicked(visitor.getPickedObject());
	}
}
