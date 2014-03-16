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

import ca.weblite.pisces.Font;

/**
 * 
 */
public abstract class Collection
    extends Object
    implements Font.Glyph.Collection
{

    private Font.Glyph[] set;

    private int width, height;


    public Collection(){
        super();
    }


    public Font.Glyph getGlyph(char id){
        int len = (null != this.set)?(this.set.length):(0);
        int idx = id;
        if (idx < len)
            return this.set[idx];
        else
            throw new IllegalArgumentException("Glyph not found: "+id);
    }
    public int getMaxWidth(){
        return this.width;
    }
    public int getMaxHeight(){
        return this.height;
    }
    protected void add(Font.Glyph glyph){
        int idx = glyph.getId();
        int cap = (idx+1);
        int len = (null != this.set)?(this.set.length):(0);
        if (cap > len){
            Font.Glyph[] set = new Font.Glyph[cap];
            if (0 != len)
                System.arraycopy(this.set,0,set,0,len);
            this.set = set;
        }
        /*
         */
        this.set[idx] = glyph;
        this.width = Math.max(this.width,glyph.getWidth());
        this.height = Math.max(this.height,glyph.getHeight());
    }
    public java.util.Iterator<Font.Glyph> iterator(){
        return new Iterator(this.set);
    }


    /**
     * Sparse list iterator
     */
    public static class Iterator
        extends Object
        implements java.util.Iterator<Font.Glyph>
    {

        private final Font.Glyph[] set;
        private final int length;
        private int index;


        public Iterator(Font.Glyph[] set){
            super();
            this.set = set;
            this.length = (null == set)?(0):(set.length);
        }

        public boolean hasNext(){
            /*
             * Sparse list iterator
             */
            while (this.index < this.length && 
                   null == this.set[this.index])
            {
                this.index++;
            }
            return (this.index < this.length);
        }
        public Font.Glyph next(){
            if (this.index < this.length)
                return this.set[index++];
            else
                throw new java.util.NoSuchElementException();
        }
        public void remove(){
            throw new RuntimeException("remove() not supported");
        }
    }
}
