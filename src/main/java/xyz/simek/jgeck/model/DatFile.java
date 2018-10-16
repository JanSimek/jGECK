package xyz.simek.jgeck.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class DatFile {

	private String filename;
	private int savedSize;
	private int realSize;
	private int filetreeSize;
	private int totalFiles;

	private Map<String, DatItem> items = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private ByteBuffer buffer;

	public DatFile(String filename) throws IOException {

		this.filename = filename;
		
		init();
	}
	
	private void init() throws IOException {
		try (FileInputStream stream = new FileInputStream(getFilename())) {
			
			this.setRealSize((int) stream.getChannel().size());
			this.buffer = ByteBuffer.allocate(getRealSize());
			this.buffer.order(ByteOrder.LITTLE_ENDIAN);
			stream.getChannel().read(buffer);

			buffer.position(getRealSize() - 4);
			setSavedSize(buffer.getInt());
			System.out.println("Saved size:    " + getSavedSize());
			System.out.println("Real size:     " + getRealSize());

			buffer.position(getRealSize() - 8);
			setFiletreeSize(buffer.getInt());
			System.out.println("Filetree size: " + filetreeSize);

			buffer.position(getRealSize() - filetreeSize - 8);
			setTotalFiles(buffer.getInt());

			for (int i = 0; i < totalFiles; i++) {

				DatItem item = new DatItem();
				item.setNameLength(buffer.getInt());
				item.setName(getFromBuffer(item.getNameLength()));
				item.setCompressed(buffer.get() == 0 ? false : true);
				item.setUnpackedSize(buffer.getInt());
				item.setPackedSize(buffer.getInt());
				item.setOffset(buffer.getInt());
				item.setDatFile(this);

				items.put(item.getName(), item);
				System.out.println("item " + (i+1) + " of " + totalFiles);
			}
		}
	}
	
	public byte[] getItemData(DatItem item) throws DataFormatException, IOException {

		byte[] data = new byte[item.getPackedSize()];

		buffer.position(item.getOffset());
		buffer.get(data);

		if(!item.isCompressed())
			return data;
		
		Inflater decompresser = new Inflater(); 
		decompresser.setInput(data, 0, item.getPackedSize()); 
		byte[] result = new byte[item.getUnpackedSize()]; 
		int resultLength = decompresser.inflate(result); 
		
		if(resultLength != item.getUnpackedSize()) 
			System.err.println("WRONG SIZE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		decompresser.end();
		return result;
	}
	
	private String getFromBuffer(int bytes) {
		byte[] barray = new byte[bytes];
		this.buffer.get(barray);

		return new String(barray);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getSavedSize() {
		return savedSize;
	}

	public void setSavedSize(int savedSize) {
		this.savedSize = savedSize;
	}

	public int getRealSize() {
		return realSize;
	}

	public void setRealSize(int realSize) {
		this.realSize = realSize;
	}

	public int getFiletreeSize() {
		return filetreeSize;
	}

	public void setFiletreeSize(int filetreeSize) {
		this.filetreeSize = filetreeSize;
	}

	public int getTotalFiles() {
		return totalFiles;
	}

	public void setTotalFiles(int totalFiles) {
		this.totalFiles = totalFiles;
	}

	public Map<String, DatItem> getItems() {
		return items;
	}

	public void setItems(Map<String, DatItem> items) {
		this.items = items;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + filetreeSize;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + realSize;
		result = prime * result + savedSize;
		result = prime * result + totalFiles;
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
		DatFile other = (DatFile) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (filetreeSize != other.filetreeSize)
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (realSize != other.realSize)
			return false;
		if (savedSize != other.savedSize)
			return false;
		if (totalFiles != other.totalFiles)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getFilename();
	}
}
