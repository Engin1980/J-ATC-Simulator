/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.weathers;

import eng.eSystem.events.EventSimple;
import eng.jAtcSim.lib.Acc;

/**
 * @author Marek Vajgl
 */
public abstract class DynamicWeatherProvider extends WeatherProvider {

  public final void updateWeather(boolean async) {
    UpdateThread t = new UpdateThread(this);
    if (async) {
      t.getUpdateFinishedEvent().add(q -> updateWeatherFinished((UpdateThread) q));
      t.start();
    } else {
      t.run();
      updateWeatherFinished(t);
    }
  }

  private void updateWeatherFinished(UpdateThread q) {
    if (q.getException() != null) {
      Acc.sim().sendTextMessageForUser("Failed to update weather. " + q.getException().getMessage());
    } else {
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
