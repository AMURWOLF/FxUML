/***************************************************************************************************
 * Copyright (C) 2018 Iteration
 * License for sources distribution: see file 'LICENSE' in the project's root
 **************************************************************************************************/


package com.iteration.fxuml;



import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;



/**
 * 
 * 
 * @author AMURWOLF
 * @since October 2018
 */
public class DialogUtils {
    
    ///////////////////////////
    ////    F I E L D S    ////
    ///////////////////////////
    
    ////////////////////////////////////////////
    ////    S T A T I C    M E T H O D S    ////
    ////////////////////////////////////////////
    
    public static void showError(Thread t, Throwable e) {
        
        System.err.println("*** DEFAULT  EXCEPTION  HANDLER ***");
        if (Platform.isFxApplicationThread()) {
            DialogUtils.showErrorDialog(e);
        } else {
            System.err.println("An unexpected error occurred in " + t);
        }
    }
    
    
    
    private static void showErrorDialog(Throwable e) {
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Uncaught exception");
        alert.setContentText(e.getMessage());
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("fxuml.css");
        dialogPane.getStyleClass().add("exception");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();
        System.err.println(exceptionText);
        
        Label label = new Label("The exception stacktrace was:");
        
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        
        alert.getDialogPane().setExpandableContent(expContent);
        
        alert.showAndWait();
    }
    
    
    
    public static void showErrorMessage(String header, String content) {
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("fxuml.css");
        dialogPane.getStyleClass().add("exception");

        alert.showAndWait();
    }
    
    ///////////////////////////////////////
    ////    C O N S T R U C T O R S    ////
    ///////////////////////////////////////
    
    /////////////////////////////
    ////    M E T H O D S    ////
    /////////////////////////////
    
    /////////////////////////
    ////    T Y P E S    ////
    /////////////////////////
    
}
