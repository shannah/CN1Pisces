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

/**
 * The <code>Dasher</code> class takes a series of linear commands
 * (<code>moveTo</code>, <code>lineTo</code>, <code>close</code> and
 * <code>end</code>) and breaks them into smaller segments according to a
 * dash pattern array and a starting dash phase.
 *
 * <p> Issues: in J2Se, a zero length dash segment as drawn as a very
 * short dash, whereas Pisces does not draw anything.  The PostScript
 * semantics are unclear.
 *
 */
public class Dasher extends LineSink {

    LineSink output;
    double[] dash;
    double startPhase;
    int startIdx;

    int idx;
    double phase;

    double sx, sy;
    double x0, y0;

    double m00, m01;
    double m10, m11;

    Matrix transform;

    boolean symmetric;
    double ldet;

    boolean firstDashOn;
    boolean starting;
    double sx1, sy1;

    /**
     * Empty constructor.  <code>setOutput</code> and
     * <code>setParameters</code> must be called prior to calling any
     * other methods.
     */
    public Dasher() {
        super();
    }
    public Dasher(LineSink output,
                  double[] dash, double phase,
                  Matrix transform)
    {
        super();
        this.setOutput(output);
        this.setParameters(dash, phase, transform);
    }


    public void dispose(){
        this.output = null;
    }
    public void setOutput(LineSink output) {
        this.output = output;
    }
    public void setParameters(double[] dash, double phase,
                              Matrix transform)
    {
        if (phase < 0) {
            throw new IllegalArgumentException("phase < 0 !");
        }
        else {
            /*
             * Normalize so 0 <= phase < dash[0]
             */
            int idx = 0;
            double d;
            while (phase >= (d = dash[idx])) {
                phase -= d;
                idx = (idx + 1) % dash.length;
            }

            this.dash = new double[dash.length];
            System.arraycopy(dash,0,this.dash,0,dash.length);

            this.startPhase = this.phase = phase;
            this.startIdx = idx;

            this.transform = transform;

            this.m00 = transform.m00;
            this.m01 = transform.m01;
            this.m10 = transform.m10;
            this.m11 = transform.m11;
            this.ldet = Z1(m00*m11 - m01*m10);
            this.symmetric = (m00 == m11 && m10 == -m01);
        }
    }

    public void moveTo(double x0, double y0) {
        output.moveTo(x0, y0);
        this.idx = startIdx;
        this.phase = this.startPhase;
        this.sx = this.x0 = x0;
        this.sy = this.y0 = y0;
        this.starting = true;
    }

    public void lineJoin() {
        output.lineJoin();
    }

    private void goTo(double x1, double y1) {
        if ((idx % 2) == 0) {
            if (starting) {
                this.sx1 = x1;
                this.sy1 = y1;
                firstDashOn = true;
                starting = false;
            }
            output.lineTo(x1, y1);
        }
        else {
            if (starting) {
                firstDashOn = false;
                starting = false;
            }
            output.moveTo(x1, y1);
        }
        this.x0 = x1;
        this.y0 = y1;
    }

    public void lineTo(double x1, double y1) {
        while (true) {
            double d = dash[idx] - phase;
            double lx = x1 - x0;
            double ly = y1 - y0;
            /*
             * Compute segment length in the untransformed coordinate
             * system
             */
            double l;
            if (symmetric) {
                l = ((Hypot(lx, ly))/ldet);
            }
            else{
                double la = (ly*m00 - lx*m10)/ldet;
                double lb = (ly*m01 - lx*m11)/ldet;
                l = Hypot(la, lb);
            }
	    
            if (l < d) {
                goTo(x1, y1);
                /*
                 * Advance phase within current dash segment
                 */
                phase += l;
                return;
            }
            else {
                double t;
                double xsplit, ysplit;

                t = (d/l);
                xsplit = x0 + (t*(x1 - x0));
                ysplit = y0 + (t*(y1 - y0));

                goTo(xsplit, ysplit);
                /*
                 * Advance to next dash segment
                 */
                idx = (idx + 1) % dash.length;
                phase = 0;
            }
        }
    }

    public void close() {
        lineTo(sx, sy);
        if (firstDashOn) {
            output.lineTo(sx1, sy1);
        }
    }

    public void end() {
        output.end();
    }
}
