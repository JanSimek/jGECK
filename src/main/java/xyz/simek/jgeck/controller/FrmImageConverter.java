package xyz.simek.jgeck.controller;

import javafx.scene.image.Image;
import xyz.simek.jgeck.model.format.frm.ColorCycleOffset;
import xyz.simek.jgeck.model.format.frm.FoPalette;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author SlowhandFastfeet
 * @see <a href="https://github.com/SlowhandFastfeet/FEV">FEV project</a>
 */
public class FrmImageConverter {

    public static Image getJavaFXImage(byte[] rawPixels, int width, int height, int offset, boolean hasBackground) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write((RenderedImage) createBufferedImage(rawPixels, width, height, offset, null, hasBackground),
                    "png", out);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new Image(in);
    }

    public static Image getJavaFXImageWithColorCycle(byte[] rawPixels, int width, int height, int offset,
                                                     ColorCycleOffset colorCycleOffset, boolean hasBackground) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write((RenderedImage) createBufferedImage(rawPixels, width, height, offset, colorCycleOffset,
                    hasBackground), "png", out);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new Image(in);

    }

    private static BufferedImage createBufferedImage(byte[] pixels, int width, int height, int offset,
                                                     ColorCycleOffset cco, boolean hasBackground) {
        SampleModel sm = getIndexSampleModel(width, height, cco, hasBackground);
        DataBuffer db = new DataBufferByte(pixels, width * height * 2, offset);
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        IndexColorModel cm;
        if (cco == null) {
            cm = FoPalette.getDefaultColorModel(hasBackground);
        } else {
            cm = FoPalette.getAnimatedDefaultColorModel(cco, hasBackground);
        }
        BufferedImage image = new BufferedImage(cm, raster, false, null);
        return image;
    }

    private static SampleModel getIndexSampleModel(int width, int height, ColorCycleOffset cco, boolean hasBackground) {
        IndexColorModel icm;
        if (cco == null) {
            icm = FoPalette.getDefaultColorModel(hasBackground);
        } else {
            icm = FoPalette.getAnimatedDefaultColorModel(cco, hasBackground);
        }
        WritableRaster wr = icm.createCompatibleWritableRaster(1, 1);
        SampleModel sampleModel = wr.getSampleModel();
        sampleModel = sampleModel.createCompatibleSampleModel(width, height);

        return sampleModel;
    }
}