package com.servifot.lfm.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * A simple parser for INI files.
 */
public class IniParser {

	/** Si hay que procesar los comentarios multilínea de los ficheros LAC */
	private boolean m_parseLACComments = true;
	/** Almacén de las secciones, claves y valores contenidos en el fichero INI */
	private LinkedHashMap<String, LinkedHashMap<String, String>> mSections;

	/**
	 * Creates an empty <code>INIParser</code> instance. Data must be loaded by
	 * calling {@link #initFromString(String) initFromString}
	 */
	public IniParser() {
		// Do nothing
	}

	/**
	 * Creates a new <code>INIParser</code> instance from the INI file at the
	 * given path. <code>aCharset</code> specifies the character encoding of the
	 * file.
	 *
	 * @param aFilename path of INI file to parse
	 * @param aCharset character encoding of file
	 * @param parseLACComments wether to parse LAC multi-line comments or not
	 * @throws FileNotFoundException if <code>aFilename</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	public IniParser(String aFilename, Charset aCharset, boolean parseLACComments) throws FileNotFoundException, IOException {
		m_parseLACComments = parseLACComments;
		initFromFile(new File(aFilename), aCharset);
	}

	/**
	 * Creates a new <code>INIParser</code> instance from the INI file at the
	 * given path. <code>aCharset</code> specifies the character encoding of the
	 * file.
	 *
	 * @param aFilename path of INI file to parse
	 * @param aCharset character encoding of file
	 * @throws FileNotFoundException if <code>aFilename</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	public IniParser(String aFilename, Charset aCharset) throws FileNotFoundException, IOException {
		initFromFile(new File(aFilename), aCharset);
	}

	/**
	 * Creates a new <code>INIParser</code> instance from the INI file at the
	 * given path, which is assumed to be in the <code>CP1252</code> charset.
	 *
	 * @param aFilename path of INI file to parse
	 * @throws FileNotFoundException if <code>aFilename</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	public IniParser(String aFilename) throws FileNotFoundException,IOException {
		initFromFile(new File(aFilename), Charset.forName("Cp1252"));
	}

	/**
	 * Creates a new <code>INIParser</code> instance from the given file.
	 * <code>aCharset</code> specifies the character encoding of the file.
	 *
	 * @param aFile INI file to parse
	 * @param aCharset character encoding of file
	 * @throws FileNotFoundException if <code>aFile</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	public IniParser(File aFile, Charset aCharset) throws FileNotFoundException, IOException {
		initFromFile(aFile, aCharset);
	}

	/**
	 * Creates a new <code>INIParser</code> instance from the given file.
	 * <code>aCharset</code> specifies the character encoding of the file.<br>
	 *
	 * @param aFile INI file to parse
	 * @param aCharset character encoding of file
	 * @param parseLACComments Wether to parse LAC multi-line comments or not
	 * @throws FileNotFoundException if <code>aFile</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	public IniParser(File aFile, Charset aCharset, boolean parseLACComments) throws FileNotFoundException, IOException {
		m_parseLACComments = parseLACComments;
		initFromFile(aFile, aCharset);
	}

	/**
	 * Creates a new <code>INIParser</code> instance from the given file, which
	 * is assumed to be in the <code>CP1252</code> charset.
	 *
	 * @param aFile INI file to parse
	 * @throws FileNotFoundException if <code>aFile</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	public IniParser(File aFile) throws FileNotFoundException, IOException {
		initFromFile(aFile, Charset.forName("Cp1252"));
	}
	
	/**
	 * Inicializa el iniparser a partir de un InputStream. Pasa el InputStream a String 
	 * y utiliza el constructor del String
	 * 
	 * @param inifile Recurso inputstream del fichero .ini
	 * @param encode Charset con la codificación a utilizar
	 * @throws IOException
	 */
	public IniParser(InputStream inifile, Charset encode) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inifile.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}
		initFromString(result.toString(encode.name()));
		System.out.println(result.toString(encode.name()));
	}

	/**
	 * Parses given INI file.
	 *
	 * @param aFile INI file to parse
	 * @param aCharset character encoding of file
	 * @throws FileNotFoundException if <code>aFile</code> does not exist.
	 * @throws IOException if there is a problem reading the given file.
	 */
	private void initFromFile(File aFile, Charset aCharset) throws FileNotFoundException, IOException {
		// Read file into String
		FileInputStream fileStream = new FileInputStream(aFile);
		InputStreamReader inStream = new InputStreamReader(fileStream, aCharset);
		BufferedReader reader = new BufferedReader(inStream);
		String fileContents = "";
		String line;
		while ((line = reader.readLine()) != null) {
			fileContents += line+"\n";
		}
		reader.close();
		
		// Use string to init class
		initFromString(fileContents);
	}

	/**
	 * Parses the given String, which stores the contents of a INI file
	 *
	 * @param iniFileData String with the contents of a INI file
	 */
	public void initFromString(String iniFileData) {
		mSections = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		String currSection = null;

		String[] lines = iniFileData.split("\\r?\\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			// skip empty lines and comment lines
			String trimmedLine = line.trim();
			if (trimmedLine.length() == 0 || trimmedLine.startsWith("#") || trimmedLine.startsWith(";")) {
				continue;
			}

			// Look for section headers (i.e. "[Section]").
			if (line.startsWith("[")) {
				if (!trimmedLine.endsWith("]") || trimmedLine.indexOf("]") != (trimmedLine.length() - 1)) {
					currSection = null;
					continue;
				}

				// remove enclosing brackets
				currSection = trimmedLine.substring(1, trimmedLine.length() - 1);
				continue;
			}

			// If we haven't found a valid section header, continue to next line
			if (currSection == null) {
				continue;
			}

			// Get the data
			String[] splitted = line.split("=", 2);
			if (splitted.length != 2) {
				continue;
			}
			String property = splitted[0];
			String value = splitted[1];

			// Patch for Comments section in LAC files
			String nextSection = null;
			if (m_parseLACComments) {
				if (property.equals("Comments")) {
					i++;
					while ((i < lines.length) && (nextSection == null)) {
						line = lines[i];
						if (line.startsWith("[") && line.endsWith("]")) {
							nextSection = line.substring(1, line.length() - 1);
						} else {
							value += "\n" + line;
							i++;
						}
					}
				}
			}

			// Store property
			LinkedHashMap<String, String> props = mSections.get(currSection);
			if (props == null) {
				props = new LinkedHashMap<String, String>();
				mSections.put(currSection, props);
			}
			props.put(property, value);

			// Patch for Comments section in LAC files
			if (nextSection != null) {
				currSection = nextSection;
			}
		}
	}

	/**
	 * Returns an iterator over the section names available in the INI file.
	 *
	 * @return an iterator over the section names
	 */
	public Iterator<String> getSections() {
		return mSections.keySet().iterator();
	}

	/**
	 * Returns an iterator over the keys available within a section.
	 *
	 * @param aSection
	 *            section name whose keys are to be returned
	 * @return an iterator over section keys, or <code>null</code> if no such
	 *         section exists
	 */
	public Iterator<String> getKeys(String aSection) {
		LinkedHashMap<String, String> props = mSections.get(aSection);
		if (props == null) {
			return null;
		}
		return props.keySet().iterator();
	}

	/**
	 * Gets the string value for a particular section and key.
	 *
	 * @param aSection
	 *            a section name
	 * @param aKey
	 *            the key whose value is to be returned.
	 * @return string value of particular section and key, <code>null</code> if
	 *         not found
	 */
	public String getString(String aSection, String aKey) {
		LinkedHashMap<String, String> props = mSections.get(aSection);
		if (props == null) {
			return null;
		}
		return props.get(aKey);
	}

	/**
	 * Gets the string value for a particular section and key. A default value
	 * is returned if key is not found.
	 *
	 * @param aSection
	 *            a section name
	 * @param aKey
	 *            the key whose value is to be returned.
	 * @param defaultValue
	 *            default value to be returned if key not found
	 * @return string value of particular section and key
	 */
	public String getString(String aSection, String aKey, String defaultValue) {
		String value = getString(aSection, aKey);
		return (value == null) ? defaultValue : value;
	}

}
