package com.servifot.lfm.lfmimporter;

import java.io.File;

import com.servifot.lfm.utils.FXWorker;
import com.servifot.lfm.utils.FileUtils;
import com.servifot.lfm.utils.LFMUtils;

import javafx.scene.layout.TilePane;

/**
 * Llena un panel de imágenes
 *
 * @author FRANCESC
 *
 */
public class PaneFiller extends Thread {

    private TilePane m_tp = null;
    private File m_folder = null;
    private boolean m_die = false;

    public PaneFiller(TilePane tp, File folder) {
    	m_tp = tp;
    	m_folder = folder;
    }

    @Override
    public void run() {
        if ((m_tp == null) || (m_folder == null) || !m_folder.exists()) {
        	return;
        }

        File[] folderFiles = m_folder.listFiles();
        if (folderFiles.length < 1) return;

        File[] sortedFiles = LFMUtils.sortByDate(folderFiles);
        for (File file : sortedFiles) {
        	if (m_die) return;
        	if (FileUtils.getExtension(file.getAbsolutePath()).toLowerCase().equals("jpg")) {
        		SearchThumbnailWidget st = new SearchThumbnailWidget(file);
        		FXWorker.runAsync(FXWorker.JOBTYPE_ADD_SEARCHCHILD, m_tp, st, 0);
        	}
        }
        m_die = false;
    }

    public void kill() {
    	m_die = true;
    }

    public boolean isKilled() {
    	return m_die;
    }
}
