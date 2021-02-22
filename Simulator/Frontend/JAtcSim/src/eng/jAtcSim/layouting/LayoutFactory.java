package eng.jAtcSim.layouting;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;

public class LayoutFactory {
  public Layout loadFromXml(XElement elm) {
    Layout ret;
    ISet<Window> tmp = new ESet<>();

    for (XElement welm : elm.getChildren("window")) {
      Window window = loadWindow(welm);
      tmp.add(window);
    }

    ret = new Layout(tmp);
    return ret;
  }

  private Window loadWindow(XElement elm) {
    EAssert.Argument.isTrue(elm.getName().equals("window"));
    Window ret;
    Position position = loadPosition(elm.getChild("position"));
    String title = elm.getAttribute("title");
    Block content = loadAnyBlock(elm);

    ret = new Window(position, content, title);

    return ret;
  }

  private Position loadPosition(XElement elm) {
    Value x = loadValue(elm.getAttribute("x"));
    Value y = loadValue(elm.getAttribute("y"));
    Value width = loadValue(elm.getAttribute("width"));
    Value height = loadValue(elm.getAttribute("height"));

    Position ret = new Position(x, y, width, height);
    return ret;
  }

  private Value loadValue(String text) {
    EAssert.Argument.matchPattern(text, "^(\\d+(px|%))|(\\*)$");

    Value ret;

    int val;
    if (text.equals("*"))
      ret = new WildValue();
    else if (text.endsWith("%")) {
      val = Integer.parseInt(text.substring(0, text.length() - 1));
      ret = new PercentageValue(val);
    } else {
      val = Integer.parseInt(text.substring(0, text.length() - 2));
      ret = new PixelValue(val);
    }

    return ret;


  }

  private Block loadAnyBlock(XElement parentElement) {
    Block ret = null;

    if (parentElement.tryGetChild("panel") != null)
      ret = loadPanel(parentElement);
    else if (parentElement.getChildren("column").count() > 0)
      ret = loadColumns(parentElement);
    else if (parentElement.getChildren("row").count() > 0)
      ret = loadRows(parentElement);

    EAssert.Argument.isNotNull(ret);

    return ret;
  }

  private RowList loadRows(XElement parentElement) {
    RowList ret;
    IList<Row> tmp = new EList<>();

    for (XElement elm : parentElement.getChildren("row")) {
      Row row = loadRow(elm);
      tmp.add(row);
    }

    ret = new RowList(tmp);
    return ret;
  }

  private Row loadRow(XElement elm) {
    Row ret;
    Value height = loadValue(elm.getAttribute("height"));
    Block content = loadAnyBlock(elm);
    ret = new Row(height, content);
    return ret;
  }

  private ColumnList loadColumns(XElement parentElement) {
    ColumnList ret;
    IList<Column> tmp = new EList<>();

    for (XElement elm : parentElement.getChildren("column")) {
      Column column = loadColumn(elm);
      tmp.add(column);
    }

    ret = new ColumnList(tmp);
    return ret;
  }

  private Column loadColumn(XElement elm) {
    Column ret;
    Value width = loadValue(elm.getAttribute("width"));
    Block content = loadAnyBlock(elm);
    ret = new Column(width, content);
    return ret;
  }

  private Panel loadPanel(XElement parentElement) {
    XElement elm = parentElement.getChild("panel");

    String view = elm.getAttribute("view");
    String id = elm.tryGetAttribute("id", view);

    Panel panel = new Panel(view);
    panel.setId(id);

    return panel;
  }
}
