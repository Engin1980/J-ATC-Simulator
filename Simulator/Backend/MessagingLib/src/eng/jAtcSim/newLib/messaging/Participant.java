package eng.jAtcSim.newLib.messaging;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.Callsign;

public class Participant {
  public enum eType {
    atc,
    airplane,
    user,
    system
  }
  private static final Participant systemParticipant = new Participant(eType.system, "-system-");
  private static final Participant userParticipant = new Participant(eType.user, "-user-");

  public static Participant createAirplane(Callsign callsign) {
    return new Participant(eType.airplane, callsign.toString());
  }

  public static Participant createAtc(String id) {
    return new Participant(eType.atc, id);
  }

  public static Participant createSystem() {
    return systemParticipant;
  }

  public static Participant createUser() {
    return userParticipant;
  }
  private final eType type;
  private final String id;

  private Participant(eType type, String id) {
    this.type = type;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public eType getType() {
    return type;
  }

  @Override
  public String toString(){
    switch (type){
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
