/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar;

import jatcsimlib.exceptions.ERuntimeException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Marek
 */
public class SoundManager {

  private static AudioInputStream planeMessageStream = null;
  private static AudioInputStream atcMessageStream = null;
  private static Clip planeClip = null;
  private static Clip atcClip = null;

  static {
    File planeMessageFile = new File("C:\\Users\\Marek\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\plane.wav");
    File atcMessageFile = new File("C:\\Users\\Marek\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\atc.wav");
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

  static void playAtcNewMessage() {
    atcClip.setMicrosecondPosition(0);
    atcClip.start();
  }

  static void playPlaneNewMessage() {
    planeClip.setMicrosecondPosition(0);
    planeClip.start();
  }

}
