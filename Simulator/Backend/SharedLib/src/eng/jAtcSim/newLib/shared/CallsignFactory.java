package eng.jAtcSim.newLib.shared;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.newLib.shared.contextLocal.Context;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ItemsDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.ItemsViaStringSerializer;

public class CallsignFactory {
  public enum Type {
    NXX,
    NNX,
    NNN
  }

  private static final Character[] numericalChars;
  private static final double COMPANY_THREE_CHAR_NUMBER_PROBABILITY = 0.3;
  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.3;

  public static CallsignFactory load(XElement element) {
    boolean uec = XmlLoadUtils.Field.loadFieldValue(element, "useExtendedCallsign", boolean.class);

    CallsignFactory ret = new CallsignFactory(uec);

    XmlLoadUtils.Field.restoreField(element, ret, "previouslyGeneratedCallsigns",
            new ItemsDeserializer(String.class, (e, q) -> e.getContent(), ret.previouslyGeneratedCallsigns));

    return ret;
  }


  private static Type getCallsignType(boolean useExtended) {
    ERandom rnd = Context.getApp().getRnd();
    Type ret;
    if (!useExtended)
      ret = Type.NNN;
    else {
      if (rnd.nextDouble() > EXTENDED_CALLSIGN_PROBABILITY)
        ret = Type.NNN;
      else if (rnd.nextDouble() < .5)
        ret = Type.NNX;
      else
        ret = Type.NXX;
    }
    return ret;
  }

  private static String generateCommercial(Type type) {
    ERandom rnd = Context.getApp().getRnd();
    StringBuilder ret = new StringBuilder();
    boolean addFourth = rnd.nextDouble() > COMPANY_THREE_CHAR_NUMBER_PROBABILITY;
    switch (type) {
      case NNN:
        ret.append((char) rnd.nextDouble('0', '9'));
        ret.append((char) rnd.nextDouble('0', '9'));
        ret.append((char) rnd.nextDouble('0', '9'));
        if (addFourth) ret.append((char) rnd.nextDouble('0', '9'));
        break;
      case NNX:
        ret.append((char) rnd.nextDouble('0', '9'));
        ret.append((char) rnd.nextDouble('0', '9'));
        ret.append(getNumericalChar());
        if (addFourth) ret.append(getNumericalChar());
        break;
      case NXX:
        ret.append((char) rnd.nextDouble('0', '9'));
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
      char c = (char) Context.getApp().getRnd().nextInt('A', 'Z');
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

  public void save(XElement target) {
    XmlSaveUtils.Field.storeField(target, this, "useExtendedCallsigns");
    XmlSaveUtils.Field.storeField(target, this, "previouslyGeneratedCallsigns",
            new ItemsViaStringSerializer<String>(q -> q));
  }
}
