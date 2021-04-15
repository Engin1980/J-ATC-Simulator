package eng.jAtcSim.newPacks.layout;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.layouting.MenuFactory;

import javax.swing.*;

public class JFrameInfo {
  private final JFrame frame;
  private final IReadOnlyList<JPanelInfo> panels;
  private final MenuFactory.MenuSimProxy menuSimProxy;

  public JFrameInfo(
          JFrame frame,
          IReadOnlyList<JPanelInfo> panels,
          MenuFactory.MenuSimProxy menuSimProxy) {
    EAssert.Argument.isNotNull(frame, "frame");
    EAssert.Argument.isNotNull(panels, "panels");

    this.frame = frame;
    this.panels = panels;
    this.menuSimProxy = menuSimProxy;
  }

  public JFrame getFrame() {
    return frame;
  }

  public MenuFactory.MenuSimProxy getMenuSimProxy() {
    return menuSimProxy;
  }

  public IReadOnlyList<JPanelInfo> getPanels() {
    return panels;
  }
}
