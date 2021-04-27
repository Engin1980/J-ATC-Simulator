package eng.jAtcSim.layouting;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.UnexpectedValueException;

import java.awt.*;

public class ColRowLayoutManager implements LayoutManager {
  public final Value[] blockSizes;
  public final Orientation orientation;
  public final int minimalSize;
  private final IList<Component> components = new EList<>();
  private final int blockSizesMinimalSum;

  public ColRowLayoutManager(Orientation orientation, int minimalSize, Value[] blockSizes) {
    this.orientation = orientation;
    this.blockSizes = blockSizes;
    this.minimalSize = minimalSize;
    this.blockSizesMinimalSum = calculateBlockSizesMinimaLSum();
  }

  @Override
  public void addLayoutComponent(String s, Component component) {
    this.components.add(component);
  }

  @Override
  public void layoutContainer(Container parent) {

    switch (orientation) {
      case rows:
        layerContainerRows(parent);
        break;
      case columns:
        layerContainerColumns(parent);
        break;
    }
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    int width, height;
    Insets insets = parent.getInsets();
    switch (orientation) {
      case rows:
        width = parent.getWidth() - insets.left - insets.right;
        height = blockSizesMinimalSum;
        break;
      case columns:
        height = parent.getHeight() - insets.top - insets.bottom;
        width = blockSizesMinimalSum;
        break;
      default:
        throw new UnexpectedValueException(orientation);
    }

    Dimension ret = new Dimension(width, height);
    return ret;
  }

  @Override
  public Dimension preferredLayoutSize(Container container) {
    return minimumLayoutSize(container);
  }

  @Override
  public void removeLayoutComponent(Component component) {
    this.components.remove(component);
  }

  private int calculateBlockSizesMinimaLSum() {
    int ret = 0;
    for (Value blockSize : blockSizes) {
      if (blockSize.value != null && blockSize.unit == Value.Unit.pixel)
        ret += blockSize.value;
    }
    return ret;
  }

  private void layerContainerColumns(Container parent) {
    Insets insets = parent.getInsets();
    int availableWidth = parent.getWidth() - insets.left - insets.right;
    int height = parent.getHeight() - insets.top - insets.bottom;

    int[] widths = evaluateBlockHeights(availableWidth);
    Component[] comps = parent.getComponents();
    int X = insets.left;
    for (int i = 0; i < comps.length; i++) {
      if (i >= blockSizes.length) break; // no more definitions
      Component comp = comps[i];

      comp.setLocation(X, insets.top);
      X += widths[i];
      comp.setSize(widths[i], height);
    }
  }

  private void layerContainerRows(Container parent) {
    Insets insets = parent.getInsets();
    int availableHeight = parent.getHeight() - insets.top - insets.bottom;
    int width = parent.getWidth() - insets.left - insets.right;

    int[] heights = evaluateBlockHeights(availableHeight);
    Component[] comps = parent.getComponents();
    int Y = insets.top;
    for (int i = 0; i < comps.length; i++) {
      if (i >= blockSizes.length) break; // no more definitions
      Component comp = comps[i];

      comp.setLocation(insets.left, Y);
      Y += heights[i];
      comp.setSize(width, heights[i]);
    }
  }

  private int[] evaluateBlockHeights(int total) {
    int[] ret = new int[blockSizes.length];
    int used = 0;
    int wilds = 0;

    for (int i = 0; i < ret.length; i++) {
      if (blockSizes[i].value == null) {
        wilds++;
      } else {
        ret[i] = blockSizes[i].convertValueToInt(total);
        used += ret[i];
      }
    }
    if (wilds > 0) {
      int wildBlock = (total - used) / wilds;
      for (int i = 0; i < ret.length; i++) {
        if (blockSizes[i].value == null)
          ret[i] = wildBlock;
      }
    }

    return ret;
  }
}
