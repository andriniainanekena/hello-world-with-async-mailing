package api.poja.app.service.exception;

public class InvalidEmailException extends RuntimeException {

  public InvalidEmailException(String email) {
    super("Invalid email address: " + email);
  }
}
