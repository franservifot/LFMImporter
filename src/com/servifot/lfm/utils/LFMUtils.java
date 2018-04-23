package com.servifot.lfm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.ObservableList;
import javafx.scene.shape.Rectangle;

public class LFMUtils {
	
	
	private static String[] s_wifidevices = null;
	
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
	
	
	/**
	 * En el comando netsh wlan show drivers saca los Nombre de interfaz disponibles
	 * 
	 * @param searchInterfazList lista a llenar con los drivers disponibles
	 */
	public static void fillWifiInterfaces(ObservableList<String> searchInterfazList) {
		// Si no se han buscado todavía las busca
		if (s_wifidevices == null) {
			Runtime rt = Runtime.getRuntime();
			String s = "";
			Process pr;
			try {
				pr = rt.exec("netsh wlan show drivers");
				
				BufferedReader br1 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				System.out.println("Buscando Dispositivos wifi");
				while ((s = br1.readLine()) != null) {
					if (s.contains("Nombre de interfaz")) {
						try {searchInterfazList.add(s.split(":")[1].trim());} catch (Exception e2) {System.err.println(e2.getMessage());}
					}
					s = "";
				}
				pr.waitFor();
				br1.close();
				
				if (searchInterfazList.size() > 0) {
					s_wifidevices = new String[searchInterfazList.size()];
					for (int i = 0; i < searchInterfazList.size(); i++) {
						s_wifidevices[i] = searchInterfazList.get(i);
					}
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		//Si ya las habían buscado als añade
		} else {
			for (String s : s_wifidevices) {
				searchInterfazList.add(s);
			}
		}
		
	}
}
