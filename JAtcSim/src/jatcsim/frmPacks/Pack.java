/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks;

import jatcsim.AppSettings;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.world.Area;

/**
 *
 * @author Marek
 */
public abstract class Pack {
  public abstract void initPack(Simulation sim, Area area, AppSettings appSettings);
  public abstract void startPack();

  public abstract EventManager<Pack, EventListener, Object> getElapseSecondEvent();
}
