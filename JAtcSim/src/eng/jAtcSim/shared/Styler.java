package eng.jAtcSim.shared;

import com.sun.javafx.iio.common.ImageLoaderImpl;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;

import javax.swing.*;
import java.awt.*;

public class Styler {

  private IMap<JComponent, IList<String>> componentStyles = new EMap<>();

  public static class Style{
    public final String name;
    public final boolean recursive;
    public final Color backColor;
    public final Color foreColor;
    public final Dimension size;
    public final Font font;

    public Style(String name, boolean recursive, Color backColor, Color foreColor, Dimension size, Font font) {
      this.name = name;
      this.recursive = recursive;
      this.backColor = backColor;
      this.foreColor = foreColor;
      this.size = size;
      this.font = font;
    }
  }

  public void addStyle(JComponent cmp, String styleName){

  }

  public void apply(JComponent component, boolean recursively){



  }



}
