package com.servifot.lfm.views;

import java.util.ArrayList;
import javafx.scene.layout.Pane;

/**
 *
 * Clase abstracta que sirve de base para definir las diferentes pantallas de la aplicación
 *
 *
 * @author FRANCESC
 *
 */
public abstract class View {
	/** Referencia a la vista de la que proviene (anterior)*/
	private View m_backView ;
	/** Referencia al panel que contiene la ventana */
	private Pane m_pane;
	/** Conjunto de listeners */
	private ArrayList<ViewListener> m_allListeners = null;
	
	
	public View() {}
	
	public Pane getPane(){
		if (m_pane == null) m_pane = createPane();
		return m_pane;
		//return m_pane == null ? createPane() : m_pane;
	}
	
	/**
	 * Método que ejecuta la clase principal después de cargar una vista.
	 */
	public void onLoad() {
		
	}
	
	/**
	 * Devuelve la vista anterior
	 * 
	 * @return View Vista anterior
	 */
	public View getBackView() {
		return m_backView;
	}
	
	/**
	 * Asigna la vista anterior
	 * 
	 * @param backView La vista anterior
	 */
	public void setBackView(View backView) {
		m_backView = backView;
	}
	
	/**
	 * Añade en la lista de los Listeners, cuando se genere un evento se invocará el método
	 * correspondiente de la implementación.
	 * 
	 * @param newListener Clase que quiera recibir los eventos
	 */
	public void addListener(ViewListener newListener) {
		if(m_allListeners == null) {
			m_allListeners = new ArrayList<>();
		}
		if(!m_allListeners.contains(newListener)){
			m_allListeners.add(newListener);	
		}
	}
	
	/**
	 * Elimina en la lista de los Listeners, cuando se genere un evento se invocará el método
	 * correspondiente de la implementación.
	 * 
	 * @param newListener Clase que quiera recibir los eventos
	 */
	public void removeListener(ViewListener remListener){
		if(m_allListeners != null) m_allListeners.remove(remListener);
	}
	
	/**
	 * Comunica a la aplicación principal el evento de finalización de esta pantalla
	 * 
	 */
	public void emitViewFinished(View nextView) {
		for(ViewListener app : m_allListeners) {
			app.onViewFinished(nextView);
		}
	}
	
	/**
	 * Comunica a la aplicación principal el evento de finalización de esta pantalla
	 */
	public void emitAddView(View nextView) {
		//allListeners = nextView.m_backView.allListeners;

		for ( ViewListener app : m_allListeners ) {
			app.onAddView(nextView);
		}
	}
	
	/**
	 * Comunica a la aplicación principal el evento de finalización de esta pantalla
	 * 
	 */
	protected void emitAddedViewFinished() {
		for(ViewListener app : m_allListeners) {
			app.onAddedViewFinished();
		}
	}
	
	/**
	 * Devuelve el nodo raíz de objetos JavaFX correspondientes a esta pantalla
	 * 
	 * @return Nodo raíz de objetos JavaFX correspondientes a la pantalla
	 */
	public abstract Pane createPane();
	
	/**
	 * Devuelve el nombre base del fichero CSS correspondiente a esta pantalla
	 * 
	 * @return Nombre base (sin extensión) del fichero CSS correspondiente a esta pantalla
	 */
	public abstract String getCssName();
	
}
