package eng.jAtcSim.shared;

import eng.eSystem.utilites.awt.ComponentUtils;

import javax.swing.*;
import java.awt.*;

public class LayoutManager {

  public interface Action {
    void apply(Component component);
  }

  public enum eHorizontalAlign {
    left,
    center,
    right
  }

  public enum eVerticalAlign {
    top,
    middle,
    bottom,
    baseline
  }

  public static void setFixedSize(Component component) {
    LayoutManager.setFixedSize(component, component.getPreferredSize());
  }

  public static void setFixedSize(Component component, Dimension dimension) {
    component.setMaximumSize(dimension);
    component.setPreferredSize(dimension);
    component.setMinimumSize(dimension);
  }

  public static void setFixedWidth(Component component) {
    setFixedWidth(component, (int) component.getPreferredSize().getWidth());
  }

  public static void setFixedWidth(Component component, int width) {
    component.setMaximumSize(
        new Dimension(
            width,
            (int) component.getMaximumSize().getHeight()));
    component.setPreferredSize(
        new Dimension(
        width,
        (int) component.getPreferredSize().getHeight()));
    component.setMinimumSize(
        new Dimension(
            width,
            (int) component.getMinimumSize().getHeight()));
  }

  public static void setFixedHeight(Component component) {
    setFixedHeight(component, (int) component.getPreferredSize().getHeight());
  }

  public static void setFixedHeight(Component component, int height) {
    component.setMaximumSize(
        new Dimension(
            (int) component.getMaximumSize().getWidth(),
            height));
    component.setPreferredSize(
        new Dimension(
            (int) component.getPreferredSize().getWidth(),
            height));
    component.setMinimumSize(
        new Dimension(
            (int) component.getMaximumSize().getWidth(),
            height));
  }

  public static void setPanelBorderText(JPanel pnl, String text) {
    pnl.setBorder(BorderFactory.createTitledBorder(text));
  }

  public static void adjustComponents(Component mostParentComponent, Action action) {
    action.apply(mostParentComponent);

    if (mostParentComponent instanceof Container) {
      Container container = (Container) mostParentComponent;
      for (Component component : container.getComponents()) {
        adjustComponents(component, action);
      }
    }
  }

  public static JPanel createFlowPanel(eVerticalAlign align, int distance, JComponent... components) {
    JPanel ret = new JPanel();
    fillFlowPanel(ret, align, distance, components);
    return ret;
  }

  public static JPanel createFlowPanel(JComponent... components) {
    return createFlowPanel(eVerticalAlign.baseline, 4, components);
  }

  public static void fillFlowPanel(Container panel, eVerticalAlign align, int distance, JComponent... components) {
    BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
    panel.setLayout(layout);

    for (int i = 0; i < components.length; i++) {
      if (distance > 0 && i > 0) {
        panel.add(Box.createHorizontalStrut(distance));
      }
      JComponent component = components[i];
      switch (align) {
        case top:
          component.setAlignmentY(Component.TOP_ALIGNMENT);
          break;
        case middle:
          component.setAlignmentY(Component.CENTER_ALIGNMENT);
          break;
        case bottom:
          component.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        default:
      }
      panel.add(component);
    }
  }

  public static JPanel createBoxPanel(eHorizontalAlign align, int distance, JComponent... components) {
    JPanel ret = new JPanel();
    fillBoxPanel(ret, align, distance, components);
    return ret;
  }

  public static JPanel createBoxPanel(JComponent... components) {
    return createBoxPanel(eHorizontalAlign.left, 4, components);
  }

  public static void fillBoxPanel(Container panel, eHorizontalAlign align, int distance, JComponent... components) {
    BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
    panel.setLayout(layout);

    for (int i = 0; i < components.length; i++) {
      if (distance > 0 && i > 0) {
        panel.add(Box.createVerticalStrut(distance));
      }

      JComponent component = components[i];
      switch (align) {
        case center:
          component.setAlignmentX(Component.CENTER_ALIGNMENT);
          break;
        case left:
          component.setAlignmentX(Component.LEFT_ALIGNMENT);
          break;
        case right:
          component.setAlignmentX(Component.RIGHT_ALIGNMENT);
          break;
      }

      panel.add(component);
    }

  }

  public static JPanel createBorderedPanel(int distance, JComponent content) {
    JPanel panel = new JPanel();
    fillBorderedPanel(panel, distance, content);
    return panel;
  }

  public static JPanel createBorderedPanel(int distance) {
    JPanel panel = new JPanel();
    fillBorderedPanel(panel, distance, null);
    return panel;
  }

  public static void fillBorderedPanel(Container panel, int distance, JComponent content) {
    fillBorderedPanel(panel, distance, distance, distance, distance, content);
  }

  public static JPanel createBorderedPanel(int marginLeft, int marginTop, int marginRight, int marginBottom, Container content) {
    JPanel ret = new JPanel();
    fillBorderedPanel(ret, marginLeft, marginTop, marginRight, marginBottom, content);
    return ret;
  }

  public static void fillBorderedPanel(Container panel, int marginLeft, int marginTop, int marginRight, int marginBottom, Container content) {
    panel.setLayout(new BorderLayout());
    panel.add(Box.createVerticalStrut(marginTop), BorderLayout.PAGE_START);
    panel.add(Box.createVerticalStrut(marginBottom), BorderLayout.PAGE_END);
    panel.add(Box.createHorizontalStrut(marginLeft), BorderLayout.LINE_START);
    panel.add(Box.createHorizontalStrut(marginRight), BorderLayout.LINE_END);
    if (content != null)
      panel.add(content, BorderLayout.CENTER);
  }

  public static JPanel createBorderedPanel(Container top, Container bottom, Container left, Container right, Container content) {
    JPanel ret = new JPanel();
    fillBorderedPanel(ret, top, bottom, left, right, content);
    return ret;
  }

  public static void fillBorderedPanel(Container panel, Container top, Container bottom, Container left, Container right, Container content) {
    panel.setLayout(new BorderLayout());
    if (top != null)
      panel.add(top, BorderLayout.PAGE_START);
    if (bottom != null)
      panel.add(bottom, BorderLayout.PAGE_END);
    if (left != null)
      panel.add(left, BorderLayout.LINE_START);
    if (right != null)
      panel.add(right, BorderLayout.LINE_END);
    if (content != null)
      panel.add(content, BorderLayout.CENTER);
  }

  public static JPanel createGridPanel(int rowCount, int columnCount, int distance, Component... components) {
    JPanel panel = new JPanel();
    fillGridPanel(panel, rowCount, columnCount, distance, components);
    return panel;
  }

  public static void fillGridPanel(Container panel, int rowCount, int columnCount, int distance, Component... components) {
    panel.setLayout(new GridLayout(rowCount, columnCount, distance, distance));

    for (Component component : components) {
      panel.add(component);
    }

  }

  public static void fillBorderedPanel(JComponent panel, int rowCount, int columnCount, int distance, Component... components) {
    GridLayout layout = new GridLayout(rowCount, columnCount, distance, distance);
    panel.setLayout(layout);

    for (Component cmp : components) {
      if (cmp == null) {
        panel.add(new JLabel());
      } else {
        panel.add(cmp);
      }
    }
  }

  public static JPanel createFormPanel(Component[][] rows, Component[][] columns) {
    JPanel panel = new JPanel();
    fillFormPanel(panel, rows, columns);
    return panel;
  }

  public static void fillFormPanel(JComponent panel, Component[][] rows, Component[][] columns) {

    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);


    GroupLayout.SequentialGroup sequentialGroup;
    GroupLayout.ParallelGroup parallelGroup;

    // horizontal
    sequentialGroup = layout.createSequentialGroup();
    for (Component[] column : columns) {
      parallelGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
      for (Component component : column) {
        if (component != null)
          parallelGroup.addComponent(component);
      }
      sequentialGroup.addGroup(parallelGroup);
    }
    layout.setHorizontalGroup(sequentialGroup);

    // vertical
    sequentialGroup = layout.createSequentialGroup();
    for (Component[] row : rows) {
      parallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
      for (Component component : row) {
        if (component != null)
          parallelGroup.addComponent(component);
      }
      sequentialGroup.addGroup(parallelGroup);
    }
    layout.setVerticalGroup(sequentialGroup);
  }

  public static JPanel createFormPanel(int rowCount, int columnCount, Component... components) {
    JPanel panel = new JPanel();
    fillFormPanel(panel, rowCount, columnCount, components);
    return panel;
  }

  public static void fillFormPanel(JPanel panel, int rowCount, int columnCount, Component... components) {
    Component[][] rows = null;
    Component[][] columns = null;

    if (components.length != rowCount * columnCount)
      throw new IllegalArgumentException(
          String.format(
              "Component array length %d is not valid for row-count %d lineTitle %d. Unable to create form-panel layout.",
              components.length, rowCount, columnCount));

    rows = new Component[rowCount][];
    for (int i = 0; i < rowCount; i++) {
      rows[i] = new Component[columnCount];

      for (int j = 0; j < columnCount; j++) {
        Component c = components[i * columnCount + j];
        rows[i][j] = c;
      }
    }

    columns = new Component[columnCount][];
    for (int i = 0; i < columnCount; i++) {
      columns[i] = new Component[rowCount];

      for (int j = 0; j < rowCount; j++) {
        Component c = components[i + j * columnCount];
        columns[i][j] = c;
      }
    }

    fillFormPanel(panel, rows, columns);
  }

  public static JPanel indentPanel(JComponent panel, int distance) {
    JPanel ret =
        LayoutManager.createFlowPanel(eVerticalAlign.baseline, 0,
            createHorizontalPlaceholder(distance),
            panel);
    return ret;
  }

  public static JPanel createHorizontalPlaceholder(int width) {
    JPanel ret = createPlaceholder(width, 1);
    return ret;
  }

  private static JPanel createVerticalPlaceholder(int height) {
    JPanel ret = createPlaceholder(1, height);
    return ret;
  }

  private static JPanel createPlaceholder(int width, int height) {
    JPanel ret = new JPanel();
    Dimension d = new Dimension(width, height);
    ret.setPreferredSize(d);
    return ret;
  }
}