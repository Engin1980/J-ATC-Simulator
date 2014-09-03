/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.shared.es;

import jatcsimdraw.global.Point;
import jatcsimlib.events.EventArgument;

/**
 *
 * @author Marek
 */
public class MousePointEventArg implements EventArgument {
  public final Point point;

  public MousePointEventArg(Point point) {
    this.point = point;
  }
}
