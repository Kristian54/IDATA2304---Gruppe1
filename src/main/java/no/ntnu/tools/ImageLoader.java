package no.ntnu.tools;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageLoader {
  /**
   * Load an image from a file, convert it to a byte stream, and create a JavaFX Image object.
   *
   * @param filePath The path to the JPG file.
   * @return JavaFX Image object.
   * @throws IOException If an error occurs while reading the file.
   */
  public static Image loadImageFromFile(String filePath) {
    try {
      File imageFile = new File(filePath);
      byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
      ByteArrayInputStream byteStream = new ByteArrayInputStream(imageBytes);
      return new Image(byteStream);
    } catch (IOException e) {
      System.err.println("Error loading image from file: " + filePath);
      e.printStackTrace();
      return null;
    }

  }

}
