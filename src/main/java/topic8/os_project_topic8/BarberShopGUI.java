package topic8.os_project_topic8;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatterBuilder;

public class BarberShopGUI extends Application {
    private BarberShop barberShop;
    private Thread simulationThread;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Barber Shop Simulation");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label chairLabel = new Label("Number of Chairs:");
        TextField chairTextField = new TextField();
        chairTextField.setMaxWidth(100);

        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(chairLabel, chairTextField);

        root.getChildren().add(inputBox);

        Label simulationLabel = new Label("Simulation Status");
        TextArea simulationTextArea = new TextArea();
        simulationTextArea.setEditable(false);
        simulationTextArea.setPrefSize(500, 200);

        Button startButton = new Button("Start Simulation");
        startButton.setOnAction(event -> {
            int numChairs = Integer.parseInt(chairTextField.getText());
            barberShop = new BarberShop(numChairs);
            simulate(barberShop, simulationTextArea);
        });


        root.getChildren().addAll(simulationLabel, simulationTextArea, startButton);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void simulate(BarberShop barberShop, TextArea simulationTextArea) {
        simulationThread = new Thread(() -> {
            barberShop.runBarberShop(simulationTextArea);
        });
        simulationThread.start();
    }

}
