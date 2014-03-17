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

import com.codename1.io.gzip.GZIPInputStream;
import ca.weblite.pisces.d.PathSink;
import ca.weblite.pisces.d.PathStore;
import ca.weblite.pisces.d.Transformer;
import ca.weblite.pisces.m.Matrix;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
//import java.util.zip.GZIPInputStream;

/**
 * Pisces path font
 */
public class Path {

    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;

    private static final String[] styles = {
        "PLAIN", "BOLD", "ITALIC", "BOLD+ITALIC"
    };

    private static Hashtable faces = new Hashtable();
    
    private static Face getFace(String name, int style) throws IOException {
        String fname = "/" + name + "_" + styles[style] + ".fnt.gz";
        Face face = (Face)faces.get(fname);
        if (face == null) {
            //InputStream in = (Path.class).getResourceAsStream(fname);
            throw new RuntimeException("getFace() not supported");
//            if (in == null && style != PLAIN) {
//                return getFace(name, PLAIN);
//            }
//            face = new Face(in);
//            faces.put(fname, face);
        }
        return face;
    }


    public static class Face {

        PathStore[] paths = new PathStore[256];
        int[] minX = new int[256];
        int[] minY = new int[256];
        int[] width = new int[256];
        int[] height = new int[256];
        double scale;

        public Face(InputStream in) throws IOException {
            GZIPInputStream gin = new GZIPInputStream(in);
            DataInputStream dis = new DataInputStream(gin);
            String name = dis.readUTF();
            String style = dis.readUTF();

            this.scale = dis.readDouble();

            while (true) {
                char glyph;
                try {
                    glyph = dis.readChar();
                } catch (EOFException eof) {
                    return;
                }
                int gx = dis.readInt();
                int gy = dis.readInt();
                int gwidth = dis.readInt();
                int gheight = dis.readInt();
                int numEntries = dis.readInt();

                PathStore ps = new PathStore(numEntries);

                int[] x = new int[4];
                int[] y = new int[4];
                int sx0 = 0, sy0 = 0, xp = 0, yp = 0;

                boolean prevIsQuad = false;
                boolean prevIsCubic = false;
                
                while (true) {
                    char tok = dis.readChar();
                    if (tok == 'Z') {
                        ps.close();
                        ps.end();
                        break;
                    } else if (tok == 'E') {
                        ps.end();
                        break;
                    }

                    int x0 = x[0];
                    int y0 = y[0];
		
                    switch (tok) {
                    case 'M':
                        x[0] = dis.readInt();
                        y[0] = dis.readInt();
                        break;

                    case 'm':
                        x[0] += dis.readShort();
                        y[0] += dis.readShort();
                        break;

                    case 'n':
                        x[0] += dis.readByte();
                        y[0] += dis.readByte();
                        break;

                    case 'H':
                        x[0] = dis.readInt();
                        break;

                    case 'h':
                        x[0] += dis.readShort();
                        break;

                    case 'i':
                        x[0] += dis.readByte();
                        break;

                    case 'V':
                        y[0] = dis.readInt();
                        break;

                    case 'v':
                        y[0] += dis.readShort();
                        break;

                    case 'w':
                        y[0] += dis.readByte();
                        break;

                    case 'L':
                        x[0] = dis.readInt();
                        y[0] = dis.readInt();
                        break;

                    case 'l':
                        x[0] += dis.readShort();
                        y[0] += dis.readShort();
                        break;

                    case 'k':
                        x[0] += dis.readByte();
                        y[0] += dis.readByte();
                        break;

                    case 'Q':
                        x[0] = dis.readInt();
                        y[0] = dis.readInt();
                        x[1] = dis.readInt();
                        y[1] = dis.readInt();
                        break;

                    case 'q':
                        x[0] = x0 + dis.readShort();
                        y[0] = y0 + dis.readShort();
                        x[1] = x0 + dis.readShort();
                        y[1] = y0 + dis.readShort();
                        break;

                    case 'r':
                        x[0] = x0 + dis.readByte();
                        y[0] = y0 + dis.readByte();
                        x[1] = x0 + dis.readByte();
                        y[1] = y0 + dis.readByte();
                        break;

                    case 'T':
                        x[0] = x0 + (prevIsQuad ? (x0 - xp) : 0);
                        y[0] = y0 + (prevIsQuad ? (y0 - yp) : 0);
                        x[1] = dis.readInt();
                        y[1] = dis.readInt();
                        break;

                    case 't':
                        x[0] = x0 + (prevIsQuad ? (x0 - xp) : 0);
                        y[0] = y0 + (prevIsQuad ? (y0 - yp) : 0);
                        x[1] = x0 + dis.readShort();
                        y[1] = y0 + dis.readShort();
                        break;

                    case 'u':
                        x[0] = x0 + (prevIsQuad ? (x0 - xp) : 0);
                        y[0] = y0 + (prevIsQuad ? (y0 - yp) : 0);
                        x[1] = x0 + dis.readByte();
                        y[1] = y0 + dis.readByte();
                        break;

                    case 'C':
                        x[0] = dis.readInt();
                        y[0] = dis.readInt();
                        x[1] = dis.readInt();
                        y[1] = dis.readInt();
                        x[2] = dis.readInt();
                        y[2] = dis.readInt();
                        break;

                    case 'c':
                        x[0] = x0 + dis.readShort();
                        y[0] = y0 + dis.readShort();
                        x[1] = x0 + dis.readShort();
                        y[1] = y0 + dis.readShort();
                        x[2] = x0 + dis.readShort();
                        y[2] = y0 + dis.readShort();
                        break;

                    case 'd':
                        x[0] = x0 + dis.readByte();
                        y[0] = y0 + dis.readByte();
                        x[1] = x0 + dis.readByte();
                        y[1] = y0 + dis.readByte();
                        x[2] = x0 + dis.readByte();
                        y[2] = y0 + dis.readByte();
                        break;

                    case 'S':
                        x[0] = x0 + (prevIsCubic ? (x0 - xp) : 0);
                        y[0] = y0 + (prevIsCubic ? (y0 - yp) : 0);
                        x[1] = dis.readInt();
                        y[1] = dis.readInt();
                        x[2] = dis.readInt();
                        y[2] = dis.readInt();
                        break;

                    case 's':
                        x[0] = x0 + (prevIsCubic ? (x0 - xp) : 0);
                        y[0] = y0 + (prevIsCubic ? (y0 - yp) : 0);
                        x[1] = x0 + dis.readShort();
                        y[1] = y0 + dis.readShort();
                        x[2] = x0 + dis.readShort();
                        y[2] = y0 + dis.readShort();
                        break;
                    
                    case 'p':
                        x[0] = x0 + (prevIsCubic ? (x0 - xp) : 0);
                        y[0] = y0 + (prevIsCubic ? (y0 - yp) : 0);
                        x[1] = x0 + dis.readByte();
                        y[1] = y0 + dis.readByte();
                        x[2] = x0 + dis.readByte();
                        y[2] = y0 + dis.readByte();
                        break;
                    }

                    switch (tok) {
                    case 'M': case 'm': case 'n':
                        ps.moveTo(x[0], y[0]);
                        sx0 = x[0];
                        sy0 = y[0];
                        prevIsQuad = prevIsCubic = false;
                        break;

                    case 'H': case 'h': case 'i':
                    case 'V': case 'v': case 'w':
                    case 'L': case 'l': case 'k':
                        ps.lineTo(x[0], y[0]);
                        prevIsQuad = prevIsCubic = false;
                        break;

                    case 'Q': case 'q': case 'r':
                    case 'T': case 't': case 'u':
                        ps.quadTo(x[0], y[0], x[1], y[1]);
                        xp = x[0];
                        yp = y[0];
                        x[0] = x[1];
                        y[0] = y[1];
                        prevIsQuad = true;
                        prevIsCubic = false;
                        break;

                    case 'C': case 'c': case 'd':
                    case 'S': case 's': case 'p':
                        ps.cubicTo(x[0], y[0], x[1], y[1], x[2], y[2]);
                        xp = x[1];
                        yp = y[1];
                        x[0] = x[2];
                        y[0] = y[2];
                        prevIsQuad = false;
                        prevIsCubic = true;
                        break;
                    
                    case 'z':
                        ps.close();
                        x[0] = sx0;
                        y[0] = sy0;
                        prevIsQuad = prevIsCubic = false;
                        break;
                    }
                }

                int idx = glyph;
                paths[idx] = ps;
                minX[idx] = gx;
                minY[idx] = gy;
                width[idx] = gwidth;
                height[idx] = gheight;
            }
        }
    }



    String name;
    int style;
    double size;

    Face face;

    public Path(String name, int style, double size) throws IOException {
        super();
        this.face = getFace(name, style);
        this.name = name;
        this.style = style;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getStyle() {
        return style;
    }

    public double getSize() {
        return size;
    }

    public void getBounds(String s, double[] bounds) {

        int c = (int)s.charAt(0);
        int minX = face.minX[c];
        int minY = face.minY[c];
        double width = 0;
        double height = 0;
        for (int i = 0; i < s.length(); i++) {
            c = (int)s.charAt(i);
            width += (face.width[c]*size);
            if (height < face.height[c]) {
                height = face.height[c];
            }
        }

        bounds[0] = minX;
        bounds[1] = minY;
        bounds[2] = width;
        bounds[3] = height;
    }
    
    public void produce(PathSink consumer, String s, double x, double y) {

        double size2 = (this.size*face.scale);

        for (int i = 0; i < s.length(); i++) {

            int c = (int)s.charAt(i);

            PathStore glyph = face.paths[c];

            double width = (face.width[c]*size);

            Matrix transform = new Matrix(size2, 0,
                                          0, size2,
                                          x, y);

            Transformer pt = new Transformer(consumer, transform);

            glyph.produce(pt);

            x += width;
        }
    }
}
