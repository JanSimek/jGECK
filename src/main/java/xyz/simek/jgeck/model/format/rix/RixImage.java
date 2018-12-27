package xyz.simek.jgeck.model.format.rix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class RixImage {

	private byte[] signature = new byte[4];
	private short width;
	private short height;
	@SuppressWarnings("unused")
	private byte paletteType;
	@SuppressWarnings("unused")
	private byte storageType;
		
	private int[] data;
	
	public void read(ByteBuffer buff) {
		try {
			buff.order(ByteOrder.LITTLE_ENDIAN);

			buff.get(signature, 0, 4);
			if(!Arrays.equals(signature, "RIX3".getBytes()))
				throw new IOException("Invalid RIX format");

			width = buff.getShort();
			height = buff.getShort();
			
			paletteType = buff.get();
			storageType = buff.get();

			buff.order(ByteOrder.BIG_ENDIAN);
			
			// Palette
			int palette[] = new int[256];
			for(int i = 0; i != 256; i++) {
				byte r = buff.get();
				byte g = buff.get();
				byte b = buff.get();
				// Falltergeist RGBA:
				//palette[i] = (r << 26 | g << 18 | b << 10 | 0x000000FF);  // RGBA
				// FIXME: something is wrong, the resulting image has a yellow tint
				palette[i] = (0xFF << 24 | r << 16 | g << 8 | b);  // ARGB ?
				
				System.out.println("Palette: " + palette[i]);
			}
			
			// Image
			data = new int[width * height];
			for(int i = 0; i != (width * height); i++) {
				data[i] = palette[Byte.toUnsignedInt(buff.get())];
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public short getWidth() {
		return width;
	}

	public void setWidth(short width) {
		this.width = width;
	}

	public short getHeight() {
		return height;
	}

	public void setHeight(short height) {
		this.height = height;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

}
