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

public interface Surface
{
    /**
     * Constant indicating 8/8/8 RGB pixel data stored in an
     * <code>int</code> array.
     */
    public static final int TYPE_INT_RGB = 1;

    /**
     * Constant indicating 8/8/8/8 ARGB pixel data stored in an
     * <code>int</code> array.
     */
    public static final int TYPE_INT_ARGB = 2;

    /**
     * Constant indicating 8/8/8/8 ARGB alpha-premultiplied pixel data stored 
     * in a <code>int</code> array.
     */
    public static final int TYPE_INT_ARGB_PRE = 3;

    /**
     * Constant indicating 5/6/5 RGB pixel data stored in an
     * <code>short</code> array.
     */
    public static final int TYPE_USHORT_565_RGB = 8;

    /**
     * Constant indicating 8 bit grayscale pixel data stored in a
     * <code>byte</code> array.
     */
    public static final int TYPE_BYTE_GRAY = 10;


    public interface Sink
        extends Surface
    {
        public int getDataType();

        public Object getData();

        public interface Int
            extends Sink
        {
            public int[] getData();
        }

        public interface Short
            extends Sink
        {
            public short[] getData();
        }

        public interface Byte
            extends Sink
        {
            public byte[] getData();
        }

        public void blit(Surface ps, int srcX, int srcY, 
                         int dstX, int dstY, int width, int height, float opacity);
    
        public void blit(int[] argb, int offset, int scanLength, 
                         int x, int y, int width, int height, float opacity);
    }

    public int getWidth();
    
    public int getHeight();
    
    public void getRGB(int[] argb, int offset, int scanLength, 
            int x, int y, int width, int height);
    
    public void setRGB(int[] argb, int offset, int scanLength, 
            int x, int y, int width, int height);

}
