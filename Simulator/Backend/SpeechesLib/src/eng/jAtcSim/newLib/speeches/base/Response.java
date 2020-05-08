package eng.jAtcSim.newLib.speeches.base;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public abstract class Response<T> {
  private final T origin;

  public Response(T origin) {
    EAssert.Argument.isNotNull(origin, "origin");
    this.origin = origin;
  }

  public T getOrigin() {
    return origin;
  }
}
