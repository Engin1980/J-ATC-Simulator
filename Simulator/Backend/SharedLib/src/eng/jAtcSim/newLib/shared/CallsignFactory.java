package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ArrayUtils;

public class CallsignFactory {
  public enum Type {
    NXX,
    NNX,
    NNN
  }

  private static final Character[] numericalChars;
  private static final double COMPANY_THREE_CHAR_NUMBER_PROBABILITY = 0.3;
  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.3;

  private static Type getCallsignType(boolean useExtended) {
    Type ret;
    if (!useExtended)
      ret = Type.NNN;
    else {
      if (SharedAcc.getRnd().nextDouble() > EXTENDED_CALLSIGN_PROBABILITY)
        ret = Type.NNN;
      else if (SharedAcc.getRnd().nextDouble() < .5)
        ret = Type.NNX;
      else
        ret = Type.NXX;
    }
    return ret;
  }

  private static String generateCommercial(Type type) {
    StringBuilder ret = new StringBuilder();
    boolean addFourth = SharedAcc.getRnd().nextDouble() > COMPANY_THREE_CHAR_NUMBER_PROBABILITY;
    switch (type) {
      case NNN:
        ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
        ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
        ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
        if (addFourth) ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
        break;
      case NNX:
        ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
        ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
        ret.append(getNumericalChar());
        if (addFourth) ret.append(getNumericalChar());
        break;
      case NXX:
        ret.append((char) SharedAcc.getRnd().nextDouble('0', '9'));
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
      char c = (char) SharedAcc.getRnd().nextInt('A', 'Z');
      ret.append(c);
    }
    return ret.toString();
  }

  private final boolean useExtendedCallsigns;
  private final ISet<String> previouslyGeneratedCallsigns = new ESet<>();

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

  public CallsignFactory(boolean useExtendedCallsigns) {
    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  public Callsign generate(String prefix, boolean isCommercial) {
    Callsign ret = null;

    while (ret == null || this.previouslyGeneratedCallsigns.contains(ret.toString())) {
      String number;
      if (!isCommercial)
        number = generateNonCommercial(prefix);
      else {
        Type type = getCallsignType(useExtendedCallsigns);
        number = generateCommercial(type);
      }
      ret = new Callsign(prefix, number);
    }

    this.previouslyGeneratedCallsigns.add(ret.toString());
    return ret;
  }

  public Callsign generateCommercial(String companyIcao) {
    Callsign ret = generate(companyIcao, true);
    return ret;
  }

  public Callsign generateGeneralAviation(String countryAircraftPrefix) {
    Callsign ret = generate(countryAircraftPrefix, false);
    return ret;
  }
}
