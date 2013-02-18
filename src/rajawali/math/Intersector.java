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

import java.util.Arrays;
import java.util.List;

import rajawali.bounds.BoundingBox;
import rajawali.math.Plane.PlaneSide;

/** Class offering various static methods for intersection testing between different geometric objects.
 * 
 * @author badlogicgames@gmail.com
 * @author jan.stria */
public final class Intersector {
	/** Returns the lowest positive root of the quadric equation given by a* x * x + b * x + c = 0. If no solution is given
	 * Float.Nan is returned.
	 * 
	 * @param a the first coefficient of the quadric equation
	 * @param b the second coefficient of the quadric equation
	 * @param c the third coefficient of the quadric equation
	 * @return the lowest positive root or Float.Nan */
	public static float getLowestPositiveRoot (float a, float b, float c) {
		float det = b * b - 4 * a * c;
		if (det < 0) return Float.NaN;

		float sqrtD = (float)Math.sqrt(det);
		float invA = 1 / (2 * a);
		float r1 = (-b - sqrtD) * invA;
		float r2 = (-b + sqrtD) * invA;

		if (r1 > r2) {
			float tmp = r2;
			r2 = r1;
			r1 = tmp;
		}

		if (r1 > 0) return r1;

		if (r2 > 0) return r2;

		return Float.NaN;
	}

	private final static Number3D v0 = new Number3D();
	private final static Number3D v1 = new Number3D();
	private final static Number3D v2 = new Number3D();

	/** Returns whether the given point is inside the triangle. This assumes that the point is on the plane of the triangle. No
	 * check is performed that this is the case.
	 * 
	 * @param point the point
	 * @param t1 the first vertex of the triangle
	 * @param t2 the second vertex of the triangle
	 * @param t3 the third vertex of the triangle
	 * @return whether the point is in the triangle */
	public static boolean isPointInTriangle (Number3D point, Number3D t1, Number3D t2, Number3D t3) {
		v0.setAll(t1.x - point.x, t1.y - point.y, t1.z - point.z);
		v1.setAll(t2.x - point.x, t2.y - point.y, t2.z - point.z);
		v2.setAll(t3.x - point.x, t3.y - point.y, t3.z - point.z);

		float ab = v0.dot(v1);
		float ac = v0.dot(v2);
		float bc = v1.dot(v2);
		float cc = v2.dot(v2);

		if (bc * ac - cc * ab < 0) return false;
		float bb = v1.dot(v1);
		if (ab * bc - ac * bb < 0) return false;
		return true;
	}

	public static boolean intersectSegmentPlane (Number3D start, Number3D end, Plane plane, Number3D intersection) {
		Number3D dir = tmp;
		dir.setAll(end.x - start.x, end.y - start.y, end.z - start.z);
		float denom = dir.dot(plane.getNormal());
		float t = -(start.dot(plane.getNormal()) + plane.getD()) / denom;
		if (t < 0 || t > 1) return false;

		intersection.setAllFrom(start);
		dir.multiply(t);
		intersection.add(dir);
		return true;
	}

	/** Determines on which side of the given line the point is. Returns -1 if the point is on the left side of the line, 0 if the
	 * point is on the line and 1 if the point is on the right side of the line. Left and right are relative to the lines direction
	 * which is linePoint1 to linePoint2. */
	public static int pointLineSide (Vector2D linePoint1, Vector2D linePoint2, Vector2D point) {
		return (int)Math.signum((linePoint2.x - linePoint1.x) * (point.y - linePoint1.y) - (linePoint2.y - linePoint1.y)
			* (point.x - linePoint1.x));
	}

	public static int pointLineSide (float linePoint1X, float linePoint1Y, float linePoint2X, float linePoint2Y, float pointX,
		float pointY) {
		return (int)Math.signum((linePoint2X - linePoint1X) * (pointY - linePoint1Y) - (linePoint2Y - linePoint1Y)
			* (pointX - linePoint1X));
	}

	/** Checks wheter the given point is in the polygon.
	 * @param polygon The polygon vertices
	 * @param point The point
	 * @return true if the point is in the polygon */
	public static boolean isPointInPolygon (List<Vector2D> polygon, Vector2D point) {
		int j = polygon.size() - 1;
		boolean oddNodes = false;
		for (int i = 0; i < polygon.size(); i++) {
			if ((polygon.get(i).y < point.y && polygon.get(j).y >= point.y)
				|| (polygon.get(j).y < point.y && polygon.get(i).y >= point.y)) {
				if (polygon.get(i).x + (point.y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(i).y)
					* (polygon.get(j).x - polygon.get(i).x) < point.x) {
					oddNodes = !oddNodes;
				}
			}
			j = i;
		}
		return oddNodes;
	}

	/** Returns the distance between the given line segment and point.
	 * 
	 * @param start The line start point
	 * @param end The line end point
	 * @param point The point
	 * 
	 * @return The distance between the line segment and the point. */
	public static float distanceLinePoint (Vector2D start, Vector2D end, Vector2D point) {
		tmp.setAll(end.x - start.x, end.y - start.y, 0);
		float l2 = tmp.lengthSquared();
		if (l2 == 0.0f) // start == end
			return point.distanceTo(start);

		tmp.setAll(point.x - start.x, point.y - start.y, 0);
		tmp2.setAll(end.x - start.x, end.y - start.y, 0);

		float t = tmp.dot(tmp2) / l2;
		if (t < 0.0f)
			return point.distanceTo(start); // Beyond 'start'-end of the segment
		else if (t > 1.0f) return point.distanceTo(end); // Beyond 'end'-end of the segment

		tmp.setAll(end.x - start.x, end.y - start.y, 0); // Projection falls on the segment
		tmp.multiply(t);
		tmp.add(start.x, start.y, 0);
		tmp2.setAll(point.x, point.y, 0);
		return tmp2.distanceTo(tmp);
	}

	/** Returns the distance between the given line and point. Note the specified line is not a line segment. */
	public static float distanceLinePoint (float startX, float startY, float endX, float endY, float pointX, float pointY) {
		float normalLength = (float)Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY));
		return Math.abs((pointX - startX) * (endY - startY) - (pointY - startY) * (endX - startX)) / normalLength;
	}

	/** Returns wheter the given line segment intersects the given circle.
	 * 
	 * @param start The start point of the line segment
	 * @param end The end point of the line segment
	 * @param center The center of the circle
	 * @param squareRadius The squared radius of the circle
	 * @return Wheter the line segment and the circle intersect */
	public static boolean intersectSegmentCircle (Vector2D start, Vector2D end, Vector2D center, float squareRadius) {
		tmp.setAll(end.x - start.x, end.y - start.y, 0);
		tmp1.setAll(center.x - start.x, center.y - start.y, 0);
		float l = tmp.normalize();
		float u = tmp1.dot(tmp);
		if (u <= 0) {
			tmp2.setAll(start.x, start.y, 0);
		} else if (u >= l) {
			tmp2.setAll(end.x, end.y, 0);
		} else {
			tmp.multiply(u);
			tmp3.setAllFrom(tmp); // remember tmp is already normalized
			tmp2.setAll(tmp3.x + start.x, tmp3.y + start.y, 0);
		}

		float x = center.x - tmp2.x;
		float y = center.y - tmp2.y;

		return x * x + y * y <= squareRadius;
	}

	private static Vector2D tmp2D1 = new Vector2D();
	private static Vector2D tmp2D2 = new Vector2D();

	/** Checks wheter the line segment and the circle intersect and returns by how much and in what direction the line has to move
	 * away from the circle to not intersect.
	 * 
	 * @param start The line segment starting point
	 * @param end The line segment end point
	 * @param point The center of the circle
	 * @param radius The radius of the circle
	 * @param displacement The displacement vector set by the method having unit length
	 * @return The displacement or Float.POSITIVE_INFINITY if no intersection is present */
	public static float intersectSegmentCircleDisplace (Vector2D start, Vector2D end, Vector2D point, float radius,
		Vector2D displacement) {
		float u = (point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y);
		float d = start.distanceTo(end);
		u /= (d * d);
		if (u < 0 || u > 1) return Float.POSITIVE_INFINITY;
		tmp2D1.setAll(end.x - start.x, end.y - start.y);
		tmp2D2.setAll(start.x, start.y);
		tmp2D1.multiply(u);
		tmp2D2.add(tmp2D1);
		d = tmp2D2.distanceTo(point.x, point.y);
		if (d < radius) {
			displacement.setAll(point.x - tmp2D2.x, point.y - tmp2D2.y);
			displacement.normalize();
			return d;
		} else
			return Float.POSITIVE_INFINITY;
	}

	/** Intersects a {@link Ray} and a {@link Plane}. The intersection point is stored in intersection in case an intersection is
	 * present.
	 * 
	 * @param ray The ray
	 * @param plane The plane
	 * @param intersection The vector the intersection point is written to (optional)
	 * @return Whether an intersection is present. */
	public static boolean intersectRayPlane (Ray ray, Plane plane, Number3D intersection) {
		float denom = ray.direction.dot(plane.getNormal());
		if (denom != 0) {
			float t = -(ray.origin.dot(plane.getNormal()) + plane.getD()) / denom;
			if (t < 0) return false;

			if (intersection != null) {
				intersection.setAllFrom(ray.origin);
				Number3D dir = Number3D.tmp();
				dir.setAllFrom(ray.direction);
				dir.multiply(t);
				intersection.add(dir);
			}
			return true;
		} else if (plane.getPointSide(ray.origin) == Plane.PlaneSide.OnPlane) {
			if (intersection != null) intersection.setAllFrom(ray.origin);
			return true;
		} else
			return false;
	}

	/** Intersects a line and a plane. The intersection is returned as the distance from the first point to the plane. In case an
	 * intersection happened, the return value is in the range [0,1]. The intersection point can be recovered by point1 + t *
	 * (point2 - point1) where t is the return value of this method.
	 * @param x
	 * @param y
	 * @param z
	 * @param x2
	 * @param y2
	 * @param z2
	 * @param plane */
	public static float intersectLinePlane (float x, float y, float z, float x2, float y2, float z2, Plane plane,
		Number3D intersection) {
		Number3D direction = tmp;
		Number3D origin = tmp2;
		direction.setAll(x2 - x, y2 - y, z2 - z);
		origin.setAll(x, y, z);
		float denom = direction.dot(plane.getNormal());
		if (denom != 0) {
			float t = -(origin.dot(plane.getNormal()) + plane.getD()) / denom;
			if ((t >= 0 && t <= 1) && intersection != null) {
				intersection.setAllFrom(origin);
				direction.multiply(t);
				intersection.add(direction);
			}
			return t;
		} else if (plane.getPointSide(origin) == Plane.PlaneSide.OnPlane) {
			if (intersection != null) intersection.setAllFrom(origin);
			return 0;
		}

		return -1;
	}

	private static final Plane p = new Plane(new Number3D(), 0);
	private static final Number3D i = new Number3D();

	/** Intersect a {@link Ray} and a triangle, returning the intersection point in intersection.
	 * 
	 * @param ray The ray
	 * @param t1 The first vertex of the triangle
	 * @param t2 The second vertex of the triangle
	 * @param t3 The third vertex of the triangle
	 * @param intersection The intersection point (optional)
	 * @return True in case an intersection is present. */
	public static boolean intersectRayTriangle (Ray ray, Number3D t1, Number3D t2, Number3D t3, Number3D intersection) {
		p.set(t1, t2, t3);
		if (!intersectRayPlane(ray, p, i)) return false;

		v0.setAll(t3.x - t1.x, t3.y - t1.y, t3.z - t1.z);
		v1.setAll(t2.x - t1.x, t2.y - t1.y, t2.z - t1.z);
		v2.setAll(i.x - t1.x, i.y - t1.y, i.z - t1.z);

		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);

		float denom = dot00 * dot11 - dot01 * dot01;
		if (denom == 0) return false;

		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;

		if (u >= 0 && v >= 0 && u + v <= 1) {
			if (intersection != null) intersection.setAllFrom(i);
			return true;
		} else {
			return false;
		}

	}

	private static final Number3D dir = new Number3D();
	private static final Number3D start = new Number3D();

	/** Intersects a {@link Ray} and a sphere, returning the intersection point in intersection.
	 * 
	 * @param ray The ray
	 * @param center The center of the sphere
	 * @param radius The radius of the sphere
	 * @param intersection The intersection point (optional)
	 * @return Whether an intersection is present. */
	public static boolean intersectRaySphere (Ray ray, Number3D center, float radius, Number3D intersection) {
		dir.setAllFrom(ray.direction);
		dir.normalize();
		start.setAllFrom(ray.origin);
		tmp.setAll(start.x - center.x, start.y - center.y, start.z - center.z);
		float b = 2 * (dir.dot(tmp));
		float c = start.distanceSquaredTo(center) - radius * radius;
		float disc = b * b - 4 * c;
		if (disc < 0) return false;

		// compute q as described above
		float distSqrt = (float)Math.sqrt(disc);
		float q;
		if (b < 0)
			q = (-b - distSqrt) / 2.0f;
		else
			q = (-b + distSqrt) / 2.0f;

		// compute t0 and t1
		float t0 = q / 1;
		float t1 = c / q;

		// make sure t0 is smaller than t1
		if (t0 > t1) {
			// if t0 is bigger than t1 swap them around
			float temp = t0;
			t0 = t1;
			t1 = temp;
		}

		// if t1 is less than zero, the object is in the ray's negative
		// direction
		// and consequently the ray misses the sphere
		if (t1 < 0) return false;

		// if t0 is less than zero, the intersection point is at t1
		if (t0 < 0) {
			if (intersection != null) {
				intersection.setAllFrom(start);
				intersection.add(dir.x * t1, dir.y * t1, dir.z * t1);
			}
			return true;
		}
		// else the intersection point is at t0
		else {
			if (intersection != null) {
				intersection.setAllFrom(start);
				intersection.add(dir.x * t0, dir.y * t0, dir.z * t0);
			}
			return true;
		}
	}

	/** Quick check whether the given {@link Ray} and {@link BoundingBox} intersect.
	 * 
	 * @param ray The ray
	 * @param box The bounding box
	 * @return Whether the ray and the bounding box intersect. */
	static public boolean intersectRayBoundsFast (Ray ray, BoundingBox box) {
		float a, b;
		float min, max;
		float divX = 1 / ray.direction.x;
		float divY = 1 / ray.direction.y;
		float divZ = 1 / ray.direction.z;

		a = (box.getTransformedMin().x - ray.origin.x) * divX;
		b = (box.getTransformedMax().x - ray.origin.x) * divX;
		if (a < b) {
			min = a;
			max = b;
		} else {
			min = b;
			max = a;
		}

		a = (box.getTransformedMin().y - ray.origin.y) * divY;
		b = (box.getTransformedMax().y - ray.origin.y) * divY;
		if (a > b) {
			float t = a;
			a = b;
			b = t;
		}

		if (a > min) min = a;
		if (b < max) max = b;

		a = (box.getTransformedMin().z - ray.origin.z) * divZ;
		b = (box.getTransformedMax().z - ray.origin.z) * divZ;
		if (a > b) {
			float t = a;
			a = b;
			b = t;
		}

		if (a > min) min = a;
		if (b < max) max = b;

		return (max >= 0) && (max >= min);
	}

	static Number3D tmp = new Number3D();
	static Number3D best = new Number3D();
	static Number3D tmp1 = new Number3D();
	static Number3D tmp2 = new Number3D();
	static Number3D tmp3 = new Number3D();

	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param triangles The triangles, each successive 3 elements from a vertex
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles (Ray ray, float[] triangles, Number3D intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if ((triangles.length / 3) % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.length - 6; i += 9) {
			tmp1.setAll(triangles[i], triangles[i + 1], triangles[i + 2]);
			tmp2.setAll(triangles[i + 3], triangles[i + 4], triangles[i + 5]);
			tmp3.setAll(triangles[i + 6], triangles[i + 7], triangles[i + 8]);
			boolean result = intersectRayTriangle(ray, tmp1, tmp2, tmp3, tmp);

			if (result == true) {
				best.setAllFrom(ray.origin);
				best.subtract(tmp);
				float dist = best.lengthSquared();
				if (dist < min_dist) {
					min_dist = dist;
					best.setAllFrom(tmp);
					hit = true;
				}
			}
		}

		if (hit == false)
			return false;
		else {
			if (intersection != null) intersection.setAllFrom(best);
			return true;
		}
	}

	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param vertices the vertices
	 * @param indices the indices, each successive 3 shorts index the 3 vertices of a triangle
	 * @param vertexSize the size of a vertex in floats
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles (Ray ray, float[] vertices, short[] indices, int vertexSize, Number3D intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if ((indices.length % 3) != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < indices.length; i += 3) {
			int i1 = indices[i] * vertexSize;
			int i2 = indices[i + 1] * vertexSize;
			int i3 = indices[i + 2] * vertexSize;

			tmp1.setAll(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]);
			tmp2.setAll(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]);
			tmp3.setAll(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]);
			boolean result = intersectRayTriangle(ray, tmp1, tmp2, tmp3, tmp);

			if (result == true) {
				best.setAllFrom(ray.origin);
				best.subtract(tmp);
				float dist = best.lengthSquared();
				if (dist < min_dist) {
					min_dist = dist;
					best.setAllFrom(tmp);
					hit = true;
				}
			}
		}

		if (hit == false)
			return false;
		else {
			if (intersection != null) intersection.setAllFrom(best);
			return true;
		}
	}

	/** Intersects the given ray with list of triangles. Returns the nearest intersection point in intersection
	 * 
	 * @param ray The ray
	 * @param triangles The triangles
	 * @param intersection The nearest intersection point (optional)
	 * @return Whether the ray and the triangles intersect. */
	public static boolean intersectRayTriangles (Ray ray, List<Number3D> triangles, Number3D intersection) {
		float min_dist = Float.MAX_VALUE;
		boolean hit = false;

		if (triangles.size() % 3 != 0) throw new RuntimeException("triangle list size is not a multiple of 3");

		for (int i = 0; i < triangles.size() - 2; i += 3) {
			boolean result = intersectRayTriangle(ray, triangles.get(i), triangles.get(i + 1), triangles.get(i + 2), tmp);

			if (result == true) {
				best.setAllFrom(ray.origin);
				best.subtract(tmp);
				float dist = best.lengthSquared();
				if (dist < min_dist) {
					min_dist = dist;
					best.setAllFrom(tmp);
					hit = true;
				}
			}
		}

		if (!hit)
			return false;
		else {
			if (intersection != null) intersection.setAllFrom(best);
			return true;
		}
	}

	/** Returns wheter the two rectangles intersect
	 * 
	 * @param a The first rectangle
	 * @param b The second rectangle
	 * @return Wheter the two rectangles intersect */
	public static boolean intersectRectangles (Rectangle a, Rectangle b) {
		return !(a.getX() > b.getX() + b.getWidth() || a.getX() + a.getWidth() < b.getX() || a.getY() > b.getY() + b.getHeight() || a
			.getY() + a.getHeight() < b.getY());
	}

	/** Intersects the two lines and returns the intersection point in intersection.
	 * 
	 * @param p1 The first point of the first line
	 * @param p2 The second point of the first line
	 * @param p3 The first point of the second line
	 * @param p4 The second point of the second line
	 * @param intersection The intersection point
	 * @return Whether the two lines intersect */
	public static boolean intersectLines (Vector2D p1, Vector2D p2, Vector2D p3, Vector2D p4, Vector2D intersection) {
		float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

		float det1 = det(x1, y1, x2, y2);
		float det2 = det(x3, y3, x4, y4);
		float det3 = det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);

		float x = det(det1, x1 - x2, det2, x3 - x4) / det3;
		float y = det(det1, y1 - y2, det2, y3 - y4) / det3;

		intersection.x = x;
		intersection.y = y;

		return true;
	}

	/** Intersects the two line segments and returns the intersection point in intersection.
	 * 
	 * @param p1 The first point of the first line segment
	 * @param p2 The second point of the first line segment
	 * @param p3 The first point of the second line segment
	 * @param p4 The second point of the second line segment
	 * @param intersection The intersection point (optional)
	 * @return Whether the two line segments intersect */
	public static boolean intersectSegments (Vector2D p1, Vector2D p2, Vector2D p3, Vector2D p4, Vector2D intersection) {
		float x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y, x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;

		float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d;
		float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / d;

		if (ua < 0 || ua > 1) return false;
		if (ub < 0 || ub > 1) return false;

		if (intersection != null) intersection.setAll(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
		return true;
	}

	static float det (float a, float b, float c, float d) {
		return a * d - b * c;
	}

	static double detd (double a, double b, double c, double d) {
		return a * d - b * c;
	}

	public static boolean overlapCircles (Circle c1, Circle c2) {
		float x = c1.x - c2.x;
		float y = c1.y - c2.y;
		float distance = x * x + y * y;
		float radiusSum = c1.radius + c2.radius;
		return distance <= radiusSum * radiusSum;
	}

	public static boolean overlapRectangles (Rectangle r1, Rectangle r2) {
		if (r1.x < r2.x + r2.width && r1.x + r1.width > r2.x && r1.y < r2.y + r2.height && r1.y + r1.height > r2.y)
			return true;
		else
			return false;
	}

	public static boolean overlapCircleRectangle (Circle c, Rectangle r) {
		float closestX = c.x;
		float closestY = c.y;

		if (c.x < r.x) {
			closestX = r.x;
		} else if (c.x > r.x + r.width) {
			closestX = r.x + r.width;
		}

		if (c.y < r.y) {
			closestY = r.y;
		} else if (c.y > r.y + r.height) {
			closestY = r.y + r.height;
		}

		closestX = closestX - c.x;
		closestX *= closestX;
		closestY = closestY - c.y;
		closestY *= closestY;

		return closestX + closestY < c.radius * c.radius;
	}

	/** Check whether specified convex polygons overlap.
	 * 
	 * @param p1 The first polygon.
	 * @param p2 The second polygon.
	 * @return Whether polygons overlap. */
	public static boolean overlapConvexPolygons (Polygon p1, Polygon p2) {
		return overlapConvexPolygons(p1, p2, null);
	}

	/** Check whether specified convex polygons overlap. If they do, optionally obtain a Minimum Translation Vector indicating the
	 * minimum magnitude vector required to push the polygons out of the collision.
	 * 
	 * @param p1 The first polygon.
	 * @param p2 The second polygon.
	 * @param mtv A Minimum Translation Vector to fill in the case of a collision (optional).
	 * @return Whether polygons overlap. */
	public static boolean overlapConvexPolygons (Polygon p1, Polygon p2, MinimumTranslationVector mtv) {
		return overlapConvexPolygons(p1.getTransformedVertices(), p2.getTransformedVertices(), mtv);
	}

	/** Check whether polygons defined by the given vertex arrays overlap. If they do, optionally obtain a Minimum Translation
	 * Vector indicating the minimum magnitude vector required to push the polygons out of the collision.
	 * 
	 * @param verts1 Vertices of the first polygon.
	 * @param verts2 Vertices of the second polygon.
	 * @param mtv A Minimum Translation Vector to fill in the case of a collision (optional).
	 * @return Whether polygons overlap. */
	public static boolean overlapConvexPolygons (float[] verts1, float[] verts2, MinimumTranslationVector mtv) {
		float overlap = Float.MAX_VALUE;
		float smallestAxisX = 0;
		float smallestAxisY = 0;

		// Get polygon1 axes
		final int numAxes1 = verts1.length;
		for (int i = 0; i < numAxes1; i += 2) {
			float x1 = verts1[i];
			float y1 = verts1[i + 1];
			float x2 = verts1[(i + 2) % numAxes1];
			float y2 = verts1[(i + 3) % numAxes1];

			float axisX = y1 - y2;
			float axisY = -(x1 - x2);

			final float length = (float)Math.sqrt(axisX * axisX + axisY * axisY);
			axisX /= length;
			axisY /= length;

			// -- Begin check for separation on this axis --//

			// Project polygon1 onto this axis
			float min1 = (axisX * verts1[0]) + (axisY * verts1[1]);
			float max1 = min1;
			for (int j = 2; j < verts1.length; j += 2) {
				float p = (axisX * verts1[j]) + (axisY * verts1[j + 1]);
				if (p < min1) {
					min1 = p;
				} else if (p > max1) {
					max1 = p;
				}
			}

			// Project polygon2 onto this axis
			float min2 = (axisX * verts2[0]) + (axisY * verts2[1]);
			float max2 = min2;
			for (int j = 2; j < verts2.length; j += 2) {
				float p = (axisX * verts2[j]) + (axisY * verts2[j + 1]);
				if (p < min2) {
					min2 = p;
				} else if (p > max2) {
					max2 = p;
				}
			}

			if (!((min1 <= min2 && max1 >= min2) || (min2 <= min1 && max2 >= min1))) {
				return false;
			} else {
				float o = Math.min(max1, max2) - Math.max(min1, min2);
				if ((min1 < min2 && max1 > max2) || (min2 < min1 && max2 > max1)) {
					float mins = Math.abs(min1 - min2);
					float maxs = Math.abs(max1 - max2);
					if (mins < maxs) {
						axisX = -axisX;
						axisY = -axisY;
						o += mins;
					} else {
						o += maxs;
					}
				}
				if (o < overlap) {
					overlap = o;
					smallestAxisX = axisX;
					smallestAxisY = axisY;
				}
			}
			// -- End check for separation on this axis --//
		}

		// Get polygon2 axes
		final int numAxes2 = verts2.length;
		for (int i = 0; i < numAxes2; i += 2) {
			float x1 = verts2[i];
			float y1 = verts2[i + 1];
			float x2 = verts2[(i + 2) % numAxes2];
			float y2 = verts2[(i + 3) % numAxes2];

			float axisX = y1 - y2;
			float axisY = -(x1 - x2);

			final float length = (float)Math.sqrt(axisX * axisX + axisY * axisY);
			axisX /= length;
			axisY /= length;

			// -- Begin check for separation on this axis --//

			// Project polygon1 onto this axis
			float min1 = (axisX * verts1[0]) + (axisY * verts1[1]);
			float max1 = min1;
			for (int j = 2; j < verts1.length; j += 2) {
				float p = (axisX * verts1[j]) + (axisY * verts1[j + 1]);
				if (p < min1) {
					min1 = p;
				} else if (p > max1) {
					max1 = p;
				}
			}

			// Project polygon2 onto this axis
			float min2 = (axisX * verts2[0]) + (axisY * verts2[1]);
			float max2 = min2;
			for (int j = 2; j < verts2.length; j += 2) {
				float p = (axisX * verts2[j]) + (axisY * verts2[j + 1]);
				if (p < min2) {
					min2 = p;
				} else if (p > max2) {
					max2 = p;
				}
			}

			if (!((min1 <= min2 && max1 >= min2) || (min2 <= min1 && max2 >= min1))) {
				return false;
			} else {
				float o = Math.min(max1, max2) - Math.max(min1, min2);

				if ((min1 < min2 && max1 > max2) || (min2 < min1 && max2 > max1)) {
					float mins = Math.abs(min1 - min2);
					float maxs = Math.abs(max1 - max2);
					if (mins < maxs) {
						axisX = -axisX;
						axisY = -axisY;
						o += mins;
					} else {
						o += maxs;
					}
				}

				if (o < overlap) {
					overlap = o;
					smallestAxisX = axisX;
					smallestAxisY = axisY;
				}
			}
			// -- End check for separation on this axis --//
		}
		if (mtv != null) {
			mtv.normal.setAll(smallestAxisX, smallestAxisY);
			mtv.depth = overlap;
		}
		return true;
	}

	/** Splits the triangle by the plane. The result is stored in the SplitTriangle instance. Depending on where the triangle is
	 * relative to the plane, the result can be:
	 * 
	 * <ul>
	 * <li>Triangle is fully in front/behind: {@link SplitTriangle#front} or {@link SplitTriangle#back} will contain the original
	 * triangle, {@link SplitTriangle#total} will be one.</li>
	 * <li>Triangle has two vertices in front, one behind: {@link SplitTriangle#front} contains 2 triangles,
	 * {@link SplitTriangle#back} contains 1 triangles, {@link SplitTriangle#total} will be 3.</li>
	 * <li>Triangle has one vertex in front, two behind: {@link SplitTriangle#front} contains 1 triangle,
	 * {@link SplitTriangle#back} contains 2 triangles, {@link SplitTriangle#total} will be 3.</li>
	 * </ul>
	 * 
	 * The input triangle should have the form: x, y, z, x2, y2, z2, x3, y3, y3. One can add additional attributes per vertex which
	 * will be interpolated if split, such as texture coordinates or normals. Note that these additional attributes won't be
	 * normalized, as might be necessary in case of normals.
	 * 
	 * @param triangle
	 * @param plane
	 * @param split output SplitTriangle */
	public static void splitTriangle (float[] triangle, Plane plane, SplitTriangle split) {
		int stride = triangle.length / 3;
		boolean r1 = plane.getPointSide(triangle[0], triangle[1], triangle[2]) == PlaneSide.Back;
		boolean r2 = plane.getPointSide(triangle[0 + stride], triangle[1 + stride], triangle[2 + stride]) == PlaneSide.Back;
		boolean r3 = plane.getPointSide(triangle[0 + stride * 2], triangle[1 + stride * 2], triangle[2 + stride * 2]) == PlaneSide.Back;

		split.reset();

		// easy case, triangle is on one side (point on plane means front).
		if ((r1 == r2) && (r2 == r3)) {
			split.total = 1;
			if (r1) {
				split.numBack = 1;
				System.arraycopy(triangle, 0, split.back, 0, triangle.length);
			} else {
				split.numFront = 1;
				System.arraycopy(triangle, 0, split.front, 0, triangle.length);
			}
			return;
		}

		// set number of triangles
		split.total = 3;
		split.numFront = (r1 ? 1 : 0) + (r2 ? 1 : 0) + (r3 ? 1 : 0);
		split.numBack = split.total - split.numFront;

		// hard case, split the three edges on the plane
		// determine which array to fill first, front or back, flip if we
		// cross the plane
		split.setSide(r1);

		// split first edge
		int first = 0;
		int second = stride;
		if (r1 != r2) {
			// split the edge
			splitEdge(triangle, first, second, stride, plane, split.edgeSplit, 0);

			// add first edge vertex and new vertex to current side
			split.add(triangle, first, stride);
			split.add(split.edgeSplit, 0, stride);

			// flip side and add new vertex and second edge vertex to current side
			split.setSide(!split.getSide());
			split.add(split.edgeSplit, 0, stride);
		} else {
			// add both vertices
			split.add(triangle, first, stride);
		}

		// split second edge
		first = stride;
		second = stride + stride;
		if (r2 != r3) {
			// split the edge
			splitEdge(triangle, first, second, stride, plane, split.edgeSplit, 0);

			// add first edge vertex and new vertex to current side
			split.add(triangle, first, stride);
			split.add(split.edgeSplit, 0, stride);

			// flip side and add new vertex and second edge vertex to current side
			split.setSide(!split.getSide());
			split.add(split.edgeSplit, 0, stride);
		} else {
			// add both vertices
			split.add(triangle, first, stride);
		}

		// split third edge
		first = stride + stride;
		second = 0;
		if (r3 != r1) {
			// split the edge
			splitEdge(triangle, first, second, stride, plane, split.edgeSplit, 0);

			// add first edge vertex and new vertex to current side
			split.add(triangle, first, stride);
			split.add(split.edgeSplit, 0, stride);

			// flip side and add new vertex and second edge vertex to current side
			split.setSide(!split.getSide());
			split.add(split.edgeSplit, 0, stride);
		} else {
			// add both vertices
			split.add(triangle, first, stride);
		}

		// triangulate the side with 2 triangles
		if (split.numFront == 2) {
			System.arraycopy(split.front, stride * 2, split.front, stride * 3, stride * 2);
			System.arraycopy(split.front, 0, split.front, stride * 5, stride);
		} else {
			System.arraycopy(split.back, stride * 2, split.back, stride * 3, stride * 2);
			System.arraycopy(split.back, 0, split.back, stride * 5, stride);
		}
	}

	static Number3D intersection = new Number3D();

	private static void splitEdge (float[] vertices, int s, int e, int stride, Plane plane, float[] split, int offset) {
		float t = Intersector.intersectLinePlane(vertices[s], vertices[s + 1], vertices[s + 2], vertices[e], vertices[e + 1],
			vertices[e + 2], plane, intersection);
		split[offset + 0] = intersection.x;
		split[offset + 1] = intersection.y;
		split[offset + 2] = intersection.z;
		for (int i = 3; i < stride; i++) {
			float a = vertices[s + i];
			float b = vertices[e + i];
			split[offset + i] = a + t * (b - a);
		}
	}

	public static class SplitTriangle {
		public float[] front;
		public float[] back;
		float[] edgeSplit;
		public int numFront;
		public int numBack;
		public int total;
		boolean frontCurrent = false;
		int frontOffset = 0;
		int backOffset = 0;

		/** Creates a new instance, assuming numAttributes attributes per triangle vertex.
		 * @param numAttributes must be >= 3 */
		public SplitTriangle (int numAttributes) {
			front = new float[numAttributes * 3 * 2];
			back = new float[numAttributes * 3 * 2];
			edgeSplit = new float[numAttributes];
		}

		@Override
		public String toString () {
			return "SplitTriangle [front=" + Arrays.toString(front) + ", back=" + Arrays.toString(back) + ", numFront=" + numFront
				+ ", numBack=" + numBack + ", total=" + total + "]";
		}

		void setSide (boolean front) {
			frontCurrent = front;
		}

		boolean getSide () {
			return frontCurrent;
		}

		void add (float[] vertex, int offset, int stride) {
			if (frontCurrent) {
				System.arraycopy(vertex, offset, front, frontOffset, stride);
				frontOffset += stride;
			} else {
				System.arraycopy(vertex, offset, back, backOffset, stride);
				backOffset += stride;
			}
		}

		void reset () {
			frontCurrent = false;
			frontOffset = 0;
			backOffset = 0;
			numFront = 0;
			numBack = 0;
			total = 0;
		}
	}

	public static class MinimumTranslationVector {
		public Vector2D normal = new Vector2D();
		public float depth = 0;
	}
}
