package com.servifot.lfm.lfmimporter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WifiSDConector extends Thread {
	
	private WifiSDConectorListener m_listener = null;
	private boolean m_killed = false;
	
	public WifiSDConector() {}
	
	@Override
	public void run() {
		try {
			Runtime rt = Runtime.getRuntime();
			Object lock = new Object();
			String s = "";
			boolean sdConection = false;
			
			while (!sdConection && !m_killed) {
				// Detenemos el wifi
				Process pr = rt.exec("netsh interface set interface name=\"" + LFMImporter.getConfig().getSearchInterface() + "\" admin=disabled");
				BufferedReader br1 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				System.out.println("Detenemos el wifi");
				while ((s = br1.readLine()) != null) {
					System.out.println(">>>>" + s);
					s = "";
				}
				pr.waitFor();
				br1.close();
				if (m_killed) return;
				// Arrancamos el wifi
				pr = rt.exec("netsh interface set interface name=\"" + LFMImporter.getConfig().getSearchInterface() + "\" admin=enabled");
				pr.waitFor();
				if (m_killed) return;
				
				boolean interfazfound = false;
				boolean encontrado = false;
				while (!interfazfound) {
					if (m_killed) return;
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
					br2.close();
					if (m_killed) return;
					if (encontrado) {
						System.out.println("HEMOS ENCONTRADO EL WIFI");
					}
				}
				
				// Si no se encuentra el wifi esperamos y repetimos el proceso
				if (!encontrado) {
					System.out.println("Start wait...");
					synchronized(lock) {
						lock.wait(2000);
						if (m_killed) return;
					}

					System.out.println("Stop wait\n\n");
				} else {
				// Si encontramos el wifi nos conectamos
					System.out.println("Vamos a conectarnos...");
					if (m_killed) return;
					String comand = "netsh wlan connect name=\""+LFMImporter.getConfig().getWifiSDName()+"\" ssid=\""+LFMImporter.getConfig().getWifiSDSSID()+"\" interface=\""+LFMImporter.getConfig().getConectSDInterface()+"\"";
					System.out.println(comand);
					pr = rt.exec(comand);
					BufferedReader br3 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					pr.waitFor();
					while ((s = br3.readLine()) != null) {
						if (s.toLowerCase().contains("correctamente")) {
							System.out.println(s);
							System.out.println("***ESTAMOS CONECTADOS***");
							sdConection = true;
							emitSDConection();
						}
						s = "";
					}
					br3.close();
//					String fotopath = "//flashair/DavWWWRoot/DCIM/100CANON/IMG_0229.JPG";
//					File img = new File(fotopath);
//					for (int i = 0; i < 10 && !img.exists(); i++) {
//						System.out.println("Start wait... " + i);
//						synchronized(lock) {
//						// write your code here. You may use wait() or notify() as per your requirement.
//						    lock.wait(1000);
//						}
//					}
//					System.out.println("File exist:" + img.exists());
//					if (img.exists()) {
//						File dest = new File("B:/Files/LFMochila/imagen1.jpg");
//						FileUtils.copyFile(img, dest, true);
//						Desktop.getDesktop().open(dest);
//					}
//					return;
				}

			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			//TODO DEAL WITH EXCEPTIONS
		}
		m_killed = true;
	}
	
	public void setListener(WifiSDConectorListener listener) {
		m_listener = listener;
	}
	
	private void emitSDConection() {
		if (m_listener != null) m_listener.onSDConnection();
		
	}
	
	public interface WifiSDConectorListener {
		public void onSDConnection();
	}

	public void kill() {
		m_killed = true;
		
	}

	public boolean isKilled() {
		return m_killed;
	}
	
}
