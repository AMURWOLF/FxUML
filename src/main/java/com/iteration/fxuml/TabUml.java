/***************************************************************************************************
 * Copyright (C) 2018 Iteration
 * License for sources distribution: see file 'LICENSE' in the project's root
 **************************************************************************************************/


package com.iteration.fxuml;



import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sourceforge.plantuml.SourceStringReader;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;



/**
 * TabUml for PlantUML's text.
 * 
 * @author AMURWOLF
 * @since October 2018
 */
public class TabUml extends Tab {
    
    ///////////////////////////
    ////    F I E L D S    ////
    ///////////////////////////
    
    private CodeArea codeArea = new CodeArea();
    
    /** Absolute path of file where you saved/read the code of UML */
    public StringProperty filename = new SimpleStringProperty();
    
    /** Indicator for unsaved changes in editor */
    public BooleanProperty isModified = new SimpleBooleanProperty(false);
    
    private static final String[] KEYWORDS = new String[] {
            "startuml", "enduml",
            "start", "stop", 
            "if", "then", "else", "endif"
    };
    
    private static final String KEYWORD_PATTERN   = "\\b(" + String.join("|", TabUml.KEYWORDS)
            + ")\\b";
    private static final String PAREN_PATTERN     = "\\((.|\\R)*?\\)";
    private static final String ACTION_PATTERN   = ":(.|\\R)*?\\;";
    
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + TabUml.KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + TabUml.PAREN_PATTERN + ")"
                    + "|(?<ACTION>" + TabUml.ACTION_PATTERN + ")"
    );
    
    ////////////////////////////////////////////
    ////    S T A T I C    M E T H O D S    ////
    ////////////////////////////////////////////
    
    
    
    ///////////////////////////////////////
    ////    C O N S T R U C T O R S    ////
    ///////////////////////////////////////
    
    public TabUml(FxUml parent) {
        
        this.setText("New UML");
        this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.codeArea));
        this.codeArea
                .multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(
                        ignore -> this.codeArea
                                .setStyleSpans(0, TabUml.computeHighlighting(this.codeArea.getText()))
                );
        
        this.codeArea.replaceText(0, 0, "@startuml\n\n@enduml\n");
        
        this.setContent(new StackPane(new VirtualizedScrollPane<>(this.codeArea)));
        Platform.runLater(() -> this.codeArea.requestFocus());
        this.codeArea.setOnKeyReleased(this::handleKeyPress);
        this.codeArea.textProperty()
                .addListener((observable, oldValue, newValue) -> this.isModified.set(true));
        
        this.isModified.addListener((observable, oldValue, newValue) -> {
            if (oldValue.equals(newValue)) {
                return;
            }
            
            if (newValue.booleanValue()) {
                this.setText(this.getText() + "*");
            } else {
                String name = this.getText();
                if (name.endsWith("*")) {
                    this.setText(name.substring(0, name.length() - 1));
                }
            }
        });
        
        this.setOnCloseRequest(this::confirmClosing);
        this.setOnClosed(parent::checkIfAllTabsClosed);
        
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        contextMenu.getItems().addAll(cut, copy, paste);
        this.codeArea.setContextMenu(contextMenu);
        
    }
    
    
    
    /////////////////////////////
    ////    M E T H O D S    ////
    /////////////////////////////
    
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = TabUml.PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher
                    .group("KEYWORD") != null ? "keyword"
                            : matcher.group("PAREN") != null ? "paren" 
                                    : matcher.group("ACTION") != null ? "action" 
                                            : null;
            /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    
    
    
    public void openFile(File fileToOpen) {
        
        this.loadTextFromFile(fileToOpen);
        this.filename.set(fileToOpen.getAbsolutePath());
        this.setText(fileToOpen.getName());
        this.isModified.set(false);
    }
    
    
    
    private void loadTextFromFile(File source) {
        
        this.codeArea.clear();
        try (
                var fstream = new FileInputStream(source);
                var reader = new BufferedReader(new InputStreamReader(fstream, "UTF8"));
        ) {
            String str;
            while ((str = reader.readLine()) != null) {
                this.codeArea.appendText(str + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void confirmClosing(Event e) {
        
        if (!this.isModified.get()) {
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("The UML was modified");
        alert.setHeaderText("Close the tab?");
        alert.setContentText("If yes, all changes will be forgotten");
        
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
        yesButton.setDefaultButton(false);
        
        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
        noButton.setDefaultButton(true);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.NO) {
            e.consume();
        }
    }
    
    
    
    public boolean saveFile() {
        
        if (this.filename.getValue() == null) {
            return this.saveFileAs();
        } else {
            return this.saveFileRev();
        }
    }
    
    
    
    private boolean saveFileRev() {
        
        if (!this.isModified.get()) {
            return true;
        }
        
        boolean success = false;
        var file = new File(this.filename.getValue());
        try (
                var fos = new FileOutputStream(file);
                var bos = new BufferedOutputStream(fos)
        ) {
            bos.write(this.codeArea.getText().getBytes());
            bos.flush();
            success = true;
            return success;
        } catch (Exception e) {
            success = false;
            System.out.println("File save failed (error: " + e.getLocalizedMessage() + ")");
            e.printStackTrace();
            return success;
        } finally {
            if (success) {
                this.setText(file.getName());
                this.isModified.set(false);
            }
        }
    }
    
    
    
    private boolean saveFileAs() {
        
        var fc = new FileChooser();
        File newFile = fc.showSaveDialog(null);
        if (newFile != null) {
            if (!newFile.getName().contains(".")) {
                String newFilePath = newFile.getAbsolutePath();
                newFilePath += ".wsd";
                newFile.delete();
                newFile = new File(newFilePath);
            }
            this.filename.set(newFile.getAbsolutePath());
            this.setText(newFile.getName());
            return this.saveFileRev();
        } else {
            return false;
        }
    }
    
    
    
    public void focus() {
        Platform.runLater(() -> this.codeArea.requestFocus());
    }
    
    
    
    /**
     * Define hot keys
     * 
     * @param ke
     *            key event
     */
    private void handleKeyPress(KeyEvent ke) {
        if (ke.isControlDown() && ke.getCode().toString().equalsIgnoreCase("s")) {
            this.saveFile();
        }
    }
    
    
    
    /**
     * Compile the code of UML into PNG image and shows results in a new window on success.
     * 
     * @return success
     */
    public boolean renderAndShow() {
        
        if (this.codeArea.getText().isEmpty()) {
            DialogUtils.showErrorMessage("Пустая вкладка", "");
            return false;
        }
        
        if (!this.saveFile()) {
            return false;
        }
        
        String fileName = this.filename.getValue();
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        fileName += ".png";
        File fileOutput = new File(fileName);
        
        boolean rendered = TabUml.render(this.codeArea.getText(), fileOutput);
        if (rendered) {
            TabUml.showImageFileInDialog(fileOutput);
        }
        return rendered;
    }
    
    
    
    private static boolean render(String input, File output) {
        
        String result = null;
        try {
            SourceStringReader reader = new SourceStringReader(input);
            result = reader.generateImage(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result != null;
    }
    
    
    
    private static void showImageFileInDialog(File fileWithImage) {
        
        final Stage imageDialogStage = new Stage();
        imageDialogStage.initModality(Modality.APPLICATION_MODAL);
        imageDialogStage.setTitle(fileWithImage.getPath());
        
        Image img = new Image("file:" + fileWithImage.getPath());
        ImageView imgView = new ImageView(img);
        
        BorderPane pane = new BorderPane();
        imgView.setPreserveRatio(true);
        imgView.fitWidthProperty().bind(imageDialogStage.widthProperty());
        imgView.fitHeightProperty().bind(imageDialogStage.heightProperty());
        pane.setCenter(imgView);
        
        Scene imageDialogScene = new Scene(pane);
        imageDialogStage.setScene(imageDialogScene);
        imageDialogStage.show();
    }
    
    /////////////////////////
    ////    T Y P E S    ////
    /////////////////////////
    
}
