package com.servifot.lfm.views;

import java.io.File;
import java.io.IOException;

import com.servifot.lfm.lfmimporter.LFMConfig;
import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.utils.LFMUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ConfigView extends View {
	private static final String CSS_NAME = "ConfigView";

	private static final int HSPACE = 10;

	private static final int VSPACE = 15;

	/** Escucha los eventos de la interfaz */
	private ConfigViewListener m_listener = null;

	@Override
	public Pane createPane() {

		// CARPETAS
		// Elegir carpeta de imágenes de la cámara
		Label carpetaImatgesLbl = new Label("Carpeta imágenes: ");
		TextField carpetaImatgespathLbl = new TextField(LFMImporter.getConfig().getCameraFolder());
		Button carpetaImatgesSelectbtn = new Button("Seleccionar");
		Button carpetaImatgesOpenbtn = new Button("Abrir");
		HBox carpetaImatgesBox = new HBox(HSPACE, carpetaImatgesLbl, carpetaImatgespathLbl, carpetaImatgesSelectbtn, carpetaImatgesOpenbtn);

		Label carpetaPrinterLbl = new Label("Carpeta impresión: ");
		TextField carpetaPrinterpathLbl = new TextField(LFMImporter.getConfig().getPrinterFolder());
		Button carpetaPrinterSelectbtn = new Button("Seleccionar");
		Button carpetaPrinterOpenbtn = new Button("Abrir");
		HBox carpetaPrinterBox = new HBox(HSPACE, carpetaPrinterLbl, carpetaPrinterpathLbl, carpetaPrinterSelectbtn, carpetaPrinterOpenbtn);

		Label carpetaOrigenlbl = new Label("Carpeta origen: ");
		TextField carpetaOrigentf = new TextField(LFMImporter.getConfig().getSourceFolder());
		HBox carpetaOrigenBox = new HBox(HSPACE, carpetaOrigenlbl, carpetaOrigentf);

		VBox carpetasBox = new VBox(VSPACE, carpetaImatgesBox, carpetaPrinterBox, carpetaOrigenBox);

		TitledPane carpetasTP = new TitledPane("Carpetas", carpetasBox);
		carpetasTP.setExpanded(true);

		// IMPRESORA
		Label selectPrinterlbl = new Label("Impresora: ");
		ObservableList<String> selectPrinterOL = LFMConfig.getPrintersAvaylable();
		ComboBox<String> selectPrinterCB = new ComboBox<>(selectPrinterOL);
		selectPrinterCB.setValue(LFMImporter.getConfig().getPrinter());
		HBox selectPrinterBox = new HBox(HSPACE, selectPrinterlbl, selectPrinterCB);

		TitledPane printerTP = new TitledPane("Impresora", selectPrinterBox);
		printerTP.setExpanded(false);

		// WIFI
		Label searchInterfacelbl = new Label("Interfaz de búsqueda: ");
		ObservableList<String> searchInterfazList = FXCollections.observableArrayList();
		LFMUtils.fillWifiInterfaces(searchInterfazList);
		ComboBox<String> searchInterfazCb = new ComboBox<>(searchInterfazList);//TODO SET OBSERVABLELIST
		searchInterfazCb.setValue(LFMImporter.getConfig().getSearchInterface());
		HBox searchInterfazBox = new HBox(HSPACE, searchInterfacelbl, searchInterfazCb);

		Label wifinamelbl = new Label("Perfil wifi: ");
		TextField wifinametf = new TextField(LFMImporter.getConfig().getWifiSDName());
		HBox wifinamebox = new HBox(HSPACE, wifinamelbl, wifinametf);

		Label wifissidlbl = new Label("SSID wifi: ");
		TextField wifissidtf = new TextField(LFMImporter.getConfig().getWifiSDSSID());
		HBox wifissidbox = new HBox(HSPACE, wifissidlbl, wifissidtf);

		Label connectInterfacelbl = new Label("Interfaz de búsqueda: ");
		ObservableList<String> connectInterfazList = FXCollections.observableArrayList();
		LFMUtils.fillWifiInterfaces(connectInterfazList);
		ComboBox<String> connectInterfazCb = new ComboBox<>(connectInterfazList);//TODO SET OBSERVABLELIST
		connectInterfazCb.setValue(LFMImporter.getConfig().getConectSDInterface());
		HBox connectInterfazBox = new HBox(HSPACE, connectInterfacelbl, connectInterfazCb);

		VBox wifisBox = new VBox(VSPACE, searchInterfazBox, wifinamebox, wifissidbox, connectInterfazBox);

		TitledPane wifisTP = new TitledPane("WiFi", wifisBox);

		Button minimBtn = new Button("Minimizar");
		Button fullscreenBtn = new Button("Full screen");
		Button closeBtn = new Button("Cerrar");
		closeBtn.getStyleClass().add("cv-close-btn");

		HBox btnsBox = new HBox(HSPACE, minimBtn, fullscreenBtn, closeBtn);

		Button accepts = new Button("Aplicar");
		Button cancel = new Button("Volver");

		HBox acceptcancelbox = new HBox(HSPACE, accepts, cancel);

		VBox rootBox = new VBox(VSPACE, carpetasTP, printerTP, wifisTP, btnsBox, acceptcancelbox);

		carpetaImatgesSelectbtn.getStyleClass().addAll("cv-btn", "cv-btn-carpetaImatgesSelectbtn");
		carpetaImatgesOpenbtn.getStyleClass().addAll("cv-btn", "cv-btn-carpetaImatgesOpenbtn");
		carpetaPrinterSelectbtn.getStyleClass().addAll("cv-btn", "cv-btn-carpetaPrinterSelectbtn");
		carpetaPrinterOpenbtn.getStyleClass().addAll("cv-btn", "cv-btn-carpetaPrinterOpenbtn");
		minimBtn.getStyleClass().addAll("cv-btn", "cv-btn-minimBtn");
		fullscreenBtn.getStyleClass().addAll("cv-btn", "cv-btn-fullscreenBtn");
		closeBtn.getStyleClass().addAll("cv-btn", "cv-btn-closeBtn");
		accepts.getStyleClass().addAll("cv-btn", "cv-btn-accepts");
		cancel.getStyleClass().addAll("cv-btn", "cv-btn-cancel");

		carpetaImatgesLbl.getStyleClass().addAll("cv-lbl", "cv-lbl-carpetaImatgesLbl");
		carpetaPrinterLbl.getStyleClass().addAll("cv-lbl", "cv-lbl-carpetaPrinterLbl");
		carpetaOrigenlbl.getStyleClass().addAll("cv-lbl", "cv-lbl-carpetaOrigenlbl");
		selectPrinterlbl.getStyleClass().addAll("cv-lbl", "cv-lbl-selectPrinterlbl");
		searchInterfacelbl.getStyleClass().addAll("cv-lbl", "cv-lbl-searchInterfacelbl");
		wifinamelbl.getStyleClass().addAll("cv-lbl", "cv-lbl-wifinamelbl");
		wifissidlbl.getStyleClass().addAll("cv-lbl", "cv-lbl-wifissidlbl");
		connectInterfacelbl.getStyleClass().addAll("cv-lbl", "cv-lbl-connectInterfacelbl");

		carpetasBox.getStyleClass().addAll("cv-vbox", "cv-vbox-carpetasBox");
		wifisBox.getStyleClass().addAll("cv-vbox", "cv-vbox-wifisBox");
		rootBox.getStyleClass().addAll("cv-vbox", "cv-vbox-rootBox");

		carpetaImatgesBox.getStyleClass().addAll("cv-hbox","cv-hbox-carpetaImatgesBox");
		carpetaPrinterBox.getStyleClass().addAll("cv-hbox","cv-hbox-carpetaPrinterBox");
		carpetaOrigenBox.getStyleClass().addAll("cv-hbox","cv-hbox-carpetaOrigenBox");
		selectPrinterBox.getStyleClass().addAll("cv-hbox","cv-hbox-selectPrinterBox");
		searchInterfazBox.getStyleClass().addAll("cv-hbox","cv-hbox-searchInterfazBox");
		wifinamebox.getStyleClass().addAll("cv-hbox","cv-hbox-wifinamebox");
		wifissidbox.getStyleClass().addAll("cv-hbox","cv-hbox-wifissidbox");
		connectInterfazBox.getStyleClass().addAll("cv-hbox","cv-hbox-connectInterfazBox");
		btnsBox.getStyleClass().addAll("cv-hbox","cv-hbox-btnsBox");
		acceptcancelbox.getStyleClass().addAll("cv-hbox","cv-hbox-acceptcancelbox");

		carpetaImatgespathLbl.getStyleClass().addAll("cv-tf", "cv-tf-carpetaImatgespathLbl");
		carpetaPrinterpathLbl.getStyleClass().addAll("cv-tf", "cv-tf-carpetaPrinterpathLbl");
		carpetaOrigentf.getStyleClass().addAll("cv-tf", "cv-tf-carpetaOrigentf");
		wifinametf.getStyleClass().addAll("cv-tf", "cv-tf-wifinametf");
		wifissidtf.getStyleClass().addAll("cv-tf", "cv-tf-wifissidtf");

		// Implementamos funcionalidades
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				emitAddedViewFinished();
			}
		});
		// Guarda la información
		accepts.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				LFMImporter.getConfig().setCameraFolder(carpetaImatgespathLbl.getText());
				LFMImporter.getConfig().setPrinterFolder(carpetaPrinterpathLbl.getText());
				LFMImporter.getConfig().setSourceFolder(carpetaOrigentf.getText());

				LFMImporter.getConfig().setPrinter(selectPrinterCB.getValue());

				LFMImporter.getConfig().setSearchInterface(searchInterfazCb.getValue());
				LFMImporter.getConfig().setWifiSDName(wifinametf.getText());
				LFMImporter.getConfig().setWifiSDSSID(wifissidtf.getText());
				LFMImporter.getConfig().setConectSDInterface(connectInterfazCb.getValue());

				LFMImporter.getConfig().save();
			}
		});
		// Cierra la aplicación
		closeBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.exit();
			}
		});

		// Pantalla completa
		fullscreenBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Stage stage = (Stage) fullscreenBtn.getScene().getWindow();
				stage.setFullScreen(!stage.isFullScreen());
			}
		});

		// Minimiza la ventana
		minimBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Stage stage = (Stage) fullscreenBtn.getScene().getWindow();
				stage.setIconified(true);
			}
		});

		// Selecionar carpeta donde caen las fotos
		carpetaImatgesSelectbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser folderc = new DirectoryChooser();
				File cameraFolder = folderc.showDialog(carpetaImatgesOpenbtn.getScene().getWindow());
				if (cameraFolder != null && cameraFolder.isDirectory()) {
					carpetaImatgespathLbl.setText(cameraFolder.getAbsolutePath());
				}
			}
		});

		carpetaImatgesOpenbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Runtime.getRuntime().exec("explorer.exe " + (carpetaImatgespathLbl.getText()).replaceAll("/", "\\\\"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// Selecionar carpeta donde caen las fotos
		carpetaPrinterSelectbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser folderc = new DirectoryChooser();
				File cameraFolder = folderc.showDialog(carpetaPrinterOpenbtn.getScene().getWindow());
				if (cameraFolder != null && cameraFolder.isDirectory()) {
					carpetaPrinterpathLbl.setText(cameraFolder.getAbsolutePath());
				}
			}
		});

		carpetaPrinterOpenbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Runtime.getRuntime().exec("explorer.exe " + (carpetaPrinterpathLbl.getText()).replaceAll("/", "\\\\"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		return rootBox;
	}

	public void setListener(ConfigViewListener listener) {
		m_listener = listener;
	}

	public void emitConfigApply() {
		if (m_listener != null) {
			m_listener.onConfigApply();
		}
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	public interface ConfigViewListener {
		public void onConfigApply();
	}

}
