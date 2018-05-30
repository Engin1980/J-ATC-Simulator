/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.extenders;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Marek
 */
public class XmlFileSelectorExtender {

  private final SwingFactory.FileDialogType fileType;
  private final JTextField txt;
  private final JButton btn;
  private File file;

  public JTextField getTextControl(){
    return txt;
  }

  public JButton getButtonControl(){
    return btn;
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn, File file, SwingFactory.FileDialogType type) {
    this.fileType = type;
    this.txt = txt;
    this.btn = btn;
    this.btn.addActionListener(e -> processDialog());
    this.txt.setEditable(false);
    setFile(file);
    this.btn.setText("(browse)");

    this.txt.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        checkFileExists();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        checkFileExists();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        checkFileExists();
      }
    });
  }

  private static final Color FAIL_COLOR = new Color(255, 150, 150);
  private Color okColor = null;
  private void checkFileExists (){
    File f = this.getFile();
    if (f == null || !f.exists()){
      if (okColor == null) okColor = txt.getBackground();
      txt.setBackground(FAIL_COLOR);
    } else {
      txt.setBackground(okColor);
      okColor = null;
    }
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn, SwingFactory.FileDialogType type) {
    this(txt, btn, null, type);
  }

  public XmlFileSelectorExtender(SwingFactory.FileDialogType type) {
    this(new JTextField(), new JButton("(browse)"), type);
  }

  public final void setFileName(String fileName) {
    if (fileName == null)
      setFile(null);
    else
      setFile(new File(fileName));
  }

  public final void setFile(File file) {
    this.file = file;
    if (this.file == null)
      this.txt.setText("< browse for file >");
    else
      this.txt.setText(this.file.getPath());
  }

  public final String getFileName(){
    if (file == null)
      return null;
    else
      return file.toString();
  }

  public final File getFile() {
    return file;
  }

  private void processDialog() {
    JFileChooser jfc = SwingFactory.createFileDialog(fileType, this.getFileName());

    int res = jfc.showOpenDialog(txt);

    if (res != JFileChooser.APPROVE_OPTION) {
      return; // cancel etc.
    }
    setFile(jfc.getSelectedFile());
  }

  public boolean isValid() {
    return file != null && file.exists();
  }

}
