/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pisces.d;

/**
 *
 * @author shannah
 */
public class Rectangle2D {
    public double x;
    public double y;
    public double width;
    public double height;
    
    public Rectangle2D(double x, double y, double width, double height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
    }
    
    public double getMaxX(){
        return x+width;
    }
    
    public double getMaxY(){
        return y+height;
    }
    
    public Rectangle2D union(Rectangle2D rect){
        double x = Math.min(rect.x, this.x);
        double y = Math.max(rect.y, this.y);
        double x1 = Math.max(rect.getMaxX(), this.getMaxX());
        double y1 = Math.max(rect.getMaxY(), this.getMaxY());
        return new Rectangle2D(x, y, x1-x, y1-y);
    }
    
    public Rectangle2D(Rectangle2D rect){
        x = rect.x;
        y = rect.y;
        width = rect.width;
        height = rect.height;
    }
    
    public String toString(){
        return "["+x+","+y+","+width+","+height+"]";
    }
}
