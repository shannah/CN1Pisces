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
 * 
 */
public class Transformer
    extends PathSink
{

    PathSink output;
    double m00, m01, m02;
    double m10, m11, m12;

    boolean scaleAndTranslate;


    public Transformer(){
        super();
    }
    public Transformer(PathSink output,
                       Matrix transform)
    {
        if (output instanceof Transformer) {
            /*
             * Collapse adjacent transforms
             */
            Transformer t = (Transformer)output;
            this.output = t.output;
            this.m00 = (transform.m00*t.m00 + transform.m10*t.m01);
            this.m01 = (transform.m01*t.m00 + transform.m11*t.m01);
            this.m10 = (transform.m00*t.m10 + transform.m10*t.m11);
            this.m11 = (transform.m01*t.m10 + transform.m11*t.m11);
            this.m02 =  transform.m02*t.m00 + transform.m12*t.m01 + t.m02;
            this.m12 =  transform.m02*t.m10 + transform.m12*t.m11 + t.m12;
        }
        else {
            this.output = output;
            setTransform(transform);
        }
        classify();
    }


    public void dispose(){
        this.output = null;
    }
    public void setTransform(Matrix transform) {
        this.m00 = transform.m00;
        this.m01 = transform.m01;
        this.m02 = transform.m02;
        this.m10 = transform.m10;
        this.m11 = transform.m11;
        this.m12 = transform.m12;

        classify();
    }

    private void classify() {
        if (m01 == 0 && m10 == 0) {
            scaleAndTranslate = true;
        } else {
            scaleAndTranslate = false;
        }
    }

    public void setOutput(PathSink output) {
        this.output = output;
    }

    public void moveTo(double x0, double y0) {
        double tx0, ty0;
        
        if (scaleAndTranslate) {
            tx0 = m00*x0 + m02;
            ty0 = m11*y0 + m12;
        } else {
            tx0 = m00*x0 + m01*y0 + m02;
            ty0 = m10*x0 + m11*y0 + m12;
        }

        output.moveTo(tx0, ty0);
    }

    public void lineJoin() {
        output.lineJoin();
    }

    public void lineTo(double x1, double y1) {
        double tx1, ty1;

        if (scaleAndTranslate) {
            tx1 = m00*x1 + m02;
            ty1 = m11*y1 + m12;
        }
        else {
            tx1 = m00*x1 + m01*y1 + m02;
            ty1 = m10*x1 + m11*y1 + m12;
        }

        output.lineTo(tx1, ty1);
    }

    public void quadTo(double x1, double y1, double x2, double y2) {
        double tx1, ty1, tx2, ty2;

        if (scaleAndTranslate) {
            tx1 = m00*x1 + m02;
            ty1 = m11*y1 + m12;
            tx2 = m00*x2 + m02;
            ty2 = m11*y2 + m12;
        }
        else {
            tx1 = m00*x1 + m01*y1 + m02;
            ty1 = m10*x1 + m11*y1 + m12;
            tx2 = m00*x2 + m01*y2 + m02;
            ty2 = m10*x2 + m11*y2 + m12;
        }

        output.quadTo(tx1, ty1, tx2, ty2);
    }

    public void cubicTo(double x1, double y1,
                        double x2, double y2,
                        double x3, double y3)
    {
        double tx1, ty1, tx2, ty2, tx3, ty3;

        if (scaleAndTranslate) {
            tx1 = m00*x1 + m02;
            ty1 = m11*y1 + m12;
            tx2 = m00*x2 + m02;
            ty2 = m11*y2 + m12;
            tx3 = m00*x3 + m02;
            ty3 = m11*y3 + m12;
        }
        else {
            tx1 = m00*x1 + m01*y1 + m02;
            ty1 = m10*x1 + m11*y1 + m12;
            tx2 = m00*x2 + m01*y2 + m02;
            ty2 = m10*x2 + m11*y2 + m12;
            tx3 = m00*x3 + m01*y3 + m02;
            ty3 = m10*x3 + m11*y3 + m12;
        }
        output.cubicTo(tx1, ty1,
                       tx2, ty2,
                       tx3, ty3);
    }
    
    public void close() {
        output.close();
    }
    public void end() {
        output.end();
    }
}
