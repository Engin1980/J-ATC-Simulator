/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.extenders;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Marek
 */
public class XmlFileSelectorExtender {

  private final JTextField txt;
  private final JButton btn;
  private File file = null;

  public JTextField getTextControl(){
    return txt;
  }

  public JButton getButtonControl(){
    return btn;
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn, File file) {
    this.txt = txt;
    this.btn = btn;
    this.btn.addActionListener(e -> processDialog());
    this.file = file;
    this.txt.setEditable(false);
    this.txt.setText("< browse for file >");
    this.btn.setText("(browse)");
  }

  public XmlFileSelectorExtender(JTextField txt, JButton btn) {
    this(txt, btn, null);
  }

  public XmlFileSelectorExtender() {
    this(new JTextField(), new JButton("(browse)"));
  }

  public final void setFile(String file) {
    File f = new File(file);
    setFile(f);
  }

  public final void setFile(File file) {
    this.file = file;
    txt.setText(file.getAbsolutePath());
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
    JFileChooser jfc = new JFileChooser();

    jfc.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));

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
