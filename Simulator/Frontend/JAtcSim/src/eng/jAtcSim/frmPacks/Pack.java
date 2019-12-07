/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.frmPacks;

import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.newLib.Game;
import eng.jAtcSim.newLib.Simulation;
import eng.jAtcSim.newLib.world.Area;

/**
 *
 * @author Marek
 */
public abstract class Pack {
  public abstract void initPack(Game g, AppSettings appSettings);
  public abstract void startPack();

  public abstract EventSimple<Pack> getElapseSecondEvent();

  public abstract IMap<String,Object> getDataToStore();

  public abstract AppSettings getAppSettings();

  public abstract void applyStoredData(IMap<String,Object> map);
}
