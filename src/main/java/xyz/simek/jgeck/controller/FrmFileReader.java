package xyz.simek.jgeck.controller;

import java.io.DataInputStream;
import java.io.IOException;

import xyz.simek.jgeck.model.format.frm.FrmFrame;
import xyz.simek.jgeck.model.format.frm.FrmHeader;

public class FrmFileReader {

	public static FrmHeader readFrm(DataInputStream dis) {
		FrmHeader header = new FrmHeader();
		try {
			readHeader(header, dis);
			readFrames(header, dis);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return header;
	}

	private static void readHeader(FrmHeader header, DataInputStream dis) throws IOException {
		readVersion(header, dis);
		readFramesPerSecond(header, dis);
		readActionFrame(header, dis);
		readFramesPerDirection(header, dis);
		readDirectionSwitchX(header, dis);
		readDirectionSwitchY(header, dis);
		readDirectionDataOffset(header, dis);
		readFrameDataSize(header, dis);
	}

	private static void readFrames(FrmHeader header, DataInputStream dis) throws IOException {
		while (dis.available() > 0) {
			FrmFrame frame = new FrmFrame();
			readFrameWidth(frame, dis);
			readFrameHeight(frame, dis);
			readFrameDataSize(frame, dis);
			readFrameOffsetX(frame, dis);
			readFrameOffsetY(frame, dis);
			readFrameData(frame, dis);
			header.addFrame(frame);
		}
	}

	private static void readFrameData(FrmFrame frame, DataInputStream dis) throws IOException {
		byte data[] = new byte[frame.getDataSize()];
		dis.read(data, 0, frame.getDataSize());
		frame.setData(data);
	}

	private static void readFrameOffsetY(FrmFrame frame, DataInputStream dis) throws IOException {
		frame.setOffsetY((short) dis.readShort());
	}

	private static void readFrameOffsetX(FrmFrame frame, DataInputStream dis) throws IOException {
		frame.setOffsetX((short) dis.readShort());
	}

	private static void readFrameDataSize(FrmFrame frame, DataInputStream dis) throws IOException {
		frame.setDataSize(dis.readInt());
	}

	private static void readFrameHeight(FrmFrame frame, DataInputStream dis) throws IOException {
		frame.setHeight((short) dis.readUnsignedShort());
	}

	private static void readFrameWidth(FrmFrame frame, DataInputStream dis) throws IOException {
		frame.setWidth((short) dis.readUnsignedShort());
	}

	private static void readFrameDataSize(FrmHeader header, DataInputStream dis) throws IOException {
		header.setFrameDataSize(dis.readInt());
	}

	private static void readVersion(FrmHeader header, DataInputStream dis) throws IOException {
		header.setVersion(dis.readInt());
	}

	private static void readFramesPerSecond(FrmHeader header, DataInputStream dis) throws IOException {
		header.setFramesPerSecond((short) dis.readUnsignedShort());
	}

	private static void readActionFrame(FrmHeader header, DataInputStream dis) throws IOException {
		header.setActionFrame((short) dis.readUnsignedShort());
	}

	private static void readFramesPerDirection(FrmHeader header, DataInputStream dis) throws IOException {
		header.setFramesPerDirection((short) dis.readUnsignedShort());
	}

	private static void readDirectionSwitchX(FrmHeader header, DataInputStream dis) throws IOException {
		for (int i = 0; i < 6; i++) {
			header.setDirectionSwitchX(i, dis.readShort());
		}
	}

	private static void readDirectionSwitchY(FrmHeader header, DataInputStream dis) throws IOException {
		for (int i = 0; i < 6; i++) {
			header.setDirectionSwitchY(i, dis.readShort());
		}
	}

	private static void readDirectionDataOffset(FrmHeader header, DataInputStream dis) throws IOException {
		for (int i = 0; i < 6; i++) {
			header.setDirectionDataOffset(i, dis.readInt());
		}
	}
}