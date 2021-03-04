package eng.jAtcSim.abstractRadar.settings;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.abstractRadar.global.Color;
import eng.jAtcSim.abstractRadar.global.Font;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RadarStyleSettings implements IXPersistable {

  ///region Inner classes
  public static class ColorWidthSettings implements IXPersistable {
    @XIgnored
    private Color color;
    @XIgnored
    private int width;

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public int getWidth() {
      return width;
    }

    public void setWidth(int width) {
      this.width = width;
    }

    @Override
    public void load(XElement elm, XContext ctx) {
      this.color = loadAttributeColor(elm, "color");
      this.width = loadAttributeInt(elm, "width");
    }
  }

  public static class ColorWidthFontSettings extends ColorWidthSettings {
    private Font font;

    public Font getFont() {
      return font;
    }

    public void setFont(Font font) {
      this.font = font;
    }
  }

  public static class ColorWidthLengthSettings extends ColorWidthSettings {
    @XIgnored private double length;

    public double getLength() {
      return length;
    }

    public void setLength(double length) {
      this.length = length;
    }

    @Override
    public void load(XElement elm, XContext ctx) {
      super.load(elm, ctx);
      this.length = loadAttributeInt(elm, "length");
    }
  }

  public static class ColorWidthBorderSettings extends ColorWidthSettings {
    @XIgnored private int borderWidth;
    @XIgnored private int borderDistance;

    public int getBorderDistance() {
      return borderDistance;
    }

    public void setBorderDistance(int borderDistance) {
      this.borderDistance = borderDistance;
    }

    public int getBorderWidth() {
      return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
      this.borderWidth = borderWidth;
    }

    @Override
    public void load(XElement elm, XContext ctx) {
      super.load(elm, ctx);
      this.borderDistance = loadAttributeInt(elm, "borderDistance");
      this.borderWidth = loadAttributeInt(elm, "borderWidth");
    }
  }

  public static class PlaneLabelSettings implements IXPersistable {

    @XIgnored
    private boolean visible = true;
    @XIgnored
    private Color color;
    @XIgnored
    private Color connectorColor;
    @XIgnored
    private int pointWidth;
    @XIgnored
    private int headingLineLength;
    @XIgnored
    private int historyDotCount;
    @XIgnored
    private int historyDotStep;
    @XIgnored
    private int separationRingRadius;
    @XIgnored
    private String firstLineFormat;
    @XIgnored
    private String secondLineFormat;
    @XIgnored
    private String thirdLineFormat;

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public Color getConnectorColor() {
      return connectorColor;
    }

    public String getFirstLineFormat() {
      return firstLineFormat;
    }

    public void setFirstLineFormat(String firstLineFormat) {
      this.firstLineFormat = firstLineFormat;
    }

    public int getHeadingLineLength() {
      return headingLineLength;
    }

    public void setHeadingLineLength(int headingLineLength) {
      this.headingLineLength = headingLineLength;
    }

    public int getHistoryDotCount() {
      return historyDotCount;
    }

    public void setHistoryDotCount(int historyDotCount) {
      this.historyDotCount = historyDotCount;
    }

    public int getHistoryDotStep() {
      return historyDotStep;
    }

    public void setHistoryDotStep(int historyDotStep) {
      this.historyDotStep = historyDotStep;
    }

    public int getPointWidth() {
      return pointWidth;
    }

    public void setPointWidth(int pointWidth) {
      this.pointWidth = pointWidth;
    }

    public String getSecondLineFormat() {
      return secondLineFormat;
    }

    public void setSecondLineFormat(String secondLineFormat) {
      this.secondLineFormat = secondLineFormat;
    }

    public int getSeparationRingRadius() {
      return separationRingRadius;
    }

    public void setSeparationRingRadius(int separationRingRadius) {
      this.separationRingRadius = separationRingRadius;
    }

    public String getThirdLineFormat() {
      return thirdLineFormat;
    }

    public void setThirdLineFormat(String thirdLineFormat) {
      this.thirdLineFormat = thirdLineFormat;
    }

    public boolean isVisible() {
      return visible;
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    @Override
    public void load(XElement elm, XContext ctx) {
      this.color = loadAttributeColor(elm, "color");
      this.connectorColor = loadAttributeColor(elm, "connectorColor");
      this.pointWidth = loadAttributeInt(elm, "pointWidth");
      this.headingLineLength = loadAttributeInt(elm, "headingLineLength");
      this.historyDotCount = loadAttributeInt(elm, "historyDotCount");
      this.historyDotStep = loadAttributeInt(elm, "historyDotStep");
      this.separationRingRadius = loadAttributeInt(elm, "separationRingRadius");
      this.firstLineFormat = elm.getAttribute("firstLineFormat");
      this.secondLineFormat = elm.getAttribute("secondLineFormat");
      this.thirdLineFormat = elm.getAttribute("thirdLineFormat");
      this.visible = elm.tryGetAttribute("visible", "true").equals("true");
    }


  }

  public static class TextSettings implements IXPersistable {
    @XIgnored
    private Color color;
    private Font font;

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public Font getFont() {
      return font;
    }

    public void setFont(Font font) {
      this.font = font;
    }

    @Override
    public void load(XElement elm, XContext ctx) {
      this.color = Color.fromHex(elm.getAttribute("color"));
    }
  }

  public static RadarStyleSettings load(String fileName) {
    RadarStyleSettings ret;
    try {
      XDocument doc = XDocument.load(fileName);
      XElement root = doc.getRoot();

      XContext ctx = new XContext();
      initContext(ctx);
      ret = ctx.loader.loadObject(root, RadarStyleSettings.class);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Unable to load radar style settings from '%s'.", fileName), e);
    }
    return ret;
  }

  private static Color loadAttributeColor(XElement elm, String attributeName) {
    return loadAttribute(elm, attributeName, s -> Color.fromHex(s));
  }

  private static int loadAttributeInt(XElement elm, String attributeName) {
    return loadAttribute(elm, attributeName, s -> Integer.parseInt(s));
  }
  ///end region

  private static <T> T loadAttribute(XElement elm, String attributeName, Selector<String, T> selector) {
    String s = elm.getAttribute(attributeName);
    T ret = selector.invoke(s);
    return ret;
  }

  private static void initContext(XContext ctx) {
    ctx.loader.setDeserializer(Color.class, e -> Color.fromHex(e.getContent()));
  }


  private static <T> Selector<XElement, T> getSimpleObjectDeserializer(Class<T> type, XContext ctx) {
    Selector<XElement, T> ret = e -> {
      T tmp = ctx.loader.loadObject(e, type);
      return tmp;
    };

    return ret;
  }

  @XIgnored public int displayTextDelay;
  public ColorWidthFontSettings infoLine;
  // runways
  public ColorWidthSettings activeRunway;
  public ColorWidthSettings closedRunway;
  // approaches
  public ColorWidthLengthSettings ilsApproach;
  public ColorWidthLengthSettings gnssApproach;
  public ColorWidthLengthSettings vorApproach;
  public ColorWidthLengthSettings ndbApproach;
  // navaids
  public ColorWidthBorderSettings navVOR;
  public ColorWidthBorderSettings navNDB;
  public ColorWidthBorderSettings navFix;
  public ColorWidthBorderSettings navFixMinor;
  public ColorWidthBorderSettings navAirport;
  // borders
  public ColorWidthSettings borderCountry;
  public ColorWidthSettings borderCtr;
  public ColorWidthSettings borderTma;
  public ColorWidthFontSettings borderMrva;

  // mapBack
  public ColorWidthFontSettings borderRestricted;
  public Color mapBackcolor;
  // routes
  public ColorWidthSettings star;
  public ColorWidthSettings sid;
  // text settings
  public TextSettings atc;
  public TextSettings plane;
  public TextSettings system;
  public TextSettings time;
  public TextSettings callsign;
  public TextSettings navaid;
  public Color switchingPlaneAlternatingColor = null;
  // plane labels
  public PlaneLabelSettings uncontrolled;
  public PlaneLabelSettings stopped;
  public PlaneLabelSettings app;
  public PlaneLabelSettings ctr;
  public PlaneLabelSettings twr;
  public PlaneLabelSettings selected;
  public PlaneLabelSettings emergency;
  // airproxes
  public Color airproxFull;
  public Color airproxPartial;
  public Color airproxWarning;
  public Color mrvaError;

  @Override
  public void load(XElement elm, XContext ctx) {
    this.displayTextDelay = loadAttributeInt(elm, "displayTextDelay");
  }
}
