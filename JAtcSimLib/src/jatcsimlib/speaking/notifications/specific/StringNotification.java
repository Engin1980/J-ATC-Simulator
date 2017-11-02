package jatcsimlib.speaking.notifications.specific;

import jatcsimlib.speaking.notifications.Notification;

public class StringNotification extends Notification {
  private final String text;

  public StringNotification(String text) {
    if (text == null) {
        throw new IllegalArgumentException("Value of {text} cannot not be null.");
    }

    this.text = text;
  }

  public StringNotification(String text, Object ... params){
    if (text == null) {
        throw new IllegalArgumentException("Value of {text} cannot not be null.");
    }

    String txt =
        String.format(text, params);
    this.text = txt;
  }
}
