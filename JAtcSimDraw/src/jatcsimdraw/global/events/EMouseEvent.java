///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jatcsimdraw.global.events;
//
//import jatcsimdraw.global.Point;
//import jatcsimlib.exceptions.ERuntimeException;
//
///**
// *
// * @author Marek
// */
//public class EMouseEvent {
//
//
//  public enum eType {
//
//    Click,
//    DoubleClick,
//    Drag,
//    Move,
//    WheelScroll;
//  }
//
//  public enum eButton {
//
//    none,
//    left,
//    middle,
//    right,
//    other;
//
//    public static eButton convertFromSpringButton(int value) {
//      eButton ret;
//      switch (value) {
//        case 0:
//          ret = eButton.none;
//          break;
//        case java.awt.event.MouseEvent.BUTTON1:
//          ret = eButton.left;
//          break;
//        case java.awt.event.MouseEvent.BUTTON2:
//          ret = eButton.middle;
//          break;
//        case java.awt.event.MouseEvent.BUTTON3:
//          ret = eButton.right;
//          break;
//        default:
//          ret = eButton.other;
//          break;
//      }
//      return ret;
//    }
//  }
//
//  public final int x;
//  public final int y;
//  public final int dropX;
//  public final int dropY;
//  public final eButton button;
//  public final EKeyboardModifier modifiers;
//  public final eType type;
//  public final int wheel;
//
//  public EMouseEvent(int x, int y, int dropX, int dropY, int wheel, eButton button, EKeyboardModifier modifiers, eType type) {
//
//    if (wheel != 0 && type != eType.WheelScroll){
//      throw new IllegalArgumentException("Wheel parameter can be set only for WheelScroll type. Otherwise it should be 0.");
//    }
//    if (type == eType.Move && button != eButton.none){
//      throw new IllegalArgumentException("Cannot set button type other than \"none\" for mouse event type \"move\".");
//    }
//    if ((dropX != 0 || dropY != 0) && type != type.Drag){
//      throw new IllegalArgumentException("Parameters \"dropX/Y\" can be set only for type \"Drag\", otherwise they should be 0.");
//    }
//
//    this.x = x;
//    this.y = y;
//    this.dropX = dropX;
//    this.dropY = dropY;
//    this.button = button;
//    this.modifiers = modifiers;
//    this.type = type;
//    this.wheel = wheel;
//  }
//
//  public static EMouseEvent createClick(int x, int y, eType type, eButton button, EKeyboardModifier modifiers) {
//    EMouseEvent ret = new EMouseEvent(x, y, 0, 0, 0, button, modifiers, type);
//    return ret;
//  }
//
//  public static EMouseEvent createScroll(int x, int y, int wheel) {
//    EMouseEvent ret = new EMouseEvent(x, y, 0, 0, wheel, eButton.other, EKeyboardModifier.NONE, eType.WheelScroll);
//    return ret;
//  }
//
//  public static EMouseEvent createDrag(int x, int y, int dropX, int dropY, eButton button, EKeyboardModifier modifiers) {
//    EMouseEvent ret = new EMouseEvent(x, y, dropX, dropY, 0, button, modifiers, eType.Drag);
//    return ret;
//  }
//
//  public static EMouseEvent createMove(int x, int y){
//    EMouseEvent ret = new EMouseEvent(x, y, 0, 0, 0, eButton.none, EKeyboardModifier.NONE, eType.Move);
//    return ret;
//  }
//
//  public Point getPoint() {
//    return new Point(x, y);
//  }
//
//  public Point getDropPoint() {
//    if (type == eType.Drag) {
//      return new Point(this.dropX, this.dropY);
//    } else {
//      return null;
//    }
//  }
//
//  public Point getDropRangePoint() {
//    int diffX = this.dropX - this.x;
//    int diffY = this.dropY - this.y;
//    Point ret = new Point(diffX, diffY);
//    return ret;
//  }
//}
