/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.events.EventSimple;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import java.util.TimerTask;

public class TimerModule extends SimulationModule {

  @XIgnored private java.util.Timer tmr = null;
  @XIgnored private final EventSimple<TimerModule> tickEvent = new EventSimple<>(this);
  private int tickInterval;
  @XIgnored private boolean tickIntervalChanged = false;

  @XConstructor
  private TimerModule(XLoadContext ctx) {
    super(ctx);
  }

  public TimerModule(Simulation parent, int tickInterval) {
    super(parent);
    setTickInterval(tickInterval);
  }

  public int getTickInterval() {
    return this.tickInterval;
  }

  public final void setTickInterval(int tickInterval) {
    EAssert.Argument.isTrue(tickInterval > 0);
    this.tickInterval = tickInterval;
    this.tickIntervalChanged = true;
  }

  public synchronized boolean isRunning() {
    return tmr != null;
  }

  public int registerOnTickListener(IEventListenerSimple<ISimulation> action) {
    EAssert.Argument.isNotNull(action, "action");
    return this.tickEvent.add(e -> action.raise(parent.isim));
  }

  public synchronized void start() {
    if (tmr != null) {
      throw new ApplicationException("Cannot start the timer, its not stopped.");
    }

    TimerTask tt = new TimerTask() {
      @Override
      public void run() {
        onTick();
      }
    };
    tmr = new java.util.Timer(true);
    tmr.scheduleAtFixedRate(tt, tickInterval, tickInterval);
  }

  private void onTick(){
    tickEvent.raise();
    if (tickIntervalChanged){
      stop();
      start();
      tickIntervalChanged = false;
    }
  }

  public synchronized void stop() {
    if (tmr != null) {
      tmr.cancel();
      tmr = null;
    }
  }

  public void unregisterOnTickListener(int listenerId) {
    this.tickEvent.remove(listenerId);
  }
}
