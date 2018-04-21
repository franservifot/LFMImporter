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
	
	/** Carpeta de configuracion */
	private String m_configFile = "";
	
	/** Carpeta donde van las fotos */
	private String m_cameraFolder = LFMImporter.USER_FOLDER+"/cameraFolder";
	
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
		try {
			IniParser ini = new IniParser(path, Charset.forName("UTF-8"));
			
			// Cargamos los datos
			m_cameraFolder = ini.getString("Settings", "cameraFolder", m_cameraFolder);
			
			
		} catch (Exception e) {
			System.err.println("Error cargando la configuración");
			e.printStackTrace();
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
	
	
}
