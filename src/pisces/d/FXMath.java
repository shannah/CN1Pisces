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
