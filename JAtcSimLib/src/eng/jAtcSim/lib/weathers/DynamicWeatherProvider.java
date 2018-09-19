/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.weathers;

import eng.eSystem.utilites.Action;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.Validator;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloader;

/**
 * @author Marek Vajgl
 */
public class DynamicWeatherProvider extends WeatherProvider {

  class UpdateResult {
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

  class UpdateThread extends Thread {
    private final MetarDownloader downloader;
    private Action<UpdateResult> onFinished;
    private final String icao;

    public UpdateThread(String icao, MetarDownloader downloader, Action<UpdateResult> onFinished) {
      Validator.isNotNull(icao);
      Validator.isNotNull(downloader);
      Validator.isNotNull(onFinished);
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
      onFinished.apply(res);
    }
  }

  private static final int updateIntervalMinutes = 5;
  private MetarDownloader downloader;
  private java.time.LocalDateTime nextUpdate = java.time.LocalDateTime.now();
  private boolean hasFailedAlready = false;
  private boolean inUpdate = false;
  private Weather updatedWeather = null;

  public DynamicWeatherProvider(MetarDownloader downloader) {
    Validator.isNotNull(downloader );
    this.downloader = downloader;
  }

  @Override
  public Weather tryGetNewWeather() {
    if (inUpdate) return null;
    if (nextUpdate.isBefore(java.time.LocalDateTime.now())) {
      inUpdate = true;
      UpdateThread ud = new UpdateThread(Acc.airport().getIcao(), this.downloader, this::newMetarDownloaded);
      ud.start();
      nextUpdate = java.time.LocalDateTime.now().plusMinutes(updateIntervalMinutes);
      return null;
    } else if (updatedWeather != null) {
      Weather ret = updatedWeather;
      updatedWeather = null;
      return ret;
    } else
      return null;
  }

  public Weather getNewWeather() {
    UpdateThread ud = new UpdateThread(Acc.airport().getIcao(), this.downloader, this::newMetarDownloaded);
    ud.run();
    Weather ret = updatedWeather;
    updatedWeather = null;
    return ret;
  }

  private void newMetarDownloaded(UpdateResult result) {
    if (result.exception != null && !hasFailedAlready) {
      Acc.log().writeLine(ApplicationLog.eType.warning,
          "Failed to download metar using %s. Reason: %s.",
          this.downloader.getClass().getName(),
          ExceptionUtils.toFullString(result.exception));
      hasFailedAlready = true;
    } else {
      hasFailedAlready = false;
      Acc.log().writeLine(ApplicationLog.eType.info,
          "Metar downloaded successfully: %s", result.metar);
      try {
        this.updatedWeather = super.decodeFromMetar(result.metar);
      } catch (Exception ex) {
        Acc.log().writeLine(ApplicationLog.eType.warning,
            "Failed to decode weather. Reason: %s.",
            ExceptionUtils.toFullString(result.exception));
      }
    }
    inUpdate = false;
  }
}
