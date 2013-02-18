/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package rajawali.math;

import java.io.Serializable;


/** Encapsulates a ray having a starting position and a unit length direction.
 * 
 * @author badlogicgames@gmail.com */
public class Ray implements Serializable {

	private static final long serialVersionUID = -2097595067517169890L;
	public final Number3D origin = new Number3D();
	public final Number3D direction = new Number3D();

	static Number3D tmp = new Number3D();

	/** Constructor, sets the starting position of the ray and the direction.
	 * 
	 * @param origin The starting position
	 * @param direction The direction */
	public Ray(Number3D origin, Number3D direction) {
		this.origin.setAllFrom(origin);
		this.direction.setAllFrom(direction);
		this.direction.normalize();
	}

	/** @return a copy of this ray. */
	public Ray clone() {
		return new Ray(this.origin, this.direction);
	}

	/** Returns and endpoint given the distance. This is calculated as startpoint + distance * direction.
	 * 
	 * @param distance The distance from the end point to the start point.
	 * @return The end point */
	public Number3D getEndPoint(float distance) {
		tmp.setAllFrom(direction);
		tmp.multiply(distance);
		return new Number3D(origin).add(tmp);
	}

	/** Multiplies the ray by the given matrix. Use this to transform a ray into another coordinate system.
	 * 
	 * @param matrix The matrix
	 * @return This ray for chaining. */
	public Ray multiply(Matrix4 matrix) {
		tmp.setAllFrom(origin);
		tmp.add(direction);
		tmp.multiply(matrix.val());
		origin.multiply(matrix.val());
		tmp.subtract(origin);
		direction.setAllFrom(tmp);
		return this;
	}

	/** {@inheritDoc} */
	public String toString() {
		return "ray [" + origin + ":" + direction + "]";
	}

	/** Sets the starting position and the direction of this ray.
	 * 
	 * @param origin The starting position
	 * @param direction The direction
	 * @return this ray for chaining */
	public Ray set (Number3D origin, Number3D direction) {
		this.origin.setAllFrom(origin);
		this.direction.setAllFrom(direction);
		return this;
	}

	/** Sets this ray from the given starting position and direction.
	 * 
	 * @param x The x-component of the starting position
	 * @param y The y-component of the starting position
	 * @param z The z-component of the starting position
	 * @param dx The x-component of the direction
	 * @param dy The y-component of the direction
	 * @param dz The z-component of the direction
	 * @return this ray for chaining */
	public Ray set (float x, float y, float z, float dx, float dy, float dz) {
		this.origin.setAll(x, y, z);
		this.direction.setAll(dx, dy, dz);
		return this;
	}

	/** Sets the starting position and direction from the given ray
	 * 
	 * @param ray The ray
	 * @return This ray for chaining */
	public Ray set (Ray ray) {

		this.origin.setAllFrom(ray.origin);
		this.direction.setAllFrom(ray.direction);
		return this;
	}
}
