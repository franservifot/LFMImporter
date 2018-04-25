package com.servifot.lfm.utils;

import java.awt.Dimension;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

/**
 * Metadatos de archivos JPEG.<br>
 */
public class JPEGMetadata extends ImageMetadata {

	/** Identificador EXIF d'orientació normal */
	private static final int ORIENTATION_UP = 1;
	/** Identificador EXIF d'orientació rotada a l'esquerra */
	private static final int ORIENTATION_LEFT = 6;
	/** Identificador EXIF d'orientació rotada a la dreta */
	private static final int ORIENTATION_RIGHT = 8;
	/** Identificador EXIF d'orientació rotada 180º */
	private static final int ORIENTATION_DOWN = 3;
		
	/** Nom del fitxer JPEG */
	private String m_filename;
	
	/** Metadata: Orientació de la imatge */
	private long m_orientation = ORIENTATION_UP;
	/** Metadata: Espai de color EXIF */
	private long m_exifColorSpace = 0;
	/** Metadata: offset del thumbnail dins del fitxer JPEG */ 
	private long m_thumbPos = 0;
	/** Metadata: Mida del thumbnail */
	private long m_thumbSize = 0;
	/** Metadata: offsets dels fragments del perfil ICC dins del fitxer JPEG */ 
	private Vector<Long> m_iccPositions = null;
	/** Metadata: mides dels fragments del perfil ICC dins del fitxer JPEG */ 
	private Vector<Long> m_iccSizes = null;
	/** Indica si la imatge conté un marcador JFIF */
	private boolean m_isJFIF = false;
	/** Indica si la imatge conté un marcador EXIF */
	private boolean m_isEXIF = false;
	/** Número de canals de color. */
	private int m_numBands;
	/** Resolució d'impressió horitzontal (ppp). */
	private double m_dpiX;
	/** Resolució d'impressió vertical (ppp). */
	private double m_dpiY;

	/* Constants JPEG */
	private static final byte JPEG_MARKER_APP0  = (byte)0xE0;
	private static final byte JPEG_MARKER_APP1  = (byte)0xE1;
	private static final byte JPEG_MARKER_APP2  = (byte)0xE2;
	private static final byte JPEG_MARKER_APP3  = (byte)0xE3;
	private static final byte JPEG_MARKER_APP4  = (byte)0xE4;
	private static final byte JPEG_MARKER_APP5  = (byte)0xE5;
	private static final byte JPEG_MARKER_APP6  = (byte)0xE6;
	private static final byte JPEG_MARKER_APP7  = (byte)0xE7;
	private static final byte JPEG_MARKER_APP8  = (byte)0xE8;
	private static final byte JPEG_MARKER_APP9  = (byte)0xE9;
	private static final byte JPEG_MARKER_APP10 = (byte)0xEA;
	private static final byte JPEG_MARKER_APP11 = (byte)0xEB;
	private static final byte JPEG_MARKER_APP12 = (byte)0xEC;
	private static final byte JPEG_MARKER_APP13 = (byte)0xED;
	private static final byte JPEG_MARKER_APP14 = (byte)0xEE;
	private static final byte JPEG_MARKER_APP15 = (byte)0xEF;	
	private static final byte SOI[] = {(byte) 0xFF,(byte) 0xD8};
	private static final byte ICC_TAG[] = {'I','C','C','_','P','R','O','F','I','L','E',0};
	private static final byte EXIF_TAG[] = {'E','x','i','f',0,0};
	private static final byte JFIF_TAG[] = {'J','F','I','F',0};
	//private static final byte JFXX_TAG[] = {'J','F','X','X',0};
		
	/**
	 * Crea el objeto listo para analizar los metadatos de un archivo JPEG.
	 * 
	 * @param absolutePath Ruta del archivo JPEG a analizar.
	 */
	public JPEGMetadata() {
		m_filename = null;
	}
	
	
	/**
	 * Crea el objeto listo para analizar los metadatos de un archivo JPEG.
	 * 
	 * @param absolutePath Ruta del archivo JPEG a analizar.
	 * 
	 * @throws IOException si el fichero no se puede cargar como JPEG
	 */
	public JPEGMetadata(String absolutePath) throws IOException {
		if (!load( new File(absolutePath) ))  {
			throw new IOException("The file '"+absolutePath+"' couldn't be load");
		}
	}


	/**
	 * Carga los metadatos de un archivo JPEG.
	 * @param jpegFile Archivo JPEG a analizar.
	 * @return <code>true</code> si se procesa correctamente, <code>false</code> si se produce algún error.
	 */
	public boolean load(File jpegFile) {
		m_filename = jpegFile.getAbsolutePath();
		boolean result = readMetadata();
		if (!result) {
			System.err.println("Error lectura metadatos JPEG: " + jpegFile.getAbsolutePath());
		} else {
			loadPixelSize(jpegFile);
			loadNumBands(m_filename);
			if (m_dpiX == 0 || m_dpiY == 0) {
				loadDpi(m_filename);
			}
		}
		return result;
	}

	
	/**
	 * Guarda la miniatura del JPEG en un fitxer
	 * @param thumbnail Path al fitxer a generar amb la miniatura
	 * @return true si tenim èxit, false en cas contrari
	 */
	public boolean saveThumbnail(String thumbnail) {
		if (m_thumbPos!=0 && m_thumbSize!=0) {
			try {
				FileInputStream fis = new FileInputStream(m_filename);
				FileOutputStream fos = new FileOutputStream(thumbnail);
				fis.getChannel().transferTo(m_thumbPos, m_thumbSize, fos.getChannel());
				fis.close();
				fos.close();
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		} else {
			return false;
		}
		
		return true;
	}
	
	@Override
	public ByteArrayInputStream getThumbnailAsInputStream() {
		if (m_thumbPos!=0 && m_thumbSize!=0) {
			byte[] buffer = new byte[(int)m_thumbSize];
			try {
				FileInputStream fis = new FileInputStream(m_filename);
				fis.getChannel().position(m_thumbPos);
				fis.read(buffer);
				fis.close();
				return new ByteArrayInputStream(buffer);
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	
	/**
	 * Obté l'imatge de la miniatura.
	 * @return un BufferedImage amb la miniatura, o null si no és possible carregar-la
	 */
	public BufferedImage getThumbnailAsImage() {
		try {
			return ImageIO.read( getThumbnailAsInputStream() );
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean hasThumbnail() {
		return (m_thumbPos!=0 && m_thumbSize!=0);
	}
	
	
	/**
	 * Llig la informació (metadata) del fitxer JPEG i la guarda en les variables corresponents
	 * 
	 * @return <code>true</code> si se procesa correctamente, <code>false</code> si se produce algún error.
	 */
	private boolean readMetadata() {
		// Buffer per a lectura del fitxer
		byte[] buffer = new byte[256];

		// Obrim el fitxer per a lectura
		File file = new File(m_filename);
		if (!file.canRead()) {
			return false;
		}

		try {
			FileInputStream fis = new FileInputStream(m_filename);
			
			// Llegim marcador d'inici
			if (fis.read(buffer,0,2) != 2) {
				fis.close();
				return false;
			}
			if (memcmp(buffer,SOI,2) != 0) {
				fis.close();
				return false;
			}

			// Detectem marcadors
			long markerLength;
			long markerStart;
			boolean finished = false;
			m_iccPositions = new Vector<Long>();
			m_iccSizes = new Vector<Long>();
			while (!finished) {
				if (fis.read(buffer,0,2) != 2) {
					fis.close();
					return false;
				}
				if (buffer[0] != (byte)0xFF) {
					fis.close();
					return false;
				}
				switch (buffer[1]) {
					case (byte)0xFF:
						break;

					case JPEG_MARKER_APP0:
						markerStart = fis.getChannel().position();
						if (fis.read(buffer,0,2) != 2) {
							fis.close();
							return false;
						}
						markerLength = (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
						if (fis.read(buffer,0,5) != 5) {
							fis.close();
							return false;
						}
						if (memcmp(buffer, JFIF_TAG, 5) == 0) {
							if (!m_isEXIF) {
								// Si hem detectat abans dades EXIF, no és un format JFIF correcte.
								m_isJFIF = true;
							}
						}
						fis.getChannel().position(markerStart+markerLength);
						break;
						
					case JPEG_MARKER_APP1:
						markerStart = fis.getChannel().position();
						if (fis.read(buffer,0,2) != 2) {
							fis.close();
							return false;
						}
						markerLength = (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
						if (fis.read(buffer,0,6) != 6) {
							fis.close();
							return false;
						}
						if (memcmp(buffer,EXIF_TAG,6) == 0) {
							long exifStart;
							exifStart = fis.getChannel().position();
							m_isEXIF = true;
							// Llegim capçalera TIFF
							if (fis.read(buffer,0,8) != 8) {
								fis.close();
								return false;
							}
							boolean littleEndian = false;
							if (buffer[0] == (byte) 0x49) {
								littleEndian = true;
							} else {
								littleEndian = false;
							}
							long offsetIFD0 =  exifReadLong(subArray(buffer,4,8),littleEndian);

							// Llegim paràmetres TIFF (IFD0)
							fis.getChannel().position(exifStart+offsetIFD0);
							if (fis.read(buffer,0,2) != 2) {
								fis.close();
								return false;
							}
							long IFD0count = exifReadWord(buffer,littleEndian);
							long exifIFDoffset = 0;
							for (int i=0; i<IFD0count; i++) {
								if (fis.read(buffer,0,12) != 12) {
									fis.close();
									return false;
								}
								long exifTag = exifReadWord(subArray(buffer,0,2),littleEndian);
								long exifType = exifReadWord(subArray(buffer,2,4),littleEndian);
								long exifCount = exifReadLong(subArray(buffer,4,8),littleEndian);
								long exifValueOffset = exifReadValue(subArray(buffer,8,12),littleEndian,exifType,exifCount);
								if (exifTag == 0x0112) {
									m_orientation = exifValueOffset;
									
								} else if (exifTag == 0x011A || exifTag == 0x011B) {
									// X Resolution / Y Resolution
									long currentPos = fis.getChannel().position();
									fis.getChannel().position(exifStart + exifValueOffset);
									fis.read(buffer, 0, 8);
									long numerator = exifReadLong(subArray(buffer, 0, 4), littleEndian);
									long denominator = exifReadLong(subArray(buffer, 4, 8), littleEndian);
									double dpi = (double)numerator/(double)denominator;
									if (exifTag == 0x011A) {
										m_dpiX = dpi;
									} else {
										m_dpiY = dpi;
									}
									fis.getChannel().position(currentPos);
									
								} else if (exifTag == 0x8769) {
									// Exif IFD
									exifIFDoffset = exifValueOffset;
								}
							}
							// Llegim offset a IFD1
							if (fis.read(buffer,0,4) != 4) {
								fis.close();
								return false;
							}
							long IFD1offset = 0;
							IFD1offset = exifReadLong(buffer,littleEndian);

							// Llegim paràmetres EXIF
							if (exifIFDoffset != 0) {
								fis.getChannel().position(exifStart+exifIFDoffset);
								if (fis.read(buffer,0,2) != 2) {
									fis.close();
									return false;
								}
								long ExifIFDcount = exifReadWord(buffer,littleEndian);
								for (int i=0; i<ExifIFDcount; i++) {
									if (fis.read(buffer,0,12) != 12) {
										fis.close();
										return false;
									}
									long exifTag = exifReadWord(buffer,littleEndian);
									long exifType = exifReadWord(subArray(buffer,2,4),littleEndian);
									long exifCount = exifReadLong(subArray(buffer,4,8),littleEndian);
									long exifValueOffset = exifReadValue(subArray(buffer,8,12),littleEndian,exifType,exifCount);
									if (exifTag == 0xA001) { // ColorSpace
										m_exifColorSpace = exifValueOffset;
									}
								}
							}

							// Llegim informació del thumbnail (IFD1)
							if (IFD1offset != 0) {
								fis.getChannel().position(exifStart+IFD1offset);
								if (fis.read(buffer,0,2) != 2) {
									fis.close();
									return false;
								}
								long ExifIFD1count = exifReadWord(buffer,littleEndian);
								for (int i=0; i<ExifIFD1count; i++) {
									if (fis.read(buffer,0,12) != 12) {
										fis.close();
										return false;
									}
									long exifTag = exifReadWord(buffer,littleEndian);
									long exifType = exifReadWord(subArray(buffer,2,4),littleEndian);
									long exifCount = exifReadLong(subArray(buffer,4,8),littleEndian);
									long exifValueOffset = exifReadValue(subArray(buffer,8,12),littleEndian,exifType,exifCount);
									if (exifTag == 0x0201) { // JPEGInterchangeFormat (thumb offset)
										m_thumbPos = exifStart+exifValueOffset;
									} else if (exifTag == 0x0202) { // JPEGInterchangeFormatLength (thumb length)
										m_thumbSize = exifValueOffset;
									}
								}
							}
						}
						fis.getChannel().position(markerStart+markerLength);
						break;

					case JPEG_MARKER_APP2:
						markerStart = fis.getChannel().position();
						if (fis.read(buffer,0,2) != 2) {
							fis.close();
							return false;
						}
						markerLength = (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
						if (fis.read(buffer,0,12) != 12) {
							fis.close();
							return false;
						}
						if (memcmp(buffer,ICC_TAG,12) == 0) {
							if (fis.read(buffer,0,2) != 2) {
								fis.close();
								return false;
							}
							long chunkSize = markerLength-2-12-2;
							m_iccPositions.add(fis.getChannel().position());
							m_iccSizes.add(chunkSize);
						}
						fis.getChannel().position(markerStart+markerLength);
						break;

						
					case JPEG_MARKER_APP3:
					case JPEG_MARKER_APP4:
					case JPEG_MARKER_APP5:
					case JPEG_MARKER_APP6:
					case JPEG_MARKER_APP7:
					case JPEG_MARKER_APP8:
					case JPEG_MARKER_APP9:
					case JPEG_MARKER_APP10:
					case JPEG_MARKER_APP11:
					case JPEG_MARKER_APP12:
					case JPEG_MARKER_APP13:
					case JPEG_MARKER_APP14:
					case JPEG_MARKER_APP15:
						if (fis.read(buffer,0,2) != 2) {
							fis.close();
							return false;
						}
						markerLength = (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
						fis.skip(markerLength-2);
						break;

					default:
						finished = true;
						break;
				}
			}
			
			// Tanquem el fitxer
			fis.close();
						
		} catch (Exception e) {
			System.err.println("JPEG Metadata read error: "+e.getMessage());
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Devuelve el perfil de color incrustado en los metadatos de la imagen JPEG.
	 * 
	 * @return Perfil de color incrustado, o <code>null</code> si no se ha detectado.
	 */
	@Override
	public ICC_Profile getIccProfile() {
		
		// Comprobamos inicializaciones correctas
		if ((m_iccPositions == null) || (m_iccSizes == null)) {
			System.err.println("Metadatos no cargados");
			return null;
		}
		if ((m_iccPositions.size() != m_iccSizes.size()) || (m_iccPositions.size() == 0)) {
			return null;
		}
		
		// Preparamos buffer para el perfil de color
		long profileSize = 0;
		for (long chunkSize : m_iccSizes) {
			profileSize += chunkSize;
		}
		byte[] profileData = new byte[(int)profileSize];

		// Cargamos segmentos del perfil en el buffer
		FileInputStream is = null;
		int currentPosition = 0;
		try {
			is = new FileInputStream(new File(m_filename));
			for (int i=0; i<m_iccPositions.size(); i++) {
				long chunkPosition = m_iccPositions.get(i);
				long chunkSize = m_iccSizes.get(i);
				is.getChannel().position(chunkPosition);
				int readBytes = is.read(profileData,currentPosition,(int)chunkSize);
				if (readBytes != chunkSize) {
					System.err.println("No se pueden leer los bytes del segmento "+i+" del perfil ICC.");
					is.close();
					return null;
				}
				currentPosition += readBytes;
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {is.close();} catch (Exception ex) {/*Ignore*/}
			return null;
		}
		
		// Cargamos perfil a partir del buffer
		ICC_Profile profile = null;
		try {
			profile = ICC_Profile.getInstance(profileData);
		} catch (IllegalArgumentException e) {
			System.err.println("Perfil de color incrustado no válido.");
			return null;
		}
		
		return profile;
	}
	
	/**
	 * Compara dos arrays de bytes.
	 * 
	 * @param src Array 1
	 * @param dst Array 2
	 * @param length Longitud de bytes a comparar
	 * @return 0 si són iguals, 1 si són diferents
	 */
	private static int memcmp(byte[] src, byte[] dst, int length) {
		boolean result = false;
		if ((src.length >= length) && (dst.length >= length)) {
			result = true;
			try {
				for (int i=0; i<length && result; i++) {
					if (src[i] != dst[i]) result = false;
				}
			} catch (IndexOutOfBoundsException e) {
				result = false;
			}			
		}
		return (result ? 0 : 1);
	}
	
	/**
	 * Funció auxiliar per a llegir dades Exif
	 * 
	 * @param buffer
	 * @param littleEndian
	 * @return
	 */
	long exifReadWord(byte[] buffer, boolean littleEndian) {
		if (littleEndian) {
			return (0xFF & buffer[1])*256 + (0xFF & buffer[0]);
		} else {
			return (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
		}
	}

	/**
	 * Funció auxiliar per a llegir dades Exif
	 * 
	 * @param buffer
	 * @param littleEndian
	 * @return
	 */
	long exifReadLong(byte[] buffer, boolean littleEndian) {
		if (littleEndian) {
			return ((0xFF & buffer[3]) << 24) | ((0xFF & buffer[2]) << 16) | ((0xFF & buffer[1]) << 8) | (0xFF & buffer[0]);
		} else {
			return ((0xFF & buffer[0]) << 24) | ((0xFF & buffer[1]) << 16) | ((0xFF & buffer[2]) << 8) | (0xFF & buffer[3]);
		}

	}

	/**
	 * Funció auxiliar per a llegir dades Exif
	 * 
	 * @param buffer
	 * @param littleEndian
	 * @return
	 */
	long exifReadValue(byte[] buffer, boolean littleEndian, long typeL, long sizeL) {

		long value = 0;
		int type = (int) typeL;
		int size = (int) sizeL;

		// Calculem dimensions de les dades en bytes
		int totalsize;
		switch (type) {
			case 1:	//BYTE
			case 2: //ASCII
			case 7: //UNDEFINED
				totalsize = size;
				break;

			case 3: // SHORT
				totalsize = 2*size;
				break;

			default:
				totalsize = 4;
				break;
		}
		if (totalsize >=4) totalsize=4;

		// Obtenim les dades sobre els bytes que correspon
		switch (totalsize) {
			case 1:
				value = (0xFF & buffer[0]);
				break;

			case 2:
				value = exifReadWord(buffer,littleEndian);
				break;

			case 3:
				if (littleEndian) {
					value = ((0xFF & buffer[2]) << 16) | ((0xFF & buffer[1]) << 8) | (0xFF & buffer[0]);
				} else {
					value = ((0xFF & buffer[0]) << 16) | ((0xFF & buffer[1]) << 8) | (0xFF & buffer[2]);
				}
				break;

			case 4:
				value = exifReadLong(buffer,littleEndian);
				break;
		}

		return value;
	}

	/**
	 * Genera un subarray a partir d'un array de bytes
	 * 
	 * @param buffer Array de bytes original
	 * @param start Índex a partir del qual s'extreuen les dades
	 * @param end Índex fins al qual s'extreuen les dades
	 * @return Nou array de bytes
	 */
	private byte[] subArray(byte[] buffer,int start, int end) {
		int newLength = end-start;
		byte[] newBuffer = new byte[newLength];
		for (int i=0; i<newLength; i++) {
			if (start+i < buffer.length) {
				newBuffer[i] = buffer[start+i];
			} else {
				newBuffer[i] = 0;
			}
		}
		return newBuffer;
	}
	

	@Override
	public ImageOrientation getOrientation() {
		if (m_orientation == ORIENTATION_LEFT) {
			return ImageOrientation.LEFT;
		} else if (m_orientation == ORIENTATION_RIGHT) {
			return ImageOrientation.RIGHT;
		} else if (m_orientation == ORIENTATION_DOWN) {
			return ImageOrientation.DOWN;
		}
		return ImageOrientation.TOP;
	}
	
	public long getOrientationValue() {
		return m_orientation;
	}
	
	/**
	 * Espai de color Exif
	 * @return
	 */
	public long getExifColorSpace() {
		return m_exifColorSpace;
	}

	/** 
	 * Indica si la imatge conté un marcador JFIF correcte.
	 * 
	 * @return <code>true</code> si la imatge conté metadades JFIF correctes, <code>false</code> en cas contrari
	 */
	public boolean isJFIF() {
		return m_isJFIF;
	}
	
	/** 
	 * Indica si la imatge conté un marcador EXIF
	 * 
	 * @return <code>true</code> si la imatge conté metadades EXIF, <code>false</code> en cas contrari
	 */
	public boolean isEXIF() {
		return m_isEXIF;
	}
	
	
	/**
	 * Devuelve las dimensiones en píxeles de un archivo de imagen, sin necesidad de cargar
	 * la imagen completamente en memoria.
	 * 
	 * @param jpegFile Fichero de imagen a analizar
	 * @return Dimensiones de la imagen. Si no es posible extraer la información del fichero, devuelve (0,0)
	 */
	public static Dimension getPixelSize(File jpegFile) {
		Dimension dimension = null;
		ImageInputStream is = null;
		ImageReader reader = null;
		
		try {
			is = ImageIO.createImageInputStream(jpegFile);
		    Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
		    if (readers.hasNext()) {
		        reader = readers.next();
	            reader.setInput(is);
		        dimension = new Dimension(reader.getWidth(0), reader.getHeight(0));
	            reader.dispose();
		    }
		    is.close();
		    
		} catch (Exception e) {
			if (reader != null) try {reader.dispose();} catch (Exception ex) {/* Ignore */}
			if (is != null) try {is.close();} catch (Exception ex) {/* Ignore */}
		}
		
		return (dimension == null ? new Dimension(0,0) : dimension);
	}
	

	/**
	 * Registra la resolució d'impressió continguda al fitxer JPEG.
	 * @param filename Ruta al fitxer JPEG.
	 */
	private void loadDpi(String filename) {
		ImageReader reader = null;
		try (ImageInputStream iis = ImageIO.createImageInputStream(new File(filename))) {
			// Get metadata from file
			reader = ImageIO.getImageReaders(iis).next();
			reader.setInput(iis, true);
			IIOMetadata metadata = reader.getImageMetadata(0);

			// Locate dpi
            String[] names = metadata.getMetadataFormatNames();
            for (String name : names) {
                Node node = metadata.getAsTree(name);
                String dpiString = (String) queryXPath(node, "JPEGvariety/app0JFIF/@Xdensity", XPathConstants.STRING);
                if (dpiString != null) {
                	m_dpiX = Double.parseDouble(dpiString);
                }
                dpiString = (String) queryXPath(node, "JPEGvariety/app0JFIF/@Ydensity", XPathConstants.STRING);
                if (dpiString != null) {
                	m_dpiY = Double.parseDouble(dpiString);
                }
                if (m_dpiX != 0 && m_dpiY != 0) {
                	break;
                }
                dpiString = (String) queryXPath(node, "Dimension/HorizontalPixelSize/@value", XPathConstants.STRING);
                if (dpiString != null) {
                	m_dpiX = Math.round(25.4 / Double.parseDouble(dpiString));
                }
                dpiString = (String) queryXPath(node, "Dimension/VerticalPixelSize/@value", XPathConstants.STRING);
                if (dpiString != null) {
                	m_dpiY = Math.round(25.4 / Double.parseDouble(dpiString));
                }
                if (m_dpiX != 0 && m_dpiY != 0) {
                	break;
                }
            }
            
            reader.dispose();
            
        } catch (Exception e) {
        	try {reader.dispose();} catch (Exception ex) {/*Ignore*/}
        }		
	}

	
	/**
	 * Gets content from a DOM tree using XPath query
	 * @param xpathQuery XPath expression to query the tree.
	 * @param type Type of content to get (<code>XPathConstants.*</code>)
	 * @return Object with found content, or <code>null</code> on error
	 */
    private Object queryXPath(Node node, String xpathQuery, QName type) {

		// Generamos objeto XPath
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		// Evaluamos expresión
		Object object = null;
		try {
			XPathExpression expression = xpath.compile(xpathQuery);
			if (expression.evaluate(node, XPathConstants.NODE) != null) {
				object = expression.evaluate(node, type);
			}
		} catch (Exception e) {
			object = null;
		}
		
		return object;
	}
	
	
	/**
	 * Embeds an ICC Profile into an existing JPEG image file.
	 * @param iccProfile ICC Profile to embed
	 * @param imagePath JPEG image file to modify
	 * @return <code>true</code> on success, <code>false</code> on failure
	 */
	public static boolean embedIccProfile(ICC_Profile iccProfile, String imagePath) {
	
		// Determine location of current APP2 ICC Profile markers
		byte[] buffer = new byte[256];
		Vector<Long> APP2Positions = new Vector<Long>();
		Vector<Long> APP2Lengths = new Vector<Long>();
		FileInputStream fis = null;
		FileOutputStream os = null;
		File tempFile = null;		
		try {
			fis = new FileInputStream(imagePath);
			
			// Check SOI marker
			if (fis.read(buffer,0,2) != 2) {
				fis.close();
				return false;
			}
			if (memcmp(buffer,SOI,2) != 0) {
				fis.close();
				return false;
			}

			// Parse APP markers
			long markerLength = 0;
			boolean finished = false;
			while (!finished) {
				if (fis.read(buffer,0,2) != 2) {
					fis.close();
					return false;
				}
				if (buffer[0] != (byte)0xFF) {
					fis.close();
					return false;
				}
				switch (buffer[1]) {
					case (byte)0xFF:
						break;

					case JPEG_MARKER_APP2:
						long markerStart = fis.getChannel().position();
						if (fis.read(buffer,0,2) != 2) {
							fis.close();
							return false;
						}
						markerLength = (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
						if (fis.read(buffer,0,12) != 12) {
							fis.close();
							return false;
						}
						if (memcmp(buffer,ICC_TAG,12) == 0) {
							APP2Positions.add(markerStart-2);
							APP2Lengths.add(markerLength+2);
						}
						fis.getChannel().position(markerStart+markerLength);
						break;

					case JPEG_MARKER_APP0:
					case JPEG_MARKER_APP1:
					case JPEG_MARKER_APP3:
					case JPEG_MARKER_APP4:
					case JPEG_MARKER_APP5:
					case JPEG_MARKER_APP6:
					case JPEG_MARKER_APP7:
					case JPEG_MARKER_APP8:
					case JPEG_MARKER_APP9:
					case JPEG_MARKER_APP10:
					case JPEG_MARKER_APP11:
					case JPEG_MARKER_APP12:
					case JPEG_MARKER_APP13:
					case JPEG_MARKER_APP14:
					case JPEG_MARKER_APP15:
						if (fis.read(buffer,0,2) != 2) {
							fis.close();
							return false;
						}
						markerLength = (0xFF & buffer[0])*256 + (0xFF & buffer[1]);
						fis.skip(markerLength-2);
						break;

					default:
						finished = true;
						break;
				}
			}
			fis.getChannel().position(0);	

			// Generate new JPEG in temp file
			tempFile = File.createTempFile("temp", ".jpg");
			os = new FileOutputStream(tempFile);
			
			// Copy SOI
			fis.getChannel().transferTo(0, 2, os.getChannel());
			
			// Insert new APP2 markers
			byte[] profileData = iccProfile.getData();
			int remainingIccBytes = profileData.length;
			int iccChunks = remainingIccBytes / 65519 + ((remainingIccBytes % 65519 > 0) ? 1 : 0);
			int chunkCount = 1;
			while (remainingIccBytes > 0) {
				int bytesToWrite = Math.min(65519, remainingIccBytes);
				os.write(0xFF);
				os.write(JPEG_MARKER_APP2);
				markerLength = 2+12+2+bytesToWrite;
				os.write((byte)((markerLength & 0xFF00) >> 8));
				os.write((byte)(markerLength & 0xFF));
				os.write(ICC_TAG);
				os.write((byte)(chunkCount & 0xFF));
				os.write((byte)(iccChunks & 0xFF));
				os.write(profileData, profileData.length - remainingIccBytes, bytesToWrite);
				chunkCount++;
				remainingIccBytes -= bytesToWrite;
			}
			
			// TODO: Copy rest of original JPEG, skipping old APP2 markers
			fis.getChannel().transferTo(2, fis.getChannel().size()-2, os.getChannel());
			
			// Finish read and write
			fis.close();
			os.close();
			
			// Replace old image file with temp file
			File oldImage = new File(imagePath);
			Files.copy(tempFile.toPath(), oldImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.delete(tempFile.toPath());
						
		} catch (Exception e) {
			e.printStackTrace();
			try {fis.close();} catch (Exception ex) {/*Ignore*/}
			try {os.close();} catch (Exception ex) {/*Ignore*/}
			if (tempFile != null) tempFile.delete();			
			return false;
		}	
		
		return true;
	}


	/**
	 * Detecta el número de canales del archivo JPEG.
	 * @param filePath Ruta al archivo JPEG.
	 */
	private void loadNumBands(String filePath) {		
		ImageInputStream in = null;
		try {
			in = ImageIO.createImageInputStream(new File(filePath));
		    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
		    boolean done = false;
		    while (readers.hasNext() && !done) {
		        ImageReader reader = readers.next();
		        try {
		            reader.setInput(in);
		            m_numBands = reader.getImageTypes(0).next().getNumBands();
		            done = true;
		        } finally {
		            reader.dispose();
		        }
		    }
		    in.close();
		} catch (Exception e) {
		    try {in.close();} catch (Exception ex) {/*Ignore*/}
		    m_numBands = 4;
		}
	}
	
	
	@Override
	public ImageType getImageType() {
		return ImageType.JPEG;
	}


	@Override
	public boolean isGreyscale() {
		return m_numBands == 1;
	}


	@Override
	public boolean isRGB() {
		return m_numBands == 3;
	}


	@Override
	public boolean isCMYK() {
		return m_numBands == 4;
	}


	@Override
	public double getDpiX() {
		return m_dpiX;
	}


	@Override
	public double getDpiY() {
		return m_dpiY;
	}	
}
