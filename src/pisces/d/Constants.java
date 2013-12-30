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
 * 
 */
public interface Constants {

    /**
     * Path commands
     */
    public static final byte COMMAND_MOVE_TO  = 0;
    public static final byte COMMAND_LINE_TO  = 1;
    public static final byte COMMAND_QUAD_TO  = 2;
    public static final byte COMMAND_CUBIC_TO = 3;
    public static final byte COMMAND_CLOSE    = 4;

    /**
     * Arc types
     */
    public static final int ARC_OPEN = 0;
    public static final int ARC_CHORD = 1;
    public static final int ARC_PIE = 2;

    /**
     * Winding rules
     */
    public static final int WIND_EVEN_ODD = 0;
    public static final int WIND_NON_ZERO = 1;

    /**
     * Stroke join style
     */
    public static final int JOIN_MITER = 0;
    public static final int JOIN_ROUND = 1;
    public static final int JOIN_BEVEL = 2;

    /**
     * Stroke end cap style
     */
    public static final int CAP_BUTT = 0;
    public static final int CAP_ROUND = 1;
    public static final int CAP_SQUARE = 2;

    /**
     * Common Math constants, epsilon for values 0.0 to 10.0.
     */
    public final static double EPS = (1e-8);

    public final static double EPS_M2 = (EPS*2.0);
    public final static double EPS_D2 = (EPS/2.0);
    public final static double EPS_1D2 = (1.0 - EPS_D2);

    public final static double Zero = (0.0+EPS);

    public final static double PI = Math.PI;
    public final static double PI_D2 = (PI / 2.0);
    public final static double PI_M2 = (PI * 2.0);
    public final static double PI_D3 = (PI / 3.0);
    /**
     * Multiply Degrees for Radians
     */
    public final static double Radians = (PI / 180.0);
    /**
     * 1.414
     */
    public static final double SQRT_TWO = Math.sqrt(2.0);
}
