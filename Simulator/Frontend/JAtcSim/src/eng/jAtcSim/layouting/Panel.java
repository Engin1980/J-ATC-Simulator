package eng.jAtcSim.layouting;

import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.validation.EAssert;

import static eng.eSystem.utilites.FunctionShortcuts.coalesce;

public class Panel extends Block {
  private final String id;
  private final String view;
  private final boolean focus;
  private final IReadOnlyMap<String, String> options;

  public Panel(String view, String id, boolean focus, IMap<String, String> options) {
    EAssert.Argument.isNotNull(view, "view");
    EAssert.Argument.isNotNull(options, "options");
    this.view = view;
    this.id = coalesce(id, view);
    this.focus = focus;
    this.options = options;
  }

  public boolean isFocus() {
    return focus;
  }

  public String getId() {
    return id;
  }

  public String getView() {
    return view;
  }

  public IReadOnlyMap<String, String> getOptions() {
    return options;
  }
}
