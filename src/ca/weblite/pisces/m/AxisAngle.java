/*
 * Copyright 2010 John Pritchard
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package ca.weblite.pisces.m;

import com.codename1.util.MathUtil;

/**
 * A four-element axis angle represented by double-precision floating point 
 * x,y,z,angle components.  An axis angle is a rotation of angle (radians)
 * about the vector (x,y,z).
 *
 */
public class AxisAngle 
    extends ca.weblite.pisces.d.FPMath
    implements Cloneable
{

    public	double	x;
    public	double	y;
    public	double	z;
    public	double	angle;


    public AxisAngle(double x, double y, double z, double angle)
    {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
    }
    public AxisAngle(double[] a) 
    { 
        super();
        this.x = a[0]; 
        this.y = a[1]; 
        this.z = a[2]; 
        this.angle = a[3]; 
    }
    public AxisAngle(AxisAngle a1)
    {
        super();
        this.x = a1.x;
        this.y = a1.y;
        this.z = a1.z;
        this.angle = a1.angle;
    }
    /**
     * Constructs and initializes an AxisAngle from the specified 
     * axis and angle.
     * @param axis the axis
     * @param angle the angle of rotation in radian
     */
    public AxisAngle(Vector axis, double angle){
        super();
        this.x = axis.x;
        this.y = axis.y;
        this.z = axis.z;
        this.angle = angle;
    }
    /**
     * Constructs and initializes an AxisAngle to (0,0,1,0).
     */
    public AxisAngle()
    {
        super();
        this.x = 0.0;
        this.y = 0.0;
        this.z = 1.0;
        this.angle = 0.0;
    }


    public final AxisAngle set(double x, double y, double z, double angle)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
        return this;
    }
    public final AxisAngle set(double[] a)
    {
        this.x = a[0];
        this.y = a[1];
        this.z = a[2];
        this.angle = a[3];
        return this;
    }
    public final AxisAngle set(AxisAngle a1)
    {
        this.x = a1.x;
        this.y = a1.y;
        this.z = a1.z;
        this.angle = a1.angle;
        return this;
    }
    public final AxisAngle set(Vector axis, double angle) {
        this.x = axis.x;
        this.y = axis.y;
        this.z = axis.z;
        this.angle = angle;
        return this;
    }
    public final double[] get(double[] a)
    {
        a[0] = this.x;
        a[1] = this.y;
        a[2] = this.z;
        a[3] = this.angle;
        return a;
    }
    /**
     * Sets the value of this axis-angle to the rotational component of
     * the passed matrix.
     * If the specified matrix has no rotational component, the value
     * of this AxisAngle is set to an angle of 0 about an axis of (0,1,0).
     *
     * @param m1 the matrix
     */
    public final AxisAngle set(Matrix m1)
    {
        Matrix nm = new Matrix(m1).normalize();

        x = (float)(nm.m21 - nm.m12);
        y = (float)(nm.m02 - nm.m20);
        z = (float)(nm.m10 - nm.m01);

        double mag = x*x + y*y + z*z;

        if (0.0 == Z(mag)){
            x = 0.0f;
            y = 1.0f;
            z = 0.0f;
            angle = 0.0f;
        }
        else {
            mag = Math.sqrt(mag);

            double sin = 0.5*mag;
            double cos = 0.5*(nm.m00 + nm.m11 + nm.m22 - 1.0);
            angle = (float)MathUtil.atan2(sin, cos);

            double invMag = 1.0/mag;
            x = x*invMag;
            y = y*invMag;
            z = z*invMag;
        } 
        return this;
    }
    /**
     * Sets the value of this axis-angle to the rotational equivalent
     * of the passed quaternion.
     * If the specified quaternion has no rotational component, the value
     * of this AxisAngle is set to an angle of 0 about an axis of (0,1,0).
     * @param q1  the Quat
     */
    public final AxisAngle set(Quat q1)
    {
        double mag = q1.x*q1.x + q1.y*q1.y + q1.z*q1.z;  

        if (0.0 == Z(mag)){
            x = 0.0f;
            y = 1.0f;
            z = 0.0f;
            angle = 0f;
        }
        else {
            mag = Math.sqrt(mag);
            double invMag = 1.0/mag;
	    
            x = q1.x*invMag;
            y = q1.y*invMag;
            z = q1.z*invMag;
            angle = 2.0*MathUtil.atan2(mag, q1.w); 
        }
        return this;
    }
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ", " + this.angle + ")";
    }
    public boolean equals(AxisAngle a1)
    {
        if (this == a1)
            return true;
        else if (null == a1)
            return false;
        else
            return (EEQ(this.x, a1.x) && EEQ(this.y, a1.y) && EEQ(this.z, a1.z)
                    && EEQ(this.angle, a1.angle));
    }
    public boolean equals(Object o1)
    {
        try {
            AxisAngle a2 = (AxisAngle) o1;
            return this.equals(a2);
        }
        catch (ClassCastException   e1) {
            return false;
        }
    }

    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different AxisAngle objects with identical data values
     * (i.e., AxisAngle.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     * @return the integer hash code value
     */  
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + Double.doubleToLongBits(x);
        bits = 31L * bits + Double.doubleToLongBits(y);
        bits = 31L * bits + Double.doubleToLongBits(z);
        bits = 31L * bits + Double.doubleToLongBits(angle);
        return (int) (bits ^ (bits >> 32));
    }
    public AxisAngle clone() {
        throw new RuntimeException("Clone not supported");
//        try {
//            return (AxisAngle)super.clone();
//        }
//        catch (CloneNotSupportedException e) {
//            throw new InternalError();
//        }
    }
	/**
	 * Get the axis angle, in radians.<br>
	 * An axis angle is a rotation angle about the vector (x,y,z).
     * 
	 * @return the angle, in radians.
	 */
	public final double getAngle() {
		return angle;
	}
	/**
	 * Set the axis angle, in radians.<br>
	 * An axis angle is a rotation angle about the vector (x,y,z).
	 * 
	 * @param angle The angle to set, in radians.
	 */
	public final AxisAngle setAngle(double angle) {
		this.angle = angle;
        return this;
	}
	public double getX() {
		return x;
	}
	public final AxisAngle setX(double x) {
		this.x = x;
        return this;
	}
	public  final double getY() {
		return y;
	}
	public final AxisAngle setY(double y) {
		this.y = y;
        return this;
	}
	public double getZ() {
		return z;
	}
	public final AxisAngle setZ(double z) {
		this.z = z;
        return this;
	}
}
