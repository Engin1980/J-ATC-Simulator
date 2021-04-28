package eng.jAtcSim.newPacks.context;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newPacks.IView;

public class ViewGlobalEventContext {
  public static abstract class EventsEventArgs {
    public final IView sender;

    public EventsEventArgs(IView sender) {
      EAssert.Argument.isNotNull(sender, "sender");

      this.sender = sender;
    }
  }

  public static class SelectedCallsignChangedEventArgs extends EventsEventArgs {

    public final Callsign callsign;

    public SelectedCallsignChangedEventArgs(IView sender, Callsign callsign) {
      super(sender);
      this.callsign = callsign;
    }
  }

  public static class RadarPositionStoreRestoreEventArgs extends EventsEventArgs {
    public enum EventAction {
      store,
      restore
    }

    public final EventAction action;
    public int bank;

    public RadarPositionStoreRestoreEventArgs(IView sender, EventAction action, int bank) {
      super(sender);
      this.action = action;
      this.bank = bank;
    }
  }

  public static class UnhandledKeyPressEventArgs extends EventsEventArgs {
    public final boolean isCtr;
    public final int keyCode;

    public UnhandledKeyPressEventArgs(IView sender, int keyCode, boolean isCtr) {
      super(sender);
      this.isCtr = isCtr;
      this.keyCode = keyCode;
    }
  }

  //TODEL if not used
//  public static class ShortcutSetEventArgs extends EventsEventArgs{
//    public final Object key;
//    public final Object value;
//
//    public ShortcutSetEventArgs(IView sender, Object key, Object value) {
//      super(sender);
//      this.key = key;
//      this.value = value;
//    }
//  }
//
//  public static class ShortcutDeleteEventArgs extends EventsEventArgs{
//    public final Object key;
//
//    public ShortcutDeleteEventArgs(IView sender, Object key) {
//      super(sender);
//      this.key = key;
//    }
//  }

  public EventAnonymous<SelectedCallsignChangedEventArgs> onSelectedCallsignChanged = new EventAnonymous<SelectedCallsignChangedEventArgs>();
  public EventAnonymous<RadarPositionStoreRestoreEventArgs> onRadarPositionStoreRestore = new EventAnonymous<RadarPositionStoreRestoreEventArgs>();
  public EventAnonymous<UnhandledKeyPressEventArgs> onUnhandledKeyPress = new EventAnonymous<>();
  //TODEL use or delete
//  public EventAnonymous<ShortcutSetEventArgs> onShortcutSet = new EventAnonymous<>();
//  public EventAnonymous<ShortcutDeleteEventArgs> onShortcutDelete = new EventAnonymous<>();


}
