/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimlib.atcs.ATC;
import jatcsimlib.global.KeyItem;
import java.awt.Color;

/**
 *
 * @author Marek
 */
public class DispPlane implements KeyItem<ATC.eType> {

  public enum eBehavior{
    any,
    ascending,
    descending,
    leveled,
    loc
  }
  
  private ATC.eType atcType;
  private eBehavior behavior;
  private Color color;
  private int pointWidth;
  private int headingLineLength;
  private int historyDotCount;
  private String firstLineFormat;
  private String secondLineFormat;
  private String thirdLineFormat;

  @Override
  public ATC.eType getKey() {
    return atcType;
  }

  public ATC.eType getAtcType() {
    return atcType;
  }

  public int getPointWidth() {
    return pointWidth;
  }

  public Color getColor() {
    return color;
  }

  public int getHeadingLineLength() {
    return headingLineLength;
  }

  public int getHistoryDotCount() {
    return historyDotCount;
  }

  public String getFirstLineFormat() {
    return firstLineFormat;
  }

  public String getSecondLineFormat() {
    return secondLineFormat;
  }

  public String getThirdLineFormat() {
    return thirdLineFormat;
  }
  
}
