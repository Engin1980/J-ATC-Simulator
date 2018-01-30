/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.frmPacks;

import eng.eSystem.events.EventSimple;
import eng.jAtcSim.AppSettings;
import jatcsimlib.Simulation;
import jatcsimlib.world.Area;

/**
 *
 * @author Marek
 */
public abstract class Pack {
  public abstract void initPack(Simulation sim, Area area, AppSettings appSettings);
  public abstract void startPack();

  public abstract EventSimple<Pack> getElapseSecondEvent();
}
