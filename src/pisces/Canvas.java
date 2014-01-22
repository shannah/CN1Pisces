/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pisces;

import com.codename1.ui.Component;
import pisces.m.Matrix;

/**
 *
 * @author shannah
 */
public class Canvas extends Component {
    
    private Graphics surface;
    private Matrix transform;
    private Paint paint;
    
    public void setTransform(Matrix transform){
        this.transform = transform;
    }
    public Matrix getTransform(){
        return this.transform;
    }
    
    public void setPaint(Paint paint){
        this.paint = paint;
    }
    public Paint getPaint(){
        return this.paint;
    }
    
    public void draw(Path path){
        surface.draw(path);
    }
    
    public void draw(String str, int x, int y){
        
    }
    
}
