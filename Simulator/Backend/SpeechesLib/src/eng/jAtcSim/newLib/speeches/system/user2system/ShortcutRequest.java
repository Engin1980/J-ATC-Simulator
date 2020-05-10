package eng.jAtcSim.newLib.speeches.system.user2system;

import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;

public class ShortcutRequest implements ISystemUserRequest {
  public enum eType {
    get,
    delete,
    set
  }

  public static ShortcutRequest createDelete(String key) {
    return new ShortcutRequest(eType.delete, key, null);
  }

  public static ShortcutRequest createGet() {
    return new ShortcutRequest(eType.get, null, null);
  }

  public static ShortcutRequest createSet(String key, String value) {
    return new ShortcutRequest(eType.set, key, value);
  }

  private final eType type;
  private final String key;
  private final String value;

  private ShortcutRequest(eType type, String key, String value) {
    this.type = type;
    this.key = key;
    this.value = value;
  }

  public eType getType() {
    return type;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
