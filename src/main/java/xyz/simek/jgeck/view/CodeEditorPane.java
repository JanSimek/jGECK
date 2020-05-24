package xyz.simek.jgeck.view;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.scene.layout.BorderPane;
import xyz.simek.jgeck.model.highlighter.Highlighter;
import xyz.simek.jgeck.model.highlighter.IniHighlighter;

public class CodeEditorPane extends BorderPane {

    private CodeArea codeEditor = new CodeArea();
    private Highlighter highlighter;

    public enum Format {
        INI, TXT, SSL
    }

    public CodeEditorPane(Format format, String text) {

        try {
            VirtualizedScrollPane<CodeArea> vPane = new VirtualizedScrollPane<>(codeEditor);

            switch (format) {
                case INI:
                    highlighter = new IniHighlighter(codeEditor);
                    codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
                    break;
                case SSL:
                    codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));
                    break;
                case TXT:
                    break;
            }

            if (highlighter != null)
                highlighter.highlight();

            codeEditor.replaceText(text);

            vPane.scrollToPixel(0.0, 0.0);

            this.setCenter(vPane);

        } finally {

        }
        //catch (DataFormatException | IOException ex) {
        //	setInfoMessage("Could not get item data: " + ex.getMessage(), Color.RED);
        //}
    }
}
