package com.servifot.lfm.views;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.lfmimporter.ThumbnailWidget;
import com.servifot.lfm.lfmimporter.ThumbnailWidget.ThumbnailWidgetListener;
import com.servifot.lfm.lfmimporter.WifiSDConector;
import com.servifot.lfm.lfmimporter.WifiSDConector.WifiSDConectorListener;
import com.servifot.lfm.utils.FXWorker;
import com.servifot.lfm.utils.FileUtils;
import com.servifot.lfm.utils.LFMUtils;

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

public class MainView extends View implements ThumbnailWidgetListener, WifiSDConectorListener {
	private static final String CSS_NAME = "MainView";
	/** Contenedor de la imagen principal */
	private ImageView m_mainView = null;
	/** Contenedor de miniaturas */
	private TilePane m_thumbsPane = null;
	/** Archivo seleccionado actual */
	private File m_currentFile = null;
	/** Imágenes ya importadas */
	private LinkedHashMap<String, File> m_importedIages = new LinkedHashMap<>();
	/** Indica si se ha encontrado ningún error importanto las imagenes*/
	boolean m_importingError = false;
	
	private static final int IV_WIDTH = LFMImporter.SCREEN_WIDTH-50;
	private static final int IV_HEIGHT = LFMImporter.SCREEN_HEIGHT-(LFMImporter.SCREEN_THUMBS_HEIGHT+50);


	/** Rotación actual de la imagen */
	private int m_rotation = 0;

	public MainView() {
		super();
	}

	@Override
	public Pane createPane() {
		// Pintamos la imagen principal
		m_mainView = new ImageView();
		m_mainView.setFitWidth(IV_WIDTH);
		m_mainView.setFitHeight(IV_HEIGHT);
		m_mainView.setPreserveRatio(true);

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
					e.printStackTrace();
				}
			}
		});
		
		configbtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ConfigView cv = new ConfigView();
				cv.setBackView(MainView.this);
				emitAddView(cv);
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
			File [] sortedFiles = LFMUtils.sortByDate(folderimg.listFiles());
			selectImage(sortedFiles[sortedFiles.length-1]);
			for (File imgFile : sortedFiles) { //TODO ORDER BY DATE
				if (FileUtils.getExtension(imgFile.getAbsolutePath()).toLowerCase().equals("jpg")) {
					addThumb(imgFile, thumbsPane);
				}
			}
		}
	}

	protected void rotate(ImageView iv) {
		if (m_rotation == 0) {
			m_rotation = 90;
			iv.setFitWidth(IV_HEIGHT);
			iv.setFitHeight(IV_WIDTH);
		} else {
			m_rotation = 0;
			iv.setFitWidth(IV_WIDTH);
			iv.setFitHeight(IV_HEIGHT);
		}
		iv.setRotate(m_rotation);
		iv.setImage(new Image("file:///" + m_currentFile.getAbsolutePath()));
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public void onThumbPresed(File file) {
		selectImage(file);
	}

	private void selectImage(File file) {
		if (file.isFile()) {
			try {
				m_mainView.setImage(new Image("file:///" + file.getAbsolutePath()));
			} catch (Exception e) {
				FXWorker.runAsync(FXWorker.JOBTYPE_IMAGEVIEW_SETIMAGE, m_mainView, file.getAbsolutePath());
			}
		}
		m_currentFile = file;
		
	}

	@Override
	public void onSDConnection() {
		importImages();
	}

	private void importImages() {
		
		while (!m_importingError) {
			File sourcefolder = new File(LFMImporter.getConfig().getSourceFolder());
			File destFolder = new File(LFMImporter.getConfig().getCameraFolder());
			
			ArrayList<File> sourceImages = getAllImages(sourcefolder);
			if (sourceImages != null) {
				addImages(sourceImages, destFolder);
			} else {
				System.err.println("No se ha podido listar ninguna imagen");
				m_importingError = false;
			}
		}
	}
	
	private void addImages(ArrayList<File> sourceImages, File destFolder) {
		if (sourceImages.size() < 1) return;
		
		File [] addFiles = LFMUtils.sortByDate(sourceImages);
		selectImage(addFiles[addFiles.length-1]);
		for (File img : addFiles) {
			File destImage = new File(destFolder+"/"+img.getName());
			if( FileUtils.copyFile(img, destImage, true)) {
				addThumb(destImage, m_thumbsPane);
			} else {
				m_importingError = true;
				if (img.exists()) img.delete();
			}
		}
	}

	private ArrayList<File> getAllImages(File sourcefolder) {
		boolean found = false;
		ArrayList<File> jpgs = null;
		for (int i = 0; i < 10 && !found; i++) {
			if (sourcefolder.exists()) {
				found = true;
			} else {
				System.out.println("Esperando la carpeta origen... " + i);
				try {
					Object lock = new Object();
					synchronized(lock) {
						lock.wait(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (found) {
			jpgs = new ArrayList<>();
			addImagesRecursively(sourcefolder, jpgs);
		}
		if (jpgs != null) System.out.println("Encontradas " + jpgs.size() + " nuevas imágenes");
		return jpgs;
	}
	
	private void addImagesRecursively(File f, ArrayList<File> jpgs) {
		if (f.isDirectory()) {
			for (File f2 : f.listFiles()) {
				addImagesRecursively(f2, jpgs);
			}
		} else if (f.isFile() && FileUtils.getExtension(f.getAbsolutePath()).toLowerCase().matches("jpe?g")) {
			if (!m_importedIages.containsKey(f.getName())) {
				jpgs.add(f);
			}
		}
	}
	
	
	private void addThumb(File imgFile, TilePane thumbsPane) {
		m_importedIages.put(imgFile.getName(), imgFile);
		ThumbnailWidget tw = new ThumbnailWidget(imgFile);
		tw.setListener(this);
		System.out.println("Añadir imagen " + imgFile.getName());
		try {
			thumbsPane.getChildren().add(0, tw);
		} catch (Exception e) {
			FXWorker.runSync(FXWorker.JOBTYPE_ADD_TILEPANECHILD, thumbsPane, tw,0);
		}
	}
	
	@Override
	public void onLoad() {
		WifiSDConector wifi = new WifiSDConector();
		wifi.setListener(this);
		wifi.start();
	}
}
