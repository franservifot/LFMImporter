package com.servifot.lfm.utils;

import java.awt.color.ICC_Profile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Metadatos de archivos PNG.<br>
 * <br>
 * PNG docs: <code>https://www.w3.org/TR/PNG/#5DataRep</code>
 */
public class PNGMetadata extends ImageMetadata {
	
	/** Anchura de la imagen (px). */
	private int m_width;
	/** Altura de la imagen (px). */
	private int m_height;
	/** Profundidad de bits de color. */
	private int m_bitDepth;
	/** Identificador PNG del tipo de color. */
	private int m_colourType;
	/** Bytes del perfil de color incrustado. */
	private byte[] m_iccProfileData;
	/** Resolución horizontal (dpi). */
	private double m_dpiX;
	/** Resolución vertical (dpi). */
	private double m_dpiY;

	/**
	 * Carga los metadatos de un archivo PNG.
	 * @param pngFile Archivo PNG a analizar.
	 * @return <code>true</code> si se procesa correctamente, <code>false</code> si se produce algún error.
	 */
	public boolean load(File pngFile) {
		try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(pngFile))) {
			readHeader(is);
			while (readNextChunk(is));
		} catch (Exception e) {
			System.err.println("Error lectura metadatos PNG: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	/**
	 * Lee la cabecera del archivo PNG.
	 * @param is Stream de donde leer la cabecera PNG.
	 * @throws Exception Si no se puede leer o no es un archivo PNG.
	 */
	private void readHeader(InputStream is) throws Exception {
		byte[] png_header = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
		byte[] header = readBytes(is, 8);
		if (!Arrays.equals(png_header, header)) {
			throw new Exception("Not a PNG file");
		}
	}
	
	
	/**
	 * Lee el siguiente "chunk" de metadatos PNG, y registra los metadatos encontrados.
	 * @param is Stream de donde leer.
	 * @return <code>true</code> se ha podido leer, <code>false</code> si el stream ha llegado al final.
	 * @throws Exception Si se produce algún error durante la lectura.
	 */
	private boolean readNextChunk(InputStream is) throws Exception {
		// Recuperar longitud de datos
		byte[] dataLengthBytes = new byte[4];
		int count = is.read(dataLengthBytes);
		if (count == -1) {
			return false;
		}
		if (count != dataLengthBytes.length) {
			throw new Exception("Can't read chunk data length");
		}
		int dataLength = readInteger(dataLengthBytes);
		
		// Recuperar tipo de chunk
		byte[] typeBytes = new byte[4];
		if (is.read(typeBytes) != typeBytes.length) {
			throw new Exception("Can't read chunk type");
		}
		String type = new String(typeBytes, Charset.forName("US-ASCII"));
		
		// Procesar datos del chunk
		if (type.equals("IHDR")) {
			parseHeader(is, dataLength);
			
		} else if (type.equals("iCCP")) {
			parseIcc(is, dataLength);
			
		} else if (type.equals("pHYs")) {
			parsePhys(is, dataLength);
			
		} else {
			int skipped = 0;
			while (skipped != dataLength) {
				skipped += is.skip(dataLength - skipped);
			}
		}
		
		// Leer CRC
		byte[] crc = new byte[4];
		if (is.read(crc) != crc.length) {
			throw new Exception("Can't read chunk CRC");
		}
				
		return true;
	}
	

	/**
	 * Analiza los metadatos de cabecera ("chunk" IHDR).
	 * @param is Stream de donde leer  los datos del chunk.
	 * @param length Longitud de los datos.
	 * @throws Exception Si se produce algún error durante la lectura.
	 */
	private void parseHeader(InputStream is, int length) throws Exception {
		if (length != 13) {
			throw new Exception("Bad header data length");
		}
		m_width = readInteger(readBytes(is, 4));
		m_height = readInteger(readBytes(is, 4));
		m_bitDepth = readBytes(is, 1)[0];
		m_colourType = readBytes(is, 1)[0];
		readBytes(is, 3);
	}
	
	
	/**
	 * Analiza los metadatos de perfil incrustado ICC (chunk iCCP)
	 * @param is Stream de donde leer los datos del chunk.
	 * @param length Longitud de los datos.
	 * @throws Exception Si se produce algún error durante la lectura.
	 */
	private void parseIcc(InputStream is, int length) throws Exception {
		// Nombre del perfil (ingorado)
		byte[] b = new byte[1];
		int nameLength = 0;
		do {
			if (is.read(b) == -1) {
				throw new Exception("Can't read ICC profile name");
			}
			nameLength++;
		} while (b[0] != 0);
		
		// Método de compresión
		if (readBytes(is, 1)[0] != 0) {
			throw new Exception("Unsupported ICC compression method.");
		}
		
		// Leer datos ICC
		int dataLength = length - nameLength - 1;
		m_iccProfileData = readBytes(is, dataLength);
		m_iccProfileData = decompress(m_iccProfileData);
	}
	

	/**
	 * Analiza los metadatos de dimensiones físicas (chunk pHYs)
	 * @param is Stream de donde leer los datos del chunk.
	 * @param length Longitud de los datos.
	 * @throws Exception Si se produce algún error durante la lectura.
	 */
	private void parsePhys(InputStream is, int length) throws Exception {
		if (length != 9) {
			throw new Exception("Bad Phys data length");
		}
		int resX = readInteger(readBytes(is, 4));
		int resY = readInteger(readBytes(is, 4));
		int unit = readBytes(is, 1)[0];

		double inchesPerMeter = 100.0 / 2.54;
		if (unit == 1) {
			m_dpiX = resX / inchesPerMeter;
			m_dpiY = resY / inchesPerMeter;
		}
	}
	
	
	/**
	 * Lee un número especificado de bytes de un stream.
	 * @param is Stream de donde leer los bytes.
	 * @param numBytes Número de bytes a leer.
	 * @return Bytes leídos.
	 * @throws IOException Si no se puede leer el número de bytes indicado.
	 */
	private static byte[] readBytes(InputStream is, int numBytes) throws IOException {
		byte[] result = new byte[numBytes];
		int read = 0;
		while (read < numBytes) {
			int count = is.read(result);
			if (count == -1) {
				throw new IOException("End of stream reached before reading all requested bytes (read "+read+", requested "+numBytes+").");
			}
			read += count;
		}
		return result;
	}
	
	
	/**
	 * Lee un entero de 32 bits a partir de 4 bytes de un archivo PNG.
	 * @param bytes Array de 4 bytes de un archivo PNG.
	 * @return Entero correspondiente a los 4 bytes.
	 */
	private static int readInteger(byte[] bytes) {
		int value = 0;
		value |= (0xFF & bytes[3]);
		value |= (0xFF & bytes[2]) << 8;
		value |= (0xFF & bytes[1]) << 16;
		value |= (0xFF & bytes[0]) << 24;
		return value;
	}

	
	/**
	 * Descomprime un buffer de datos comprimido con ZLIB.
	 * @param data Datos comprimidos.
	 * @return Datos descomprimidos, o <code>null</code> si se produce algún error.
	 */
	private static byte[] decompress(byte[] data) {  
		Inflater inflater = new Inflater();   
		inflater.setInput(data);
		byte[] output = null;
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
			byte[] buffer = new byte[1024];  
			while (!inflater.finished()) {  
				int count = inflater.inflate(buffer);  
				outputStream.write(buffer, 0, count);  
			}  
			output = outputStream.toByteArray();	
		} catch (Exception e) {
			return null;
		}
		return output;  
	}
	
	
	/**
	 * Comprime un buffer de datos con el algoritmo ZLIB.
	 * @param data Datos originales.
	 * @return Datos comprimidos.
	 */
	private static byte[] compress(byte[] data) {
		byte[] output = new byte[data.length];
		Deflater compresser = new Deflater();
		compresser.setInput(data);
		compresser.finish();
		int compressedDataLength = compresser.deflate(output);
		compresser.end();
		return Arrays.copyOfRange(output, 0, compressedDataLength);
	}
	
	
	/**
	 * Incrusta un perfil ICC en un archivo PNG existente.
	 * @param imagePath Ruta del archivo PNG a modificar.
	 * @param iccProfile Perfil ICC a incrustar.
	 * @return <code>true</code> si se procesa correctamente, <code>false</code> si se produce algún error.
	 */
	public static boolean embedIccProfile(String imagePath, ICC_Profile iccProfile) {
		return embedMetadata(imagePath, iccProfile, 0);
	}


	/**
	 * Incrusta datos de resolución de impresión en un archivo PNG existente.
	 * @param dpi Resolución a incrustar (puntos por pulgada).
	 * @param imagePath Ruta del archivo PNG a modificar.
	 * @return <code>true</code> si se procesa correctamente, <code>false</code> si se produce algún error.
	 */
	public static boolean embedResolution(String imagePath, double dpi) {
		return embedMetadata(imagePath, null, dpi);
	}
	
	
	/**
	 * Incrusta metadatos en un archivo PNG existente.
	 * @param iccProfile Perfil ICC a incrustar, o <code>null</code> para no incrustar ninguno.
	 * @param dpi Resolución a incrustar (puntos por pulgada), o <code>0</code> para no incrustar resolución.
	 * @param imagePath Ruta del archivo PNG a modificar.
	 * @return <code>true</code> si se procesa correctamente, <code>false</code> si se produce algún error.
	 */
	public static boolean embedMetadata(String imagePath, ICC_Profile iccProfile, double dpi) {
		
		// Crear archivo temporal
		File tempFile = null;
		try {
			tempFile = File.createTempFile("tmp", "png");
		} catch (Exception e) {
			System.err.println("No se puede crear archivo temporal para incrustar perfil: " + e.getMessage());
			return false;			
		}
		
		// Copiar datos del PNG original, añadendo chunk iCCP con el perfil
		File imageFile = new File(imagePath);
		try (
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(imageFile));
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));
			)
		{
			// Cabecera
			byte[] header = readBytes(is, 8);
			os.write(header);
			
			// Chunks
			boolean headerRead = false;
			boolean iccWritten = false;
			boolean physWritten = false;
			do {
				// Leer longitud del chunk
				byte[] dataLengthBytes = new byte[4];
				int count = is.read(dataLengthBytes);
				if (count == -1) {
					break;
				}
				if (count != dataLengthBytes.length) {
					throw new Exception("Can't read chunk data length");
				}
				int dataLength = readInteger(dataLengthBytes);
				
				// Leer tipo
				byte[] type = readBytes(is, 4);
				String typeStr = new String(type, Charset.forName("US-ASCII"));
				
				// Leer datos
				byte[] data = null;
				if (dataLength > 0) {
					data = readBytes(is, dataLength);
				}
				
				// Leer CRC
				byte[] crc = readBytes(is, 4);
				
				// Copiamos chunk al archivo de salida
				boolean skipChunk = false;
				skipChunk |= (typeStr.equals("iCCP") && iccProfile != null);
				skipChunk |= (typeStr.equals("pHYs") && dpi > 0);
				if (!skipChunk) {
					os.write(dataLengthBytes);
					os.write(type);
					if (dataLength > 0) {
						os.write(data);
					}
					os.write(crc);
				}
				
				// Registramos lectura de la cabecera
				if (typeStr.equals("IHDR")) {
					headerRead = true;
				}
				
				// Añadimos chunk con el nuevo perfil
				if (headerRead && iccProfile != null && !iccWritten) {
					byte[] profileData = compress(iccProfile.getData());
					String profileName = "ICC Profile";
					byte[] namebytes = profileName.getBytes(Charset.forName("ISO-8859-1"));
					byte[] chunkData = new byte[profileName.length() + 1 + 1 + profileData.length];

					System.arraycopy(namebytes, 0, chunkData, 0, namebytes.length);
					chunkData[namebytes.length] = 0;
					chunkData[namebytes.length + 1] = 0;
					System.arraycopy(profileData, 0, chunkData, namebytes.length + 2, profileData.length);
					
					writeChunk(os, "iCCP", chunkData);					
					iccWritten = true;
				}
				
				// Añadimos chunk con la resolución
				if (headerRead && dpi > 0 && !physWritten) {
					int pixelsPerMeter = (int) Math.round(dpi * 100.0 / 2.54);
					
					byte[] chunkData = new byte[9];
					chunkData[0] = (byte) ((pixelsPerMeter & 0xFF000000) >> 24);
					chunkData[1] = (byte) ((pixelsPerMeter & 0x00FF0000) >> 16);
					chunkData[2] = (byte) ((pixelsPerMeter & 0x0000FF00) >> 8);
					chunkData[3] = (byte) (pixelsPerMeter & 0x000000FF);
					System.arraycopy(chunkData, 0, chunkData, 4, 4);
					chunkData[8] = 1;

					writeChunk(os, "pHYs", chunkData);					
					physWritten = true;
				}
				
			} while (true);			
			
		} catch (Exception e) {
			System.err.println("Can't generate file with embeded metadata: " + e.getMessage());
			tempFile.delete();
			return false;
		}
		
		// Sustituir fichero con el temporal generado
		try {
			Files.delete(imageFile.toPath());
			Files.move(tempFile.toPath(), imageFile.toPath());
		} catch (IOException e) {
			System.err.println("Can't generate file with embeded metadata: " + e.getMessage());
			tempFile.delete();
			return false;
		}

		return true;
	}
	
	
	/**
	 * Escribe un chunk PNG en un stream.
	 * @param os Stream donde escribir el chunk. 
	 * @param chunkType Tipo de chunk.
	 * @param chunkData Datos del chunk.
	 * @throws Exception Si se produce algún error durante la escritura.
	 */
	private static void writeChunk(OutputStream os, String chunkType, byte[] chunkData) throws Exception {
		int length = chunkData.length;
		os.write((length & 0xFF000000) >> 24);
		os.write((length & 0x00FF0000) >> 16);
		os.write((length & 0x0000FF00) >> 8);
		os.write((length & 0x000000FF));
		
		if (chunkType.length() != 4) {
			throw new Exception("Invalid chunk type: " + chunkType);
		}
		byte[] iccType = chunkType.getBytes(Charset.forName("US-ASCII")); 
		os.write(iccType);
		
		os.write(chunkData);
		
		CRC32 crc32 = new CRC32();
		crc32.update(iccType);
		crc32.update(chunkData);
		long crcValue = crc32.getValue();
		os.write((int)((crcValue & 0x00000000FF000000) >> 24));
		os.write((int)((crcValue & 0x0000000000FF0000) >> 16));
		os.write((int)((crcValue & 0x000000000000FF00) >> 8));
		os.write((int)((crcValue & 0x00000000000000FF)));
	}
	
		
	/**
	 * Devuelve el perfil de color ICC incrustado en la imagen.
	 * @return Perfil de color ICC incrustado en la imagen, o <code>null</code> si no se ha podido
	 * determinar.
	 */
	@Override
	public ICC_Profile getIccProfile() {
		ICC_Profile profile = null;
		try {
			profile = ICC_Profile.getInstance(m_iccProfileData);
		} catch (Exception e) {
			/* Ignore */
		}
		return profile;
	}


	/**
	 * Devuelve la anchura de la imagen.
	 * @return Anchura de la imagen (px).
	 */
	@Override
	public int getWidth() {
		return m_width;
	}

	
	/**
	 * Devuelve la altura de la imagen.
	 * @return Altura de la imagen (px).
	 */
	@Override
	public int getHeight() {
		return m_height;
	}


	/**
	 * Devuelve profundidad de color en bits de la imagen.
	 * @return Número de bits de profundidad de color.
	 */
	@Override
	public int getBitDepth() {
		return m_bitDepth;
	}

	
	/** 
	 * Indica si la imagen está en modo escala de grises.
	 * @return <code>true</code> si la imagen está en modo escala de grises, <code>false</code> en caso
	 * contrario.
	 */
	@Override
	public boolean isGreyscale() {
		return m_colourType == 0 || m_colourType == 4;
	}
	

	/** 
	 * Indica si la imagen está en modo RGB.
	 * @return <code>true</code> si la imagen está en modo RGB, <code>false</code> en caso
	 * contrario.
	 */
	@Override
	public boolean isRGB() {
		return m_colourType == 2 || m_colourType == 6;
	}

	
	/** 
	 * Indica si la imagen está en modo de paleta indexada.
	 * @return <code>true</code> si la imagen está en modo de paleta indexada, <code>false</code> en caso
	 * contrario.
	 */
	@Override
	public boolean isIndexed() {
		return m_colourType == 3;
	}

	
	/** 
	 * Indica si la imagen tiene transparencia.
	 * @return <code>true</code> si la imagen tiene transparencia, <code>false</code> en caso
	 * contrario.
	 */
	@Override
	public boolean isTransparent() {
		return m_colourType == 4 || m_colourType == 6;
	}

	
	@Override
	public ImageType getImageType() {
		return ImageType.PNG;
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
