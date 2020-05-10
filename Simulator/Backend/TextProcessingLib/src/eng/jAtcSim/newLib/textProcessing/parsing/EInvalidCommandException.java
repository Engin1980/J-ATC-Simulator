package eng.jAtcSim.newLib.textProcessing.parsing;

public class EInvalidCommandException extends RuntimeException {

  public EInvalidCommandException(String message, String processedCommandLinePart, String errorCommandLinePart) {
    super(message + " (processed: " + processedCommandLinePart + "; failed command: " + errorCommandLinePart + ")");
  }
  public EInvalidCommandException(String message, String errorCommandLinePart) {
    super(message + " (failed command: " + errorCommandLinePart + ")");
  }

}
