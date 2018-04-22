package com.servifot.lfm.views;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.lfmimporter.ThumbnailWidget;
import com.servifot.lfm.lfmimporter.ThumbnailWidget.ThumbnailWidgetListener;
import com.servifot.lfm.utils.FileUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class MainView extends View implements ThumbnailWidgetListener {
	private static final String CSS_NAME = "MainView";
	/** Contenedor de la imagen principal */
	private ImageView m_mainView = null;
	/** Contenedor de miniaturas */
	private TilePane m_thumbsPane = null;
	/** Archivo seleccionado actual */
	private File m_currentFile = null;


	/** Rotación actual de la imagen */
	private int m_rotation = 0;

	public MainView() {
		super();
	}

	@Override
	public Pane createPane() {
		// Pintamos la imagen principal
		m_mainView = new ImageView("file:///C:/Users/Vicent/Pictures/LFM/3.JPG");
		m_mainView.setFitWidth(LFMImporter.SCREEN_WIDTH);
		m_mainView.setFitHeight(LFMImporter.SCREEN_HEIGHT-(LFMImporter.SCREEN_THUMBS_HEIGHT+50));
		m_mainView.setPreserveRatio(true);
		//TODO m_currentFile = CURRENT FILE;

		VBox centerbox = new VBox(m_mainView);
		centerbox.getStyleClass().add("mv-centerbox");

		centerbox.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() > 1 ) {
					if (m_mainView != null) {
						rotate(m_mainView);
					}
				}

			}
		});

		// Creamos el panel para las miniaturas
		m_thumbsPane = new TilePane(Orientation.VERTICAL);
		m_thumbsPane.setPrefRows(1);
		m_thumbsPane.getStyleClass().add("mv-tp");
		//m_thumbsPane.setMaxSize(LFMImporter.SCREEN_WIDTH, LFMImporter.SCREEN_HEIGHT);

		ScrollPane thumbsScrollPane = new ScrollPane(m_thumbsPane);
		thumbsScrollPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		thumbsScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		thumbsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		//thumbsScrollPane.setMaxWidth(LFMImporter.SCREEN_WIDTH);
		thumbsScrollPane.setMaxHeight(LFMImporter.SCREEN_THUMBS_HEIGHT);
		thumbsScrollPane.setFitToWidth(false);
		thumbsScrollPane.getStyleClass().add("mv-sp");
		// Llenamos el panel de miniaturas
		fillThumbsPane(m_thumbsPane, LFMImporter.getConfig().getCameraFolder());

		// Creamos la caja de los botones
		Button editbtn = new Button("Editar");
		Button configbtn = new Button("Config");

		editbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					Desktop.getDesktop().open(m_currentFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		VBox btnsBox = new VBox(editbtn, configbtn);
		btnsBox.getStyleClass().add("mv-btnsBox");

		HBox botBox = new HBox(thumbsScrollPane, btnsBox);

		// Ensamblamos
		BorderPane rootpane = new BorderPane(centerbox);
		rootpane.setBottom(botBox);
		rootpane.getStyleClass().add("mv-rootpane");
		return rootpane;
	}

	private void fillThumbsPane(TilePane thumbsPane, String folder) {
		File folderimg = new File(folder);
		if (folderimg.exists() && folderimg.isDirectory()) {
			for (File imgFile : folderimg.listFiles()) { //TODO ORDER BY DATE
				if (FileUtils.getExtension(imgFile.getAbsolutePath()).toLowerCase().equals("jpg")) {
					ThumbnailWidget tw = new ThumbnailWidget(imgFile);
					tw.setListener(this);
					thumbsPane.getChildren().add(tw);
				}
			}
		}
	}

	protected void rotate(ImageView iv) {
		if (m_rotation == 0) {
			m_rotation = 90;
			iv.setFitWidth(LFMImporter.SCREEN_HEIGHT-(LFMImporter.SCREEN_THUMBS_HEIGHT+50));
			iv.setFitHeight(LFMImporter.SCREEN_WIDTH);
		} else {
			m_rotation = 0;
			iv.setFitWidth(LFMImporter.SCREEN_WIDTH);
			iv.setFitHeight(LFMImporter.SCREEN_HEIGHT-(LFMImporter.SCREEN_THUMBS_HEIGHT+50));
		}
		iv.setRotate(m_rotation);
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public void onThumbPresed(File file) {
		if (file.isFile()) {
			m_mainView.setImage(new Image("file:///" + file.getAbsolutePath()));
		}
		m_currentFile = file;
	}

}
