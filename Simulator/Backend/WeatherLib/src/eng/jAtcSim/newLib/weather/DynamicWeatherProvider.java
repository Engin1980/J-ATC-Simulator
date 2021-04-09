/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.weather;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.functionalInterfaces.Consumer;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newLib.weather.contextLocal.Context;
import eng.jAtcSim.newLib.weather.downloaders.MetarDownloader;

/**
 * @author Marek Vajgl
 */
public class DynamicWeatherProvider extends WeatherProvider {

  static class UpdateResult {
    public final String metar;
    public final Exception exception;

    public UpdateResult(String metar) {
      this.metar = metar;
      this.exception = null;
    }

    public UpdateResult(Exception exception) {
      this.metar = null;
      this.exception = exception;
    }
  }

  static class UpdateThread extends Thread {
    private final MetarDownloader downloader;
    private final String icao;
    private Consumer<UpdateResult> onFinished;

    public UpdateThread(String icao, MetarDownloader downloader, Consumer<UpdateResult> onFinished) {
      EAssert.Argument.isNotNull(icao);
      EAssert.Argument.isNotNull(downloader);
      EAssert.Argument.isNotNull(onFinished);
      this.icao = icao;
      this.downloader = downloader;
      this.onFinished = onFinished;
    }

    @Override
    public void run() {
      UpdateResult res;
      try {
        String metar = downloader.downloadMetar(icao);
        res = new UpdateResult(metar);
      } catch (Exception ex) {
        res = new UpdateResult(ex);
      }
      onFinished.invoke(res);
    }
  }

  private static final int updateIntervalMinutes = 5;
  private final MetarDownloader downloader;
  private final String icao;
  private java.time.LocalDateTime nextUpdate = java.time.LocalDateTime.now();
  private boolean hasFailedAlready = false;
  private boolean inUpdate = false;
  private Weather updatedWeather = null;

  public DynamicWeatherProvider(MetarDownloader downloader, String icao, Weather initialWeather, boolean downloadNow) {
    EAssert.Argument.isNotNull(downloader);
    EAssert.Argument.matchPattern(icao, "^[A-Z]{4}$");
    EAssert.Argument.isNotNull(initialWeather);
    this.downloader = downloader;
    this.icao = icao;
    this.updatedWeather = initialWeather;

    if (downloadNow) {
      UpdateThread ud = new UpdateThread(icao, this.downloader, this::newMetarDownloaded);
      ud.run();
      try {
        ud.join();
        nextUpdate = java.time.LocalDateTime.now().plusMinutes(updateIntervalMinutes);
      } catch (InterruptedException e) {
        throw new EApplicationException("Failed to wait for weather download.", e);
      }
    }
  }

  @Override
  public Weather tryGetNewWeather() {
    Weather ret;

    if (updatedWeather != null) {
      ret = updatedWeather;
      updatedWeather = null;
    } else
      ret = null;

    if (!inUpdate && nextUpdate.isBefore(java.time.LocalDateTime.now())) {
      inUpdate = true;
      UpdateThread ud = new UpdateThread(icao, this.downloader, this::newMetarDownloaded);
      ud.start();
      nextUpdate = java.time.LocalDateTime.now().plusMinutes(updateIntervalMinutes);
    }
    return ret;
  }

  private ApplicationLog getLog() {
    return Context.getApp().getAppLog();
  }

  private void newMetarDownloaded(UpdateResult result) {
    if (result.exception != null && !hasFailedAlready) {
      this.getLog().write(LogItemType.warning,
              "Failed to download metar using %s. Reason: %s.",
              this.downloader.getClass().getName(),
              ExceptionUtils.toFullString(result.exception));
      hasFailedAlready = true;
    } else {
      hasFailedAlready = false;
      this.getLog().write(LogItemType.info,
              "Metar downloaded successfully: %s", result.metar);
      try {
        this.updatedWeather = super.decodeFromMetar(result.metar);
      } catch (Exception ex) {
        this.getLog().write(LogItemType.warning,
                "Failed to decode weather. Reason: %s.",
                ExceptionUtils.toFullString(result.exception));
      }
    }
    inUpdate = false;
  }
}
