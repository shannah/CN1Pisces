/*
 * Pisces User
 * Copyright (C) 2009 John Pritchard
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

import com.codename1.system.NativeLookup;
import com.codename1.ui.Display;
import pisces.f.NativeFontLoader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
//import java.net.URL;

/**
 * Vector fonts must be drawn, and will throw an
 * UnsupportedOperationException on blitting.
 * 
 * Bitmap fonts must be blitted, and will throw an
 * UnsupportedOperationException on drawing.
 * 
 * @see pisces.f.Psf2
 */
public class Font
    extends Object
    implements Iterable<Font.Glyph>
{
    /**
     * Vector or Bitmap
     */
    public enum Kind {

        Draw, Blit
    }

    /**
     * 
     */
    public interface Glyph {

        /**
         * The implementor defines a public constructor for no
         * arguments.  An instance is created with this constructor,
         * and then initialized via the read method.
         */
        public interface Collection
            extends java.lang.Iterable<Glyph>
        {
            public Font.Kind getKind();

            public Glyph getGlyph(char id);

            public int getMaxWidth();

            public int getMaxHeight();

            public void read(InputStream in) throws IOException;
        }

        public char getId();

        public int getWidth();

        public int getHeight();
        /**
         * Blit the glyph in its ideal box as defined for all glyphs
         * in the collection.  The argument coordinates locate the
         * glyph box at its top left location.  
         * 
         * Glyph coordinates are not relative to a font baseline.
         * 
         * Bitmap fonts are typically blitted.  
         * 
         * Vector fonts will throw an UnsupportedOperationException on
         * blitting.
         * 
         * In future the rendering engine may expose methods to draw a
         * bitmap font with transformations and antialiasing.
         */
        public Glyph blit(Graphics g, int x, int y, float op);
        /**
         * Draw the glyph.
         * 
         * Stroke or vector fonts must be drawn.  
         * 
         * Bitmap fonts may throw an UnsupportedOperationException on
         * drawing.
         */
        public Glyph draw(Graphics g, int x, int y, float op);
    }

    /**
     * Map from font (file) name extension to implementation class.
     */
    public enum Type {

        PSFU(pisces.f.Psf2.class);


        public final Class<Font.Glyph.Collection> jclass;


        Type(Class jclass){
            this.jclass = jclass;
        }

        public Font.Glyph.Collection newInstance(){
            //throw new RuntimeException("newInstance not supported yet");
           try {
                return (Font.Glyph.Collection)this.jclass.newInstance();
            }
           catch (InstantiationException exc){
                throw new RuntimeException(exc.getMessage());
            }
            catch (IllegalAccessException exc){
              throw new RuntimeException(exc.getMessage());
            }
        }


        public final static Font.Type Of(String name){
            int idx = name.lastIndexOf('.');
            if (-1 == idx)
                throw new IllegalArgumentException("Font file name missing extension");
            else
                return Type.valueOf(name.substring(idx+1).toUpperCase());
        }
        public final static Font.Glyph.Collection Create(String name)
            throws IOException
        {
            Font.Type type = Type.Of(name);
            Font.Glyph.Collection collection = type.newInstance();
            NativeFontLoader loader = (NativeFontLoader)NativeLookup.create(NativeFontLoader.class);
            if ( loader.isSupported() ){
                byte[] data = loader.getFontData(name);
                InputStream is = new ByteArrayInputStream(data);
                try {
                    collection.read(is);
                } finally {
                    is.close();
                }
                return collection;
                
            }
            
            /*
             */
            String url = null;
            //if ('/' == name.charAt(0))
            //    url = Font.class.getResource(name);
            //else
            //    url = Font.class.getResource("/"+name);
            ///*
            // */
            if (null == url)
                throw new IllegalArgumentException("Font file not found "+name);
            else {
                InputStream in = Display.getInstance().getResourceAsStream(Font.class, url);
                try {
                    collection.read(in);
                }
                finally {
                    in.close();
                }
                return collection;
            }
        }
    }


    private String name;

    private Font.Glyph.Collection collection;


    /**
     * @param name Font file name, for example "sun12x22.psfu".
     */
    public Font(String name)
        throws IOException
    {
        super();
        if (null != name && 0 < name.length()){
            this.name = name;
            this.collection = Font.Type.Create(name);
        }
        else
            throw new IllegalArgumentException();
    }


    public Kind getKind(){
        return this.collection.getKind();
    }
    public Glyph getGlyph(char id){
        return this.collection.getGlyph(id);
    }
    /**
     * Bitmap font
     */
    public Font blit(Graphics g, String string, int x, int y, float op){
        if (null != string){
            char[] cary = string.toCharArray();
            int clen = cary.length;
            if (0 < clen){
                char ch;
                int px = x;
                int py = y;
                Glyph glyph;
                for (int cc = 0; cc < clen; cc++){
                    ch = cary[cc];
                    switch (ch){
                    case 0x20:
                        glyph = this.collection.getGlyph(ch);
                        if (null != glyph)
                            px += glyph.getWidth();
                        else
                            px += this.collection.getMaxWidth();
                        break;
                    case 0x0A:
                        px = x;
                        py += this.collection.getMaxHeight();
                        break;
                    case 0x0D:
                        px = x;
                        break;
                    default:
                        glyph = this.collection.getGlyph(ch);

                        if (null != glyph){
                            glyph.blit(g,px,py,op);

                            px += glyph.getWidth();
                        }
                        else
                            px += this.collection.getMaxWidth();
                        break;
                    }
                }
            }
        }
        return this;
    }
    /**
     * Vector font
     */
    public Font draw(Graphics g, String string, int x, int y, float op){
        if (null != string){
            char[] cary = string.toCharArray();
            int clen = cary.length;
            if (0 < clen){
                char ch;
                int px = x;
                int py = y;
                Glyph glyph;
                for (int cc = 0; cc < clen; cc++){
                    ch = cary[cc];
                    switch (ch){
                    case 0x20:
                        glyph = this.collection.getGlyph(ch);
                        if (null != glyph)
                            px += glyph.getWidth();
                        else
                            px += this.collection.getMaxWidth();
                        break;
                    case 0x0A:
                        px = x;
                        py += this.collection.getMaxHeight();
                        break;
                    case 0x0D:
                        px = x;
                        break;
                    default:
                        glyph = this.collection.getGlyph(ch);

                        if (null != glyph){
                            glyph.draw(g,px,py,op);

                            px += glyph.getWidth();
                        }
                        else
                            px += this.collection.getMaxWidth();
                        break;
                    }
                }
            }
        }
        return this;
    }
    public int getMaxWidth(){
        return this.collection.getMaxWidth();
    }
    public int getMaxHeight(){
        return this.collection.getMaxHeight();
    }
    public java.util.Iterator<Font.Glyph> iterator(){
        return this.collection.iterator();
    }
}
