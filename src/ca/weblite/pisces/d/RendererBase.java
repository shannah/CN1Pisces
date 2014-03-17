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
package ca.weblite.pisces.d;

/**
 * 
 */
public abstract class RendererBase
    extends LineSink
{

    public static final int DEFAULT_SUBPIXEL_LG_POSITIONS_X = 3;
    public static final int DEFAULT_SUBPIXEL_LG_POSITIONS_Y = 3;

    /**
     * @defgroup CompositingRules Compositing rules supported by PISCES 
     * When drawing two objects to one pixel area, there are several possibilities
     * how composite color is made of source and destination contributions.
     * Objects can overlap pixel fully and/or partialy. One object could be above
     * the second one and they both can be partialy or fully transparent (alpha).  
     * The way, we count composite color and alpha from theirs contributions is
     * called compositing rule (Porter-Duff).
     * @def COMPOSITE_CLEAR
     * @ingroup CompositingRules 
     * Compositing rule COMPOSITE_CLEAR. This rule applied to destination pixel sets 
     * its color to 0x00000000 - transparent black - regardless to source color.
     * @see setCompositeRule(int), setComposite(int, float) 
     * @def COMPOSITE_SRC
     * @ingroup CompositingRules  
     * Compositing rule COMPOSITE_SRC. This rule applied to destination pixel sets 
     * its color to source color - regardless to previous color of destination 
     * pixel.
     * @see setCompositeRule(int), setComposite(int, float)  
     * @def COMPOSITE_SRC_OVER
     * @ingroup CompositingRules 
     * Compositing rule COMPOSITE_SRC_OVER. This rule is kind of intuitive. When we
     * look through transparent green glass bottle at some object, we can see 
     * mixture of glass and objects colors. Composite color is alpha-weigth average 
     * of source and destination.
     * @see setCompositeRule(int), Pisces.setComposite(int, float)    
     */
    public static final int COMPOSITE_CLEAR    = 0;
    public static final int COMPOSITE_SRC      = 1;
    public static final int COMPOSITE_SRC_OVER = 2;
    
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


    protected final int imageType;


    public RendererBase(int imageType) {
        this.imageType = imageType;
    }


    public int getImageType() {
        return imageType;
    }
    public abstract void setAntialiasing(int subpixelLgPositionsX,
                                         int subpixelLgPositionsY);

    public abstract int getSubpixelLgPositionsX();

    public abstract int getSubpixelLgPositionsY();

    public abstract void setColor(int red, int green, int blue, int alpha);

    public abstract void setPaint(Paint paint);

    public abstract void beginRendering(double boundsX, double boundsY,
                                        double boundsWidth, double boundsHeight,
                                        int windingRule);

    public abstract void moveTo(double x0, double y0);

    public void lineJoin() {
    }

    public abstract void lineTo(double x1, double y1);

    public abstract void close();

    public void end() {
    }

    public abstract void endRendering();

    public abstract void getBoundingBox(double[] bbox);

    public abstract void setCache(PiscesCache cache);

    public abstract void renderFromCache(PiscesCache cache);
    
    public abstract void clearRect(double x, double y, double w, double h);

    public abstract void dispose();

    public RendererBase clone(){
        return (RendererBase)super.clone();
    }
}
