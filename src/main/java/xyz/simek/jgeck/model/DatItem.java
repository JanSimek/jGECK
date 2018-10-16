package xyz.simek.jgeck.model;

import java.io.IOException;
import java.util.zip.DataFormatException;

public class DatItem {
	private String name;
	private int nameLength;
	private boolean compressed;
	private int unpackedSize;
	private int packedSize;
	private int offset;

	byte[] data;

	private DatFile datFile;

	public DatItem() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNameLength() {
		return nameLength;
	}

	public void setNameLength(int nameLength) {
		this.nameLength = nameLength;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public int getUnpackedSize() {
		return unpackedSize;
	}

	public void setUnpackedSize(int unpackedSize) {
		this.unpackedSize = unpackedSize;
	}

	public int getPackedSize() {
		return packedSize;
	}

	public void setPackedSize(int packedSize) {
		this.packedSize = packedSize;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public byte[] getData() throws DataFormatException, IOException {
		if(data != null)
			return data;
		
		setData(datFile.getItemData(this));
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public DatFile getDatFile() {
		return datFile;
	}

	public void setDatFile(DatFile datFile) {
		this.datFile = datFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (compressed ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + nameLength;
		result = prime * result + offset;
		result = prime * result + packedSize;
		result = prime * result + unpackedSize;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatItem other = (DatItem) obj;
		if (compressed != other.compressed)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nameLength != other.nameLength)
			return false;
		if (offset != other.offset)
			return false;
		if (packedSize != other.packedSize)
			return false;
		if (unpackedSize != other.unpackedSize)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}
}
