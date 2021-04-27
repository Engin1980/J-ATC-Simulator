package eng.jAtcSim.newPacks.layout;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.layouting.Panel;
import eng.jAtcSim.layouting.Window;
import eng.jAtcSim.layouting.*;
import eng.jAtcSim.newPacks.IView;
import eng.jAtcSim.newPacks.ViewFactory;

import javax.swing.*;
import java.awt.*;

public class JFrameFactory {

  private static final boolean COLORIZE_PANELS = false;

  public JFrameInfo buildFrame(Layout layout, String frameName) {
    Window window = layout.getWindows()
            .tryGetFirst(q -> q.getTitle().equals(frameName))
            .orElseThrow(() -> new ApplicationException("Unable to find frame titled " + frameName));
    JFrameInfo ret = buildFrame(window, new ESet<>());
    return ret;
  }

  public IList<JFrameInfo> buildFrames(Layout layout) {
    EAssert.Argument.isNotNull(layout, "layout");

    IList<JFrameInfo> ret = new EList<>();
    for (Window window : layout.getWindows()) {
      if (window.getStyle() == Window.WindowStyle.hidden) continue;
      JFrameInfo frameInfo = buildFrame(window, layout.getWindows().select(q -> q.getTitle()));
      ret.add(frameInfo);
    }

    return ret;
  }

  private JFrameInfo buildFrame(Window window, ISet<String> windowNames) {
    EAssert.Argument.isNotNull(window, "window");
    JFrame frame = new JFrame();
    MenuFactory.MenuSimProxy menuSimProxy;

    frame.setTitle(window.getTitle() + " [jAtcSim]");

    if (window.isWithMenu()) {
      menuSimProxy = MenuFactory.buildMenu(frame, windowNames);
    } else
      menuSimProxy = null;

    switch (window.getStyle()) {
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

    IList<JPanelInfo> panelInfos = new EList<>();

    buildContent((JPanel) frame.getContentPane(), window.getContent(), panelInfos);

    JFrameInfo ret = new JFrameInfo(frame, panelInfos, menuSimProxy);

    if (window.isOnCloseQuit())
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

  private void buildContent(JPanel pane, Block content, IList<JPanelInfo> panelInfos) {
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

  private void buildPanelContent(JPanel pane, Panel content, IList<JPanelInfo> panelInfos) {
    String viewName = content.getView();
    pane.setName(viewName);
    boolean focus = content.isFocus();
    IView view = ViewFactory.getView(viewName);
    panelInfos.add(new JPanelInfo(pane, view, focus, content.getOptions()));
  }

  private void buildPanelContent(JPanel parent, ColumnList columns, IList<JPanelInfo> panelInfos) {
    Value[] blocks = columns.getColumns().select(q -> q.getWidth()).toArray(Value.class);
    parent.setLayout(new ColRowLayoutManager(Orientation.columns, parent.getHeight(), blocks));
    for (Column column : columns.getColumns()) {
      JPanel pane = new JPanel();
      if (COLORIZE_PANELS) pane.setBackground(ColorProvider.nextColor());
      buildContent(pane, column.getContent(), panelInfos);
      parent.add(pane);
    }
  }

  private void buildPanelContent(JPanel parent, RowList rows, IList<JPanelInfo> panelInfos) {
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


