/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.simulation;

import eng.eSystem.events.EventSimple;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;

import java.util.TimerTask;

public class Timer {

  private java.util.Timer tmr = null;
  private final EventSimple<Timer> tickEvent = new EventSimple<>(this);
  private int tickInterval;

  public Timer(IEventListenerSimple<Timer> listener) {

    this.tickEvent.add(listener);
  }

  public int getTickInterval() {
    return this.tickInterval;
  }

  public synchronized void start(int ms) {
    EAssert.Argument.isTrue(ms > 0);
    if (tmr != null) {
      throw new EApplicationException("Cannot start the timer, its not stopped.");
    }

    this.tickInterval = ms;

    TimerTask tt = new TimerTask() {
      @Override
      public void run() {
        tickEvent.raise();
      }
    };
    
    tmr = new java.util.Timer(true);
    tmr.scheduleAtFixedRate(tt, ms, ms);
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
}
