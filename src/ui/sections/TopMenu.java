package ui.sections;

import core.Interpreter;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import ui.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TopMenu extends HBox {

    public TopMenu(TabPane panel, FileTree ft,Terminal terminal) {
        Node runIcon = new ImageView(new Image("ui/assets/run.png"));

        Menu m = new Menu("File");

        MenuItem newFile = new MenuItem("New file");
        MenuItem openFile = new MenuItem("Open file");
        MenuItem saveFile = new MenuItem("Save file");
        MenuItem openFolder = new MenuItem("Open folder");
        m.getItems().add(newFile);
        m.getItems().add(saveFile);
        m.getItems().add(openFile);
        m.getItems().add(openFolder);

        MenuBar mb = new MenuBar();
        mb.getMenus().add(m);

        Button run = new Button("Run", runIcon);

        Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        setHgrow(spacer, Priority.SOMETIMES);

        getChildren().addAll(mb, spacer, run);

        saveFile.setOnAction(e -> Utility.saveTab(panel,ft));

        run.setOnAction(e -> {
            var result=Utility.saveTab(panel,ft);
            if(result) {
                var tab = panel.getSelectionModel().getSelectedItem();
                List<Integer> errors = new ArrayList<>();
                try {
                    errors = Interpreter.x2java(((Editor) (((VirtualizedScrollPane) tab.getContent()).getContent())).getText(), ((EditorTab) tab).getFile().getParentFile().getPath());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (errors.isEmpty()) {
                    try {

                        String command = "javac Interpreted.java";
                        terminal.runCommand(command, ((EditorTab) tab).getFile().getParentFile().getPath());
                        command = "java Interpreted";
                        terminal.runCommand(command, ((EditorTab) tab).getFile().getParentFile().getPath());

                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    for (var error : errors) {
                        terminal.appendText("Error at line:" + error + "\n");
                    }
                }
            }
        });

        openFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("X files (*.X)", "*.X"));
            try {
                FileView file = new FileView(fileChooser.showOpenDialog(null).getPath());
                if (file.isFile()) {
                    if(!ft.getFiles().contains(file.getPath()))
                        try {
                            ft.addItem(file);

                            EditorTab t = new EditorTab(file.getName(), panel, file);
                            panel.getTabs().add(t);
                            panel.getSelectionModel().select(t);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    else{
                        for(var t:panel.getTabs()){
                            var path1=((EditorTab) t).getFile().getPath();
                            var path2=file.getPath();
                            if(path1.equals(path2)) {
                                panel.getSelectionModel().select(t);
                                break;
                            }
                        }
                    }
                }
            }
            catch(Exception ex){
                System.out.println("Can not open file");
            }
        });
//
        newFile.setOnAction(e -> {
            EditorTab t =new EditorTab("new",panel);
            panel.getTabs().add(t);
            panel.getSelectionModel().select(t);
        });
    }
}
