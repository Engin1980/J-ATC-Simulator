/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import com.sun.istack.internal.Nullable;
import com.sun.javafx.iio.common.ImageLoaderImpl;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
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
  private static final char[] CALLSIGN_NUMBERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
  /**
   * Specifies delay probability, range 0.0-1.0.
   */
  private final double delayProbability = 0.3;
  /**
   * Max delay in minutes per step.
   */
  private final int maxDelayInMinutesPerStep = 15;
  private String title;
  @XmlOptional
  private String description;
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
    Callsign ret =
        CallsignGenerator.generateCallsign(prefix, !isPrefixCountryCode, useExtendedCallsigns);
    return ret;
  }
}

class CallsignGenerator {

  public enum Type {
    NXX,
    NNX,
    NNN
  }
  public static Character[]numericalChars;

  static{
    IList<Character> tmp = new EList<>();
    for (int i = 'A'; i <= 'Z'; i++) {
      tmp.add((char)i);
    }
    tmp.remove('I');
    tmp.remove('O');
    tmp.remove('Q');
    numericalChars = tmp.toArray(Character.class);
  }

  private static final double COMPANY_THREE_CHAR_NUMBER_PROBABILITY = 0.3;
  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.3;

  public static Callsign generateCallsign(String prefix, boolean isCommercial, boolean useExtended) {
    String number;
    if (!isCommercial)
      number = generateNonCommercial(prefix);
    else {
      Type type = getCallsignType(useExtended);
      number = generateCommercial(type);
    }

    Callsign ret = new Callsign(prefix, number);
    return ret;
  }

  private static Type getCallsignType(boolean useExtended) {
    Type ret;
    if (!useExtended)
      ret = Type.NNN;
    else{
      if (Acc.rnd().nextDouble() > EXTENDED_CALLSIGN_PROBABILITY)
        ret = Type.NNN;
      else if (Acc.rnd().nextDouble() < .5)
        ret = Type.NNX;
      else
        ret = Type.NXX;
    }
    return ret;
  }

  private static String generateCommercial(Type type) {
    StringBuilder ret = new StringBuilder();
    boolean addFourth = Acc.rnd().nextDouble() > COMPANY_THREE_CHAR_NUMBER_PROBABILITY;
    switch (type) {
      case NNN:
        ret.append((char) Acc.rnd().nextDouble('0', '9'));
        ret.append((char) Acc.rnd().nextDouble('0', '9'));
        ret.append((char) Acc.rnd().nextDouble('0', '9'));
        if (addFourth) ret.append((char) Acc.rnd().nextDouble('0', '9'));
        break;
      case NNX:
        ret.append((char) Acc.rnd().nextDouble('0', '9'));
        ret.append((char) Acc.rnd().nextDouble('0', '9'));
        ret.append(getNumericalChar());
        if (addFourth) ret.append(getNumericalChar());
        break;
      case NXX:
        ret.append((char) Acc.rnd().nextDouble('0', '9'));
        ret.append(getNumericalChar());
        ret.append(getNumericalChar());
        if (addFourth) ret.append(getNumericalChar());
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }

    return ret.toString();
  }

  private static char getNumericalChar() {
    char ret = ArrayUtils.getRandom(numericalChars);
    return ret;
  }

  private static String generateNonCommercial(String prefix) {
    StringBuilder ret = new StringBuilder();
    for (int i = prefix.length(); i < 5; i++) {
      char c = (char) Acc.rnd().nextInt('A', 'Z');
      ret.append(c);
    }
    return ret.toString();
  }

}

