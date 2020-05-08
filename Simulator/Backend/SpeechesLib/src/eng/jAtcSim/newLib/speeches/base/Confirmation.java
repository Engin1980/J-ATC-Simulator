package eng.jAtcSim.newLib.speeches.base;

import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class Confirmation<T> extends Response<T> {
  public Confirmation(T origin) {
    super(origin);
  }
}
