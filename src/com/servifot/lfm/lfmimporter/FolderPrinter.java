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
import java.util.List;

/**
 * Clase pensada para escuchar en una carpeta y cuando detecta un nuevo archivo, si es una imagen.
 * Lo imprime por la impresora indicada. Rellenando con la imagen la impresión.
 *
 * @author FRANCESC
 *
 */
public class FolderPrinter extends Thread {

	private static final String PRINTEDFILES_NAME = "printedfiles.txt";
	private File m_folderprint = null;

	/** Indica si el hilo debe detenerse */
	private boolean m_die = false;
	/** Archivo con los nombres de las imágenes impresas */
	private File m_printedNames = null;
	/** Oservador de la carpeta que recibe las imágenes a imprimir*/
	private WatchService m_watcher = null;

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
				m_watcher = myDir.getFileSystem().newWatchService();
				myDir.register(m_watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				System.out.println("Escuachando en... " + m_folderprint.getPath());
				WatchKey watckKey = m_watcher.take();

				List<WatchEvent<?>> events = watckKey.pollEvents();
				for (WatchEvent<?> event : events) {
					if (!m_die && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						System.out.println("Created: " + event.context().toString());
						File newImage = new File(m_folderprint.getAbsolutePath()+"/" + event.context().toString());
						printImage(newImage);
					}
				}

			} catch (Exception e) {
				System.out.println("Error: " + e.toString());
			}
		}

	}

	private void printImage(File file) {
		String printcmd = "rundll32 shimgvw.dll ImageView_PrintTo /pt \""+file.getAbsolutePath()+"\" \""+LFMImporter.getConfig().getPrinter()+"\"";
		System.out.println("Printing: " + printcmd);
		try {
			Runtime.getRuntime().exec(printcmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		addImagePrinted(file, m_printedNames);

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
		if(m_watcher != null)
			try {
				m_watcher.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		m_die = true;
	}

	public boolean isDie() {
		return m_die;
	}
}
