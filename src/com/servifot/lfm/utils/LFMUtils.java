package com.servifot.lfm.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.shape.Rectangle;

public class LFMUtils {
	/**
	 * Calcula la zona de recorte de un rectángulo para respetar unas porporciones concretas.
	 * (Encaja un rectángulo con proporciones indicadas dentro del rectángulo original)
	 * 
	 * @param rect Rectángulo sobre el que calcular la zona de recorte
	 * @param proportion Rectángulo con las proporciones a respetar.
	 * @return Rectángulo que representa la zona de recorte sobre el rectángulo original
	 */
	public static Rectangle fitRectangle(Rectangle rect, Rectangle proportion) {
		double width = Math.min(rect.getWidth(), (rect.getHeight() * proportion.getWidth()/proportion.getHeight()));
		double height = Math.min(rect.getHeight(),(rect.getWidth() * proportion.getHeight()/proportion.getWidth()));
		double x = rect.getX() + (rect.getWidth() - width)/2;
		double y = rect.getY() + (rect.getHeight() - height)/2;
		return new Rectangle(x,y,width,height);
	}
	
	public static File[] sortByDate(ArrayList<File> unsortedFiles) {
		return sortByDate(unsortedFiles.toArray(new File[unsortedFiles.size()])); 
	}

	public static File[] sortByDate(File[] listFiles) {
		class Pair implements Comparable {
		    public long t;
		    public File f;

		    public Pair(File file) {
		        f = file;
		        t = file.lastModified();
		    }

		    public int compareTo(Object o) {
		        long u = ((Pair) o).t;
		        return t < u ? -1 : t == u ? 0 : 1;
		    }
		};

		// Obtain the array of (file, timestamp) pairs.
		File[] files = listFiles;
		Pair[] pairs = new Pair[files.length];
		for (int i = 0; i < files.length; i++)
		    pairs[i] = new Pair(files[i]);

		// Sort them by timestamp.
		Arrays.sort(pairs);

		// Take the sorted pairs and extract only the file part, discarding the timestamp.
		//for (int i = files.length-1; i >= 0; i--) files[i] = pairs[i].f;
		for (int i = 0; i <files.length; i++) files[i] = pairs[i].f;
		
		return files;
	}
}
