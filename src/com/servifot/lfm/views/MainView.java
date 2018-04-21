package com.servifot.lfm.views;


import java.io.File;

import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.lfmimporter.ThumbnailWidget;
import com.servifot.lfm.lfmimporter.ThumbnailWidget.ThumbnailWidgetListener;
import com.servifot.lfm.utils.FileUtils;

import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class MainView extends View implements ThumbnailWidgetListener {
	private static final String CSS_NAME = "MainView";
	/** Contenedor de la imagen principal */
	private ImageView m_mainView = null;
	/** Contenedor de miniaturas */
	private TilePane m_thumbsPane = null;


	public MainView() {
		super();
	}

	@Override
	public Pane createPane() {

		m_mainView = new ImageView("file:///C:/Users/Vicent/Pictures/LFM/3.JPG");
		m_mainView.setFitWidth(LFMImporter.SCREEN_WIDTH);
		m_mainView.setFitHeight(LFMImporter.SCREEN_HEIGHT-LFMImporter.SCREEN_THUMBS_HEIGHT);
		m_mainView.setPreserveRatio(true);

		VBox centerbox = new VBox(m_mainView);
		centerbox.getStyleClass().add("mv-centerbox");

		m_thumbsPane = new TilePane(Orientation.HORIZONTAL);
		m_thumbsPane.setMaxSize(LFMImporter.SCREEN_WIDTH, LFMImporter.SCREEN_HEIGHT);

		ScrollPane thumbsScrollPane = new ScrollPane(m_thumbsPane);
		thumbsScrollPane.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
		thumbsScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		thumbsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		thumbsScrollPane.setMaxWidth(LFMImporter.SCREEN_WIDTH);
		thumbsScrollPane.setMaxHeight(LFMImporter.SCREEN_THUMBS_HEIGHT);

		fillThumbsPane(m_thumbsPane, LFMImporter.getConfig().getCameraFolder());

		BorderPane rootpane = new BorderPane(centerbox);
		rootpane.setBottom(thumbsScrollPane);
		rootpane.getStyleClass().add("mv-rootpane");
		return rootpane;
	}

	private void fillThumbsPane(TilePane thumbsPane, String folder) {
		File folderimg = new File(folder);
		if (folderimg.exists() && folderimg.isDirectory()) {
			for (File imgFile : folderimg.listFiles()) { //TODO ORDER BY DATE
				if (FileUtils.getExtension(imgFile.getAbsolutePath()).toLowerCase().equals("jpg")) {
					ThumbnailWidget tw = new ThumbnailWidget(imgFile);
					thumbsPane.getChildren().add(tw);
				}
			}
		}
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public void onThumbPresed(File file) {
		// TODO Auto-generated method stub

	}

}
