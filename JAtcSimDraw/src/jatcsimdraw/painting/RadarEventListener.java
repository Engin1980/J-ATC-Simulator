/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimdraw.painting;

import jatcsimlib.coordinates.Coordinate;

/**
 *
 * @author Marek
 */
public abstract class RadarEventListener {
  public abstract void raise (Radar sender, Coordinate coordinate);
}
