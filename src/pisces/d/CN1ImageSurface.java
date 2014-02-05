/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pisces.d;

import com.codename1.ui.RGBImage;

/**
 *
 * @author shannah
 */
public class CN1ImageSurface implements Surface {
    RGBImage image;
    
    public CN1ImageSurface(com.codename1.ui.Image img){
        if ( img instanceof RGBImage ){
            image = (RGBImage)img;
        } else {
            image = new RGBImage(img);
        }
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void getRGB(int[] argb, int offset, int scanLength, int x, int y, int width, int height) {
        int[] rgb = image.getRGBCached();
        
        copy(   argb, offset, scanLength, 
                rgb, y*getWidth()+x, getWidth(), width, height);
        
    }

    public void setRGB(int[] argb, int offset, int scanLength, int x, int y, int width, int height) {
        int[] rgb = image.getRGB();
        copy(   rgb, y*getWidth()+x, getWidth(),
                argb, offset, scanLength, width, height);
    }
    
    private static void copy(int[] dstRGB, int dstOffset, int dstScanLength,
                             int[] srcRGB, int srcOffset, int srcScanLength,
                             int width, int height)
    {
        int srcScanRest = srcScanLength - width;
        int dstScanRest = dstScanLength - width;

        for (; height > 0; --height) {

            for (int w = width; w > 0; --w) {

                dstRGB[dstOffset++] = srcRGB[srcOffset++];
            }
            srcOffset += srcScanRest;
            dstOffset += dstScanRest;
        }
    }
    
}
