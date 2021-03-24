package eng.jAtcSim.layouting;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;
import eng.eSystem.validation.EAssert;

import javax.swing.*;
import java.awt.*;

public class JFrameFactory {

  public static final boolean COLORIZE_PANELS  = false;

  public static class JFrameInfo {
    private final JFrame frame;
    private final ISet<JPanelInfo> panels;
    private final MenuFactory.MenuSimProxy menuSimProxy;

    public JFrameInfo(JFrame frame, ISet<JPanelInfo> panels, MenuFactory.MenuSimProxy menuSimProxy) {
      EAssert.Argument.isNotNull(frame, "frame");
      EAssert.Argument.isNotNull(panels, "panels");

      this.frame = frame;
      this.panels = panels;
      this.menuSimProxy = menuSimProxy;
    }

    public JFrame getFrame() {
      return frame;
    }

    public ISet<JPanelInfo> getPanels() {
      return panels;
    }

    public MenuFactory.MenuSimProxy getMenuSimProxy() {
      return menuSimProxy;
    }
  }

  public static class JPanelInfo {
    private final JPanel panel;
    private final String viewName;

    public JPanelInfo(JPanel panel, String viewName) {
      EAssert.Argument.isNotNull(panel, "panel");
      EAssert.Argument.isNonemptyString(viewName, "viewName");

      this.panel = panel;
      this.viewName = viewName;
    }

    public JPanel getPanel() {
      return panel;
    }

    public String getViewName() {
      return viewName;
    }
  }

  public ISet<JFrameInfo> build(Layout layout) {
    EAssert.Argument.isNotNull(layout, "layout");

    ISet<JFrameInfo> ret = new ESet<>();
    for (Window window : layout.getWindows()) {
      JFrameInfo frameInfo = buildFrame(window);
      ret.add(frameInfo);
    }

    return ret;
  }

  private JFrameInfo buildFrame(Window window) {
    EAssert.Argument.isNotNull(window, "window");
    JFrame frame = new JFrame();
    MenuFactory.MenuSimProxy menuSimProxy;

    frame.setTitle(window.getTitle() + " [jAtcSim]");

    if (window.isWithMenu()) {
      menuSimProxy = MenuFactory.buildMenu(frame);
    } else
      menuSimProxy = null;

    switch (window.getStyle()){
      case maximized:
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        break;
      case minimized:
        frame.setExtendedState(Frame.ICONIFIED);
        break;
    }

    Rectangle rect = convertPositionToFrameRectangle(window.getPosition());
    frame.setLocation(rect.getLocation());
    frame.setSize(rect.getSize());

    ISet<JPanelInfo> panelInfos = new ESet<>();

    buildContent((JPanel) frame.getContentPane(), window.getContent(), panelInfos);

    JFrameInfo ret = new JFrameInfo(frame, panelInfos, menuSimProxy);

    return ret;
  }

  private Rectangle convertPositionToFrameRectangle(Position position) {
    EAssert.Argument.isNotNull(position, "position");

    Dimension monitorResolution = getMonitorResolution(position.getMonitor());

    int x = position.getX().convertValueToInt(monitorResolution.width);
    int y = position.getY().convertValueToInt(monitorResolution.height);
    int width = position.getWidth().convertValueToInt(monitorResolution.width);
    int height = position.getHeight().convertValueToInt(monitorResolution.height);

    Rectangle ret = new Rectangle(x, y, width, height);
    return ret;
  }

  private void buildContent(JPanel pane, Block content, ISet<JPanelInfo> panelInfos) {
    if (content instanceof Panel)
      buildPanelContent(pane, (Panel) content, panelInfos);
    else if (content instanceof ColumnList)
      buildPanelContent(pane, (ColumnList) content, panelInfos);
    else if (content instanceof RowList)
      buildPanelContent(pane, (RowList) content, panelInfos);
    else if (content instanceof EmptyBlock) {
      // intentionally blank
    } else
      throw new UnsupportedOperationException();
  }

  private void buildPanelContent(JPanel pane, Panel content, ISet<JPanelInfo> panelInfos) {
    panelInfos.add(new JPanelInfo(pane, content.getView()));
  }

  private void buildPanelContent(JPanel parent, ColumnList columns, ISet<JPanelInfo> panelInfos) {
    Value[] blocks = columns.getColumns().select(q -> q.getWidth()).toArray(Value.class);
    parent.setLayout(new ColRowLayoutManager(Orientation.columns, parent.getHeight(), blocks));
    for (Column column : columns.getColumns()) {
      JPanel pane = new JPanel();
      if (COLORIZE_PANELS) pane.setBackground(ColorProvider.nextColor());
      buildContent(pane, column.getContent(), panelInfos);
      parent.add(pane);
    }
  }

  private void buildPanelContent(JPanel parent, RowList rows, ISet<JPanelInfo> panelInfos) {
    Value[] blocks = rows.getRows().select(q -> q.getHeight()).toArray(Value.class);
    parent.setLayout(new ColRowLayoutManager(Orientation.rows, parent.getHeight(), blocks));

    for (Row row : rows.getRows()) {
      JPanel pane = new JPanel();
      if (COLORIZE_PANELS) pane.setBackground(ColorProvider.nextColor());
      buildContent(pane, row.getContent(), panelInfos);
      parent.add(pane);
    }
  }

  private Dimension getMonitorResolution(Integer monitorIndex) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] graphicsDevices = ge.getScreenDevices();
    if (monitorIndex == null) monitorIndex = 0;
    else if (monitorIndex >= graphicsDevices.length) monitorIndex = graphicsDevices.length - 1;
    GraphicsDevice graphicsDevice = graphicsDevices[monitorIndex];
    DisplayMode displayMode = graphicsDevice.getDisplayMode();
    Dimension ret = new Dimension(
            displayMode.getWidth(),
            displayMode.getHeight());
    return ret;
  }
}

class ColorProvider {
  private static Color[] colors = {
          Color.CYAN,
          Color.BLUE,
          Color.DARK_GRAY,
          Color.GREEN,
          Color.MAGENTA,
          Color.ORANGE,
          Color.PINK,
          Color.RED,
          Color.WHITE,
          Color.YELLOW
  };
  private static int index = 0;

  public static Color nextColor() {
    Color ret = colors[index];
    index++;
    if (index >= colors.length) index = 0;
    return ret;
  }
}
