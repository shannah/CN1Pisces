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

import java.lang.Math;

/**
 * A 3 element point that is represented by double precision floating point 
 * x,y,z coordinates.
 *
 */
public class Point
    extends Tuple
{


    public Point(double x, double y, double z)
    {
        super(x,y,z);
    }
    public Point(double[] p)
    {
        super(p);
    }
    public Point(Point p1)
    {
        super(p1);
    }
    public Point(Tuple t1)
    {
        super(t1);
    }
    public Point()
    {
        super();
    }


    /**
     * Returns the square of the distance between this point and point p1.
     * @param p1 the other point 
     * @return the square of the distance
     */
    public final double distanceSquared(Point p1)
    {
        double dx, dy, dz;

        dx = this.x-p1.x;
        dy = this.y-p1.y;
        dz = this.z-p1.z;
        return (dx*dx+dy*dy+dz*dz);
    }
    /**
     * Returns the distance between this point and point p1.
     * @param p1 the other point
     * @return the distance 
     */
    public final double distance(Point p1)
    {
        double dx, dy, dz;

        dx = this.x-p1.x;
        dy = this.y-p1.y;
        dz = this.z-p1.z;
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
    /**
     * Computes the L-1 (Manhattan) distance between this point and
     * point p1.  The L-1 distance is equal to:
     *  abs(x1-x2) + abs(y1-y2) + abs(z1-z2).
     * @param p1 the other point
     * @return  the L-1 distance
     */
    public final double distanceL1(Point p1) {
        return Math.abs(this.x-p1.x) + Math.abs(this.y-p1.y) +
            Math.abs(this.z-p1.z);
    }
    /**
     * Computes the L-infinite distance between this point and
     * point p1.  The L-infinite distance is equal to
     * MAX[abs(x1-x2), abs(y1-y2), abs(z1-z2)].
     * @param p1 the other point
     * @return  the L-infinite distance
     */
    public final double distanceLinf(Point p1) {
        double tmp;
        tmp = Math.max( Math.abs(this.x-p1.x), Math.abs(this.y-p1.y));

        return Math.max(tmp,Math.abs(this.z-p1.z));
    }
    public Point clone(){
        return (Point)super.clone();
    }
}
