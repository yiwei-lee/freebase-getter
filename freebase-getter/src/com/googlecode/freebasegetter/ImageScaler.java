package com.googlecode.freebasegetter;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * The program will load images from a directory and scale all the images to the
 * same height as the least high image, during the process the aspect ratio of
 * each image is preserved
 * 
 * @author hezzze
 * 
 */
public class ImageScaler {

	/**
	 * The path of the images to be processed
	 */
	static final Path SOURCE_DIR = Paths.get("images-downloaded");

	/**
	 * The path of the scaled images
	 */
	static final String DEST_DIR = "images-scaled";

	/**
	 * The list for the original images
	 */
	static ArrayList<ImageFile> oldImages = new ArrayList<ImageFile>();

	/**
	 * The program first load all the images into memory, process them and then
	 * output the data to files
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		LoadImage ldimg = new LoadImage();
		try {
			Files.walkFileTree(SOURCE_DIR, ldimg);

			int minHeight = Integer.MAX_VALUE;
			for (int i = 0; i < oldImages.size(); i++) {
				ImageFile imgFile = oldImages.get(i);
				if (imgFile.getHeight() < minHeight) {
					minHeight = imgFile.getHeight();

				}

			}

			System.out.println("Min height: " + minHeight);

			// Output to file the images
			
			new File(DEST_DIR).mkdir();
			
			for (ImageFile imgFile : oldImages) {
				BufferedImage img = imgFile.img;
				Image newImg = img.getScaledInstance(-1, minHeight,
						Image.SCALE_SMOOTH);
				BufferedImage newBuffImg = getBufferedImage(newImg);
				File outputImage = new File(DEST_DIR + "/" + imgFile.fileName
						+ ".png");
				ImageIO.write(newBuffImg, "png", outputImage);

				System.out.println(imgFile.fileName + "...done");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * http://helpdesk.objects.com.au/java/how-to-convert-an-image-to-a-
	 * bufferedimage
	 * 
	 * @param image
	 * @return
	 * @throws InterruptedException
	 */
	private static BufferedImage getBufferedImage(Image image)
			throws InterruptedException {

		// Determine transparency for BufferedImage
		// http://helpdesk.objects.com.au/java/how-to-determine-if-image-supports-alpha
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		pg.grabPixels();
		boolean hasAlpha = pg.getColorModel().hasAlpha();
		int transparency = hasAlpha ? Transparency.BITMASK
				: Transparency.OPAQUE;

		// Create the buffered image

		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gs.getDefaultConfiguration();
		BufferedImage bufferedImage = gc.createCompatibleImage(
				image.getWidth(null), image.getHeight(null), transparency);

		if (bufferedImage == null) {

			// if that failed then use the default color model

			int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB
					: BufferedImage.TYPE_INT_RGB;
			bufferedImage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}

		// Copy image

		Graphics g = bufferedImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bufferedImage;

	}

	/**
	 * A file visitor for processing all the files in a folder
	 * 
	 * @author hezzze
	 * 
	 */
	static class LoadImage extends SimpleFileVisitor<Path> {

		ImageInputStream iis;

		/**
		 * For each file visited, using ImageIO to read the file to a
		 * ImageInputStream for determining the format and then store into a
		 * ImageFile object for processing
		 */
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr)
				throws IOException {
			if (attr.isSymbolicLink()) {
				// System.out.format("Symbolic link: %s", file);
			} else if (attr.isRegularFile()) {
				// System.out.format("Regular file: %s\n", file.getFileName());

				ImageInputStream iis = ImageIO.createImageInputStream(file
						.toFile());
				Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
				String format = it.next().getFormatName();
				BufferedImage oldImage = ImageIO.read(iis);
				oldImages.add(new ImageFile(oldImage, file.getFileName()
						.toString().split("\\.")[0], format));

			} else {
				// System.out.format("Other: %s", file);
			}
			// System.out.println(" (" + attr.size() + "bytes)");
			return CONTINUE;
		}

		// Print each directory visited.
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			System.out.format("Directory: %s%n", dir);
			return TERMINATE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return CONTINUE;
		}

	}

	/**
	 * A helper class for storing the information of the image files
	 * 
	 * @author hezzze
	 * 
	 */
	static class ImageFile {
		BufferedImage img;
		String fileName;
		String format;

		ImageFile(BufferedImage img, String fileName, String format) {
			this.img = img;
			this.fileName = fileName;
			this.format = format;
		}

		int getHeight() {
			return img.getHeight();
		}

		int getWidth() {
			return img.getWidth();
		}

		String getFileName() {
			return fileName;
		}

		String getFormatString() {
			String formatStr = "";
			if (format.equals("JPEG")) {
				formatStr = "jpg";
			} else if (format.toUpperCase().equals("PNG")) {
				formatStr = "png";
			} else if (format.toUpperCase().equals("GIF")) {
				formatStr = "gif";
			}
			return formatStr;
		}

	}

}
