package com.servifot.lfm.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.servifot.lfm.lfmimporter.LFMImporter;

/**
 * Utilidades para trabajar con ficheros contenidos como recursos dentro del classpath o en un jar.
 *
 */
public class ResourceUtils {

	/**
	 * Lista los nombres de todos los ficheros contenidos dentro de una ruta del classpath.
	 * 
	 * @param clazz Clase a partir de la que buscar los recursos
	 * @param path Ruta del classpath a analizar
	 * @return Listado de nombres de fichero, puede estar vacío. Nunca <code>null</code>.
	 */
	public static String[] listResources(Class<?> clazz, String path) {
		// Formateamos el path
		String cleanPath = path;
		if (cleanPath.startsWith("/")) {
			cleanPath = path.substring(1);
		}
		if (!cleanPath.endsWith("/")) {
			cleanPath += "/";
		}
		
		// Recuperamos listado de recursos
		String[] resourceNames = null;
		try {
			resourceNames = getResourceListing(clazz, cleanPath);
		} catch (Exception e) {
			/* Ignore */
		}
		
		// Aseguramos que no se devuelve nunca null
		return (resourceNames == null) ? new String[] {} : resourceNames;
	}	
	
	
	/**
	 * Extrae un recurso del classpath a un fichero 
	 * @param clazz Clase a partir de la que buscar los recursos
	 * @param resourcePath Ruta del recurso en el classpath
	 * @param outPath Fichero de salida
	 * @return <code>true</code> si se ha podido extraer correctamente, <code>false</code> en caso contrario
	 */
	public static boolean extractResourceFile(Class<?> clazz, String resourcePath, String outPath) {
		InputStream is = clazz.getResourceAsStream(resourcePath);
		System.out.println("I am extractResourceFile");
		if (is == null) return false;
		OutputStream os = null;
		try {
			os = new FileOutputStream(outPath);
			byte[] buffer = new byte[8192];
			int bytesRead = 0;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {os.close();} catch (Exception ex) {/*Ignore*/}
			try {is.close();} catch (Exception ex) {/*Ignore*/}
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Extrae todos los recursos contenidos en una ruta del classpath a una carpeta.
	 *  
	 * @param clazz Clase a partir de la que buscar los recursos
	 * @param path Ruta del classpath donde buscar recursos
	 * @param folder Carpeta de salida.
	 * @return <code>true</code> si se ha podido extraer correctamente los recursos,
	 *  <code>false</code> en caso contrario
	 */
	public static boolean extractResourceFolder(Class<?> clazz, String path, File folder) {
		String[] resources = ResourceUtils.listResources(clazz, path);
		System.out.println("Extrayendo recursos. Encontrados: " + resources.length);
		if (resources.length > 0) {
			for (String resource : resources) {
				if (resource.equals("")) {
					continue;
				}
				if (!ResourceUtils.extractResourceFile(clazz, path+"/"+resource, folder.getAbsolutePath()+"/"+resource)) {
					System.err.println("No se puede extraer el recurso: "+path+"/"+resource);
					return false;
				}
			}
		} else {
//			InputStream link = clazz.getResourceAsStream(path);
//			try {
//				Files.copy(link, folder.toPath());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		return true;
	}
	
	/**
	  * List directory contents for a resource folder. Not recursive.
	  * This is basically a brute-force implementation.
	  * Works for regular files and also JARs.
	  * 
	  * @author Greg Briggs
	  * @param clazz Any java class that lives in the same place as the resources you want.
	  * @param path Should end with "/", but not start with one.
	  * @return Just the name of each member item, not the full paths.
	  * @throws URISyntaxException 
	  * @throws IOException 
	  */
	private static String[] getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException {
		URL dirURL = clazz.getClassLoader().getResource(path);
		
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			System.out.println(dirURL.getFile());
			/* A file path: easy enough */
			return new File(dirURL.toURI()).list();
		} 
		
		if (dirURL == null) {
			System.out.println("DIRURL IS NULL");
			/* 
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/")+".class";
				dirURL = clazz.getClassLoader().getResource(me);
			  }
			  
			if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
			Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
			while(entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path)) { //filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
			        }
			        result.add(entry);
			    }
			}
			jar.close();
			return result.toArray(new String[result.size()]);
		} 
		
		throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	}
	
	/**
	 * Método que escriben el file que devuelve el contenido del inputstream que se le pasa
	 * 
	 * @param entrada debe representar un fichero
	 * @param nombreTemporal nombre para el fichero que se devuelve (con la extensión) Ej: "archivo.ini"
	 * 
	 * @return File con el contenido del inputstream
	 * 
	 * @throws IOException Si no puede crear, leer, escribir o cerrar el fichero
	 */
	public static File inputStreamToFile(InputStream entrada, String nombreTemporal) throws IOException{
		File f = new File(nombreTemporal);//Aqui le dan el nombre y/o con la ruta del archivo salida
		OutputStream salida = new FileOutputStream(f);
		byte[] buf = new byte[1024];//Actualizado me olvide del 1024
		int len;
		while((len = entrada.read(buf)) > 0) {
			salida.write(buf,0,len);
		}
		salida.close();
		entrada.close();
		return f;
	}
}
