package controllers.utils;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Generate images thumbnails.
 */
public class ThumbnailGen {

    private static byte[] writeJpeg(BufferedImage image, float quality)
            throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        IIOImage iioImage = new IIOImage(image, null, null);
        writer.write(null, iioImage, param);
        if (writer != null) writer.dispose();
        baos.flush();
        return baos.toByteArray();
    }

    public static byte[] scaleImage(byte[] source, int width, int height)
            throws IOException {
        BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(source));
        ResampleOp resampleOp = new ResampleOp(width, height);
        resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
        resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
        BufferedImage destImage = resampleOp.filter(sourceImage, null);
        return writeJpeg(destImage, 0.9f);
    }
}