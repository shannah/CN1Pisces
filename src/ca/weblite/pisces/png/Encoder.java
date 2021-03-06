/*
 * Pisces PNG Encoder
 * Copyright (C) 2010 John Pritchard
 * Copyright 2003 J. David Eisenberg
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.  The copyright
 * holders designate particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this
 * code.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public
 * License version 2 along with this work; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * A copy of the GNU LGPL may be found at
 * http://www.gnu.org/copyleft/lesser.html
 */
package ca.weblite.pisces.png;

import ca.weblite.pisces.d.Surface;
import com.codename1.io.gzip.CRC32;
import com.codename1.io.gzip.Deflater;
import com.codename1.io.gzip.DeflaterOutputStream;
import com.codename1.io.gzip.GZIPException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.util.zip.CRC32;
//import java.util.zip.Deflater;
//import java.util.zip.DeflaterOutputStream;

/**
 * PNG Encoder adapted to pisces graphics.
 *
 * @author J. David Eisenberg, david@catcode.com
 * @version 1.5, 19 Oct 2003
 */
public class Encoder extends Object {

    /** Constant specifying that alpha channel should be encoded. */
    public static final boolean ENCODE_ALPHA = true;

    /** Constant specifying that alpha channel should not be encoded. */
    public static final boolean NO_ALPHA = false;

    /** Constants for filter (NONE) */
    public static final int FILTER_NONE = 0;

    /** Constants for filter (SUB) */
    public static final int FILTER_SUB = 1;

    /** Constants for filter (UP) */
    public static final int FILTER_UP = 2;

    /** Constants for filter (LAST) */
    public static final int FILTER_LAST = 2;
    
    /** IHDR tag. */
    protected static final byte IHDR[] = {73, 72, 68, 82};
    
    /** IDAT tag. */
    protected static final byte IDAT[] = {73, 68, 65, 84};
    
    /** IEND tag. */
    protected static final byte IEND[] = {73, 69, 78, 68};

    /** The png bytes. */
    protected byte[] pngBytes;

    /** The prior row. */
    protected byte[] priorRow;

    /** The left bytes. */
    protected byte[] leftBytes;

    /** The image. */
    protected Surface surface;

    /** The width. */
    protected int width, height;

    /** The byte position. */
    protected int bytePos, maxPos;

    /** CRC. */
    protected CRC32 crc = new CRC32();

    /** The CRC value. */
    protected long crcValue;

    /** Encode alpha? */
    protected boolean encodeAlpha;

    /** The filter type. */
    protected int filter;

    /** The bytes-per-pixel. */
    protected int bytesPerPixel;

    /** The compression level. */
    protected int compressionLevel;


    public Encoder(Surface surface) {
        this(surface, false, FILTER_NONE, 0);
    }
    public Encoder(Surface surface, boolean encodeAlpha) {
        this(surface, encodeAlpha, FILTER_NONE, 0);
    }
    public Encoder(Surface surface, boolean encodeAlpha, int whichFilter) {
        this(surface, encodeAlpha, whichFilter, 0);
    }
    public Encoder(Surface surface, boolean encodeAlpha, int whichFilter, int compLevel) {
        super();
        if (null != surface){
            this.surface = surface;
            this.encodeAlpha = encodeAlpha;
            this.setFilter(whichFilter);
            if (compLevel >= 0 && compLevel <= 9) {
                this.compressionLevel = compLevel;
            }
        }
        else
            throw new IllegalArgumentException("Missing surface");
    }


    public byte[] encode() throws IOException {
        return this.encode(this.encodeAlpha);
    }
    /**
     * @param encodeAlpha boolean false=no alpha, true=encode alpha
     * @return an array of bytes, or null if there was a problem
     */
    public byte[] encode(boolean encodeAlpha) throws IOException {
        byte[]  pngIdBytes = {-119, 80, 78, 71, 13, 10, 26, 10};

        if (surface == null) {
            return null;
        }
        width = surface.getWidth();
        height = surface.getHeight();

        /*
         * start with an array that is big enough to hold all the pixels
         * (plus filter bytes), and an extra 200 bytes for header info
         */
        pngBytes = new byte[((width + 1) * height * 3) + 200];

        /*
         * keep track of largest byte written to the array
         */
        maxPos = 0;

        bytePos = writeBytes(pngIdBytes, 0);
        //hdrPos = bytePos;
        writeHeader();
        //dataPos = bytePos;
        if (writeImageData()) {
            writeEnd();
            pngBytes = Resize(pngBytes, maxPos);
        }
        else {
            pngBytes = null;
        }
        return pngBytes;
    }
    public void setEncodeAlpha(boolean encodeAlpha) {
        this.encodeAlpha = encodeAlpha;
    }
    public boolean getEncodeAlpha() {
        return encodeAlpha;
    }
    public void setFilter(int whichFilter) {
        this.filter = FILTER_NONE;
        if (whichFilter <= FILTER_LAST) {
            this.filter = whichFilter;
        }
    }
    public int getFilter() {
        return filter;
    }
    public void setCompressionLevel(int level) {
        if (level >= 0 && level <= 9) {
            this.compressionLevel = level;
        }
    }
    public int getCompressionLevel() {
        return compressionLevel;
    }


    /**
     * Write an array of bytes into the pngBytes array.
     * Note: This routine has the side effect of updating
     * maxPos, the largest element written in the array.
     * The array is resized by 1000 bytes or the length
     * of the data to be written, whichever is larger.
     *
     * @param data The data to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeBytes(byte[] data, int offset) {
        maxPos = Math.max(maxPos, offset + data.length);
        if (data.length + offset > pngBytes.length) {
            pngBytes = Resize(pngBytes, pngBytes.length + Math.max(1000, data.length));
        }
        System.arraycopy(data, 0, pngBytes, offset, data.length);
        return offset + data.length;
    }

    /**
     * Write an array of bytes into the pngBytes array, specifying number of bytes to write.
     * Note: This routine has the side effect of updating
     * maxPos, the largest element written in the array.
     * The array is resized by 1000 bytes or the length
     * of the data to be written, whichever is larger.
     *
     * @param data The data to be written into pngBytes.
     * @param nBytes The number of bytes to be written.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeBytes(byte[] data, int nBytes, int offset) {
        maxPos = Math.max(maxPos, offset + nBytes);
        if (nBytes + offset > pngBytes.length) {
            pngBytes = Resize(pngBytes, pngBytes.length + Math.max(1000, nBytes));
        }
        System.arraycopy(data, 0, pngBytes, offset, nBytes);
        return offset + nBytes;
    }

    /**
     * Write a two-byte integer into the pngBytes array at a given position.
     *
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeInt2(int n, int offset) {
        byte[] temp = {(byte) ((n >> 8) & 0xff), (byte) (n & 0xff)};
        return writeBytes(temp, offset);
    }

    /**
     * Write a four-byte integer into the pngBytes array at a given position.
     *
     * @param n The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeInt4(int n, int offset) {
        byte[] temp = {(byte) ((n >> 24) & 0xff),
                       (byte) ((n >> 16) & 0xff),
                       (byte) ((n >> 8) & 0xff),
                       (byte) (n & 0xff)};
        return writeBytes(temp, offset);
    }

    /**
     * Write a single byte into the pngBytes array at a given position.
     *
     * @param b The integer to be written into pngBytes.
     * @param offset The starting point to write to.
     * @return The next place to be written to in the pngBytes array.
     */
    protected int writeByte(int b, int offset) {
        byte[] temp = {(byte) b};
        return writeBytes(temp, offset);
    }

    /**
     * Write a PNG "IHDR" chunk into the pngBytes array.
     */
    protected void writeHeader() {
        int startPos;

        startPos = bytePos = writeInt4(13, bytePos);
        bytePos = writeBytes(IHDR, bytePos);
        width = surface.getWidth();
        height = surface.getHeight();
        bytePos = writeInt4(width, bytePos);
        bytePos = writeInt4(height, bytePos);
        bytePos = writeByte(8, bytePos); // bit depth
        bytePos = writeByte((encodeAlpha) ? 6 : 2, bytePos); // direct model
        bytePos = writeByte(0, bytePos); // compression method
        bytePos = writeByte(0, bytePos); // filter method
        bytePos = writeByte(0, bytePos); // no interlace
        crc.reset();
        crc.update(pngBytes, startPos, bytePos - startPos);
        crcValue = crc.getValue();
        bytePos = writeInt4((int) crcValue, bytePos);
    }

    /**
     * Perform "sub" filtering on the given row.
     * Uses temporary array leftBytes to store the original values
     * of the previous pixels.  The array is 16 bytes long, which
     * will easily hold two-byte samples plus two-byte alpha.
     *
     * @param pixels The array holding the scan lines being built
     * @param startPos Starting position within pixels of bytes to be filtered.
     * @param width Width of a scanline in pixels.
     */
    protected void filterSub(byte[] pixels, int startPos, int width) {
        int i;
        int offset = bytesPerPixel;
        int actualStart = startPos + offset;
        int nBytes = width * bytesPerPixel;
        int leftInsert = offset;
        int leftExtract = 0;

        for (i = actualStart; i < startPos + nBytes; i++) {
            leftBytes[leftInsert] =  pixels[i];
            pixels[i] = (byte) ((pixels[i] - leftBytes[leftExtract]) % 256);
            leftInsert = (leftInsert + 1) % 0x0f;
            leftExtract = (leftExtract + 1) % 0x0f;
        }
    }

    /**
     * Perform "up" filtering on the given row.
     * Side effect: refills the prior row with current row
     *
     * @param pixels The array holding the scan lines being built
     * @param startPos Starting position within pixels of bytes to be filtered.
     * @param width Width of a scanline in pixels.
     */
    protected void filterUp(byte[] pixels, int startPos, int width) {
        int     i, nBytes;
        byte    currentByte;

        nBytes = width * bytesPerPixel;

        for (i = 0; i < nBytes; i++) {
            currentByte = pixels[startPos + i];
            pixels[startPos + i] = (byte) ((pixels[startPos  + i] - priorRow[i]) % 256);
            priorRow[i] = currentByte;
        }
    }

    /**
     * Write the image data into the pngBytes array.
     * This will write one or more PNG "IDAT" chunks. In order
     * to conserve memory, this method grabs as many rows as will
     * fit into 32K bytes, or the whole image; whichever is less.
     *
     *
     * @return true if no errors; false if error grabbing pixels
     */
    protected boolean writeImageData() throws GZIPException, IOException {
        final int width = this.width;

        int rowsLeft = height;  // number of rows remaining to write
        int startRow = 0;       // starting row to process this time through
        int nRows;              // how many rows to grab at a time

        byte[] scanLines;       // the scan lines to be compressed
        int scanPos;            // where we are in the scan lines
        int startPos;           // where this line's actual pixels start (used for filtering)

        byte[] compressedLines; // the resultant compressed lines
        int nCompressed;        // how big is the compressed area?

        //int depth;              // color depth ( handle only 8 or 32 )

        bytesPerPixel = (encodeAlpha) ? 4 : 3;

        Deflater scrunch = new Deflater(compressionLevel);
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream(1024);

        DeflaterOutputStream compBytes = new DeflaterOutputStream(outBytes, scrunch);
        try {
            while (rowsLeft > 0) {
                nRows = Math.min(32767 / (width * (bytesPerPixel + 1)), rowsLeft);
                nRows = Math.max( nRows, 1 );

                int[] pixels = new int[width * nRows];

                surface.getRGB(pixels, 0, width,
                               0, startRow, width, nRows);
                /*
                 * Create a data chunk. scanLines adds "nRows" for
                 * the filter bytes.
                 */
                scanLines = new byte[width * nRows * bytesPerPixel +  nRows];

                if (filter == FILTER_SUB) {
                    leftBytes = new byte[16];
                }
                if (filter == FILTER_UP) {
                    priorRow = new byte[width * bytesPerPixel];
                }

                scanPos = 0;
                startPos = 1;
                for (int i = 0; i < width * nRows; i++) {
                    if (i % width == 0) {
                        scanLines[scanPos++] = (byte) filter;
                        startPos = scanPos;
                    }
                    scanLines[scanPos++] = (byte) ((pixels[i] >> 16) & 0xff);
                    scanLines[scanPos++] = (byte) ((pixels[i] >>  8) & 0xff);
                    scanLines[scanPos++] = (byte) ((pixels[i]) & 0xff);
                    if (encodeAlpha) {
                        scanLines[scanPos++] = (byte) ((pixels[i] >> 24) & 0xff);
                    }
                    if ((i % width == width - 1) && (filter != FILTER_NONE)) {
                        if (filter == FILTER_SUB) {
                            filterSub(scanLines, startPos, width);
                        }
                        if (filter == FILTER_UP) {
                            filterUp(scanLines, startPos, width);
                        }
                    }
                }

                /*
                 * Write these lines to the output area
                 */
                compBytes.write(scanLines, 0, scanPos);

                startRow += nRows;
                rowsLeft -= nRows;
            }
            compBytes.close();

            /*
             * Write the compressed bytes
             */
            compressedLines = outBytes.toByteArray();
            nCompressed = compressedLines.length;

            crc.reset();
            bytePos = writeInt4(nCompressed, bytePos);
            bytePos = writeBytes(IDAT, bytePos);
            
            crc.update(IDAT, 0, IDAT.length);
            bytePos = writeBytes(compressedLines, nCompressed, bytePos);
            crc.update(compressedLines, 0, nCompressed);

            crcValue = crc.getValue();
            bytePos = writeInt4((int) crcValue, bytePos);
            scrunch.end();
            //scrunch.finish();
            return true;
        }
        catch (IOException e) {
            System.err.println(e.toString());
            return false;
        }
    }

    /**
     * Write a PNG "IEND" chunk into the pngBytes array.
     */
    protected void writeEnd() {
        bytePos = writeInt4(0, bytePos);
        bytePos = writeBytes(IEND, bytePos);
        crc.reset();
        crc.update(IEND, 0, IEND.length);
        crcValue = crc.getValue();
        bytePos = writeInt4((int) crcValue, bytePos);
    }
    protected static byte[] Resize(byte[] array, int newLength) {
        byte[]  newArray = new byte[newLength];
        int     oldLength = array.length;

        System.arraycopy(array, 0, newArray, 0, Math.min(oldLength, newLength));
        return newArray;
    }

}
