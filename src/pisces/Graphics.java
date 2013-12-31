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

import java.io.IOException;
import pisces.d.NativeSurface;
import pisces.d.Pisces;
import pisces.d.Surface;
import pisces.m.Matrix;
import pisces.png.Encoder;

/**
 * Pisces user interface.
 * 
 * Most operations pass through the graphics pipeline including
 * stroking and filling and transformations.  
 * 
 * Blit operations skip the pipeline and pass directly into the pixel
 * blender and onto the surface (framebuffer).  The blit pixels
 * include transparency for blending into the surface, but subpixel
 * sampling for antialiasing is not performed.  
 * 
 * @see Path
 * @see Polygon
 */
public class Graphics
    extends Object
    implements Cloneable
{

    public final int width, height;

    protected final Surface.Sink surface;

    private Pisces renderer;

    private Font font;


    public Graphics(int w, int h){
        super();
        if (0 < w && 0 < h){
            this.width = w;
            this.height = h;
            this.surface = new NativeSurface(w,h);
            this.renderer = new Pisces(this.surface);
        }
        else
            throw new IllegalArgumentException();
    }
    public Graphics(Image img){
        super();
        if (null != img){
            this.width = img.getWidth();
            this.height = img.getHeight();
            this.surface = img;
            this.renderer = new Pisces(img);
        }
        else
            throw new IllegalArgumentException();
    }


    public com.codename1.ui.Image toImage(){
        int[] rgb = new int[this.width*this.height];
        this.surface.getRGB(rgb, 0, width, 0, 0, width, height);
        return com.codename1.ui.Image.createImage(rgb, width, height);
        
    }
    
    public final byte[] toPNG() throws IOException{

        Encoder png = new Encoder(this.surface);
        return png.encode();
    }
    public final byte[] toPNG(boolean alpha) throws IOException{

        Encoder png = new Encoder(this.surface,alpha);
        return png.encode();
    }
    public final byte[] toPNG(boolean alpha, int compression) throws IOException{

        Encoder png = new Encoder(this.surface,alpha,Encoder.FILTER_NONE,compression);
        return png.encode();
    }
    public final Graphics create(){
        return this.clone();
    }
    public final Graphics create(double x, double y, double w, double h){
        return this.create().setClip(x,y,w,h);
    }
    public void dispose(){
        Pisces r = this.renderer;
        if (null != r){
            this.renderer = null;
            r.dispose();
        }
        this.font = null;
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
     * @return Self for chaining.
     */
    public final Graphics setStroke(double lineWidth, int capStyle, int joinStyle, double miterLimit, double[] dashArray, double dashPhase){
        this.renderer.setStroke(lineWidth, capStyle, joinStyle, miterLimit, dashArray, dashPhase);
        return this;
    }
    
    public final Graphics setAntialiasing(boolean antialiasingOn) {
        this.renderer.setAntialiasing(antialiasingOn);
        return this;
    }
    public final boolean getAntialiasing() {
        return this.renderer.getAntialiasing();
    }
    public final Color getColor(){
        return this.renderer.getColor();
    }
    public final Graphics setColor(Color color){
        this.renderer.setColor(color);
        return this;
    }
    public final Font getFont(){
        return this.font;
    }
    public final Graphics setFont(Font font){
        this.font = font;
        return this;
    }
    public final Graphics setTransform(Matrix transform) {
        this.renderer.setTransform(transform);
        return this;
    }
    public final Matrix getTransform() {
        return this.renderer.getTransform();
    }
    public final Graphics setClip(double x, double y, double width, double height) {
        this.renderer.setClip(x, y, width, height);
        return this;
    }
    public final Graphics resetClip() {
        this.renderer.resetClip();
        return this;
    }
    public final Graphics setStroke(){
        this.renderer.setStroke();
        return this;
    }
    public final Graphics setFill(){
        this.renderer.setFill();
        return this;
    }
    /**
     * Begin path operations
     */
    public final Graphics beginRendering(int windingRule) {
        this.renderer.beginRendering(windingRule);
        return this;
    }
    public final Graphics moveTo(double x0, double y0) {
        this.renderer.moveTo(x0, y0);
        return this;
    }
    public final Graphics lineTo(double x1, double y1) {
        this.renderer.lineTo(x1, y1);
        return this;
    }
    public final Graphics lineJoin() {
        this.renderer.lineJoin();
        return this;
    }
    public final Graphics quadTo(double x1, double y1, double x2, double y2) {
        this.renderer.quadTo(x1, y1, x2, y2);
        return this;
    }
    public final Graphics cubicTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.renderer.cubicTo(x1, y1, x2, y2, x3, y3);
        return this;
    }
    /**
     * Close a path by emitting a line from the last point "pen down"
     * (e.g. last lineTo) to the last point "pen up" (last moveTo).
     */
    public final Graphics close() {
        this.renderer.close();
        return this;
    }
    /**
     * End path operations
     */
    public final Graphics endRendering() {
        this.renderer.endRendering();
        return this;
    }
    public final Graphics getBoundingBox(double[] bbox) {
        this.renderer.getBoundingBox(bbox);
        return this;
    }
    public final Graphics draw(Path p){
        if (null != p){
            this.setStroke();
            this.renderer.beginRendering(p.windingRule);
            p.produce(this.renderer);
            this.renderer.endRendering();
            return this;
        }
        else
            throw new IllegalArgumentException();
    }
    public final Graphics fill(Path p){
        if (null != p){
            this.setFill();
            this.renderer.beginRendering(p.windingRule);
            p.produce(this.renderer);
            this.renderer.endRendering();
            return this;
        }
        else
            throw new IllegalArgumentException();
    }
    public final Graphics blit(Surface image){
        return this.blit(image,0,0);
    }
    public final Graphics blit(Surface image, int x, int y){
        return this.blit(image,x,y,1.0f);
    }
    public final Graphics blit(Surface image, int x, int y, float opacity){
        return this.blit(image,0,0,x,y,opacity);
    }
    public final Graphics blit(Surface image, int srcX, int srcY, int dstX, int dstY, float opacity){
        return this.blit(image,srcX,srcY,dstX,dstY,image.getWidth(),image.getHeight(),opacity);
    }
    public final Graphics blit(Surface image, int srcX, int srcY, int dstX, int dstY, int w, int h, float opacity)
    {
        this.renderer.blit(image,srcX,srcY,dstX,dstY,w,h,opacity);
        return this;
    }
    /**
     * Bitmap font
     */
    public final Graphics blit(String string, int x, int y){
        return this.blit(string,x,y,1.0f);
    }
    /**
     * Bitmap font
     */
    public final Graphics blit(String string, int x, int y, float op){
        Font font = this.font;
        if (null != font){
            font.blit(this,string,x,y,op);
            return this;
        }
        else
            throw new IllegalStateException("Missing font");
    }
    /**
     * Vector font
     */
    public final Graphics draw(String string, int x, int y){
        return this.draw(string,x,y,1.0f);
    }
    /**
     * Vector font
     */
    public final Graphics draw(String string, int x, int y, float op){
        Font font = this.font;
        if (null != font){
            font.draw(this,string,x,y,op);
            return this;
        }
        else
            throw new IllegalStateException("Missing font");
    }
    public final Graphics drawLine(double x0, double y0, double x1, double y1) {
        this.renderer.drawLine(x0, y0, x1, y1);
        return this;
    }
    public final Graphics fillRect(double x, double y, double w, double h) {
        this.renderer.fillRect(x, y, w, h);
        return this;
    }
    public final Graphics drawRect(double x, double y, double w, double h) {
        this.renderer.drawRect(x, y, w, h);
        return this;
    }
    public final Graphics drawOval(double x, double y, double w, double h) {
        this.renderer.drawOval(x, y, w, h);
        return this;
    }
    public final Graphics fillOval(double x, double y, double w, double h) {
        this.renderer.fillOval(x, y, w, h);
        return this;
    }
    public final Graphics drawArc(double x, double y, double width, double height,
                                  double startAngle, double arcAngle, int arcType)
    {
        this.renderer.drawArc(x, y, width, height,
                              startAngle, arcAngle, arcType);
        return this;
    }
    public final Graphics fillArc(double x, double y, double width, double height,
                                  double startAngle, double arcAngle, int arcType)
    {
        this.renderer.fillArc(x, y, width, height,
                              startAngle, arcAngle, arcType);
        return this;
    }
    public final Graphics fillOrDrawArc(double x, double y, double width, double height,
                                        double startAngle, double arcAngle, int arcType,
                                        boolean stroke)
    {
        this.renderer.fillOrDrawArc(x, y, width, height,
                                    startAngle, arcAngle, arcType,
                                    stroke);
        return this;
    }
    public final Graphics clearRect(double x, double y, double w, double h) {
        this.renderer.clearRect(x, y, w, h);
        return this;
    }
    protected Graphics clone(){
        throw new RuntimeException("Clone not supported");
//        try {
//            Graphics clone = (Graphics)super.clone();
//            clone.renderer = this.renderer.clone();
//            return clone;
//        }
//        catch (CloneNotSupportedException err){
//            throw new InternalError();
//        }
    }
}
