package eng.jAtcSim.abstractRadar.settings;

import eng.eSystem.eXml.EXmlException;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.abstractRadar.global.Color;
import eng.jAtcSim.abstractRadar.global.Font;
import exml.IXPersistable;
import exml.annotations.XAttribute;
import exml.annotations.XOptional;
import exml.loading.XLoadContext;

import java.nio.file.Path;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class RadarStyleSettings implements IXPersistable {

  ///region Inner classes
  public static class ColorWidthSettings implements IXPersistable {
    @XAttribute
    private Color color;
    @XAttribute
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
    @XAttribute
    private double length;

    public double getLength() {
      return length;
    }

    public void setLength(double length) {
      this.length = length;
    }
  }

  public static class ColorWidthBorderSettings extends ColorWidthSettings {
    @XAttribute
    private int borderWidth;
    @XAttribute
    private int borderDistance;

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
  }

  public static class PlaneLabelSettings implements IXPersistable {

    @XOptional
    @XAttribute
    private boolean visible = true;
    @XAttribute
    private Color color;
    @XAttribute
    private Color connectorColor;
    @XAttribute
    private int pointWidth;
    @XAttribute
    private boolean showHeadingLine;
    @XAttribute
    private int historyDotCount;
    @XAttribute
    private int historyDotStep;
    @XAttribute
    private int separationRingRadius;
    @XAttribute
    private String firstLineFormat;
    @XAttribute
    private String secondLineFormat;
    @XAttribute
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

    public boolean isShowHeadingLine() {
      return showHeadingLine;
    }

    public void setShowHeadingLine(boolean showHeadingLine) {
      this.showHeadingLine = showHeadingLine;
    }

    public boolean isVisible() {
      return visible;
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
    }
  }

  public static class TextSettings implements IXPersistable {
    @XAttribute private Color color;
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
  }

  public static RadarStyleSettings load(Path fileName) {
    RadarStyleSettings ret;
    try {
      XDocument doc = XDocument.load(fileName);
      XElement root = doc.getRoot();

      XLoadContext ctx = new XLoadContext();
      initContext(ctx);
      ret = ctx.loadObject(root, RadarStyleSettings.class);
    } catch (EXmlException e) {
      throw new ApplicationException(sf("Unable to load radar style settings from '%s'.", fileName), e);
    }
    return ret;
  }
  ///end region

  private static void initContext(XLoadContext ctx) {
    ctx.addDefaultParsers();
    ctx.setParser(Color.class, s -> Color.fromHex(s));
  }

  @XAttribute public int displayTextDelay;
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

  //line
  public int headingLineLength;
  public int headingLineSectionCount;

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
}
