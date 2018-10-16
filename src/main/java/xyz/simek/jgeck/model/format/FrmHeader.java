package xyz.simek.jgeck.model.format;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import xyz.simek.jgeck.controller.FrmImageConverter;

/**
 * @author SlowhandFastfeet
 * @see <a href="https://github.com/SlowhandFastfeet/FEV">FEV project</a>
 */
public class FrmHeader {
	protected short framesPerSecond; // unsigned
	protected short framesPerDirection; // unsigned
	protected List<FrmFrame> frames;

	private int version; // unsigned
	private short actionFrame; // unsigned
	private short directionSwitchX[]; // signed, size 6
	private short directionSwitchY[]; // signed, size 6
	private int directionDataOffset[]; // signed, size 6
	private int frameDataSize; // unsigned

	public FrmHeader() {
		frames = new ArrayList<FrmFrame>();
		directionSwitchX = new short[6];
		directionSwitchY = new short[6];
		directionDataOffset = new int[6];
		frames = new ArrayList<FrmFrame>();
	}

	public short getFramesPerSecond() {
		return framesPerSecond;
	}

	public void setFramesPerSecond(short framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}

	public short getFramesPerDirection() {
		return framesPerDirection;
	}

	public void setFramesPerDirection(short framesPerDirection) {
		this.framesPerDirection = framesPerDirection;
	}

	public void addFrame(FrmFrame frame) {
		frames.add(frame);
	}

	public FrmFrame getFrame(int index) {
		if (frames == null) {
			throw new NullPointerException("Frames list is not initialized.");
		}
		if (index < 0 || index >= frames.size()) {
			throw new IndexOutOfBoundsException(
					"Cannot get index " + index + " for frame list. Curent size of list is: " + frames.size());
		}
		return frames.get(index);
	}

	public int getTotalFrames() {
		return frames.size();
	}

	public int getVersion() {
		return version;
	}

	public int setVersion(int version) {
		this.version = version;
		return version;
	}

	public short getActionFrame() {
		return actionFrame;
	}

	public void setActionFrame(short actionFrame) {
		this.actionFrame = actionFrame;
	}

	public short getDirectionSwitchX(int index) {
		if (index < 0 || index > 5) {
			throw new IndexOutOfBoundsException(
					"Cannot get index " + index + " for direction switch X array. Must be between 0 and 5.");
		}
		return directionSwitchX[index];
	}

	public void setDirectionSwitchX(int index, short value) {
		if (index < 0 || index > 5) {
			throw new IndexOutOfBoundsException(
					"Cannot set index " + index + " for direction switch X array. Must be between 0 and 5.");
		}
		this.directionSwitchX[index] = value;
	}

	public short getDirectionSwitchY(int index) {
		if (index < 0 || index > 5) {
			throw new IndexOutOfBoundsException(
					"Cannot get index " + index + " for direction switch Y array. Must be between 0 and 5.");
		}
		return directionSwitchY[index];
	}

	public void setDirectionSwitchY(int index, short value) {
		if (index < 0 || index > 5) {
			throw new IndexOutOfBoundsException(
					"Cannot set index " + index + " for direction switch Y array. Must be between 0 and 5.");
		}
		this.directionSwitchY[index] = value;
	}

	public int getDirectionDataOffset(int index) {
		if (index < 0 || index > 5) {
			throw new IndexOutOfBoundsException(
					"Cannot get index " + index + " for direction data offset array. Must be between 0 and 5.");
		}
		return directionDataOffset[index];
	}

	public void setDirectionDataOffset(int index, int value) {
		if (index < 0 || index > 5) {
			throw new IndexOutOfBoundsException(
					"Cannot set index " + index + " for direction data offset array. Must be between 0 and 5.");
		}
		this.directionDataOffset[index] = value;
	}

	public int getFrameDataSize() {
		return frameDataSize;
	}

	public void setFrameDataSize(int frameDataSize) {
		this.frameDataSize = frameDataSize;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Format: frm" + System.lineSeparator());
		sb.append("Version: " + getVersion() + System.lineSeparator());
		sb.append("Frames per second: " + getFramesPerSecond() + System.lineSeparator());
		sb.append("Action frame index: " + getActionFrame() + System.lineSeparator());
		sb.append("Frames per direction: " + getFramesPerDirection() + System.lineSeparator());
		sb.append("DirectionSwitchX: ");
		for (int i = 0; i < 6; i++) {
			sb.append(getDirectionSwitchX(i) + " ");
		}
		sb.append(System.lineSeparator());
		sb.append("DirectionSwitchY: ");
		for (int i = 0; i < 6; i++) {
			sb.append(getDirectionSwitchY(i) + " ");
		}
		sb.append(System.lineSeparator());
		sb.append("DirectionDataOffset: ");
		for (int i = 0; i < 6; i++) {
			sb.append(getDirectionDataOffset(i) + " ");
		}
		sb.append(System.lineSeparator());
		sb.append("FrameDataSize: " + getFrameDataSize() + System.lineSeparator());
		sb.append("Total number of frames: " + frames.size());
		return sb.toString();
	}

	public Image getImage(int direction, int frameIndex, boolean hasBackground) throws Exception {
		int frameOffset = 0;
		FrmFrame frame = frames.get(direction * framesPerDirection + frameIndex);
		int width = frame.getWidth();
		int height = frame.getHeight();
		byte[] data = frame.getData();
		if (frameIndex >= 0 && (frameIndex + 1) * width * height <= data.length) {
			frameOffset = frameIndex * width * height;
		}
		return FrmImageConverter.getJavaFXImage(data, width, height, frameOffset, hasBackground);
	}
}
