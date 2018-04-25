package com.servifot.lfm.utils;

/**
 * Orientaciones de los píxeles de una imagen digital.
 */
public enum ImageOrientation {
	TOP("TOP"),
	RIGHT("RIGHT"),
	LEFT("LEFT"),
	DOWN("DOWN");
	
	public static final String VALUE_TOP = "TOP";
	public static final String VALUE_RIGHT = "RIGHT";
	public static final String VALUE_LEFT = "LEFT";
	public static final String VALUE_DOWN = "DOWN";
	
	private String m_description;
	
	private ImageOrientation(String description) {
		m_description = description;
	}
	
	public String toString() {
		return m_description;
	}
}
