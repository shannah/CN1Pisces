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


import pisces.m.Matrix;
import pisces.m.Tuple;

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

    public Point2D getCurrentPoint(){
        return new Point2D(this.x0, this.y0);
    }
    

    /**
     * Will change the first path instruction from a moveTo to a lineTo of join is
     * true.  Will change to a moveTo if it is false.
     * @param join 
     */
    public void join(boolean join){
        if ( pathTypes.length >= 1 ){
            if ( join && pathTypes[0] == MOVE_TO ){
                pathTypes[0] = LINE_TO;
            } else if ( !join && pathTypes[0] == LINE_TO ){
                pathTypes[0] = MOVE_TO;
            }
        }
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
            if ( grow == 0 ){
                ensureCapacity(1);
                return;
            }
            double[] newPathData = new double[pathData.length + grow];
            System.arraycopy(pathData, 0, newPathData, 0, pathData.length);
            this.pathData = newPathData;
        }

        if (tindex + 1 > pathTypes.length) {
            if ( grow == 0 ){
                ensureCapacity(1);
                return;
            }
            byte[] newPathTypes = new byte[pathTypes.length + grow];
            System.arraycopy(pathTypes, 0, newPathTypes, 0, pathTypes.length);
            this.pathTypes = newPathTypes;
        }
    }
    
    public void transform(Matrix transform){
        int len = pathData.length;
        Tuple tuple = new Tuple();
        Tuple res = new Tuple();
        for ( int i=0; i<len; i+=2 ){
            tuple.x = pathData[i];
            tuple.y = pathData[i+1];
            tuple.z = 1;
            transform.transform(tuple, res);
            pathData[i] = res.x;
            pathData[i+1] = res.y;
        }
        
    }
    /**
     * CURRENTLY BROKEN!!!!  For some reason the path gets corrupted when using this
     * method.  Need to work it out.
     * @param it
     * @param append 
     */
    public void append(PathIterator it, boolean append){
        double[] buf = new double[6];
        int type = 0;
        
        while ( !it.isDone() ){
            it.next();
            type = it.currentSegment(buf);
            if ( type == PathIterator.SEG_MOVETO && append ){
                type = PathIterator.SEG_LINETO;
                append = false;
            }
            
            switch ( type ){
                case PathIterator.SEG_MOVETO:
                    this.moveTo(buf[0], buf[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    this.lineTo(buf[0], buf[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    this.quadTo(buf[0], buf[1], buf[2], buf[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    this.cubicTo(buf[0], buf[1], buf[2], buf[3], buf[4], buf[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    this.close();
                    break;
                default:
                    throw new RuntimeException("Invalid segment type "+type);
            }
        }
    }        
            
    public void append(PathStore path, boolean append){
        if ( path.tindex <= 0 ){
            return;
        }
        
        path.join(append);
        int len = path.tindex;
        int j = 0;
        for ( int i=0; i<len; i++){
            byte command = path.pathTypes[i];
            switch ( command ){
                case MOVE_TO:
                    this.moveTo(path.pathData[j++], path.pathData[j++]);
                    break;
                case LINE_TO:
                    this.lineTo(path.pathData[j++], path.pathData[j++]);
                    break;
                case QUAD_TO:
                    this.quadTo(path.pathData[j++], path.pathData[j++], path.pathData[j++], path.pathData[j++]);
                    break;
                case  CUBIC_TO:
                    this.cubicTo(path.pathData[j++], path.pathData[j++], path.pathData[j++], path.pathData[j++], path.pathData[j++], path.pathData[j++]);
                    break;
                case LINE_JOIN:
                    this.lineJoin();
                    break;
                case CLOSE:
                    this.close();
                    break;
            }
        }
    }
    
    
    
    public Rectangle2D getBounds2D(){
        double minX = 0;
        double minY = 0;
        double maxY = 0;
        double maxX = 0;
        
        int len = pathData.length;
        for ( int i=0; i<len; i+=2){
            double x1 = pathData[i];
            double y1 = pathData[i+1];
            if ( x1 < minX ){
                minX = x1;
            }
            if ( x1 > maxX ){
                maxX = x1;
            }
            if ( y1 < minY ){
                minY = y1;
            }
            if ( y1 > maxY ){
                maxY = y1;
            }
        }
        return new Rectangle2D(minX, minY, maxX-minX, maxY-minY);
        
    }
    
    public PathIterator getPathIterator(Matrix transform){
        return new PathIteratorImpl(transform);
    }
    
    private class PathIteratorImpl implements PathIterator {
        
        private int dindex=0;
        private int tindex=-1;
        private boolean done = false;
        private Matrix transform = null;
        private double sx=0;
        private double sy=0;

        
        private PathIteratorImpl(Matrix transform){
            this.transform = transform;
        }
        
        
        public int currentSegment(double[] coords) {
            int type = 0;
            switch ( PathStore.this.pathTypes[tindex]){
                case MOVE_TO:
                    
                    coords[0] = pathData[dindex];
                    coords[1] = pathData[dindex+1];
                    type = SEG_MOVETO;
                    sx = coords[0];
                    sy = coords[1];
                    break;
                    
                case LINE_TO:
                    coords[0] = pathData[dindex];
                    coords[1] = pathData[dindex+1];
                    type = SEG_LINETO;
                    break;
                    
                case QUAD_TO:
                    coords[0] = pathData[dindex];
                    coords[1] = pathData[dindex+1];
                    coords[2] = pathData[dindex+2];
                    coords[3] = pathData[dindex+3];
                    type = SEG_QUADTO;
                    break;
                    
                case CUBIC_TO:
                    coords[0] = pathData[dindex];
                    coords[1] = pathData[dindex+1];
                    coords[2] = pathData[dindex+2];
                    coords[3] = pathData[dindex+3];
                    coords[4] = pathData[dindex+4];
                    coords[5] = pathData[dindex+5];
                    type = SEG_CUBICTO;
                    break;
                
                case LINE_JOIN:
                    coords[0] = pathData[0];
                    coords[1] = pathData[1];
                    type = SEG_LINETO;
                    break;
                    
                case CLOSE:
                    type = SEG_CLOSE;
                    break;
                    
                case END:
                    throw new RuntimeException("Path is ended");
                    
                    
            }
            
            if ( transform != null ){
                Tuple t = new Tuple(0,0,1);
                Tuple res = new Tuple();
                for ( int i=0; i<coords.length; i+=2 ){
                    t.x = coords[i];
                    t.y = coords[i+1];
                    transform.transform(t, res);
                    coords[i] = res.x;
                    coords[i+1] = res.y;
                }
            }
            
            return type;
        }

        public int currentSegment(float[] coords) {
            double[] tmp = new double[coords.length];
            int type = currentSegment(tmp);
            for ( int i=0; i<coords.length; i++){
                coords[i] = (float)tmp[i];
            }
            return type;
        }

        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        public boolean isDone() {
            return done || PathStore.this.tindex==0 || PathStore.this.tindex <= this.tindex+1 || pathTypes.length <= tindex || pathTypes[tindex+1] == END ;
        }

        public void next() {
            if ( tindex == -1 ){
                tindex++;
                return;
            }
            switch ( pathTypes[tindex] ){
                case MOVE_TO:
                case LINE_TO:
                    dindex += 2;
                    break;
                case QUAD_TO:
                    dindex += 4;
                    break;
                    
                case CUBIC_TO:
                    dindex += 6;
                    break;
                
                case LINE_JOIN:
                case CLOSE:
                    break;
                    
                case END:
                    done = true;
            }
            tindex++;
        }
        
    }

}
