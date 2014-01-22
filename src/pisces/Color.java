/*
 * Pisces User
 * Copyright (C) 2009 John Pritchard
 * Codename One Modifications Copyright (C) 2013 Steve Hannah
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.  The copyright
 * holders designate particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this
 * code.
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
 * 
 * 
 * @see Graphics
 */
public class Color
    extends Object
    implements Cloneable
{

    public final static Color White     = new Color(255, 255, 255);
    public final static Color LightGray = new Color(192, 192, 192);
    public final static Color Gray      = new Color(128, 128, 128);
    public final static Color DarkGray  = new Color( 64,  64,  64);
    public final static Color Black     = new Color(  0,   0,   0);
    public final static Color Red       = new Color(255,   0,   0);
    public final static Color Pink      = new Color(255, 175, 175);
    public final static Color Orange    = new Color(255, 200,   0);
    public final static Color Yellow    = new Color(255, 255,   0);
    public final static Color Green     = new Color(  0, 255,   0);
    public final static Color Magenta   = new Color(255,   0, 255);
    public final static Color Cyan      = new Color(  0, 255, 255);
    public final static Color Blue      = new Color(  0,   0, 255);


    public static class Transparent
        extends Color
    {

        public final static Transparent White     = new Transparent(255, 255, 255);
        public final static Transparent LightGray = new Transparent(192, 192, 192);
        public final static Transparent Gray      = new Transparent(128, 128, 128);
        public final static Transparent DarkGray  = new Transparent( 64,  64,  64);
        public final static Transparent Black     = new Transparent(  0,   0,   0);
        public final static Transparent Red       = new Transparent(255,   0,   0);
        public final static Transparent Pink      = new Transparent(255, 175, 175);
        public final static Transparent Orange    = new Transparent(255, 200,   0);
        public final static Transparent Yellow    = new Transparent(255, 255,   0);
        public final static Transparent Green     = new Transparent(  0, 255,   0);
        public final static Transparent Magenta   = new Transparent(255,   0, 255);
        public final static Transparent Cyan      = new Transparent(  0, 255, 255);
        public final static Transparent Blue      = new Transparent(  0,   0, 255);


        public Transparent(int r, int g, int b){
            super(0,r,g,b);
        }
    }



    public final int alpha, red, green, blue;

    public final int argb;


    public Color(int argb){
        super();

        int a = ((argb >>> 24) & 0xff);
        if (0 == a)
            this.alpha = 255;
        else
            this.alpha = a;

        this.red = (argb >>> 16) & 0xff;
        this.green = (argb >>> 8) & 0xff;
        this.blue = (argb & 0xff);

        this.argb = ToARGB(this);
    }
    public Color(int r, int g, int b){
        this(0xff,r,g,b);
    }
    public Color(int a, int r, int g, int b){
        super();
        this.alpha = (a & 0xff);
        this.red   = (r & 0xff);
        this.green = (g & 0xff);
        this.blue  = (b & 0xff);

        this.argb = ToARGB(this);
    }


    public Color clone(){
        //try {
        //    return (Color)super.clone();
        //}
        //catch (CloneNotSupportedException err){
            throw new RuntimeException();
        //}
    }
    public int hashCode(){
        return this.argb;
    }
    public boolean equals(Object that){
        if (this == that)
            return true;
        else if (null == that)
            return false;
        else if (that instanceof Color)
            return (this.hashCode() == that.hashCode());
        else
            return false;
    }



    private final static int ToARGB(Color c){
        return ((c.alpha<<24) | 
                (c.red<<16)|
                (c.green<<8)|
                (c.blue & 0xff));
    }
    
    public String toString(){
        return "{Red:"+this.red+" Green:"+this.green+" Blue:"+this.blue+" Alpha:"+this.alpha+"}";
    }

}
