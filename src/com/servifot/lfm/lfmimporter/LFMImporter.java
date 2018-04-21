package com.servifot.lfm.lfmimporter;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.print.attribute.standard.PrinterMoreInfoManufacturer;

import com.servifot.lfm.utils.FileUtils;
import com.servifot.lfm.utils.ResourceUtils;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class LFMImporter extends Application {
	
	/** Ruta de recursos css de la aplicación */
	private static final String RESOURCE_PATH_CSS = "/com/servifot/lfm/css";
	/** Ruta de recursos de imágenes de la aplicación */
	public static final String RESOURCE_PATH_IMAGES = "/com/servifot/lfm/images";
	
	/** Carpeta del programa en el directorio del usuario */
	public static final String USER_FOLDER = System.getProperty("user.home")+"/.lfmimporter";
	/** */
	public static final String USER_IMAGESFOLDER = USER_FOLDER + "/cameraImages";
	
	/** Ruta con el archivo de configuración */
	private static String Config_Path = "";
	/** Configuración del programa */
	private static LFMConfig s_config = null;
	/** Carpeta con los recursos gráficos de la aplicación */
	private static File s_graphicsFolder = null;
	
	/** Contenedor principal de layout de la ventana de la aplicación */
	private BorderPane m_root = null;
	/** Gestor principal de layout de la ventana de la aplicación */
	private StackPane m_stackRoot = null;
	
	public static void main (String[] args) { 
		
		// Buscamos el archivo de configuración en los parámetros
		if (args.length < 1) {
			System.err.println("No tengo el archivo de configuración");
			return;
		} else {
			Config_Path = args[0];
		}
		
		if (!initFolders()) {
			System.err.println("No es posible inicializar las carpetas de trabajo.");
			return;
		}
		s_graphicsFolder = getGraphicsFolder();
		if (s_graphicsFolder == null) {
			System.err.println("No se ha podido generar la carpeta de recursos gráficos");
			return;
		}
		if (!extractGraphicsResources(s_graphicsFolder)) {
			System.err.println("No se puede preparar los recursos gráficos del programa.");
			return;
		}
		
		System.out.println("********************************");
		System.out.println("********* ¡ARRANCAMOS! *********");
		System.out.println("********************************");
		
		launch(args);
		
	}
	

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		
		m_root = new BorderPane();
		m_stackRoot = new StackPane();
		m_root.setCenter(m_stackRoot);
		Scene scene = new Scene(m_root);
		
		primaryStage.setTitle("La Foto Mochila");
		primaryStage.setWidth(675);
		primaryStage.setHeight(1080);
		primaryStage.setX(0);
		primaryStage.setY(0);
		//primaryStage.setFullScreen(true);
		primaryStage.setFullScreenExitHint("Para salir, ves a la configuración");
		primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		primaryStage.setScene(scene);
		
		primaryStage.show();
	}
	
	/**
	 * Devuelve la configuración cargada. SI no estaba cargada la carga.
	 * 
	 * @return configuración del programa
	 */
	public static LFMConfig getConfig() {
		if (s_config == null) {
			s_config = new LFMConfig(Config_Path);
		}
		return s_config;
	}
	
	/**
	 * Inicializa las carpetas de trabajo de la aplicación.
	 * 
	 * @return  <code>true</code> si se completa correctamente, <code>false</code> en caso contrario
	 */
	private static boolean initFolders() {
		// Crear carpeta de datos de usuario
		File userFolder = new File(USER_FOLDER);
		if (!FileUtils.createFolder(userFolder)) {
			System.err.println("No es posible crear la carpeta de datos de usuario " + userFolder.getAbsolutePath());
			return false;
		}
		
		// Crea la carpeta donde van las imágenes de la cámara
		File cameraFolder = new File(getConfig().getCameraFolder());
		if (!FileUtils.createFolder(cameraFolder)) {
			System.err.println("No es posible crear la carpeta de la cámara " + userFolder.getAbsolutePath());
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Crea una carpeta para almacenar las clases con los estilos.
	 * 
	 * @return El archivo que representa la carpeta o null si no la puede crear.
	 */
	public static File getGraphicsFolder() {
		File tempGraphicsFolder = null;
		try {
			String graphicsPath = USER_FOLDER +"/estilos";
			int i = 0;
			while (new File(graphicsPath).exists()) {
				graphicsPath = USER_FOLDER + "/estilos_" + i;
				i++;
			}
			
			tempGraphicsFolder = new File(graphicsPath);
			if (!tempGraphicsFolder.mkdirs()) {
				return null;
			}
		} catch (Exception e) {
			System.err.println("Error creando la carpeta de los estilos " + e.getMessage());
			return null;
		}
		
		return tempGraphicsFolder;
	}
	
	/**
	 * Extrae los ficheros de diseño (CSS e imágenes) contenidos en los recursos de la aplicación.
	 * 
	 * @param folder Carpeta donde extraer los ficheros de diseño
	 * @return <code>true</code> si se extraen correctamente, <code>false</code> en caso contrario
	 */
	private static boolean extractGraphicsResources(File folder) {
		
		// Preparamos carpetas donde extraer los recursos
		File cssFolder = new File(folder.getAbsolutePath()+"/css");
		if (!FileUtils.createFolder(cssFolder)) {
			System.err.println("No se puede crear la carpeta "+cssFolder.getAbsolutePath());
			return false;
		}
		File imagesFolder = new File(folder.getAbsolutePath()+"/images"); 
		if (!FileUtils.createFolder(imagesFolder)) {
			System.err.println("No se puede crear la carpeta "+imagesFolder.getAbsolutePath());
			return false;
		}

		// Extraemos todos los recursos
		if (!ResourceUtils.extractResourceFolder(LFMImporter.class, RESOURCE_PATH_CSS, cssFolder)) {
			return false;
		}
		if (!ResourceUtils.extractResourceFolder(LFMImporter.class, RESOURCE_PATH_IMAGES, imagesFolder)) {
			return false;
		}		

		return true;
	}
	
	
	public static void searchWifi() {
		Runtime rt = Runtime.getRuntime();
		
		try {
			Object lock = new Object();
			String s = "";
			
			while (true) {
			
				// Detenemos el wifi
				Process pr = rt.exec("netsh interface set interface name=\"Wi-Fi\" admin=disabled");
				BufferedReader br1 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				System.out.println("Detenemos el wifi");
				while ((s = br1.readLine()) != null) {
					System.out.println(">>>>" + s);
					s = "";
				}
				pr.waitFor();

				// Arrancamos el wifi
				pr = rt.exec("netsh interface set interface name=\"Wi-Fi\" admin=enabled");	
				pr.waitFor();
				
				boolean interfazfound = false;
				boolean encontrado = false;
				while (!interfazfound) {
					// Buscamos si está disponible el wifi
					pr = rt.exec("netsh wlan show networks");
					BufferedReader br2 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					pr.waitFor();
					System.out.println("Buscamos flashair...");
					
					while ((s = br2.readLine()) != null) {
						if (s.toLowerCase().contains("no hay")) {
							System.out.println("No hay ninguna interfaz, esperamos 1 segundo...");
							System.out.println("Esperamos...");
							synchronized(lock) {
							// write your code here. You may use wait() or notify() as per your requirement.
							    lock.wait(1000);
							}
							System.out.println("Seguimos");
						} else {
							interfazfound = true;
						}
						if (s.contains("Actualmente")) {
							System.out.println(s);
						} else if (s.toLowerCase().contains("flashair")) {
							System.out.println(s);
							encontrado = true;
						}
						s = "";
					}
				
					if (encontrado) {
						System.out.println("HEMOS ENCONTRADO EL WIFI");
					}
				}
				
				//pr.waitFor();
				
				if (!encontrado) {
					System.out.println("Start wait...");
					synchronized(lock) {
					// write your code here. You may use wait() or notify() as per your requirement.
					    lock.wait(2000);
					}
					
					System.out.println("Stop wait\n\n");
				} else {
					System.out.println("Vamos a conectarnos...");
					//netsh wlan connect name=flashair ssid=flashair interface=Wi-Fi
					pr = rt.exec("netsh wlan connect name=flashair ssid=flashair interface=Wi-Fi");
					BufferedReader br3 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					pr.waitFor();
					System.out.println("Buscamos flashair...");
					while ((s = br3.readLine()) != null) {
						if (s.toLowerCase().contains("correctamente")) {
							System.out.println(s);
							System.out.println("***ESTAMOS CONECTADOS. FIN***");
						} 
						s = "";
					}
					
					String fotopath = "//flashair/DavWWWRoot/DCIM/100CANON/IMG_0229.JPG";
					File img = new File(fotopath);
					for (int i = 0; i < 10 && !img.exists(); i++) {
						System.out.println("Start wait... " + i);
						synchronized(lock) {
						// write your code here. You may use wait() or notify() as per your requirement.
						    lock.wait(1000);
						}
					}
					System.out.println("File exist:" + img.exists());
					if (img.exists()) {
						File dest = new File("B:/Files/LFMochila/imagen1.jpg");
						FileUtils.copyFile(img, dest, true);
						Desktop.getDesktop().open(dest);
					}
					return;
				}

			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}