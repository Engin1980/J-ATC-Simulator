package eng.jAtcSim.layouting;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;
import eng.eSystem.validation.EAssert;

import javax.swing.*;
import java.awt.*;

public class JFrameFactory {

  public static class JFrameInfo {
    private final JFrame frame;
    private final ISet<JPanelInfo> panels;

    public JFrameInfo(JFrame frame, ISet<JPanelInfo> panels) {
      EAssert.Argument.isNotNull(frame, "frame");
      EAssert.Argument.isNotNull(panels, "panels");

      this.frame = frame;
      this.panels = panels;
    }

    public JFrame getFrame() {
      return frame;
    }

    public ISet<JPanelInfo> getPanels() {
      return panels;
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

    frame.setTitle(window.getTitle());

    Rectangle rect = convertPositionToRectangle(window.getPosition());
    frame.setLocation(rect.getLocation());
    frame.setSize(rect.getSize());

    ISet<JPanelInfo> panelInfos = new ESet<>();

    buildContent((JPanel) frame.getContentPane(), window.getContent(), panelInfos);

    JFrameInfo ret = new JFrameInfo(frame, panelInfos);

    return ret;
  }

  private void buildContent(JPanel pane, Block content, ISet<JPanelInfo> panelInfos) {
    if (content instanceof Panel)
      buildPanelContent(pane, (Panel) content, panelInfos);
    else if (content instanceof ColumnList)
      buildPanelContent(pane, (ColumnList) content, panelInfos);
    else if (content instanceof RowList)
      buildPanelContent(pane, (RowList) content, panelInfos);
    else
      throw new UnsupportedOperationException();
  }

  private void buildPanelContent(JPanel pane, Panel content, ISet<JPanelInfo> panelInfos) {
    panelInfos.add(new JPanelInfo(pane, content.getView()));
  }

  private void buildPanelContent(JPanel parent, ColumnList columns, ISet<JPanelInfo> panelInfos) {
    UsedRange ur = new UsedRange(parent.getWidth());
    ur.use(columns.getColumns().select(q -> q.getWidth()));

    int x = 0;

    for (Column column : columns.getColumns()) {
      JPanel pane = new JPanel();

      int y = 0;
      int width = ur.getWidth(column.getWidth());
      int height = pane.getHeight();

      pane.setLocation(x, y);
      pane.setSize(width, height);

      buildContent(pane, column.getContent(), panelInfos);

      x += width;
    }
  }

  private void buildPanelContent(JPanel parent, RowList rows, ISet<JPanelInfo> panelInfos) {
    UsedRange ur = new UsedRange(parent.getWidth());
    ur.use(rows.getRows().select(q -> q.getHeight()));

    int y = 0;

    for (Row row : rows.getRows()) {
      JPanel pane = new JPanel();

      int x = 0;
      int width = pane.getWidth();
      int height = ur.getWidth(row.getHeight());

      pane.setLocation(x, y);
      pane.setSize(width, height);

      buildContent(pane, row.getContent(), panelInfos);

      y += height;
    }
  }

  private Rectangle convertPositionToRectangle(Position position) {
    EAssert.Argument.isNotNull(position, "position");

    Dimension monitorResolution = getMonitorResolution(position.getMonitor());
    UsedRange urw = new UsedRange(monitorResolution.width);
    UsedRange urh = new UsedRange(monitorResolution.height);

    int x = convertValueToInteger(position.getX(), urw);
    int y = convertValueToInteger(position.getY(), urh);
    int width = convertValueToInteger(position.getWidth(), urw);
    int height = convertValueToInteger(position.getHeight(), urh);

    Rectangle ret = new Rectangle(x, y, width, height);
    return ret;
  }

  private int convertValueToInteger(Value value, UsedRange ur) {
    int ret;
    if (value instanceof PixelValue)
      ret = ((PixelValue) value).getValue();
    else if (value instanceof WildValue)
      ret = Math.max(ur.getWildWidth(), 1);
    else if (value instanceof PercentageValue)
      ret = ur.getPercentageWidth(((PercentageValue) value).getValue());
    else
      throw new UnsupportedOperationException("Unknown value type.");

    return ret;
  }

  private Dimension getMonitorResolution(Integer monitorIndex) {
    if (monitorIndex == 0) monitorIndex = 0;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] graphicsDevices = ge.getScreenDevices();
    GraphicsDevice graphicsDevice = graphicsDevices[monitorIndex];
    DisplayMode displayMode = graphicsDevice.getDisplayMode();
    Dimension ret = new Dimension(
            displayMode.getWidth(),
            displayMode.getHeight());
    return ret;
  }
}
