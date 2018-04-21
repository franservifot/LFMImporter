package com.servifot.lfm.lfmimporter;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ThumbnailWidget extends VBox {

	private File m_imagefile = null;
	private ThumbnailWidgetListener m_listener = null;

	public ThumbnailWidget(File imagefile) {
		m_imagefile = imagefile;

		ImageView iv = new ImageView();
		iv.setFitHeight(LFMImporter.SCREEN_THUMBS_HEIGHT);
		iv.setFitWidth(LFMImporter.SCREEN_THUMBS_HEIGHT);
		iv.setPreserveRatio(true);

		if(m_imagefile.isFile()) {
			iv.setImage(new Image("file:///" + imagefile.getAbsolutePath()));
		}

		getChildren().add(iv);
	}

	public void emitThumbPresed() {
		if (m_listener != null) m_listener.onThumbPresed(m_imagefile);
	}

	public void setListener(ThumbnailWidgetListener listener) {
		m_listener = listener;
	}

	public interface ThumbnailWidgetListener {
		public void onThumbPresed(File file);
	}

}
