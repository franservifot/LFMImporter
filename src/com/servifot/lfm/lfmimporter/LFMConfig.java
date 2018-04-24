package com.servifot.lfm.lfmimporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.servifot.lfm.utils.IniParser;

/**
 * Esta clase carga, guarda y administra la configuración del programa durante
 * su ejecución.
 * 
 * Necesita de un archivo ini para funcionar.
 * El archivo ini tiene [Secciones] y parejas de Clave=Valor
 * 
 * @author FRANCESC
 *
 */
public class LFMConfig {
	
	// CARPETAS
	/** Carpeta de configuracion */
	private String m_configFile = "";
	/** Carpeta donde van las fotos */
	private String m_cameraFolder = LFMImporter.USER_IMAGESFOLDER;
	/** Carpeta donde van las fotos para imprimir */
	private String m_printerFolder = LFMImporter.USER_PRINTERFOLDER;
	/** Carpeta desde donde sacar las fotos */
	private String m_sourceFolder = "\\\\flashair\\DavWWWRoot\\";
	
	// WIFI
	/** Interfaz que se usa para buscar redes disponibles (Se apaga y enciende) */
	private String m_searchInterface = "Wi-Fi";
	/** Nombre del perfil de la red con el que se conecta */
	private String m_wifiSDName = "flashair";
	/** SSID del wifi que genera la carpeta */
	private String m_wifiSDSSID = "flashair";
	/** Interfaz que se usa para conectarse a la tarjeta SD */
	private String m_conectSDInterface = "Wi-Fi";
	
	/**
	 * Ruta del archivo de configuración
	 * 
	 * @param path
	 */
	public LFMConfig(String path) {
		m_configFile = path;
		load(path);
	}
	
	/**
	 * Carga y mantiene en memoria la información de la configuración
	 * 
	 * @param path
	 */
	private void load(String path) {
		if (!new File(path).exists()) {
			System.err.println("En load la ruta [" +path + "] no corresponde a ningún archivo");
			System.err.println("Se cargará la ruta por defecto [" + LFMImporter.USER_CONFIGURATION +"]");
			path = LFMImporter.USER_CONFIGURATION;
		}
		try {
			IniParser ini = new IniParser(path, Charset.forName("UTF-8"));
			
			// Cargamos los datos
			m_cameraFolder = ini.getString("Settings", "cameraFolder", m_cameraFolder);
			m_printerFolder = ini.getString("Settings", "printerFolder", m_printerFolder);
			m_sourceFolder = ini.getString("Settings", "sourceFolder", m_sourceFolder);
			
			m_searchInterface = ini.getString("Wifi", "searchInterface", m_searchInterface);
			m_wifiSDName = ini.getString("Wifi", "wifiSDName", m_wifiSDName);
			m_wifiSDSSID = ini.getString("Wifi", "wifiSDSSID", m_wifiSDSSID);
			m_conectSDInterface = ini.getString("Wifi", "conectSDInterface", m_conectSDInterface);
			
		} catch (Exception e) {
			System.err.println("Error cargando la configuración" + e.getMessage());
		}
	}
	
	/**
	 * Guarda la configuración actual
	 * 
	 * @return <code>true</code> si todo va bien <code>false</code> en caso contrario
	 */
	public boolean save() {
		try {
			File configFile = new File(m_configFile);
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), Charset.forName("UTF-8")));
			String br = System.lineSeparator();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			
			out.write("#Configuración LFMImporter" + br);
			out.write("#" + dateFormat.format(Calendar.getInstance().getTime()) + br + br);
			
			out.write("[Settings]" + br);
			out.write("cameraFolder=" + m_cameraFolder + br);
			out.write("printerFolder=" + m_printerFolder + br);
			out.write("sourceFolder=" + m_sourceFolder + br);
			
			out.write("[Wifi]" + br);
			out.write("searchInterface=" + m_searchInterface + br);
			out.write("wifiSDName=" + m_wifiSDName + br);
			out.write("wifiSDSSID=" + m_wifiSDSSID + br);
			out.write("conectSDInterface=" + m_conectSDInterface + br);
			
			out.close();
			
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	public String getCameraFolder() {
		return m_cameraFolder;
	}

	public void setCameraFolder(String cameraFolder) {
		m_cameraFolder = cameraFolder;
	}
	
	public void setPrinterFolder(String printerFolder) {
		m_printerFolder = printerFolder;
	}
	
	public String getPrinterFolder() {
		return m_printerFolder;
	}

	public String getSourceFolder() {
		return m_sourceFolder;
	}

	public void setSourceFolder(String sourceFolder) {
		m_sourceFolder = sourceFolder;
	}

	public String getSearchInterface() {
		return m_searchInterface;
	}

	public void setSearchInterface(String searchInterface) {
		m_searchInterface = searchInterface;
	}

	public String getWifiSDName() {
		return m_wifiSDName;
	}

	public void setWifiSDName(String wifiSDName) {
		m_wifiSDName = wifiSDName;
	}

	public String getWifiSDSSID() {
		return m_wifiSDSSID;
	}

	public void setWifiSDSSID(String wifiSDSSID) {
		m_wifiSDSSID = wifiSDSSID;
	}

	public String getConectSDInterface() {
		return m_conectSDInterface;
	}

	public void setConectSDInterface(String conectSDInterface) {
		m_conectSDInterface = conectSDInterface;
	}


}
