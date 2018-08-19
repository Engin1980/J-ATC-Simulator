/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.weathers;

import eng.eSystem.events.EventSimple;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.logging.ApplicationLog;

/**
 * @author Marek Vajgl
 */
public abstract class DynamicWeatherProvider extends WeatherProvider {

  private boolean hasFailedAlready = false;

  public final void updateWeather(boolean async) {
    UpdateThread t = new UpdateThread(this);
    if (async) {
      t.getUpdateFinishedEvent().add(q -> updateWeatherFinished(q));
      t.start();
    } else {
      t.run();
      updateWeatherFinished(t);
    }
  }

  private void updateWeatherFinished(UpdateThread q) {
    if (q.getException() != null && !hasFailedAlready) {
      Acc.log().writeLine(ApplicationLog.eType.warning,
          "Failed to download weather using %s. Reason: %s.",
          this.getClass().getName(),
          ExceptionUtils.toFullString(q.getException()));
      hasFailedAlready = true;
    } else {
      Acc.log().writeLine(ApplicationLog.eType.info,
          "Weather downloaded successfully: %s",
          q.getResult().toInfoString());
      if (hasFailedAlready) hasFailedAlready = false;
      Weather w = q.getResult();
      super.setWeather(w);
    }
  }

  abstract Weather getUpdatedWeather();
}

class UpdateThread extends Thread {

  private Weather result;
  private Exception exception;
  private DynamicWeatherProvider provider;
  private EventSimple<UpdateThread> updateFinishedEvent = new EventSimple<>(this);

  public UpdateThread(DynamicWeatherProvider provider) {
    this.provider = provider;
  }

  public EventSimple<UpdateThread> getUpdateFinishedEvent() {
    return updateFinishedEvent;
  }

  public Weather getResult() {
    return result;
  }

  public Exception getException() {
    return exception;
  }

  @Override
  public void run() {
    try {
      result = provider.getUpdatedWeather();
    } catch (Exception ex) {
      exception = ex;
    }
    updateFinishedEvent.raise();
  }
}
