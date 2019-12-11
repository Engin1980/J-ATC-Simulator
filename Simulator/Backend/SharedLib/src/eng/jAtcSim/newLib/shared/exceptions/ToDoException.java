package eng.jAtcSim.newLib.shared.exceptions;

public class ToDoException extends RuntimeException {
  public ToDoException() {
    super("To-Do implement");
  }

  public ToDoException(String message) {
    super("To-Do implement: " + message);
  }
}
