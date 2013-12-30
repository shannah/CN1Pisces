/*
 * Copyright (C) 2010 John Pritchard
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved. 
 *  
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License version 
 * 2 only, as published by the Free Software Foundation. 
 *  
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License version 2 for more details (a copy is 
 * included at /legal/license.txt). 
 *  
 * You should have received a copy of the GNU General Public License 
 * version 2 along with this work; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 
 * 02110-1301 USA 
 *  
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa 
 * Clara, CA 95054 or visit www.sun.com if you need additional 
 * information or have any questions.
 */
package pisces.d;

/**
 * The <code>LineSink</code> interface extends the
 * <code>PathSink</code> interface, and additionally accepts curve
 * drawing commands: <code>quadTo</code> and <code>curveTo</code>.
 */
public abstract class PathSink extends LineSink {

    /**
     * Draws a quadratic Bezier curve starting at the current drawing
     * position and ending at the point <code>(x2, y2)</code>
     * according to the formulas:
     *
     * <pre>
     * x(t) = (1 - t)^2*x0 + 2*(1 - t)*t*x1 + t^2*x2
     * y(t) = (1 - t)^2*y0 + 2*(1 - t)*t*y1 + t^2*x2
     * 
     * 0 <= t <= 1
     * </pre>
     *
     * where <code>(x0, y0)</code> is the current drawing position.
     * Finally, the current drawing position is set to <code>(x2,
     * y2)</code>.
     *
     * @param x1 the X coordinate of the control point 
     * @param y1 the Y coordinate of the control point 
     * @param x2 the final X coordinate 
     * @param y2 the final Y coordinate 
     */
    public abstract void quadTo(double x1, double y1, double x2, double y2);

    /**
     * Draws a cubic Bezier curve starting at the current drawing
     * position and ending at the point <code>(x3, y3)</code>
     * according to the formulas:
     *
     * <pre>
     * x(t) = (1 - t)^3*x0 + 3*(1 - t)^2*t*x1 + 3*(1 - t)*t^2*x2 + t^3*x3
     * y(t) = (1 - t)^3*y0 + 3*(1 - t)^2*t*y1 + 3*(1 - t)*t^2*y2 + t^3*x3
     * 
     * 0 <= t <= 1
     * </pre>
     *
     * where <code>(x0, y0)</code> is the current drawing position.
     * Finally, the current drawing position is set to <code>(x3,
     * y3)</code>.
     *
     * @param x1 the X coordinate of the first control point 
     * @param y1 the Y coordinate of the first control point 
     * @param x2 the X coordinate of the second control point
     * @param y2 the Y coordinate of the second control point
     * @param x3 the final X coordinate 
     * @param y3 the final Y coordinate 
     */
    public abstract void cubicTo(double x1, double y1, double x2, double y2, double x3, double y3);

    public PathSink clone(){
        return (PathSink)super.clone();
    }
}
