package eng.jAtcSim.newPacks.layout;

import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newPacks.IView;

import javax.swing.*;

public class JPanelInfo {
  private IView view;
  private final JPanel panel;
  private final IReadOnlyMap<String, String> options;

  public JPanelInfo(JPanel panel, IView view, IReadOnlyMap<String, String> options) {
    EAssert.Argument.isNotNull(panel, "panel");
    EAssert.Argument.isNotNull(view, "view");
    EAssert.Argument.isNotNull(options, "options");

    this.panel = panel;
    this.view = view;
    this.options = options;
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
