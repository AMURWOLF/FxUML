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
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sourceforge.plantuml.SourceStringReader;



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
    
    private TextArea textArea = new TextArea();
    
    /** Absolute path of file where you saved/read the code of UML */
    public StringProperty filename = new SimpleStringProperty();
    
    /** Indicator for unsaved changes in editor */
    public BooleanProperty isModified = new SimpleBooleanProperty(false);
    
    ////////////////////////////////////////////
    ////    S T A T I C    M E T H O D S    ////
    ////////////////////////////////////////////
    
    
    
    ///////////////////////////////////////
    ////    C O N S T R U C T O R S    ////
    ///////////////////////////////////////
    
    public TabUml(FxUml parent) {
        
        this.setText("New UML");
        this.textArea.setText("@startuml\n\n@enduml\n\n");
        
        this.setContent(this.textArea);
        Platform.runLater(() -> this.textArea.requestFocus());
        this.textArea.setOnKeyReleased(this::handleKeyPress);
        this.textArea.textProperty()
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
    }
    
    /////////////////////////////
    ////    M E T H O D S    ////
    /////////////////////////////
    
    
    
    public void openFile(File fileToOpen) {
        
        this.loadTextFromFile(fileToOpen);
        this.filename.set(fileToOpen.getAbsolutePath());
        this.setText(fileToOpen.getName());
        this.isModified.set(false);
    }
    
    
    
    private void loadTextFromFile(File source) {
        
        this.textArea.clear();
        try (
                var fstream = new FileInputStream(source);
                var reader = new BufferedReader(new InputStreamReader(fstream, "UTF8"));
        ) {
            String str;
            while ((str = reader.readLine()) != null) {
                this.textArea.appendText(str + "\n");
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
            bos.write(this.textArea.getText().getBytes());
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
        Platform.runLater(() -> this.textArea.requestFocus());
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
        
        if (this.textArea.getText().isEmpty()) {
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
        
        boolean rendered = TabUml.render(this.textArea.getText(), fileOutput);
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
