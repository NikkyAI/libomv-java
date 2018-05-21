/**
 * Copyright (c) 2010-2012, Dahlia Trimble
 * Copyright (c) 2011-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.primMesher.types;

import java.util.ArrayList;

import libomv.types.Vector3;

public class AngleList {
	private float iX;
	private float iY; // intersection point

	private static Angle[] angles3 = new Angle[] { new Angle(0.0f, 1.0f, 0.0f),
			new Angle(0.33333333333333333f, -0.5f, 0.86602540378443871f),
			new Angle(0.66666666666666667f, -0.5f, -0.86602540378443837f), new Angle(1.0f, 1.0f, 0.0f) };

	private static Vector3[] normals3 = { new Vector3(0.25f, 0.4330127019f, 0.0f).normalize(),
			new Vector3(-0.5f, 0.0f, 0.0f).normalize(), new Vector3(0.25f, -0.4330127019f, 0.0f).normalize(),
			new Vector3(0.25f, 0.4330127019f, 0.0f).normalize() };

	private static Angle[] angles4 = { new Angle(0.0f, 1.0f, 0.0f), new Angle(0.25f, 0.0f, 1.0f),
			new Angle(0.5f, -1.0f, 0.0f), new Angle(0.75f, 0.0f, -1.0f), new Angle(1.0f, 1.0f, 0.0f) };

	private static Vector3[] normals4 = { new Vector3(0.5f, 0.5f, 0.0f).normalize(),
			new Vector3(-0.5f, 0.5f, 0.0f).normalize(), new Vector3(-0.5f, -0.5f, 0.0f).normalize(),
			new Vector3(0.5f, -0.5f, 0.0f).normalize(), new Vector3(0.5f, 0.5f, 0.0f).normalize() };

	private static Angle[] angles24 = { new Angle(0.0f, 1.0f, 0.0f),
			new Angle(0.041666666666666664f, 0.96592582628906831f, 0.25881904510252074f),
			new Angle(0.083333333333333329f, 0.86602540378443871f, 0.5f),
			new Angle(0.125f, 0.70710678118654757f, 0.70710678118654746f),
			new Angle(0.16666666666666667f, 0.5f, 0.8660254037844386f),
			new Angle(0.20833333333333331f, 0.25881904510252096f, 0.9659258262890682f), new Angle(0.25f, 0.0f, 1.0f),
			new Angle(0.29166666666666663f, -0.25881904510252063f, 0.96592582628906831f),
			new Angle(0.33333333333333333f, -0.5f, 0.86602540378443871f),
			new Angle(0.375f, -0.70710678118654746f, 0.70710678118654757f),
			new Angle(0.41666666666666663f, -0.86602540378443849f, 0.5f),
			new Angle(0.45833333333333331f, -0.9659258262890682f, 0.25881904510252102f), new Angle(0.5f, -1.0f, 0.0f),
			new Angle(0.54166666666666663f, -0.96592582628906842f, -0.25881904510252035f),
			new Angle(0.58333333333333326f, -0.86602540378443882f, -0.5f),
			new Angle(0.62499999999999989f, -0.70710678118654791f, -0.70710678118654713f),
			new Angle(0.66666666666666667f, -0.5f, -0.86602540378443837f),
			new Angle(0.70833333333333326f, -0.25881904510252152f, -0.96592582628906809f),
			new Angle(0.75f, 0.0f, -1.0f), new Angle(0.79166666666666663f, 0.2588190451025203f, -0.96592582628906842f),
			new Angle(0.83333333333333326f, 0.5f, -0.86602540378443904f),
			new Angle(0.875f, 0.70710678118654735f, -0.70710678118654768f),
			new Angle(0.91666666666666663f, 0.86602540378443837f, -0.5f),
			new Angle(0.95833333333333326f, 0.96592582628906809f, -0.25881904510252157f), new Angle(1.0f, 1.0f, 0.0f) };

	private Angle interpolatePoints(float newPoint, Angle p1, Angle p2) {
		float m = (newPoint - p1.angle) / (p2.angle - p1.angle);
		return new Angle(newPoint, p1.x + m * (p2.x - p1.x), p1.y + m * (p2.y - p1.y));
	}

	private void intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) { // ref:
																														// http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		double uaNumerator = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);

		if (denom != 0.0) {
			double ua = uaNumerator / denom;
			iX = (float) (x1 + ua * (x2 - x1));
			iY = (float) (y1 + ua * (y2 - y1));
		}
	}

	ArrayList<Angle> angles;
	ArrayList<Vector3> normals;

	protected void makeAngles(int sides, float startAngle, float stopAngle) throws Exception {
		angles = new ArrayList<Angle>();
		normals = new ArrayList<Vector3>();

		double twoPi = Math.PI * 2.0;
		float twoPiInv = 1.0f / (float) twoPi;

		if (sides < 1)
			throw new Exception("number of sides not greater than zero");
		if (stopAngle <= startAngle)
			throw new Exception("stopAngle not greater than startAngle");

		if ((sides == 3 || sides == 4 || sides == 24)) {
			startAngle *= twoPiInv;
			stopAngle *= twoPiInv;

			Angle[] sourceAngles;
			if (sides == 3)
				sourceAngles = angles3;
			else if (sides == 4)
				sourceAngles = angles4;
			else
				sourceAngles = angles24;

			int startAngleIndex = (int) (startAngle * sides);
			int endAngleIndex = sourceAngles.length - 1;
			if (stopAngle < 1.0f)
				endAngleIndex = (int) (stopAngle * sides) + 1;
			if (endAngleIndex == startAngleIndex)
				endAngleIndex++;

			for (int angleIndex = startAngleIndex; angleIndex < endAngleIndex + 1; angleIndex++) {
				angles.add(sourceAngles[angleIndex]);
				if (sides == 3)
					normals.add(normals3[angleIndex]);
				else if (sides == 4)
					normals.add(normals4[angleIndex]);
			}

			if (startAngle > 0.0f)
				angles.set(0, interpolatePoints(startAngle, angles.get(0), angles.get(1)));

			if (stopAngle < 1.0f) {
				int lastAngleIndex = angles.size() - 1;
				angles.set(lastAngleIndex,
						interpolatePoints(stopAngle, angles.get(lastAngleIndex - 1), angles.get(lastAngleIndex)));
			}
		} else {
			double stepSize = twoPi / sides;

			int startStep = (int) (startAngle / stepSize);
			double angle = stepSize * startStep;
			int step = startStep;
			double stopAngleTest = stopAngle;
			if (stopAngle < twoPi) {
				stopAngleTest = stepSize * ((int) (stopAngle / stepSize) + 1);
				if (stopAngleTest < stopAngle)
					stopAngleTest += stepSize;
				if (stopAngleTest > twoPi)
					stopAngleTest = twoPi;
			}

			while (angle <= stopAngleTest) {
				angles.add(new Angle((float) angle, (float) Math.cos(angle), (float) Math.sin(angle)));
				step += 1;
				angle = stepSize * step;
			}

			if (startAngle > angles.get(0).angle) {
				Angle angle1 = angles.get(0);
				Angle angle2 = angles.get(1);
				intersection(angle1.x, angle1.y, angle2.x, angle2.y, 0.0f, 0.0f, (float) Math.cos(startAngle),
						(float) Math.sin(startAngle));
				angles.set(0, new Angle(startAngle, iX, iY));
			}

			int index = angles.size() - 1;
			if (stopAngle < angles.get(index).angle) {
				Angle angle1 = angles.get(index - 1);
				Angle angle2 = angles.get(index);
				intersection(angle1.x, angle1.y, angle2.x, angle2.y, 0.0f, 0.0f, (float) Math.cos(stopAngle),
						(float) Math.sin(stopAngle));
				angles.set(index, new Angle(stopAngle, iX, iY));
			}
		}
	}
}
