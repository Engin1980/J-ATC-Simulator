/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib;

import jatcsimlib.events.EventListener;
import jatcsimlib.exceptions.ERuntimeException;
import java.util.TimerTask;

/**
 *
 * @author Marek Vajgl
 */
public class Timer {

  private java.util.Timer tmr = null;
  private final EventListener<Timer, Object> tick;

  public Timer(EventListener<Timer, Object> tickEvent) {
    this.tick = tickEvent;
  }

  public synchronized void start(int ms) {
    if (tmr != null) {
      throw new ERuntimeException("Cannot start the timer, its not stopped.");
    }

    TimerTask tt = new TimerTask() {

      @Override
      public void run() {
        tick.raise(Timer.this, null);
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
