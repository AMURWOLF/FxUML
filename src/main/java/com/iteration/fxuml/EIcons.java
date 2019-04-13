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
    HELP_CONTENTS("22x22/help-contents.png"),;

    final private String fileName;

    private EIcons(String _fileName) {
        this.fileName = _fileName;
    }

    public Image getImage() {
        return new Image(this.getClass().getResourceAsStream("/icons/" + this.fileName));
    }

    public ImageView getView() {
        return new ImageView(this.getImage());
    }
}
