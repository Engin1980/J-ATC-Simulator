package eng.jAtcSim.radarBase.global;

import eng.eSystem.exceptions.EApplicationException;

import javax.sound.sampled.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SoundManager {
  //TODO this class should be somehow rewritten to be a non-static and configurable

  private static Clip planeClip = null;
  private static Clip atcClip = null;
  private static Clip planeNegClip = null;
  private static Clip atcNegClip = null;
  private static Clip systemClip = null;
  private static Clip airproxClip = null;
  private static boolean enabled = true;

  public static void init(String wavFolderPath) {
    AudioInputStream audioStream;

    Path p = Paths.get(wavFolderPath);
    File planeMessageFile = Paths.get(wavFolderPath, "plane.wav").toFile();
    File atcMessageFile = Paths.get(wavFolderPath, "atc.wav").toFile();
    File planeNegMessageFile = Paths.get(wavFolderPath, "planeNeg.wav").toFile();
    File atcNegMessageFile = Paths.get(wavFolderPath, "atcNeg.wav").toFile();
    File systemMessageFile = Paths.get(wavFolderPath, "system.wav").toFile();
    File airproxFile = Paths.get(wavFolderPath, "airprox.wav").toFile();
    try {
      planeClip = loadAndOpenClip(planeMessageFile);
      planeNegClip = loadAndOpenClip(planeNegMessageFile);
      atcClip = loadAndOpenClip(atcMessageFile);
      atcNegClip = loadAndOpenClip(atcNegMessageFile);
      systemClip = loadAndOpenClip(systemMessageFile);
      airproxClip = loadAndOpenClip(airproxFile);

    } catch (EApplicationException ex) {
      throw new EApplicationException("Sound area init fail!", ex);
    }
  }

  private static Clip loadAndOpenClip(File file){
    Clip ret;
    try {
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
      AudioFormat format = audioStream.getFormat();
      DataLine.Info info = new DataLine.Info(Clip.class, format);
      ret = (Clip) AudioSystem.getLine(info);
      ret.open(audioStream);
    } catch (Exception ex){
      throw new EApplicationException("Unable to initialize sound clip from " + file + ".", ex);
    }
    return ret;
  }

  public static void playAtcNewMessage(boolean isNegative) {
    if (!isNegative) {
      playClip(atcClip);
    } else {
      playClip(atcNegClip);
    }
  }

  public static void playPlaneNewMessage(boolean isNegative) {
    if (!isNegative) {
      playClip(planeClip);
    } else {
      playClip(planeNegClip);
    }
  }

  public static void switchEnabled() {
    SoundManager.enabled = !SoundManager.enabled;
  }

  public static boolean isEnabled() {
    return SoundManager.enabled;
  }

  public static void setEnabled(boolean enabled) {
    SoundManager.enabled = enabled;
  }

  public static void playSystemMessage() {
    playClip(systemClip);
  }

  public static void playAirprox() {
    playClip(airproxClip);
  }

  private static void playClip(Clip clip) {
    if (enabled) {
      clip.setMicrosecondPosition(0);
      clip.start();
    }
  }
}
