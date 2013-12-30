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

import pisces.m.Matrix;

public class Stroker
    extends LineSink
{

    private static final int MOVE_TO = 0;
    private static final int LINE_TO = 1;
    private static final int CLOSE = 2;

    private static final double ROUND_JOIN_THRESHOLD = 1e3;
    private static final double ROUND_JOIN_INTERNAL_THRESHOLD = 1e9;


    private LineSink output;

    private double lineWidth;
    private int capStyle;
    private int joinStyle;
    private double miterLimit;

    private Matrix transform;
    private double m00, m01;
    private double m10, m11;

    private double lineWidth2;
    private double scaledLineWidth2;
    /*
     * For any pen offset (pen_dx, pen_dy) that does not depend on the
     * line orientation, the pen should be transformed so that:
     *
     * pen_dx' = m00*pen_dx + m01*pen_dy
     * pen_dy' = m10*pen_dx + m11*pen_dy
     *
     * For a round pen, this means:
     *
     * pen_dx(r, theta) = r*cos(theta)
     * pen_dy(r, theta) = r*sin(theta)
     *
     * pen_dx'(r, theta) = r*(m00*cos(theta) + m01*sin(theta))
     * pen_dy'(r, theta) = r*(m10*cos(theta) + m11*sin(theta))
     */
    private int numPenSegments;
    private double[] pen_dx;
    private double[] pen_dy;
    private boolean[] penIncluded;
    private double[] join;

    private double[] offset = new double[2];
    private double[] reverse = new double[100];
    private double[] miter = new double[2];
    private double miterLimitSq;

    private int prev;
    private int rindex;
    private boolean started;
    private boolean lineToOrigin;
    private boolean joinToOrigin;

    private double sx0, sy0, sx1, sy1, x0, y0, x1, y1;
    private double mx0, my0, mx1, my1, omx, omy;
    private double lx0, ly0, lx1, ly1, lx0p, ly0p, px0, py0;

    private double m00_2_m01_2;
    private double m10_2_m11_2;
    private double m00_m10_m01_m11;
    private double determinant;

    private boolean joinSegment = false;

    /**
     * Empty constructor.  <code>setOutput</code> and
     * <code>setParameters</code> must be called prior to calling any
     * other methods.
     */
    public Stroker() {
        super();
    }
    /**
     * Constructs a <code>Stroker</code>.
     *
     * @param output an output <code>LineSink</code>.
     * @param lineWidth the desired line width in pixels, in S15.16
     * format.
     * @param capStyle the desired end cap style, one of
     * <code>CAP_BUTT</code>, <code>CAP_ROUND</code> or
     * <code>CAP_SQUARE</code>.
     * @param joinStyle the desired line join style, one of
     * <code>JOIN_MITER</code>, <code>JOIN_ROUND</code> or
     * <code>JOIN_BEVEL</code>.
     * @param miterLimit the desired miter limit
     * @param transform a <code>Matrix</code> object indicating
     * the transform that has been previously applied to all incoming
     * coordinates.  This is required in order to produce consistently
     * shaped end caps and joins.
     */
    public Stroker(LineSink output,
                   double lineWidth,
                   int capStyle,
                   int joinStyle,
                   double miterLimit,
                   Matrix transform)
    {
        super();
        this.setOutput(output);
        this.setParameters(lineWidth, capStyle, joinStyle, miterLimit, transform);
    }


    public void dispose(){
        this.output = null;
    }
    /**
     * Sets the output <code>LineSink</code> of this
     * <code>Stroker</code>.
     *
     * @param output an output <code>LineSink</code>.
     */
    public void setOutput(LineSink output) {
        this.output = output;
    }
    /**
     * Sets the parameters of this <code>Stroker</code>.
     * @param lineWidth the desired line width in pixels, in S15.16
     * format.
     * @param capStyle the desired end cap style, one of
     * <code>CAP_BUTT</code>, <code>CAP_ROUND</code> or
     * <code>CAP_SQUARE</code>.
     * @param joinStyle the desired line join style, one of
     * <code>JOIN_MITER</code>, <code>JOIN_ROUND</code> or
     * <code>JOIN_BEVEL</code>.
     * @param miterLimit the desired miter limit, in S15.16 format.
     * @param transform a <code>Matrix</code> object indicating
     * the transform that has been previously applied to all incoming
     * coordinates.  This is required in order to produce consistently
     * shaped end caps and joins.
     */
    public void setParameters(double lineWidth,
                              int capStyle,
                              int joinStyle,
                              double miterLimit,
                              Matrix transform)
    {
        this.lineWidth = lineWidth;
        this.lineWidth2 = lineWidth/2.0;
        this.scaledLineWidth2 = transform.m00*lineWidth2;
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;
        this.miterLimit = miterLimit;

        this.transform = transform;
        this.m00 = transform.m00;
        this.m01 = transform.m01;
        this.m10 = transform.m10;
        this.m11 = transform.m11;

        this.m00_2_m01_2 = m00*m00 + m01*m01;
        this.m10_2_m11_2 = m10*m10 + m11*m11;
        this.m00_m10_m01_m11 = m00*m10 + m01*m11;

        this.determinant = m00*m11 - m01*m10;

        if (joinStyle == JOIN_MITER) {

            double limit = (miterLimit*lineWidth2*this.determinant);

            this.miterLimitSq = (limit*limit);
        }

        this.numPenSegments = (int)(PI*lineWidth);

        if (pen_dx == null || pen_dx.length < numPenSegments) {
            this.pen_dx = new double[numPenSegments];
            this.pen_dy = new double[numPenSegments];
            this.penIncluded = new boolean[numPenSegments];
            this.join = new double[2*numPenSegments];
        }

        for (int i = 0; i < numPenSegments; i++) {

            double r = lineWidth/2.0;

            double theta = i*PI_M2/numPenSegments;

            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            pen_dx[i] = (r*(m00*cos + m01*sin));
            pen_dy[i] = (r*(m10*cos + m11*sin));
        }

        prev = CLOSE;
        rindex = 0;
        started = false;
        lineToOrigin = false;
    }
    public void moveTo(double x0, double y0) {

        if (lineToOrigin) {
            /*
             * not closing the path, do the previous lineTo
             */
            lineToImpl(sx0, sy0, joinToOrigin);
            lineToOrigin = false;
        }
        
        if (prev == LINE_TO) {
            finish();
        }

        this.sx0 = this.x0 = x0;
        this.sy0 = this.y0 = y0;
        this.rindex = 0;
        this.started = false;
        this.joinSegment = false;
        this.prev = MOVE_TO;
    }
    public void lineJoin() {
        this.joinSegment = true;
    }
    public void lineTo(double x1, double y1) {

        if (lineToOrigin) {
            if (EEQ(x1,sx0) && EEQ(y1,sy0)) {
                /*
                 * Staying in the starting point
                 */
                return;
            }
            else {
                /*
                 * Not closing the path, do the previous lineTo   
                 */
                lineToImpl(sx0, sy0, joinToOrigin);
                lineToOrigin = false;
            }
        }
        else if (EEQ(x1,x0) && EEQ(y1,y0)) {
            return;
        }
        else if (EEQ(x1,sx0) && EEQ(y1,sy0)) {
            lineToOrigin = true;
            joinToOrigin = joinSegment;
            joinSegment = false;
            return;
        }

        lineToImpl(x1, y1, joinSegment);
        joinSegment = false;
    }

    public void close() {
        
        if (lineToOrigin) {
            /*
             * Ignore the previous lineTo
             */
            lineToOrigin = false;
        }

        if (!started) {
            finish();
            return;
        }
        else {
            computeOffset(x0, y0, sx0, sy0, offset);
            double mx = offset[0];
            double my = offset[1];
            /*
             * Draw penultimate join
             */
            boolean ccw = isCCW(px0, py0, x0, y0, sx0, sy0);
            if (joinSegment) {
                if (joinStyle == JOIN_MITER) {
                    drawMiter(px0, py0, x0, y0, sx0, sy0, omx, omy, mx, my, ccw);
                }
                else if (joinStyle == JOIN_ROUND) {
                    drawRoundJoin(x0, y0, omx, omy, mx, my, 0, false, ccw,
                                  ROUND_JOIN_THRESHOLD);
                }
            }
            else {
                /*
                 * Draw internal joins as round
                 */
                drawRoundJoin(x0, y0, 
                              omx, omy,
                              mx, my, 0, false, ccw,
                              ROUND_JOIN_INTERNAL_THRESHOLD);
            }

            emitLineTo(x0 + mx, y0 + my);
            emitLineTo(sx0 + mx, sy0 + my);

            ccw = isCCW(x0, y0, sx0, sy0, sx1, sy1);
            /*
             * Draw final join on the outside
             */
            if (!ccw) {
                if (joinStyle == JOIN_MITER) {
                    drawMiter(x0, y0, sx0, sy0, sx1, sy1,
                              mx, my, mx0, my0, false);
                }
                else if (joinStyle == JOIN_ROUND) {
                    drawRoundJoin(sx0, sy0, mx, my, mx0, my0, 0, false, false,
                                  ROUND_JOIN_THRESHOLD);
                }
            }

            emitLineTo(sx0 + mx0, sy0 + my0);
            emitLineTo(sx0 - mx0, sy0 - my0);  // same as reverse[0], reverse[1]
            /*
             * Draw final join on the inside
             */
            if (ccw) {
                if (joinStyle == JOIN_MITER) {
                    drawMiter(x0, y0, sx0, sy0, sx1, sy1,
                              -mx, -my, -mx0, -my0, false);
                }
                else if (joinStyle == JOIN_ROUND) {
                    drawRoundJoin(sx0, sy0, -mx, -my, -mx0, -my0, 0,
                                  true, false,
                                  ROUND_JOIN_THRESHOLD);
                }
            }

            emitLineTo(sx0 - mx, sy0 - my);
            emitLineTo(x0 - mx, y0 - my);
            for (int i = rindex - 2; i >= 0; i -= 2) {
                emitLineTo(reverse[i], reverse[i + 1]);
            }

            this.joinSegment = false;
            this.prev = CLOSE;
            emitClose();
        }
    }

    public void end() {

        if (lineToOrigin) {
            /*
             * not closing the path, do the previous lineTo
             */
            lineToImpl(sx0, sy0, joinToOrigin);
            lineToOrigin = false;
        }

        if (prev == LINE_TO) {
            finish();
        }

        output.end();
        this.joinSegment = false;
        this.prev = MOVE_TO;
    }
    /*
     *
     */
    private void lineToImpl(double x1, double y1, boolean joinSegment) {
        computeOffset(x0, y0, x1, y1, offset);
        double mx = offset[0];
        double my = offset[1];

        if (!started) {
            emitMoveTo(x0 + mx, y0 + my);
            this.sx1 = x1;
            this.sy1 = y1;
            this.mx0 = mx;
            this.my0 = my;
            started = true;
        }
        else {
            boolean ccw = isCCW(px0, py0, x0, y0, x1, y1);
            if (joinSegment) {
                if (joinStyle == JOIN_MITER) {
                    drawMiter(px0, py0, x0, y0, x1, y1, omx, omy, mx, my,
                              ccw);
                }
                else if (joinStyle == JOIN_ROUND) {
                    drawRoundJoin(x0, y0, 
                                  omx, omy,
                                  mx, my, 0, false, ccw,
                                  ROUND_JOIN_THRESHOLD);
                }
            }
            else {
                /*
                 * Draw internal joins as round
                 */
                drawRoundJoin(x0, y0, 
                              omx, omy,
                              mx, my, 0, false, ccw,
                              ROUND_JOIN_INTERNAL_THRESHOLD);
            }

            emitLineTo(x0, y0, !ccw);
        }

        emitLineTo(x0 + mx, y0 + my, false);
        emitLineTo(x1 + mx, y1 + my, false);

        emitLineTo(x0 - mx, y0 - my, true);
        emitLineTo(x1 - mx, y1 - my, true);

        lx0 = x1 + mx; ly0 = y1 + my;
        lx0p = x1 - mx; ly0p = y1 - my;
        lx1 = x1; ly1 = y1;

        this.omx = mx;
        this.omy = my;
        this.px0 = x0;
        this.py0 = y0;
        this.x0 = x1;
        this.y0 = y1;
        this.prev = LINE_TO;
    }
    private void computeOffset(double x0, double y0, double x1, double y1, double[] m) {
        double lx = x1 - x0;
        double ly = y1 - y0;

        double dx, dy;
        if (m00 > 0 && m00 == m11 && m01 == 0 & m10 == 0) {
            double ilen = Hypot(lx, ly);
            if (ilen == 0)
                dx = dy = 0;
            else {
                dx = ( (ly*scaledLineWidth2)/ilen);
                dy = (-(lx*scaledLineWidth2)/ilen);
            }
        }
        else {
            double dlx = x1 - x0;
            double dly = y1 - y0;

            double sdet = (determinant > 0) ? 1 : -1;
            double a = dly*m00 - dlx*m10;
            double b = dly*m01 - dlx*m11;
            double dh = Hypot(a, b);
            double div = sdet*lineWidth2/(dh);
            double ddx = dly*m00_2_m01_2 - dlx*m00_m10_m01_m11;
            double ddy = dly*m00_m10_m01_m11 - dlx*m10_2_m11_2;
            dx = (ddx*div);
            dy = (ddy*div);
        }

        m[0] = dx;
        m[1] = dy;
    }
    private void ensureCapacity(int newrindex) {
        if (reverse.length < newrindex) {
            double[] tmp = new double[Math.max(newrindex, 6*reverse.length/5)];
            System.arraycopy(reverse, 0, tmp, 0, rindex);
            this.reverse = tmp;
        }
    }
    private static boolean isCCW(double x0, double y0,
                          double x1, double y1,
                          double x2, double y2)
    { 
        double dx0 = x1 - x0;
        double dy0 = y1 - y0;
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        return dx0*dy1 < dy0*dx1;
    }
    private boolean side(double x, double y, double x0, double y0, double x1, double y1) {

        return (0.0 < (y0 - y1)*x + (x1 - x0)*y + (x0*y1 - x1*y0));
    }
    private int computeRoundJoin(double cx, double cy,
                                 double xa, double ya,
                                 double xb, double yb,
                                 int side,
                                 boolean flip,
                                 double[] join)
    {
        double px, py;
        int ncoords = 0;

        boolean centerSide;
        switch(side){
        case 0:
            centerSide = side(cx, cy, xa, ya, xb, yb);
            break;
        case 1:
            centerSide = true;
            break;
        default:
            centerSide = false;
            break;
        }

        for (int i = 0; i < numPenSegments; i++) {
            px = cx + pen_dx[i];
            py = cy + pen_dy[i];

            boolean penSide = side(px, py, xa, ya, xb, yb);

            if (penSide != centerSide)
                penIncluded[i] = true;
            else
                penIncluded[i] = false;
        }

        int start = -1, end = -1;

        for (int i = 0; i < numPenSegments; i++) {

            if (penIncluded[i]){

                if (!penIncluded[(i + numPenSegments - 1) % numPenSegments])

                    start = i;


                if (!penIncluded[(i + 1) % numPenSegments])

                    end = i;
            }
        }

        if (end < start) {
            end += numPenSegments;
        }

        if (start != -1 && end != -1) {

            double dxa = cx + pen_dx[start] - xa;
            double dya = cy + pen_dy[start] - ya;
            double dxb = cx + pen_dx[start] - xb;
            double dyb = cy + pen_dy[start] - yb;

            boolean rev = (dxa*dxa + dya*dya > dxb*dxb + dyb*dyb);

            int i = rev ? end : start;

            int incr = rev ? -1 : 1;

            while (true) {

                int idx = i % numPenSegments;

                px = cx + pen_dx[idx];
                py = cy + pen_dy[idx];

                join[ncoords++] = px;
                join[ncoords++] = py;

                if (i == (rev ? start : end))
                    break;
                else
                    i += incr;
            }
        }

        return ncoords/2;
    }
    private void drawRoundJoin(double x, double y,
                               double omx, double omy, double mx, double my,
                               int side,
                               boolean flip,
                               boolean rev,
                               double threshold)
    {
        if ((omx == 0 && omy == 0) || (mx == 0 && my == 0))
            return;
        else {

            double domx = omx - mx;
            double domy = omy - my;

            if ((domx*domx + domy*domy) < threshold)
                return;
            else {

                if (rev) {
                    omx = -omx;
                    omy = -omy;
                    mx = -mx;
                    my = -my;
                }

                double bx0 = x + omx;
                double by0 = y + omy;
                double bx1 = x + mx;
                double by1 = y + my;

                int npoints = computeRoundJoin(x, y,
                                               bx0, by0, bx1, by1, side, flip,
                                               join);

                for (int i = 0; i < npoints; i++) {

                    this.emitLineTo(join[2*i], join[2*i + 1], rev);
                }
            }
        }
    }
    /*
    * Return the intersection point of the lines (x0, y0) -> (x1, y1)
    * and (x0p, y0p) -> (x1p, y1p) in m[0] and m[1]
    */
    private static void computeMiter(double x0, double y0, double x1, double y1,
                                     double x0p, double y0p, double x1p, double y1p,
                                     double[] m)
    {
        double x10 = x1 - x0;
        double y10 = y1 - y0;
        double x10p = x1p - x0p;
        double y10p = y1p - y0p;

        double den = (x10*y10p - x10p*y10);

        if (den == 0) {
            m[0] = x0;
            m[1] = y0;
        }
        else {
            double t = (x1p*(y0 - y0p) - x0*y10p + x0p*(y1p - y0));
            m[0] = (x0 + (t*x10)/den);
            m[1] = (y0 + (t*y10)/den);
        }
    }

    private void drawMiter(double px0, double py0,
                           double x0, double y0, 
                           double x1, double y1,
                           double omx, double omy, double mx, double my,
                           boolean rev)
    {
        if (mx == omx && my == omy)
            return;

        else if (px0 == x0 && py0 == y0)
            return;

        else if (x0 == x1 && y0 == y1)
            return;

        else {

            if (rev) {
                omx = -omx;
                omy = -omy;
                mx = -mx;
                my = -my;
            }

            computeMiter(px0 + omx, py0 + omy, x0 + omx, y0 + omy,
                         x0 + mx, y0 + my, x1 + mx, y1 + my,
                         miter);
            /*
             * Compute miter length in untransformed coordinates
             */
            double dx = miter[0] - x0;
            double dy = miter[1] - y0;
            double a = (dy*m00 - dx*m10);
            double b = (dy*m01 - dx*m11);
            double lenSq = a*a + b*b;

            if (lenSq < miterLimitSq)

                this.emitLineTo(miter[0], miter[1], rev);
        }
    }
    private double lineLength(double ldx, double ldy) {

        double la = (ldy*m00 - ldx*m10)/this.determinant;

        double lb = (ldy*m01 - ldx*m11)/this.determinant;

        return Hypot(la, lb);
    }
    private void finish() {

        switch (capStyle){
        case CAP_ROUND:

            drawRoundJoin(x0, y0,
                          omx, omy, -omx, -omy, 1, false, false,
                          ROUND_JOIN_THRESHOLD);
            break;
        case CAP_SQUARE: {

            double ldx = (px0 - x0);
            double ldy = (py0 - y0);
            double llen = lineLength(ldx, ldy);
            double s = lineWidth2/llen;

            double capx = x0 - (ldx*s);
            double capy = y0 - (ldy*s);

            emitLineTo(capx + omx, capy + omy);
            emitLineTo(capx - omx, capy - omy);
        }
            break;
        }

        for (int i = rindex - 2; i >= 0; i -= 2) {
            emitLineTo(reverse[i], reverse[i + 1]);
        }

        switch (capStyle){
        case CAP_ROUND:

            drawRoundJoin(sx0, sy0,
                          -mx0, -my0, mx0, my0, 1, false, false,
                          ROUND_JOIN_THRESHOLD);

            break;
        case CAP_SQUARE: {

            double ldx = (sx1 - sx0);
            double ldy = (sy1 - sy0);
            double llen = lineLength(ldx, ldy);
            double s = lineWidth2/llen;

            double capx = sx0 - (ldx*s);
            double capy = sy0 - (ldy*s);

            emitLineTo(capx - mx0, capy - my0);
            emitLineTo(capx + mx0, capy + my0);
        }
            break;
        }

        this.emitClose();
        this.joinSegment = false;
    }
    private void emitMoveTo(double x0, double y0) {

        output.moveTo(x0, y0);
    }
    private void emitLineTo(double x1, double y1) {

        output.lineTo(x1, y1);
    }
    private void emitLineTo(double x1, double y1, boolean rev) {
        if (rev) {
            ensureCapacity(rindex + 2);
            reverse[rindex++] = x1;
            reverse[rindex++] = y1;
        }
        else {
            emitLineTo(x1, y1);
        }
    }
    private void emitClose() {

        output.close();
    }
}
