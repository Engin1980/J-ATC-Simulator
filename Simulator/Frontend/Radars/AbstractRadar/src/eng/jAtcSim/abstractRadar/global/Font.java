package eng.jAtcSim.abstractRadar.global;

import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.ReflectionUtils;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

public class Font implements IXPersistable {
  @XIgnored private String family;
  @XIgnored private int style;
  @XIgnored private int size;

  public Font(String family, int size, int style) {
    this.family = family;
    this.style = style;
    this.size = size;
  }

  @XConstructor
  private Font() {
    this.family = null;
    this.style = -1;
    this.size = -1;
  }

  public String getFamily() {
    return family;
  }

  public int getSize() {
    return size;
  }

  public int getStyle() {
    return style;
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    this.family = elm.getAttribute("family");
    this.style = Integer.parseInt(elm.getAttribute("style"));
    this.size = Integer.parseInt(elm.getAttribute("size"));
  }
}
