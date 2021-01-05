package exml;

public class SimPersistenceExeption extends RuntimeException {

  public SimPersistenceExeption(String message, Throwable cause) {
    super(message, cause);
  }

  public SimPersistenceExeption(String message) {
    super(message);
  }
}
