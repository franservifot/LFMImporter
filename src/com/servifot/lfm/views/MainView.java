package com.servifot.lfm.views;


import com.servifot.lfm.lfmimporter.LFMImporter;

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MainView extends View {
	private static final String CSS_NAME = "MainView";

	
	public MainView() {
		super();
	}
	
	@Override
	public Pane createPane() {
		
		ImageView mainView = new ImageView("file:///C:/Users/FRANCESC/.lfmimporter/cameraFolder/Imprimir047.JPG");
		mainView.setFitWidth(LFMImporter.SCREEN_WIDTH);
		mainView.setFitHeight(LFMImporter.SCREEN_HEIGHT-LFMImporter.SCREEN_THUMBS_HEIGHT);
		mainView.setPreserveRatio(true);
		
		VBox centerbox = new VBox(mainView);
		centerbox.getStyleClass().add("mv-centerbox");
		
		
		BorderPane rootpane = new BorderPane(centerbox);
		rootpane.getStyleClass().add("mv-rootpane");
		return rootpane;
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

}
