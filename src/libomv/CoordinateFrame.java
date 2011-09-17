/**
 * Copyright (c) 2007-2008, openmetaverse.org
 * Portions Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without 
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names 
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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

package libomv;

import libomv.types.Matrix4;
import libomv.types.Quaternion;
import libomv.types.Vector3;

public class CoordinateFrame {
    public static final Vector3 X_AXIS = new Vector3(1f, 0f, 0f);
    public static final Vector3 Y_AXIS = new Vector3(0f, 1f, 0f);
    public static final Vector3 Z_AXIS = new Vector3(0f, 0f, 1f);

    /* Origin position of this coordinate frame */
    public final Vector3 getOrigin()
    {
        return origin;
    }
    public final void setOrigin(Vector3 value) throws Exception
    {
        if (!value.IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame.Origin assignment");
        }
        origin = value;
    }
    /* X axis of this coordinate frame, or Forward/At in grid terms */
    public final Vector3 getXAxis()
    {
        return xAxis;
    }
    public final void setXAxis(Vector3 value) throws Exception
    {
        if (!value.IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame.XAxis assignment");
        }
        xAxis = value;
    }
    /* Y axis of this coordinate frame, or Left in grid terms */
    public final Vector3 getYAxis()
    {
        return yAxis;
    }
    public final void setYAxis(Vector3 value) throws Exception
    {
        if (!value.IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame.YAxis assignment");
        }
        yAxis = value;
    }
    /* Z axis of this coordinate frame, or Up in grid terms */
    public final Vector3 getZAxis()
    {
        return zAxis;
    }
    public final void setZAxis(Vector3 value) throws Exception
    {
        if (!value.IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame.ZAxis assignment");
        }
        zAxis = value;
    }

    protected Vector3 origin;
    protected Vector3 xAxis;
    protected Vector3 yAxis;
    protected Vector3 zAxis;

    public CoordinateFrame(Vector3 origin) throws Exception
    {
        this.origin = origin;
        xAxis = X_AXIS;
        yAxis = Y_AXIS;
        zAxis = Z_AXIS;

        if (!this.origin.IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame constructor");
        }
    }

    public CoordinateFrame(Vector3 origin, Vector3 direction) throws Exception
    {
        this.origin = origin;
        LookDirection(direction);

        if (!IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame constructor");
        }
    }

    public CoordinateFrame(Vector3 origin, Vector3 xAxis, Vector3 yAxis, Vector3 zAxis) throws Exception
    {
        this.origin = origin;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;

        if (!IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame constructor");
        }
    }

    public CoordinateFrame(Vector3 origin, Matrix4 rotation) throws Exception
    {
        this.origin = origin;
        xAxis = rotation.getAtAxis();
        yAxis = rotation.getLeftAxis();
        zAxis = rotation.getUpAxis();

        if (!IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame constructor");
        }
    }

    public CoordinateFrame(Vector3 origin, Quaternion rotation) throws Exception
    {
        Matrix4 m = Matrix4.CreateFromQuaternion(rotation);

        this.origin = origin;
        xAxis = m.getAtAxis();
        yAxis = m.getLeftAxis();
        zAxis = m.getUpAxis();

        if (!IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame constructor");
        }
    }

    public final void ResetAxes()
    {
        xAxis = X_AXIS;
        yAxis = Y_AXIS;
        zAxis = Z_AXIS;
    }

    public final void Rotate(float angle, Vector3 rotationAxis) throws Exception
    {
        Quaternion q = Quaternion.CreateFromAxisAngle(rotationAxis, angle);
        Rotate(q);
    }

    public final void Rotate(Quaternion q) throws Exception
    {
        Matrix4 m = Matrix4.CreateFromQuaternion(q);
        Rotate(m);
    }

    public final void Rotate(Matrix4 m) throws Exception
    {
        xAxis = Vector3.Transform(xAxis, m);
        yAxis = Vector3.Transform(yAxis, m);

        Orthonormalize();

        if (!IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame.Rotate()");
        }
    }

    public final void Roll(float angle) throws Exception
    {
        Quaternion q = Quaternion.CreateFromAxisAngle(xAxis, angle);
        Matrix4 m = Matrix4.CreateFromQuaternion(q);
        Rotate(m);

        if (!yAxis.IsFinite() || !zAxis.IsFinite())
        {
            throw new Exception("Non-finite in CoordinateFrame.Roll()");
        }
    }

    public final void Pitch(float angle) throws Throwable
    {
        Quaternion q = Quaternion.CreateFromAxisAngle(yAxis, angle);
        Matrix4 m = Matrix4.CreateFromQuaternion(q);
        Rotate(m);

        if (!xAxis.IsFinite() || !zAxis.IsFinite())
        {
            throw new Throwable("Non-finite in CoordinateFrame.Pitch()");
        }
    }

    public final void Yaw(float angle) throws Throwable
    {
        Quaternion q = Quaternion.CreateFromAxisAngle(zAxis, angle);
        Matrix4 m = Matrix4.CreateFromQuaternion(q);
        Rotate(m);

        if (!xAxis.IsFinite() || !yAxis.IsFinite())
        {
            throw new Throwable("Non-finite in CoordinateFrame.Yaw()");
        }
    }

    public final void LookDirection(Vector3 at)
    {
        LookDirection(at, Z_AXIS);
    }

    /**
     *  @param at Looking direction, must be a normalized vector
     *  @param upDirection Up direction, must be a normalized vector
     */
    public final void LookDirection(Vector3 at, Vector3 upDirection)
    {
        // The two parameters cannot be parallel
        Vector3 left = Vector3.Cross(upDirection, at);
        if (left == Vector3.Zero)
        {
            // Prevent left from being zero
            at.X += 0.01f;
            at.Normalize();
            left = Vector3.Cross(upDirection, at);
        }
        left.Normalize();

        xAxis = at;
        yAxis = left;
        zAxis = Vector3.Cross(at, left);
    }

    /** Align the coordinate frame X and Y axis with a given rotation around the Z axis in radians
     * 
     *  @param heading Absolute rotation around the Z axis in radians
     */
    public final void LookDirection(double heading)
    {
        yAxis.X = (float)Math.cos(heading);
        yAxis.Y = (float)Math.sin(heading);
        xAxis.X = (float)-Math.sin(heading);
        xAxis.Y = (float)Math.cos(heading);
    }

    public final void LookAt(Vector3 origin, Vector3 target)
    {
        LookAt(origin, target, new Vector3(0f, 0f, 1f));
    }

    public final void LookAt(Vector3 origin, Vector3 target, Vector3 upDirection)
    {
        this.origin = origin;
        Vector3 at = target.subtract(origin);
        at.Normalize();

        LookDirection(at, upDirection);
    }

    protected final boolean IsFinite()
    {
        if (xAxis.IsFinite() && yAxis.IsFinite() && zAxis.IsFinite())
        {
            return true;
        }
        return false;
    }

    protected final void Orthonormalize()
    {
        // Make sure the axis are orthagonal and normalized
        xAxis.Normalize();
        yAxis.subtract(Vector3.multiply(xAxis, Vector3.multiply(xAxis, yAxis)));
        yAxis.Normalize();
        zAxis = Vector3.Cross(xAxis, yAxis);
    }
}

