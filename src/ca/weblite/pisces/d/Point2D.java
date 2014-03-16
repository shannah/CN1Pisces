/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.pisces.d;

/**
 *
 * @author shannah
 */
public class Point2D {
    public double x;
    public double y;
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
    
    public Point2D(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    public void setX(double x){
        this.x = x;
    }
    
    public void setY(double y){
        this.y = y;
    }
}
