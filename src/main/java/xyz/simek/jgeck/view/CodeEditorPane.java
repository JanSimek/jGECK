package xyz.simek.jgeck.view;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.scene.layout.BorderPane;
import xyz.simek.jgeck.model.Highlighter;
import xyz.simek.jgeck.model.IniHighlighter;

public class CodeEditorPane extends BorderPane {

	private CodeArea codeEditor = new CodeArea();
	private Highlighter highlighter;
	
	public CodeEditorPane(String text) {

		try {
	        VirtualizedScrollPane<CodeArea> vPane = new VirtualizedScrollPane<>(codeEditor);


			codeEditor.setParagraphGraphicFactory(LineNumberFactory.get(codeEditor));

	        highlighter = new IniHighlighter(codeEditor);        
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
