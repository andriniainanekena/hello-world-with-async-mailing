package api.poja.app.file.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageResizerTest {

  private final ImageResizer imageResizer = new ImageResizer();

  @Test
  void resizes_small_square_png_to_exactly_256x256() throws Exception {
    File source = createImage(20, 20, "png");

    File resized = imageResizer.resizeTo256(source);

    assertDimensions(resized, 256, 256);
  }

  @Test
  void resizes_large_wide_jpeg_to_exactly_256x256() throws Exception {
    File source = createImage(1600, 900, "jpg");

    File resized = imageResizer.resizeTo256(source);

    assertDimensions(resized, 256, 256);
  }

  @Test
  void resizes_tall_png_to_exactly_256x256() throws Exception {
    File source = createImage(100, 900, "png");

    File resized = imageResizer.resizeTo256(source);

    assertDimensions(resized, 256, 256);
  }

  @Test
  void resizes_already_256x256_image_to_exactly_256x256() throws Exception {
    File source = createImage(256, 256, "png");

    File resized = imageResizer.resizeTo256(source);

    assertDimensions(resized, 256, 256);
  }

  private File createImage(int width, int height, String format) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    File file = File.createTempFile("source-", "." + format);
    ImageIO.write(image, format, file);
    return file;
  }

  private void assertDimensions(File file, int expectedWidth, int expectedHeight) throws Exception {
    BufferedImage image = ImageIO.read(file);
    assertThat(image.getWidth()).isEqualTo(expectedWidth);
    assertThat(image.getHeight()).isEqualTo(expectedHeight);
  }
}
