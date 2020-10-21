package eng.jAtcSim.newLib.speeches.system.system2user;

import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newLib.speeches.system.ISystemNotification;

public class ErrorNotification implements ISystemNotification {

  public enum ErrorType {
    callsignNotFound,
    multipleCallsignsFound
  }

  private final IMap<String, Object> arguments;
  private final ErrorType type;

  public ErrorNotification(ErrorType type, IMap<String, Object> arguments) {
    this.arguments = arguments;
    this.type = type;
  }

  public IReadOnlyMap<String, Object> getArguments() {
    return arguments;
  }

  public ErrorType getType() {
    return type;
  }
}
