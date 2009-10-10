import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import spic.SPIConnector;


/**
 * SPICSample. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070723 nsano initial version <br>
 */
public class SPICSample {
	private Image bgImage;
//	private Image charaImage;
//	private Image chipImage;
//	private Image unknownImage;

	public SPICSample(String[] args) {
		JFrame frame = new JFrame("SPIConnector Sample");
		JPanel panel = new JPanel() {
		    public void paint(Graphics g) {
System.err.println("paint: " + bgImage);
		        if (bgImage != null) {
		            g.drawImage(bgImage, 0, 0, this);
		        }
		            g.setColor(Color.red);
		            g.drawOval(0, 0, 640, 480);
//		      if (charaImage != null) {
//		          g.drawImage(charaImage, insets.left + 59, insets.top, null);
//		      }
//		      if (chipImage != null) {
//		          g.drawImage(chipImage, insets.left, insets.top, null);
//		      }
//		      if (unknownImage != null) {
//		          g.drawImage(unknownImage, insets.left + 400, insets.top + 250, null);
//		      }
		    }
		};
		panel.setPreferredSize(new Dimension(640, 480));
		frame.setContentPane(panel);
		frame.pack();
		Insets insets = frame.getInsets();
		frame.setBounds(0, 0, 640 + insets.left + insets.right, 480 + insets.top + insets.bottom);
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
        SPIConnector.setSpiDir("/usr/local/lib/susie/plugins");
		bgImage = SPIConnector.getImage(args[0]);
System.err.println("image: " + bgImage);
//		charaImage = ImageFactory.createAlphaImageByMask(
//				new FileInfo("file/SG03.MRG", "CLA02_05.MCG"),
//				new FileInfo("file/SG03.MRG", "CLA02_05.ACD")
//			);
//		chipImage = ImageFactory.createAlphaImageByColor(
//				new FileInfo("file/SG02.MRG", "CHARA02.MCA"),
//				Color.GREEN 
//			);
//
//		ImageData imageData = SPIConnector.getImageData("file/SG03.MRG", "CLG02_03.MCG");
//		ImageData maskData = SPIConnector.getImageData("file/SG03.MRG", "CLG02_03.ACD");
//		ImageDataFactory.toAlphaByMask(imageData, maskData);
//		ImageDataFactory.toSepia(imageData);
//		ImageDataFactory.toMozaic(imageData, 6);
//		unknownImage = SPIConnector.createImage(imageData);
        frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new SPICSample(args);
	}
}
