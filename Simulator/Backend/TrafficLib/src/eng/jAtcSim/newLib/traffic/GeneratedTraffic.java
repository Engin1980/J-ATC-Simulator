package eng.jAtcSim.newLib.traffic;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import static eng.jAtcSim.newLib.shared.SharedFactory.getRnd;

public abstract class GeneratedTraffic extends Traffic {
  private final CallsignGenerator callsignGenerator = new CallsignGenerator();

  public GeneratedTraffic(IReadOnlyList<MovementTemplate> movements) {
    super(movements);
  }

  protected int generateDelayMinutes(double probability, int perStepDelay) {
    int ret = 0;
    while (getRnd().nextDouble() < probability) {
      int del = getRnd().nextInt(perStepDelay);
      ret += del;
    }
    return ret;
  }

  protected Callsign generateRandomCallsign(@Nullable String prefix, boolean isCommercialFlight, boolean useExtendedCallsigns) {
    Callsign ret = this.callsignGenerator.generateCallsign(prefix, isCommercialFlight, useExtendedCallsigns);
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

  private final ISet<String> generatedCallsigns = new ESet<>();

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

  public Callsign generateCallsign(String prefix, boolean isCommercial, boolean useExtended) {
    Callsign ret = null;

    while (ret == null || this.generatedCallsigns.contains(ret.toString())) {
      String number;
      if (!isCommercial)
        number = generateNonCommercial(prefix);
      else {
        Type type = getCallsignType(useExtended);
        number = generateCommercial(type);
      }
      ret = new Callsign(prefix, number);
    }

    this.generatedCallsigns.add(ret.toString());
    return ret;
  }

}
