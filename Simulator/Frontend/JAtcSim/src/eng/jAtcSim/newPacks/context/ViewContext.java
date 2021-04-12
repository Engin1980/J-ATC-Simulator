package eng.jAtcSim.newPacks.context;

import eng.eSystem.collections.ICollection;
import eng.jAtcSim.newPacks.IView;

public class ViewContext {
  public final Events events = new Events();
  public final ICollection<IView> views;

  public ViewContext(ICollection<IView> views) {
    this.views = views;
  }
}
