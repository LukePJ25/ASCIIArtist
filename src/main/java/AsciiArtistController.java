import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;

public class AsciiArtistController {
    private GridPane root;
    private VBox assetItems;
    private GridPane paramItems;
    private GridPane imageDetails;
    private Button selectButton;
    private Button uploadButton;
    private Label paramLabelBrightness;
    private Slider paramSliderBrightness;
    private Spinner<Integer> paramSpinnerBrightness;
    private Label paramLabelContrast;
    private Slider paramSliderContrast;
    private Spinner<Integer> paramSpinnerContrast;
    private Label paramLabelScale;
    private Spinner<Double> paramSpinnerScale;
    private ComboBox<String> formatSel;
    private Label paramLabelSkipLine;
    private Spinner<Integer> paramSpinnerSkipLine;
    private FileChooser imageOpen;
    private Stage primStage;
    private File currentResourcePath;
    private Image selImage;
    private ImageView imgPreview;
    private Label imageDetailsLabel;
    private Label outputDetailsLabel;
    private GridPane outputArea;
    private TextArea outputText;
    private final Processor processor;
    private String returnData;
    private format currentType;

    public AsciiArtistController(Stage stage) {
        primStage = stage;
        processor = new Processor();
    }

    public void syncParamInputs(Slider slider, Spinner<Integer> spinner, int minValue, int maxValue) {
        double sliderMin = slider.getMin();
        double sliderMax = slider.getMax();
        double sliderRange = sliderMax - sliderMin;
        double spinnerRange = (double) maxValue - (double) minValue;
        double scale = spinnerRange / sliderRange;

        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double scaledValue = (newValue.doubleValue() - sliderMin) * scale + (double) minValue;
            spinner.getValueFactory().setValue((int) Math.round(scaledValue));
        });
        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            double scaledValue = (newValue - (double) minValue) / scale + sliderMin;
            slider.setValue(scaledValue);
        });
        formatSel.valueProperty().addListener((obs, oldValue, newValue) -> {
            refreshType();
            ensureVal();
        });
        double initialValue = (slider.getValue() - sliderMin) * scale + (double) minValue;
        spinner.getValueFactory().setValue((int) Math.round(initialValue));
    }

    public void ensureVal() {
        uploadButton.setDisable(true);
        paramSpinnerScale.setDisable(true);
        if (selImage != null) {
            if (currentType != null) {
                uploadButton.setDisable(false);
                paramSpinnerScale.setDisable(false);
            }
        }
    }

    EventHandler<ActionEvent> processImageEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            processImage();
        }

        private void processImage() {
            returnData = processor.callIJConvert(paramSpinnerContrast.getValue(), paramSpinnerBrightness.getValue(), currentType);
            outputText.clear();
            outputText.setText(returnData);
        }
    };

    EventHandler<ActionEvent> selectImageEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            selectImageInterface();
        }

        private void selectImageInterface() {
            imageOpen = new FileChooser();
            imageOpen.setTitle("Select Image File");
            imageOpen.getExtensionFilters().addAll(
                    new ExtensionFilter("Image Files", "*.png", "*.jpg"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = imageOpen.showOpenDialog(primStage);
            if (selectedFile != null) {
                currentResourcePath = selectedFile;
                selImage = new Image(String.valueOf(currentResourcePath.toURI()));
                imgPreview.setImage(selImage);
                imageDetailsLabel.setText(String.valueOf(currentResourcePath.getAbsoluteFile()));
                processor.setRes(currentResourcePath);
                processor.setOutputScale(1);
                updateOutputDetails();
            }
            ensureVal();
        }
    };

    public void refreshType() {
        switch (formatSel.getValue()) {
            case "Alphanumeric":
                currentType = format.alpha;
                break;
            case "Block":
                currentType = format.block;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + formatSel.getValue());
        }
    }

    public GridPane initGetGrid() {
        root = new GridPane();
        assetItems = new VBox(5);
        paramItems = new GridPane();
        imageDetails = new GridPane();

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(200);
        col1.setMaxWidth(200);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(col1, col2);

        selectButton = new Button("Select Image");
        uploadButton = new Button("Generate ASCII Art");
        selectButton.setOnAction(selectImageEvent);
        uploadButton.setOnAction(processImageEvent);

        paramLabelBrightness = new Label("Brightness");
        paramSliderBrightness = new Slider(-1, 1, 0);
        paramSpinnerBrightness = new Spinner<>(-100, 100, 0);
        paramLabelContrast = new Label("Contrast");
        paramSliderContrast = new Slider(-1, 1, 0.01);
        paramSpinnerContrast = new Spinner<>(-100, 100, 1);
        paramLabelScale = new Label("Output Scale");

        paramSpinnerScale = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 2.0, 1.0, 0.05));
        paramSpinnerScale.setPrefWidth(52);
        paramSpinnerScale.setEditable(true);
        paramSpinnerScale.valueProperty().addListener((obs, oldValue, newValue) -> {
            processor.setOutputScale(newValue);
            updateOutputDetails();
        });

        paramLabelSkipLine = new Label("Skip Line Every...");
        paramSpinnerSkipLine = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        paramSpinnerSkipLine.setPrefWidth(52);
        paramSpinnerSkipLine.setEditable(true);
        paramSpinnerSkipLine.valueProperty().addListener((obs, oldValue, newValue) -> {
            processor.setSkipLineEvery(newValue);
        });

        paramSpinnerBrightness.setPrefWidth(52);
        paramSpinnerContrast.setPrefWidth(52);
        paramItems.setPrefWidth(200);

        formatSel = new ComboBox<>();
        formatSel.getItems().add("Alphanumeric");
        formatSel.getItems().add("Block");
        formatSel.setValue("Alphanumeric");
        refreshType();

        outputDetailsLabel = new Label("");

        paramSpinnerBrightness.setEditable(true);
        paramSpinnerContrast.setEditable(true);

        syncParamInputs(paramSliderBrightness, paramSpinnerBrightness, -100, 100);
        syncParamInputs(paramSliderContrast, paramSpinnerContrast, -100, 100);

        assetItems.getChildren().addAll(selectButton, uploadButton);
        paramItems.add(paramLabelBrightness, 0, 0);
        paramItems.add(paramSpinnerBrightness, 1, 0);
        paramItems.add(paramSliderBrightness, 0, 1, 2, 1);
        paramItems.add(paramLabelContrast, 0, 2);
        paramItems.add(paramSpinnerContrast, 1, 2);
        paramItems.add(paramSliderContrast, 0, 3, 2, 1);
        paramItems.add(formatSel, 0, 4, 2, 1);
        paramItems.add(paramLabelScale, 0, 5);
        paramItems.add(paramSpinnerScale, 1, 5);
        paramItems.add(paramLabelSkipLine,0,6);
        paramItems.add(paramSpinnerSkipLine, 1,6);
        paramItems.add(outputDetailsLabel,0,7,2,1);

        imgPreview = new ImageView();
        imgPreview.setFitWidth(200);
        imgPreview.setPreserveRatio(true);
        imageDetailsLabel = new Label();
        imageDetailsLabel.setMaxWidth(200);

        imageDetails.add(imgPreview, 0, 0, 2, 1);
        imageDetails.add(imageDetailsLabel, 0, 1, 1, 1);

        outputArea = new GridPane();
        outputText = new TextArea();
        outputText.setFont(Font.font("Monospaced", 12));
        outputText.setPrefWidth(1800);
        outputText.setPrefHeight(1800);
        outputText.clear();
        outputArea.add(outputText, 0, 1, 1, 1);

        root.setHgap(5);
        root.setVgap(5);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.add(assetItems, 0, 0, 1, 1);
        root.add(paramItems, 0, 1, 1, 1);
        root.add(imageDetails, 0, 2, 1, 1);
        root.add(outputArea, 1, 0, 1, 3);

        ensureVal();
        return root;
    }


    private void updateOutputDetails() {
        String warningText;
        String resol = (processor.getOutputResX() + " x " + processor.getOutputResY());
        if(processor.getNumberOfCharacters() >= 120000) {
            warningText = "\nThis may take some time to process!";
        } else {
            warningText = "";
        }
        outputDetailsLabel.setText("Output Size: \n" + resol + "\nNumber of Characters (approx.):\n" + processor.getNumberOfCharacters() + warningText);
    }
}