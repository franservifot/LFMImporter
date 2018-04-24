package com.servifot.lfm.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class FileUtils {

	/* Constantes de tipo de fichero */
	/** Tipo Fichero de Imagen JPG */
	public static final int FILETYPE_JPG = 1;
	/** Tipo Fichero de Imagen PNG */
	public static final int FILETYPE_PNG = 2;
	/** Tipo Fichero de Imagen BMP */
	public static final int FILETYPE_BMP = 3;
	/** Tipo Fichero de Imagen XML */
	public static final int FILETYPE_XML = 4;
	/** Tipo Fichero de Photoshop */
	public static final int FILETYPE_PSD = 5;
	/** Tipo Fichero de Texto LAC */
	public static final int FILETYPE_LAC = 6;
	/** Tipo Fichero de Documento PDF */
	public static final int FILETYPE_PDF = 7;
	/** Tipo Fichero de Archivo IPF */
	public static final int FILETYPE_IPF = 8;
	/** Tipo Fichero Desconocido */
	public static final int FILETYPE_UNK = 10;

	/**
	 * Filtro para listar ficheros "ini"
	 */
	public static FileFilter ini_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".ini");
		}
	};

	/**
	 * Filtro para ficheros JPG
	 */
	public static FileFilter jpg_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.isFile() && f.getName().toLowerCase().matches(".*\\.jpe?g");
		}
	};

	/**
	 * Filtro para ficheros IPF
	 */
	public static FileFilter ipf_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.isFile() && f.getName().toLowerCase().endsWith(".ipf");
		}
	};

	/**
	 * Filtro para ficheros PDF
	 */
	public static FileFilter pdf_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.isFile() && f.getName().toLowerCase().endsWith(".pdf");
		}
	};

	/**
	 * Filtro para ficheros LAC
	 */
	public static FileFilter lac_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".lac");
		}
	};
	
	/**
	 * Filtro para ficheros Template
	 */
	public static FileFilter template_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(".template");
		}
	};

	/**
	 * Filtro para directorios
	 */
	public static FileFilter dir_filter = new FileFilter() {
		public boolean accept(File f) {
			return f.isDirectory();
		}
	};

	/**
	 * Devuelve una fecha en formato dia/mes/año horas:minutos:segundos
	 * 
	 * @param datetime
	 *            - Un objeto de tipo <code>java.util.Date</code> con la fecha
	 *            que queremos formatear
	 * @return Una cadena con la fecha y hora en un formato legible
	 */
	public static String formatDate(Date datetime) {
		// Obtenemos la fecha en formato String
		DateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return formater.format(datetime);
	}

	/**
	 * Devuelve el nombre del equipo en el que se ejecuta la aplicación
	 * 
	 * @return Cadena con el nombre del equipo.
	 */
	public static String getComputerName() {
		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			hostname = "Desconocido";
		}

		return hostname;
	}

	/**
	 * Obtiene la ruta relativa de un fichero respecto a una carpeta que lo contiene
	 * 
	 * @param folder - Carpeta que contiene al fichero
	 * @param file - Fichero a buscar
	 * 
	 * @return Ruta relativa del fichero respecto a la carpeta (incluyendo el nombre del fichero), o <code>null</code> si la carpeta no contiene al fichero
	 */
	public static String getRelativePath(File folder, File file) {
		if (folder == null || !folder.exists()) {
			return null;
		}
		
		if (file == null || !file.exists()) {
			return null;
		}
		
		String folderpath = folder.getAbsolutePath();
		String filepath = file.getAbsolutePath();
		if (!filepath.contains(folderpath)) {
			return null;
		}
			
		return filepath.substring(folderpath.length()+1);
	}
	
	/**
	 * Abre una carpeta en una ventana del explorador
	 * 
	 * @param folder
	 *            - Carpeta a abrir
	 * @return <code>true</code> si se ha podido abrir la carpeta,
	 *         <code>false</code> en caso contrario
	 * @throws IOException
	 *             - If the Runtime can't open folder
	 */
	public static boolean openFolder(File folder) throws IOException {
		// Comprobamos que la carpeta exista
		if (folder == null || !folder.exists()) {
			System.err.println("Folder to open NOT EXISTS");
			return false;
		}
		// Ejecutamos explorador
		Runtime.getRuntime().exec("explorer \"" + folder.getAbsolutePath() + "\"");
		return true;
	}

	/**
	 * Crea una carpeta y, si fuera necesario, toda la ruta hasta llegar a ella.
	 * Si ya existe la carpeta, no hace nada.
	 * 
	 * @param folder
	 *            - Carpeta a crear
	 * @return <code>true</code> si se crea correctamente, <code>false</code> en
	 *         caso contrario
	 */
	public static boolean createFolder(File folder) {
		try {
			if (!folder.exists()) {
				return folder.mkdirs();
			}
			return true;

		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}

	}

	/**
	 * Crea un fichero y, si fuera necesario, toda la ruta hasta llegar a él
	 * 
	 * @param file
	 *            - Fichero a crear
	 */
	public static boolean createFile(File file) {
		try {
			File parent = file.getParentFile();
			if (!parent.exists()) {
				if (!createFolder(parent))
					return false;
			}
			file.createNewFile();
			return true;

		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Cierra un stream o fichero
	 * @param f - Elemento a cerrar
	 */
	public static void close(Closeable f) {
		try {
			f.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Borrado recursivo de una carpeta
	 * 
	 * @param folder
	 *            - carpeta a vaciar
	 * @param del_dir
	 *            - si es true, elimina también la carpeta a vaciar
	 * @return <code>true</code> si todo ha ido bien, <code>false</code> en caso
	 *         contrario
	 */
	public static boolean deleteFolder(File folder, boolean del_dir) {

		if (folder == null)
			return true;

		boolean deleted = true;
		// obtenemos los archivos que queremos borrar
		File[] archivos = folder.listFiles();
		File current_file = null;
		// Llamamos al Garbage Collector para que cierre los lectores sobre el
		// fichero
		System.gc();

		try {
			if (archivos != null && archivos.length != 0) {
				// recorremos el directorio
				for (int i = 0; i < archivos.length; i++) {
					current_file = archivos[i];
					if (current_file.isDirectory()) {
						// Si encontramos una carpeta, hacemos recursión
						deleted = deleteFolder(current_file, true);
					} else {
						// Si es un fichero, lo eliminamos
						if (current_file.delete()) {
							//System.out.println("\t" + current_file.getName() + " borrado.");
						} else {
							if (!deleteFile(current_file))
								System.out.println("\t" + current_file.getPath() + " NO SE HA PODIDO BORRAR.");
						}
					}
				}
			}

			// resultado del borrado
			if (del_dir) {
				if (deleted && folder.delete()) {
					deleted = true;
				} else {
					deleted = false;
				}
			}
			return deleted;

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error borrando carpeta: " + folder.getAbsolutePath());
			return false;
		}

	}

	/**
	 * Para eliminar un fichero de forma definitiva
	 * 
	 * @param file
	 *            Fichero a eliminar
	 * @return <code>true</code> si hemos eliminado el fichero,
	 *         <code>false</code> en caso contrario
	 */
	public static boolean deleteFile(File file) {
		try {
			if (!file.exists())
				return true;
			if (file.delete())
				return true;
			// Llamamos al Garbage Collector para que cierre los lectores sobre
			// el fichero
			System.gc();
			// Esperamos un poquito a que el Garbage Collector haga su trabajo
			Thread.sleep(500);
			// Intentamos borrar el fichero de nuevo
			if (!file.delete()) {
				return false;
			}

			System.out.println("\t" + file.getName() + " borrado a la fuerza.");
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * Copia el fichero origen en el fichero destino
	 * 
	 * @param orig
	 *            Fichero de origen (debe existir)
	 * @param dest
	 *            Fichero destino (si no existe, lo crea)
	 * @param overwrite
	 *            Para indicar si hay que sobreescribir el fichero destino en
	 *            caso de que exista
	 * 
	 * @return <code>true</code> si todo ha ido bien, <code>false</code> en caso
	 *         contrario
	 */
	public static boolean copyFile(File orig, File dest, boolean overwrite) {

		// Comprobamos si hay que sobreescribir
		//System.out.println(">>>>COPY [" + orig.getAbsolutePath() + "]\n >>>>TO [" + dest.getAbsolutePath() + "] ...");
		if (dest.exists()) {
			if (overwrite) {
				// FileUtils.deleteFile(dest);
			} else {
				return true;
			}
		}

		// Inicializamos variables
		boolean result = false;

		// Si el fichero de origen no existe, devolvemos false
		if (!orig.exists()) {
			return result;
		}
		
		// Si el fichero de destino no existe, se crea
		if (!dest.exists()) {
			try {
				dest.getParentFile().mkdirs();
				dest.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error al crear fichero destino: "+dest.getAbsolutePath());
				result = false;
			}
		}
		
		try (
			FileInputStream fileInputStream = new FileInputStream(orig);
			FileOutputStream fileOutputStream = new FileOutputStream(dest);
			FileChannel	source = fileInputStream.getChannel();
			FileChannel destination = fileOutputStream.getChannel();
		) {
			// Nº mágico en Windows (64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = source.size();
			long position = 0;
			while (position < size) {
				position += source.transferTo(position, maxCount, destination);
			}

			result = true;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			System.err.println("Error al copiar " + orig.getAbsolutePath() + " en " + dest.getAbsolutePath());
			result = false;
		}

		return result;
	}

	/**
	 * Copia todo el contenido de la carpeta <code>orig_folder</code> a la
	 * carpeta <code>dest_folder</code>
	 * 
	 * @param orig_folder
	 *            - Ruta origen
	 * @param dest_folder
	 *            - Ruta destino
	 */
	public static boolean copyContent(File orig_folder, File dest_folder) {
		// Comprobamos que exista la ruta de origen
		if (!orig_folder.exists()) {
			return false;
		}
		// Creamos la ruta destino
		dest_folder.mkdirs();
		if (!dest_folder.exists()) {
			return false;
		}

		// Obtenemos el contenido de la ruta de origen
		File content[] = orig_folder.listFiles();
		for (int i = 0; i < content.length; i++) {

			File ini = content[i];
			File fin = new File(dest_folder.getAbsolutePath() + File.separator + ini.getName());
			if (ini.isDirectory()) {
				// Si es un directorio, lo creamos y movemos el contenido
				fin.mkdirs();
				copyContent(ini, fin);
			} else {
				// Si es un fichero, lo copiamos tal cual
				FileUtils.copyFile(ini, fin, true);
			}
		}

		return true;
	}
	
	/**
	 * Busca ficheros en una carpeta de forma recursiva y los añade a una lista
	 * 
	 * @param folder - Carpeta en la que buscar
	 * @param regex - Expresión regular que debe cumplir el nombre del fichero
	 * @param files - Lista en la que añadir los ficheros encontrados
	 */
	public static void searchFiles(File folder, String regex, ArrayList<File> files) {
		// Comprobamos que exista la ruta de origen
		if (folder == null || !folder.exists()) {
			System.out.println("La carpeta origen no existe");
			return;
		}
		// Comprobamos las variables
		if (regex == null || files == null) {
			return;
		}
		
		// Recorremos todos los ficheros de la carpeta
		File content[] = folder.listFiles();
		for (int i = 0; i < content.length; i++) {
			File file = content[i];
			if (file.isDirectory()) {
				// Si es un directorio, búsqueda recursiva
				searchFiles(file, regex, files);
			} else if (file.getName().matches(regex)) {
				// Si es un fichero, lo añadimos al vector
				files.add(file);
			}
		}
	}
	
	/**
	 * Ordena alfabéticamente una colecciones de ficheros según su nombre.<br>
	 * Si el nombre del fichero acaba en número, ordena los ficheros por su numeración
	 * 
	 * @param files - Colección de ficheros a ordenar
	 */
	public static void sortFiles(ArrayList<File> files) {
		// Ordenamos lista de ficheros 
		files.sort(new Comparator<File>() {
			public int compare(File f1, File f2) {
				// Obtenemos nombre de las imágenes a comparar (en minúsculas, sin extensión)
				File file1 = new File(f1.getPath());
				String name1 = FileUtils.removeExtension(file1.getName()).toLowerCase();
				File file2 = new File(f2.getPath());
				String name2 = FileUtils.removeExtension(file2.getName()).toLowerCase();
				
				String regex = "[^\\d]*([\\d]+)";
				if (name1.matches(regex) && name2.matches(regex)) {
					String str1 = name1.replaceAll(regex, "$1");
					String str2 = name2.replaceAll(regex, "$1");
					try {
						// Comparamos números
						Integer n1 = Integer.parseInt(str1);
						Integer n2 = Integer.parseInt(str2);
						return n1.compareTo(n2);
					} catch(NumberFormatException ex) { ex.printStackTrace(); }
				}
				
				// Comparamos nombres
				return name1.compareTo(name2);
			}
		});
	}

	/**
	 * Obtiene el nombre de un fichero sin la extensión
	 * 
	 * @param filename
	 *            String con el nombre del fichero a analizar
	 * @return El nombre del fichero sin extensión (y sin punto)
	 */
	public static String removeExtension(String filename) {
		int pos = filename.lastIndexOf(".");
		if (pos == -1) {
			return filename;
		}
		return filename.substring(0, pos);
	}

	/**
	 * Devuelve la extensión de un fichero
	 * 
	 * @param filename
	 *            String con el nombre del fichero a analizar
	 * @return Extensión del fichero (sin el 'punto'), o una cadena vacía si no
	 *         se ha podido obtener la extensión.
	 */
	public static String getExtension(String filename) {
		int pos = filename.lastIndexOf(".");
		if ((pos == -1) || (pos == (filename.length() - 1))) {
			return "";
		}
		return filename.substring(pos + 1, filename.length());
	}

	/**
	 * Ejecuta un programa externo llamando al sistema operativo.
	 * 
	 * @param command
	 *            Cadena con el comando a ejecutar
	 * @return <code>true</code> si la ejecución es correcta, <code>false</code>
	 *         si se produce algún error
	 */
	public static boolean executeCommand(String command) {
		try {
			System.out.println("Ejecutar: " + command);
			Process process = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String linea = null;
			while ((linea = br.readLine()) != null) {
				System.out.println(linea);
			}
			br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			if (br.ready()) {
				while ((linea = br.readLine()) != null) {
					System.err.println(linea);
				}
			}
			return (process.waitFor() == 0);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	/**
	 * Ejecuta un programa externo llamando al sistema operativo.
	 * 
	 * @param cmd
	 *            Array con el comando y los parámetros a ejecutar
	 * @return <code>true</code> si la ejecución es correcta, <code>false</code>
	 *         si se produce algún error
	 */
	public static boolean executeCommand(String[] cmd) {
		try {

			// Mostramos comando completo
			String cmdLine = "";
			for (String item : cmd) {
				if (cmdLine.length() != 0) {
					cmdLine += " ";
				}
				cmdLine += item;
			}
			System.out.println("Ejecutar: " + cmdLine);

			// Ejecutamos proceso y mostramos por consola la salida
			Process process = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String linea = null;
			while ((linea = br.readLine()) != null) {
				System.out.println(linea);
			}
			br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			if (br.ready()) {
				while ((linea = br.readLine()) != null) {
					System.err.println(linea);
				}
			}
			return (process.waitFor() == 0);

		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Check if a file is readable
	 * 
	 * @param file - File to test
	 * 
	 * @return <code>true</code> is the file exists and is read successfully, <code>false</code> if not
	 */
	public static boolean checkFileCanRead(File file) {
	    if (!file.exists())  {
	        return false;
	    }
	    if (!file.canRead()) {
	        return false;
	    }
	    
    	// Test text file
    	try {
	        FileReader fileReader = new FileReader(file.getAbsolutePath());
	        fileReader.read();
	        fileReader.close();
	    } catch (Exception e) {
	        return false;
	    }
   
	    // Everything is ok!
	    return true;
	}

	/**
	 * Carga el contenido de un fichero en una cadena de texto.
	 * 
	 * @param filename
	 *            Ruta al nombre del fichero.
	 * @param charsetName
	 *            Nombre de la codificación de caracteres con la que interpretar
	 *            el texto
	 * @return Una cadena con el contenido del fichero, o una cadena vacía si se
	 *         produce algún error.
	 */
	public static String readFile(String filename, String charsetName) {
		String contents = "";
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), charsetName));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + ls);
			}
			contents = stringBuilder.toString();

			reader.close();
		} catch (Exception e) {
			// Ignore
		}

		return contents;
	}
	
	/**
	 * Escribe una cadena de texto en un fichero
	 * 
	 * @param content
	 *            Contenido a escribir en el fichero
	 * @param filename
	 *            Ruta al nombre del fichero.
	 * @param charsetName
	 *            Nombre de la codificación de caracteres con la que interpretar
	 *            el texto
	 *            
	 * @return <code>true</code> si se ha podido guardar correctamente, <code>false</code> en caso contrario
	 */
	public static boolean saveToFile(String content, String filename, String charsetName) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(filename), charsetName) );
//		    writer = new BufferedWriter(new FileWriter(filename));
		    writer.write(content);

		} catch ( IOException e) {
			e.printStackTrace();
			return false;
		} finally {
		    try {
		        if (writer != null) writer.close();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		}
		return true;
	}
	

	/**
	 * Crea una carpeta temporal
	 * 
	 * @return La carpeta temporal creada, o <code>null</code> si no se puede
	 *         crear
	 */
	public static File createTempFolder() {
		File tempFolder = null;
		try {
			// Creamos fichero temporal para localizar carpeta temporal y
			// generar nombre único
			File tempFile = File.createTempFile("tempFolder", null);
			String tempFolderName = tempFile.getAbsolutePath();
			tempFile.delete();

			// Generamos carpeta a partir del nombre del fichero temporal
			tempFolder = new File(tempFolderName.substring(0,tempFolderName.length()-4));
			if (!tempFolder.mkdirs()) {
				return null;
			}

		} catch (Exception e) {
			return null;
		}

		return tempFolder;
	}

	/**
	 * Duplica todas las barras ('/','\') transformándolas en una barra doble
	 * ('\\'). Para filtrar nombres de archivo en llamadas a la utilidad
	 * exiftool.
	 * 
	 * @param str
	 *            Cadena a filtrar
	 * @return Cadena filtrada con barras dobles.
	 */
	public static String doubleSlashes(String str) {
		return str.replace("/", "\\").replace("\\", "\\\\");
	}

	/**
	 * Normaliza el nombre de un fichero sustituyendo los espacios por barras
	 * bajas y los corchetes por paréntesis
	 * 
	 * @param filename
	 *            Cadena a filtrar
	 * 
	 * @return Nombre del fichero normalizado
	 */
	public static String normalizeFilename(String filename) {
		return filename.replaceAll("\\s+", "_").replaceAll("\\[+", "(").replaceAll("\\]+", ")");
	}

	/**
	 * Replace illegal characters in a filename with "_" illegal characters : :
	 * \ / * ? | < > '
	 * 
	 * @param name
	 * @return A correct filename
	 */
	public static String sanitizeFilename(String name) {
		return name.replaceAll("[:\\\\/*?|<>']", "_").replaceAll("\\[+", "_").replaceAll("\\]+", "_");
	}

	/**
	 * Compara dos números de versión
	 * 
	 * @return El valor 0 si las versiones coiniciden,<br>
	 *         un valor mayor que cero si la versión actual es superior a la
	 *         versión web<br>
	 *         o un valor menor que cero si la versión actual es inferior a la
	 *         web
	 */
	public static int compareVersions(String current_ver, String web_ver) {
		System.out.println("Versión actual: " + current_ver + " - Última versión: " + web_ver);
		try {
			if (current_ver == null || current_ver.trim().length() == 0) {
				return Integer.MAX_VALUE;
			}

			String orig[] = current_ver.split("\\.");
			String web[] = web_ver.split("\\.");
			int numbers = Math.max(orig.length, web.length);
			int n_current, n_web;
			for (int i = 0; i < numbers; i++) {
				try {
					n_current = Integer.parseInt(orig[i]);
				} catch (Exception ex) {
					n_current = 0;
				}
				try {
					n_web = Integer.parseInt(web[i]);
				} catch (Exception ex) {
					n_web = 0;
				}
				if (n_current != n_web)
					return n_current - n_web;
				// else continue;
			}

			return 0;

		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}
	
	
	/**
	 * Comprueba el tipo de un fichero analizando los primeros bits del mismo.
	 * 
	 * @param f
	 *            - Fichero a analizar
	 * @return código númerico con el tipo del fichero analizado.
	 */
	public static int getFileType(File f) {
		byte[] buffer = new byte[4];
		FileInputStream fis = null;
		try {
			if (f.isFile()) {
				fis = new FileInputStream(f);
				fis.read(buffer);
				String filetype = FileUtils.byteArrayToHexString(buffer);
				System.out.println(filetype + " -> " + f.getName());
				if (filetype.startsWith("FFD8")) {
					return FILETYPE_JPG;
				} else if (filetype.startsWith("424D")) {
					return FILETYPE_BMP;
				} else if (filetype.startsWith("3C50")) {
					return FILETYPE_XML;
				} else if (filetype.startsWith("8950")) {
					return FILETYPE_PNG;
				} else if (filetype.startsWith("38425053")) {
					return FILETYPE_PSD;
				} else if (filetype.startsWith("5375626A")) {
					return FILETYPE_LAC;
				}

			}
		} catch (FileNotFoundException e) {
			System.err.println("ERROR >> Can't find " + f.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("ERROR >> Can't read " + f.getAbsolutePath());
		} finally {
			try {
				fis.close();
			} catch (Exception ex) {
				/* Ignore */}
		}
		return FILETYPE_UNK;
	}
	

	/**
	 * Muestra por consola qué tipo de archivo es cada uno de los ficheros
	 * contenidos en una carpeta.
	 * 
	 * @param foldername
	 *            Nombre de la carpeta
	 */
	public static void showFileTypes(String foldername) {

		// Comprovem que la carpeta és vàlida
		File folder = new File(foldername);
		if (!folder.isDirectory()) {
			System.err.println("ERROR >> Can't read " + foldername);
			return;
		}

		// Llistem fitxers
		File[] files = folder.listFiles();
		byte[] buffer = new byte[4];
		FileInputStream fis = null;
		for (File f : files) {
			try {
				if (f.isFile()) {
					fis = new FileInputStream(f);
					fis.read(buffer);
					String filetype = byteArrayToHexString(buffer);
					if (filetype.startsWith("FFD8")) {
						filetype = "(JPEG)      ";
					} else if (filetype.startsWith("424D")) {
						filetype = "(BMP)       ";
					} else if (filetype.startsWith("3C50")) {
						filetype = "(XML)       ";
					} else if (filetype.startsWith("8950")) {
						filetype = "(PNG)       ";
					} else if (filetype.startsWith("38425053")) {
						filetype = "(Photoshop) ";
					} else if (filetype.startsWith("5375626A")) {
						filetype = "(LAC)       ";
					}
					System.out.println(filetype + " -> " + f.getName());
				}
			} catch (FileNotFoundException e) {
				System.err.println("ERROR >> Can't find " + f.getAbsolutePath());
			} catch (IOException e) {
				System.err.println("ERROR >> Can't read " + f.getAbsolutePath());
			} finally {
				try {
					fis.close();
				} catch (Exception ex) {
					/* Ignore */}
			}
		}
	}

	/**
	 * Genera una cadena hexadecimal que representa un array de bytes
	 * 
	 * @param b
	 *            Array de bytes a representar
	 * @return Cadena de caracteres con la representación hexadecimal del array
	 *         de bytes
	 */
	private static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	
	/**
	 * Compara si dos ficheros son iguales.
	 * 
	 * @param path1 Ruta al primer fichero.
	 * @param path2 Ruta al segundo fichero.
	 * @return <code>true</code> si son iguales, <code>false</code> si no son iguales o se produce algún error
	 */
	public static boolean compareFiles(String path1, String path2) {
		// Validar parámetros
		if (path1 == null || path2 == null) {
			return false;
		}
		File file1 = new File(path1);
		File file2 = new File(path2);
		if (!file1.isFile() || !file2.isFile()) {
			return false;
		}
		
		// Comparar longitud
		if (file1.length() != file2.length()) {
			return false;
		}
		
		// Comparar bytes
		BufferedInputStream in1 = null;
		BufferedInputStream in2 = null;
		boolean result = true;
		try {
			in1 = new BufferedInputStream(new FileInputStream(file1));
			in2 = new BufferedInputStream(new FileInputStream(file2));

			int byte1 = -1;
			int byte2 = -1;
			do {
				byte1 = in1.read();
				byte2 = in2.read();
				if (byte1 != byte2) {
					throw new Exception("Not equal");
				}
			} while (byte1 >= 0);		
		} catch (Exception e) {
			result = false;
		} finally {
			try {in1.close();}  catch (Exception ex) {}
			try {in2.close();}  catch (Exception ex) {}
		}
		
		return result;
	}
	
	/**
	 * Comprova si una carpeta conté un fitxer amb un nom concret. Busca tant en la carpeta
	 * com en totes les subcarpetes.
	 * @param folder Carpeta a analitzar. Inclou les subcarpetes.
	 * @param name Nom de fitxer que s'ha de buscar.
	 * @return <code>true</code> si la carpeta conté un fitxer amb el nom indicat, <code>false</code>
	 * en cas contrari.
	 */
	public static boolean containsFile(File folder, String name) {
		// Comprovem si tenim fitxer amb mateix nom a la carpeta en procés
		File check = new File(folder, name);
		if (check.exists()) {
			return true;
		}
		
		// Comprovem totes les subcarpetes
		File[] subfolders = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		for (File subfolder : subfolders) {
			if (containsFile(subfolder, name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Devuelve la fecha de la última modificación de un archivo
	 * 
	 * @param file archivo
	 * @return fecha de la última modificación 
	 * @throws IOException si no puede obtener los atributos del archivo
	 */
	public static String getLastModified(File file) throws IOException {
		Path p = Paths.get(file.getAbsolutePath());
		BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
		return view.lastModifiedTime().toString();
	}
	
	
    
	/**  
	 * Detecta si hay imágenes en una carpeta o sus subcarpetas.<br>
	 * Se buscan imágenes en formato JPEG 
	 * <br><br>
	 * <strong>AVISO:</strong> Solo se consideran imágenes válidas JPEG (jpe?g). TIFF o PNG no se contemplan.
	 * <br>
	 * 
	 * @param folder Carpeta a partir de la que buscar imágenes
	 * @return true si encuentra una imagen | false si no encuentra ninguna imagen.
	 **/
	public static boolean hasImages(File folder) {
		File[] allFiles = folder.listFiles();
		if (allFiles == null) {
			System.err.println("No es posible listar los archivos de: "+folder.getAbsolutePath());
			return false;
		}
		for (File file : allFiles) {
			if (file.isFile() && file.getName().toLowerCase().matches(".*\\.(jpe?g|png)")) { //.*\\.(jpe?g|tiff?|png)
				return true;
			}
			if (file.isDirectory()) {				
				if (hasImages(file)) {
					return true;
				}
			}
		}
		return false;
	}
}
