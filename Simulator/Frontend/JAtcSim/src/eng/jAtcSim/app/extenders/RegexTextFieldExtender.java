//package eng.jAtcSim.app.extenders;
//
//import eng.eSystem.utilites.ArrayUtils;
//import eng.eSystem.validation.EAssert;
//
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import java.awt.*;
//import java.util.regex.Pattern;
//
//public class RegexTextFieldExtender {
//  private static final Color FAIL_COLOR = new Color(255, 150, 150);
//  private final JTextField txt;
//  private Pattern regexPattern = Pattern.compile(".?");
//  private Color okColor = null;
//
//  public RegexTextFieldExtender() {
//    this.txt = new JTextField();
//    this.txt.getDocument().addDocumentListener(new DocumentListener() {
//      @Override
//      public void insertUpdate(DocumentEvent e) {
//        update();
//      }
//
//      @Override
//      public void removeUpdate(DocumentEvent e) {
//        update();
//      }
//
//      @Override
//      public void changedUpdate(DocumentEvent e) {
//        update();
//      }
//    });
//  }
//
//  public Pattern getRegexPattern() {
//    return regexPattern;
//  }
//
//  public void setRegexPattern(Pattern regexPattern) {
//    EAssert.Argument.isNotNull(regexPattern, "regexPattern");
//    this.regexPattern = regexPattern;
//  }
//
//  private void update() {
//    boolean isOk = true;
//
//    isOk = this.regexPattern.matcher(this.txt.getText()).find();
//
//    if (isOk) {
//      txt.setBackground(okColor);
//      okColor = null;
//    } else {
//      if (okColor == null) okColor = txt.getBackground();
//      txt.setBackground(FAIL_COLOR);
//    }
//  }
//}
