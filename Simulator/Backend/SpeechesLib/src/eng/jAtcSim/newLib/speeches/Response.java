package eng.jAtcSim.newLib.speeches;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;

public abstract class Response implements INotification {
  private ICommand origin;

  public Response(ICommand origin) {
    EAssert.Argument.isNotNull(origin);
    this.origin = origin;
  }

  public ICommand getOrigin() {
    return origin;
  }
}
