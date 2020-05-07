package eng.jAtcSim.abstractRadar.global.events;

/**
 *
 * @author Marek Vajgl
 */
public class EKeyboardModifier {

  public final boolean alt;
  public final boolean ctr;
  public final boolean shift;

  public static final EKeyboardModifier NONE = new EKeyboardModifier(false, false, false);

  public EKeyboardModifier(boolean alt, boolean ctr, boolean shift) {
    this.alt = alt;
    this.ctr = ctr;
    this.shift = shift;
  }

  public EKeyboardModifier(int springModifierValue) {
    boolean isAlt = (springModifierValue & java.awt.event.InputEvent.ALT_MASK) > 0;
    boolean isCtr = (springModifierValue & java.awt.event.InputEvent.CTRL_MASK) > 0;
    boolean isShift = (springModifierValue & java.awt.event.InputEvent.SHIFT_MASK) > 0;

    this.alt = isAlt;
    this.ctr = isCtr;
    this.shift = isShift;
  }

  public boolean is(boolean alt, boolean ctr, boolean shift){
    return alt == this.alt && ctr == this.ctr && shift == this.shift;
  }

}
