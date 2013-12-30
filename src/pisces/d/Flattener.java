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
 * The <code>Flattener</code> class rewrites a general path, which
 * may include curved segments, into one containing only linear
 * segments suitable for sending to a <code>LineSink</code>.
 *
 * <p> Curved segments specified by <code>quadTo</code> and
 * <code>curveTo</code> commands will be subdivided into two pieces.
 * When the control points of a segment lie sufficiently close
 * togther, such that <code>max(x_i) - min(x_i) < flatness</code> and
 * <code>max(y_i) - min(y_i) < flatness</code> for a user-supplied
 * <code>flatness</code> parameter, a <code>lineTo</code> command is
 * emitted between the first and last points of the curve.
 */
public class Flattener extends PathSink {
    /*
     * Always subdivide segments where the endpoints are
     * separated by more than this amount
     */
    public static final double MAX_CHORD_LENGTH_SQ = 16.0;
    public static final double MIN_CHORD_LENGTH_SQ = 0.25;

    public static final double LG_FLATNESS = 0.5; // half pixel, 2^(-1)
    public static final int FLATNESS_SQ_SHIFT = (int)(2.0*(-LG_FLATNESS));

    LineSink output;
    double flatness, flatnessSq;
    double x0, y0;

    /**
     * Empty constructor.  <code>setOutput</code> and
     * <code>setFlatness</code> must be called prior to calling any
     * other methods.
     */
    public Flattener() {
        super();
    }
    /**
     * Constructs a <code>Flattener</code> that will rewrite any
     * incoming <code>quadTo</code> and <code>curveTo</code> commands
     * into a series of <code>lineTo</code> commands with maximum X
     * and Y extents no larger than the supplied <code>flatness</code>
     * value.  The flat segments will be sent as commands to the given
     * <code>LineSink</code>.
     *
     * @param output a <code>LineSink</code> to which commands
     * should be sent.
     * @param flatness the maximum extent of a subdivided output line
     * segment
     */
    public Flattener(LineSink output, double flatness) {
        super();
        setOutput(output);
        setFlatness(flatness);
    }


    public void dispose(){
        this.output = null;
    }
    public void setOutput(LineSink output) {
        this.output = output;
    }
    public void setFlatness(double flatness) {
        this.flatness = flatness;
        this.flatnessSq = (flatness*flatness);
    }
    public void moveTo(double x0, double y0) {
        output.moveTo(x0, y0);
        this.x0 = x0;
        this.y0 = y0;
    }
    public void lineJoin() {
        output.lineJoin();
    }
    public void lineTo(double x1, double y1) {
        output.lineJoin();
        output.lineTo(x1, y1);
        this.x0 = x1;
        this.y0 = y1;
    }
    public void quadTo(double x1, double y1, double x2, double y2) {
        output.lineJoin();
        quadToHelper(x1, y1, x2, y2);
    }
    /*
     * See cubic (8 argument) version below for commentary
     */
    private boolean flatEnough(double x0, double y0,
                               double x1, double y1,
                               double x2, double y2)
    {
        double dx = x2 - x0;
        double dy = y2 - y0;
        double denom2 = dx*dx + dy*dy;
        if (denom2 > MAX_CHORD_LENGTH_SQ) {
            return false;
        }
        else {
            /*
             * Stop dividing if all control points are close together
             */
            if (denom2 < MIN_CHORD_LENGTH_SQ) {
                double minx = Math.min(Math.min(x0, x1), x2);
                double miny = Math.min(Math.min(y0, y1), y2);
                double maxx = Math.max(Math.max(x0, x1), x2);
                double maxy = Math.max(Math.max(y0, y1), y2);

                double dx1 = maxx - minx;
                double dy1 = maxy - miny;
                double l2 = dx1*dx1 + dy1*dy1;
                if (l2 < MIN_CHORD_LENGTH_SQ) {
                    return true;
                }
            }

            double num = -dy*x1 + dx*y1 + (x0*y2 - x2*y0);
            double numsq = (num*num);
            double df2 = (denom2*flatnessSq);
            return (numsq < df2);
        }
    }
    private void quadToHelper(double x1, double y1, double x2, double y2)
    {
        if (flatEnough(x0, y0, x1, y1, x2, y2)) {
            output.lineTo(x1, y1);
            output.lineTo(x2, y2);
        }
        else {

            double x01 = x0 + x1; // >> 1
            double y01 = y0 + y1; // >> 1
            double x12 = x1 + x2; // >> 1
            double y12 = y1 + y2; // >> 1

            double x012 = x01 + x12; // >> 2
            double y012 = y01 + y12; // >> 2

            quadToHelper((x01/2.0), (y01/2.0),
                         (x012/4.0), (y012/4.0));

            quadToHelper((x12/2.0), (y12/2.0),
                         x2, y2); 
        }
	
        this.x0 = x2;
        this.y0 = y2;
    }
    public void cubicTo(double x1, double y1,
                        double x2, double y2,
                        double x3, double y3)
    {
        output.lineJoin();
        cubicToHelper(x1, y1, x2, y2, x3, y3);
    }
    /*
     * IMPL_NOTE - analyze position of radix points to avoid
     * possibility of overflow
     */
    private boolean flatEnough(double x0, double y0,
                               double x1, double y1,
                               double x2, double y2,
                               double x3, double y3)
    {
        double dx = x3 - x0;
        double dy = y3 - y0;
        double denom2 = dx*dx + dy*dy;
        /*
         * Always subdivide curves with a large chord length
         */
        if (denom2 > MAX_CHORD_LENGTH_SQ) {
            return false;
        }
        else {
            /*
             * Stop dividing if all control points are close together
             */
            if (denom2 < MIN_CHORD_LENGTH_SQ) {
                double minx = Math.min(Math.min(Math.min(x0, x1), x2), x3);
                double miny = Math.min(Math.min(Math.min(y0, y1), y2), y3);
                double maxx = Math.max(Math.max(Math.max(x0, x1), x2), x3);
                double maxy = Math.max(Math.max(Math.max(y0, y1), y2), y3);

                double dx1 = maxx - minx;
                double dy1 = maxy - miny;
                double l2 = dx1*dx1 + dy1*dy1;
                if (l2 < MIN_CHORD_LENGTH_SQ) {
                    return true;
                }
            }
            /*
             * Want to know if num/denom < flatness, so compare
             * numerator^2 against (denominator*flatness)^2 to avoid a square root
             */
            double df2 = denom2*flatnessSq;

            double cross = x0*y3 - x3*y0;
            double num1 = dx*y1 - dy*x1 + cross;
            double num1sq = num1*num1;
            if (num1sq > df2) {
                return false;
            }
            else {
                double num2 = dx*y2 - dy*x2 + cross;
                double num2sq = num2*num2;

                return (num2sq < df2);
            }
        }
    }

    private void cubicToHelper(double x1, double y1,
                               double x2, double y2,
                               double x3, double y3) {
        if (flatEnough(x0, y0, x1, y1, x2, y2, x3, y3)) {
            output.lineTo(x1, y1);
            output.lineTo(x2, y2);
            output.lineTo(x3, y3);
        } 
        else {

            double x01 = x0 + x1; // >> 1
            double y01 = y0 + y1; // >> 1
            double x12 = x1 + x2; // >> 1
            double y12 = y1 + y2; // >> 1
            double x23 = x2 + x3; // >> 1
            double y23 = y2 + y3; // >> 1

            double x012 = x01 + x12; // >> 2
            double y012 = y01 + y12; // >> 2
            double x123 = x12 + x23; // >> 2
            double y123 = y12 + y23; // >> 2
            double x0123 = x012 + x123; // >> 3
            double y0123 = y012 + y123; // >> 3
            
            cubicToHelper((x01/2.0), (y01/2.0),
                          (x012/4.0), (y012/4.0),
                          (x0123/8.0), (y0123/8.0));
            cubicToHelper((x123/4.0), (y123/4.0),
                          (x23/2.0), (y23/2.0),
                          x3, y3);
        }
            
        this.x0 = x3;
        this.y0 = y3;
    }
    public void close() {
        output.lineJoin();
        output.close();
    }
    public void end() {
        output.end();
    }
}

