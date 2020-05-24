package xyz.simek.jgeck.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;
import xyz.simek.jgeck.controller.service.DatLoaderService;
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

    private DatFile datFile = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void loadDatFile(String path) {

        try {
            datFile = new DatFile(path);
        } catch (IOException ex) {
            setInfoMessage("Could not open DAT file: " + ex.getMessage(), Color.RED);
            return;
        }

        fileTree.setShowRoot(false);
        TreeItem<FalloutDatItem> root = new TreeItem<>();
        fileTree.setRoot(root);

        fileTree.setOnKeyReleased(e -> handleSelect());
        fileTree.setOnMouseClicked(e -> handleSelect());

        //Collections.sort(sortedFiles, Comparator.comparing(FalloutFile::getFilename, String.CASE_INSENSITIVE_ORDER));

        DatLoaderService datLoaderService = new DatLoaderService(fileTree, datFile);

        final VBox vBox = new VBox();
        Label loadingText = new Label("Reading DAT file");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.progressProperty().bind(datLoaderService.progressProperty());
        vBox.setSpacing(5);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(loadingText, progressIndicator);
        mainPane.setCenter(vBox);

        datLoaderService.start();
        datLoaderService.setOnSucceeded(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(3000), vBox);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            ft.setOnFinished(ev -> mainPane.getChildren().remove(vBox));
        });

    }

    private void handleSelect() {
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
