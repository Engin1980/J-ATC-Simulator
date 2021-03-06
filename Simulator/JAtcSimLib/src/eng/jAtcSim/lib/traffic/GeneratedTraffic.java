package eng.jAtcSim.lib.traffic;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.Callsign;

public abstract class GeneratedTraffic extends Traffic {
  private boolean useExtendedCallsigns = true;

  public boolean isUseExtendedCallsigns() {
    return useExtendedCallsigns;
  }

  public void setUseExtendedCallsigns(boolean useExtendedCallsigns) {
    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  protected int generateDelayMinutes() {
    int ret = 0;
    while (Acc.rnd().nextDouble() < getDelayProbability()) {
      int del = Acc.rnd().nextInt(getMaxDelayInMinutesPerStep());
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