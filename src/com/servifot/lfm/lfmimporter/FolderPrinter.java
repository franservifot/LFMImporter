package com.servifot.lfm.lfmimporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.PrinterResolution;

import com.servifot.lfm.utils.FileUtils;
import com.servifot.lfm.utils.LFMUtils;

import javafx.print.Paper;
import javafx.print.PrintQuality;
import javafx.print.PrintResolution;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

/**
 * Clase pensada para escuchar en una carpeta y cuando detecta un nuevo archivo, si es una imagen.
 * Lo imprime por la impresora indicada. Rellenando con la imagen la impresión.
 * 
 * @author FRANCESC
 *
 */
public class FolderPrinter extends Thread {
	
	/** Ancho del papel en px a 300dpi */
	private static final int PAPER_W = 1748;
	/** Alto del papel en px a 300dpi*/
	private static final int PAPER_H = 1181;
	
	private static final String PRINTEDFILES_NAME = "printedfiles.txt";
	private File m_folderprint = null;	
	
	/** Indica si el hilo debe detenerse */
	private boolean m_die = false;
	/** Archivo con los nombres de las imágenes impresas */
	private File m_printedNames = null;
	
	/** Lista de archivos ya impresos */
	ArrayList<String> m_printedFiles = new ArrayList<>();
	
	public FolderPrinter(File folder) {
		m_folderprint = folder;
	}
	
	@Override
	public void run() {
		if (!m_folderprint.exists()) {
			System.out.println("La carpeta de impresión no existe... Creandola...");
			if (!m_folderprint.mkdirs()) {
				System.out.println("No se ha podido crear la carpeta de impresión... ADIOS");
				m_die = true;
				return;
			}
		}
		// Si el archivo de impresiones ya hechas existe las cargamos, si no lo creamos
		m_printedNames = new File(m_folderprint.getAbsolutePath()+"/"+PRINTEDFILES_NAME);
		if (m_printedNames.exists()) {
			loadPrintedFiles(m_printedNames);
		} else {
			try {
				if (!m_printedNames.createNewFile()) {
					System.err.println("Imposible crear un archivo para registrar las impresiones... ADIOS");
					m_die = true;
					return;
				}
			} catch (IOException e) {
				System.err.println("Imposible crear un archivo para registrar las impresiones... ADIOS");
				e.printStackTrace();
				m_die = true;
				return;
			}
		}
		
		while (!m_die) {
			try {	
				Path myDir = m_folderprint.toPath();
				WatchService watcher = myDir.getFileSystem().newWatchService();
				myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				System.out.println("Escuachando en... " + m_folderprint.getPath());
				WatchKey watckKey = watcher.take();

				List<WatchEvent<?>> events = watckKey.pollEvents();
				for (WatchEvent<?> event : events) {
					if (!m_die && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						System.out.println("Created: " + event.context().toString());
						File newImage = new File(m_folderprint.getAbsolutePath()+"/" + event.context().toString());
						print(newImage);
					}
//					Ignoramos estos eventos->					
//					if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
//						System.out.println("Delete: " + event.context().toString());
//					}
//					if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
//						System.out.println("Modify: " + event.context().toString());
//					}
				}

			} catch (Exception e) {
				System.out.println("Error: " + e.toString());
			}
		}
		
	}
	
	/**
	 * Dado un archivo lo imprime si es una imagen
	 * 
	 * @param newImage
	 */
	private void print(File newImage) {
		
		if (newImage != null && newImage.getAbsolutePath().toLowerCase().matches(".*\\.jpe?g")) {
			Printer printer = getPrinter();
			PrinterJob job = PrinterJob.createPrinterJob(printer);
			
			if (job != null && !m_die) {
				Canvas cv = new Canvas(PAPER_W, PAPER_H);
				Image img = new Image("file:///" + newImage.getAbsolutePath());
				
				Rectangle imgrectangle = new Rectangle(0, 0, img.getWidth(), img.getHeight());
				Rectangle canvasRectangle = new Rectangle(0, 0, PAPER_W, PAPER_H);
				Rectangle imgSource = LFMUtils.fitRectangle(imgrectangle, canvasRectangle);
				//Rectangle imgSource = LFMUtils.fitRectangle(canvasRectangle, imgrectangle);
				cv.getGraphicsContext2D().drawImage(img, imgSource.getX(), imgSource.getY(), imgSource.getWidth(), imgSource.getHeight(), canvasRectangle.getX(), canvasRectangle.getY(), canvasRectangle.getWidth(), canvasRectangle.getHeight());
				job.getJobSettings().setPrintQuality(PrintQuality.HIGH);
				if (job.printPage(cv)) {
					job.endJob(); 
					//Si conseguimos imprimir la imagen lo registramos
					addImagePrinted(newImage, m_printedNames);
				}
			}
		}
	}
	
	
	/**
	 * Añade una imagen impresa al registro
	 * 
	 * @param newImage Archivo de la nueva imagen
	 * @param printedNames Archivo donde se escriben las imágenes impresas
	 */
	private void addImagePrinted(File newImage, File printedNames) {
		m_printedFiles.add(newImage.getName());
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(printedNames, true), Charset.forName("UTF-8"));
			out.write(newImage.getName() + System.lineSeparator());
			if (out!= null) out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Devuelve la impresora configurada
	 * 
	 * @return impresora configurada o null si no la encuentra
	 */
	private Printer getPrinter() {
		Iterator<Printer> iter = Printer.getAllPrinters().iterator();
		while(iter.hasNext()) {
			Printer p = iter.next(); 
			if (p.getName().equals(LFMImporter.getConfig().getPrinter())) {
				return p;
			}
		}
		
		return null;
	}

	/**
	 * Busca los archivos ya impresos. Si no hay ninguno deja la lista vacía.
	 * 
	 */
	private void loadPrintedFiles(File listOfPrinteds) {
		if (listOfPrinteds.exists() && listOfPrinteds.isFile()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(listOfPrinteds), Charset.forName("UTF-8")));
				String line = null;
				while ((line = br.readLine()) != null) {
					m_printedFiles.add(line);
				}
				br.close();
				
			} catch (Exception e) {
				System.err.println("ERROR: No se ha podido cargar la lista de archivos impresos");
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void kill() {
		m_die = true;
	}
	
	public boolean isDie() {
		return m_die;
	}
}
