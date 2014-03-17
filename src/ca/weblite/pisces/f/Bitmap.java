/*
 * Pisces User
 * Copyright (C) 2009 John Pritchard
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
package ca.weblite.pisces.f;

import ca.weblite.pisces.Color;
import ca.weblite.pisces.Font;
import ca.weblite.pisces.Graphics;
import ca.weblite.pisces.d.PathSink;

import java.io.IOException;
import java.io.InputStream;

/**
 * Row ordered and padded bitmap.
 * 
 * Example: width 12
 *
 *    Row order pixel offsets with padding
 * <pre>
 *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 *    | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 || 8 | 9 | 10| 11| p | p | p | p |
 *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 * </pre>
 *
 *    Row shift keys
 * <pre>
 *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 *    | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 || 7 | 6 | 5 | 4 | p | p | p | p |
 *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
 * </pre>
 */
public class Bitmap
    extends Object
    implements Font.Glyph,
               ca.weblite.pisces.d.Surface
{

    protected byte[] bits;

    protected char id;
    protected int width, height;
    /*
     * Number of bytes per row
     */
    protected int stride;
    /*
     * Number of unused bits per row
     */
    protected int pad;
    /*
     * Number of used bits in padded byte
     */
    protected int fill;

    private Color color;


    protected Bitmap(){
        super();
    }


    public char getId(){
        return this.id;
    }
    public int getWidth(){
        return this.width;
    }
    public int getHeight(){
        return this.height;
    }
    public Font.Glyph blit(Graphics g, int x, int y, float op){

        this.color = g.getColor();

        g.blit(this,x,y,op);

        return this;
    }
    public Font.Glyph draw(Graphics g, int x, int y, float op){
        throw new RuntimeException("Draw not supported");
    }
    public void getRGB(int[] dstRGB, int dstOffset, int dstScanLength, 
                       int x, int y, int width, int height)
    {

        int dstX = 0;
        int dstY = 0;
        
        if (x < 0) {
            dstX -= x;
            width += x;
            x = 0;
        }

        if (y < 0) {
            dstY -= y;
            height += y;
            y = 0;
        }

        if ((x + width) > this.width) {
            width = this.width - x;
        }

        if ((y + height) > this.height) {
            height = this.height - y;
        }

        if ((width > 0) && (height > 0)) {

            dstOffset += dstY * dstScanLength + dstX;    

            int dstScanRest = dstScanLength - width;

            int srcOffset = y * this.width + x;
            int srcScanLength = this.width;

            Color color = this.color;

            for (; height > 0; --height) {

                for (int w = width; w > 0; --w) {

                    if (this.bit(srcOffset++))
                        dstRGB[dstOffset++] = color.argb;
                    else
                        dstRGB[dstOffset++] = Color.Transparent.White.argb;
                }
                dstOffset += dstScanRest;
            }

        }
    }

    public void setRGB(int[] argb, int offset, int scanLength, 
                       int x, int y, int width, int height)
    {
        throw new RuntimeException("setRGB not supported");
    }
    /**
     * @param ofs Bitmap offset index
     * @return Bitmap pixel value
     */
    public boolean bit(final int ofs){
        /* 
         * Example, width 12
         *
         *    Row order
         *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         *    | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 || 8 | 9 | 10| 11| p | p | p | p |
         *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         *
         *    Row shift keys (var 'ib')
         *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         *    | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 || 7 | 6 | 5 | 4 | p | p | p | p |
         *    +---+---+---+---+---+---+---+---++---+---+---+---+---+---+---+---+
         */

        final int row = (ofs/width);            // row index
        final int col = (ofs - (row*width));    // col index

        int iB = (row*stride);
        int ib = (col);

        while (7 < ib){
            iB += 1;
            ib -= 8;
        }
        ib = (7 - ib);

        return (1 == ((this.bits[iB]>>ib) & 0x1));
    }
    /**
     * Subclass method to fill bitmap bytes
     */
    protected void read(InputStream in, int many)
        throws IOException
    {
        if (null == this.bits || many != this.bits.length)
            this.bits = new byte[many];
        /*
         */
        {
            int ofs = 0, read;

            while (0 < (read = in.read(this.bits,ofs,many))){
                ofs += read;
                many -= read;
            }
        }
    }
    /**
     * Subclass method to define bitmap properties
     */
    protected void init(int width, int height){
        this.width = width;
        this.height = height;
        this.stride = ((width+7)>>3);
        this.pad = ((this.stride<<3)-this.width);
        int f = (8 - this.pad);
        if (0 == f)
            this.fill = 8;
        else
            this.fill = f;

        if (null == this.bits)
            this.bits = new byte[this.height*this.stride];
    }

    public Font.Glyph draw(PathSink sink, int x, int y, float op) {
        throw new RuntimeException("draw() not supported in bitmaps");
    }

}
