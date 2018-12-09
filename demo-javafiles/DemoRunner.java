//
//  DemoRunner.java
//  Demo
//
//  Created by Oras Phongpanangam on Fri Aug 10 2018.
//

import java.io.FileReader;
import java.io.File;

/**
 * This class extends demo.DemoApplet and provide a main function to run Demo with a
 * specified DATA tag
 */
public class DemoRunner extends demo.DemoApplet {
  public void main(String[] args) {
    // start a new demo
    System.out.println("Starting Demo application...");
    if (args.length == 1) {
      // if there's one argument it's either a file to open or the data tag
        try {
            FileReader r = new FileReader(new File(args[0]));
            String str = "";
            int c;
            while ((c = r.read()) != -1)
                str += (char) c;
            r.close();
            new demo.Demo(this, str);
        }
        catch (java.io.FileNotFoundException ex) {
            System.out.println("File not found: " + args[0]);
            System.exit(1);
        }
        catch (java.io.IOException ex) {
            try {
              new demo.Demo(this, args[0]);
            }
            catch (java.io.Exception ex) {
              new demo.Demo(this, null);
            }
        }
    }
    else new demo.Demo(this, null);
  }
}
