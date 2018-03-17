package eng.jAtcSim.radarBase.global;

import eng.jAtcSim.lib.exceptions.ERuntimeException;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SoundManager {
  //TODO this class should be somehow rewritten to be a non-static and configurable


  private static Clip planeClip = null;
  private static Clip atcClip = null;
  private static Clip planeNegClip = null;
  private static Clip atcNegClip = null;

  public static void init(String wavFolderPath) {
    AudioInputStream audioStream = null;

    Path p = Paths.get(wavFolderPath);
    File planeMessageFile = Paths.get(wavFolderPath, "plane.wav").toFile();
    File atcMessageFile = Paths.get(wavFolderPath, "atc.wav").toFile();
    File planeNegMessageFile = Paths.get(wavFolderPath, "planeNeg.wav").toFile();
    File atcNegMessageFile = Paths.get(wavFolderPath, "atcNeg.wav").toFile();
    try {
      audioStream = AudioSystem.getAudioInputStream(planeMessageFile);
      planeClip = AudioSystem.getClip();
      planeClip.open(audioStream);

      audioStream = AudioSystem.getAudioInputStream(planeNegMessageFile);
      planeNegClip = AudioSystem.getClip();
      planeNegClip.open(audioStream);

      audioStream = AudioSystem.getAudioInputStream(atcMessageFile);
      atcClip = AudioSystem.getClip();
      atcClip.open(audioStream);

      audioStream = AudioSystem.getAudioInputStream(atcNegMessageFile);
      atcNegClip = AudioSystem.getClip();
      atcNegClip.open(audioStream);

    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
      throw new ERuntimeException("Sound area init fail!", ex);
    }
  }

  public static void playAtcNewMessage(boolean isNegative) {
    if (!isNegative) {
      atcClip.setMicrosecondPosition(0);
      atcClip.start();
    } else {
      atcNegClip.setMicrosecondPosition(0);
      atcNegClip.start();
    }
  }

  public static void playPlaneNewMessage(boolean isNegative) {
    if (!isNegative) {
      planeClip.setMicrosecondPosition(0);
      planeClip.start();
    } else {
      planeNegClip.setMicrosecondPosition(0);
      planeNegClip.start();
    }
  }
}
