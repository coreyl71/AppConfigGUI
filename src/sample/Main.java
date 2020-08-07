package sample;

import com.lifwear.AppConfig;
import com.lifwear.inter.IConfigCallback;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    // 根布局
    private Pane rootPane;

    // 站点地区
    private CheckBox cbAreaAs, cbAreaNa, cbAreaAll;
    // 站点类型
    private CheckBox cbServerTest, cbServerRelease, cbServerAll;
    // 生产厂商
    private CheckBox cbManJC, cbManRJ, cbManAll;
    // 设备版本
    private CheckBox cbModelT5, cbModelT6, cbModelAll;
    // WiFi热点
    private CheckBox cbWifi1, cbWifi2, cbWifiAll;
    // 选择 APK 文件路径按钮
    private Button btnChooseFile;
    // 源 APK 路径显示框
    private TextField tfFilePath;
    // 全部选中/取消选中
    private CheckBox cbPackageAll;
    // 确定按钮
    private Button btn_confirm;

    // WaitingDialog 中的组件
    private Stage waitingStage;
    private Label lb_wait;
    private VBox waitingPane;
    private Scene waitingScene;
    private ProgressBar pb_packing;

    // 打包完成 Dialog 中的组件
    private Stage doneStage;
    private Label lb_done;
    private StackPane donePane;
    private Scene doneScene;

    // 源 APK 文件路径
    private String srcFilePath = "";

    // 站点地区的参数 list
    private List<String> areaList;

    // 站点类型的参数 list
    private List<String> serverTypeList;

    // 生产厂商的参数 list
    private List<String> manList;

    // 设备版本的参数 list
    private List<String> modelList;

    // WiFi热点的参数 list
    private List<WifiInfo> wifiList;

    private final FileChooser fileChooser = new FileChooser();
    private final Desktop desktop = Desktop.getDesktop();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        // 找控件
        initView(root);
        // 初始化正在打包的进度框
        initWaitingDialog();
        // 初始化打包完成的提示框
        initDoneDialog();
        // 初始化各项参数List
        initParamLists();
        // 设置点击事件监听
        setOnActionListener(primaryStage);
        primaryStage.setTitle("AppConfigGUI");
        //设置窗口的图标.
        primaryStage.getIcons().add(new javafx.scene.image.Image(
                Main.class.getResourceAsStream("/image/logo.png")));
        primaryStage.setScene(new Scene(root, 520, 500));
        primaryStage.show();

        // 调试布局代码
//        waitingStage.showAndWait();
//        doneStage.showAndWait();

    }

    private void initDoneDialog() {
        donePane = new StackPane();
        donePane.setStyle("-fx-background-color:transparent;-fx-padding:10px;");
        // 组件加入面板
        lb_done = new Label("打包已完成，请关闭当前提示框！");
        donePane.getChildren().addAll(lb_done);
        donePane.setAlignment(Pos.CENTER);
        doneScene = new Scene(donePane, 300, 100);
        // 创建另一个stage
        doneStage = new Stage();
        doneStage.setScene(doneScene);
        // 指定 stage 的模式
        doneStage.initModality(Modality.APPLICATION_MODAL);
        doneStage.setTitle("打包完成");
        //设置窗口的图标.
        doneStage.getIcons().add(new javafx.scene.image.Image(
                Main.class.getResourceAsStream("/image/logo.png")));
    }

    private void initWaitingDialog() {
        waitingPane = new VBox();
        waitingPane.setStyle("-fx-background-color:transparent;-fx-padding:10px;");
        // 组件加入面板
        lb_wait = new Label("正在初始化打包资源，请等待...");
        lb_wait.setPadding(new Insets(5, 5, 5, 5));
        pb_packing = new ProgressBar();
        pb_packing.setPadding(new Insets(15, 5, 5, 5));
        pb_packing.prefWidthProperty().setValue(350);
        pb_packing.prefHeightProperty().setValue(20);
        pb_packing.setProgress(0);
        waitingPane.getChildren().addAll(lb_wait, pb_packing);
        // make 2 scenes from 2 panes
        waitingScene = new Scene(waitingPane, 400, 100);
        // 创建另一个stage
        waitingStage = new Stage();
        waitingStage.setScene(waitingScene);
        // 指定 stage 的模式
        waitingStage.initModality(Modality.APPLICATION_MODAL);
        waitingStage.setTitle("正在打包");
        //设置窗口的图标.
        waitingStage.getIcons().add(new javafx.scene.image.Image(
                Main.class.getResourceAsStream("/image/logo.png")));
        // 禁用弹框的关闭按钮
        waitingStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });
    }

    private void initParamLists() {
        if (null == areaList) {
            areaList = new ArrayList<>();
        } else {
            areaList.clear();
        }
        if (null == serverTypeList) {
            serverTypeList = new ArrayList<>();
        } else {
            serverTypeList.clear();
        }
        if (null == manList) {
            manList = new ArrayList<>();
        } else {
            manList.clear();
        }
        if (null == modelList) {
            modelList = new ArrayList<>();
        } else {
            modelList.clear();
        }
        if (null == wifiList) {
            wifiList = new ArrayList<>();
        } else {
            wifiList.clear();
        }
    }

    private void setOnActionListener(Stage stage) {
        /******************拖入文件监听 Start*********************/
        rootPane.setOnDragOver(new EventHandler<DragEvent>() { //node添加拖入文件事件
            public void handle(DragEvent event) {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles()) {
                    File file = dragboard.getFiles().get(0);
//                    if (file.getAbsolutePath().endsWith(".java")) { //用来过滤拖入类型
                    event.acceptTransferModes(TransferMode.COPY);//接受拖入文件
//                    }
                }

            }
        });
        rootPane.setOnDragDropped(new EventHandler<DragEvent>() { //拖入后松开鼠标触发的事件
            public void handle(DragEvent event) {
                // get drag enter file
                Dragboard dragboard = event.getDragboard();
                if (event.isAccepted()) {
                    File file = dragboard.getFiles().get(0); //获取拖入的文件
                    if (file != null) {
                        srcFilePath = file.getAbsolutePath();
                        System.out.println(srcFilePath);
                        tfFilePath.setText(srcFilePath);
                    }
                }
            }
        });
        /******************拖入文件监听 End*********************/
        /******************站点地区 Start*********************/
        cbAreaAs.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbAreaAs:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!areaList.contains("AS")) {
                        areaList.add("AS");
                    }
                } else {
                    areaList.remove("AS");
                }
            }
        });
        cbAreaNa.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbAreaNa:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!areaList.contains("NA")) {
                        areaList.add("NA");
                    }
                } else {
                    areaList.remove("NA");
                }
            }
        });
        cbAreaAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                cbAreaAs.setSelected(newValue);
                cbAreaNa.setSelected(newValue);
            }
        });
        /******************站点地区 End*********************/
        /******************站点类型 Start*********************/
        cbServerTest.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbServerTest:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!serverTypeList.contains("T")) {
                        serverTypeList.add("T");
                    }
                } else {
                    serverTypeList.remove("T");
                }
            }
        });
        cbServerRelease.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbServerRelease:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!serverTypeList.contains("R")) {
                        serverTypeList.add("R");
                    }
                } else {
                    serverTypeList.remove("R");
                }
            }
        });
        cbServerAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                cbServerTest.setSelected(newValue);
                cbServerRelease.setSelected(newValue);
            }
        });
        /******************站点类型 End*********************/
        /******************生产厂商 Start*********************/
        cbManJC.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbManJC:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!manList.contains("JC")) {
                        manList.add("JC");
                    }
                } else {
                    manList.remove("JC");
                }
            }
        });
        cbManRJ.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbManRJ:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!manList.contains("RJ")) {
                        manList.add("RJ");
                    }
                } else {
                    manList.remove("RJ");
                }
            }
        });
        cbManAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                cbManJC.setSelected(newValue);
                cbManRJ.setSelected(newValue);
            }
        });
        /******************生产厂商 End*********************/
        /******************设备版本 Start*********************/
        cbModelT5.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbModelT5:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!modelList.contains("T5")) {
                        modelList.add("T5");
                    }
                } else {
                    modelList.remove("T5");
                }
            }
        });
        cbModelT6.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbModelT6:" + newValue);
                if (newValue) {
                    // 选中，地区列表新增当前项
                    if (!modelList.contains("T6")) {
                        modelList.add("T6");
                    }
                } else {
                    modelList.remove("T6");
                }
            }
        });
        cbModelAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                cbModelT5.setSelected(newValue);
                cbModelT6.setSelected(newValue);
            }
        });
        /******************设备版本 End*********************/
        /******************WiFi热点 Start*********************/
        cbWifi1.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbWifi1:" + newValue);
                if (newValue) {
                    // 选中
                    WifiInfo wifiInfoAdd = new WifiInfo();
                    wifiInfoAdd.setSsid("smartwatch");
                    wifiInfoAdd.setPwd("12345678");
                    wifiList.removeIf(wifiInfo -> "smartwatch".equals(wifiInfo.getSsid()));
                    wifiList.add(wifiInfoAdd);
                } else {
                    wifiList.removeIf(wifiInfo -> "smartwatch".equals(wifiInfo.getSsid()));
                }
            }
        });
        cbWifi2.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("cbWifi2:" + newValue);
                if (newValue) {
                    // 选中
                    WifiInfo wifiInfoAdd = new WifiInfo();
                    wifiInfoAdd.setSsid("HSRG-RJ");
                    wifiInfoAdd.setPwd("HS12345678");
                    wifiList.removeIf(wifiInfo -> "HSRG-RJ".equals(wifiInfo.getSsid()));
                    wifiList.add(wifiInfoAdd);
                } else {
                    wifiList.removeIf(wifiInfo -> "HSRG-RJ".equals(wifiInfo.getSsid()));
                }
            }
        });
        cbWifiAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                cbWifi1.setSelected(newValue);
                cbWifi2.setSelected(newValue);
            }
        });
        /******************WiFi热点 End*********************/
        /******************全参数选择框 End*********************/
        cbPackageAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                cbAreaAll.setSelected(newValue);
                cbServerAll.setSelected(newValue);
                cbManAll.setSelected(newValue);
                cbModelAll.setSelected(newValue);
                cbWifiAll.setSelected(newValue);
            }
        });
        /******************全参数选择框 End*********************/

        // 选择文件按钮点击监听
        btnChooseFile.setOnAction(event -> chooseFile(stage));

        // 确定按钮点击监听
        btn_confirm.setOnAction(event -> calParams());
    }

    /**
     * 选择本机文件
     */
    private void chooseFile(Stage stage) {
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            srcFilePath = file.getAbsolutePath();
            System.out.println(srcFilePath);
            tfFilePath.setText(srcFilePath);
        }
    }

    private List<String[]> paramList;
    private int currentCount = 0;
    private int failCount = 0;
    private int countTotal = 0;

    /**
     * 根据选择的情况，组合打包参数
     */
    private void calParams() {
        if (null == paramList) {
            paramList = new ArrayList<>();
        } else {
            paramList.clear();
        }
        srcFilePath = tfFilePath.getText();
        System.out.println(srcFilePath);
        if (StringUtils.isEmpty(srcFilePath)) {
            System.out.println("文件路径不可为空！");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("错误提示");
            alert.setHeaderText(null);
            alert.setContentText("文件路径不可为空！\n请选择文件或者手动输入文件路径~");
            alert.showAndWait();
            return;
        }

        // 如果未选必选项，则按缺省值来传
        if (modelList.isEmpty()) {
            modelList.add("");
        }
        if (areaList.isEmpty()) {
            areaList.add("");
        }
        if (serverTypeList.isEmpty()) {
            serverTypeList.add("");
        }
        if (manList.isEmpty()) {
            manList.add("");
        }
        if (wifiList.isEmpty()) {
            WifiInfo wifiInfo = new WifiInfo();
            wifiInfo.setSsid("");
            wifiInfo.setPwd("");
            wifiList.add(wifiInfo);
        }

        // 遍历打包参数
        for (String model : modelList) {
            for (String area : areaList) {
                for (String serverType : serverTypeList) {
                    for (String man : manList) {
                        for (WifiInfo wifiInfo : wifiList) {
                            String[] singleParamArray = new String[]{srcFilePath,
                                    "LIFWEAR_MODEL:" + model,
                                    "AREA:" + area,
                                    "SERVER_TYPE:" + serverType,
                                    "MANUFACTURE:" + man,
                                    "WIFI_SSID:" + wifiInfo.getSsid(),
                                    "WIFI_PWD:" + wifiInfo.getPwd()};
                            paramList.add(singleParamArray);
                        }
                    }
                }
            }
        }

        // TODO: 2020/8/7 弹框拦截点击
        currentCount = 0;
        failCount = 0;
        countTotal = paramList.size();
        waitingStage.setTitle("正在打包");
        lb_wait.setText("正在初始化打包资源，请等待...");
        pb_packing.setProgress(0);
        waitingStage.show();
        doneStage.close();

        // 子线程启动任务
        new Thread(() -> AppConfig.multiConfig(paramList, new IConfigCallback() {
            @Override
            public void onCancel() {
                Platform.runLater(() -> {
                    lb_wait.setText("已取消");
                    waitingStage.setTitle("正在打包");
                    waitingStage.close();
                    doneStage.close();
                });

            }

            @Override
            public void onFailed(String s) {
                // TODO: 2020/8/7 单个包失败，后续加进度显示
                Platform.runLater(() -> {
                    // 因为从 0 开始计数，所以先加再减
                    currentCount++;
                    failCount++;
                    int countLeft = countTotal - currentCount;
                    waitingStage.setTitle("正在打包（" + currentCount + "/" + countTotal +"）");
                    lb_wait.setText("已打包 " + currentCount + " 个，剩余 " + countLeft + " 个，失败 " + failCount + " 个，请耐心等待...");
                    pb_packing.setProgress((double)currentCount/(double)countTotal);
                });

            }

            @Override
            public void onSuccess(String s) {
                // TODO: 2020/8/7 单个包完成，后续加进度显示
                Platform.runLater(() -> {
                    // 因为从 0 开始计数，所以先加再减
                    currentCount++;
                    int countLeft = countTotal - currentCount;
                    waitingStage.setTitle("正在打包（" + currentCount + "/" + countTotal +"）");
                    lb_wait.setText("已打包 " + currentCount + " 个，剩余 " + countLeft + " 个，失败 " + failCount + " 个，请耐心等待...");
                    pb_packing.setProgress((double)currentCount/(double)countTotal);
                });

            }

            @Override
            public void onFinished(int i, int i1) {
                Platform.runLater(() -> {
                    //更新JavaFX的主线程的代码放在此处
                    pb_packing.setProgress(1.0);
                    waitingStage.setTitle("正在打包");
                    lb_wait.setText("已完成");
                    waitingStage.close();
                    doneStage.show();
                });
            }

            @Override
            public void onError(String s) {
                Platform.runLater(() -> {
                    waitingStage.setTitle("正在打包");
                    lb_wait.setText("打包失败，请重新执行打包操作");
                    waitingStage.close();
                    doneStage.close();
                });
            }
        })).start();
    }

    private void initView(Parent root) {

        rootPane = (Pane) root.lookup("#rootPane");

        cbAreaAs = (CheckBox) root.lookup("#cbAreaAs");
        cbAreaNa = (CheckBox) root.lookup("#cbAreaNa");
        cbAreaAll = (CheckBox) root.lookup("#cbAreaAll");

        cbServerTest = (CheckBox) root.lookup("#cbServerTest");
        cbServerRelease = (CheckBox) root.lookup("#cbServerRelease");
        cbServerAll = (CheckBox) root.lookup("#cbServerAll");

        cbManJC = (CheckBox) root.lookup("#cbManJC");
        cbManRJ = (CheckBox) root.lookup("#cbManRJ");
        cbManAll = (CheckBox) root.lookup("#cbManAll");

        cbModelT5 = (CheckBox) root.lookup("#cbModelT5");
        cbModelT6 = (CheckBox) root.lookup("#cbModelT6");
        cbModelAll = (CheckBox) root.lookup("#cbModelAll");

        cbWifi1 = (CheckBox) root.lookup("#cbWifi1");
        cbWifi2 = (CheckBox) root.lookup("#cbWifi2");
        cbWifiAll = (CheckBox) root.lookup("#cbWifiAll");

        btnChooseFile = (Button) root.lookup("#btnChooseFile");
        tfFilePath = (TextField) root.lookup("#tfFilePath");

        cbPackageAll = (CheckBox) root.lookup("#cbPackageAll");

        btn_confirm = (Button) root.lookup("#btn_confirm");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
