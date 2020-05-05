/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ArrayUtils;

import java.util.Objects;

public class Callsign {

  private static final ISet<String> previouslyGeneratedCallsigns = new ESet<>();
  private static final CallsignGenerator generator = new CallsignGenerator();

  public static Callsign generateCommercial(String companyIcao) {
    Callsign ret;
    do {
      ret = generator.generateCallsign(companyIcao, true, SharedAcc.getSettings().isUseExtendedCallsigns());
    } while (previouslyGeneratedCallsigns.contains(ret.toString()));
    previouslyGeneratedCallsigns.add(ret.toString());
    return ret;
  }

  public static Callsign generateGeneralAviation(String countryAircraftPrefix) {
    Callsign ret;
    do {
      ret = generator.generateCallsign(countryAircraftPrefix, false, SharedAcc.getSettings().isUseExtendedCallsigns());
    } while (previouslyGeneratedCallsigns.contains(ret.toString()));
    previouslyGeneratedCallsigns.add(ret.toString());
    return ret;
  }

  private final String company;
  private final String number;

  public Callsign(String company, String number) {
    this.company = company;
    this.number = number;
  }


  public Callsign(String value) {
    if (value == null)
      throw new IllegalArgumentException("Callsign string cannot be null");
    if (value.length() < 4)
      throw new IllegalArgumentException("Callsign string must be at least 4 chars long.");

    this.company = value.substring(0, 3).toUpperCase();
    this.number = value.substring(3).toUpperCase();
  }

//  public static String getRandomNumber(boolean isIfr, boolean useExtendedCallsigns) {
//    StringBuilder sb = new StringBuilder();
//    if (isIfr) {
//      if (useExtendedCallsigns && Acc.rnd().nextDouble() < EXTENDED_CALLSIGN_PROBABILITY) {
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('A', 'Z'));
//        sb.append(getRandomChar('A', 'Z'));
//      } else {
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('0', '9'));
//      }
//    } else {
//      sb.append(getRandomChar('A', 'Z'));
//      sb.append(getRandomChar('A', 'Z'));
//      sb.append(getRandomChar('A', 'Z'));
//    }
//    return sb.toString();
//  }
//
//  private static char getRandomChar(char from, char to) {
//    int val = Acc.rnd().nextInt(from, to + 1);
//    char ret = (char) val;
//    return ret;
//  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Callsign other = (Callsign) obj;
    if (!Objects.equals(this.company, other.company)) {
      return false;
    }
    if (!Objects.equals(this.number, other.number)) {
      return false;
    }
    return true;
  }

  public String getCompany() {
    return company;
  }

  public String getNumber() {
    return number;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.company);
    hash = 97 * hash + Objects.hashCode(this.number);
    return hash;
  }

  /**
   * Returns company and number together, without separating space, e.g. EZY5405.
   *
   * @return Company and number together
   */
  @Override
  public String toString() {
    return company + " " + number;
  }

  public String toString(boolean separateCompanyAndNumber) {
    if (separateCompanyAndNumber)
      return company + " " + number;
    else
      return company + number;
  }
}


class CallsignGenerator {

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