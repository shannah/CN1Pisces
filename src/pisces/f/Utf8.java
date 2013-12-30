/*
 * Copyright (C) 1998, 2009  John Pritchard and the Alto Project Group.
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
package pisces.f ;

/**
 * <p> This class contains static tools for doing UTF-8 encoding and
 * decoding.</p>
 *
 * <p> UTF-8 is ASCII- transparent.  It supports character sets
 * requiring more than the seven bit ASCII base range of UTF-8,
 * including Unicode, ISO-8859, ISO-10646, etc..</p>
 * 
 * <p> ISO UCS code signature is not implemented.  </p>
 *
 * @author John Pritchard (jdp@syntelos)
 * @since 1.1
 */
public abstract class Utf8 {

    /**
     * Decode UTF-8 input, terminates decoding at a null character,
     * value 0x0.
     * 
     * @exception alto.sys.Error.State Bad format.
     */
    public final static char[] decode( byte[] code){

        if ( null == code) return null;

        return decode(code,0,code.length);
    }
    /**
     * Decode UTF-8 input, terminates decoding at a null character,
     * value 0x0.
     * 
     * @exception alto.sys.Error.State Bad format.
     */
    public final static char[] decode( byte[] code, int off, int many){

        if ( null == code || 0 >= code.length) 
            return null;
        else {
            StringBuilder strbuf = new StringBuilder();
            int trm = (off+many);
            int ch, ch2, ch3;
            char tmpc;
            for ( int cc = off; cc < trm; ){
                ch = (code[cc]&0xff);
                switch (ch >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    cc += 1;
                    tmpc = (char)ch; // for debugging
                    strbuf.append(tmpc);
                    break;
                case 12: 
                case 13:
                    cc += 2;
                    if (cc > trm)
                        throw new IllegalStateException();
                    else {
                        ch2 = (int) (code[cc-1]&0xff);
                        if (0x80 != (ch2 & 0xC0))
                            throw new IllegalStateException();
                        else {
                            tmpc = (char)(((ch & 0x1F) <<6)|(ch2 & 0x3F));
                            strbuf.append(tmpc);
                        }
                    }
                    break;
                case 14:
                    cc += 3;
                    if (cc > trm)
                        throw new IllegalStateException();
                    else {
                        ch2 = (code[cc-2]&0xff);
                        ch3 = (code[cc-1]&0xff);
                        if ((0x80 != (ch2 & 0xC0)) || (0x80 != (ch3 & 0xC0)))
                            throw new IllegalStateException();
                        else {
                            tmpc = (char)(((ch  & 0x0F) << 12)|
                                          ((ch2 & 0x3F) << 6) |
                                          ((ch3 & 0x3F) << 0));
                            strbuf.append(tmpc);
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException();
                }
            }
            return strbuf.toString().toCharArray();
        }
    }

    /**
     * Encode string in UTF-8.
     */
    public final static byte[] encode( char[] str){
        if ( null == str) 
            return null;
        else {
            int strlen = str.length;
            if (1 > strlen)
                return null;
            else {
                byte[] re = new byte[strlen];
                char ch, sch;
                for ( int cc = 0, bc = 0; cc < strlen; cc++){
                    ch = str[cc];
                    if (0x80 > ch)
                        re[bc++] = (byte)ch;
                    else if (0x07FF < ch){
                        int nlen = re.length+2;
                        byte[] copier = new byte[nlen];
                        System.arraycopy(re,0,copier,0,bc);
                        re = copier;
                        re[bc++] = (byte)(0xE0 | ((ch >> 12) & 0x0F));
                        re[bc++] = (byte)(0x80 | ((ch >>  6) & 0x3F));
                        re[bc++] = (byte)(0x80 | (ch & 0x3F));
                    }
                    else {
                        int nlen = re.length+1;
                        byte[] copier = new byte[nlen];
                        System.arraycopy(re,0,copier,0,bc);
                        re = copier;
                        re[bc++] = (byte)(0xC0 | ((ch >>  6) & 0x1F));
                        re[bc++] = (byte)(0x80 | (ch & 0x3F));
                    }
                }
                return re;
            }
        }
    }

    /**
     * Encode string in UTF-8.
     */
    public final static byte[] encode ( String s){
        if ( null == s)
            return null;
        else 
            return encode(s.toCharArray());
    }

    /**
     * Returns the length of the string encoded in UTF-8.
     */
    public final static int encoded ( String s){

        if ( null == s)
            return 0;
        else
            return encoded(s.toCharArray());
    }

    /**
     * Returns the length of the string encoded in UTF-8.
     */
    public final static int encoded( char[] str){

        if ( null == str || 0 >= str.length) return 0;

        int bytlen = 0;

        char ch, sch;

        for ( int c = 0; c < str.length; c++){

            ch = str[c];

            if (  0x7f >= ch)

                bytlen++;

            else if ( 0x7ff >= ch)

                bytlen += 2;

            else 
                bytlen += 3;

        }

        return bytlen;
    }

}
