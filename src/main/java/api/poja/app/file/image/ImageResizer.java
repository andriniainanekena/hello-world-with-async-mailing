package api.poja.app.file.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class ImageResizer {

  public static final int THUMBNAIL_SIZE = 256;
  private static final String OUTPUT_FORMAT = "png";

  @SneakyThrows
  public File resizeTo256(File source) {
    BufferedImage original = ImageIO.read(source);
    if (original == null) {
      throw new IOException("Unreadable image file: " + source.getName());
    }

    BufferedImage resized =
        new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = resized.createGraphics();
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.drawImage(original, 0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE, null);
    graphics.dispose();

    File output = File.createTempFile("thumbnail-", "." + OUTPUT_FORMAT);
    ImageIO.write(resized, OUTPUT_FORMAT, output);
    return output;
  }
}
