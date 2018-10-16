package xyz.simek.jgeck.controller;

import javax.naming.OperationNotSupportedException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class ViewFactory {

	public static ViewFactory defaultFactory = new ViewFactory(); 
	
	private MainController mainController;
	private static boolean mainViewInitialized = false;
	
	//private final String DEFAULT_CSS = "style.css";
	private final String MAIN_FXML = "MainLayout.fxml"; 
			
	public Scene getMainScene() throws OperationNotSupportedException {
		if(!mainViewInitialized) {
			mainController = new MainController();
			mainViewInitialized = true;
			return initializeScene(MAIN_FXML, mainController);
		} else {
			throw new OperationNotSupportedException("Main scene already initialized");
		}
	}
	
	private Scene initializeScene(String fxmlPath, Object controller) {
		FXMLLoader loader;
		Parent parent;
		Scene scene;
		
		try {
			loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath));
			loader.setController(controller);
			parent = loader.load();
			System.out.println("Loading FXML " + fxmlPath);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not load FXML " + fxmlPath);
			return null;
		}
		scene = new Scene(parent);
		//scene.getStylesheets().add(getClass().getClassLoader().getResource(DEFAULT_CSS).toExternalForm());
		
		return scene;
	}
}
