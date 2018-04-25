package com.servifot.lfm.lfmimporter;

import java.io.File;
import java.io.IOException;

import com.servifot.lfm.utils.JPEGMetadata;
import com.servifot.lfm.utils.LFMUtils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class ThumbnailWidget extends VBox {

	private File m_imagefile = null;
	private ThumbnailWidgetListener m_listener = null;

	public ThumbnailWidget(File imagefile) {
		m_imagefile = imagefile;

		Canvas iv = new Canvas(LFMImporter.SCREEN_THUMBS_HEIGHT, LFMImporter.SCREEN_THUMBS_HEIGHT);

		if(m_imagefile.isFile()) {
			// Probamos de cargar la miniatura
			JPEGMetadata prevjpg = null;
			try {
				prevjpg = new JPEGMetadata(imagefile.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Image img = null;
			if (prevjpg != null && prevjpg.hasThumbnail()) {
				img = new Image(prevjpg.getThumbnailAsInputStream());
			} else {
				img = new Image("file:///"+m_imagefile.getAbsolutePath());	
			}
			
			Rectangle imgrectangle = new Rectangle(0, 0, img.getWidth(), img.getHeight());
			Rectangle canvasRectangle = new Rectangle(0, 0, LFMImporter.SCREEN_THUMBS_HEIGHT, LFMImporter.SCREEN_THUMBS_HEIGHT);
			Rectangle imgSource = LFMUtils.fitRectangle(imgrectangle, canvasRectangle);
			//Rectangle imgSource = LFMUtils.fitRectangle(canvasRectangle, imgrectangle);
			iv.getGraphicsContext2D().drawImage(img, imgSource.getX(), imgSource.getY(), imgSource.getWidth(), imgSource.getHeight(), canvasRectangle.getX(), canvasRectangle.getY(), canvasRectangle.getWidth(), canvasRectangle.getHeight());
		}
		getStyleClass().add("thumbnailwidget");
		getChildren().add(iv);

		this.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				emitThumbPresed();
			}
		});
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
