package ImageProcessing;
import javafx.application.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Interface extends Application{



   public static void main(String[] args) {

       launch(args);
    }
    Button inv;
    Button gammacorr;
    Button grayscale;
    Button erosion;
    Button blur;
    Button reset;
    Button edgedetect;
    Button constr;
    Button dilation;
    MenuBar menubar;
    ImageView pic;
    Engine eng;
    private String path;


    public Interface(){
        eng = new Engine();
        pic = new ImageView();
    }

    @Override
    public void start(Stage window){
        inv = new Button();
        inv.setText("Inversion");
        inv.setMaxSize(130,130);
        gammacorr = new Button();
        gammacorr.setText("Gamma Correction");
        gammacorr.setMaxSize(130,130);
        grayscale = new Button();
        grayscale.setText("Grayscale");
        grayscale.setMaxSize(130,130);
        blur = new Button();
        blur.setText("Gaussian Blur");
        blur.setMaxSize(130,130);
        edgedetect = new Button();
        edgedetect.setText("Edge Detection");
        edgedetect.setMaxSize(130,130);
        constr = new Button();
        constr.setText("Contrast Stretching");
        constr.setMaxSize(130,130);
        dilation = new Button();
        dilation.setText("Dilation");
        dilation.setMaxSize(130,130);
        erosion = new Button();
        erosion.setText("Erosion");
        erosion.setMaxSize(130,130);
        reset = new Button();
        reset.setText("Reset");
        reset.setMaxSize(130,130);
        menubar = new MenuBar();
        Menu menu1 = new Menu("File");
        MenuItem load = new MenuItem("Load");
        MenuItem quit = new MenuItem("Quit");
        menu1.getItems().add(load);
        menu1.getItems().add(quit);
        menubar.getMenus().add(menu1);

        menu1.getItems().get(0).setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();

                FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
                FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
                fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

                File file = fileChooser.showOpenDialog(null);

                try {
                    String pathEng = file.getPath().toString().replace("\\", "\\\\");
                    path = pathEng;
                    eng.load(pathEng);
                    BufferedImage bufferedImage = eng.getBuffImg();
                    refreshImage(bufferedImage);
                } catch (IOException ex) {
                    System.out.println("Wrong format");
                }
            } catch (Exception exc){
                System.out.println("No file selected");
            }
        });

        menu1.getItems().get(1).setOnAction(e -> {
            Platform.exit();
        });

        inv.setOnAction(event -> {eng.Inversion();refreshImage(eng.getBuffImg());});
        gammacorr.setOnAction(event -> {gammaSlider();});
        grayscale.setOnAction(event -> {eng.Grayscale();refreshImage(eng.getBuffImg());});
        blur.setOnAction(event -> {eng.GaussBlur();refreshImage(eng.getBuffImg());});
        edgedetect.setOnAction(event -> {eng.EdgeDetection();refreshImage(eng.getBuffImg());});
        dilation.setOnAction(event -> {eng.applyDilation();refreshImage(eng.getBuffImg());});
        erosion.setOnAction(event -> {eng.applyErosion();refreshImage(eng.getBuffImg());});
        constr.setOnAction(event -> {contrastStretch();});
        reset.setOnAction(event -> reset());

        pic = new ImageView();
        pic.setFitWidth(900);
        pic.setFitHeight(700);

        HBox buttons = new HBox(inv,gammacorr,grayscale,blur,edgedetect,constr,dilation,erosion,reset);
        buttons.setSpacing(10);

        VBox img = new VBox(pic);

        VBox box = new VBox(menubar,buttons,img);
        VBox.setMargin(buttons, new Insets(10,10,10,10));
        pic.fitWidthProperty().bind(box.widthProperty());
        //pic.fitHeightProperty().bind(box.heightProperty());
        pic.preserveRatioProperty();
        Scene scene = new Scene(box, 1000,800);
        window.setResizable(false);
        window.setScene(scene);
        window.setTitle("Image processing");
        window.show();
    }

    private void gammaSlider() {
        Stage stage = new Stage();
        stage.setTitle("Gamma");
        Slider slider = new Slider(0.1, 5, 0);
        Button apply = new Button();
        apply.setText("Apply");
        apply.setMaxSize(100,100);
        slider.setBlockIncrement(0.1);
        slider.setMajorTickUnit(0.1);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);

        slider.valueProperty().addListener((observable, a, b) -> {
            double val = b.doubleValue();
            eng.GammaCorrection(val);
            refreshImage(eng.getBuffImg());
            //System.out.println("Slider: " + val);
        });
        apply.setOnAction(event -> {eng.replaceCopy();});
        HBox btn = new HBox(apply);
        btn.setAlignment(Pos.CENTER);
        VBox pane = new VBox(slider,btn);
        Scene gammaScene = new Scene(pane, 200, 200);
        stage.setResizable(false);

        stage.setScene(gammaScene);
        stage.show();
    }

    private void contrastStretch() {
        Stage stage = new Stage();
        stage.setTitle("Contrast Stretching");
        Label lbs1 = new Label();
        Label lbr1 = new Label();
        Label lbs2 = new Label();
        Label lbr2 = new Label();
        lbs1.setText("X1: ");
        lbr1.setText("Y1: ");
        lbs2.setText("X2: ");
        lbr2.setText("Y2: ");
        TextField s1 = new TextField();
        TextField r1 = new TextField();
        TextField s2 = new TextField();
        TextField r2 = new TextField();
        Button send = new Button();
        send.setText("Apply");
        send.setMaxSize(100,100);

        send.setOnAction(event -> {
            if((s1.getText().length()==0) || (r1.getText().length()==0) || (s2.getText().length()==0) || (r2.getText().length()==0)){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("All text fields are required.");
                alert.showAndWait();
            }
            else{
                float x1 = Float.parseFloat(s1.getText());
                float y1 = Float.parseFloat(r1.getText());
                float x2 = Float.parseFloat(s2.getText());
                float y2 = Float.parseFloat(r2.getText());
                eng.ContrastStretching(x1,y1,x2,y2);
                refreshImage(eng.getBuffImg());
            }
        });

        HBox S1 = new HBox(lbs1,s1);
        HBox R1 = new HBox(lbr1,r1);
        HBox S2 = new HBox(lbs2,s2);
        HBox R2 = new HBox(lbr2,r2);
        HBox btn = new HBox(send);
        btn.setAlignment(Pos.CENTER);
        VBox pane = new VBox(S1,R1,S2,R2,btn);
        Scene constretch = new Scene(pane,200,200);
        stage.setScene(constretch);
        stage.setResizable(false);
        stage.show();
    }

    private void refreshImage(BufferedImage img) {
        Image image = SwingFXUtils.toFXImage(img, null);
        pic.setImage(image);
    }

    private void reset(){
        try{
            eng.load(path);
        } catch (IOException e){
            System.out.println("Failed");
        }
        BufferedImage bufferedImage = eng.getBuffImg();
        refreshImage(bufferedImage);
    }

}


