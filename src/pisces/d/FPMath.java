/*
 * Copyright (C) 2010 John Pritchard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package pisces.d;

/**
 * Floating point
 * 
 * <h3>Epsilon scalar radii</h3>
 * 
 * Because floating point numeric values are not uniformly distributed
 * on the real number domain
 * 
 * <pre>
 * V = s * 1.f * 2**(e*-127)
 * </pre>
 * 
 * for sign 's', exponent 'e' and fraction 'f', rounding error checks
 * must be scaled to the range of the value under test.
 * 
 */
public abstract class FPMath
    extends Object
    implements Constants
{

    public static double Hypot(double x, double y) {

        return Math.sqrt(x*x + y*y);
    }
    public final static boolean EEQ(double a, double b){

        final double d = (a - b);

        if (Double.isNaN(d))

            return false;
        else
            return (Math.abs(d) <= E(a,b));
    }
    public final static boolean ELE(double a, double b){

        final double d = (a - b);

        if (Math.abs(d) <= E(a,b))
            return true;
        else if (0.0 > d)
            return true;
        else
            return false;
    }
    public final static boolean ELT(double a, double b){

        final double d = (a - b);

        if (Math.abs(d) <= E(a,b))
            return false;
        else if (0.0 > d)
            return true;
        else
            return false;
    }
    public final static boolean EGE(double a, double b){

        final double d = (a - b);

        if (Math.abs(d) <= E(a,b))
            return true;
        else if (0.0 < d)
            return true;
        else
            return false;
    }
    public final static boolean EGT(double a, double b){

        final double d = (a - b);

        if (Math.abs(d) <= E(a,b))
            return false;
        else if (0.0 < d)
            return true;
        else
            return false;
    }
    /**
     * Scale FP epsilon
     */
    public final static double E(double v){
        return (EPS * Math.max(1.0,Math.abs(v)));
    }
    public final static double E(double u, double v){
        return (EPS * Math.max(1.0,Math.max(Math.abs(u),Math.abs(v))));
    }
    /**
     * Epsilon-zero
     */
    public final static double Z(double v){
        if (EPS > Math.abs(v))
            return 0.0;
        else
            return v;
    }
    /**
     * Epsilon-(zero or one)
     */
    public final static double Z1(double v){

        double av = Math.abs(v);

        if (EPS > av)
            return 0.0;
        else {
            av -= EPS_1D2;

            if (0.0 < av && EPS_D2 > av){
                if (0.0 > v)
                    return -1.0;
                else
                    return 1.0;
            }
        }
        return v;
    }
}
