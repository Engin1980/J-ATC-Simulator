/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.radarBase;

import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.radarBase.global.Color;
import eng.jAtcSim.radarBase.global.Font;

/**
 *
 * @author Marek
 */
public class RadarStyleSettings {

  ///region Inner classes
  public static class ColorWidthSettings {
    private Color color;
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

  public static class ColorWidthFontSettings extends ColorWidthSettings{
    private Font font;

    public Font getFont() {
      return font;
    }

    public void setFont(Font font) {
      this.font = font;
    }
  }

  public static class ColorWidthLengthSettings extends ColorWidthSettings{
    private double length;

    public double getLength() {
      return length;
    }

    public void setLength(double length) {
      this.length = length;
    }
  }

  public static class ColorWidthBorderSettings extends ColorWidthSettings {
    private int borderWidth;
    private int borderDistance;

    public int getBorderWidth() {
      return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
      this.borderWidth = borderWidth;
    }

    public int getBorderDistance() {
      return borderDistance;
    }

    public void setBorderDistance(int borderDistance) {
      this.borderDistance = borderDistance;
    }
  }

  public static class PlaneLabelSettings{

    @XmlOptional
    private boolean visible = true;

    private Color color;
    private Color connectorColor;
    private int pointWidth;
    private int headingLineLength;
    private int historyDotCount;
    private int historyDotStep;
    private int separationRingRadius;
    private String firstLineFormat;
    private String secondLineFormat;
    private String thirdLineFormat;

    public boolean isVisible() {
      return visible;
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    public Color getColor() {
      return color;
    }

    public Color getConnectorColor() {
      return connectorColor;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public int getPointWidth() {
      return pointWidth;
    }

    public void setPointWidth(int pointWidth) {
      this.pointWidth = pointWidth;
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

    public int getSeparationRingRadius() {
      return separationRingRadius;
    }

    public void setSeparationRingRadius(int separationRingRadius) {
      this.separationRingRadius = separationRingRadius;
    }

    public String getFirstLineFormat() {
      return firstLineFormat;
    }

    public void setFirstLineFormat(String firstLineFormat) {
      this.firstLineFormat = firstLineFormat;
    }

    public String getSecondLineFormat() {
      return secondLineFormat;
    }

    public void setSecondLineFormat(String secondLineFormat) {
      this.secondLineFormat = secondLineFormat;
    }

    public String getThirdLineFormat() {
      return thirdLineFormat;
    }

    public void setThirdLineFormat(String thirdLineFormat) {
      this.thirdLineFormat = thirdLineFormat;
    }
  }

  public static class TextSettings{
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
  }
  ///end region

  public int displayTextDelay;

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
  public ColorWidthFontSettings borderRestricted;

  // mapBack
  /** Map background color in hex */
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
  @XmlOptional
  public Color switchingPlaneAlternatingColor = null;

  // plane labels
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
