package com.iteration.fxuml;

import java.io.File;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main window of the application 'FxUML'.
 *
 * @author AMURWOLF
 * @since October 2018
 */
public class FxUml extends Application {

    private Stage primaryStage;
    private TabPane umlTabPane;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("FxUML started");
        Application.launch(args);
    }

    @Override
    public void start(Stage _primaryStage) {

        Thread.setDefaultUncaughtExceptionHandler(DialogUtils::showError);

        this.primaryStage = _primaryStage;

        ToolBar toolBar = this.createToolBar();
        this.umlTabPane = this.createTabPane();
        this.createNewTabUml(null);

        var layout = new VBox(toolBar, this.umlTabPane);
        layout.setFillWidth(true);

        final var scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add("fxuml.css");
        scene.getStylesheets().add("java-keywords.css");
        this.umlTabPane.prefWidthProperty().bind(scene.widthProperty());
        this.umlTabPane.prefHeightProperty().bind(scene.heightProperty());

        this.primaryStage.setScene(scene);
        this.primaryStage.setTitle("FxUML");
        this.primaryStage.minWidthProperty().setValue(300);
        this.primaryStage.minHeightProperty().setValue(200);
        this.primaryStage.show();

        this.primaryStage.setOnCloseRequest(this::confirmClosingAllTabs);
        this.primaryStage.setOnHidden(e -> Platform.exit());
    }

    /**
     * @param e some closing event. Side effect: could be consumed by some tab.
     * @return true, if each tab confirmed closing.
     */
    private <T extends Event> boolean confirmClosingAllTabs(T e) {

        this.umlTabPane.getTabs()
                .stream()
                .filter(tab -> tab instanceof TabUml)
                .map(tab -> ((TabUml) tab))
                .forEach(tabUml -> tabUml.confirmClosing(e));
        return !e.isConsumed();
    }

    private ToolBar createToolBar() {

        Button newUML = FxUml.newToolButton(EIcons.FILE_NEW, "Create UML in new tab");
        newUML.setOnAction(this::createNewTabUml);

        Button openUML = FxUml.newToolButton(EIcons.FILE_OPEN, "Open UML from file in new tab");
        openUML.setOnAction(this::chooseAndLoadFile);

        Button saveUML = FxUml.newToolButton(EIcons.FILE_SAVE, "Save UML in file");
        saveUML.setOnAction(this::saveFile);

        Button render = FxUml.newToolButton(EIcons.RENDER, "Render the UML to PNG");
        render.setOnAction(this::renderFile);

        Button settings = FxUml.newToolButton(EIcons.SETTINGS, "Edit application settings");
        settings.setOnAction(e -> DialogUtils.showErrorMessage("Not yet implemented", ""));

        Button help = FxUml.newToolButton(EIcons.HELP_CONTENTS, "Show help contents");
        help.setOnAction(e -> DialogUtils.showErrorMessage("Not yet implemented", ""));

        Button exitApp = FxUml.newToolButton(EIcons.APP_EXIT, "Exit the application");
        exitApp.setOnAction(this::exitApp);

        return new ToolBar(
                newUML, openUML, saveUML, render, new Separator(), settings, help,
                new Separator(), exitApp
        );
    }

    private static Button newToolButton(EIcons icon, String... tipText) {

        var button = new Button(null, icon.getView());
        if (tipText.length > 0) {
            var tip = new Tooltip(tipText[0]);
            tip.setShowDelay(new Duration(400));
            button.setTooltip(tip);
        }
        return button;
    }

    private TabPane createTabPane() {
        var tabPane = new TabPane();

        ChangeListener<Tab> tabChanged = (tab, oldTab, newTab) -> {
            this.getCurrentTabUml().ifPresent(TabUml::focus);
        };

        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener(tabChanged);
        return tabPane;
    }

    private Optional<TabUml> getCurrentTabUml() {

        SingleSelectionModel<Tab> selectionModel = this.umlTabPane.getSelectionModel();
        var current = (TabUml) selectionModel.getSelectedItem();
        return Optional.ofNullable(current);
    }

    private TabUml createNewTabUml(ActionEvent e) {

        var newUmlTab = new TabUml(this);
        this.umlTabPane.getTabs().add(newUmlTab);
        SingleSelectionModel<Tab> selectionModel = this.umlTabPane.getSelectionModel();
        selectionModel.select(newUmlTab);
        return newUmlTab;
    }

    private void chooseAndLoadFile(ActionEvent e) {

        var fc = new FileChooser();
        File fileToOpen = fc.showOpenDialog(null);
        if (fileToOpen != null) {
            this.closeCurrentTabIfEmpty();
            TabUml tab = this.createNewTabUml(e);
            tab.openFile(fileToOpen);
        }
    }

    private void closeCurrentTabIfEmpty() {

        this.getCurrentTabUml().ifPresent(currentTab -> {
            if (!currentTab.isModified.get() && (currentTab.filename.getValue() == null)) {
                this.umlTabPane.getTabs().remove(currentTab);
            }
        });
    }

    public void checkIfAllTabsClosed(Event e) {
        if (this.umlTabPane.getTabs().isEmpty()) {
            this.createNewTabUml(null);
        }
    }

    private void saveFile(ActionEvent e) {
        this.getCurrentTabUml().ifPresent(TabUml::saveFile);
        this.getCurrentTabUml().ifPresent(TabUml::focus);
    }

    private void renderFile(ActionEvent e) {
        this.getCurrentTabUml().ifPresent(TabUml::renderAndShow);
    }

    private void exitApp(ActionEvent e) {
        if (this.confirmClosingAllTabs(e)) {
            this.primaryStage.close();
        }
    }
}
