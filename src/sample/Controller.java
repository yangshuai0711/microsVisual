package sample;

import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import calc.dbscan.Dbscan;
import calc.dbscan.model.Cluster;
import calc.dbscan.model.Point;
import com.asprise.util.tiff.MyTIFFReader;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * @author alex.ys
 */
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
    private Slider colorHSlider;
    @FXML
    private Slider colorSSlider;
    @FXML
    private Slider colorLSlider;
    @FXML
    private Slider clusterPixelCount;
    @FXML
    private Slider clusterPixelRadius;
    @FXML
    private CheckBox isColorPickOpen;
    @FXML
    private ScrollPane canvasPane;

    private Image image;
    private String defaultPicPath = null;
    private String defaultConfPath = null;

    public void pickPics(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("图片", "*.png", "*.jpg", "*.tif", "*.tiff"));
        fileChooser.setTitle("请选择图片");
        if (defaultPicPath != null) {
            fileChooser.setInitialDirectory(new File(defaultPicPath));
        }
        File file = fileChooser.showOpenDialog(picCanvas.getScene().getWindow());
        if (file == null) {
            return;
        }
        defaultPicPath = file.getParentFile().getAbsolutePath();
        if (file.getName().endsWith(".tiff") || file.getName().endsWith(".tif")) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
                MyTIFFReader reader = new MyTIFFReader(file);
                RenderedImage image2 = reader.getPage(0);
                ImageIO.write(image2, "png", outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                image = new Image(inputStream);
            } catch (IOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("出错了");
                alert.setContentText(e.getMessage());
                alert.show();
                return;
            }
        } else {
            FileInputStream fileIn;
            try {
                fileIn = new FileInputStream(file);
                image = new Image(fileIn);
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("出错了");
                alert.setContentText("文件不存在");
                alert.show();
                return;
            }
        }
        picCanvas.setWidth(canvasPane.getViewportBounds().getWidth());
        picCanvas.setHeight(canvasPane.getViewportBounds().getHeight());
        GraphicsContext g = picCanvas.getGraphicsContext2D();
        g.drawImage(image, 0, 0, picCanvas.getWidth(), picCanvas.getHeight());
        colorPicker.getCustomColors().addListener(new ListChangeListener<Color>() {
            @Override
            public void onChanged(Change<? extends Color> c) {
                parsePic(null);
            }
        });
        ChangeListener<Number> sliderChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                parsePic(null);
            }
        };
        colorBSlider.valueProperty().addListener(sliderChangeListener);
        colorGSlider.valueProperty().addListener(sliderChangeListener);
        colorRSlider.valueProperty().addListener(sliderChangeListener);
        colorHSlider.valueProperty().addListener(sliderChangeListener);
        colorSSlider.valueProperty().addListener(sliderChangeListener);
        colorLSlider.valueProperty().addListener(sliderChangeListener);
        clusterPixelCount.valueProperty().addListener(sliderChangeListener);
        clusterPixelRadius.valueProperty().addListener(sliderChangeListener);
    }

    private ExecutorService executor = new ThreadPoolExecutor(1, 1,
        0L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<Runnable>(1), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "backgroundThread");
            t.setDaemon(true);
            return t;
        }
    }, new DiscardOldestPolicy());

    public void parsePic(ActionEvent actionEvent) {
        Callable<Object> callable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (image != null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    double scaleX = image.getWidth() / picCanvas.getWidth();
                    double scaleY = image.getHeight() / picCanvas.getHeight();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(true);
                        }
                    });
                    long start = System.currentTimeMillis();
                    Dbscan dbscan = new Dbscan((int)clusterPixelCount.getValue(), (int)clusterPixelRadius.getValue());
                    PixelReader reader = image.getPixelReader();
                    for (int i = 0; i < picCanvas.getWidth(); i++) {
                        for (int i1 = 0; i1 < picCanvas.getHeight(); i1++) {
                            int rgb = reader.getArgb((int)(i * scaleX), (int)(i1 * scaleY));
                            if ((rgb & 0xFF) > 200 && ((rgb >> 8) & 0xFF) > 200 && ((rgb >> 16) & 0xFF) > 200) {
                                continue;
                            }
                            if ((rgb & 0xFF) < 10 && ((rgb >> 8) & 0xFF) < 10 && ((rgb >> 16) & 0xFF) < 10) {
                                continue;
                            }
                            if (match(reader.getColor((int)(i * scaleX), (int)(i1 * scaleY)))) {
                                dbscan.addData(i, i1);
                            }
                        }
                    }
                    final List<Cluster> clusterList = dbscan.dBScan();
                    final double[][] circles = new double[clusterList.size()][4];
                    int index = 0;
                    for (Cluster cluster : clusterList) {
                        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, bot = 0, right = 0;
                        for (Point point : cluster.getMembers()) {
                            left = (int)Math.min(left, point.getX());
                            top = (int)Math.min(top, point.getY());
                            bot = (int)Math.max(bot, point.getY());
                            right = (int)Math.max(right, point.getX());
                        }
                        int r = Math.max(right - left, bot - top);
                        circles[index] = new double[] {left - r / 2, top - r / 2, 2 * r, 2 * r};
                        index++;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(false);
                        }
                    });
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            GraphicsContext g = picCanvas.getGraphicsContext2D();
                            g.clearRect(0, 0, picCanvas.getWidth(), picCanvas.getHeight());
                            g.drawImage(image, 0, 0, canvasPane.getViewportBounds().getWidth(),
                                canvasPane.getViewportBounds().getHeight());
                            g.setStroke(Color.YELLOW);
                            resultLabel.setText("共" + clusterList.size() + "个");
                            for (double[] circle : circles) {
                                g.strokeOval(circle[0], circle[1], circle[2], circle[3]);
                            }
                        }
                    });
                }
                return null;
            }
        };
        executor.submit(callable);
    }

    public boolean match(Color value) {
        for (Color color : colorPicker.getCustomColors()) {
            boolean matchR = value.getRed() >= color.getRed() - colorRSlider.getValue() / 255
                && value.getRed() <= color.getRed() + colorRSlider.getValue() / 255;
            boolean matchG = value.getGreen() >= color.getGreen() - colorGSlider.getValue() / 255
                && value.getGreen() <= color.getGreen() + colorGSlider.getValue() / 255;
            boolean matchB = value.getBlue() >= color.getBlue() - colorRSlider.getValue() / 255
                && value.getBlue() <= color.getBlue() + colorRSlider.getValue() / 255;
            boolean matchH = value.getHue() >= color.getHue() - colorHSlider.getValue()
                && value.getHue() <= color.getHue() + colorHSlider.getValue();
            boolean matchS = value.getSaturation() >= color.getSaturation() - colorSSlider.getValue() / 100
                && value.getSaturation() <= color.getSaturation() + colorSSlider.getValue() / 100;
            boolean matchL = value.getBrightness() >= color.getBrightness() - colorLSlider.getValue() / 100
                && value.getBrightness() <= color.getBrightness() + colorLSlider.getValue() / 100;
            if ((matchR && matchG && matchB) || (matchH && matchS && matchL)) {
                return true;
            }
        }
        return false;
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
            double scaleX = image.getWidth() / picCanvas.getWidth();
            double scaleY = image.getHeight() / picCanvas.getHeight();
            Color color = image.getPixelReader().getColor((int)(mouseEvent.getX() * scaleX),
                (int)(mouseEvent.getY() * scaleY));
            colorPicker.setValue(color);
            colorPicker.getCustomColors().add(color);
        }
    }

    public void saveConfig(ActionEvent actionEvent) {
        List<String> confList = new ArrayList<>();
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == Slider.class) {
                    Slider value = (Slider)field.get(this);
                    confList.add(field.getName() + "=" + value.getValue());
                }
                if (field.getType() == ColorPicker.class) {
                    ColorPicker value = null;
                    value = (ColorPicker)field.get(this);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Color color : value.getCustomColors()) {
                        stringBuilder.append(color.getRed()).append(',').append(color.getGreen()).append(',').append(
                            color.getBlue()).append('#');
                    }
                    if (stringBuilder.length() > 1) {
                        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }
                    confList.add(field.getName() + "=" + stringBuilder.toString());
                }
            }
        } catch (IllegalAccessException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("保存失败:" + e.getMessage());
            alert.show();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("配置文件", "*.conf"));
        fileChooser.setTitle("保存配置文件");
        if (defaultPicPath != null) {
            fileChooser.setInitialDirectory(new File(defaultPicPath));
        }
        File file = fileChooser.showSaveDialog(picCanvas.getScene().getWindow());
        if (file == null) {
            return;
        }
        defaultConfPath = file.getParentFile().getAbsolutePath();
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(file, "utf-8");
            for (String s : confList) {
                printWriter.println(s);
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
            return;
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }

    }

    public void loadConfig(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("配置文件", "*.conf"));
        fileChooser.setTitle("加载配置文件");
        if (defaultPicPath != null) {
            fileChooser.setInitialDirectory(new File(defaultPicPath));
        }
        File file = fileChooser.showOpenDialog(picCanvas.getScene().getWindow());
        if (file == null) {
            return;
        }
        defaultConfPath = file.getParentFile().getAbsolutePath();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String str = null;
            Map<String, String> confMap = new HashMap<>();
            while ((str = reader.readLine()) != null) {
                String[] arr = str.split("=", 2);
                if (arr.length != 2) {
                    continue;
                }
                confMap.put(arr[0], arr[1]);
            }
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType() == Slider.class && confMap.containsKey(field.getName())) {
                    Slider slider = (Slider)field.get(this);
                    try {
                        double value = Double.parseDouble(confMap.get(field.getName()));
                        slider.setValue(value);
                    } catch (NumberFormatException e) {
                    }
                }
                if (field.getType() == ColorPicker.class && confMap.containsKey(field.getName())) {
                    ColorPicker picker = (ColorPicker)field.get(this);
                    picker.getCustomColors().clear();
                    String values = confMap.get(field.getName());
                    if (values == null || values.trim().length() < 1) {
                        continue;
                    }
                    String[] colorArrStrs = values.split("#");
                    for (String arrStr : colorArrStrs) {
                        String[] arr = arrStr.split(",");
                        if (arr.length != 3) {
                            continue;
                        }
                        try {
                            picker.getCustomColors().add(
                                new Color(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]),
                                    Double.parseDouble(arr[2]), 1));
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    private Cursor prevCursor = null;

    private void setBusy(boolean isBusy) {
        if (isBusy) {
            resultLabel.setText("处理中...");
            if (!Cursor.WAIT.equals(picCanvas.getScene().getCursor())) {
                prevCursor = picCanvas.getScene().getCursor();
            }
            picCanvas.getScene().setCursor(Cursor.WAIT);
        } else if (prevCursor != null && !prevCursor.equals(picCanvas.getScene().getCursor())
            && Cursor.WAIT.equals(picCanvas.getScene().getCursor())) {
            picCanvas.getScene().setCursor(prevCursor);
        }
    }
}
