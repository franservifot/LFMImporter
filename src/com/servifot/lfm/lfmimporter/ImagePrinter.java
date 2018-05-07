package com.servifot.lfm.lfmimporter;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.servifot.lfm.utils.FileUtils;
import com.servifot.lfm.utils.ImageOrientation;
import com.servifot.lfm.utils.JPEGMetadata;
import com.servifot.lfm.utils.LFMUtils;

import javafx.scene.shape.Rectangle;


public class ImagePrinter extends Thread {

    // Archivo a imprimir
    File m_printFile = null;

    public ImagePrinter(File printFile) {
        m_printFile = printFile;
    }

    @Override
    public void run() {
        if(m_printFile == null || !m_printFile.exists()) return;

        File maskFile = new File(LFMImporter.getConfig().getVMaskPath());
        File destFile = new File(LFMImporter.getConfig().getPrinterFolder() + "/" + m_printFile.getName());

        try {
			BufferedImage printimage = ImageIO.read(m_printFile);
			boolean ish = printimage.getWidth() > printimage.getHeight();

			JPEGMetadata meta = new JPEGMetadata(m_printFile.getAbsolutePath());
			switch (meta.getOrientation().toString()) {
			case ImageOrientation.VALUE_DOWN:

				break;
			case ImageOrientation.VALUE_LEFT:
				ish = !ish;
				break;
			case ImageOrientation.VALUE_RIGHT:
				ish = !ish;
				break;
			}

			if (ish) {
				maskFile = new File(LFMImporter.getConfig().getHMaskPath());
				if (maskFile == null || !maskFile.exists()) {
		        	FileUtils.copyFile(m_printFile, destFile, true);
		        	System.out.print("Impreso archivo sin máscara: " + destFile.getAbsolutePath());
		        	return;
		        }
			}
			BufferedImage maskimage = ImageIO.read(maskFile);
			BufferedImage combined = new BufferedImage(printimage.getWidth(), printimage.getHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics g = combined.getGraphics();
			g.drawImage(printimage, 0, 0, null);

			Rectangle canvasrect = new Rectangle(printimage.getWidth(), printimage.getHeight());
			Rectangle maskrect = new Rectangle(maskimage.getWidth(), maskimage.getHeight());

			Rectangle dest = LFMUtils.fitRectangle(maskrect, canvasrect);

			g.drawImage(maskimage, (int)dest.getX(), (int)dest.getY(), (int)dest.getWidth(), (int)dest.getHeight(), 0, 0, maskimage.getWidth(), maskimage.getHeight(), null);

			ImageIO.write(combined, "jpg", destFile);

		} catch (IOException e) {
			e.printStackTrace();
		}


    }

}
