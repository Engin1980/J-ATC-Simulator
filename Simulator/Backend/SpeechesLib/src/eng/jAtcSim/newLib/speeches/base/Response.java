package eng.jAtcSim.newLib.speeches.base;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public abstract class Response<T> {
  private final T origin;

  public Response(T origin) {
    this.origin = origin;
    PostContracts.register(this, () -> this.origin != null);
  }

  public T getOrigin() {
    return origin;
  }
}
