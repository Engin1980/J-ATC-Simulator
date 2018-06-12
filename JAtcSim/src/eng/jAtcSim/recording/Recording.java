package eng.jAtcSim.recording;

import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.BitmapRadar.BitmapCanvas;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.InitialPosition;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.radarBase.DisplaySettings;
import eng.jAtcSim.radarBase.Radar;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Recording {
  private final Settings settings;
  private final Simulation simulation;
  private final Radar radar;
  private final BitmapCanvas bmpCanvas;
  private final IEventListenerSimple<Simulation> listener;
  private int imgCounter;
  private int secondCounter;
  private boolean isActive;

  public boolean isActive() {
    return isActive;
  }

  public Recording(Settings settings, Simulation sim, Area area, InitialPosition initialPosition,
                   DisplaySettings ds, BehaviorSettings bs) {

    File f = new File(settings.getPath());
    if (f.exists() == false || f.canWrite() == false){
      throw new EApplicationException("Unable to create a new recorder to the folder " + settings.getPath() + ". Folder does not exist or write-access is not granted.");
    }

    this.simulation = sim;
    this.settings = settings;

    bmpCanvas = new BitmapCanvas(settings.getWidth(), settings.getHeight());
    bmpCanvas.getImageDrawn().add(this::bmpCanvas_imageDrawn);

    ds.refreshRate = settings.getInterval(); // set custom refresh rate
    radar = new Radar(bmpCanvas, initialPosition, simulation, area, ds, bs);

    listener = s -> simulationSecondElapsed();
    this.secondCounter = 0;
    simulation.getSecondElapsedEvent().add(listener);
    this.radar.start();
    this.isActive = true;
  }

  private void bmpCanvas_imageDrawn(BitmapCanvas o) {
    assert isActive;

    BufferedImage img = bmpCanvas.getGuiControl();
    imgCounter++;
    String imgType = this.settings.getImageType();
    String fullFileName = sf("%05d.%s", imgCounter, imgType);
    fullFileName = Paths.get(settings.getPath(), fullFileName).toString();
    try {
      ImageIO.write(img, imgType, new File(fullFileName));
    } catch (Exception e) {
      System.out.println("Recording error. " + ExceptionUtil.toFullString(e, "\n\t"));
      this.stop();
    }
  }

  public Settings getSettings() {
    return settings;
  }

  public void stop() {
    simulation.getSecondElapsedEvent().remove(listener);
    isActive = false;
    radar.stop();
  }

  private void simulationSecondElapsed() {
    assert isActive;

    secondCounter++;
    if (secondCounter >= this.settings.getInterval()) {
      secondCounter = 0;
      radar.redraw(true);
    }
  }
}
