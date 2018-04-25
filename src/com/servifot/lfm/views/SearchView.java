package com.servifot.lfm.views;

import java.io.File;

import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.lfmimporter.PaneFiller;
import com.servifot.lfm.lfmimporter.SearchThumbnailWidget;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class SearchView extends View {
	private static final String CSS_NAME = "SearchView";

	private File m_folder = null;
	PaneFiller m_panefiller = null;


	public SearchView(File folder) {
		m_folder = folder;
	}

	public SearchView(String cameraFolder) {
		m_folder = new File(cameraFolder);
	}

	@Override
	public Pane createPane() {

		Slider sl = new Slider(-100, 400, SearchThumbnailWidget.getZoom());

		Button closebutton = new Button("Close");
		closebutton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (m_panefiller != null) m_panefiller.kill();
				emitAddedViewFinished();
			}
		});

		HBox topbhBox = new HBox(25, sl, closebutton);

		TilePane tp = new TilePane();
		tp.setMaxWidth(LFMImporter.SCREEN_WIDTH-50);
		tp.setPrefWidth(LFMImporter.SCREEN_WIDTH-50);
		tp.setMinWidth(LFMImporter.SCREEN_WIDTH-50);

		ScrollPane sp = new ScrollPane(tp);
		sp.setMaxWidth(LFMImporter.SCREEN_WIDTH);

		m_panefiller = new PaneFiller(tp, m_folder);
		m_panefiller.start();

		VBox rootBox = new VBox(25, topbhBox, sp);


		sl.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				SearchThumbnailWidget.setZoom(newValue.intValue());
				for (Object n : tp.getChildren()) {
					if (n instanceof SearchThumbnailWidget) {
						((SearchThumbnailWidget)n).resize();
					}
				}
			}
		});

		return rootBox;
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

}
