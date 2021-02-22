package eng.jAtcSim.layouting;

import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.collections.ISet;
import eng.eSystem.validation.EAssert;

public class Layout {
  private final ISet<Window> windows;

  public Layout(ISet<Window> windows) {
    EAssert.Argument.isNotNull(windows, "windows");
    this.windows = windows;
  }

  IReadOnlySet<Window> getWindows() {
    return windows;
  }
}
