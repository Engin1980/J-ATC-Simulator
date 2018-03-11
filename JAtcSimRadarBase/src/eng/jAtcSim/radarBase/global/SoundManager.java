package eng.jAtcSim.radarBase.global;

import eng.jAtcSim.lib.exceptions.ERuntimeException;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SoundManager {
  //TODO this class should be somehow rewritten to be a non-static and configurable

  private static AudioInputStream planeMessageStream = null;
  private static AudioInputStream atcMessageStream = null;
  private static Clip planeClip = null;
  private static Clip atcClip = null;

  public static void init(String wavFolderPath) {
    Path p = Paths.get(wavFolderPath);
    File planeMessageFile = Paths.get(wavFolderPath, "plane.wav").toFile();
    File atcMessageFile = Paths.get(wavFolderPath , "atc.wav").toFile();
    try {
      planeMessageStream = AudioSystem.getAudioInputStream(planeMessageFile);
      atcMessageStream = AudioSystem.getAudioInputStream(atcMessageFile);

      planeClip = AudioSystem.getClip();
      planeClip.open(planeMessageStream);

      atcClip = AudioSystem.getClip();
      atcClip.open(atcMessageStream);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
      throw new ERuntimeException("Sound area init fail!", ex);
    }
  }

  public static void playAtcNewMessage() {
    atcClip.setMicrosecondPosition(0);
    atcClip.start();
  }

  public static void playPlaneNewMessage() {
    planeClip.setMicrosecondPosition(0);
    planeClip.start();
  }
}
