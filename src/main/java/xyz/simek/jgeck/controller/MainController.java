package xyz.simek.jgeck.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import xyz.simek.jgeck.model.DatFile;
import xyz.simek.jgeck.model.format.dat.FalloutDatItem;
import xyz.simek.jgeck.model.format.dat.FalloutDirectory;
import xyz.simek.jgeck.model.format.dat.FalloutFile;
import xyz.simek.jgeck.model.format.frm.FrmHeader;
import xyz.simek.jgeck.model.format.map.MapFormat;
import xyz.simek.jgeck.model.format.rix.RixImage;
import xyz.simek.jgeck.view.CodeEditorPane;
import xyz.simek.jgeck.view.ImagePane;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.DataFormatException;

public class MainController implements Initializable {

    @FXML
    private TreeView<FalloutDatItem> fileTree;

    @FXML
    private Label selectedFileLabel;
    private Label infoLabel;

    @FXML
    private Button openButton;

    @FXML
    private Button exportButton;

    @FXML
    private BorderPane mainPane;

    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");

    private final Glyph iconFolder = fontAwesome.create("FOLDER");
    private final Glyph iconSound = fontAwesome.create("FILE_SOUND_ALT");
    private final Glyph iconMovie = fontAwesome.create("FILE_MOVIE_ALT");
    private final Glyph iconImage = fontAwesome.create("FILE_IMAGE_ALT");
    private final Glyph iconFont = fontAwesome.create("FONT");
    private final Glyph iconCode = fontAwesome.create("FILE_CODE_ALT");
    private final Glyph iconGear = fontAwesome.create("GEAR");
    private final Glyph iconMap = fontAwesome.create("MAP_MARKER");
    private final Glyph iconText = fontAwesome.create("FILE_TEXT");
    private final Glyph iconFile = fontAwesome.create("FILE");

    private DatFile datFile = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void loadDatFile(String path) {

        try {
            datFile = new DatFile(path);
        } catch (IOException ex) {
            setInfoMessage("Could not open dat file: " + ex.getMessage(), Color.RED);
            return;
        }

        fileTree.setShowRoot(false);
        TreeItem<FalloutDatItem> root = new TreeItem<>();
        fileTree.setRoot(root);

        fileTree.setOnMouseClicked(e -> {

            TreeItem<FalloutDatItem> selected = fileTree.getSelectionModel().getSelectedItem();

            if (selected == null || selected.getValue() instanceof FalloutDirectory)
                return;

            FalloutFile file = (FalloutFile) selected.getValue();

            selectedFileLabel.setText(file.getFilename());

            ByteBuffer buff = null;
            try {
                buff = ByteBuffer.wrap(file.getData());
            } catch (DataFormatException | IOException ex) {
                ex.printStackTrace();
            }

            String extension = getFileExtension(file.getFilename());
            switch (extension) {
                case "BIO":
                    mainPane.setCenter(new CodeEditorPane(CodeEditorPane.Format.TXT, new String(buff.array())));
                    break;
                case "TXT":
                case "MSG": // TODO: MsgHighlighter
                case "SVE": // TODO: SveHighlighter
                case "GAM": // TODO: GamHighlighter
                case "BAK":
                case "LST":
                    mainPane.setCenter(new CodeEditorPane(CodeEditorPane.Format.INI, new String(buff.array())));
                    break;

                case "FRM":
                    try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(file.getData()))) {
                        FrmHeader frm = FrmFileReader.readFrm(is);
                        mainPane.setCenter(new ImagePane(frm.getImage(0, 0, true)));

                    } catch (Exception ex) {
                        setInfoMessage("Could not access item data: " + ex.getMessage(), Color.RED);
                    }
                    break;

                case "RIX":

                    RixImage rix = new RixImage();
                    rix.read(buff);
                    WritableImage img = new WritableImage(rix.getWidth(), rix.getHeight());
                    PixelWriter pw = img.getPixelWriter();
                    pw.setPixels(0, 0, rix.getWidth(), rix.getHeight(), PixelFormat.getIntArgbPreInstance(), rix.getData(), 0, rix.getWidth());

                    mainPane.setCenter(new ImagePane(img));

                    break;

                case "MAP":
                    // TODO: highlight selected: https://stackoverflow.com/questions/30625039/set-border-around-imageview-with-no-background-in-javafx

                    MapFormat map = new MapFormat();
                    map.read(buff);

                    FalloutFile tileList = datFile.getItems().get("art\\tiles\\TILES.LST");

                    ScrollPane sp = new ScrollPane();
                    Pane tileMap = new Pane();

                    try (BufferedReader bf = new BufferedReader(
                            new InputStreamReader(new ByteArrayInputStream(tileList.getData())))) {

                        String name;
                        List<String> names = new ArrayList<>();
                        while ((name = bf.readLine()) != null) {
                            names.add(name);
                        }

                        map.getTiles().forEach((elevation, v) -> {

                            Map<String, Image> tiles = new HashMap<>();

                            for (int i = 0; i < 100 * 100; i++) {
                                if (v.get(i) == 1) continue;

                                //if(v.get(i) > names.size()) throw(...)

                                FalloutFile tile = datFile.getItems().get("art\\tiles\\" + names.get(v.get(i)));

                                Image image = tiles.get(names.get(v.get(i)));

                                if (image == null) {
                                    try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(tile.getData()))) {

                                        FrmHeader frm = FrmFileReader.readFrm(is);
                                        image = frm.getImage(0, 0, false);
                                        tiles.put(names.get(v.get(i)), image);
                                    } catch (Exception ex) {
                                        setInfoMessage("Could not access item data: " + ex.getMessage(), Color.RED);
                                    }
                                }

                                ImageView tileView = new ImageView();
                                tileView.setImage(image);

                                int tileX = (int) Math.ceil(((double) i) / 100); // FIXME: possibly wrong
                                int tileY = i % 100;
                                int x = (100 - tileY - 1) * 48 + 32 * (tileX - 1);
                                int y = tileX * 24 + (tileY - 1) * 12 + 1;

                                tileView.setTranslateX(x);
                                tileView.setTranslateY(y);
                                tileMap.getChildren().add(tileView);
                            }
                        });

                    } catch (IOException | DataFormatException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    int w = 80 * 100;
                    int h = 36 * 100;
                    sp.setHvalue(h);
                    sp.setVvalue(w);
                    sp.setPannable(true);

                    //sp.setFitToHeight(true);
                    //sp.setHbarPolicy(ScrollBarPolicy.ALWAYS);
                    //sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
                    tileMap.setMinHeight(h);
                    tileMap.setMinWidth(w);

                    sp.setContent(tileMap);
                    sp.setHvalue(0.5);
                    sp.setVvalue(0.5);

                    mainPane.setCenter(sp);

                    break;

                default:
                    setInfoMessage("Preview for this filetype is not available.", Color.RED);
                    break;
            }
        });

        //Collections.sort(sortedFiles, Comparator.comparing(FalloutFile::getFilename, String.CASE_INSENSITIVE_ORDER));

        Task task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                List<FalloutFile> sortedFiles = new ArrayList<>(datFile.getItems().values());
                sortedFiles.forEach((file) -> {

                    TreeItem<FalloutDatItem> current = root;

                    for (String component : file.getFilename().split("\\\\")) {
                        current.getChildren().sort((i1, i2) -> Boolean.compare(i2.getValue() instanceof FalloutDirectory, i1.getValue() instanceof FalloutDirectory));
                        if (file.getFilename().endsWith(component)) {
                            // FILE
                            current.getChildren().add(new TreeItem<>(file, getIconForFile(file.getFilename())));
                        } else {
                            // FOLDER
                            current = getOrCreateDir(current, component);
                        }
                    }
                });
                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Helper method for creating hierarchical representation of folder within the DAT archive
     *
     * @param parent    Parent folder
     * @param subfolder Current subfolder
     * @return item if it already exists or creates a new subitem
     */
    private TreeItem<FalloutDatItem> getOrCreateDir(TreeItem<FalloutDatItem> parent, String subfolder) {

        // Is the directory already present in the file tree?
        for (TreeItem<FalloutDatItem> child : parent.getChildren()) {
            if (child.getValue().getFilename().endsWith(subfolder)) {
                return child;
            }
        }

        TreeItem<FalloutDatItem> newChild = new TreeItem<>(new FalloutDirectory(subfolder), getIconForFile(subfolder));
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

    private Glyph getIconForFile(String filename) {

        Glyph icon;

        switch (getFileExtension(filename)) {
            case "":
                icon = iconFolder.duplicate();
                break;
            case "ACM":
                icon = iconSound.duplicate();
                break;
            case "MVE":
                icon = iconMovie.duplicate();
                break;
            case "FRM":
            case "PRO":
            case "RIX":
            case "PAL":
                icon = iconImage.duplicate();
                break;
            case "AAF":
            case "FON":
                icon = iconFont.duplicate();
                break;
            case "SSL":
                icon = iconCode.duplicate();
                break;
            case "INT":
                icon = iconGear.duplicate();
                break;
            case "MAP":
            case "MSK":
                icon = iconMap.duplicate();
                break;
            case "GAM":
            case "LST":
            case "MSG":
            case "SVE":
            case "TXT":
            case "BAK":
            case "BIO":
                icon = iconText.duplicate();
                break;
            case "CFG":
            case "GCD":
            case "LIP":
            default:
                icon = iconFile.duplicate();
                break;
        }

        return icon;
    }

    @FXML
    void openButtonClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Fallout 2 DAT File");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Fallout 2 DAT files", "*.dat"));
        File datFile = fileChooser.showOpenDialog(new Stage());

        if (datFile != null)
            loadDatFile(datFile.toString());
    }

    private void setInfoMessage(String msg, Color color) {

        infoLabel = new Label(msg);
        infoLabel.setTextFill(Color.RED);
        mainPane.setCenter(infoLabel);
    }

}
