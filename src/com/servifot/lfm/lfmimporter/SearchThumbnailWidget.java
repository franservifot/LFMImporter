package com.servifot.lfm.lfmimporter;

import java.io.File;
import java.io.IOException;

import com.servifot.lfm.utils.ImageOrientation;
import com.servifot.lfm.utils.JPEGMetadata;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class SearchThumbnailWidget extends VBox {

	private static final int VIEW_SIZE = 125;

	public static int s_zoom = 0;

	private File m_imgFile = null;
	private ImageView m_iv = new ImageView();
	private SearchThumbnailWidgetListener m_listener = null;

	public SearchThumbnailWidget(File imgFile) {
		m_imgFile = imgFile;

		if (m_imgFile.isFile()) {
			// Sacamos la miniatura si tiene
			JPEGMetadata prevjpg = null;
			try {
				prevjpg = new JPEGMetadata(m_imgFile.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Image img = null;
			if (prevjpg != null && prevjpg.hasThumbnail()) {
				img = new Image(prevjpg.getThumbnailAsInputStream());
			} else {
				img = new Image("file:///"+ m_imgFile.getAbsolutePath());
			}

			m_iv = new ImageView();
			m_iv.setPreserveRatio(true);
			m_iv.setFitHeight(VIEW_SIZE + s_zoom);
			m_iv.setFitWidth(VIEW_SIZE + s_zoom);
			m_iv.setImage(img);
			
			this.setPrefHeight(VIEW_SIZE + s_zoom);
			this.setMaxHeight(VIEW_SIZE + s_zoom);
			this.setMinHeight(VIEW_SIZE + s_zoom);
			this.setPrefWidth(VIEW_SIZE + s_zoom);
			this.setMaxWidth(VIEW_SIZE + s_zoom);
			this.setMinWidth(VIEW_SIZE + s_zoom);

			// Rotamos la miniatura para que se vea vertical
			switch (prevjpg.getOrientation().toString()) {
			case ImageOrientation.VALUE_DOWN:
				m_iv.setRotate(180);
				break;
			case ImageOrientation.VALUE_LEFT:
				m_iv.setRotate(90);
				break;
			case ImageOrientation.VALUE_RIGHT:
				m_iv.setRotate(270);
				break;
			default:
				m_iv.setRotate(0);
				break;
			}
			getStyleClass().add("sv-searchthumb-vbox");
			m_iv.getStyleClass().add("sv-searchthumb-imageview");
			getChildren().add(m_iv);
			this.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getClickCount() > 1) {
						emitThumbSelect();
					}
				}
			});
		}
	}

	public void resize() {
		m_iv.setFitHeight(VIEW_SIZE + s_zoom);
		m_iv.setFitWidth(VIEW_SIZE + s_zoom);
		this.setPrefHeight(VIEW_SIZE + s_zoom);
		this.setMaxHeight(VIEW_SIZE + s_zoom);
		this.setMinHeight(VIEW_SIZE + s_zoom);
		this.setPrefWidth(VIEW_SIZE + s_zoom);
		this.setMaxWidth(VIEW_SIZE + s_zoom);
		this.setMinWidth(VIEW_SIZE + s_zoom);
	}

	private void emitThumbSelect() {
		if (m_listener != null) m_listener.onSearchThumbSelect(m_imgFile);
	}

	public File getImgFile() {
		return m_imgFile;
	}

	public void setImgFile(File imgFile) {
		m_imgFile = imgFile;
	}

	public SearchThumbnailWidgetListener getListener() {
		return m_listener;
	}

	public void setListener(SearchThumbnailWidgetListener listener) {
		m_listener = listener;
	}

	public static int getZoom() {
		return s_zoom;
	}

	public static void setZoom(int zoom) {
		s_zoom = zoom;
	}
	
	public interface SearchThumbnailWidgetListener {
		public void onSearchThumbSelect(File file);
	}
}
