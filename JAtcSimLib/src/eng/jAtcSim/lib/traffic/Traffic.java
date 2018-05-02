/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.world.*;
import sun.security.x509.IssuingDistributionPointExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marek Vajgl
 */
public abstract class Traffic {

  private static final double COMPANY_THREE_CHAR_NUMBER_PROBABILITY = 0.3;
  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.3;

  private String title;
  @XmlOptional
  private String description;
  /**
   * Specifies delay probability, range 0.0-1.0.
   */
  private final double delayProbability = 0.3;
  /**
   * Max delay in minutes per step.
   */
  private final int maxDelayInMinutesPerStep = 15;
  /**
   * Specifies if extended callsigns containing characters at the end can be used.
   */
  private boolean useExtendedCallsigns = true;

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public void setUseExtendedCallsigns(boolean useExtendedCallsigns) {
    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  public abstract GeneratedMovementsResponse generateMovements(Object syncObject);

  protected int generateDelayMinutes() {
    int ret = 0;
    while (Acc.rnd().nextDouble() < delayProbability) {
      int del = Acc.rnd().nextInt(maxDelayInMinutesPerStep);
      ret += del;
    }
    return ret;
  }

  protected Callsign generateUnusedCallsign(String companyOrCountryPrefix, boolean isPrefixCountryCode) {
    Callsign ret = null;

    while (ret == null) {

      ret = generateRandomCallsign(companyOrCountryPrefix, isPrefixCountryCode);
      for (Airplane p : Acc.planes()) { // check not existing in current planes
        if (ret.equals(p.getCallsign())) {
          ret = null;
          break;
        }
      }
      for (Movement m : Acc.scheduledMovements()) { // check not existing in future planes
        if (m.getCallsign().equals(ret)) {
          ret = null;
          break;
        }
      }

    }

    return ret;
  }

  private Callsign generateRandomCallsign(@Nullable String prefix, boolean isPrefixCountryCode) {
    String number;
    boolean useExtendedNow = this.useExtendedCallsigns && Acc.rnd().nextDouble() < EXTENDED_CALLSIGN_PROBABILITY;

    if (!isPrefixCountryCode) {
      int length = (Acc.rnd().nextDouble() < COMPANY_THREE_CHAR_NUMBER_PROBABILITY) ? 3 : 4;
      number = getRandomCallsignNumber(true, useExtendedNow, length);
    } else {
      number = getRandomCallsignNumber(false, true, 5 - prefix.length());
    }
    Callsign ret = new Callsign(prefix, number);
    return ret;
  }

  private String getRandomCallsignNumber(boolean useNumbers, boolean useChars, int length) {
    char[] tmp = new char[length];
    boolean isNumber = useNumbers;

    for (int i = 0; i < length; i++) {
      if (isNumber)
        tmp[i] = getRandomCallsignChar('0','9' );
      else
        tmp[i] = getRandomCallsignChar('A','Z' );
      if (useChars && useNumbers){
        if ((i+2) == length)
          isNumber = false;
        else if ((length == 4 && i == 1) || length == 3)
          isNumber = Math.random() > 0.5;
      }
    }

    String ret = new String(tmp);
    return ret;
  }

  private char getRandomCallsignChar(char from, char to) {
    int val = Acc.rnd().nextInt(from, to + 1);
    char ret = (char) val;
    return ret;
  }
}
