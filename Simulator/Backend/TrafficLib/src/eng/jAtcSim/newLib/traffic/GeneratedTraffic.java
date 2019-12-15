package eng.jAtcSim.newLib.traffic;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.newLib.shared.Callsign;

import static eng.jAtcSim.newLib.shared.SharedFactory.getRnd;

public abstract class GeneratedTraffic extends Traffic {
  private final boolean useExtendedCallsigns;

  public GeneratedTraffic(double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns) {
    super(delayProbability, maxDelayInMinutesPerStep);
    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  public boolean isUseExtendedCallsigns() {
    return useExtendedCallsigns;
  }

  protected int generateDelayMinutes() {
    int ret = 0;
    while (getRnd().nextDouble() < getDelayProbability()) {
      int del = getRnd().nextInt(getMaxDelayInMinutesPerStep());
      ret += del;
    }
    return ret;
  }

  private Callsign generateRandomCallsign(@Nullable String prefix, boolean isPrefixCountryCode) {
    Callsign ret = CallsignGenerator.generateCallsign(prefix, !isPrefixCountryCode, useExtendedCallsigns);
    return ret;
  }
}

class CallsignGenerator {

  public enum Type {
    NXX,
    NNX,
    NNN
  }

  private static Character[] numericalChars;
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
    else {
      if (getRnd().nextDouble() > EXTENDED_CALLSIGN_PROBABILITY)
        ret = Type.NNN;
      else if (getRnd().nextDouble() < .5)
        ret = Type.NNX;
      else
        ret = Type.NXX;
    }
    return ret;
  }

  private static String generateCommercial(Type type) {
    StringBuilder ret = new StringBuilder();
    boolean addFourth = getRnd().nextDouble() > COMPANY_THREE_CHAR_NUMBER_PROBABILITY;
    switch (type) {
      case NNN:
        ret.append((char) getRnd().nextDouble('0', '9'));
        ret.append((char) getRnd().nextDouble('0', '9'));
        ret.append((char) getRnd().nextDouble('0', '9'));
        if (addFourth) ret.append((char) getRnd().nextDouble('0', '9'));
        break;
      case NNX:
        ret.append((char) getRnd().nextDouble('0', '9'));
        ret.append((char) getRnd().nextDouble('0', '9'));
        ret.append(getNumericalChar());
        if (addFourth) ret.append(getNumericalChar());
        break;
      case NXX:
        ret.append((char) getRnd().nextDouble('0', '9'));
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
      char c = (char) getRnd().nextInt('A', 'Z');
      ret.append(c);
    }
    return ret.toString();
  }

  static {
    IList<Character> tmp = new EList<>();
    for (int i = 'A'; i <= 'Z'; i++) {
      tmp.add((char) i);
    }
    tmp.remove('I');
    tmp.remove('O');
    tmp.remove('Q');
    numericalChars = tmp.toArray(Character.class);
  }

}
