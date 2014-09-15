/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks;

import jatcsimdraw.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.world.Area;

/**
 *
 * @author Marek
 */
public abstract class Pack {
  public abstract void initPack(Simulation sim, Area area, Settings displSettings);
  public abstract void startPack();
}
