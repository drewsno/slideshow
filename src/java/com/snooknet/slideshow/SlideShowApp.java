package com.snooknet.slideshow;

//==============================================================================
//Example Java Slide Show Applications
//Based on code by Jasper Potts: http://www.jasperpotts.com/blog/2005/05/javaswing-can-be-as-fast-as-native/
//==============================================================================

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;

public class SlideShowApp {

	private static List<MediaFile> images = new ArrayList<MediaFile>();
	Frame mainFrame;
	GraphicsConfiguration gc = null;

	private boolean paused = false;

	private int iImageIndex;
	private boolean firstMouseMove = true;
	private Thread mainLoopThread = null;
	private Flick flick = new Flick();

	public SlideShowApp(int numBuffers, GraphicsDevice device) {
		try {
			gc = device.getDefaultConfiguration();
			mainFrame = new Frame(gc);
			addListeners();
			mainFrame.setUndecorated(true);
			mainFrame.setIgnoreRepaint(true);
			device.setFullScreenWindow(mainFrame);

			mainFrame.createBufferStrategy(numBuffers);
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image image = toolkit.createImage(new byte[0]);
			Cursor cursor = toolkit.createCustomCursor(image, new Point(mainFrame.getX(), mainFrame.getY()), "img");
			mainFrame.setCursor(cursor);
			mainLoop();
		} catch (Exception e) {
			e.printStackTrace();
			device.setFullScreenWindow(null);
			JOptionPane.showMessageDialog(null, "Error \"" + e.getMessage() + "\"", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} finally {
			device.setFullScreenWindow(null);
		}
	}

	private void addListeners() {
		mainFrame.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("Exited due to mouse click");
				System.exit(0);
			}
		});

		mainFrame.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				System.out.println("Exiting due to focus lost");
				System.exit(0);
			}

			@Override
			public void focusGained(FocusEvent e) {

			}
		});
		mainFrame.addMouseMotionListener(new MouseInputAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// System.out.println(e.getX());
				if (!firstMouseMove) {
					System.out.println("Exiting due to mouse move");
					System.exit(0);
				}
				firstMouseMove = false;
			}
		});

		mainFrame.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int c = e.getExtendedKeyCode();
				switch (c) {
				case 32:
					paused = !paused;
					mainLoopThread.interrupt();
					break;
				case 37:
					// left arrow
					iImageIndex = Math.max(0, iImageIndex - 2);
					mainLoopThread.interrupt();
					break;
				case 39:
					// right arrow
					mainLoopThread.interrupt();
					break;
				default:
					System.out.println("Exiting due to key press");

					System.exit(0);
					break;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});
	}

	// -draw next image to translucent image
	// -repeat:
	// - draw current image to next buffer
	// - merge translucent image into next buffer using alpha based on tick value
	// - show next buffer
	// -until translucent image fully replaces current image
	// -go to next image
	private void mainLoop() throws Exception, InterruptedException {
		mainLoopThread = Thread.currentThread();

		Rectangle bounds = mainFrame.getBounds();
		BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();

		BufferedImage oTranslucentImage = gc.createCompatibleImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);

		MediaFile mediaFile = images.get(0);
		BufferedImage oImage = loadScaled(new File(mediaFile.getFilename()), bounds.width, bounds.height);

		// pre-fetch locations of first 2 images
		LocationFetcher.setLocation(images.get(0));
		LocationFetcher.setLocation(images.get(1));

		BufferedImage oNextImage;
		MediaFile nextMediaFile;
		for (iImageIndex = 0; iImageIndex < (images.size() - 1); iImageIndex++) {
			if (paused) {
				iImageIndex--;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// nothing
				}
				continue;
			}
			oNextImage = null;
			nextMediaFile = null;
			for (float tick = 0; tick <= 10; tick += 0.1) {
				if (Thread.interrupted()) {
					break;
				}
				Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
				if (!bufferStrategy.contentsLost()) {
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, bounds.width, bounds.height);
					if (oNextImage == null) {
						nextMediaFile = images.get(iImageIndex + 1);
						oNextImage = loadScaled(new File(nextMediaFile.getFilename()), bounds.width, bounds.height);
						Graphics2D g2 = oTranslucentImage.createGraphics();
						g2.setColor(new Color(0, 0, 0, 255));
						g2.fillRect(0, 0, bounds.width, bounds.height);
						g2.drawImage(oNextImage, (bounds.width - oNextImage.getWidth()) / 2, (bounds.height - oNextImage.getHeight()) / 2, null);
						drawLabels(g2, nextMediaFile);
						// temp
						// g2.drawString("image: " + (iImageIndex + 1), 1, 1000);
						// temp
						g2.dispose();

						// get location of image after next
						LocationFetcher.setLocation(images.get(iImageIndex + 2));
					}
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, bounds.width, bounds.height);
					g.drawImage(oImage, (bounds.width - oImage.getWidth()) / 2, (bounds.height - oImage.getHeight()) / 2, null);
					drawLabels(g, mediaFile);
					// temp
					// g.drawString("image: " + iImageIndex, 1, 1000);
					// temp

					// Graphics2D g3 = (Graphics2D) mainFrame.getGraphics();
					// g3.setColor(Color.WHITE);
					// // g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
					// g3.fillRect(900, 500, 15, 45);
					// g3.fillRect(925, 500, 15, 45);

					// float fAlpha = (float) Math.log10(tick);
					float fAlpha = tick / 10;
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fAlpha));
					g.drawImage(oTranslucentImage, (bounds.width - oTranslucentImage.getWidth()) / 2, (bounds.height - oTranslucentImage.getHeight()) / 2, null);
					bufferStrategy.show();
					g.dispose();
				}

				try {
					Thread.sleep((tick == 0) ? Config.getDisplaySeconds() * 1000 : 1);
				} catch (InterruptedException e) {
					System.out.println("was on tick " + tick + ", image " + iImageIndex);
					oNextImage = oImage;
					nextMediaFile = mediaFile;
					break;
				}
			}
			flick.fetchPrice();
			oImage = oNextImage;
			mediaFile = nextMediaFile;
		}
	}

	private void drawLabels(Graphics2D g, MediaFile mediaFile) {

		int yearDif = Years.yearsBetween(new DateTime(mediaFile.getCreationDate()), new DateTime().plusMonths(6)).getYears();
		String yearsAgo;
		switch (yearDif) {
		case 0:
			yearsAgo = "This year";
			break;
		case 1:
			yearsAgo = "Last Year";
			break;
		default:
			yearsAgo = yearDif + " Years Ago";
			break;
		}

		// years ago
		g.setFont(new Font("TimesRoman", Font.BOLD, 96));
		g.setColor(Color.BLACK);
		g.drawString(yearsAgo, 1504, 102);
		g.setColor(Color.RED);
		g.drawString(yearsAgo, 1500, 100);

		// camera model
		if (mediaFile.getCameraModel() != null) {
			g.setFont(new Font("TimesRoman", Font.ITALIC, 24));
			g.setColor(Color.BLACK);
			g.drawString(mediaFile.getCameraModel(), 2, 1991);
			g.setColor(Color.CYAN);
			g.drawString(mediaFile.getCameraModel(), 1, 1990);
		}

		// photo date
		String date = new LocalDateTime(mediaFile.getCreationDate()).toString(DateTimeFormat.forPattern("E d/M/YY hh:mm a"));
		g.setFont(new Font("TimesRoman", Font.PLAIN, 24));
		g.setColor(Color.BLACK);
		g.drawString(date, 2, 2041);
		g.setColor(Color.CYAN);
		g.drawString(date, 1, 2040);

		// filename
		String filename = mediaFile.getFilename().substring(Config.getMediaRootDir().length() + 1);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 28));
		g.setColor(Color.BLACK);
		g.drawString(filename, 2, 2081);
		g.setColor(Color.CYAN);
		g.drawString(filename, 1, 2080);

		// location
		if (mediaFile.getLocation() != null) {
			g.setFont(new Font("TimesRoman", Font.BOLD, 28));
			g.setColor(Color.BLACK);
			g.drawString(mediaFile.getLocation(), 2, 2131);
			g.setColor(Color.CYAN);
			g.drawString(mediaFile.getLocation(), 1, 2130);
		}

		// current time
		String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		g.setFont(new Font("TimesRoman", Font.BOLD, 96));
		g.setColor(Color.BLACK);
		g.drawString(currentTime, 3602, 102);
		g.setColor(Color.PINK);
		g.drawString(currentTime, 3600, 100);

		// flick
		if (flick.getPrice() != null) {
			String price = flick.getPrice() + "c";
			g.setFont(new Font("TimesRoman", Font.BOLD, 70));
			g.setColor(Color.BLACK);
			g.drawString(price, 3612, 202);
			g.setColor(flick.getColor());
			g.drawString(price, 3610, 200);
		}

		if (paused) {
			g.setColor(Color.WHITE);
			// g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
			g.fillRect(1800, 1000, 30, 90);
			g.fillRect(1850, 1000, 30, 90);
		}
	}

	public BufferedImage loadScaled(File i_oImageFile, int i_iWidth, int i_iHeight) throws Exception {
		BufferedImage oImage = null;
		while (oImage == null) {
			try {
				oImage = ImageIO.read(i_oImageFile);
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(10000);
			}
		}
		double dScale = calculateScaleFactor(oImage.getWidth(), oImage.getHeight(), i_iWidth, i_iHeight);
		if (dScale != 1) {
			int iWidth = (int) (oImage.getWidth() * dScale);
			int iHeight = (int) (oImage.getHeight() * dScale);
			BufferedImage oScaledImage = gc.createCompatibleImage(iWidth, iHeight);
			Graphics2D g = oScaledImage.createGraphics();
			g.drawImage(oImage, 0, 0, iWidth, iHeight, null);
			g.dispose();
			return oScaledImage;
		} else {
			return oImage;
		}
	}

	public static final double calculateScaleFactor(int i_iSrcWidth, int i_iSrcHeight, int i_iReqWidth, int i_iReqHeight) {
		double dXscale = (double) i_iReqWidth / (double) i_iSrcWidth;
		double dYscale = (double) i_iReqHeight / (double) i_iSrcHeight;
		return Math.min(dXscale, dYscale);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// scan for images
		images = (new FilteredImageContainer()).getFilteredList();
		if (images.size() == 0) {
			JOptionPane.showMessageDialog(null, "No images found", "No Images Found", JOptionPane.ERROR_MESSAGE);
		} else {
			// do slideshow
			int numBuffers = 2;
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice device = env.getDefaultScreenDevice();
			new SlideShowApp(numBuffers, device);
		}
	}

}