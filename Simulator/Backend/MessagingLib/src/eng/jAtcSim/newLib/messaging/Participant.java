package eng.jAtcSim.newLib.messaging;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;

import java.util.Objects;

public class Participant implements IXPersistable {
  public enum eType {
    atc,
    airplane,
    user,
    system
  }

  private static final Participant systemParticipant = new Participant(eType.system, "-system-");
  private static final Participant userParticipant = new Participant(eType.user, "-user-");

  public static Participant createAirplane(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    return new Participant(eType.airplane, callsign.toString());
  }

  public static Participant createAtc(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");
    return new Participant(eType.atc, atcId.getName());
  }

  public static Participant createAtc(String atcName) {
    EAssert.Argument.isNotNull(atcName, "atcName");
    return new Participant(eType.atc, atcName);
  }

  public static Participant createSystem() {
    return systemParticipant;
  }

  public static Participant createUser() {
    return userParticipant;
  }

  private final eType type;
  private final String id;

  @XmlConstructor
  private Participant(eType type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Participant that = (Participant) o;
    return type == that.type &&
        id.equals(that.id);
  }

  public String getId() {
    return id;
  }

  public eType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id);
  }

  @Override
  public String toString() {
    switch (type) {
      case airplane:
        return "✉ " + id + " (plane)";
      case atc:
        return "✉ " + id + " (atc)";
      case system:
        return "✉ SYS";
      case user:
        return "✉ USR";
      default:
        throw new EEnumValueUnsupportedException(type);
    }
  }
}
