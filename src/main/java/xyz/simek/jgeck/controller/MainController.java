package xyz.simek.jgeck.controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

import org.controlsfx.glyphfont.FontAwesome.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import xyz.simek.jgeck.model.DatFile;
import xyz.simek.jgeck.model.DatItem;
import xyz.simek.jgeck.model.Highlighter;
import xyz.simek.jgeck.model.IniHighlighter;
import xyz.simek.jgeck.model.format.FrmHeader;
import xyz.simek.jgeck.model.format.RixImage;

public class MainController implements Initializable {

	@FXML
	private TreeView<String> fileTree; // FIXME: DatFile

	@FXML
	private Label selectedFileLabel;
	private Label infoLabel;

    @FXML
    private Button openButton;

    @FXML
    private Button exportButton;
    
	@FXML
	private BorderPane mainPane;

	private CodeArea codeEditor = new CodeArea();
	private Highlighter highlighter;
	
	private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");

	private DatFile datFile = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		openButton.setGraphic(fontAwesome.create(Glyph.FOLDER_OPEN));
		exportButton.setGraphic(fontAwesome.create(Glyph.SAVE));
	}
	
	private void loadDatFile(String path) {
		
		try {
			datFile = new DatFile(path);
		} catch (IOException ex) {
			System.err.println("Could not open dat file: " + ex.getMessage());
		}
		
		fileTree.setShowRoot(false);
		TreeItem<String> root = new TreeItem<>();
		fileTree.setRoot(root);

		fileTree.setOnMouseClicked(e -> {
			
			if (fileTree.getSelectionModel().getSelectedItem() == null)
				return;

			String filename = getSelectedFilename();
			if (filename == null)
				return;

			selectedFileLabel.setText(filename);

			DatItem file = datFile.getItems().get(filename);
			if (file == null) {
				System.err.println("File not found: " + filename);
				return;
			}
			
			String extension = getFileExtension(filename);
			switch (extension) {
			case "BIO":
			case "TXT":
			case "MSG": // TODO: MsgHighlighter
			case "SVE": // TODO: SveHighlighter
			case "GAM": // TODO: GamHighlighter
			case "BAK":
			case "LST":
				try {
					codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));

			        VirtualizedScrollPane<CodeArea> vPane = new VirtualizedScrollPane<>(codeEditor);
			        
			        highlighter = new IniHighlighter(codeEditor);        
			        highlighter.highlight();
			        
			        codeEditor.replaceText(new String(file.getData()));
			        			        
					vPane.scrollToPixel(0.0, 0.0);
					
					mainPane.setCenter(vPane);
					
				} catch (DataFormatException | IOException ex) {
					System.err.println("Could not get item data: " + ex.getMessage());
					infoLabel = new Label("Could not get item data: " + ex.getMessage());
					infoLabel.setTextFill(Color.RED);
					mainPane.setCenter(infoLabel);
				}
				break;

			case "FRM":
				try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(file.getData()))) {
					FrmHeader frm = FrmFileReader.readFrm(is);

					Image image = frm.getImage(0, 0, true);
					ImageView view = new ImageView();
					view.setImage(image);
					mainPane.setCenter(view);

				} catch (Exception ex) {
					System.err.println("Could not access item data: " + ex.getMessage());
				}
				break;

			case "RIX":
				ByteBuffer buff = null;
				try {
					buff = ByteBuffer.wrap(file.getData());
				} catch (DataFormatException | IOException e1) {
					e1.printStackTrace();
				}
				
				RixImage rix = new RixImage();
				rix.read(buff);
				WritableImage img = new WritableImage(rix.getWidth(), rix.getHeight());
				PixelWriter pw = img.getPixelWriter();
				pw.setPixels(0, 0, rix.getWidth(), rix.getHeight(), PixelFormat.getIntArgbPreInstance(), rix.getData(), 0, rix.getWidth());
				
				ImageView view = new ImageView();
				view.setImage(img);
				mainPane.setCenter(view);

				break;
				
			default:
				infoLabel = new Label("Preview for this filetype is not available.");
				infoLabel.setTextFill(Color.RED);
				mainPane.setCenter(infoLabel);
				break;
			}
		});

		// FIXME: processor bottleneck
		// FOLDERS
		datFile.getItems().forEach((k, v) -> {

			TreeItem<String> current = root;

			for (String component : k.split("\\\\")) {
				if (k.endsWith(component))
					continue;
				current = getOrCreateChild(current, component);
			}
		});

		// FILES
		datFile.getItems().forEach((k, v) -> {

			TreeItem<String> current = root;

			for (String component : k.split("\\\\")) {
				current = getOrCreateChild(current, component);
			}
		});
	}
	
	/**
	 * Helper method for creating hierarchical representation of files in the DAT
	 * archive
	 * 
	 * @param parent
	 * @param value part of file path
	 * @return item if it already exists or creates a new subitem
	 */
	private TreeItem<String> getOrCreateChild(TreeItem<String> parent, String value) {

		for (TreeItem<String> child : parent.getChildren()) {
			if (value.equals(child.getValue())) {
				return child;
			}
		}

		TreeItem<String> newChild = new TreeItem<>(value, getIconForFile(value));
		parent.getChildren().add(newChild);
		return newChild;
	}

	/**
	 * Extracts file extension from filename
	 * 
	 * @param filename
	 * @return extension in UPPERCASE
	 */
	private String getFileExtension(String filename) {
		int dot = filename.lastIndexOf(".");
		if (dot == -1 || filename.length() < (dot + 1)) {
			return "";
		}
		return filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
	}

	/**
	 * Reconstructs full path to file of selected TreeItem from {@link #fileTree}
	 * 
	 * @return path
	 */
	private String getSelectedFilename() {

		TreeItem<String> item = fileTree.getSelectionModel().getSelectedItem();
		StringBuilder sb = new StringBuilder();
		while (true) {
			sb.insert(0, item.getValue());
			if (item.getParent() == null || item.getParent() == fileTree.getRoot())
				break;

			sb.insert(0, "\\");
			item = item.getParent();
		}

		return sb.toString();
	}

	private org.controlsfx.glyphfont.Glyph getIconForFile(String filename) {

		Glyph icon;

		switch (getFileExtension(filename)) {
		case "":
			icon = Glyph.FOLDER;
			break;
		case "ACM":
			icon = Glyph.FILE_SOUND_ALT;
			break;
		case "MVE":
			icon = Glyph.FILE_MOVIE_ALT;
			break;
		case "FRM":
		case "PRO":
		case "RIX":
		case "PAL":
			icon = Glyph.FILE_IMAGE_ALT;
			break;
		case "AAF":
		case "FON":
			icon = Glyph.FONT;
			break;
		case "SSL":
			icon = Glyph.FILE_CODE_ALT;
			break;
		case "INT":
			icon = Glyph.GEAR;
			break;
		case "MAP":
		case "MSK":
			icon = Glyph.MAP_MARKER;
			break;
		case "GAM":
		case "LST":
		case "MSG":
		case "SVE":
		case "TXT":
		case "BAK":
		case "BIO":
			icon = Glyph.FILE_TEXT;
			break;
		case "CFG":
		case "GCD":
		case "LIP":
		default:
			icon = Glyph.FILE;
			break;
		}

		return fontAwesome.create(icon);
	}
	
    @FXML
    void openButtonClicked(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Fallout 2 DAT File");
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("Fallout 2 DAT files", "*.dat"));
    	File datFile = fileChooser.showOpenDialog(new Stage());
    	
    	if(datFile != null)
    		loadDatFile(datFile.toString());
    }
	
}
