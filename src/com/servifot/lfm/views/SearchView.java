package com.servifot.lfm.views;

import java.io.File;

import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.lfmimporter.PaneFiller;
import com.servifot.lfm.lfmimporter.SearchThumbnailWidget;
import com.servifot.lfm.lfmimporter.SearchThumbnailWidget.SearchThumbnailWidgetListener;

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

public class SearchView extends View implements SearchThumbnailWidgetListener {
	private static final String CSS_NAME = "SearchView";

	private File m_folder = null;
	private PaneFiller m_panefiller = null;
	private SearchViewListener m_listener = null;


	public SearchView(File folder) {
		m_folder = folder;
	}

	public SearchView(String cameraFolder) {
		m_folder = new File(cameraFolder);
	}

	@Override
	public Pane createPane() {

		Slider sl = new Slider(-100, 400, SearchThumbnailWidget.getZoom());
		sl.setMinWidth(300);
		sl.setMaxWidth(300);
		sl.setPrefWidth(300);
		sl.getStyleClass().add("sv-slider");

		Button closebutton = new Button("Close");
		closebutton.getStyleClass().add("sv-closebutton");
		closebutton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (m_panefiller != null) m_panefiller.kill();
				emitAddedViewFinished();
			}
		});

		HBox topbhBox = new HBox(25, sl, closebutton);
		topbhBox.getStyleClass().add("sv-tophbox");

		TilePane tp = new TilePane();
		tp.getStyleClass().add("sv-tilepane");
		tp.setMaxWidth(LFMImporter.SCREEN_WIDTH-50);
		tp.setPrefWidth(LFMImporter.SCREEN_WIDTH-50);
		tp.setMinWidth(LFMImporter.SCREEN_WIDTH-50);

		ScrollPane sp = new ScrollPane(tp);
		sp.getStyleClass().add("sv-scrollpane");
		sp.setMaxWidth(LFMImporter.SCREEN_WIDTH);

		m_panefiller = new PaneFiller(tp, m_folder, this);
		m_panefiller.start();

		VBox rootBox = new VBox(25, topbhBox, sp);
		rootBox.getStyleClass().add("sv-rootVbox");


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
	
	public SearchViewListener getListener() {
		return m_listener;
	}

	public void setListener(SearchViewListener listener) {
		m_listener = listener;
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public void onSearchThumbSelect(File file) {
		emitThumbSelect(file);
		emitAddedViewFinished();
	}
	
	private void emitThumbSelect(File file) {
		if (m_listener != null) m_listener.onSearchThumbSelect(file);
		
	}

	public interface SearchViewListener {
		public void onSearchThumbSelect(File f);
	}

}
