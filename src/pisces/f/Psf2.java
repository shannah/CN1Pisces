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
package pisces.f;

import pisces.Color;
import pisces.Font;
import pisces.Graphics;

import java.io.InputStream;
import java.io.IOException;

/**
 * 
 */
public final class Psf2
    extends Collection
{

   
    public final static class Glyph
        extends Bitmap
    {

        public Glyph(InputStream in, int count, int w, int h) throws IOException {
            super();
            this.init(w,h);
            this.read(in,count);
        }


        protected final static Glyph[] Add(Glyph[] list, Glyph g){
            if (null == g)
                return list;
            else if (null == list)
                return new Glyph[]{g};
            else {
                int len = list.length;
                if (1 == len)
                    return new Glyph[]{list[0],g};
                else {
                    Glyph[] copier = new Glyph[len+1];
                    System.arraycopy(list,0,copier,0,len);
                    copier[len] = g;
                    return copier;
                }
            }
        }
    }


    public Psf2(){
        super();
    }


    public Font.Kind getKind(){
        return Font.Kind.Blit;
    }

    public void read(InputStream in) throws IOException {
        byte[] magic = new byte[4];
        in.read(magic);
        if (IsMagic(magic)){
            int version = ReadInt(in);
            int headersize = ReadInt(in);
            int flags = ReadInt(in);
            int length = ReadInt(in);
            int charsize = ReadInt(in);
            int h = ReadInt(in);
            int w = ReadInt(in);
            {
                int skip = (headersize-HZ);
                for (int cc = 0; cc < skip; cc++){
                    in.read();
                }
            }
            if (1 == (flags & UNITAB)){
                Glyph[] list = null;
                for (int cc = 0; cc < length; cc++){

                    list = Glyph.Add(list,new Glyph(in,charsize,w,h));
                }
                int idx = 0;
                {
                    int u;
                    byte[] ub = null;
                    char c, idl[];
                    unitab:
                    while (idx < length){

                        u = in.read();
                        if (-1 == u)
                            throw new java.io.EOFException();
                        else {
                            switch(u){
                            case START:
                                ub = null;
                                continue unitab;
                            case SEP:
                                Glyph g = list[idx++];
                                idl = Utf8.decode(ub);
                                if (null != idl){

                                    for (int cc = 0, cz = idl.length; cc < cz; cc++){
                                        g.id = idl[cc];
                                        this.add(g);
                                    }
                                }
                                ub = null;
                                continue unitab;
                            default:
                                ub = Add(ub, (byte)u);
                                break;
                            }
                        }
                    }
                }
                /*
                 */
                if (idx < length)
                    throw new IllegalArgumentException("File format error, not all glyphs described ("+idx+","+length+")");
                else
                    return;
            }
            else {
                throw new IllegalArgumentException("Unrecognized file format missing unicode table.");
            }
        }
        else
            throw new RuntimeException("Clone not supported");
            //throw new IllegalArgumentException("Unrecognized file format, magic {0x%x,0x%x,0x%x,0x%x}",(magic[0] & 0xff),(magic[1] & 0xff),(magic[2] & 0xff),(magic[3] & 0xff)));
    }


    private final static int HZ = (7<<2)+4;
    private final static int UNITAB = 0x1;
    private final static int SEP = 0xff;
    private final static int START = 0xfe;

    private static boolean IsMagic(byte[] magic){
        return (0x72 == (magic[0] & 0xff) &&
                0xb5 == (magic[1] & 0xff) &&
                0x4a == (magic[2] & 0xff) &&
                0x86 == (magic[3] & 0xff));
    }
    private final static int ReadInt(InputStream in) throws IOException {
        int le0 = in.read();
        if (-1 < le0){
            int le1 = in.read();
            if (-1 < le1){
                int le2 = in.read();
                if (-1 < le2){
                    int le3 = in.read();
                    if (-1 < le3){

                        return ((le3<<24)|
                                (le2<<16)|
                                (le1<<8)|
                                (le0<<0));
                    }
                }
            }
        }
        throw new java.io.EOFException();
    }
    private final static byte[] Add(byte[] list, byte value){
        if (null == list)
            return new byte[]{value};
        else {
            int len = list.length;
            if (1 == len)
                return new byte[]{list[0],value};
            else {
                byte[] copier = new byte[len+1];
                System.arraycopy(list,0,copier,0,len);
                copier[len] = value;
                return copier;
            }
        }
    }

}
