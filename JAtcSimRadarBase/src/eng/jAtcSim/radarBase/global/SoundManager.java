package eng.jAtcSim.radarBase.global;

import eng.jAtcSim.lib.exceptions.ERuntimeException;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;



public class SoundManager {
  //TODO this class should be somehow rewritten to be a non-static and configurable

  private static AudioInputStream planeMessageStream = null;
  private static AudioInputStream atcMessageStream = null;
  private static Clip planeClip = null;
  private static Clip atcClip = null;

  public static void init(String wavFolderPath) {
    File planeMessageFile = new File(wavFolderPath + "plane.wav");
    File atcMessageFile = new File(wavFolderPath + "atc.wav");
    try {
      planeMessageStream = AudioSystem.getAudioInputStream(planeMessageFile);
      atcMessageStream = AudioSystem.getAudioInputStream(atcMessageFile);

      planeClip = AudioSystem.getClip();
      planeClip.open(planeMessageStream);

      atcClip = AudioSystem.getClip();
      atcClip.open(atcMessageStream);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
      throw new ERuntimeException("Sound area init fail!");
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
