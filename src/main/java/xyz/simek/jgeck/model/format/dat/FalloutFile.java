package xyz.simek.jgeck.model.format.dat;

import xyz.simek.jgeck.model.DatFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DataFormatException;

public class FalloutFile implements FalloutDatItem {
    private String filename;
    private int nameLength;
    private boolean compressed;
    private int unpackedSize;
    private int packedSize;
    private int offset;

    private byte[] data;

    private DatFile datFile;

    public FalloutFile() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
        if (data != null)
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FalloutFile that = (FalloutFile) o;
        return nameLength == that.nameLength &&
                compressed == that.compressed &&
                unpackedSize == that.unpackedSize &&
                packedSize == that.packedSize &&
                offset == that.offset &&
                Objects.equals(filename, that.filename) &&
                Arrays.equals(data, that.data) &&
                Objects.equals(datFile, that.datFile);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(filename, nameLength, compressed, unpackedSize, packedSize, offset, datFile);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {

        if (filename.contains("\\")) {
            return filename.substring(filename.lastIndexOf("\\") + 1);
        } else {
            return filename;
        }
    }
}
