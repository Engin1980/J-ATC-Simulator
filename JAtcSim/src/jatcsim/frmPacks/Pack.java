/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks;

import jatcsimdraw.painting.Settings;
import jatcsimlib.Simulation;

/**
 *
 * @author Marek
 */
public abstract class Pack {
  public abstract void initPack(Simulation sim, Settings displSettings);
  public abstract void startPack();
}
