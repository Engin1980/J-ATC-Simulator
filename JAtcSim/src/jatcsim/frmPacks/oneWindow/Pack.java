/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks.oneWindow;

import jatcsimdraw.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.world.Area;

/**
 *
 * @author Marek
 */
public class Pack extends jatcsim.frmPacks.Pack {

  @Override
  public void initPack(Simulation sim, Area area, Settings displaySettings) {
    FrmMain f = new FrmMain();
    f.init(sim, area, displaySettings);
  }

  @Override
  public void startPack() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
