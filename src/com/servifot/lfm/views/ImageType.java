package com.servifot.lfm.views;

/**
 * Identificadores de tipos de imagen.
 */
public enum ImageType {
	UNKNOWN("Desconocido"),
	PNG("PNG"),
	JPEG("JPEG"),
	BMP("BMP"),
	TIFF_LE("TIFF_LE"),
	TIFF_BE("TIFF_BE");
	
	private String m_description;
	
	private ImageType(String description) {
		m_description = description;
	}
	
	public String toString() {
		return m_description;
	}
}
