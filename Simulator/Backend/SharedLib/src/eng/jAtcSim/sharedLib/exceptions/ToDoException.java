package eng.jAtcSim.sharedLib.exceptions;

public class ToDoException extends RuntimeException {
  public ToDoException() {
    super("To-Do implement");
  }

  public ToDoException(String message) {
    super("To-Do implement: " + message);
  }
}
