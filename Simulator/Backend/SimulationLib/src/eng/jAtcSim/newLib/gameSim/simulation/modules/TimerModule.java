/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.eXml.XElement;
import eng.eSystem.events.EventSimple;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

import java.util.TimerTask;

public class TimerModule extends SimulationModule {

  public static TimerModule load(Simulation parent, XElement source) {
    int ti = XmlLoadUtils.Field.loadFieldValue(source, "tickInterval", int.class);
    TimerModule ret = new TimerModule(parent, ti);

    boolean running = XmlLoadUtils.Field.loadFieldValue(source, "running", boolean.class);
    if (running)
      ret.start();
    return ret;
  }

  private java.util.Timer tmr = null;
  private final EventSimple<TimerModule> tickEvent = new EventSimple<>(this);
  private int tickInterval;

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
  }

  public synchronized void start() {
    if (tmr != null) {
      throw new EApplicationException("Cannot start the timer, its not stopped.");
    }

    TimerTask tt = new TimerTask() {
      @Override
      public void run() {
        tickEvent.raise();
      }
    };
    tmr = new java.util.Timer(true);
    tmr.scheduleAtFixedRate(tt, tickInterval, tickInterval);
  }

  public synchronized void stop() {
    if (tmr != null) {
      tmr.cancel();
      tmr = null;
    }
  }

  public synchronized boolean isRunning() {
    return tmr != null;
  }

  public int registerOnTickListener(IEventListenerSimple<ISimulation> action){
    EAssert.Argument.isNotNull(action, "action");
    return this.tickEvent.add(e -> action.raise(parent.isim));
  }

  public void unregisterOnTickListener(int listenerId){
    this.tickEvent.remove(listenerId);
  }
}
