package com.servifot.lfm.views;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.servifot.lfm.lfmimporter.FolderPrinter;
import com.servifot.lfm.lfmimporter.ImagePrinter;
import com.servifot.lfm.lfmimporter.LFMImporter;
import com.servifot.lfm.lfmimporter.ThumbnailWidget;
import com.servifot.lfm.lfmimporter.ThumbnailWidget.ThumbnailWidgetListener;
import com.servifot.lfm.lfmimporter.WifiSDConector;
import com.servifot.lfm.lfmimporter.LFMImporter.LFMImporterListener;
import com.servifot.lfm.lfmimporter.WifiSDConector.WifiSDConectorListener;
import com.servifot.lfm.utils.FXWorker;
import com.servifot.lfm.utils.FileUtils;
import com.servifot.lfm.utils.ImageOrientation;
import com.servifot.lfm.utils.JPEGMetadata;
import com.servifot.lfm.utils.LFMUtils;
import com.servifot.lfm.views.ConfigView.ConfigViewListener;
import com.servifot.lfm.views.SearchView.SearchViewListener;

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

public class MainView extends View implements ThumbnailWidgetListener, WifiSDConectorListener, LFMImporterListener, ConfigViewListener, SearchViewListener {
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
	private boolean m_importingError = false;
	/** Indica si se debe denter cualquier bucle */
	private boolean m_die = false;

	private static final double IV_WIDTH = LFMImporter.SCREEN_WIDTH-50;
	private static final double IV_HEIGHT = LFMImporter.SCREEN_HEIGHT-(LFMImporter.SCREEN_THUMBS_HEIGHT+50);

	/** Hilo que se conecta al wifi de la SD */
	private WifiSDConector m_wifiConector = null;
	/** Hilo que gestiona la impresión de una carpeta */
	private FolderPrinter m_folderprinter = null;

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
		Button editbtn = new Button("Edit");
		Button printbtn = new Button("Print");
		Button configbtn = new Button("Config");
		Button searchbtn = new Button("Search");

		editbtn.getStyleClass().addAll("mv-btn", "mv-btn-editbtn");
		printbtn.getStyleClass().addAll("mv-btn", "mv-btn-print");
		configbtn.getStyleClass().addAll("mv-btn", "mv-btn-configbtn");
		searchbtn.getStyleClass().addAll("mv-btn", "mv-btn-searchbtn");

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

		printbtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				print(m_currentFile);
			}
		});

		configbtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ConfigView cv = new ConfigView();
				cv.setBackView(MainView.this);
				cv.setListener(MainView.this);
				emitAddView(cv);
			}
		});

		searchbtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				SearchView sv = new SearchView(LFMImporter.getConfig().getCameraFolder());
				sv.setBackView(MainView.this);
				sv.setListener(MainView.this);
				emitAddView(sv);
			}
		});

		HBox firstrowbtnsbox = new HBox(printbtn, editbtn);
		firstrowbtnsbox.getStyleClass().add("mb-btnsBox-first");
		HBox secondrowbtnsbox = new HBox(searchbtn, configbtn);
		secondrowbtnsbox.getStyleClass().add("mb-btnsBox-second");
		VBox btnsBox = new VBox(firstrowbtnsbox, secondrowbtnsbox);
		btnsBox.getStyleClass().add("mv-btnsBox");

		HBox botBox = new HBox(thumbsScrollPane, btnsBox);
		botBox.getStyleClass().add("mv-botbox");

		// Ensamblamos
		BorderPane rootpane = new BorderPane(centerbox);
		rootpane.setBottom(botBox);
		rootpane.getStyleClass().add("mv-rootpane");
		return rootpane;
	}

	/**
	 * Arranca un hilo que imprime una imagen
	 *
	 * @param currentFile
	 */
	protected void print(File currentFile) {
		ImagePrinter ip = new ImagePrinter(currentFile);
		ip.start();

	}

	private void fillThumbsPane(TilePane thumbsPane, String folder) {
		File folderimg = new File(folder);

		if (folderimg.exists() && folderimg.isDirectory()) {
			File [] folderFiles = folderimg.listFiles();
			if (folderFiles.length < 1) return;
			File [] sortedFiles = LFMUtils.sortByDate(folderFiles);
			selectImage(sortedFiles[sortedFiles.length-1]);
			for (File imgFile : sortedFiles) {
				if (FileUtils.getExtension(imgFile.getAbsolutePath()).toLowerCase().equals("jpg")) {
					addThumb(imgFile, thumbsPane);
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
		selectImage(file);
	}

	private void selectImage(File file) {
		if (file.isFile()) {
			Image source = null;
			try {

				JPEGMetadata meta = new JPEGMetadata(file.getAbsolutePath());
				source = new Image("file:///" + file.getAbsolutePath(), true);
				switch (meta.getOrientation().toString()) {
				case ImageOrientation.VALUE_DOWN:
					m_mainView.setRotate(180);
					break;
				case ImageOrientation.VALUE_LEFT:
					m_mainView.setRotate(90);
					break;
				case ImageOrientation.VALUE_RIGHT:
					m_mainView.setRotate(270);
					break;
				default:
					m_mainView.setRotate(0);
					break;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			try {
				m_mainView.setImage(source);
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

		while (!m_importingError && !m_die) {
			File sourcefolder = new File(LFMImporter.getConfig().getSourceFolder());
			File destFolder = new File(LFMImporter.getConfig().getCameraFolder());

			System.out.println("DEBUG: Getting AllImages");
			ArrayList<File> sourceImages = getAllImages(sourcefolder);
			System.out.println("DEBUG: FIN getting allimages");
			if (sourceImages != null) {
				addImages(sourceImages, destFolder);
			} else {
				System.err.println("No se ha podido listar ninguna imagen");
				m_importingError = true;
			}
		}

		System.out.println("No se ha podido acceder a la carpeta. Revisamos la conexión wifi");
		startWifi(true);
	}

	private void addImages(ArrayList<File> sourceImages, File destFolder) {
		if (sourceImages.size() < 1) return;
		System.out.println("DEBUG: Sorting");
		File [] addFiles = LFMUtils.sortByDate(sourceImages);
		System.out.println("DEBUG: FIN Sorting");
		boolean firstAdd = false;
		for (File img : addFiles) {
			File destImage = new File(destFolder+"/"+img.getName());
			System.out.println("DEBUG: Copy file");
			//if( FileUtils.copyFile(img, destImage, true))
			try {
				Files.copy(img.toPath(), destImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("DEBUG: FIN Copy file");
				if (!firstAdd) {
					System.out.println("DEBUG: Select Image");
					selectImage(destImage);
					System.out.println("DEBUG: FIN Select Image");
					firstAdd = true;
				}
				addThumb(destImage, m_thumbsPane);
			} catch (Exception e) {
				m_importingError = true;
				if (img.exists()) img.delete();
			}
		}
	}

	/**
	 * Devuelve todas las imágenes que nose han descargado de la tarjeta.
	 *
	 * @param sourcefolder Carpeta de origen
	 * @return Imágenes nuevas no descargadas o null si tiene algún problema
	 */
	private ArrayList<File> getAllImages(File sourcefolder) {
		boolean found = false;
		ArrayList<File> jpgs = null;
		for (int i = 0; i < 10 && !found; i++) {
			System.out.println("DEBUG: Exists");
			if (sourcefolder.exists()) {
				System.out.println("DEBUG: FIN Exists");
				found = true;
			} else {
				System.out.println("DEBUG: FIN Exists");
				System.out.println("Esperando la carpeta origen... " + i);
				try {
					Object lock = new Object();
					synchronized(lock) {
						lock.wait(1500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Fin de la espera " + i);
			}
		}

		if (found) {
			jpgs = new ArrayList<>();
			addImagesRecursively(sourcefolder.listFiles(), jpgs);
		} else {
			return null;
		}
		if (jpgs != null) {
			System.out.println("Encontradas " + jpgs.size() + " nuevas imágenes");
			if (jpgs.size() < 1) {
				Object lock = new Object();
				synchronized(lock) {
					try {
						lock.wait(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return jpgs;
	}

	private void addImagesRecursively(File[] f, ArrayList<File> jpgs) {
		if (f == null) {
			System.err.println("f es null");
			//m_importingError = true;
			return;
		}
		for (File file : f) {
			if (file.isDirectory()) {
				addImagesRecursively(file.listFiles(), jpgs);
			} else if (file.isFile() && FileUtils.getExtension(file.getAbsolutePath()).toLowerCase().matches("jpe?g")) {
				if (!m_importedIages.containsKey(file.getName())) {
					jpgs.add(file);
				}
			}
		}
	}


	private void addThumb(File imgFile, TilePane thumbsPane) {
		m_importedIages.put(imgFile.getName(), imgFile);
		System.out.println("DEBUG: Creating ThumbnailW");
		ThumbnailWidget tw = new ThumbnailWidget(imgFile);
		System.out.println("DEBUG: FIN Creating ThumbnailW");
		tw.setListener(this);
		System.out.println("Añadir imagen " + imgFile.getName());
		try {
			thumbsPane.getChildren().add(0, tw);
		} catch (Exception e) {
			FXWorker.runSync(FXWorker.JOBTYPE_ADD_TILEPANECHILD, thumbsPane, tw,0);
		}
	}

	/**
	 * Deten el hilo que se conecta al wifi (si estuviese activo) y arranca uno nuevo
	 */
	public void startWifi(boolean unableaccess) {
		m_importingError = false;
		m_die = false;
		if (m_wifiConector != null && !m_wifiConector.isKilled()) {
			m_wifiConector.kill();
		}
		m_wifiConector = new WifiSDConector();
		m_wifiConector.setListener(this);
		m_wifiConector.setUnableAccess(unableaccess);
		m_wifiConector.start();
	}
	/** Detiene el buscadoor del wifi */
	public void stopWifi() {
		if (m_wifiConector != null) m_wifiConector.kill();
		m_wifiConector = null;
	}

	/** Arranca la impresión */
	private void startPrinter() {
		 if (m_folderprinter != null) {
			 m_folderprinter.kill();
		 } else if (m_folderprinter == null || m_folderprinter.isDie()) {
			 m_folderprinter = new FolderPrinter(new File(LFMImporter.getConfig().getPrinterFolder()));
		 }
		 m_folderprinter.start();
	}

	private void stopPrinter() {
		if (m_folderprinter != null) {
			m_folderprinter.kill();
		}
		m_folderprinter = null;
	}

	@Override
	public void onLoad() {
		startWifi(false);
		startPrinter();
	}



	@Override
	public void onStop() {
		m_die = true;
		stopWifi();
		stopPrinter();
	}

	@Override
	public void onConfigApply() {
		// Reiniciamos la impresión para se ejecute con la nueva configuración
		stopPrinter();
		startPrinter();

	}

	@Override
	public void onSearchThumbSelect(File f) {
		selectImage(f);
	}

}
