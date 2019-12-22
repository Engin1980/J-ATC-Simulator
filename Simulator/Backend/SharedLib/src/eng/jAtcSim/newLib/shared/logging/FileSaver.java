//package eng.jAtcSim.newLib.shared.logging;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//
//public class FileSaver extends AbstractSaver {
//  private final String fileName;
//  private BufferedWriter bw = null;
//
//  public FileSaver(String fileName) {
//    if (fileName == null) {
//      throw new IllegalArgumentException("Value of {fileName} cannot not be null.");
//    }
//
//    this.fileName = fileName;
//  }
//
//  @Override
//  public void write(String text) throws IOException {
//    if (bw == null)
//      openWriter();
//    bw.write(text);
//    bw.flush();
//  }
//
//  @Override
//  public void close() {
//    if (bw != null)
//      try {
//        bw.close();
//      } catch (IOException ex) {
//      } finally {
//        bw = null;
//      }
//  }
//
//  private void openWriter() throws IOException {
//    try {
//      bw = new BufferedWriter(new FileWriter(this.fileName));
//    } catch (IOException ex) {
//      throw new IOException("Unable to open a file " + this.fileName + " for writing.", ex);
//    }
//  }
//}
