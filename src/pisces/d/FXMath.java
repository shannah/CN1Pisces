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
 * S15.16 fixed point
 * 
 * @see Renderer
 */
public abstract class FXMath
    extends FPMath
{

    public static final int FRACTION_BITS = 16;

    public static final int ONE = (1 << FRACTION_BITS);

    public static final int MAX_VALUE = (1 << 31) - 1;    
    public static final int MIN_VALUE = -(1 << 31);

    public static final double MAX_DOUBLE_VALUE = 32767.999992370602;
    public static final double MIN_DOUBLE_VALUE = -32768.0;

    public static final int ToFixed(double n) {
        if (n > MAX_DOUBLE_VALUE)
            return MAX_VALUE;
        else if (n < MIN_DOUBLE_VALUE)
            return MIN_VALUE;
        else 
            return (int)java.lang.Math.round(n * ONE);
    }
    public final static int ToFixed(int n){
        return (n << FRACTION_BITS);
    }
    public static final double ToFloat(int f) {
        return (double)f / ONE;
    }

    public static int Clamp(int x, int min, int max) {
        if (x < min)
            return min;
        else if (x > max)
            return max;
        else
            return x;
    }

}
