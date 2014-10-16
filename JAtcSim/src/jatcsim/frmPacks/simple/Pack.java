/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks.simple;

import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.world.Area;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 *
 * @author Marek
 */
public class Pack extends jatcsim.frmPacks.Pack {

  private Simulation sim;
  private FrmMain frmMain;
  private FrmFlightList frmList;
  private Timer tmr;

  public Pack() {
  }
  
  @Override
  public void initPack(Simulation sim, Area area, Settings displaySettings) {
    this.sim = sim;
    
    this.frmMain = new FrmMain();
    frmMain.init(sim, area, displaySettings);
    
    this.frmList = new FrmFlightList();
    frmList.init(sim);
  }

  @Override
  public void startPack() {
    int delay = 500; 
    ActionListener taskPerformer = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        sim.elapseSecond();
        frmMain.elapseSecond();
        frmList.elapseSecond();
      }
    };
    
    tmr = new Timer(delay, taskPerformer);
    tmr.start();
  }
}
