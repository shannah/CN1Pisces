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

package pisces.m;

import com.codename1.util.MathUtil;
import java.lang.Math;

/**
 * A 3-element vector that is represented by double-precision floating point 
 * x,y,z coordinates.  If this value represents a normal, then it should
 * be normalized.
 *
 */
public class Vector 
    extends Tuple
{

    public Vector(double x, double y, double z)
    {
        super(x,y,z);
    }
    public Vector(double[] v)
    {
        super(v);
    }
    public Vector(Vector v1)
    {
        super(v1);
    }
    public Vector(Tuple t1) 
    {
        super(t1); 
    }
    public Vector()
    {
        super();
    }


    /**
     * Sets this vector to the vector cross product of vectors v1 and v2.
     * @param v1 the first vector
     * @param v2 the second vector
     */
    public final Vector cross(Vector v1, Vector v2)
    { 
        double x,y;

        x = v1.y*v2.z - v1.z*v2.y;
        y = v2.x*v1.z - v2.z*v1.x;
        this.z = v1.x*v2.y - v1.y*v2.x;
        this.x = x;
        this.y = y;

        return this;
    }
    /**
     * Sets the value of this vector to the normalization of vector v1.
     * @param v1 the un-normalized vector
     */
    public final Vector normalize(Vector v1)
    {
        double norm;

        norm = 1.0/Math.sqrt(v1.x*v1.x + v1.y*v1.y + v1.z*v1.z);
        this.x = v1.x*norm;
        this.y = v1.y*norm;
        this.z = v1.z*norm;

        return this;
    }
    /**
     * Normalizes this vector in place.
     */
    public final Vector normalize()
    {
        double norm;

        norm = 1.0/Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
        this.x *= norm;
        this.y *= norm;
        this.z *= norm;

        return this;
    }
    /**
     * Returns the dot product of this vector and vector v1.
     * @param v1 the other vector
     * @return the dot product of this and v1
     */
    public final double dot(Vector v1)
    {
        return (this.x*v1.x + this.y*v1.y + this.z*v1.z);
    }
    /**
     * Returns the squared length of this vector.
     * @return the squared length of this vector
     */
    public final double lengthSquared()
    {
        return (this.x*this.x + this.y*this.y + this.z*this.z);
    }
    /**
     * Returns the length of this vector.
     * @return the length of this vector
     */
    public final double length()
    {
        return Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }
    /** 
     *   Returns the angle in radians between this vector and the vector
     *   parameter; the return value is constrained to the range [0,PI]. 
     *   @param v1    the other vector 
     *   @return   the angle in radians in the range [0,PI] 
     */   
    public final double angle(Vector v1) 
    { 
        double vDot = (this.dot(v1) / ( this.length()*v1.length()));

        if ( vDot < -1.0)
            vDot = -1.0;
        else if ( vDot >  1.0)
            vDot =  1.0;

        return MathUtil.acos(vDot);
    } 
    public Vector clone(){
        return (Vector)super.clone();
    }
}
