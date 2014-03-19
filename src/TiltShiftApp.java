/*
 * Tilt Shift Generator
 * user:vmavram
 * pid:a09533664
 */ 


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

public class TiltShiftApp extends Component {
	
	private String inputFilename;
          
    BufferedImage img;

    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    public TiltShiftApp(String filename, double angle, double width) {
    	
    	inputFilename = filename;
       
       try {
           img = ImageIO.read(new File(inputFilename));
       } catch (IOException e) {
       }
             
       //create 3x3 gaussian kernel
		float[] matrix = {
			    1/16f, 1/8f, 1/16f, 
			    1/8f, 1/4f, 1/8f, 
			    1/16f, 1/8f, 1/16f, 
			};
		
		//apply gaussian blur to entire image
		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);	
		BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, matrix) );
		BufferedImage blurredImage = op.filter(img, dest);
	
		
		
		//angle in degrees of effect area
		double lineangleDegrees = angle;
		
		//width of focused area
		double widthlinePixels = width;
		
		//generate slope from angles input
		double slope = Math.tan(Math.toRadians(lineangleDegrees));
		
		
		
		//create line with the input angle that focused area will follow
		int centerx = dest.getWidth()/2;
		int centery = dest.getHeight()/2;
				
		Point2D start = new Point2D.Double();
		int starty = centerx;
		int startx = centery;
		start.setLocation(startx,starty);
		
		Point2D end = new Point2D.Double();
		int endy = (int) (centery*slope);
		int endx = (int) (centerx/slope);
		end.setLocation(endx,endy);
		
		Line2D l = new Line2D.Double();
		l.setLine(start, end);
		
		
		//iterate through blurred image
		//set area of blurred image near the line to be transparent 
		for(int x = 0; x<dest.getWidth(null); x++)
		{
			for(int y=0; y<dest.getHeight(); y++)
			{
				Point2D curr = new Point2D.Double(x,y);
				double dist = l.ptLineDist(curr);
				if(dist<widthlinePixels)			
				{
					int r = 0;
					int g = 0;
					int b = 0;
					int a = 0;
					int rgb = (a << 24) | (r << 16) | (g << 8) | b;
					dest.setRGB(x, y, rgb);
				}
			}
		}
		
		
		//now i have two images: the original and the blurred one with the selected area transparent

		//i can overlay the blurred with transparency image over the original image which gives the effect of the surround areas being blurred and the effect area in focus
		BufferedImage overlayed = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = overlayed.getGraphics();
		g.drawImage(img, 0, 0, null);
		g.drawImage(dest, 0, 0, null);
		
		//save the tilt shifted file
		try{
			ImageIO.write(overlayed, "PNG", new File(inputFilename+"_tiltshifted"+".png"));
		} catch(IOException e)
		{
			System.err.println("Error writing file\n" + e);
		}
		img = overlayed;	
    }
    

    public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(100,100);
        } 
           return new Dimension(img.getWidth(null), img.getHeight(null));   
    }

    public static void main(String[] args) {

    	JFrame fileFrame = new JFrame("Filename");
        String filename = JOptionPane.showInputDialog(fileFrame, "What file do you want to apply the tilt shift effect to? Enter the filename with extension.");
        
    	JFrame angleFrame = new JFrame("Tilt Shift Band Angle");
        double angle = Double.valueOf(JOptionPane.showInputDialog(angleFrame, "What angle do you want the tilt shift band to have? Enter a number (ie. degrees rotation)"));
        
    	JFrame widthFrame = new JFrame("Tilt Shift Band Width");
        double width = Double.valueOf(JOptionPane.showInputDialog(widthFrame, "How wide do you want the tilt shift band to be? Enter a number (ie. how many pixels wide)"));
    	
        JFrame f = new JFrame("Tilt Shifted Image");
            
        f.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        f.add(new TiltShiftApp(filename,angle,width));
        f.pack();
        f.setVisible(true);
    }
}
