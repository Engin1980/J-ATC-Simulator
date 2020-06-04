package eng.jAtcSim.recording;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.BitmapRadar.BitmapCanvas;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.abstractRadar.settings.RadarBehaviorSettings;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.abstractRadar.Radar;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.InitialPosition;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Recording {
  private final Settings settings;
  private final ISimulation simulation;
  private final Radar radar;
  private final BitmapCanvas bmpCanvas;
  private int imgCounter;
  private boolean isActive;

  public Recording(Settings settings, ISimulation sim, Area area, InitialPosition initialPosition,
                   RadarStyleSettings ss, RadarDisplaySettings ds, RadarBehaviorSettings bs) {

    File f = new File(settings.getPath());
    if (f.exists() == false || f.canWrite() == false) {
      throw new EApplicationException("Unable to create a new recorder to the folder " + settings.getPath() + ". Folder does not exist or write-access is not granted.");
    }

    this.simulation = sim;
    this.settings = settings;

    bmpCanvas = new BitmapCanvas(settings.getWidth(), settings.getHeight());
    bmpCanvas.getImageDrawn().add(this::bmpCanvas_imageDrawn);

    radar = new Radar(bmpCanvas, initialPosition, simulation, area, ss, ds, bs);

    this.radar.start(settings.getInterval(), settings.getInterval());
    this.isActive = true;
  }

  public boolean isActive() {
    return isActive;
  }

  public Settings getSettings() {
    return settings;
  }

  public void stop() {
    radar.stop();
    isActive = false;
  }

  private void bmpCanvas_imageDrawn(BitmapCanvas o) {
    if (!isActive) return;

    BufferedImage img = bmpCanvas.getGuiControl();
    imgCounter++;
    String imgType = this.settings.getImageType();
    String fullFileName = sf("%05d.%s", imgCounter, imgType);
    fullFileName = Paths.get(settings.getPath(), fullFileName).toString();
    try {
      switch (imgType) {
        case "jpg":
          saveJpg(img, fullFileName, this.settings.getJpgQuality());
          break;
        default:
          saveOther(img, imgType, fullFileName);
          break;
      }
    } catch (Exception e) {
      SharedAcc.getAppLog().writeLine(ApplicationLog.eType.critical,
          "Recording error. " + ExceptionUtils.toFullString(e, "\n\t"));
      this.stop();
    }
  }

  private void saveOther(BufferedImage img, String imageType, String fullFileName) throws IOException {
    ImageIO.write(img, imageType, new File(fullFileName));
  }

  private void saveJpg(BufferedImage image, String fullFileName, double quality) throws IOException {
    ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
    ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
    jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    jpgWriteParam.setCompressionQuality((float) quality);

    ImageOutputStream outputStream = new FileImageOutputStream(new File(fullFileName));
    jpgWriter.setOutput(outputStream);
    IIOImage outputImage = new IIOImage(image, null, null);
    jpgWriter.write(null, outputImage, jpgWriteParam);
    jpgWriter.dispose();
    outputStream.flush();
    outputStream.close();
  }
}
