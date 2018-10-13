/***************************************************************************************************
 * Copyright (C) 2018 Iteration
 * License for sources distribution: see file 'LICENSE' in the project's root
 **************************************************************************************************/


package com.iteration.fxuml;



import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



/**
 * Icons for buttons.
 * 
 * @author AMURWOLF (Dmitry Malkov)
 * @since October 2018
 */
@SuppressWarnings("javadoc")
public enum EIcons {
    
    FILE_NEW("22x22/document-new.png"),
    FILE_OPEN("22x22/document-open.png"),
    FILE_SAVE("22x22/document-save.png"),
    APP_EXIT("22x22/application-exit.png"),
    RENDER("22x22/render.png"),
    SETTINGS("22x22/system-settings.png"),
    HELP_CONTENTS("22x22/help-contents.png"),
    ;
    
    ///////////////////////////
    ////    F I E L D S    ////
    ///////////////////////////
    
    final private String fileName;
    
    ////////////////////////////////////////////
    ////    S T A T I C    M E T H O D S    ////
    ////////////////////////////////////////////
    
    
    
    ///////////////////////////////////////
    ////    C O N S T R U C T O R S    ////
    ///////////////////////////////////////
    
    private EIcons(String _fileName) {
        this.fileName = _fileName;
    }
    
    
    
    /////////////////////////////
    ////    M E T H O D S    ////
    /////////////////////////////
    
    public Image getImage() {
        return new Image(this.getClass().getResourceAsStream("/icons/" + this.fileName));
    }
    
    
    
    public ImageView getView() {
        return new ImageView(this.getImage());
    }
    
    /////////////////////////
    ////    T Y P E S    ////
    /////////////////////////
}
