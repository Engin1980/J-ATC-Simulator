package eng.jAtcSim.newLib.speeches.system;

import eng.eSystem.validation.EAssert;

public class StringMessage implements ISystemNotification {

  private final String message;

  public StringMessage(String message) {
    EAssert.Argument.isNonemptyString(message, "message");
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
