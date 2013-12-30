/*
 * Pisces User
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
package pisces;

/**
 * Closed polygon
 * @see Graphics
 * @see Path
 */
public class Polygon
    extends Path
{

    public static class Rectangle
        extends Polygon
    {

        public Rectangle(double x, double y, double w, double h){
            super(x,(x+w),y,(y+w));
        }
    }
    public static class Square 
        extends Polygon.Rectangle
    {
        public Square(double x, double y, double s){
            super(x,y,s,s);
        }
    }


    public Polygon(double x0, double x1, double y0, double y1){
        super();
        this.moveTo(x0,y0);
        this.lineTo(x0,y1);
        this.lineTo(x1,y1);
        this.lineTo(x1,y0);
        this.close();
    }
    public Polygon(int[] x, int[] y){
        super();
        if (null != x && null != y && x.length == y.length){

            final int len = x.length;

            for (int cc = 0; cc < len; cc++){
                if (0 == cc)
                    this.moveTo(x[0],y[0]);
                else
                    this.lineTo(x[cc],y[cc]);
            }
            this.close();
        }
        else
            throw new IllegalArgumentException();
    }
    public Polygon(double[] x, double[] y){
        super();
        if (null != x && null != y && x.length == y.length){

            final int len = x.length;

            for (int cc = 0; cc < len; cc++){
                if (0 == cc)
                    this.moveTo(x[0],y[0]);
                else
                    this.lineTo(x[cc],y[cc]);
            }
            this.close();
        }
        else
            throw new IllegalArgumentException();
    }

}
