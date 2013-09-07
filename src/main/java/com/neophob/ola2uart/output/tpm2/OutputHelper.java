/**
 * Copyright (C) 2011-2013 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.ola2uart.output.tpm2;


/**
 * Output Helper Class
 * Contains some common helper methods used by the output devices 
 *
 * @author michu
 */
public class OutputHelper {

	private OutputHelper() {
		//no instance allowed
	}

	/**
	 * Convert internal buffer to 24bit byte buffer, using colorformat.
	 *
	 * @param data the data
	 * @param colorFormat the color format
	 * @return the byte[]
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public static byte[] convertBufferTo24bit(int[] data) throws IllegalArgumentException {
		int targetBuffersize = data.length;

		int[] r = new int[targetBuffersize];
		int[] g = new int[targetBuffersize];
		int[] b = new int[targetBuffersize];

		splitUpBuffers(targetBuffersize, data, r, g, b);

		int ofs=0;
		byte[] buffer = new byte[targetBuffersize*3];
		for (int i=0; i<targetBuffersize; i++) {
			buffer[ofs++] = (byte)r[i];
			buffer[ofs++] = (byte)g[i];
			buffer[ofs++] = (byte)b[i];
		}

		return buffer;
	}


	/**
	 * convert the int buffer in byte buffers, respecting the color order
	 * 
	 * @param targetBuffersize
	 * @param data
	 * @param colorFormat
	 * @param r
	 * @param g
	 * @param b
	 */
	private static void splitUpBuffers(int targetBuffersize, int[] data, int[] r, int[] g, int[] b) {
		int ofs = 0;
		int tmp;
		for (int n=0; n<targetBuffersize; n++) {
			//one int contains the rgb color
			tmp = data[ofs];

			r[ofs] = (int) ((tmp>>16) & 255);
			g[ofs] = (int) ((tmp>>8)  & 255);
			b[ofs] = (int) ( tmp      & 255);                       

			ofs++;
		}       
	}


}
