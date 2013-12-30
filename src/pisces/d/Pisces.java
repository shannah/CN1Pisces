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

import com.codename1.io.Log;
import pisces.Color;
import pisces.m.Matrix;

/**
 * Floating point user interface (package driver).
 * 
 * @see pisces.Graphics
 * @see pisces.d2.Renderer
 */
public final class Pisces
    extends PathSink
    implements Surface.Sink
{

    private static final double STROKE_X_BIAS;
    private static final double STROKE_Y_BIAS;
    private static final double DEFAULT_FILLER_FLATNESS;
    static {
        String sval;
        double dval;

        sval = System.getProperty("pisces.stroke.xbias");
        dval = 0;
        if (sval != null) {
            try {
                dval = Double.parseDouble(sval);
            }
            catch (NumberFormatException e) {
            }
        }
        STROKE_X_BIAS = dval;
        
        sval = System.getProperty("pisces.stroke.ybias");
        dval = 0;
        if (sval != null) {
            try {
                dval = Double.parseDouble(sval);
            }
            catch (NumberFormatException e) {
            }
        }
        STROKE_Y_BIAS = dval;

        sval = System.getProperty("pisces.filler.flatness");
        dval = 1.0;
        if (null != sval){
            try {
                dval = Double.parseDouble(sval);
            }
            catch (NumberFormatException exc){
            }
        }
        DEFAULT_FILLER_FLATNESS = dval;
    }

    private static final double acv = 0.22385762508460333;


    private Surface.Sink surface;
    private int width, height;

    private RendererBase rdr;
    private PathSink fillerP = null;
    private PathSink textFillerP = null;
    private PathSink strokerP = null;

    private PathSink externalConsumer;
    private boolean inSubpath = false;
    private boolean isPathFilled = false;
    
    private double lineWidth = 1;
    private int capStyle = 0;
    private int joinStyle = 0;
    private double miterLimit = 10;

    private double[] dashArray = null;
    private double dashPhase = 0;

    private Matrix transform = new Matrix();

    private Paint paint;
    private Matrix paintTransform;
    private Matrix paintCompoundTransform;

    private int[] gcm_fractions = null;
    private int[] gcm_rgba = null;
    private int gcm_cycleMethod = -1;

    private Color color = Color.Transparent.Black;

    /*
     * Current bounding box for all primitives
     */
    private double bbMinX = Integer.MIN_VALUE;
    private double bbMinY = Integer.MIN_VALUE;
    private double bbMaxX = Integer.MAX_VALUE;
    private double bbMaxY = Integer.MAX_VALUE;
    
    private Flattener fillFlattener = new Flattener();
    private Transformer fillTransformer = new Transformer();
    
    private Flattener textFlattener = new Flattener();
    private Transformer textTransformer = new Transformer();

    private Stroker strokeStroker = new Stroker();
    private Dasher strokeDasher = new Dasher();
    private Flattener strokeFlattener = new Flattener();
    private Transformer strokeTransformer = new Transformer();

    private boolean antialiasingOn = true;


    public Pisces(Surface.Sink surface){
        super();
        if (null != surface){
            this.surface = surface;
            this.width = surface.getWidth();
            this.height = surface.getHeight();
            /*
             * offset = 0
             * scanlineStride = surface.getWidth()
             * pixelStride = 1
             */
            this.rdr = new Renderer(surface.getData(), this.width, this.height,
                                    0, this.width, 1, surface.getDataType());

            this.invalidate();
            this.setFill();
        }
        else
            throw new IllegalArgumentException();
    }


    public void dispose(){
        this.surface = null;
        RendererBase r = this.rdr;
        if (null != r){
            this.rdr = null;
            r.dispose();
        }
        LineSink p;

        p = this.fillerP;
        if (null != p){
            this.fillerP = null;
            p.dispose();
        }
        p = this.textFillerP;
        if (null != p){
            this.textFillerP = null;
            p.dispose();
        }
        p = this.strokerP;
        if (null != p){
            this.strokerP = null;
            p.dispose();
        }
        p = this.externalConsumer;
        if (null != p){
            this.externalConsumer = null;
            p.dispose();
        }
        this.transform = null;
        this.paint = null;
        this.paintTransform = null;
        this.paintCompoundTransform = null;
        p = this.fillFlattener;
        if (null != p){
            this.fillFlattener = null;
            p.dispose();
        }
        p = this.fillTransformer;
        if (null != p){
            this.fillTransformer = null;
            p.dispose();
        }
        p = this.textFlattener;
        if (null != p){
            this.textFlattener = null;
            p.dispose();
        }
        p = this.strokeStroker;
        if (null != p){
            this.strokeStroker = null;
            p.dispose();
        }
        p = this.strokeDasher;
        if (null != p){
            this.strokeDasher = null;
            p.dispose();
        }
        p = this.strokeFlattener;
        if (null != p){
            this.strokeFlattener = null;
            p.dispose();
        }
        p = this.strokeTransformer;
        if (null != p){
            this.strokeTransformer = null;
            p.dispose();
        }
    }
    public int getDataType(){
        return this.surface.getDataType();
    }
    public Object getData(){
        return this.surface.getData();
    }
    public int getWidth(){
        return this.width;
    }
    public int getHeight(){
        return this.height;
    }
    public void getRGB(int[] argb, int offset, int scan, 
                       int x, int y, int w, int h)
    {
        this.surface.getRGB(argb,offset,scan,x,y,w,h);
    }
    public void setRGB(int[] argb, int offset, int scan, 
                       int x, int y, int w, int h)
    {
        this.surface.setRGB(argb,offset,scan,x,y,w,h);
    }
    public void blit(Surface ps, int srcX, int srcY, 
                     int dstX, int dstY, int w, int h, float opacity)
    {
        this.surface.blit(ps,srcX, srcY, dstX, dstY, w, h, opacity);
    }
    public void blit(int[] argb, int offset, int scan, 
                        int x, int y, int w, int h, float opacity)
    {
        this.surface.blit(argb,offset,scan,x,y,w,h,opacity);
    }
    public void setAntialiasing(boolean antialiasingOn) {
        this.antialiasingOn = antialiasingOn;
        int samples = antialiasingOn ? 3 : 0;
        this.rdr.setAntialiasing(samples, samples);
        this.invalidate();
    }
    public boolean getAntialiasing() {
        return this.antialiasingOn;
    }
    public Color getColor(){
        return this.color;
    }
    public void setColor(Color color){
        if (null != color){
            this.color = color;
            this.paint = null;
            this.rdr.setColor(color.red, color.green, color.blue, color.alpha);
        }
        else
            throw new IllegalArgumentException();
    }
    public void setPaint(Paint paint, Matrix transform)
    {
        Matrix paintCompoundTransform = new Matrix(this.transform);

        paintCompoundTransform.mul(transform);

        this.setPaintTransform(paintCompoundTransform);

        this.paint = paint; /* new Texture(imageType, imageData,
                             *              width, height, offset, stride,
                             *              textureCompoundTransform, repeat);
                             */
        this.rdr.setPaint(paint);
    }
    public PathSink getStroker() {
        if (this.strokerP == null) {
            strokeStroker.setOutput(rdr);
            strokeStroker.setParameters(lineWidth,
                                        capStyle, joinStyle,
                                        miterLimit, transform);
            if (dashArray == null) {
                strokeFlattener.setOutput(strokeStroker);
                strokeFlattener.setFlatness(1);
            }
            else {
                strokeDasher.setOutput(strokeStroker);
                strokeDasher.setParameters(dashArray, dashPhase, transform);
                strokeFlattener.setOutput(strokeDasher);
                strokeFlattener.setFlatness(1);
            }

            Matrix t = transform;
            t = new Matrix(transform);
            t.m02 += STROKE_X_BIAS;
            t.m12 += STROKE_Y_BIAS;
            strokeTransformer.setTransform(t);
            strokeTransformer.setOutput(strokeFlattener);
            this.strokerP = strokeTransformer;
        }
        return strokerP;
    }
    public PathSink getFiller() {
        if (this.fillerP == null) {
            fillFlattener.setOutput(rdr);
            fillFlattener.setFlatness(DEFAULT_FILLER_FLATNESS);

            fillTransformer.setOutput(fillFlattener);
            fillTransformer.setTransform(transform);
            this.fillerP = fillTransformer;
        }
        return fillerP;
    }
    public PathSink getTextFiller() {
        if (textFillerP == null) {
            textFlattener.setOutput(rdr);
            /*
             * Intended for sample (3) or (0)
             */
            if (0 != Math.min(rdr.getSubpixelLgPositionsX(),
                              rdr.getSubpixelLgPositionsY()))

                textFlattener.setFlatness(Flattener.MIN_CHORD_LENGTH_SQ);
            else
                textFlattener.setFlatness(DEFAULT_FILLER_FLATNESS);

            textTransformer.setOutput(textFlattener);
            textTransformer.setTransform(transform);
            this.textFillerP = textTransformer;
        }
        return textFillerP;
    }
    /**
     * Sets the current stroke parameters.
     *
     * @param lineWidth the sroke width
     * @param capStyle the line cap style, one of
     * <code>Stroker.CAP_*</code>.
     * @param joinStyle the line cap style, one of
     * <code>Stroker.JOIN_*</code>.
     * @param miterLimit the stroke miter limit
     * @param dashArray an <code>int</code> array containing the dash
     * segment lengths in S15.16 format, or <code>null</code>.
     * @param dashPhase the starting dash offset, in S15.16 format.
     */
    public void setStroke(double lineWidth, int capStyle, int joinStyle,
                          double miterLimit, double[] dashArray, double dashPhase)
    {

        this.lineWidth = lineWidth;
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;
        this.miterLimit = miterLimit;
        this.dashArray = dashArray;
        this.dashPhase = dashPhase;
        this.strokerP = null;
        this.setStroke();
    }
    /**
     * Sets the current transform from user to window coordinates.
     *
     * @param transform an <code>Matrix</code> object.
     */
    public void setTransform(Matrix transform) {

        this.transform = transform;

        if (paint != null) {
            setPaintTransform(paintTransform);
            paint.setTransform(this.paintCompoundTransform);
            rdr.setPaint(paint);
        }

        invalidate();
    }
    public Matrix getTransform() {
        return new Matrix(transform);
    }
    /**
     * Sets a clip rectangle for all primitives.  Each primitive will be
     * clipped to the intersection of this rectangle and the destination
     * image bounds.
     */
    public void setClip(double minX, double minY, double width, double height) {
        this.bbMinX = minX;
        this.bbMinY = minY;
        this.bbMaxX = minX + width;
        this.bbMaxY = minY + height;
    }
    /**
     * Resets the clip rectangle.  Each primitive will be clipped only
     * to the destination image bounds.
     */
    public void resetClip() {
        this.bbMinX = Integer.MIN_VALUE;
        this.bbMinY = Integer.MIN_VALUE;
        this.bbMaxX = Integer.MAX_VALUE;
        this.bbMaxY = Integer.MAX_VALUE;
    }
    public void beginRendering(int windingRule) {

        double minX = Math.max(0, bbMinX);
        double minY = Math.max(0, bbMinY);
        double maxX = Math.min(width, bbMaxX);
        double maxY = Math.min(height, bbMaxY);
        this.beginRendering(minX, minY, maxX - minX, maxY - minY, windingRule);
    }
    /**
     * Begins the rendering of path data.  The supplied clipping
     * bounds are intersected against the current clip rectangle and
     * the destination image bounds; only pixels within the resulting
     * rectangle may be written to.
     */
    public void beginRendering(double minX, double minY, double width, double height,
                               int windingRule)
    {
        this.inSubpath = false;
        
        double maxX = minX + width;
        double maxY = minY + height;
        
        minX = Math.max(minX, 0);
        minX = Math.max(minX, this.bbMinX);

        minY = Math.max(minY, 0);
        minY = Math.max(minY, this.bbMinY);

        maxX = Math.min(maxX, this.width);
        maxX = Math.min(maxX, this.bbMaxX);

        maxY = Math.min(maxY, this.height);
        maxY = Math.min(maxY, this.bbMaxY);

        width = maxX - minX;
        height = maxY - minY;

        this.rdr.beginRendering(minX, minY, width, height, windingRule);
    }
    public void moveTo(double x0, double y0) {

        if (inSubpath && isPathFilled) {
            externalConsumer.close();
        }
        inSubpath = false;
        externalConsumer.moveTo(x0, y0);
    }
    public void lineTo(double x1, double y1) {

        inSubpath = true;
        externalConsumer.lineTo(x1, y1);
    }
    public void lineJoin() {

        externalConsumer.lineJoin();
    }
    public void quadTo(double x1, double y1, double x2, double y2) {

        inSubpath = true;
        externalConsumer.quadTo(x1, y1, x2, y2);
    }
    public void cubicTo(double x1, double y1, double x2, double y2, double x3, double y3) {

        inSubpath = true;
        externalConsumer.cubicTo(x1, y1, x2, y2, x3, y3);
    }
    public void close() {

        inSubpath = false;
        externalConsumer.close();
    }
    public void end() {

        if (inSubpath && isPathFilled) {
            close();
        }
        inSubpath = false;
        externalConsumer.end();
    }
    /**
     * Completes the rendering of path data.  Destination pixels will
     * be written at this time.
     */
    public void endRendering() {

        this.end();

        this.rdr.endRendering();
    }
    /**
     * Render a complex path, possibly caching the results in a form
     * that can be rendered more rapidly at a future time.  The cache
     * will be valid across changes in paint style, but not across
     * changes to the transform, stroke/fill mode setting, stroke
     * parameters, or winding rule.
     
     * <p> The implementation does not check the validity of the cache
     * relative to changes in the renderer state.  It is up to the
     * caller to manually invalidate the cache object as needed.  The
     * other parameters must contain a valid description of the path
     * even if a valid cache is passed in.  If <code>cache</code> is
     * <code>null</code>, no caching is performed.
     *
     * <p> A command that would reference coordinates outside the
     * bounds of the arguments will be silently ignored.
     *
     * <p> Retrieval of the bounding box using
     * <code>getBoundingBox</code> following a call to
     * <code>render</code> is supported.
     * 
     * @param commands One or more members of COMMAND_*, and implying
     * references to coords.
     * @param coords One or (X,Y) pairs referenced by commands.
     * @param windingRule Render fill rule, one of WIND_*.
     * @param cache Optional rendering cache.
     */
    public void renderPath(byte[] commands, double[] coords,
                           int windingRule, PiscesCache cache)
    {
        if (cache != null) {
            if (cache.isValid())
                this.rdr.renderFromCache(cache);
            else {
                this.rdr.setCache(cache);
                this.renderPath(commands, coords, windingRule);
                this.rdr.setCache(null);
            }
        } 
        else 
            this.renderPath(commands, coords, windingRule);
    }
    /**
     * Returns a bounding box containing all pixels drawn during the
     * rendering of the most recent primitive
     * (beginRendering/endRendering pair).
     * 
     * @return (x, y, width, height)
     */
    public void getBoundingBox(double[] bbox) {

        this.rdr.getBoundingBox(bbox);
    }
    public void setStroke() {
        this.isPathFilled = false;
        this.externalConsumer = this.getStroker();
    }
    public void setFill() {
        this.isPathFilled = true ;
        this.externalConsumer = this.getFiller();
    }
    public void setTextFill() {
        this.isPathFilled = true;
        this.externalConsumer = this.getTextFiller();
    }
    public void drawLine(double x0, double y0, double x1, double y1) {

        this.beginRendering(RendererBase.WIND_NON_ZERO);
        PathSink stroker = getStroker();
        stroker.moveTo(x0, y0);
        stroker.lineTo(x1, y1);
        stroker.end();
        this.endRendering();//this.rdr.endRendering();
    }
    public void fillRect(double x, double y, double w, double h) {

        if (w <= 0 || h <= 0) {
            return;
        }
        else {
            // Renderer will detect aligned rectangles
            PathSink filler = getFiller();
            this.fillOrDrawRect(filler, x, y, w, h);
        }
    }
    public void drawRect(double x, double y, double w, double h) {

        if (w <= 0 || h <= 0) {
            return;
        }
        else {
            /*
             * If dashing is disabled, and using mitered joins, simply
             * draw two opposing rect outlines separated by linewidth
             */
            if (dashArray == null) {

                if (joinStyle == Stroker.JOIN_MITER
                    && EGE(miterLimit, SQRT_TWO))
                {
                    double x0 = x + STROKE_X_BIAS;
                    double y0 = y + STROKE_Y_BIAS;
                    double x1 = x0 + w;
                    double y1 = y0 + h;
                
                    double lw = lineWidth;
                    double m = lineWidth/2.0;
                
                    PathSink filler = getFiller();
                    this.beginRendering(RendererBase.WIND_NON_ZERO);
                    filler.moveTo(x0 - m, y0 - m);
                    filler.lineTo(x1 + m, y0 - m);
                    filler.lineTo(x1 + m, y1 + m);
                    filler.lineTo(x0 - m, y1 + m);
                    filler.close();
                    /*
                     * Hollow out interior if w and h are greater than
                     * linewidth
                     */
                    if ((x1 - x0) > lw && (y1 - y0) > lw) {
                        filler.moveTo(x0 + m, y0 + m);
                        filler.lineTo(x0 + m, y1 - m);
                        filler.lineTo(x1 - m, y1 - m);
                        filler.lineTo(x1 - m, y0 + m);
                        filler.close();
                    }
                
                    filler.end();
                    this.endRendering();//this.rdr.endRendering();
                    return;
                }
                else if (joinStyle == Stroker.JOIN_ROUND) {
                    /*
                     * IMPL NOTE - accelerate hollow rects with round joins
                     */
                }
            }

            PathSink stroker = getStroker();
            this.fillOrDrawRect(stroker, x, y, w, h);
        }
    }
    public void drawOval(double x, double y, double w, double h) {

        this.fillOrDrawOval(x, y, w, h, true);
    }
    public void fillOval(double x, double y, double w, double h) {

        this.fillOrDrawOval(x, y, w, h, false);
    }
    public void fillRoundRect(double x, double y, double w, double h,
                              double aw, double ah)
    {
        if (w < Zero || h < Zero)
            return;
        else
            this.fillOrDrawRoundRect(x, y, w, h, aw, ah, false);
    }
    public void drawRoundRect(double x, double y, double w, double h,
                              double aw, double ah)
    {
        if (w < Zero || h < Zero)
            return;
        else
            this.fillOrDrawRoundRect(x, y, w, h, aw, ah, true);
    }
    public void drawArc(double x, double y, double width, double height,
                              double startAngle, double arcAngle, int arcType)
    {
        this.fillOrDrawArc(x, y, width, height, startAngle, arcAngle, arcType, true);
    }
    public void fillArc(double x, double y, double width, double height,
                              double startAngle, double arcAngle, int arcType)
    {
        this.fillOrDrawArc(x, y, width, height, startAngle, arcAngle, arcType, false);
    }
    public void fillOrDrawArc(double x, double y, double width, double height,
                              double startAngle, double arcAngle, int arcType,
                              boolean stroke)
    {
        PathSink consumer = stroke ? getStroker() : getFiller();
        if (width < Zero || height < Zero)
            return;
        else {
            double w2 = width/2.0;
            double h2 = height/2.0;
            double cx = x + w2;
            double cy = y + h2;

            startAngle *= Radians;
            arcAngle *= Radians;

            double endAngle = startAngle + arcAngle;

            int nPoints = (int)Math.max(16.0, Math.max(w2, h2));

            this.beginRendering(RendererBase.WIND_NON_ZERO);
            {
                this.emitArc(consumer, cx, cy, w2, h2, startAngle, endAngle, nPoints);

                if (arcType == ARC_PIE)
                    consumer.lineTo(cx, cy);

                if (!stroke || (arcType == ARC_CHORD) || (arcType == ARC_PIE))
                    consumer.close();

                consumer.end();
            }
            this.endRendering();//this.rdr.endRendering();
        }
    }

    public void clearRect(double x, double y, double w, double h) {

        double maxX = x + w;
        double maxY = y + h;
        
        x = Math.max(x, 0.0);
        x = Math.max(x, bbMinX);

        y = Math.max(y, 0.0);
        y = Math.max(y, bbMinY);

        maxX = Math.min(maxX, this.width);
        maxX = Math.min(maxX, bbMaxX);

        maxY = Math.min(maxY, this.height);
        maxY = Math.min(maxY, bbMaxY);

        rdr.clearRect(x, y, maxX - x, maxY - y);
    }
    /*
     * TODO: add cases for fields
     */
    public Pisces clone(){

        Pisces clone = (Pisces)super.clone();

        clone.rdr = clone.rdr.clone();

        return clone;
    }
    /*
     * 
     */
    private void invalidate() {
        this.fillerP = null;
        this.textFillerP = null;
        this.strokerP = null;
    }
    private void setPaintTransform(Matrix paintTransform) {
        this.paintTransform = new Matrix(paintTransform);
        this.paintCompoundTransform = new Matrix(paintTransform);
    }
    private void fillOrDrawRect(PathSink consumer,
                                double x, double y, double w, double h)
    {
        double x0 = x;
        double y0 = y;
        double x1 = x0 + w;
        double y1 = y0 + h;
        
        this.beginRendering(RendererBase.WIND_NON_ZERO);
        {
            consumer.moveTo(x0, y0);
            consumer.lineTo(x1, y0);
            consumer.lineTo(x1, y1);
            consumer.lineTo(x0, y1);
            consumer.close();
            consumer.end();
        }
        this.endRendering();//this.rdr.endRendering();
    }
    /*
     * Emit quarter-arc about a central point (cx, cy).  Each quadrant
     * is suitably reflected.
     */
    private void emitQuadrants(PathSink consumer, double cx, double cy,
                               double[] points, int np)
    {
        final int np2 = (np<<1);
        /*
         * Emit quarter-arc once for each quadrant, suitably reflected
         */
        for (int pass = 0; pass < 4; pass++) {
            //Log.p("Pass "+pass);
            int xsign, ysign;

            switch(pass){
                case 0:
                    xsign = +1;
                    ysign = +1;
                    break;
            case 1:
                xsign = -1;
                ysign = +1;
                break;
            case 2:
                xsign = -1;
                ysign = -1;
                break;
            case 3:
                xsign = +1;
                ysign = -1;
                break;
            default:
                throw new Error();
            }
            int incr = ((xsign*ysign)<<1);

            int idx = (incr > 0)?(0):(np2 - 2);

            for (int loop = 0; loop < np; loop++) {

                consumer.lineTo(cx + xsign*points[idx],
                                cy + ysign*points[idx + 1]);
                idx += incr;
            }
        }
    }
    private void emitOval(PathSink consumer,
                          double cx, double cy, double rx, double ry,
                          int np, boolean reverse)
    {
        double a = reverse ? np - 1 : 1;
        final double ainc = reverse ? -1 : 1;

        consumer.moveTo(cx + rx, cy);

        final double arc = np;

        np >>= 1;
        double[] points = new double[np];
        np >>= 1;

        int idx = 0;
        for (int j = 0; j < np; j++) {

            double theta = (a*PI_M2)/arc;

            double ox = Math.cos(theta);
            double oy = Math.sin(theta);

            points[idx++] = (rx*ox);
            points[idx++] = (ry*oy);

            a += ainc;
        }
        
        this.emitQuadrants(consumer, cx, cy, points, np);
        consumer.close();
    }
    /*
     * Emit the outline of an oval, offset by pen radius lw2
     * The interior path may self-intersect, but this is handled
     * by using a WIND_NON_ZERO wining rule
     */
    private void emitOffsetOval(PathSink consumer,
                                double cx, double cy, double rx, double ry,
                                double lw2,
                                int np, boolean inside)
    {
        double a = inside ? np - 1 : 1;
        final double ainc = inside ? -1 : 1;

        consumer.moveTo(cx + rx + lw2*ainc, cy);

        np >>= 1;
        double[] points = new double[np];
        np >>= 1;

        final double arc = PI_D2/np;

        int idx = 0;
        for (int j = 0; j < np; j++) {

            double theta = (a*arc);

            double cos = Math.cos(theta);
            double sin = Math.sin(theta);
            double rxSin = rx*sin;
            double ryCos = ry*cos;
            double den = lw2/Math.sqrt(rxSin*rxSin +
                                       ryCos*ryCos);
            double dpx = cos*(rx + ainc*ry*den);
            double dpy = sin*(ry + ainc*rx*den);

            points[idx++] = dpx;
            points[idx++] = dpy;

            a += ainc;
        }

        emitQuadrants(consumer, cx, cy, points, np);
        consumer.close();
    }
    private void fillOrDrawOval(double x, double y, double w, double h,
                                boolean hollow)
    {
        if (w <= 0 || h <= 0)
            return;
        else {
            double w2 = w/2.0;
            double h2 = h/2.0;
            double cx = x + w2;
            double cy = y + h2;
            double lineWidth2 = (hollow)?(lineWidth/2.0):(0);
        
            double wl = w2 + lineWidth2;
            double hl = h2 + lineWidth2;
            int np = (int)(Math.max(16.0, Math.max(wl, hl)));

            this.beginRendering(RendererBase.WIND_NON_ZERO);
            /*
             * Stroke the outline if dashing
             */
            if (hollow && dashArray != null) {
                PathSink stroker = getStroker();
                this.emitOval(stroker, cx, cy, w2, h2, np, false);
                stroker.end();
                this.endRendering();//this.rdr.endRendering();
            }
            else {
                if (!antialiasingOn) {
                    cx += STROKE_X_BIAS;
                }
                /*
                 * Draw exterior outline
                 */
                PathSink filler = getFiller();

                if (EEQ(w,h))
                    this.emitOval(filler, cx, cy, wl, hl, np, false);
                else
                    this.emitOffsetOval(filler, cx, cy, w2, h2, lineWidth2,
                                        np, false);

                /*
                 * Draw interior in the reverse direction
                 */
                if (hollow) {
                    wl = (w2 - lineWidth2);
                    hl = (h2 - lineWidth2);
            
                    if (wl > 0 && hl > 0) {
                        if (EEQ(w,h))
                            this.emitOval(filler, cx, cy, wl, hl, np, true);
                        else 
                            this.emitOffsetOval(filler, cx, cy, w2, h2, lineWidth2,
                                                np, true);
                    }
                }
                filler.end();
                this.endRendering();//this.rdr.endRendering();
            }
        }
    }
    private void emitArc(PathSink consumer,
                         double cx, double cy, double rx, double ry,
                         double startAngle, double endAngle,
                         int np)
    {
        final double np1 = (np - 1);
        final double delta = (endAngle - startAngle);
        final double dp1 = (delta/np1);

        for (int cc = 0; cc < np; cc++) {

            double theta = startAngle + cc*dp1;

            double ox = Math.cos(theta);
            double oy = Math.sin(theta);

            double lx = cx + (rx*ox);
            double ly = cy - (ry*oy);
            if (0 == cc) 
                consumer.moveTo(lx, ly);
            else 
                consumer.lineTo(lx, ly);
        }
    }
    private void fillOrDrawRoundRect(double x, double y, double w, double h,
                                     double aw, double ah,
                                     boolean stroke)
    {
        PathSink consumer = (stroke)?(this.getStroker()):(this.getFiller());

        if (aw < 0)
            aw = -aw;

        if (aw > w)
            aw = w;

        if (ah < 0)
            ah = -ah;

        if (ah > h)
            ah = h;
        /*
         * If stroking but not dashing, draw the outer and inner
         * contours explicitly as round rects
         *
         * Note - this only works if aw == ah since the result of
         * tracing a circle with a circular pen is a larger circle,
         * but the result of tracing an ellipse with a circular pen is
         * not (generally) an ellipse...
         */
        if (stroke && dashArray == null && EEQ(aw,ah)) {

            double lineWidth2 = lineWidth/2.0;

            this.beginRendering(RendererBase.WIND_NON_ZERO);
            {
                PathSink filler = getFiller();

                x += STROKE_X_BIAS;
                y += STROKE_Y_BIAS;

                emitRoundRect(filler,
                              x - lineWidth2, y - lineWidth2,
                              w + lineWidth, h + lineWidth,
                              aw + lineWidth, ah + lineWidth, false);
                /*
                 * Empty out inner rect
                 */
                w -= lineWidth;
                h -= lineWidth;

                if (w > 0 && h > 0) {
                    this.emitRoundRect(filler,
                                       x + lineWidth2, y + lineWidth2,
                                       w, h,
                                       aw - lineWidth, ah - lineWidth, true);
                }
            }
            this.endRendering();//this.rdr.endRendering();
        }
        else {
            this.beginRendering(RendererBase.WIND_NON_ZERO);
            {
                this.emitRoundRect(consumer, x, y, w, h, aw, ah, false);
            }
            this.endRendering();//this.rdr.endRendering();
        }
    }
    private void emitRoundRect(PathSink consumer,
                               double x, double y, double w, double h, 
                               double aw, double ah,
                               boolean reverse)
    {
        double xw = x + w;
        double yh = y + h;

        double aw2 = aw/2.0;
        double ah2 = ah/2.0;
        double acvaw = (acv*aw);
        double acvah = (acv*ah);
        double xacvaw = x + acvaw;
        double xw_acvaw = xw - acvaw;
        double yacvah = y + acvah;
        double yh_acvah = yh - acvah;
        double xaw2 = x + aw2;
        double xw_aw2 = xw - aw2;
        double yah2 = y + ah2;
        double yh_ah2 = yh - ah2;
        
        consumer.moveTo(x, yah2);
        if (reverse) {
            consumer.cubicTo(x, yacvah, xacvaw, y, xaw2, y);
            consumer.lineTo(xw_aw2, y);
            consumer.cubicTo(xw_acvaw, y, xw, yacvah, xw, yah2);
            consumer.lineTo(xw, yh_ah2);
            consumer.cubicTo(xw, yh_acvah, xw_acvaw, yh, xw_aw2, yh);
            consumer.lineTo(xaw2, yh);
            consumer.cubicTo(xacvaw, yh, x, yh_acvah, x, yh_ah2);
        }
        else {
            consumer.lineTo(x, yh_ah2);
            consumer.cubicTo(x, yh_acvah, xacvaw, yh, xaw2, yh);
            consumer.lineTo(xw_aw2, yh);
            consumer.cubicTo(xw_acvaw, yh, xw, yh_acvah, xw, yh_ah2);
            consumer.lineTo(xw, yah2);
            consumer.cubicTo(xw, yacvah, xw_acvaw, y, xw_aw2, y);
            consumer.lineTo(xaw2, y);
            consumer.cubicTo(xacvaw, y, x, yacvah, x, yah2);
        }
        consumer.close();
        consumer.end();
    }
    private void renderPath(byte[] commands,
                            double[] coords,
                            int windingRule)
    {
        this.beginRendering(windingRule);

        final int ncmd = commands.length;
        final int ncor = coords.length;
        final int ncor1 = (ncor-1);
        final int ncor3 = (ncor-3);
        final int ncor5 = (ncor-5);
        int cor = 0;

        for (int cmd = 0; cmd < ncmd; cmd++) {

            switch (commands[cmd]) {
            case COMMAND_MOVE_TO:
                if (cor < ncor1) {

                    this.moveTo(coords[cor++],coords[cor++]);
                }
                break;

            case COMMAND_LINE_TO:
                if (cor < ncor1) {

                    this.lineTo(coords[cor++],coords[cor++]);
                }
                break;

            case COMMAND_QUAD_TO:
                if (cor < ncor3) {

                    this.quadTo(coords[cor++],coords[cor++],
                                coords[cor++],coords[cor++]);
                }
                break;

            case COMMAND_CUBIC_TO:
                if (cor < ncor5) {

                    this.cubicTo(coords[cor++],coords[cor++],
                                 coords[cor++],coords[cor++],
                                 coords[cor++],coords[cor++]);
                }
                break;

            case COMMAND_CLOSE:
                this.close();
                break;
            }
        }
        this.endRendering();
    }

}
