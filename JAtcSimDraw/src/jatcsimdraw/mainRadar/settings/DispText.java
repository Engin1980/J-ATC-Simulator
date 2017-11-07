/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.mainRadar.settings;

import jatcsimlib.global.KeyItem;
import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Marek
 */
public class DispText implements KeyItem<DispText.eType> {

  @Override
  public eType getKey() {
    return type;
  }
  public enum eType{
    atc,
    plane, // speech from plane
    system,
    time,
    navaid,
    callsign // callsign of plane
  }
  
  private eType type;
  private Color color;
  private String fontName;
  private int fontSize;
  private int fontStyle;

  public eType getType() {
    return type;
  }

  public Color getColor() {
    return color;
  }

  public String getFontName() {
    return fontName;
  }

  public int getSize() {
    return fontSize;
  }
  
  private Font _font = null;
  public Font getFont(){
    if (_font == null){
      _font = new Font(fontName, fontStyle, fontSize);
    }
    return _font;
  }
}
