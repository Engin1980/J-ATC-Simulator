package eng.jAtcSim.newLib.speeches.system.user2system;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class DeletePlaneRequest implements ISystemUserRequest {
  private final Squawk squawk;

  public DeletePlaneRequest(Squawk squawk) {
    this.squawk = squawk;
  }

  public Squawk getSquawk() {
    return squawk;
  }
}
