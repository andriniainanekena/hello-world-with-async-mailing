package api.poja.app.service.exception;

public class UnsupportedImageTypeException extends RuntimeException {

  public UnsupportedImageTypeException(String detectedType) {
    super("Unsupported file type: " + detectedType + ". Only PNG and JPEG are accepted.");
  }
}
