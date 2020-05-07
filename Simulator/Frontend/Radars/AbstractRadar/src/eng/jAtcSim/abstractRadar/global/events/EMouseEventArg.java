package eng.jAtcSim.abstractRadar.global.events;

import eng.jAtcSim.abstractRadar.global.Point;

/**
 * @author Marek
 */
public class EMouseEventArg {

  public enum eType {

    click,
    doubleClick,
    dragged,
    dragging,
    move,
    wheelScroll;
  }

  public enum eButton {

    none,
    left,
    middle,
    right,
    other;
  }

  public final int x;
  public final int y;
  public final int dropX;
  public final int dropY;
  public final eButton button;
  public final EKeyboardModifier modifiers;
  public final eType type;
  public final int wheel;

  public static EMouseEventArg createClick(int x, int y, eType type, eButton button, EKeyboardModifier modifiers) {
    EMouseEventArg ret = new EMouseEventArg(x, y, 0, 0, 0, button, modifiers, type);
    return ret;
  }

  public static EMouseEventArg createScroll(int x, int y, int wheel) {
    EMouseEventArg ret = new EMouseEventArg(x, y, 0, 0, wheel, eButton.other, EKeyboardModifier.NONE, eType.wheelScroll);
    return ret;
  }

  public static EMouseEventArg createDragged(int x, int y, int dropX, int dropY, eButton button, EKeyboardModifier modifiers) {
    EMouseEventArg ret = new EMouseEventArg(x, y, dropX, dropY, 0, button, modifiers, eType.dragged);
    return ret;
  }

  public static EMouseEventArg createDragging(int x, int y, int dropX, int dropY, eButton button, EKeyboardModifier modifiers) {
    EMouseEventArg ret = new EMouseEventArg(x, y, dropX, dropY, 0, button, modifiers, eType.dragging);
    return ret;
  }

  public static EMouseEventArg createMove(int x, int y) {
    EMouseEventArg ret = new EMouseEventArg(x, y, 0, 0, 0, eButton.none, EKeyboardModifier.NONE, eType.move);
    return ret;
  }

  private EMouseEventArg(int x, int y, int dropX, int dropY, int wheel, eButton button, EKeyboardModifier modifiers, eType type) {

    if (wheel != 0 && type != eType.wheelScroll) {
      throw new IllegalArgumentException("Wheel parameter can be set only for wheelScroll kind. Otherwise it should be 0.");
    }
    if (type == eType.move && button != eButton.none) {
      throw new IllegalArgumentException("Cannot set button kind other than \"none\" for mouse event kind \"move\".");
    }
    if ((dropX != 0 || dropY != 0) && type != type.dragged && type != type.dragging) {
      throw new IllegalArgumentException("Parameters \"dropX/Y\" can be set only for kind \"dragged\", otherwise they should be 0." +
          " (kind " + type + ")");
    }

    this.x = x;
    this.y = y;
    this.dropX = dropX;
    this.dropY = dropY;
    this.button = button;
    this.modifiers = modifiers;
    this.type = type;
    this.wheel = wheel;
  }

  public Point getPoint() {
    return new Point(x, y);
  }

  public Point getDropPoint() {
    if (type == eType.dragged || type == eType.dragging) {
      return new Point(this.dropX, this.dropY);
    } else {
      return null;
    }
  }

  public Point getDropRangePoint() {
    int diffX = this.dropX - this.x;
    int diffY = this.dropY - this.y;
    Point ret = new Point(diffX, diffY);
    return ret;
  }
}
