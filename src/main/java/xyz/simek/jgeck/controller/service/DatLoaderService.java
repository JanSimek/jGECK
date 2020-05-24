package xyz.simek.jgeck.controller.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import xyz.simek.jgeck.model.DatFile;
import xyz.simek.jgeck.model.format.dat.FalloutDatItem;
import xyz.simek.jgeck.model.format.dat.FalloutDirectory;
import xyz.simek.jgeck.model.format.dat.FalloutFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatLoaderService extends Service<Void> {

    private TreeView<FalloutDatItem> fileTree;
    private DatFile datFile;

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

    public DatLoaderService(TreeView<FalloutDatItem> fileTree, DatFile datFile) {
        this.fileTree = fileTree;
        this.datFile = datFile;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<FalloutFile> sortedFiles = new ArrayList<>(datFile.getItems().values());

                final AtomicInteger progress = new AtomicInteger(1);

                TreeItem<FalloutDatItem> root = fileTree.getRoot();

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
                    updateProgress(progress.getAndAdd(1), sortedFiles.size());
                });
                return null;
            }
        };
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


    /**
     * Extracts file extension from filename
     *
     * @param filename
     * @return extension in UPPERCASE
     */
    private String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot == -1 || filename.length() < (dot + 1)) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
    }
}
