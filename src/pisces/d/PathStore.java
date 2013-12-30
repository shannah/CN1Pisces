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
 * Reproducible path sink
 */
public class PathStore
    extends PathSink
    implements PathSource
{

    private static final byte MOVE_TO            = (byte)'M';
    private static final byte LINE_JOIN          = (byte)'J';
    private static final byte LINE_TO            = (byte)'L';
    private static final byte QUAD_TO            = (byte)'Q';
    private static final byte CUBIC_TO           = (byte)'C';
    private static final byte CLOSE              = (byte)'z';
    private static final byte END                = (byte)'E';

    private int numSegments = 0;

    private double[] pathData;
    private int dindex = 0;

    private byte[] pathTypes;
    private int tindex = 0;

    private double x0, y0, sx0, sy0, xp, yp;


    public PathStore() {
        this(128);
    }
    public PathStore(int inicap) {
        super();
        this.pathData = new double[inicap];
        this.pathTypes = new byte[inicap];
    }


    public void moveTo(double x0, double y0) {
        ensureCapacity(2);
        pathTypes[tindex++] = MOVE_TO;
        pathData[dindex++] = x0;
        pathData[dindex++] = y0;
        
        this.sx0 = this.x0 = x0;
        this.sy0 = this.y0 = y0;
    }
    public void lineJoin() {
        ensureCapacity(0);
        pathTypes[tindex++] = LINE_JOIN;
    }
    public void lineTo(double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;

        ensureCapacity(2);
        pathTypes[tindex++] = LINE_TO;
        pathData[dindex++] = x1;
        pathData[dindex++] = y1;

        this.x0 = x1;
        this.y0 = y1;
    }
    public void quadTo(double x1, double y1, double x2, double y2) {
        double dx1 = x1 - x0;
        double dy1 = y1 - y0;
        double dx2 = x2 - x0;
        double dy2 = y2 - y0;

        ensureCapacity(4);
        pathTypes[tindex++] = QUAD_TO;
        pathData[dindex++] = x1;
        pathData[dindex++] = y1;
        pathData[dindex++] = x2;
        pathData[dindex++] = y2;

        this.xp = x1;
        this.yp = y1;
        this.x0 = x2;
        this.y0 = y2;
    }
    public void cubicTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        ensureCapacity(6);
        pathTypes[tindex++] = CUBIC_TO;
        pathData[dindex++] = x1;
        pathData[dindex++] = y1;
        pathData[dindex++] = x2;
        pathData[dindex++] = y2;
        pathData[dindex++] = x3;
        pathData[dindex++] = y3;

        this.x0 = x3;
        this.y0 = y3;
    }
    public void close() {
        ensureCapacity(0);
        pathTypes[tindex++] = CLOSE;

        this.x0 = sx0;
        this.y0 = sy0;
    }
    public void end() {
        ensureCapacity(0);
        pathTypes[tindex++] = END;

        this.x0 = 0;
        this.y0 = 0;
    }

    public void produce(PathSink consumer) {
        int tidx = 0;
        int didx = 0;
        double x0 = 0, y0 = 0, sx0 = 0, sy0 = 0;

        while (tidx < tindex) {
            switch (pathTypes[tidx++]) {
            case MOVE_TO:
                sx0 = x0 = pathData[didx++];
                sy0 = y0 = pathData[didx++];
                consumer.moveTo(x0, y0);
                break;

            case LINE_JOIN:
                consumer.lineJoin();
                break;

            case LINE_TO:
                consumer.lineTo(x0 = pathData[didx++], y0 = pathData[didx++]);
                break;

            case QUAD_TO:
                consumer.quadTo(pathData[didx++], pathData[didx++],
                                x0 = pathData[didx++], y0 = pathData[didx++]);
                break;

            case CUBIC_TO:
                consumer.cubicTo(pathData[didx++], pathData[didx++],
                                 pathData[didx++], pathData[didx++],
                                 x0 = pathData[didx++], y0 = pathData[didx++]);
                break;

            case CLOSE:
                consumer.close();
                x0 = sx0;
                y0 = sy0;
                break;

            case END:
                consumer.end();
                x0 = 0;
                y0 = 0;
                break;
            }
        }
    }
    public void dispose(){
    }
    protected void ensureCapacity(int elements) {

        final int grow = (elements<<1);

        if (dindex + elements > pathData.length) {
            double[] newPathData = new double[pathData.length + grow];
            System.arraycopy(pathData, 0, newPathData, 0, pathData.length);
            this.pathData = newPathData;
        }

        if (tindex + 1 > pathTypes.length) {
            byte[] newPathTypes = new byte[pathTypes.length + grow];
            System.arraycopy(pathTypes, 0, newPathTypes, 0, pathTypes.length);
            this.pathTypes = newPathTypes;
        }
    }

}
