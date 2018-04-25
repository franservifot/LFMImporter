package com.servifot.lfm.utils;

import java.awt.color.ICC_Profile;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Extracción de metadatos de archivos de imagen.<br>
 * <br>
 * Ejemplo de uso:<br>
 * <pre>
 * <code>
 *     File imageFile = new File("/path/to/image.jpg");
 *     ImageMetadata md = ImageMetadata.getInstance(imageFile);
 *     System.out.println("  > Type: " + md.getImageType());
 *     System.out.println("  > Size: " + md.getWidth() + " x " + md.getHeight());
 *     ICC_Profile profile = md.getIccProfile();
 *     if (profile != null) {
 *         System.out.println("  > ICC : " + ImageUtils.getProfileDescription(profile));
 *     }
 *     System.out.println("  > Orientation: " + md.getOrientation());
 *     System.out.println("  > Transparent: " + md.isTransparent());
 *     System.out.println("  > Greyscale  : " + md.isGreyscale());
 *     System.out.println("  > RGB        : " + md.isRGB());
 *     System.out.println("  > Indexed    : " + md.isIndexed());
 *     System.out.println("  > Miniatura  : " + md.hasThumbnail());
 *     System.out.println();
 * </code>
 * </pre>
 */
public class ImageMetadata {

	/** Bytes de cabecera de ficheros PNG. */
	private static final int[] PNG_SIGNATURE = {137, 80, 78, 71, 13, 10, 26, 10};
	/** Bytes de cabecera de ficheros JPEG. */
	private static final int[] JPEG_SIGNATURE = {0xFF, 0xD8};
	/** Bytes de cabecera de ficheros BMP. */
	private static final int[] BMP_SIGNATURE = {0x42, 0x4D};
	/** Bytes de cabecera de ficheros TIFF (little-endian). */
	private static final int[] TIFF_LE_SIGNATURE = {0x49, 0x49, 0x2A, 0x00};
	/** Bytes de cabecera de ficheros TIFF (big-endian). */
	private static final int[] TIFF_BE_SIGNATURE = {0x4D, 0x4D, 0x00, 0x2A};
	

	/** Lista de cabeceras de tipos de archivo conocidos. */
	private static HashMap<ImageType, int[]> s_signatures = new HashMap<>();	
	static {
		s_signatures.put(ImageType.PNG, PNG_SIGNATURE);
		s_signatures.put(ImageType.JPEG, JPEG_SIGNATURE);		
		s_signatures.put(ImageType.BMP, BMP_SIGNATURE);		
		s_signatures.put(ImageType.TIFF_LE, TIFF_LE_SIGNATURE);		
		s_signatures.put(ImageType.TIFF_BE, TIFF_BE_SIGNATURE);		
	}
	
	
	/** Tipo de imagen detectado. */
	private ImageType m_imageType = ImageType.UNKNOWN;
	/** Ancho de la imagen (px). */
	private int m_width;
	/** Alto de la imagen (px). */
	private int m_height;
	
	/**
	 * Genera una instancia del objeto apropiado para analizar los
	 * metadatos de un archivo de imagen.
	 * @param filename Ruta al archivo de imagen.
	 * @return Objeto de metadatos.
	 */
	public static ImageMetadata getInstance(String filename) {
		return getInstance(new File(filename));
	}

	/**
	 * Genera una instancia del objeto apropiado para analizar los
	 * metadatos de un archivo de imagen.
	 * @param filename Ruta al archivo de imagen.
	 * @return Objeto de metadatos.
	 */
	public static ImageMetadata getInstance(File file) {
		ImageMetadata md = new ImageMetadata();
		md.setImageType(getImageType(file));
		switch (md.getImageType()) {
			case JPEG:
				JPEGMetadata jpeg = new JPEGMetadata();
				if (jpeg.load(file)) {
					md = jpeg;
				}
				break;
				
			case PNG:
				PNGMetadata png = new PNGMetadata();
				if (png.load(file)) {
					md = png;
				}
				break;
				
			default:
				md.loadPixelSize(file);
				break;				
		}
		
		return md;
	}
		
	
	/**
	 * Detecta el tipo de imagen de un fichero.
	 * @param file Fichero a analizar.
	 * @return Tipo de imagen detectado, o <code>ImageType.UNKNOWN</code> si no se detecta.
	 */
	public static ImageType getImageType(File file) {
		ImageType imageType = ImageType.UNKNOWN;
		ArrayList<ImageType> discardedTypes = new ArrayList<>();
		try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
			int pos = 0;
			int b;
			while (discardedTypes.size() < s_signatures.size() && imageType == ImageType.UNKNOWN && (b = is.read()) != -1) {
				for (ImageType type : s_signatures.keySet()) {
					if (!discardedTypes.contains(type)) {
						if (s_signatures.get(type)[pos] == b) {
							if ((pos+1) == s_signatures.get(type).length) {
								imageType = type;
							}
						} else {
							discardedTypes.add(type);
						}						
					}
				}
				pos++;
			}
		} catch (Exception e) {
			System.err.println("No se puede leer el archivo: " + file.getAbsolutePath() + ": " + e.getMessage());
		}
		return imageType;
	}
	

	/**
	 * Devuelve el perfil de color ICC incrustado en un archivo de imagen
	 * @param imagePath Ruta al archivo de imagen.
	 * @return Perfil ICC incrustado, o <code>null</code> si no se ha detectado. 
	 */
	public static ICC_Profile getIccProfile(String imagePath) {
		return getIccProfile(new File(imagePath));
	}
	

	/**
	 * Devuelve el perfil de color ICC incrustado en un archivo de imagen
	 * @param imageFile Archivo de imagen.
	 * @return Perfil ICC incrustado, o <code>null</code> si no se ha detectado. 
	 */
	public static ICC_Profile getIccProfile(File imageFile) {
		ImageMetadata md = getInstance(imageFile);
		return md.getIccProfile();
	}
	
	
	/**
	 * Devuelve el tipo de imagen detectado.
	 * @return Tipo de imagen detectado.
	 */
	public ImageType getImageType() {
		return m_imageType;
	}

	
	/**
	 * Establece el tipo de imagen detectado.
	 * @param imageType Tipo de imagen detectado.
	 */
	private void setImageType(ImageType imageType) {
		m_imageType = imageType;
	}

	
	/**
	 * Devuelve la anchura de la imagen.
	 * @return Anchura de la imagen (px), o <code>0</code> si no se ha detectado.
	 */
	public int getWidth() {
		return m_width;
	}

	
	/**
	 * Devuelve la altura de la imagen.
	 * @return Altura de la imagen (px), o <code>0</code> si no se ha detectado.
	 */
	public int getHeight() {
		return m_height;
	}


	/**
	 * Detecta las dimensiones en píxeles de un archivo de imagen. 
	 * @param imageFile Fichero de imagen a analizar.
	 */
	protected void loadPixelSize(File imageFile) {
		ImageInputStream is = null;
		ImageReader reader = null;
		try {
			is = ImageIO.createImageInputStream(imageFile);
		    Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
		    if (readers.hasNext()) {
		        reader = readers.next();
	            reader.setInput(is);
		        m_width = reader.getWidth(0);
		        m_height = reader.getHeight(0);
	            reader.dispose();
		    }
		    is.close();
		    
		} catch (Exception e) {
			if (reader != null) try {reader.dispose();} catch (Exception ex) {/* Ignore */}
			if (is != null) try {is.close();} catch (Exception ex) {/* Ignore */}
		}		
	}
	
	
	/**
	 * Devuelve profundidad de color en bits de la imagen.
	 * @return Número de bits de profundidad de color, o <code>0</code> si no se ha detectado.
	 */
	public int getBitDepth() {
		return 0;
	}

	
	/** 
	 * Indica si la imagen está en modo escala de grises.
	 * @return <code>true</code> si la imagen está en modo escala de grises, <code>false</code> en caso
	 * contrario.
	 */
	public boolean isGreyscale() {
		return false;
	}
	

	/** 
	 * Indica si la imagen está en modo RGB.
	 * @return <code>true</code> si la imagen está en modo RGB, <code>false</code> en caso
	 * contrario.
	 */
	public boolean isRGB() {
		return false;
	}

	
	/** 
	 * Indica si la imagen está en modo de paleta indexada.
	 * @return <code>true</code> si la imagen está en modo de paleta indexada, <code>false</code> en caso
	 * contrario.
	 */
	public boolean isIndexed() {
		return false;
	}

	
	/** 
	 * Indica si la imagen está en modo CMYK
	 * @return <code>true</code> si la imagen está en modo CMYK, <code>false</code> en caso
	 * contrario.
	 */
	public boolean isCMYK() {
		return false;
	}

	
	/** 
	 * Indica si la imagen tiene transparencia.
	 * @return <code>true</code> si la imagen tiene transparencia, <code>false</code> en caso
	 * contrario.
	 */
	public boolean isTransparent() {
		return false;
	}
	
	/**
	 * Devuelve el perfil de color ICC incrustado en la imagen.
	 * @return Perfil de color ICC incrustado en la imagen, o <code>null</code> si no se ha podido
	 * determinar.
	 */
	public ICC_Profile getIccProfile() {
		return null;
	}
	
	
	/**
	 * Devuelve la orientación de los píxeles de la imagen.
	 * @return Orientación de los píxeles de la imagen.
	 */
	public ImageOrientation getOrientation() {
		return ImageOrientation.TOP;
	}
	
	
	/**
	 * Indica si se ha detectado una miniatura incrustada.
	 * @return <code>true</code> si se ha detectado miniatura incrustada, <code>false</code> si no.
	 */
	public boolean hasThumbnail() {
		return false;
	}
	
	
	/**
	 * Genera un stream para acceder a la miniatura incrustada.
	 * @return Stream con los datos de la minatura, o <code>null</code> si no se ha detectado.
	 */
	public ByteArrayInputStream getThumbnailAsInputStream() {
		return null;
	}

	
	/**
	 * Devuelve la resolución horizontal de la imagen.
	 * @return Resolución horizontal de la imagen (dpi), o <code>0</code> si no se ha detectado.
	 */
	public double getDpiX() {
		return 0;
	}

	
	/**
	 * Devuelve la resolución vertical de la imagen.
	 * @return Resolución vertical de la imagen (dpi), o <code>0</code> si no se ha detectado.
	 */
	public double getDpiY() {
		return 0;
	}
	
}
