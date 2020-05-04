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

public class TimerProvider {

  private java.util.Timer tmr = null;
  private final EventSimple<TimerProvider> tickEvent = new EventSimple<>(this);
  private int tickInterval;

  public TimerProvider(int tickInterval, IEventListenerSimple<TimerProvider> listener) {
    setTickInterval(tickInterval);
    this.tickEvent.add(listener);
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
}
