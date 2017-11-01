package jatcsimlib.speaking.commands;

public abstract class CommandResponse {

  private final Command origin;

  public CommandResponse(Command origin) {
    if (origin == null) {
      throw new IllegalArgumentException("Value of {origin} cannot not be null.");
    }

    this.origin = origin;
  }

  public Command getOrigin() {
    return origin;
  }
}
