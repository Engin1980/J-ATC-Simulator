package eng.jAtcSim.newPacks.layout;

import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newPacks.IView;

import javax.swing.*;

public class JPanelInfo {
  private final IView view;
  private final boolean focus;
  private final JPanel panel;
  private final IReadOnlyMap<String, String> options;

  public JPanelInfo(JPanel panel, IView view, boolean focus, IReadOnlyMap<String, String> options) {
    EAssert.Argument.isNotNull(panel, "panel");
    EAssert.Argument.isNotNull(view, "view");
    EAssert.Argument.isNotNull(options, "options");

    this.panel = panel;
    this.view = view;
    this.focus = focus;
    this.options = options;
  }

  public boolean isFocus() {
    return focus;
  }

  public IReadOnlyMap<String, String> getOptions() {
    return options;
  }

  public JPanel getPanel() {
    return panel;
  }

  public IView getView() {
    return view;
  }
}
