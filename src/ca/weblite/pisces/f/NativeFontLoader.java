/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.pisces.f;

import com.codename1.system.NativeInterface;

/**
 *
 * @author shannah
 */
public interface NativeFontLoader extends NativeInterface {
    
    public byte[] getFontData(String fontName);
    
}
