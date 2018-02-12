package eng.jAtcSim.startup;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;

public class LayoutManager {

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

  public static JPanel createFlowPanel(eVerticalAlign align, int distance, JComponent... components) {
    JPanel ret = new JPanel();
    fillFlowPanel(ret, align, distance, components);
    return ret;
  }

  public static void fillFlowPanel(Container panel, eVerticalAlign align, int distance, JComponent... components) {
    BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
    panel.setLayout(layout);

    for (int i = 0; i < components.length; i++) {
      if (distance > 0 && i > 0) {
        panel.add(Box.createHorizontalStrut(distance));
      }
      JComponent component = components[i];
      switch (align){
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

  public static void fillBorderedPanel(Container panel, int distance, JComponent content) {
    panel.setLayout(new BorderLayout());
    panel.add(Box.createVerticalStrut(distance), BorderLayout.PAGE_START);
    panel.add(Box.createVerticalStrut(distance), BorderLayout.PAGE_END);
    panel.add(Box.createHorizontalStrut(distance), BorderLayout.LINE_START);
    panel.add(Box.createHorizontalStrut(distance), BorderLayout.LINE_END);
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
              "Component array length %d is not valid for row-count %d x %d. Unable to create form-panel layout.",
              components.length,  rowCount,  columnCount));

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

  public static JPanel indentPanel(JComponent panel, int distance){
    JPanel ret =
        LayoutManager.createFlowPanel(eVerticalAlign.baseline, 0,
            createHorizontalPlaceholder(distance),
            panel);
    return ret;
  }

  private static JPanel createHorizontalPlaceholder(int width){
    JPanel ret = createPlaceholder(width, 1);
    return ret;
  }

  private static JPanel createVerticalPlaceholder(int height){
    JPanel ret = createPlaceholder(1, height);
    return ret;
  }

  private static JPanel createPlaceholder(int width, int height){
    JPanel ret = new JPanel();
    Dimension d = new Dimension(width, height);
    ret.setPreferredSize(d);
    return ret;
  }
}