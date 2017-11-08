package sample;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import calc.alg.Dbscan;
import calc.alg.model.Cluster;
import com.asprise.util.tiff.MyTIFFReader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import util.ColorUtil;

public class Controller {

    @FXML
    private Canvas picCanvas;
    @FXML
    private Label resultLabel;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Slider colorRSlider;
    @FXML
    private Slider colorGSlider;
    @FXML
    private Slider colorBSlider;
    @FXML
    private CheckBox isColorPickOpen;

    private Image image;

    public void pickPics(ActionEvent actionEvent) {
        Button button = (Button)actionEvent.getTarget();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("请选择图片格式", "*.png", "*.jpg", "*.tif", "*.tiff"));
        File file = fileChooser.showOpenDialog(button.getParent().getScene().getWindow());
        if (file == null) {
            return;
        }
        if (file.getName().endsWith(".tiff") || file.getName().endsWith(".tif")) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
                MyTIFFReader reader = new MyTIFFReader(file);
                RenderedImage image2 = reader.getPage(0);
                ImageIO.write(image2, "png", outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                image = new Image(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FileInputStream fileIn;
            try {
                fileIn = new FileInputStream(file);
                image = new Image(fileIn);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        picCanvas.setWidth(image.getWidth());
        picCanvas.setHeight(image.getHeight());
        System.out.println(image.getWidth() + "," + image.getHeight());
        GraphicsContext g = picCanvas.getGraphicsContext2D();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
        g.save();
        ChangeListener<Number> listener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Color color = colorPicker.getValue();

            }
        };
        colorBSlider.valueProperty().addListener(listener);
        colorGSlider.valueProperty().addListener(listener);
        colorRSlider.valueProperty().addListener(listener);
    }

    public void parsePic(ActionEvent actionEvent) {
        if (image != null) {
            Dbscan dbscan = new Dbscan(25, 8);
            for (int i = 0; i < image.getWidth(); i++) {
                for (int i1 = 0; i1 < image.getHeight(); i1++) {
                    int rgb = image.getPixelReader().getArgb(i, i1);
                    if ((rgb & 0xFF) > 200 && ((rgb >> 8) & 0xFF) > 200 && ((rgb >> 16) & 0xFF) > 200) {
                        continue;
                    }
                    if ((rgb & 0xFF) < 10 && ((rgb >> 8) & 0xFF) < 10 && ((rgb >> 16) & 0xFF) < 10) {
                        continue;
                    }
                    if (match(rgb)) {
                        dbscan.addData(i, i1);
                    }
                }
            }
            java.util.List<Cluster> clusterList = dbscan.dBScan();
            GraphicsContext g = picCanvas.getGraphicsContext2D();
            g.clearRect(0, 0, picCanvas.getWidth(), picCanvas.getHeight());
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
            g.setStroke(Color.YELLOW);
            resultLabel.setText("共" + clusterList.size() + "个");
            for (Cluster cluster : clusterList) {
                //calc.alg.model.Point center = cluster.getCenter();
                int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, bot = 0, right = 0;
                for (calc.alg.model.Point point : cluster.getMembers()) {
                    left = (int)Math.min(left, point.getX());
                    top = (int)Math.min(top, point.getY());
                    bot = (int)Math.max(bot, point.getY());
                    right = (int)Math.max(right, point.getX());
                }
                int r = (int)Math.max(right - left, bot - top);
                g.strokeOval(left - r / 2, top - r / 2, 2 * r, 2 * r);
            }
        }
    }

    public static boolean match(int value) {
        int[] max = new int[] {366, 95, 140};
        int[] min = new int[] {322, 34, 80};
        double[] hsl = ColorUtil.RGB2HSL(ColorUtil.rgbFromInt(value));
        if (hsl[0] <= max[0] && hsl[0] >= min[0] &&
            hsl[1] <= max[1] && hsl[1] >= min[1] &&
            hsl[2] <= max[2] && hsl[2] >= min[2]) { return true; }
        return false;
    }

    public void restore(ActionEvent actionEvent) {
        GraphicsContext g = picCanvas.getGraphicsContext2D();
        g.restore();
    }

    public void colorPicked(Event event) {
        System.out.println(colorPicker.getValue());
    }

    public void missChange(SwipeEvent swipeEvent) {
        System.out.println(swipeEvent.getTarget().getClass().toString());
    }

    public void inPickColorMode(MouseEvent mouseEvent) {
        if (isColorPickOpen.isSelected()) {
            picCanvas.getScene().setCursor(Cursor.CROSSHAIR);
        }
    }

    public void outPickColorMode(MouseEvent mouseEvent) {
        if (isColorPickOpen.isSelected()) {
            picCanvas.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    public void pickColor(MouseEvent mouseEvent) {
        if (image != null && isColorPickOpen.isSelected()) {
            Color color = image.getPixelReader().getColor((int)mouseEvent.getX(), (int)mouseEvent.getY());
            colorPicker.setValue(color);
            colorPicker.getCustomColors().add(color);
        }
    }
}
