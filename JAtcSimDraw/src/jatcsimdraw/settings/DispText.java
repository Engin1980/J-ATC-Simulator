/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.settings;

import jatcsimlib.global.KeyItem;
import java.awt.Color;

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
    plane,
    system
  }
  
  private eType type;
  private Color color;

  public eType getType() {
    return type;
  }

  public Color getColor() {
    return color;
  }
}
