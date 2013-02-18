package rajawali.animation;

import android.view.animation.Interpolator;
import rajawali.ATransformable3D;
import rajawali.BaseObject3D;
import rajawali.math.Number3D;

public class TranslateAnimation3D extends Animation3D {
	protected Number3D mToPosition;
	protected Number3D mFromPosition;
	protected Number3D mDiffPosition;
	protected Number3D mMultipliedPosition = new Number3D();
	protected Number3D mAddedPosition = new Number3D();
	protected boolean mOrientToPath = false;
	protected ISpline mSplinePath;
	private static Number3D tmpVec = new Number3D();

	public TranslateAnimation3D(Number3D toPosition) {
		super();
		mToPosition = toPosition;
	}

	public TranslateAnimation3D(Number3D fromPosition, Number3D toPosition) {
		super();
		mFromPosition = fromPosition;
		mToPosition = toPosition;
	}

	public TranslateAnimation3D(ISpline splinePath) {
		super();
		mSplinePath = splinePath;
	}

	public TranslateAnimation3D(BaseObject3D object, Number3D toPosition, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(toPosition);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}

	public TranslateAnimation3D(BaseObject3D object, Number3D fromPosition, Number3D toPosition, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(fromPosition, toPosition);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}

	public TranslateAnimation3D(BaseObject3D object, ISpline splinePath, long duration, long start, long length, int repeatCount, int repeatMode, Interpolator interpolator) {
		this(splinePath);

		setTransformable3D(object);
		setDuration(duration);
		setStart(start);
		setLength(length);
		setRepeatCount(repeatCount);
		setRepeatMode(repeatMode);
		setInterpolator(interpolator);
	}
	
	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mFromPosition == null)
			mFromPosition = new Number3D(transformable3D.getPosition());
	}

	@Override
	protected void applyTransformation(float interpolatedTime) {
		if (mSplinePath == null) {
			if (mDiffPosition == null) {
				mDiffPosition = new Number3D();
				Number3D.subtract(mToPosition, mFromPosition, mDiffPosition);
			}
			mMultipliedPosition.setAllFrom(mDiffPosition);
			mMultipliedPosition.multiply(interpolatedTime);
			mAddedPosition.setAllFrom(mFromPosition);
			mAddedPosition.add(mMultipliedPosition);
			mTransformable3D.getPosition().setAllFrom(mAddedPosition);
		} else {
			mSplinePath.calculatePoint(interpolatedTime, mTransformable3D.getPosition());

			if (mOrientToPath) {
				mSplinePath.getCurrentTangent(tmpVec);
				mTransformable3D.setLookAt(tmpVec);
			}
		}
	}

	public boolean getOrientToPath() {
		return mOrientToPath;
	}

	public void setOrientToPath(boolean orientToPath) {
		this.mOrientToPath = orientToPath;
		mSplinePath.setCalculateTangents(orientToPath);
	}
}
