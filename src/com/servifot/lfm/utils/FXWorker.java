package com.servifot.lfm.utils;

import java.util.concurrent.FutureTask;

import com.servifot.lfm.lfmimporter.SearchThumbnailWidget;
import com.servifot.lfm.lfmimporter.ThumbnailWidget;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

/**
 * Gestiona trabajos sobre la interfaz de usuario de JavaFX desde threads secundarios.<br>
 * <br>
 * Desde un thread secundario permite invocar trabajos que se ejecuten en el thread principal de
 * la aplicación FX. En el constructor se determina el tipo de trabajo a realizar, y los objetos
 * con los que trabajar.<br><br>
 * - El tipo de trabajo será una de las constantes <code>FXWorker.JOBTYPE_*</code>.<br><br>
 * - El número, tipo y orden de los objetos con los que trabajar vendrá especificado en la
 * documentación de la constante <code>FXWorker.JOBTYPE_*</code> correspondiente.<br><br>
 * <br>
 * Ejemplo: Añadir un control (<code>Button button</code>) a un panel (<code>HBox hbox</code>)
 *  desde un thread secundario:<br>
 * <pre>
 * Platform.runLater(new FXWorker(FXWorker.JOBTYPE_ADD_CHILD, button, hbox));
 * </pre>
 *
 */
public class FXWorker implements Runnable {

	/* Definición de tipos de trabajo que se puede realizar */

	/**
	 * Añade una miniatura en un TilePane en la posición indicada.<br><br>
	 * <strong>Parámetros:</strong><br>
	 * <code>TilePane</code> Panel donde se insertará el nodo.<br>
	 * <code>ThumbnailWidget</code> Nodo a insertar.<br>
	 * <code>int</code> posición donde va el nodo a insertar.<br>
	 */
	public static final int JOBTYPE_ADD_TILEPANECHILD = 1;

	/**
	 * Añade una miniatura en un TilePane en la posición indicada.<br><br>
	 * <strong>Parámetros:</strong><br>
	 * <code>ImageView</code> Image view al que poner la imagen <br>
	 * <code>String</code> Ruta del archivo con la imagen <br>
	 */
	public static final int JOBTYPE_IMAGEVIEW_SETIMAGE = 2;

	/**
	 * Añade una miniatura en un TilePane en la posición indicada.<br><br>
	 * <strong>Parámetros:</strong><br>
	 * <code>TilePane</code> Panel donde se insertará el nodo.<br>
	 * <code>SearchThumbnailWidget</code> Nodo a insertar.<br>
	 * <code>int</code> posición donde va el nodo a insertar. (si es < 0, lo pone al final)<br>
	 */
	public static final int JOBTYPE_ADD_SEARCHCHILD = 3;




	/* Campos de la clase */

	/** Tipo de trabajo que se realizará */
	private int m_jobType;
	/** Objetos con los que trabajar */
	private Object[] m_objects;

	/**
	 * Constructor. Determina el trabajo a realizar y los objetos con los que trabajar.<br>
	 * <br>
	 * El número y tipo de objetos necesarios depende del trabajo a realizar. Para determinarlos,
	 * consultar la documentación de las constantes <code>FXWorker.JOBTYPE_*</code>.
	 *
	 * @param jobType Tipo de trabajo a realizar (constante <code>FXWorker.JOBTYPE_*</code>)
	 * @param objects Objetos con los que trabajar
	 */
	public FXWorker(int jobType, Object... objects) {
		m_jobType = jobType;
		m_objects = objects;
	}

	@Override
	public void run() {
		try {
			switch (m_jobType) {
				case JOBTYPE_ADD_TILEPANECHILD:
					TilePane pane = (TilePane) m_objects[0];
					ThumbnailWidget node = (ThumbnailWidget) m_objects[1];
					int pos = (int) m_objects[2];
					pane.getChildren().add(pos, node);
					break;

				case JOBTYPE_IMAGEVIEW_SETIMAGE:
					ImageView iv = (ImageView) m_objects[0];
					String path = (String) m_objects[1];
					iv.setImage(new Image("file:///" + path));
					break;

				case JOBTYPE_ADD_SEARCHCHILD:
					TilePane pane2 = (TilePane) m_objects[0];
					SearchThumbnailWidget node2 = (SearchThumbnailWidget) m_objects[1];
					int pos2 = (int) m_objects[2];
					if (pos2 >= 0) {
						pane2.getChildren().add(pos2, node2);
					} else {
						pane2.getChildren().add(node2);
					}
					break;

				default:
					throw new Exception("Identificador de trabajo no definido.");
			}

		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Error procesando trabajo de tipo "+m_jobType+": "+e.getMessage());
		}
	}

	public static void runSync(int jobType, Object... objects) {
		FXWorker worker = new FXWorker(jobType, objects);
		if (Platform.isFxApplicationThread()) {
			worker.run();
		} else {
			FutureTask<Void> future = new FutureTask<Void>(worker, null);
			Platform.runLater(future);
			try {
				future.get();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public static void runAsync(int jobType, Object... objects) {
		FXWorker worker = new FXWorker(jobType, objects);
		Platform.runLater(worker);
	}

	public interface FxWorkerCallable {
		public void onFxWorkerCallback(FxWorkerCallable receiver, Object object);
	}
}
