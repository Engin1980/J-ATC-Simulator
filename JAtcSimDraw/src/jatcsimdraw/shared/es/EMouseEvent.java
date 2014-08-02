/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.shared.es;

import jatcsimdraw.painting.Point;

/**
 *
 * @author Marek
 */
public class EMouseEvent {

  public enum eType {

    Click,
    DoubleClick,
    Drag,
    Move,
    WheelScroll,
  }

  public final int x;
  public final int y;
  public final eType type;
  public final int wheel;

  public EMouseEvent(int x, int y, int wheel, eType type) {
    this.x = x;
    this.y = y;
    this.type = type;
    this.wheel = wheel;
  }

  public EMouseEvent(int x, int y, eType type) {
    this(x, y, 0, type);
  }

  public EMouseEvent(java.awt.Point p, eType type) {
    this(p.x, p.y, type);
  }

  public EMouseEvent(java.awt.Point p, int wheel, eType type) {
    this(p.x, p.y, wheel, type);
  }

  public Point getPoint() {
    return new Point(x, y);
  }
}
