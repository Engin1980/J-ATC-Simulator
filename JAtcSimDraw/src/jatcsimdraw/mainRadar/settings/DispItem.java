/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar.settings;

import jatcsimlib.global.XmlOptional;
import jatcsimlib.global.KeyItem;
import java.awt.Color;

/**
 *
 * @author Marek
 */
public class DispItem implements KeyItem<String> {

  public static final String RUNWAY_ACTIVE = "activeRunway";
  public static final String RUNWAY_CLOSED = "closedRunway";
  
  public static final String NAV_VOR = "navVOR";
  public static final String NAV_FIX = "navFix";
  public static final String NAV_FIX_MINOR = "navFixMinor";
  public static final String NAV_NDB = "navNDB";
  public static final String NAV_AIRPORT = "navAirport";
  
  public static final String BORDER_COUNTRY = "borderCountry";
  public static final String BORDER_CTR = "borderCtr";
  public static final String BORDER_TMA = "borderTma";
  
  public static final String MAP_BACKCOLOR = "mapBackcolor";
  public static final String SID = "sid";
  public static final String STAR = "star";

  private String key;
  private Color color;
  @XmlOptional
  private int width;
  @XmlOptional
  private int borderDistance;
  @XmlOptional
  private int borderWidth;
  
  @Override
  public String getKey() {
    return key;
  }

  public Color getColor() {
    return color;
  }

  public int getWidth() {
    return width;
  }

  public int getBorderDistance() {
    return borderDistance;
  }

  public int getBorderWidth() {
    return borderWidth;
  }
}
