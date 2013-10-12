package com.googlecode.freebasegetter;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * The program will download images from freebase using Image API for a list of
 * words specified in a csv file
 * 
 * @author hezzze
 * 
 */
public class FreebaseImageGetter implements Runnable {

	/**
	 * The relative path containing the downloaded images
	 */
	static final String DOWNLOAD_DIR = "images-downloaded";

	/**
	 * The max height of the images downloaded
	 */
	static final int MAX_HEIGHT = 768;

	/**
	 * The API_KEY from a google account to make more API calls
	 */
	static final String API_KEY = "AIzaSyCBSvWFF2EzTFi_BF3ywJUCNa50AxI_kOk";

	/**
	 * A list of Entries containing the names and mids of words
	 */
	private ArrayList<Entry> data = new ArrayList<Entry>();

	/**
	 * A class for each name and mid pair for the images to be downloaded
	 * 
	 * @author hezzze
	 * 
	 */
	class Entry {
		String name;
		String mid;

		public Entry(String name, String mid) {
			this.name = name;
			this.mid = mid;
		}
	}

	/**
	 * The program will read from a csv file that has two columns storing the
	 * names and the mids of all the words
	 */
	@Override
	public void run() {
		try {
			BufferedReader rd = new BufferedReader(new FileReader(new File(
					"Word list.csv")));
			String line;

			// for the header
			line = rd.readLine();

			while ((line = rd.readLine()) != null) {
				String[] temp = line.split(",");
				Entry newEntry = new Entry(temp[0], temp[1]);
				data.add(newEntry);
			}

			rd.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ImageInputStream imgInputStream = null;
		StringBuilder bd = null;
		BufferedImage bufferedImg = null;

		new File(DOWNLOAD_DIR).mkdir();

		for (int i = 0; i < data.size(); i++) {
			Entry entry = data.get(i);
			bd = new StringBuilder(
					"https://www.googleapis.com/freebase/v1/image/m/");
			bd.append(entry.mid);
			bd.append("?maxheight=");
			bd.append(MAX_HEIGHT);
			bd.append("&key=" + API_KEY);
			String url = bd.toString();
			String format = "jpg";

			try {
				// Get an img input stream using google image API
				imgInputStream = ImageIO.createImageInputStream(new URL(url)
						.openStream());
				Iterator<ImageReader> it = ImageIO
						.getImageReaders(imgInputStream);
				format = it.next().getFormatName();

				bufferedImg = ImageIO.read(imgInputStream);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String formatStr = "";
			if (format.equals("JPEG")) {
				formatStr = "jpg";
			} else if (format.toUpperCase().equals("PNG")) {
				formatStr = "png";
			} else if (format.toUpperCase().equals("GIF")) {
				formatStr = "gif";
			}
			File outputImage = new File(DOWNLOAD_DIR + "/" + entry.mid + "."
					+ formatStr);
			String msg = entry.mid + "." + formatStr + "...done";
			try {
				ImageIO.write(bufferedImg, formatStr, outputImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(msg);
		}

	}

	public static void main(String[] args) {
		new FreebaseImageGetter().run();
	}
}
