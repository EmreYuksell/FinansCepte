package com.finanscepte.desktop;

import com.finanscepte.desktop.ui.FinancialGaugeCanvas;
import com.finanscepte.desktop.ui.SparklineCanvas;
import com.finanscepte.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CepteFinansApp extends Application {

    private Stage primaryStage;
    private final ObjectMapper mapper = new ObjectMapper();
    private final NumberFormat tl = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
    private static final String[] COLORS = {"#e94560","#4ecca3","#f5a623","#7b68ee","#00bcd4","#ff6f61","#66bb6a","#ab47bc"};

    // Tablolar
    private TableView<JsonNode> productTable, transactionTable, budgetTable, notifTable;
    private ObservableList<JsonNode> productList, transactionList, budgetList, notifList;

    // Dashboard özet
    private Label totalIncomeLbl, totalExpenseLbl, balanceLbl, budgetStatusLbl, unreadCountLbl;
    private Label incomeChangeLbl, expenseChangeLbl, balanceChangeLbl;
    private PieChart categoryPie;
    private BarChart<String, Number> monthlyBar;

    // Custom Graphics bileşenleri
    private FinancialGaugeCanvas gaugeCanvas;
    private SparklineCanvas incomeSparkline;
    private SparklineCanvas expenseSparkline;

    // Otomatik yenileme
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> autoRefreshTask;

    // İşlem filtre alanları
    private DatePicker txStartDate, txEndDate;
    private ComboBox<String> txTypeFilter;

    // Bildirim detay paneli
    private Label notifDetailLbl;

    // Bütçe filtre alanları
    private ComboBox<String> budgetMonthFilter, budgetYearFilter;

    @Override
    public void start(Stage s) { primaryStage = s; showLogin(); }

    // ================ LOGIN ================
    private void showLogin() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("CepteFinans");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        VBox form = new VBox(12);
        form.setMaxWidth(380);
        form.setStyle("-fx-background-color: #16213e; -fx-background-radius: 16; -fx-padding: 30;");

        TextField email = new TextField(); email.setPromptText("E-posta"); email.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #eee; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14px;");
        PasswordField pass = new PasswordField(); pass.setPromptText("Şifre"); pass.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #eee; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14px;");

        Button loginBtn = new Button("Giriş Yap");
        loginBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 12; -fx-min-width: 200; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> { if(!email.getText().isEmpty()) showDashboard(); });

        form.getChildren().addAll(new Label("Hoş geldiniz"), email, pass, loginBtn);
        form.getChildren().forEach(n -> { if(n instanceof Label) n.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;"); });

        box.getChildren().addAll(title, form);
        primaryStage.setScene(new Scene(box, 600, 500));
        primaryStage.setTitle("CepteFinans");
        primaryStage.show();
    }

    // ================ DASHBOARD ================
    private void showDashboard() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildTabPane());

        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        // Her 60 saniyede dashboard'u otomatik yenile
        if (autoRefreshTask != null) autoRefreshTask.cancel(false);
        autoRefreshTask = scheduler.scheduleAtFixedRate(
            () -> Platform.runLater(this::refreshDashboard), 60, 60, TimeUnit.SECONDS);
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(15);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("CepteFinans");
        title.getStyleClass().add("top-title");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        unreadCountLbl = new Label("0");
        unreadCountLbl.getStyleClass().add("badge");
        unreadCountLbl.setVisible(false);

        Button logout = new Button("Çıkış");
        logout.getStyleClass().addAll("btn", "logout-btn");
        logout.setOnAction(e -> showLogin());

        bar.getChildren().addAll(title, spacer, unreadCountLbl, logout);
        return bar;
    }

    private TabPane buildTabPane() {
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tab-pane");

        Tab dash = tab("Dashboard", buildDashboard());
        Tab prod = tab("Ürünler", buildProductsView());
        Tab trans = tab("İşlemler", buildTransactionsView());
        Tab budget = tab("Bütçeler", buildBudgetsView());
        Tab notif = tab("Bildirimler", buildNotificationsView());

        tabs.getTabs().addAll(dash, prod, trans, budget, notif);

        // Dashboard seçilince veri çek
        dash.setOnSelectionChanged(e -> { if(dash.isSelected()) refreshDashboard(); });
        trans.setOnSelectionChanged(e -> { if(trans.isSelected()) loadTransactions(); });
        budget.setOnSelectionChanged(e -> { if(budget.isSelected()) loadBudgets(); });
        notif.setOnSelectionChanged(e -> { if(notif.isSelected()) loadNotifications(); });
        prod.setOnSelectionChanged(e -> { if(prod.isSelected()) loadProducts(); });

        refreshDashboard();
        return tabs;
    }

    private Tab tab(String name, javafx.scene.Node content) {
        Tab t = new Tab(name); t.setClosable(false); t.setContent(content); return t;
    }

    // ================ DASHBOARD CONTENT ================
    private ScrollPane buildDashboard() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(20));

        // Özet kartıları
        HBox cards = new HBox(15);
        totalIncomeLbl  = cardValue("");
        totalExpenseLbl = cardValue("");
        balanceLbl      = cardValue("");
        budgetStatusLbl = cardValue("");
        incomeChangeLbl  = changeLbl();
        expenseChangeLbl = changeLbl();
        balanceChangeLbl = changeLbl();
        cards.getChildren().addAll(
            cardBoxWithChange("Toplam Gelir",  totalIncomeLbl,  incomeChangeLbl,  "card-positive"),
            cardBoxWithChange("Toplam Gider",  totalExpenseLbl, expenseChangeLbl, "card-negative"),
            cardBoxWithChange("Net Bakiye",     balanceLbl,      balanceChangeLbl, "card-neutral"),
            cardBox("Bütçe Durumu", budgetStatusLbl, "card-positive")
        );

        // Grafikler (standart JavaFX)
        HBox charts = new HBox(15);
        categoryPie = new PieChart();
        categoryPie.setTitle("Kategori Dağılımı");
        categoryPie.setLabelsVisible(true);
        categoryPie.setPrefSize(400, 300);

        CategoryAxis x = new CategoryAxis(); x.setLabel("Ay");
        NumberAxis y = new NumberAxis(); y.setLabel("Tutar (₺)");
        monthlyBar = new BarChart<>(x, y);
        monthlyBar.setTitle("Aylık Gelir / Gider");
        monthlyBar.setPrefSize(550, 300);
        monthlyBar.setCategoryGap(20);

        charts.getChildren().addAll(categoryPie, monthlyBar);

        // ── Custom Graphics Satırı ────────────────────────────────────────
        // FinancialGaugeCanvas: Canvas + GraphicsContext ile çizilen animasyonlu
        // hız ölçer göstergesi (standart chart kütüphanesi kullanılmamıştır)
        gaugeCanvas = new FinancialGaugeCanvas();

        // SparklineCanvas: Bezier eğrili mini trend grafiği
        incomeSparkline  = new SparklineCanvas("Gelir Trendi",  Color.web("#4ecca3"));
        expenseSparkline = new SparklineCanvas("Gider Trendi", Color.web("#e94560"));

        HBox customRow = new HBox(20);
        customRow.setAlignment(Pos.CENTER_LEFT);

        // Gauge'ı bir çerçeveye koy
        VBox gaugeBox = new VBox(4);
        gaugeBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 10;");
        gaugeBox.getChildren().add(gaugeCanvas);

        // Sparkline'ları dikey sırala
        VBox sparkBox = new VBox(12);
        sparkBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 10;");
        Label sparkTitle = new Label("Son İşlem Trendleri");
        sparkTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;");
        sparkBox.getChildren().addAll(sparkTitle, incomeSparkline, expenseSparkline);

        customRow.getChildren().addAll(gaugeBox, sparkBox);

        main.getChildren().addAll(cards, customRow, charts);
        ScrollPane sp = new ScrollPane(main);
        sp.setFitToWidth(true);
        return sp;
    }

    private VBox cardBox(String title, Label value, String styleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        value.getStyleClass().addAll("card-value", styleClass);
        card.getChildren().addAll(t, value);
        return card;
    }

    private Label cardValue(String text) {
        Label l = new Label(text); return l;
    }

    private VBox cardBoxWithChange(String title, Label value, Label change, String styleClass) {
        VBox card = cardBox(title, value, styleClass);
        card.getChildren().add(change);
        return card;
    }

    private Label changeLbl() {
        Label l = new Label("");
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        return l;
    }

    private String changeStr(double current, double prev) {
        if (prev == 0) return "";
        double pct = (current - prev) / Math.abs(prev) * 100;
        return String.format("%s%.1f%% geçen aya göre", pct >= 0 ? "▲ " : "▼ ", Math.abs(pct));
    }

    private Color changeColor(double current, double prev) {
        if (prev == 0) return Color.GRAY;
        return current >= prev ? Color.web("#4ecca3") : Color.web("#e94560");
    }

    private void refreshDashboard() {
        new Thread(() -> {
            try {
                JsonNode[] tx = ApiClient.get("/api/transactions", JsonNode[].class);
                JsonNode[] bg = ApiClient.get("/api/budgets", JsonNode[].class);
                JsonNode[] nf = ApiClient.get("/api/notifications", JsonNode[].class);

                String curMonth  = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                String prevMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

                double income = 0, expense = 0;
                double prevIncome = 0, prevExpense = 0;
                Map<String, Double> categorySpending = new HashMap<>();
                Map<String, Double> monthlyIncome = new TreeMap<>();
                Map<String, Double> monthlyExpense = new TreeMap<>();

                for (JsonNode t : tx) {
                    double amt = t.get("amount").asDouble();
                    String type = t.get("type").asText();
                    String month = t.has("createdAt") && !t.get("createdAt").isNull()
                        ? t.get("createdAt").asText().substring(0, 7) : curMonth;

                    if (type.equalsIgnoreCase("GELIR")) {
                        income += amt;
                        monthlyIncome.merge(month, amt, Double::sum);
                        if (month.equals(prevMonth)) prevIncome += amt;
                    } else {
                        expense += amt;
                        monthlyExpense.merge(month, amt, Double::sum);
                        if (month.equals(prevMonth)) prevExpense += amt;
                        String cat = t.has("description") && !t.get("description").asText().isEmpty() ? t.get("description").asText() : "Diğer";
                        categorySpending.merge(cat.length() > 12 ? cat.substring(0,12) : cat, amt, Double::sum);
                    }
                }

                double finalIncome = income, finalExpense = expense, finalBalance = income - expense;
                double fPrevIncome = prevIncome, fPrevExpense = prevExpense, fPrevBalance = prevIncome - prevExpense;
                int budgetOver = 0;
                if (bg != null) for (JsonNode b : bg) { if (b.get("spentAmount").asDouble() >= b.get("limitAmount").asDouble()) budgetOver++; }
                int finalBudgetOver = budgetOver;

                List<Double> incomeValues  = new ArrayList<>(monthlyIncome.values());
                List<Double> expenseValues = new ArrayList<>(monthlyExpense.values());

                Platform.runLater(() -> {
                    totalIncomeLbl.setText(tl.format(finalIncome));
                    totalExpenseLbl.setText(tl.format(finalExpense));
                    balanceLbl.setText(tl.format(finalBalance));
                    budgetStatusLbl.setText(finalBudgetOver > 0 ? "⚠ " + finalBudgetOver + " aşıldı" : "✓ Tümü limit içinde");

                    // Değişim yüzdeleri
                    incomeChangeLbl.setText(changeStr(finalIncome, fPrevIncome));
                    expenseChangeLbl.setText(changeStr(finalExpense, fPrevExpense));
                    balanceChangeLbl.setText(changeStr(finalBalance, fPrevBalance));

                    // Custom Graphics
                    gaugeCanvas.update(finalIncome, finalExpense);
                    if (!incomeValues.isEmpty())  incomeSparkline.setData(incomeValues);
                    if (!expenseValues.isEmpty()) expenseSparkline.setData(expenseValues);

                    // Pasta grafik
                    categoryPie.getData().clear();
                    for (Map.Entry<String, Double> e : categorySpending.entrySet())
                        categoryPie.getData().add(new PieChart.Data(e.getKey(), e.getValue()));

                    // Çubuk grafik
                    monthlyBar.getData().clear();
                    XYChart.Series<String, Number> incSeries = new XYChart.Series<>(); incSeries.setName("Gelir");
                    XYChart.Series<String, Number> expSeries = new XYChart.Series<>(); expSeries.setName("Gider");
                    Set<String> months = new TreeSet<>(); months.addAll(monthlyIncome.keySet()); months.addAll(monthlyExpense.keySet());
                    if (months.isEmpty()) months.add(curMonth);
                    for (String m : months) {
                        incSeries.getData().add(new XYChart.Data<>(m, monthlyIncome.getOrDefault(m, 0.0)));
                        expSeries.getData().add(new XYChart.Data<>(m, monthlyExpense.getOrDefault(m, 0.0)));
                    }
                    monthlyBar.getData().addAll(incSeries, expSeries);

                    // Okunmamış bildirim
                    long unread = nf != null ? Arrays.stream(nf).filter(n -> !n.get("read").asBoolean()).count() : 0;
                    unreadCountLbl.setText(String.valueOf(unread));
                    unreadCountLbl.setVisible(unread > 0);
                });
            } catch (Exception ex) { Platform.runLater(() -> {}); }
        }).start();
    }

    // ================ ÜRÜNLER ================
    private VBox buildProductsView() {
        productTable = new TableView<>(); productList = FXCollections.observableArrayList();
        productTable.getColumns().addAll(col("ID","id",220), col("Ad","name",160), col("Fiyat","price",90), col("Kategori","category",120));
        productTable.setItems(productList);

        HBox bar = btnBar(
            btn("Yenile", "btn-success", () -> loadProducts()),
            btn("Yeni Ürün", "btn-primary", () -> showProductDialog(null)),
            btn("Sil", "btn-danger", () -> deleteSelected(productTable, "/api/products/", this::loadProducts))
        );
        return vbox(bar, productTable);
    }

    private void loadProducts() {
        new Thread(() -> {
            try { JsonNode[] d = ApiClient.get("/api/products", JsonNode[].class); Platform.runLater(() -> { productList.clear(); productList.addAll(d); }); } catch (Exception e) {}
        }).start();
    }

    private void showProductDialog(JsonNode existing) {
        Stage d = dialog("Ürün", existing != null);
        GridPane g = formGrid();
        TextField name = field("Ad"); TextField desc = field("Açıklama"); TextField price = field("Fiyat"); TextField cat = field("Kategori");
        if (existing != null) { name.setText(existing.get("name").asText()); desc.setText(existing.has("description")?existing.get("description").asText():""); price.setText(existing.get("price").asText()); cat.setText(existing.get("category").asText()); }
        formRow(g, "Ad", name, 0); formRow(g, "Açıklama", desc, 1); formRow(g, "Fiyat", price, 2); formRow(g, "Kategori", cat, 3);

        Button save = btn("Kaydet", "btn-primary", () -> {
            try { String json = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"price\":%s,\"category\":\"%s\"}", name.getText(), desc.getText(), price.getText(), cat.getText());
                ApiClient.post("/api/products", json); d.close(); loadProducts(); } catch (Exception ex) {}
        });
        g.add(save, 1, 4);
        d.setScene(new Scene(g, 400, 260)); d.show();
    }

    // ================ İŞLEMLER ================
    private VBox buildTransactionsView() {
        transactionTable = new TableView<>(); transactionList = FXCollections.observableArrayList();
        transactionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        transactionTable.getColumns().addAll(col("Tarih","createdAt",160), col("Tür","type",70), col("Tutar","amount",100), col("Açıklama","description",250));
        transactionTable.setItems(transactionList);

        // Çift tıklama ile düzenleme
        transactionTable.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) -> {
                if (item != null) {
                    String type = item.get("type").asText();
                    row.setStyle(type.equalsIgnoreCase("GELIR") ? "-fx-text-fill: #4ecca3;" : "-fx-text-fill: #e94560;");
                }
            });
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) showTransactionDialog(row.getItem()); });
            return row;
        });

        txTypeFilter = new ComboBox<>(FXCollections.observableArrayList("Tümü", "GELIR", "GIDER"));
        txTypeFilter.setValue("Tümü"); txTypeFilter.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #eee;");
        txTypeFilter.setOnAction(e -> loadTransactions());

        txStartDate = new DatePicker(); txStartDate.setPromptText("Başlangıç"); txStartDate.setPrefWidth(140);
        txEndDate   = new DatePicker(); txEndDate.setPromptText("Bitiş");     txEndDate.setPrefWidth(140);
        txStartDate.setOnAction(e -> loadTransactions());
        txEndDate.setOnAction(e -> loadTransactions());

        HBox bar = btnBar(
            btn("Yenile",       "btn-success", this::loadTransactions),
            btn("Yeni İşlem",   "btn-primary", () -> showTransactionDialog(null)),
            btn("Seçileni Sil", "btn-danger",  this::deleteSelectedTransactions),
            btn("CSV Aktar",    "btn-success", this::exportTransactionsCsv),
            txTypeFilter, txStartDate, txEndDate
        );
        return vbox(bar, transactionTable);
    }

    private void loadTransactions() {
        new Thread(() -> {
            try {
                JsonNode[] all = ApiClient.get("/api/transactions", JsonNode[].class);
                String typeVal = txTypeFilter != null ? txTypeFilter.getValue() : "Tümü";
                LocalDate start = txStartDate != null ? txStartDate.getValue() : null;
                LocalDate end   = txEndDate   != null ? txEndDate.getValue()   : null;
                List<JsonNode> filtered = Arrays.stream(all).filter(t -> {
                    if (!"Tümü".equals(typeVal) && !t.get("type").asText().equalsIgnoreCase(typeVal)) return false;
                    if (start != null && t.has("createdAt") && !t.get("createdAt").isNull()) {
                        LocalDate d = LocalDate.parse(t.get("createdAt").asText().substring(0,10));
                        if (d.isBefore(start)) return false;
                    }
                    if (end != null && t.has("createdAt") && !t.get("createdAt").isNull()) {
                        LocalDate d = LocalDate.parse(t.get("createdAt").asText().substring(0,10));
                        if (d.isAfter(end)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());
                Platform.runLater(() -> { transactionList.clear(); transactionList.addAll(filtered); });
            } catch (Exception e) {}
        }).start();
    }

    private void deleteSelectedTransactions() {
        List<JsonNode> selected = new ArrayList<>(transactionTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, selected.size() + " işlem silinecek. Emin misiniz?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(bt -> { if (bt == ButtonType.OK)
            new Thread(() -> { for (JsonNode n : selected) { try { ApiClient.delete("/api/transactions/" + n.get("id").asText()); } catch (Exception ex) {} } Platform.runLater(this::loadTransactions); }).start();
        });
    }

    private void exportTransactionsCsv() {
        try (FileWriter fw = new FileWriter("transactions_export.csv")) {
            fw.write("Tarih,Tür,Tutar,Açıklama\n");
            for (JsonNode t : transactionList) {
                fw.write(String.format("%s,%s,%s,%s\n",
                    t.has("createdAt") ? t.get("createdAt").asText() : "",
                    t.get("type").asText(),
                    t.get("amount").asText(),
                    t.has("description") ? t.get("description").asText().replace(","," ") : ""));
            }
            Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "CSV kaydedildi: transactions_export.csv").show());
        } catch (Exception e) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "CSV hatası: " + e.getMessage()).show());
        }
    }

    private void showTransactionDialog(JsonNode existing) {
        Stage d = dialog("İşlem", existing != null);
        GridPane g = formGrid();
        TextField userId = field("Kullanıcı ID"); TextField amount = field("Tutar");
        ComboBox<String> typeCb = new ComboBox<>(FXCollections.observableArrayList("GELIR", "GIDER"));
        typeCb.setValue("GELIR"); typeCb.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #eee;");
        TextField desc = field("Açıklama");
        if (existing != null) {
            userId.setText(existing.has("userId") ? existing.get("userId").asText() : "");
            amount.setText(existing.get("amount").asText());
            typeCb.setValue(existing.get("type").asText());
            desc.setText(existing.has("description") ? existing.get("description").asText() : "");
        }
        formRow(g, "Kullanıcı", userId, 0); formRow(g, "Tutar", amount, 1); formRowCb(g, "Tür", typeCb, 2); formRow(g, "Açıklama", desc, 3);
        Button save = btn("Kaydet", "btn-primary", () -> {
            try {
                String json = String.format("{\"userId\":\"%s\",\"amount\":%s,\"type\":\"%s\",\"description\":\"%s\"}",
                    userId.getText(), amount.getText(), typeCb.getValue(), desc.getText());
                if (existing != null) ApiClient.put("/api/transactions/" + existing.get("id").asText(), json);
                else ApiClient.post("/api/transactions", json);
                d.close(); loadTransactions();
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
        });
        g.add(save, 1, 4);
        d.setScene(new Scene(g, 400, 260)); d.show();
    }

    // ================ BÜTÇELER ================
    private VBox buildBudgetsView() {
        budgetTable = new TableView<>(); budgetList = FXCollections.observableArrayList();

        TableColumn<JsonNode, String> catCol = col("Kategori","category",120);
        TableColumn<JsonNode, String> limitCol = col("Limit","limitAmount",100);
        TableColumn<JsonNode, String> spentCol = col("Harcanan","spentAmount",100);
        TableColumn<JsonNode, String> progCol = col("Doluluk","spentAmount",180);
        progCol.setCellFactory(tc -> new TableCell<JsonNode, String>() {
            private final ProgressBar pb = new ProgressBar(0);
            { pb.setPrefWidth(160); pb.setPrefHeight(20); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                JsonNode b = getTableRow().getItem();
                double spent = b.get("spentAmount").asDouble(), limit = b.get("limitAmount").asDouble();
                double ratio = limit > 0 ? spent / limit : 0;
                pb.setProgress(Math.min(ratio, 1.0));
                pb.setStyle(ratio >= 0.9 ? "-fx-accent: #e94560;" : "-fx-accent: #4ecca3;");
                setGraphic(pb);
            }
        });
        TableColumn<JsonNode, String> percCol = col("%","spentAmount",60);
        percCol.setCellFactory(tc -> new TableCell<JsonNode, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(""); return; }
                JsonNode b = getTableRow().getItem();
                double spent = b.get("spentAmount").asDouble(), limit = b.get("limitAmount").asDouble();
                setText(limit > 0 ? String.format("%.0f%%", spent/limit*100) : "0%");
                setStyle((spent >= limit) ? "-fx-text-fill: #e94560;" : "-fx-text-fill: #4ecca3;");
            }
        });
        budgetTable.getColumns().addAll(col("Ay","month",50), col("Yıl","year",50), catCol, limitCol, spentCol, progCol, percCol);
        budgetTable.setItems(budgetList);

        // Ay/Yıl filtre
        budgetMonthFilter = new ComboBox<>(FXCollections.observableArrayList("Tümü","1","2","3","4","5","6","7","8","9","10","11","12"));
        budgetMonthFilter.setValue("Tümü"); budgetMonthFilter.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #eee;");
        budgetMonthFilter.setOnAction(e -> loadBudgets());
        int curYear = LocalDate.now().getYear();
        budgetYearFilter = new ComboBox<>(FXCollections.observableArrayList("Tümü", String.valueOf(curYear-1), String.valueOf(curYear), String.valueOf(curYear+1)));
        budgetYearFilter.setValue("Tümü"); budgetYearFilter.setStyle("-fx-background-color: #0f3460; -fx-text-fill: #eee;");
        budgetYearFilter.setOnAction(e -> loadBudgets());

        HBox bar = btnBar(
            btn("Yenile",     "btn-success", this::loadBudgets),
            btn("Yeni Bütçe", "btn-primary", () -> showBudgetDialog(null)),
            btn("Düzenle",    "btn-primary", () -> { JsonNode s = budgetTable.getSelectionModel().getSelectedItem(); if (s != null) showBudgetDialog(s); }),
            btn("Sil",        "btn-danger",  this::deleteBudgetWithConfirm),
            budgetMonthFilter, budgetYearFilter
        );
        return vbox(bar, budgetTable);
    }

    private void deleteBudgetWithConfirm() {
        JsonNode sel = budgetTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Seçili bütçe silinecek. Emin misiniz?", ButtonType.OK, ButtonType.CANCEL);
        a.showAndWait().ifPresent(bt -> { if (bt == ButtonType.OK)
            new Thread(() -> { try { ApiClient.delete("/api/budgets/" + sel.get("id").asText()); Platform.runLater(this::loadBudgets); } catch (Exception ex) {} }).start();
        });
    }

    private void loadBudgets() {
        new Thread(() -> {
            try {
                JsonNode[] all = ApiClient.get("/api/budgets", JsonNode[].class);
                String mf = budgetMonthFilter != null ? budgetMonthFilter.getValue() : "Tümü";
                String yf = budgetYearFilter  != null ? budgetYearFilter.getValue()  : "Tümü";
                List<JsonNode> filtered = Arrays.stream(all).filter(b -> {
                    if (!"Tümü".equals(mf) && b.has("month") && !b.get("month").asText().equals(mf)) return false;
                    if (!"Tümü".equals(yf) && b.has("year")  && !b.get("year").asText().equals(yf))  return false;
                    return true;
                }).collect(Collectors.toList());
                Platform.runLater(() -> { budgetList.clear(); budgetList.addAll(filtered); });
            } catch (Exception e) {}
        }).start();
    }

    private void showBudgetDialog(JsonNode existing) {
        Stage d = dialog("Bütçe", existing != null);
        GridPane g = formGrid();
        TextField userId = field("Kullanıcı ID"), cat = field("Kategori"), limit = field("Limit"), month = field("Ay (1-12)"), year = field("Yıl");
        if (existing != null) {
            userId.setText(existing.has("userId") ? existing.get("userId").asText() : "");
            cat.setText(existing.get("category").asText());
            limit.setText(existing.get("limitAmount").asText());
            month.setText(existing.get("month").asText());
            year.setText(existing.get("year").asText());
        }
        formRow(g, "Kullanıcı", userId, 0); formRow(g, "Kategori", cat, 1); formRow(g, "Limit", limit, 2); formRow(g, "Ay", month, 3); formRow(g, "Yıl", year, 4);
        Button save = btn("Kaydet", "btn-primary", () -> {
            try {
                String json = String.format("{\"userId\":\"%s\",\"category\":\"%s\",\"limitAmount\":%s,\"month\":%s,\"year\":%s}",
                    userId.getText(), cat.getText(), limit.getText(), month.getText(), year.getText());
                if (existing != null) ApiClient.put("/api/budgets/" + existing.get("id").asText(), json);
                else ApiClient.post("/api/budgets", json);
                d.close(); loadBudgets();
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
        });
        g.add(save, 1, 5);
        d.setScene(new Scene(g, 400, 290)); d.show();
    }

    // ================ BİLDİRİMLER ================
    private VBox buildNotificationsView() {
        notifTable = new TableView<>(); notifList = FXCollections.observableArrayList();
        notifTable.getColumns().addAll(col("Tarih","createdAt",160), col("Tip","type",80), col("Mesaj","message",350), col("Durum","read",80));
        notifTable.getStyleClass().add("table-view");
        notifTable.setItems(notifList);

        // Detay paneli
        notifDetailLbl = new Label("Bir bildirim seçin...");
        notifDetailLbl.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px; -fx-padding: 12; -fx-background-color: #16213e; -fx-background-radius: 8; -fx-wrap-text: true;");
        notifDetailLbl.setMaxWidth(Double.MAX_VALUE);
        notifTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                String msg  = sel.has("message") ? sel.get("message").asText() : "";
                String type = sel.has("type")    ? sel.get("type").asText()    : "";
                String date = sel.has("createdAt")? sel.get("createdAt").asText(): "";
                String read = sel.has("read")     ? (sel.get("read").asBoolean() ? "✅ Okundu" : "🔔 Okunmadı") : "";
                notifDetailLbl.setText(String.format("📨  %s\n🔖 Tip: %s   |   📅 %s   |   %s\n\n%s", type, type, date, read, msg));
            }
        });

        HBox bar = btnBar(
            btn("Yenile",          "btn-success", this::loadNotifications),
            btn("Okundu İşaretle", "btn-primary", () -> {
                JsonNode sel = notifTable.getSelectionModel().getSelectedItem();
                if (sel != null) new Thread(() -> { try { ApiClient.patch("/api/notifications/" + sel.get("id").asText() + "/read"); Platform.runLater(this::loadNotifications); } catch (Exception ex) {} }).start();
            }),
            btn("Sil", "btn-danger", () -> deleteSelected(notifTable, "/api/notifications/", this::loadNotifications))
        );
        return vbox(bar, notifTable, notifDetailLbl);
    }

    private void loadNotifications() {
        new Thread(() -> {
            try { JsonNode[] d = ApiClient.get("/api/notifications", JsonNode[].class); Platform.runLater(() -> { notifList.clear(); notifList.addAll(d); });
                long unread = d != null ? Arrays.stream(d).filter(n -> !n.get("read").asBoolean()).count() : 0;
                Platform.runLater(() -> { unreadCountLbl.setText(String.valueOf(unread)); unreadCountLbl.setVisible(unread > 0); });
            } catch (Exception e) {}
        }).start();
    }

    // ================ HELPER METOTLAR ================
    private TableColumn<JsonNode, String> col(String name, String prop, int w) {
        TableColumn<JsonNode, String> c = new TableColumn<>(name);
        c.setPrefWidth(w);
        c.setCellValueFactory(data -> {
            JsonNode n = data.getValue().get(prop);
            if (n == null || n.isNull()) return javafx.beans.binding.Bindings.createStringBinding(() -> "");
            if (n.isNumber()) return javafx.beans.binding.Bindings.createStringBinding(() -> {
                double v = n.asDouble();
                return prop.contains("price") || prop.contains("Amount") || prop.contains("amount") ? tl.format(v) : String.format("%.0f", v);
            });
            return new javafx.beans.property.SimpleStringProperty(n.asText());
        });
        return c;
    }

    private HBox btnBar(javafx.scene.Node... nodes) { HBox b = new HBox(10); b.setPadding(new Insets(12)); b.getChildren().addAll(nodes); return b; }
    private VBox vbox(javafx.scene.Node... children) { VBox v = new VBox(8); v.getChildren().addAll(children); return v; }
    private Button btn(String text, String styleClass, Runnable action) {
        Button b = new Button(text); b.getStyleClass().addAll("btn", styleClass); b.setOnAction(e -> action.run()); return b;
    }

    private Stage dialog(String title, boolean edit) {
        Stage s = new Stage(); s.setTitle(title + (edit ? " Düzenle" : " Ekle"));
        return s;
    }

    private GridPane formGrid() { GridPane g = new GridPane(); g.setPadding(new Insets(20)); g.setHgap(10); g.setVgap(10); return g; }
    private TextField field(String prompt) { TextField f = new TextField(); f.setPromptText(prompt); return f; }
    private void formRow(GridPane g, String label, TextField f, int row) { g.add(lbl(label), 0, row); g.add(f, 1, row); }
    private void formRowCb(GridPane g, String label, ComboBox<?> cb, int row) { g.add(lbl(label), 0, row); g.add(cb, 1, row); }
    private Label lbl(String text) { Label l = new Label(text); l.setStyle("-fx-text-fill: #aaa;"); return l; }

    private void deleteSelected(TableView<?> table, String apiPath, Runnable refresh) {
        JsonNode sel = (JsonNode) table.getSelectionModel().getSelectedItem();
        if (sel != null) new Thread(() -> { try { ApiClient.delete(apiPath + sel.get("id").asText()); Platform.runLater(refresh); } catch (Exception ex) {} }).start();
    }

    public static void main(String[] args) { launch(args); }

    @Override
    public void stop() {
        if (autoRefreshTask != null) autoRefreshTask.cancel(false);
        scheduler.shutdownNow();
        if (gaugeCanvas != null) gaugeCanvas.stop();
    }
}

