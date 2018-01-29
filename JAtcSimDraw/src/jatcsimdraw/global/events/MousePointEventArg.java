/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.global.events;

import jatcsimdraw.global.Point;

/**
 *
 * @author Marek
 */
public class MousePointEventArg{
  public final Point point;

  public MousePointEventArg(Point point) {
    this.point = point;
  }
}
