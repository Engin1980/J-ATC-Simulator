/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar.settings;

import jatcsimlib.global.KeyItem;
import jatcsimlib.global.XmlOptional;
import java.awt.Color;

/**
 *
 * @author Marek
 */
public class DispPlane implements KeyItem<DispPlane.eBehavior> {

  
  public enum eBehavior{
    stopped,
    twr,
    ctr,
    app
  }
  
  private eBehavior behavior;
  @XmlOptional
  private boolean visible = true;
  private Color color;
  private int pointWidth;
  private int headingLineLength;
  private int separationRingRadius;
  private int historyDotCount;
  private String firstLineFormat;
  private String secondLineFormat;
  private String thirdLineFormat;

  @Override
  public eBehavior getKey() {
    return behavior;
  }

  /**
   * Get separation ring radius in NM.
   * @return 
   */
  public int getSeparationRingRadius() {
    return separationRingRadius;
  }
  
  /**
   * Get behavior what this disp-plane item is used to.
   * @return 
   */
  public eBehavior getBehavior() {
    return behavior;
  }

  /**
   * Get if plane is visible on radar.
   * @return 
   */
  public boolean isVisible() {
    return visible;
  }
  
  /**
   * Get width of point representing the plane in pixels.
   * @return 
   */
  public int getPointWidth() {
    return pointWidth;
  }

  /**
   * Get color of plane in pixels.
   * @return 
   */
  public Color getColor() {
    return color;
  }

  /**
   * Get length of the heading line indicator in NM.
   * @return 
   */
  public int getHeadingLineLength() {
    return headingLineLength;
  }

  /**
   * Get number of history dots of the plane.
   * @return 
   */
  public int getHistoryDotCount() {
    return historyDotCount;
  }

  /**
   * Get content of first line of caption.
   * @return 
   */
  public String getFirstLineFormat() {
    return firstLineFormat;
  }

  /**
   * Get content of second line of caption.
   * @return 
   */
  public String getSecondLineFormat() {
    return secondLineFormat;
  }

  /**
   * Get content of third line of caption.
   * @return 
   */
  public String getThirdLineFormat() {
    return thirdLineFormat;
  }
  
}
