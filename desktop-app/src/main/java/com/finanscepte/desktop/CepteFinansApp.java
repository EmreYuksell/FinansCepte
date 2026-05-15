package com.finanscepte.desktop;

import com.finanscepte.desktop.ui.FinancialGaugeCanvas;
import com.finanscepte.desktop.ui.SparklineCanvas;
import com.finanscepte.desktop.util.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final String[] COLORS = {"#4f46e5","#10b981","#f59e0b","#6366f1","#00bcd4","#ff6f61","#66bb6a","#ab47bc","#06b6d4","#ffcc00"};

    // UI Containers
    private BorderPane mainRoot;
    private VBox sidebar;
    private StackPane contentArea;
    private Button activeSidebarBtn;
    private Label unreadBadge;
    private Label sidebarNotifBadge;

    // Data lists
    private ObservableList<JsonNode> transactionList = FXCollections.observableArrayList();
    private ObservableList<JsonNode> budgetList = FXCollections.observableArrayList();
    private ObservableList<JsonNode> productList = FXCollections.observableArrayList();
    private ObservableList<JsonNode> notifList = FXCollections.observableArrayList();
    private ObservableList<JsonNode> accountList = FXCollections.observableArrayList();
    private ObservableList<JsonNode> assetList = FXCollections.observableArrayList();
    private ObservableList<JsonNode> goalList = FXCollections.observableArrayList();

    // Dashboard refs
    private Label totalIncomeLbl, totalExpenseLbl, balanceLbl, budgetStatusLbl, savingsLbl, dailyAvgLbl;
    private Label incomeChangeLbl, expenseChangeLbl, balanceChangeLbl, budgetChangeLbl, savingsChangeLbl, dailyChangeLbl;
    private PieChart categoryPie;
    private BarChart<String, Number> monthlyBar;
    private LineChart<String, Number> trendLine;
    private TableView<JsonNode> recentTxTable;
    private FinancialGaugeCanvas financialGauge;
    private SparklineCanvas reportSparkline;

    // Otomatik yenileme
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> autoRefreshTask;

    @Override
    public void start(Stage s) {
        primaryStage = s;
        primaryStage.setTitle("CepteFinans - Profesyonel Finans Takibi");
        showLogin();
    }

    // ================ LOGIN / REGISTER ================
    private void showAuthScene(Node cardContent) {
        VBox root = new VBox();
        root.getStyleClass().add("login-bg");
        root.setAlignment(Pos.CENTER);

        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);
        card.getChildren().add(cardContent);

        root.getChildren().add(card);
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showLogin() {
        showLogin(null);
    }

    private void showLogin(String prefilledEmail) {
        Label title = new Label("CepteFinans");
        title.getStyleClass().add("login-title");

        Label sub = new Label("Finanslarınızı profesyonelce yönetin");
        sub.getStyleClass().add("login-subtitle");

        VBox form = new VBox(14);
        form.setPadding(new Insets(10, 0, 0, 0));

        Label eml = new Label("E-posta Adresi");
        eml.getStyleClass().add("login-label");
        TextField email = new TextField();
        email.setPromptText("ornek@email.com");
        email.getStyleClass().add("text-field");
        if (prefilledEmail != null) {
            email.setText(prefilledEmail);
        }

        Label psl = new Label("Şifre");
        psl.getStyleClass().add("login-label");
        PasswordField pass = new PasswordField();
        pass.setPromptText("••••••••");
        pass.getStyleClass().add("text-field");

        Button loginBtn = new Button("Giriş Yap");
        loginBtn.getStyleClass().addAll("btn", "btn-primary");
        loginBtn.setPrefWidth(340);
        loginBtn.setOnAction(e -> performLogin(email.getText().trim(), pass.getText()));

        HBox extra = new HBox(10);
        extra.setAlignment(Pos.CENTER);
        CheckBox remember = new CheckBox("Beni hatırla");
        remember.setTextFill(Color.web("#888"));
        Hyperlink forgot = new Hyperlink("Şifremi unuttum");
        forgot.setTextFill(Color.web("#4f46e5"));
        forgot.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                "Şifre sıfırlama bu sürümde yok. Yeni hesap için «Kayıt Ol» kullanın veya mevcut şifrenizle giriş yapın.").show());
        extra.getChildren().addAll(remember, forgot);

        HBox registerRow = new HBox(6);
        registerRow.setAlignment(Pos.CENTER);
        registerRow.setPadding(new Insets(8, 0, 0, 0));
        Label noAccount = new Label("Hesabınız yok mu?");
        noAccount.setTextFill(Color.web("#64748b"));
        Hyperlink registerLink = new Hyperlink("Kayıt Ol");
        registerLink.getStyleClass().add("login-link");
        registerLink.setOnAction(e -> showRegister());
        registerRow.getChildren().addAll(noAccount, registerLink);

        form.getChildren().addAll(eml, email, psl, pass, loginBtn, extra, registerRow);

        VBox content = new VBox(0, title, sub, form);
        content.setAlignment(Pos.CENTER);
        showAuthScene(content);
    }

    private void showRegister() {
        Label title = new Label("Hesap Oluştur");
        title.getStyleClass().add("login-title");

        Label sub = new Label("CepteFinans'a katılın");
        sub.getStyleClass().add("login-subtitle");

        VBox form = new VBox(14);
        form.setPadding(new Insets(10, 0, 0, 0));

        Label nameLbl = new Label("Ad Soyad");
        nameLbl.getStyleClass().add("login-label");
        TextField name = new TextField();
        name.setPromptText("Adınız Soyadınız");
        name.getStyleClass().add("text-field");

        Label eml = new Label("E-posta Adresi");
        eml.getStyleClass().add("login-label");
        TextField email = new TextField();
        email.setPromptText("ornek@email.com");
        email.getStyleClass().add("text-field");

        Label psl = new Label("Şifre");
        psl.getStyleClass().add("login-label");
        PasswordField pass = new PasswordField();
        pass.setPromptText("En az 3 karakter");
        pass.getStyleClass().add("text-field");

        Label psl2 = new Label("Şifre (Tekrar)");
        psl2.getStyleClass().add("login-label");
        PasswordField pass2 = new PasswordField();
        pass2.setPromptText("Şifrenizi tekrar girin");
        pass2.getStyleClass().add("text-field");

        Button registerBtn = new Button("Kayıt Ol");
        registerBtn.getStyleClass().addAll("btn", "btn-success");
        registerBtn.setPrefWidth(340);
        registerBtn.setOnAction(e -> performRegister(
                name.getText().trim(),
                email.getText().trim(),
                pass.getText(),
                pass2.getText()
        ));

        HBox loginRow = new HBox(6);
        loginRow.setAlignment(Pos.CENTER);
        loginRow.setPadding(new Insets(8, 0, 0, 0));
        Label hasAccount = new Label("Zaten hesabınız var mı?");
        hasAccount.setTextFill(Color.web("#64748b"));
        Hyperlink loginLink = new Hyperlink("Giriş Yap");
        loginLink.getStyleClass().add("login-link");
        loginLink.setOnAction(ev -> showLogin());
        loginRow.getChildren().addAll(hasAccount, loginLink);

        form.getChildren().addAll(nameLbl, name, eml, email, psl, pass, psl2, pass2, registerBtn, loginRow);

        VBox content = new VBox(0, title, sub, form);
        content.setAlignment(Pos.CENTER);
        showAuthScene(content);
    }

    private void performLogin(String em, String pw) {
        if (em.isEmpty() || pw.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "E-posta ve şifre gerekli.").show();
            return;
        }
        new Thread(() -> {
            try {
                ObjectNode body = mapper.createObjectNode();
                body.put("email", em);
                body.put("password", pw);
                String resp = ApiClient.post("/api/users/login", mapper.writeValueAsString(body));
                JsonNode json = mapper.readTree(resp);
                String uid = json.get("id").asText();
                String uname = json.has("name") ? json.get("name").asText() : em;
                ApiClient.currentUserId = uid;
                Platform.runLater(() -> showMain(uid, uname));
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                        "Giriş başarısız: " + formatError(ex)).show());
            }
        }).start();
    }

    private void performRegister(String name, String email, String password, String passwordConfirm) {
        if (name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Ad soyad gerekli.").show();
            return;
        }
        if (email.isBlank() || !email.contains("@")) {
            new Alert(Alert.AlertType.WARNING, "Geçerli bir e-posta girin.").show();
            return;
        }
        if (password.length() < 3) {
            new Alert(Alert.AlertType.WARNING, "Şifre en az 3 karakter olmalıdır.").show();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            new Alert(Alert.AlertType.WARNING, "Şifreler eşleşmiyor.").show();
            return;
        }
        new Thread(() -> {
            try {
                ObjectNode body = mapper.createObjectNode();
                body.put("name", name);
                body.put("email", email);
                body.put("password", password);
                String resp = ApiClient.post("/api/users", mapper.writeValueAsString(body));
                JsonNode json = mapper.readTree(resp);
                String uid = json.get("id").asText();
                String uname = json.has("name") ? json.get("name").asText() : name;
                ApiClient.currentUserId = uid;
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Kayıt başarılı. Hoş geldiniz, " + uname + "!").show();
                    showMain(uid, uname);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                        "Kayıt başarısız: " + formatError(ex)).show());
            }
        }).start();
    }

    private static String formatError(Exception ex) {
        String errMsg = ex.getMessage();
        if (errMsg == null || errMsg.isBlank() || "null".equals(errMsg)) {
            return "Bilinmeyen bir hata oluştu. Backend bağlantısını kontrol edin.";
        }
        return errMsg;
    }

    private String userListPath(String resource) {
        if (ApiClient.currentUserId != null && !ApiClient.currentUserId.isBlank()) {
            if ("budgets".equals(resource)) {
                return "/api/budgets/user/" + ApiClient.currentUserId + "/all";
            }
            return "/api/" + resource + "/user/" + ApiClient.currentUserId;
        }
        return "/api/" + resource;
    }

    private String transactionMonthKey(JsonNode t) {
        if (t == null || !t.has("createdAt") || t.get("createdAt").isNull()) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        JsonNode createdAt = t.get("createdAt");
        if (createdAt.isTextual()) {
            String s = createdAt.asText();
            return s.length() >= 7 ? s.substring(0, 7) : s;
        }
        if (createdAt.isArray() && createdAt.size() >= 2) {
            return String.format("%04d-%02d", createdAt.get(0).asInt(), createdAt.get(1).asInt());
        }
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /** Giriş yapan kullanıcının kimliği; formlarda userId alanı gösterilmez. */
    private String requireCurrentUserId() {
        if (ApiClient.currentUserId == null || ApiClient.currentUserId.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Oturum bulunamadı. Lütfen tekrar giriş yapın.").show();
            return null;
        }
        return ApiClient.currentUserId;
    }

    private void showApiError(String action, Exception ex) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, action + ": " + formatError(ex)).show());
    }

    // ================ MAIN APP ================
    private void showMain(String userId, String userName) {
        mainRoot = new BorderPane();
        mainRoot.setLeft(buildSidebar());
        mainRoot.setTop(buildTopBar(userName));

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: transparent;");
        contentArea.setPadding(new Insets(20));
        mainRoot.setCenter(contentArea);

        Scene scene = new Scene(mainRoot, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        showPage("dashboard");

        // Her 60 saniyede dashboard'u otomatik yenile
        if (autoRefreshTask != null) autoRefreshTask.cancel(false);
        autoRefreshTask = scheduler.scheduleAtFixedRate(
            () -> Platform.runLater(() -> {
                if (totalIncomeLbl != null) refreshDashboard();
            }), 60, 60, TimeUnit.SECONDS);
    }

    // ================ SIDEBAR ================
    private VBox buildSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("💰 CepteFinans");
        brand.getStyleClass().add("sidebar-brand");

        Label sub = new Label("KİŞİSEL FİNANS ASİSTANI");
        sub.getStyleClass().add("sidebar-section");

        sidebar.getChildren().addAll(brand, sub);

        addSidebarItem("dashboard", "📊", "Dashboard");
        addSidebarItem("transactions", "💳", "İşlemler");
        addSidebarItem("budgets", "📋", "Bütçeler");
        addSidebarItem("accounts", "🏦", "Hesaplar & Varlıklar");
        addSidebarItem("goals", "🎯", "Tasarruf Hedefleri");
        addSidebarItem("reports", "📈", "Raporlar & Analiz");
        addSidebarItem("currency", "💱", "Döviz & Kripto");

        Label sys = new Label("SİSTEM");
        sys.getStyleClass().add("sidebar-section");
        sidebar.getChildren().add(sys);

        addSidebarItem("notifications", "🔔", "Bildirimler");
        addSidebarItem("settings", "⚙️", "Ayarlar");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        VBox userBox = new VBox(4);
        userBox.getStyleClass().add("sidebar-footer");
        Label uname = new Label("Kullanıcı"); uname.getStyleClass().add("sidebar-user");
        Label umail = new Label("user@ceptefinans.com"); umail.getStyleClass().add("sidebar-email");
        userBox.getChildren().addAll(uname, umail);
        sidebar.getChildren().add(userBox);

        return sidebar;
    }

    private void addSidebarItem(String id, String icon, String text) {
        if ("notifications".equals(id)) {
            HBox h = new HBox(8);
            h.setAlignment(Pos.CENTER_LEFT);
            Button btn = new Button(icon + "  " + text);
            btn.getStyleClass().add("sidebar-btn");
            btn.setOnAction(e -> showPage(id));
            btn.setUserData(id);
            btn.setPrefWidth(180);
            sidebarNotifBadge = new Label("0");
            sidebarNotifBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: #fff; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 10;");
            sidebarNotifBadge.setVisible(false);
            h.getChildren().addAll(btn, sidebarNotifBadge);
            sidebar.getChildren().add(h);
        } else {
            Button btn = new Button(icon + "  " + text);
            btn.getStyleClass().add("sidebar-btn");
            btn.setOnAction(e -> showPage(id));
            btn.setUserData(id);
            sidebar.getChildren().add(btn);
        }
    }

    private void showPage(String id) {
        // Update sidebar active state
        sidebar.getChildren().forEach(n -> {
            if(n instanceof Button) {
                Button b = (Button) n;
                if(id.equals(b.getUserData())) b.getStyleClass().add("active");
                else b.getStyleClass().remove("active");
            }
        });

        contentArea.getChildren().clear();
        switch(id) {
            case "dashboard": contentArea.getChildren().add(buildDashboard()); break;
            case "transactions": contentArea.getChildren().add(buildTransactions()); break;
            case "budgets": contentArea.getChildren().add(buildBudgets()); break;
            case "accounts": contentArea.getChildren().add(buildAccounts()); break;
            case "goals": contentArea.getChildren().add(buildGoals()); break;
            case "reports": contentArea.getChildren().add(buildReports()); break;
            case "currency": contentArea.getChildren().add(buildCurrency()); break;
            case "notifications": contentArea.getChildren().add(buildNotifications()); break;
            case "settings": contentArea.getChildren().add(buildSettings()); break;
        }
    }

    // ================ TOP BAR ================
    private HBox buildTopBar(String userName) {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox titles = new VBox(2);
        Label t = new Label("Genel Bakış"); t.getStyleClass().add("top-title");
        Label st = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", new Locale("tr"))));
        st.getStyleClass().add("top-subtitle");
        titles.getChildren().addAll(t, st);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField(); search.setPromptText("🔍 İşlem, kategori veya hesap ara..."); search.getStyleClass().add("search-field");

        Label userLbl = new Label("👤 " + userName); userLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        unreadBadge = new Label("0");
        unreadBadge.getStyleClass().add("badge");
        unreadBadge.setVisible(false);

        Button notifBtn = new Button("🔔"); notifBtn.getStyleClass().add("top-btn");
        notifBtn.setOnAction(e -> showPage("notifications"));

        Button logout = new Button("Çıkış"); logout.getStyleClass().addAll("top-btn", "btn-danger");
        logout.setOnAction(e -> showLogin());

        bar.getChildren().addAll(titles, spacer, search, userLbl, notifBtn, unreadBadge, logout);
        return bar;
    }

    // ================ DASHBOARD ================
    private ScrollPane buildDashboard() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(10,0,20,0));

        // Summary cards row
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);
        totalIncomeLbl = new Label("₺0,00"); totalIncomeLbl.getStyleClass().addAll("card-value", "card-positive");
        totalExpenseLbl = new Label("₺0,00"); totalExpenseLbl.getStyleClass().addAll("card-value", "card-negative");
        balanceLbl = new Label("₺0,00"); balanceLbl.getStyleClass().addAll("card-value", "card-neutral");
        budgetStatusLbl = new Label("0 aşıldı"); budgetStatusLbl.getStyleClass().addAll("card-value", "card-info");
        savingsLbl = new Label("₺0,00"); savingsLbl.getStyleClass().addAll("card-value", "card-positive");
        dailyAvgLbl = new Label("₺0,00"); dailyAvgLbl.getStyleClass().addAll("card-value", "card-neutral");

        incomeChangeLbl = new Label("..."); incomeChangeLbl.getStyleClass().add("positive");
        expenseChangeLbl = new Label("..."); expenseChangeLbl.getStyleClass().add("positive");
        balanceChangeLbl = new Label("..."); balanceChangeLbl.getStyleClass().add("positive");
        budgetChangeLbl = new Label("..."); budgetChangeLbl.getStyleClass().add("positive");
        savingsChangeLbl = new Label("..."); savingsChangeLbl.getStyleClass().add("positive");
        dailyChangeLbl = new Label("..."); dailyChangeLbl.getStyleClass().add("positive");

        cards.getChildren().addAll(
            statCard("📈 Toplam Gelir", totalIncomeLbl, incomeChangeLbl),
            statCard("📉 Toplam Gider", totalExpenseLbl, expenseChangeLbl),
            statCard("💰 Net Bakiye", balanceLbl, balanceChangeLbl),
            statCard("📋 Bütçe Durumu", budgetStatusLbl, budgetChangeLbl),
            statCard("🏦 Tasarruf", savingsLbl, savingsChangeLbl),
            statCard("📊 Günlük Ort.", dailyAvgLbl, dailyChangeLbl)
        );

        // Middle row: Charts + widgets
        HBox middle = new HBox(16);
        HBox.setHgrow(middle, Priority.ALWAYS);

        // Left: Monthly bar chart
        CategoryAxis x = new CategoryAxis(); x.setLabel("Ay");
        NumberAxis y = new NumberAxis(); y.setLabel("Tutar (₺)");
        monthlyBar = new BarChart<>(x,y);
        monthlyBar.setTitle("Aylık Gelir / Gider Karşılaştırması");
        monthlyBar.setPrefWidth(520); monthlyBar.setMinHeight(340);
        monthlyBar.getStyleClass().add("chart");
        monthlyBar.setLegendVisible(true);

        // Right: widgets column
        VBox widgets = new VBox(16);
        widgets.setPrefWidth(360);

        // Pie chart
        categoryPie = new PieChart();
        categoryPie.setTitle("Kategori Dağılımı");
        categoryPie.setPrefHeight(260);
        categoryPie.getStyleClass().add("chart");
        categoryPie.setLabelsVisible(true);

        // Budget mini widget
        VBox budgetWidget = new VBox(10);
        budgetWidget.getStyleClass().add("widget-small");
        Label bwTitle = new Label("🚨 Bütçe Uyarıları"); bwTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 13px;");
        budgetWidget.getChildren().add(bwTitle);
        // Will populate on refresh
        budgetWidget.setUserData("budgetWidget");

        financialGauge = new FinancialGaugeCanvas();

        widgets.getChildren().addAll(categoryPie, financialGauge, budgetWidget);

        middle.getChildren().addAll(monthlyBar, widgets);

        // Bottom: Recent transactions + quick stats
        HBox bottom = new HBox(16);
        VBox recentBox = new VBox(10);
        recentBox.getStyleClass().add("card");
        recentBox.setPrefWidth(600);
        Label recTitle = new Label("🕐 Son İşlemler"); recTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        recentTxTable = buildMiniTable();
        recentBox.getChildren().addAll(recTitle, recentTxTable);

        VBox quickBox = new VBox(10);
        quickBox.getStyleClass().add("card");
        quickBox.setPrefWidth(380);
        Label qTitle = new Label("⚡ Hızlı Eylemler"); qTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        GridPane quickGrid = new GridPane();
        quickGrid.setHgap(10); quickGrid.setVgap(10);
        quickGrid.add(quickBtn("➕ Yeni Gelir", "btn-success", () -> showTransactionDialog(null)), 0, 0);
        quickGrid.add(quickBtn("➖ Yeni Gider", "btn-danger", () -> showTransactionDialog(null)), 1, 0);
        quickGrid.add(quickBtn("🎯 Hedef Ekle", "btn-primary", () -> showPage("goals")), 0, 1);
        quickGrid.add(quickBtn("📋 Bütçe Oluştur", "btn-ghost", () -> showPage("budgets")), 1, 1);
        quickBox.getChildren().addAll(qTitle, quickGrid);

        bottom.getChildren().addAll(recentBox, quickBox);

        main.getChildren().addAll(cards, middle, bottom);
        ScrollPane sp = new ScrollPane(main);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");

        refreshDashboard();
        return sp;
    }

    private VBox statCard(String title, Label value, Label changeLbl) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(180);
        Label t = new Label(title); t.getStyleClass().add("card-title");
        changeLbl.getStyleClass().add("card-change");
        card.getChildren().addAll(t, value, changeLbl);
        return card;
    }

    private Button quickBtn(String text, String style, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().addAll("btn", style);
        b.setPrefWidth(170);
        b.setOnAction(e -> action.run());
        return b;
    }

    private TableView<JsonNode> buildMiniTable() {
        TableView<JsonNode> t = new TableView<>();
        t.getStyleClass().add("table-view");
        t.setPrefHeight(200);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<JsonNode, String> date = new TableColumn<>("Tarih");
        date.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().has("createdAt") ? data.getValue().get("createdAt").asText().substring(0,10) : ""));
        TableColumn<JsonNode, String> type = new TableColumn<>("Tür");
        type.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().has("type") ? data.getValue().get("type").asText() : ""));
        type.setCellFactory(col -> new TableCell<JsonNode, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle(empty ? "" : (item.equalsIgnoreCase("GELIR") ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;"));
            }
        });
        TableColumn<JsonNode, String> amount = new TableColumn<>("Tutar");
        amount.setCellValueFactory(data -> {
            if(!data.getValue().has("amount")) return new javafx.beans.property.SimpleStringProperty("");
            double v = data.getValue().get("amount").asDouble();
            return new javafx.beans.property.SimpleStringProperty(tl.format(v));
        });
        TableColumn<JsonNode, String> desc = new TableColumn<>("Açıklama");
        desc.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().has("description") ? data.getValue().get("description").asText() : ""));
        t.getColumns().addAll(date, type, amount, desc);
        // Çift tıklama ile düzenleme
        t.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showTransactionDialog(row.getItem());
                }
            });
            return row;
        });
        return t;
    }

    private void refreshDashboard() {
        new Thread(() -> {
            JsonNode[] tx = null;
            JsonNode[] bg = null;
            JsonNode[] nf = null;
            try {
                tx = ApiClient.getJsonArray(userListPath("transactions"));
            } catch (Exception ex) {
                showApiError("İşlemler yüklenemedi", ex);
            }
            try {
                bg = ApiClient.getJsonArray(userListPath("budgets"));
            } catch (Exception ex) {
                showApiError("Bütçeler yüklenemedi", ex);
            }
            try {
                nf = ApiClient.getJsonArray(userListPath("notifications"));
            } catch (Exception ex) {
                showApiError("Bildirimler yüklenemedi", ex);
            }
            try {
                double income = 0, expense = 0;
                Map<String, Double> catSpending = new HashMap<>();
                Map<String, Double> monthlyIncome = new TreeMap<>();
                Map<String, Double> monthlyExpense = new TreeMap<>();
                List<JsonNode> recent = new ArrayList<>();

                String curMonth  = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                String prevMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                int dayCount = 0;
                double prevIncome = 0, prevExpense = 0;
                if(tx != null) {
                    recent = Arrays.stream(tx)
                            .sorted((a, b) -> transactionMonthKey(b).compareTo(transactionMonthKey(a)))
                            .limit(5).collect(Collectors.toList());
                    for (JsonNode t : tx) {
                        double amt = t.get("amount").asDouble();
                        String type = t.get("type").asText();
                        String month = transactionMonthKey(t);
                        if (type.equalsIgnoreCase("GELIR")) {
                            income += amt; monthlyIncome.merge(month, amt, Double::sum);
                            if (month.equals(prevMonth)) prevIncome += amt;
                        } else {
                            expense += amt; monthlyExpense.merge(month, amt, Double::sum);
                            if (month.equals(prevMonth)) prevExpense += amt;
                            String cat = t.has("description") && !t.get("description").asText().isEmpty() ? t.get("description").asText() : "Diğer";
                            catSpending.merge(cat.length() > 14 ? cat.substring(0,14) : cat, amt, Double::sum);
                        }
                        dayCount++;
                    }
                }

                double balance = income - expense;
                double savings = Math.max(0, income * 0.15);
                double dailyAvg = dayCount > 0 ? expense / Math.max(dayCount, 30) : 0;

                int budgetOver = 0;
                List<String> overBudgets = new ArrayList<>();
                if (bg != null) {
                    for (JsonNode b : bg) {
                        double spent = b.has("spentAmount") ? b.get("spentAmount").asDouble() : 0;
                        double limit = b.get("limitAmount").asDouble();
                        if (spent >= limit) { budgetOver++; overBudgets.add(b.get("category").asText()); }
                    }
                }

                double finalIncome = income, finalExpense = expense, finalBalance = balance;
                double finalSavings = savings, finalDaily = dailyAvg;
                double finalPrevIncome = prevIncome, finalPrevExpense = prevExpense;
                int finalBudgetOver = budgetOver;
                List<JsonNode> finalRecent = recent;
                List<String> finalOverBudgets = overBudgets;

                Platform.runLater(() -> {
                    totalIncomeLbl.setText(tl.format(finalIncome));
                    totalExpenseLbl.setText(tl.format(finalExpense));
                    balanceLbl.setText(tl.format(finalBalance));
                    budgetStatusLbl.setText(finalBudgetOver > 0 ? finalBudgetOver + " aşıldı" : "Tümü limit içinde");
                    savingsLbl.setText(tl.format(finalSavings));
                    dailyAvgLbl.setText(tl.format(finalDaily));

                    // Dynamic change labels
                    updateChangeLabel(incomeChangeLbl, finalIncome, finalPrevIncome, false);
                    updateChangeLabel(expenseChangeLbl, finalExpense, finalPrevExpense, true);
                    updateChangeLabel(balanceChangeLbl, finalBalance, (finalPrevIncome - finalPrevExpense), false);
                    budgetChangeLbl.setText(finalBudgetOver > 0 ? finalBudgetOver + " aşıldı" : "Tümü limit içinde");
                    budgetChangeLbl.getStyleClass().removeAll("positive", "negative");
                    budgetChangeLbl.getStyleClass().add(finalBudgetOver > 0 ? "negative" : "positive");
                    savingsChangeLbl.setText(tl.format(finalSavings));
                    savingsChangeLbl.getStyleClass().removeAll("positive", "negative");
                    savingsChangeLbl.getStyleClass().add("positive");
                    dailyChangeLbl.setText(String.format("%.0f ₺/gün", finalDaily));
                    dailyChangeLbl.getStyleClass().removeAll("positive", "negative");
                    dailyChangeLbl.getStyleClass().add(finalDaily > 0 ? "negative" : "positive");

                    if (financialGauge != null) {
                        financialGauge.update(finalIncome, finalExpense);
                    }

                    // Pie
                    categoryPie.getData().clear();
                    int ci = 0;
                    for (Map.Entry<String, Double> e : catSpending.entrySet()) {
                        PieChart.Data d = new PieChart.Data(e.getKey(), e.getValue());
                        categoryPie.getData().add(d);
                        if(++ci >= 8) break;
                    }

                    // Bar
                    monthlyBar.getData().clear();
                    XYChart.Series<String, Number> incS = new XYChart.Series<>(); incS.setName("Gelir");
                    XYChart.Series<String, Number> expS = new XYChart.Series<>(); expS.setName("Gider");
                    Set<String> months = new TreeSet<>(); months.addAll(monthlyIncome.keySet()); months.addAll(monthlyExpense.keySet());
                    if(months.isEmpty()) months.add(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
                    for (String m : months) {
                        incS.getData().add(new XYChart.Data<>(m, monthlyIncome.getOrDefault(m, 0.0)));
                        expS.getData().add(new XYChart.Data<>(m, monthlyExpense.getOrDefault(m, 0.0)));
                    }
                    monthlyBar.getData().addAll(incS, expS);
                    monthlyBar.setCategoryGap(20);

                    // Recent table
                    recentTxTable.getItems().setAll(finalRecent);

                    // Budget widget
                    for(Node n : contentArea.lookupAll(".widget-small")) {
                        if("budgetWidget".equals(n.getUserData()) && n instanceof VBox) {
                            VBox bw = (VBox) n;
                            bw.getChildren().removeIf(c -> c instanceof HBox);
                            for(String ob : finalOverBudgets) {
                                HBox row = new HBox(8);
                                row.setAlignment(Pos.CENTER_LEFT);
                                Label dot = new Label("●"); dot.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 10px;");
                                Label l = new Label(ob + " bütçesi aşıldı!"); l.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 12px;");
                                row.getChildren().addAll(dot, l);
                                bw.getChildren().add(row);
                            }
                            if(finalOverBudgets.isEmpty()) {
                                Label ok = new Label("✓ Tüm bütçeler limit içinde"); ok.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
                                bw.getChildren().add(ok);
                            }
                        }
                    }

                    long unread = nf != null ? Arrays.stream(nf).filter(n -> !n.get("read").asBoolean()).count() : 0;
                    unreadBadge.setText(String.valueOf(unread));
                    unreadBadge.setVisible(unread > 0);
                });
            } catch (Exception ex) {
                showApiError("Dashboard güncellenemedi", ex);
            }
        }).start();
    }

    private void updateChangeLabel(Label lbl, double current, double prev, boolean inverse) {
        String text;
        boolean positive;
        if (prev == 0) {
            text = "Önceki ay yok";
            positive = true;
        } else {
            double pct = (current - prev) / Math.abs(prev) * 100;
            text = String.format("%s%.1f%% önceki aya göre", pct >= 0 ? "▲ " : "▼ ", Math.abs(pct));
            positive = inverse ? (pct < 0) : (pct >= 0);
        }
        lbl.setText(text);
        lbl.getStyleClass().removeAll("positive", "negative");
        lbl.getStyleClass().add(positive ? "positive" : "negative");
    }

    // ================ TRANSACTIONS ================
    private VBox buildTransactions() {
        VBox root = new VBox(16);

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> typeFilter = new ComboBox<>(FXCollections.observableArrayList("Tümü", "GELIR", "GIDER"));
        typeFilter.setValue("Tümü"); typeFilter.getStyleClass().add("combo-box"); typeFilter.setPrefWidth(120);

        DatePicker fromDate = new DatePicker(); fromDate.setPromptText("Başlangıç"); fromDate.getStyleClass().add("text-field");
        DatePicker toDate = new DatePicker(); toDate.setPromptText("Bitiş"); toDate.getStyleClass().add("text-field");

        TextField search = new TextField(); search.setPromptText("Ara..."); search.getStyleClass().add("search-field"); search.setPrefWidth(200);

        Button applyBtn = new Button("🔍 Filtrele"); applyBtn.getStyleClass().addAll("btn", "btn-primary");
        Button exportBtn = new Button("📤 Dışa Aktar"); exportBtn.getStyleClass().addAll("btn", "btn-ghost");
        exportBtn.setOnAction(e -> exportTransactionsCsv());
        Button deleteSelBtn = new Button("🗑️ Seçileni Sil"); deleteSelBtn.getStyleClass().addAll("btn", "btn-danger");
        Button addBtn = new Button("➕ Yeni İşlem"); addBtn.getStyleClass().addAll("btn", "btn-success");
        addBtn.setOnAction(e -> showTransactionDialog(null));

        filters.getChildren().addAll(typeFilter, fromDate, toDate, search, applyBtn, exportBtn, deleteSelBtn, addBtn);

        TableView<JsonNode> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setItems(transactionList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        table.getColumns().addAll(
            col("Tarih", "createdAt", 120),
            col("Tür", "type", 80),
            col("Tutar", "amount", 110),
            col("Kategori", "description", 180),
            col("Hesap", "userId", 120)
        );

        // Çift tıklama ile düzenleme
        table.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showTransactionDialog(row.getItem());
                }
            });
            return row;
        });

        HBox stats = new HBox(16);
        Label totalLbl = new Label("Toplam: ₺0,00"); totalLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        Label countLbl = new Label("0 işlem"); countLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        stats.getChildren().addAll(totalLbl, countLbl);

        deleteSelBtn.setOnAction(e -> {
            List<JsonNode> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
            if (selected.isEmpty()) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                selected.size() + " işlem silinecek. Emin misiniz?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    new Thread(() -> {
                        for (JsonNode n : selected) {
                            try { ApiClient.delete("/api/transactions/" + n.get("id").asText()); }
                            catch (Exception ex) {}
                        }
                        Platform.runLater(() -> loadTransactions(typeFilter.getValue(), search.getText(), fromDate.getValue(), toDate.getValue(), totalLbl, countLbl));
                    }).start();
                }
            });
        });

        applyBtn.setOnAction(e -> loadTransactions(typeFilter.getValue(), search.getText(), fromDate.getValue(), toDate.getValue(), totalLbl, countLbl));
        loadTransactions("Tümü", "", null, null, totalLbl, countLbl);

        root.getChildren().addAll(filters, table, stats);
        return root;
    }

    private void loadTransactions(String typeFilter, String search, LocalDate from, LocalDate to, Label totalLbl, Label countLbl) {
        new Thread(() -> {
            try {
                JsonNode[] d = ApiClient.getJsonArray(userListPath("transactions"));
                List<JsonNode> list = d != null ? Arrays.asList(d) : new ArrayList<>();
                if(!"Tümü".equals(typeFilter)) {
                    list = list.stream().filter(n -> n.get("type").asText().equalsIgnoreCase(typeFilter)).collect(Collectors.toList());
                }
                if(!search.isEmpty()) {
                    list = list.stream().filter(n -> n.toString().toLowerCase().contains(search.toLowerCase())).collect(Collectors.toList());
                }
                if (from != null || to != null) {
                    list = list.stream().filter(n -> {
                        if (!n.has("createdAt") || n.get("createdAt").isNull()) return false;
                        LocalDate dte = LocalDate.parse(n.get("createdAt").asText().substring(0,10));
                        if (from != null && dte.isBefore(from)) return false;
                        if (to != null && dte.isAfter(to)) return false;
                        return true;
                    }).collect(Collectors.toList());
                }
                double total = list.stream().mapToDouble(n -> n.get("amount").asDouble()).sum();
                List<JsonNode> finalList = list;
                Platform.runLater(() -> {
                    transactionList.setAll(finalList);
                    totalLbl.setText("Toplam: " + tl.format(total));
                    countLbl.setText(finalList.size() + " işlem");
                });
            } catch (Exception e) {}
        }).start();
    }

    private void showTransactionDialog(JsonNode existing) {
        Stage d = dialogStage(existing != null ? "İşlem Düzenle" : "Yeni İşlem");
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);

        TextField amount = new TextField(); amount.setPromptText("0,00"); amount.getStyleClass().add("text-field");
        ComboBox<String> typeCb = new ComboBox<>(FXCollections.observableArrayList("GELIR", "GIDER")); typeCb.setValue("GELIR"); typeCb.getStyleClass().add("combo-box");
        TextField desc = new TextField(); desc.setPromptText("Açıklama / Kategori"); desc.getStyleClass().add("text-field");
        DatePicker date = new DatePicker(LocalDate.now()); date.getStyleClass().add("text-field");
        TextField tags = new TextField(); tags.setPromptText("Etiketler (virgülle)"); tags.getStyleClass().add("text-field");

        if(existing != null) {
            amount.setText(existing.get("amount").asText());
            typeCb.setValue(existing.get("type").asText());
            desc.setText(existing.has("description") ? existing.get("description").asText() : "");
        }

        g.add(newLabel("Tutar"), 0, 0); g.add(amount, 1, 0);
        g.add(newLabel("Tür"), 0, 1); g.add(typeCb, 1, 1);
        g.add(newLabel("Açıklama"), 0, 2); g.add(desc, 1, 2);
        g.add(newLabel("Tarih"), 0, 3); g.add(date, 1, 3);
        g.add(newLabel("Etiketler"), 0, 4); g.add(tags, 1, 4);

        Button save = new Button("💾 Kaydet"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            String uid = requireCurrentUserId();
            if (uid == null) return;
            try {
                ObjectNode body = mapper.createObjectNode();
                body.put("userId", uid);
                body.put("amount", new java.math.BigDecimal(amount.getText().trim().replace(",", ".")));
                body.put("type", typeCb.getValue());
                body.put("description", desc.getText().trim());
                String json = mapper.writeValueAsString(body);
                if (existing != null) {
                    ApiClient.put("/api/transactions/" + existing.get("id").asText(), json);
                } else {
                    ApiClient.post("/api/transactions", json);
                }
                d.close();
                if(contentArea.getChildren().get(0) instanceof VBox) showPage("transactions");
                else refreshDashboard();
            } catch (Exception ex) { showApiError("İşlem kaydedilemedi", ex); }
        });

        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost");
        cancel.setOnAction(e -> d.close());

        HBox btns = new HBox(10, save, cancel);
        Label dialogTitle = newLabel("İşlem Bilgileri");
        dialogTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        root.getChildren().addAll(dialogTitle, g, btns);
        d.setScene(new Scene(root, 460, 380)); d.show();
    }

    private void exportTransactionsCsv() {
        try (java.io.FileWriter fw = new java.io.FileWriter("transactions_export.csv")) {
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

    // ================ BUDGETS ================
    private VBox buildBudgets() {
        VBox root = new VBox(16);
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("➕ Yeni Bütçe"); addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showBudgetDialog(null));
        Button refresh = new Button("🔄 Yenile"); refresh.getStyleClass().addAll("btn", "btn-ghost");
        refresh.setOnAction(e -> loadBudgets());
        bar.getChildren().addAll(addBtn, refresh);

        TableView<JsonNode> table = new TableView<>(); table.getStyleClass().add("table-view");
        table.setItems(budgetList); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(col("Ay","month",60), col("Yıl","year",60), col("Kategori","category",120), col("Limit","limitAmount",100), col("Harcanan","spentAmount",100));

        TableColumn<JsonNode, String> progCol = new TableColumn<>("İlerleme");
        progCol.setPrefWidth(200);
        progCol.setCellFactory(tc -> new TableCell<JsonNode, String>() {
            private final ProgressBar pb = new ProgressBar(0);
            private final Label perc = new Label("0%");
            { pb.setPrefWidth(160); pb.setPrefHeight(18); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                JsonNode b = getTableRow().getItem();
                double spent = b.get("spentAmount").asDouble(), limit = b.get("limitAmount").asDouble();
                double ratio = limit > 0 ? Math.min(spent / limit, 1.0) : 0;
                pb.setProgress(ratio);
                pb.setStyle(ratio >= 0.9 ? "-fx-accent: #4f46e5;" : "-fx-accent: #10b981;");
                perc.setText(String.format("%.0f%%", ratio*100));
                perc.setStyle(ratio >= 0.9 ? "-fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-font-size: 11px;" : "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 11px;");
                HBox h = new HBox(8, pb, perc); h.setAlignment(Pos.CENTER_LEFT);
                setGraphic(h);
            }
        });

        TableColumn<JsonNode, String> statusCol = new TableColumn<>("Durum");
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(tc -> new TableCell<JsonNode, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || getTableRow() == null || getTableRow().getItem() == null) { setText(""); return; }
                double spent = getTableRow().getItem().get("spentAmount").asDouble();
                double limit = getTableRow().getItem().get("limitAmount").asDouble();
                if(spent > limit) { setText("🔴 Aşıldı"); setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold;"); }
                else if(spent >= limit*0.9) { setText("🟡 Kritik"); setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;"); }
                else { setText("🟢 İyi"); setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;"); }
            }
        });

        table.getColumns().addAll(progCol, statusCol);
        loadBudgets();

        // Çift tıklama ile düzenleme
        table.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showBudgetDialog(row.getItem());
                }
            });
            return row;
        });

        // Sağ tık menüsü - Silme
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Sil");
        deleteItem.setOnAction(e -> {
            JsonNode selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Bu bütçeyi silmek istediğinize emin misiniz?",
                    ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                ApiClient.delete("/api/budgets/" + selected.get("id").asText());
                                Platform.runLater(() -> { loadBudgets(); refreshDashboard(); });
                            } catch (Exception ex) {}
                        }).start();
                    }
                });
            }
        });
        cm.getItems().add(deleteItem);
        table.setContextMenu(cm);

        HBox summary = new HBox(16);
        Label sum1 = new Label("0 aktif bütçe"); sum1.setStyle("-fx-text-fill: #64748b;");
        summary.getChildren().addAll(sum1);

        root.getChildren().addAll(bar, table, summary);
        return root;
    }

    private void loadBudgets() {
        new Thread(() -> {
            try {
                JsonNode[] d = ApiClient.getJsonArray(userListPath("budgets"));
                Platform.runLater(() -> budgetList.setAll(d != null ? d : new JsonNode[0]));
            } catch (Exception e) {
                showApiError("Bütçeler yüklenemedi", e);
            }
        }).start();
    }

    private void showBudgetDialog(JsonNode existing) {
        Stage d = dialogStage(existing != null ? "Bütçe Düzenle" : "Yeni Bütçe");
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        TextField cat = new TextField(); cat.setPromptText("Kategori"); cat.getStyleClass().add("text-field");
        TextField limit = new TextField(); limit.setPromptText("Limit"); limit.getStyleClass().add("text-field");
        TextField month = new TextField(); month.setPromptText("Ay (1-12)"); month.getStyleClass().add("text-field");
        TextField year = new TextField(); year.setPromptText("Yıl"); year.getStyleClass().add("text-field");

        if (existing != null) {
            cat.setText(existing.has("category") ? existing.get("category").asText() : "");
            limit.setText(existing.has("limitAmount") ? existing.get("limitAmount").asText() : "");
            month.setText(existing.has("month") ? existing.get("month").asText() : "");
            year.setText(existing.has("year") ? existing.get("year").asText() : "");
        } else {
            LocalDate now = LocalDate.now();
            month.setText(String.valueOf(now.getMonthValue()));
            year.setText(String.valueOf(now.getYear()));
        }

        g.add(newLabel("Kategori"), 0, 0); g.add(cat, 1, 0);
        g.add(newLabel("Limit"), 0, 1); g.add(limit, 1, 1);
        g.add(newLabel("Ay"), 0, 2); g.add(month, 1, 2);
        g.add(newLabel("Yıl"), 0, 3); g.add(year, 1, 3);

        Button save = new Button("💾 Kaydet"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            String uid = requireCurrentUserId();
            if (uid == null) return;
            try { String json = String.format("{\"userId\":\"%s\",\"category\":\"%s\",\"limitAmount\":%s,\"month\":%s,\"year\":%s}",
                uid, cat.getText(), limit.getText(), month.getText(), year.getText());
                if (existing != null) {
                    ApiClient.put("/api/budgets/" + existing.get("id").asText(), json);
                } else {
                    ApiClient.post("/api/budgets", json);
                }
                d.close(); loadBudgets(); refreshDashboard();
            } catch (Exception ex) {
                showApiError("Bütçe kaydedilemedi", ex);
            }
        });
        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost"); cancel.setOnAction(e -> d.close());
        HBox btns = new HBox(10, save, cancel);
        Label dialogTitle2 = newLabel("Bütçe Bilgileri");
        dialogTitle2.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        root.getChildren().addAll(dialogTitle2, g, btns);
        d.setScene(new Scene(root, 440, 340)); d.show();
    }

    // ================ ACCOUNTS ================
    private ScrollPane buildAccounts() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(10,0,20,0));

        Label title = new Label("Hesaplar & Varlıklar");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button addAccountBtn = new Button("➕ Hesap Ekle"); addAccountBtn.getStyleClass().addAll("btn", "btn-primary");
        addAccountBtn.setOnAction(e -> showAccountDialog(null));
        Button addAssetBtn = new Button("➕ Varlık Ekle"); addAssetBtn.getStyleClass().addAll("btn", "btn-success");
        addAssetBtn.setOnAction(e -> showAssetDialog(null));
        Button refreshBtn = new Button("🔄 Yenile"); refreshBtn.getStyleClass().addAll("btn", "btn-ghost");
        refreshBtn.setOnAction(e -> { loadAccounts(); loadAssets(); });
        toolbar.getChildren().addAll(addAccountBtn, addAssetBtn, refreshBtn);

        // Accounts Table
        Label accTitle = new Label("💳 Hesaplar"); accTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 16px;");
        TableView<JsonNode> accTable = new TableView<>(); accTable.getStyleClass().add("table-view");
        accTable.setItems(accountList); accTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        accTable.getColumns().addAll(
            col("Ad","name",150), col("Tür","type",100), col("Kurum","institution",150),
            col("Bakiye","balance",120), col("Para Birimi","currency",80)
        );
        // Çift tıklama düzenleme
        accTable.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) showAccountDialog(row.getItem()); });
            return row;
        });
        // Sağ tık silme
        ContextMenu accCm = new ContextMenu();
        MenuItem delAcc = new MenuItem("Sil");
        delAcc.setOnAction(e -> {
            JsonNode sel = accTable.getSelectionModel().getSelectedItem();
            if (sel != null) confirmDelete("Bu hesabı silmek istediğinize emin misiniz?", "/api/accounts/", sel.get("id").asText(), this::loadAccounts);
        });
        accCm.getItems().add(delAcc);
        accTable.setContextMenu(accCm);

        // Assets Table
        Label assetTitle = new Label("📈 Varlıklar"); assetTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 16px;");
        TableView<JsonNode> assetTable = new TableView<>(); assetTable.getStyleClass().add("table-view");
        assetTable.setItems(assetList); assetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<JsonNode,String> assetName = col("Ad","name",120);
        TableColumn<JsonNode,String> assetType = col("Tür","type",100);
        TableColumn<JsonNode,String> assetQty = col("Miktar","quantity",80);
        TableColumn<JsonNode,String> assetCur = col("Anlık Değer","currentValue",110);
        TableColumn<JsonNode,String> assetPur = col("Alış","purchaseValue",110);
        TableColumn<JsonNode,String> assetTot = new TableColumn<>("Toplam Değer");
        assetTot.setCellValueFactory(data -> {
            double q = data.getValue().get("quantity").asDouble();
            double c = data.getValue().get("currentValue").asDouble();
            return new javafx.beans.property.SimpleStringProperty(tl.format(q * c));
        });
        TableColumn<JsonNode,String> assetPnL = new TableColumn<>("Kar/Zarar");
        assetPnL.setCellFactory(tc -> new TableCell<JsonNode,String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(""); return; }
                JsonNode n = getTableRow().getItem();
                double q = n.get("quantity").asDouble();
                double cur = n.get("currentValue").asDouble();
                double pur = n.get("purchaseValue").asDouble();
                double pnl = (cur - pur) * q;
                setText(tl.format(pnl));
                setStyle(pnl >= 0 ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #4f46e5; -fx-font-weight: bold;");
            }
        });
        assetTable.getColumns().addAll(assetName, assetType, assetQty, assetCur, assetPur, assetTot, assetPnL);
        // Çift tıklama
        assetTable.setRowFactory(tv -> {
            TableRow<JsonNode> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) showAssetDialog(row.getItem()); });
            return row;
        });
        // Sağ tık
        ContextMenu astCm = new ContextMenu();
        MenuItem delAst = new MenuItem("Sil");
        delAst.setOnAction(e -> {
            JsonNode sel = assetTable.getSelectionModel().getSelectedItem();
            if (sel != null) confirmDelete("Bu varlığı silmek istediğinize emin misiniz?", "/api/assets/", sel.get("id").asText(), this::loadAssets);
        });
        astCm.getItems().add(delAst);
        assetTable.setContextMenu(astCm);

        // Investment Pie
        VBox investBox = new VBox(10); investBox.getStyleClass().add("card"); investBox.setMaxWidth(500);
        Label iTitle = new Label("📈 Yatırım Dağılımı"); iTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        PieChart investPie = new PieChart(); investPie.setPrefHeight(260); investPie.getStyleClass().add("chart");
        investPie.setUserData("investPie");
        investBox.getChildren().addAll(iTitle, investPie);

        root.getChildren().addAll(title, toolbar, accTitle, accTable, assetTitle, assetTable, investBox);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true); sp.getStyleClass().add("scroll-pane");

        loadAccounts(); loadAssets();
        return sp;
    }

    private void loadAccounts() {
        new Thread(() -> {
            try {
                JsonNode[] d = ApiClient.getJsonArray(userListPath("accounts"));
                Platform.runLater(() -> accountList.setAll(d != null ? d : new JsonNode[0]));
            } catch (Exception e) {}
        }).start();
    }

    private void loadAssets() {
        new Thread(() -> {
            try {
                JsonNode[] d = ApiClient.getJsonArray(userListPath("assets"));
                Platform.runLater(() -> {
                    assetList.setAll(d != null ? d : new JsonNode[0]);
                    // Update pie chart
                    for (Node n : contentArea.lookupAll(".chart")) {
                        if ("investPie".equals(n.getUserData()) && n instanceof PieChart) {
                            PieChart pie = (PieChart) n;
                            pie.getData().clear();
                            Map<String, Double> byType = new HashMap<>();
                            for (JsonNode a : assetList) {
                                String t = a.has("type") ? a.get("type").asText() : "Diğer";
                                double val = a.get("quantity").asDouble() * a.get("currentValue").asDouble();
                                byType.merge(t, val, Double::sum);
                            }
                            for (Map.Entry<String, Double> e : byType.entrySet()) {
                                pie.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
                            }
                        }
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    private void showAccountDialog(JsonNode existing) {
        Stage d = dialogStage(existing != null ? "Hesap Düzenle" : "Yeni Hesap");
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        TextField name = new TextField(); name.setPromptText("Hesap adı"); name.getStyleClass().add("text-field");
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("VADESIZ", "BIRIKIM", "YATIRIM", "KREDI_KARTI"));
        type.setValue("VADESIZ"); type.getStyleClass().add("combo-box");
        TextField institution = new TextField(); institution.setPromptText("Banka / Kurum"); institution.getStyleClass().add("text-field");
        TextField balance = new TextField(); balance.setPromptText("0,00"); balance.getStyleClass().add("text-field");
        TextField currency = new TextField(); currency.setPromptText("TRY"); currency.setText("TRY"); currency.getStyleClass().add("text-field");

        if (existing != null) {
            name.setText(existing.has("name") ? existing.get("name").asText() : "");
            type.setValue(existing.has("type") ? existing.get("type").asText() : "VADESIZ");
            institution.setText(existing.has("institution") ? existing.get("institution").asText() : "");
            balance.setText(existing.has("balance") ? existing.get("balance").asText() : "");
            currency.setText(existing.has("currency") ? existing.get("currency").asText() : "TRY");
        }

        g.add(newLabel("Hesap Adı"), 0, 0); g.add(name, 1, 0);
        g.add(newLabel("Tür"), 0, 1); g.add(type, 1, 1);
        g.add(newLabel("Kurum"), 0, 2); g.add(institution, 1, 2);
        g.add(newLabel("Bakiye"), 0, 3); g.add(balance, 1, 3);
        g.add(newLabel("Para Birimi"), 0, 4); g.add(currency, 1, 4);

        Button save = new Button("💾 Kaydet"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            String uid = requireCurrentUserId();
            if (uid == null) return;
            try {
                String json = String.format("{\"userId\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"institution\":\"%s\",\"balance\":%s,\"currency\":\"%s\"}",
                    uid, name.getText(), type.getValue(), institution.getText(), balance.getText(), currency.getText());
                if (existing != null) ApiClient.put("/api/accounts/" + existing.get("id").asText(), json);
                else ApiClient.post("/api/accounts", json);
                d.close(); loadAccounts();
            } catch (Exception ex) {}
        });
        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost"); cancel.setOnAction(e -> d.close());
        HBox btns = new HBox(10, save, cancel);
        root.getChildren().addAll(newLabel("Hesap Bilgileri"), g, btns);
        d.setScene(new Scene(root, 440, 420)); d.show();
    }

    private void showAssetDialog(JsonNode existing) {
        Stage d = dialogStage(existing != null ? "Varlık Düzenle" : "Yeni Varlık");
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        TextField name = new TextField(); name.setPromptText("Varlık adı"); name.getStyleClass().add("text-field");
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("HISSE", "KRIPTO", "ALTIN", "DOVIZ", "DIGER"));
        type.setValue("HISSE"); type.getStyleClass().add("combo-box");
        TextField curVal = new TextField(); curVal.setPromptText("Anlık değer"); curVal.getStyleClass().add("text-field");
        TextField purVal = new TextField(); purVal.setPromptText("Alış fiyatı"); purVal.getStyleClass().add("text-field");
        TextField qty = new TextField(); qty.setPromptText("Miktar"); qty.getStyleClass().add("text-field");
        TextField currency = new TextField(); currency.setPromptText("TRY"); currency.setText("TRY"); currency.getStyleClass().add("text-field");

        if (existing != null) {
            name.setText(existing.has("name") ? existing.get("name").asText() : "");
            type.setValue(existing.has("type") ? existing.get("type").asText() : "HISSE");
            curVal.setText(existing.has("currentValue") ? existing.get("currentValue").asText() : "");
            purVal.setText(existing.has("purchaseValue") ? existing.get("purchaseValue").asText() : "");
            qty.setText(existing.has("quantity") ? existing.get("quantity").asText() : "");
            currency.setText(existing.has("currency") ? existing.get("currency").asText() : "TRY");
        }

        g.add(newLabel("Varlık Adı"), 0, 0); g.add(name, 1, 0);
        g.add(newLabel("Tür"), 0, 1); g.add(type, 1, 1);
        g.add(newLabel("Anlık Değer"), 0, 2); g.add(curVal, 1, 2);
        g.add(newLabel("Alış Fiyatı"), 0, 3); g.add(purVal, 1, 3);
        g.add(newLabel("Miktar"), 0, 4); g.add(qty, 1, 4);
        g.add(newLabel("Para Birimi"), 0, 5); g.add(currency, 1, 5);

        Button save = new Button("💾 Kaydet"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            String uid = requireCurrentUserId();
            if (uid == null) return;
            try {
                String json = String.format("{\"userId\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"currentValue\":%s,\"purchaseValue\":%s,\"quantity\":%s,\"currency\":\"%s\"}",
                    uid, name.getText(), type.getValue(), curVal.getText(), purVal.getText(), qty.getText(), currency.getText());
                if (existing != null) ApiClient.put("/api/assets/" + existing.get("id").asText(), json);
                else ApiClient.post("/api/assets", json);
                d.close(); loadAssets();
            } catch (Exception ex) {}
        });
        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost"); cancel.setOnAction(e -> d.close());
        HBox btns = new HBox(10, save, cancel);
        root.getChildren().addAll(newLabel("Varlık Bilgileri"), g, btns);
        d.setScene(new Scene(root, 440, 460)); d.show();
    }

    private void confirmDelete(String message, String apiPath, String id, Runnable onSuccess) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                new Thread(() -> {
                    try { ApiClient.delete(apiPath + id); Platform.runLater(onSuccess); }
                    catch (Exception ex) {}
                }).start();
            }
        });
    }

    private VBox bigStatCard(String title, String value, String change, boolean pos) {
        VBox c = new VBox(8); c.getStyleClass().add("card"); c.setPrefWidth(240);
        Label t = new Label(title); t.getStyleClass().add("card-title");
        Label v = new Label(value); v.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label ch = new Label(change); ch.getStyleClass().addAll("card-change", pos ? "positive" : "negative");
        c.getChildren().addAll(t, v, ch);
        return c;
    }

    private VBox accountCard(String name, String type, String balance, String color) {
        VBox c = new VBox(10); c.getStyleClass().add("card"); c.setPrefWidth(200);
        Rectangle r = new Rectangle(40, 6, Color.web(color)); r.setArcWidth(4); r.setArcHeight(4);
        Label n = new Label(name); n.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label t = new Label(type); t.getStyleClass().add("card-title");
        Label b = new Label(balance); b.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 18px; -fx-font-weight: bold;");
        c.getChildren().addAll(r, n, t, b);
        return c;
    }

    // ================ GOALS ================
    private ScrollPane buildGoals() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(10,0,20,0));

        Label title = new Label("🎯 Tasarruf Hedefleri");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Dynamic summary cards
        Label totalTargetLbl = new Label("₺0,00"); totalTargetLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label totalCurrentLbl = new Label("₺0,00"); totalCurrentLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label totalRemainingLbl = new Label("₺0,00"); totalRemainingLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label activeCountLbl = new Label("0 aktif"); activeCountLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox top = new HBox(16);
        VBox s1 = bigStatCardDyn("Toplam Hedef", totalTargetLbl, "aktif", true);
        VBox s2 = bigStatCardDyn("Biriken", totalCurrentLbl, "hedefe", true);
        VBox s3 = bigStatCardDyn("Kalan", totalRemainingLbl, "kalan", false);
        VBox s4 = bigStatCardDyn("Aktif Hedef", activeCountLbl, "devam ediyor", true);
        top.getChildren().addAll(s1, s2, s3, s4);

        // Toolbar
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = new Button("➕ Yeni Hedef"); addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> showGoalDialog(null));
        Button refresh = new Button("🔄 Yenile"); refresh.getStyleClass().addAll("btn", "btn-ghost");
        refresh.setOnAction(e -> showPage("goals"));
        bar.getChildren().addAll(addBtn, refresh);

        // Goals flow pane
        FlowPane goalsPane = new FlowPane(16, 16);
        goalsPane.setUserData("goalsPane");
        goalsPane.setPrefWrapLength(1000);

        root.getChildren().addAll(title, top, bar, goalsPane);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true); sp.getStyleClass().add("scroll-pane");

        // Load data
        loadGoals(totalTargetLbl, totalCurrentLbl, totalRemainingLbl, activeCountLbl, goalsPane);
        return sp;
    }

    private VBox bigStatCardDyn(String title, Label valueLbl, String change, boolean pos) {
        VBox c = new VBox(8); c.getStyleClass().add("card"); c.setPrefWidth(240);
        Label t = new Label(title); t.getStyleClass().add("card-title");
        Label ch = new Label(change); ch.getStyleClass().addAll("card-change", pos ? "positive" : "negative");
        c.getChildren().addAll(t, valueLbl, ch);
        return c;
    }

    private void loadGoals(Label totalTargetLbl, Label totalCurrentLbl, Label totalRemainingLbl, Label activeCountLbl, FlowPane goalsPane) {
        new Thread(() -> {
            try {
                JsonNode[] d = ApiClient.getJsonArray(userListPath("goals"));
                List<JsonNode> list = d != null ? Arrays.asList(d) : new ArrayList<>();
                double totalTarget = list.stream().mapToDouble(n -> n.get("targetAmount").asDouble()).sum();
                double totalCurrent = list.stream().mapToDouble(n -> n.get("currentAmount").asDouble()).sum();
                int active = list.size();
                Platform.runLater(() -> {
                    goalList.setAll(list);
                    totalTargetLbl.setText(tl.format(totalTarget));
                    totalCurrentLbl.setText(tl.format(totalCurrent));
                    totalRemainingLbl.setText(tl.format(Math.max(0, totalTarget - totalCurrent)));
                    activeCountLbl.setText(active + " aktif");
                    goalsPane.getChildren().clear();
                    for (JsonNode g : list) goalsPane.getChildren().add(goalCard(g));
                });
            } catch (Exception e) {}
        }).start();
    }

    private VBox goalCard(JsonNode goal) {
        String name = goal.has("name") ? goal.get("name").asText() : "";
        double target = goal.has("targetAmount") ? goal.get("targetAmount").asDouble() : 0;
        double current = goal.has("currentAmount") ? goal.get("currentAmount").asDouble() : 0;
        double ratio = target > 0 ? current / target : 0;
        String color = goal.has("color") && !goal.get("color").asText().isEmpty() ? goal.get("color").asText() : "#4f46e5";
        String id = goal.get("id").asText();

        VBox c = new VBox(12); c.getStyleClass().add("card"); c.setPrefWidth(240);
        Label n = new Label(name); n.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label ta = new Label("Hedef: " + tl.format(target)); ta.getStyleClass().add("card-title");
        Label cu = new Label("Biriken: " + tl.format(current)); cu.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 16px; -fx-font-weight: bold;");
        ProgressBar pb = new ProgressBar(Math.min(ratio, 1.0)); pb.setPrefWidth(200); pb.setPrefHeight(14);
        pb.setStyle("-fx-accent: " + color + ";");
        Label per = new Label(String.format("%.0f%%", ratio*100)); per.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        HBox ph = new HBox(10, pb, per); ph.setAlignment(Pos.CENTER_LEFT);

        HBox actions = new HBox(8);
        Button depositBtn = new Button("+ Para Ekle"); depositBtn.getStyleClass().addAll("btn", "btn-success");
        depositBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        depositBtn.setOnAction(e -> showDepositDialog(id, name));
        Button editBtn = new Button("Düzenle"); editBtn.getStyleClass().addAll("btn", "btn-ghost");
        editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        editBtn.setOnAction(e -> showGoalDialog(goal));
        Button delBtn = new Button("Sil"); delBtn.getStyleClass().addAll("btn", "btn-danger");
        delBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        delBtn.setOnAction(e -> confirmDelete("Bu hedefi silmek istediğinize emin misiniz?", "/api/goals/", id, () -> showPage("goals")));
        actions.getChildren().addAll(depositBtn, editBtn, delBtn);

        c.getChildren().addAll(n, ta, cu, ph, actions);
        return c;
    }

    private void showDepositDialog(String goalId, String goalName) {
        Stage d = dialogStage("Para Ekle: " + goalName);
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        TextField amount = new TextField(); amount.setPromptText("0,00"); amount.getStyleClass().add("text-field");
        Button save = new Button("💾 Ekle"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            try {
                ObjectNode body = mapper.createObjectNode();
                body.put("amount", Double.parseDouble(amount.getText().trim().replace(",", ".")));
                ApiClient.patch("/api/goals/" + goalId + "/deposit", mapper.writeValueAsString(body));
                d.close(); showPage("goals");
            } catch (Exception ex) { showApiError("Para eklenemedi", ex); }
        });
        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost"); cancel.setOnAction(e -> d.close());
        root.getChildren().addAll(newLabel("Eklenecek Tutar"), amount, new HBox(10, save, cancel));
        d.setScene(new Scene(root, 320, 200)); d.show();
    }

    private void showGoalDialog(JsonNode existing) {
        Stage d = dialogStage(existing != null ? "Hedef Düzenle" : "Yeni Hedef");
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        TextField name = new TextField(); name.setPromptText("Hedef adı"); name.getStyleClass().add("text-field");
        TextField target = new TextField(); target.setPromptText("Hedef tutar"); target.getStyleClass().add("text-field");
        TextField current = new TextField(); current.setPromptText("Mevcut birikim"); current.getStyleClass().add("text-field");
        DatePicker deadline = new DatePicker(LocalDate.now().plusMonths(12)); deadline.getStyleClass().add("text-field");
        ComboBox<String> color = new ComboBox<>(FXCollections.observableArrayList("#4f46e5", "#10b981", "#f59e0b", "#6366f1", "#00bcd4"));
        color.setValue("#4f46e5"); color.getStyleClass().add("combo-box");
        TextField category = new TextField(); category.setPromptText("Kategori"); category.getStyleClass().add("text-field");

        if (existing != null) {
            name.setText(existing.has("name") ? existing.get("name").asText() : "");
            target.setText(existing.has("targetAmount") ? existing.get("targetAmount").asText() : "");
            current.setText(existing.has("currentAmount") ? existing.get("currentAmount").asText() : "");
            if (existing.has("deadline")) deadline.setValue(LocalDate.parse(existing.get("deadline").asText()));
            if (existing.has("color")) color.setValue(existing.get("color").asText());
            if (existing.has("category")) category.setText(existing.get("category").asText());
        }

        g.add(newLabel("Hedef Adı"), 0, 0); g.add(name, 1, 0);
        g.add(newLabel("Hedef Tutar"), 0, 1); g.add(target, 1, 1);
        g.add(newLabel("Mevcut Birikim"), 0, 2); g.add(current, 1, 2);
        g.add(newLabel("Bitiş Tarihi"), 0, 3); g.add(deadline, 1, 3);
        g.add(newLabel("Renk"), 0, 4); g.add(color, 1, 4);
        g.add(newLabel("Kategori"), 0, 5); g.add(category, 1, 5);

        Button save = new Button("💾 Kaydet"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            String uid = requireCurrentUserId();
            if (uid == null) return;
            try {
                String json = String.format("{\"userId\":\"%s\",\"name\":\"%s\",\"targetAmount\":%s,\"currentAmount\":%s,\"deadline\":\"%s\",\"color\":\"%s\",\"category\":\"%s\"}",
                    uid, name.getText(), target.getText(), current.getText(), deadline.getValue(), color.getValue(), category.getText());
                if (existing != null) ApiClient.put("/api/goals/" + existing.get("id").asText(), json);
                else ApiClient.post("/api/goals", json);
                d.close(); showPage("goals");
            } catch (Exception ex) {}
        });
        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost"); cancel.setOnAction(e -> d.close());
        HBox btns = new HBox(10, save, cancel);
        root.getChildren().addAll(newLabel("Hedef Bilgileri"), g, btns);
        d.setScene(new Scene(root, 440, 480)); d.show();
    }

    // ================ REPORTS ================
    private ScrollPane buildReports() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(10,0,20,0));

        Label title = new Label("📈 Raporlar & Analiz");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        HBox filters = new HBox(10);
        ComboBox<String> period = new ComboBox<>(FXCollections.observableArrayList("30d", "3m", "6m", "1y", "all"));
        period.setValue("6m"); period.getStyleClass().add("combo-box");
        ComboBox<String> reportType = new ComboBox<>(FXCollections.observableArrayList("Gelir/Gider Analizi", "Kategori Dağılımı", "Trend Analizi", "Aylık Karşılaştırma"));
        reportType.setValue("Gelir/Gider Analizi"); reportType.getStyleClass().add("combo-box");
        Button genBtn = new Button("📊 Rapor Oluştur"); genBtn.getStyleClass().addAll("btn", "btn-primary");
        Button exportPdf = new Button("📄 PDF İndir"); exportPdf.getStyleClass().addAll("btn", "btn-ghost");
        filters.getChildren().addAll(period, reportType, genBtn, exportPdf);

        // Charts
        HBox charts = new HBox(16);
        CategoryAxis x = new CategoryAxis(); NumberAxis y = new NumberAxis();
        LineChart<String, Number> line = new LineChart<>(x,y);
        line.setTitle("Gelir / Gider Trendi"); line.setPrefWidth(520); line.setPrefHeight(320);
        line.getStyleClass().add("chart");
        line.setUserData("reportLine");

        VBox stats = new VBox(10); stats.getStyleClass().add("card"); stats.setPrefWidth(360);
        stats.setUserData("reportStats");
        Label st = new Label("📋 Özet İstatistikler"); st.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        stats.getChildren().add(st);

        charts.getChildren().addAll(line, stats);

        HBox bottom = new HBox(16);
        CategoryAxis bx = new CategoryAxis(); NumberAxis by = new NumberAxis();
        BarChart<String, Number> catBar = new BarChart<>(bx, by);
        catBar.setTitle("Kategori Bazlı Harcama"); catBar.setPrefWidth(500); catBar.setPrefHeight(300);
        catBar.getStyleClass().add("chart");
        catBar.setUserData("reportBar");

        VBox insight = new VBox(10); insight.getStyleClass().add("card"); insight.setPrefWidth(380);
        insight.setUserData("reportInsight");
        Label it = new Label("💡 Finansal Öngörüler"); it.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        insight.getChildren().add(it);

        reportSparkline = new SparklineCanvas("Gider Trendi (Sparkline)", Color.web("#4f46e5"));

        VBox sparkBox = new VBox(8, reportSparkline);
        sparkBox.getStyleClass().add("card");

        bottom.getChildren().addAll(sparkBox, catBar, insight);

        root.getChildren().addAll(title, filters, charts, bottom);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true); sp.getStyleClass().add("scroll-pane");

        genBtn.setOnAction(e -> loadReports(period.getValue(), line, catBar, stats, insight));
        exportPdf.setOnAction(e -> {
            new Thread(() -> {
                try {
                    byte[] pdf = ApiClient.getBytes("/api/reports/export/pdf?period=" + period.getValue() + "&userId=" + ApiClient.currentUserId);
                    String filename = "CepteFinans_Rapor_" + java.time.LocalDate.now() + ".pdf";
                    java.nio.file.Files.write(java.nio.file.Path.of(filename), pdf);
                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.INFORMATION, "PDF kaydedildi: " + filename).show();
                        try { java.awt.Desktop.getDesktop().open(new java.io.File(filename)); } catch (Exception ignored) {}
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "PDF oluşturulamadı: " + ex.getMessage()).show());
                }
            }).start();
        });

        loadReports("6m", line, catBar, stats, insight);
        return sp;
    }

    private void loadReports(String period, LineChart<String, Number> line, BarChart<String, Number> catBar, VBox statsBox, VBox insightBox) {
        new Thread(() -> {
            try {
                // Trend
                JsonNode[] trend = ApiClient.getJsonArray("/api/reports/trend?period=" + period);
                // Category
                JsonNode[] cats = ApiClient.getJsonArray("/api/reports/category?period=" + period);
                // Summary
                JsonNode summary = ApiClient.get("/api/reports/summary?period=" + period, JsonNode.class);
                // Insights
                JsonNode[] insights = ApiClient.getJsonArray("/api/reports/insights");

                Platform.runLater(() -> {
                    // Line chart
                    line.getData().clear();
                    XYChart.Series<String, Number> incS = new XYChart.Series<>(); incS.setName("Gelir");
                    XYChart.Series<String, Number> expS = new XYChart.Series<>(); expS.setName("Gider");
                    if (trend != null) {
                        for (JsonNode t : trend) {
                            incS.getData().add(new XYChart.Data<>(t.get("month").asText(), t.get("income").asDouble()));
                            expS.getData().add(new XYChart.Data<>(t.get("month").asText(), t.get("expense").asDouble()));
                        }
                    }
                    line.getData().addAll(incS, expS);

                    if (reportSparkline != null) {
                        List<Double> sparkData = new ArrayList<>();
                        if (trend != null) {
                            for (JsonNode t : trend) {
                                sparkData.add(t.get("expense").asDouble());
                            }
                        }
                        reportSparkline.setData(sparkData);
                    }

                    // Category bar
                    catBar.getData().clear();
                    XYChart.Series<String, Number> catS = new XYChart.Series<>(); catS.setName("Harcama");
                    if (cats != null) {
                        for (JsonNode c : cats) {
                            catS.getData().add(new XYChart.Data<>(c.get("category").asText(), c.get("amount").asDouble()));
                        }
                    }
                    catBar.getData().add(catS);

                    // Stats
                    statsBox.getChildren().clear();
                    Label st = new Label("📋 Özet İstatistikler"); st.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
                    statsBox.getChildren().add(st);
                    if (summary != null) {
                        statsBox.getChildren().addAll(
                            reportRow("Toplam Gelir", tl.format(summary.has("totalIncome") ? summary.get("totalIncome").asDouble() : 0), true),
                            reportRow("Toplam Gider", tl.format(summary.has("totalExpense") ? summary.get("totalExpense").asDouble() : 0), false),
                            reportRow("Net Tasarruf", tl.format(summary.has("netSavings") ? summary.get("netSavings").asDouble() : 0), true),
                            reportRow("Tasarruf Oranı", String.format("%.1f%%", summary.has("savingsRate") ? summary.get("savingsRate").asDouble() : 0), true),
                            reportRow("Toplam İşlem", (summary.has("transactionCount") ? summary.get("transactionCount").asInt() : 0) + " adet", true)
                        );
                    }

                    // Insights
                    insightBox.getChildren().clear();
                    Label it = new Label("💡 Finansal Öngörüler"); it.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
                    insightBox.getChildren().add(it);
                    if (insights != null) {
                        String[] icons = {"💰", "📉", "📈", "🎯", "⚠️"};
                        int i = 0;
                        for (JsonNode in : insights) {
                            insightBox.getChildren().add(insightRow(icons[i % icons.length], in.asText()));
                            i++;
                        }
                    }
                });
            } catch (Exception ex) {}
        }).start();
    }

    private HBox reportRow(String label, String value, boolean pos) {
        HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(6,0,6,0));
        Label l = new Label(label); l.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;"); l.setPrefWidth(180);
        Label v = new Label(value); v.setStyle("-fx-text-fill: " + (pos ? "#10b981" : "#4f46e5") + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        h.getChildren().addAll(l, v);
        return h;
    }

    private HBox insightRow(String icon, String text) {
        HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(8,0,8,0));
        Label i = new Label(icon); i.setStyle("-fx-font-size: 18px;");
        Label t = new Label(text); t.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-wrap-text: true;"); t.setPrefWidth(300);
        h.getChildren().addAll(i, t);
        return h;
    }

    // ================ CURRENCY ================
    private VBox buildCurrency() {
        VBox root = new VBox(20);

        Label title = new Label("💱 Döviz & Kripto Piyasası");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label lastUpdateLbl = new Label("Kurlar yükleniyor…");
        lastUpdateLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Cards row - dynamically filled
        HBox cards = new HBox(16);
        cards.setUserData("currencyCards");

        HBox bottom = new HBox(16);
        VBox tableBox = new VBox(10); tableBox.getStyleClass().add("card"); tableBox.setPrefWidth(600);
        Label tTitle = new Label("📊 Piyasa Özeti"); tTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        TableView<JsonNode> tbl = new TableView<>();
        tbl.getStyleClass().add("table-view");
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.getColumns().addAll(col("Sembol","symbol",80), col("Fiyat","rate",100), col("Düşük","low24h",100), col("Yüksek","high24h",100));

        TableColumn<JsonNode,String> changeCol = new TableColumn<>("Değişim");
        changeCol.setPrefWidth(100);
        changeCol.setCellFactory(tc -> new TableCell<JsonNode,String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || getTableRow() == null || getTableRow().getItem() == null) { setText(""); return; }
                double chg = getTableRow().getItem().get("changePercent24h").asDouble();
                setText(String.format("%.2f%%", chg));
                setStyle(chg >= 0 ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #4f46e5; -fx-font-weight: bold;");
            }
        });
        tbl.getColumns().add(changeCol);
        tbl.getColumns().add(col("Tür","type",80));

        VBox alarmBox = new VBox(10); alarmBox.getStyleClass().add("card"); alarmBox.setPrefWidth(360);
        Label aTitle = new Label("🔔 Fiyat Alarmları"); aTitle.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14px;");
        VBox alarms = new VBox(8); alarms.setUserData("alarmList");
        Button addAlarm = new Button("➕ Alarm Ekle"); addAlarm.getStyleClass().addAll("btn", "btn-primary");
        addAlarm.setOnAction(e -> showAlarmDialog());
        alarmBox.getChildren().addAll(aTitle, alarms, addAlarm);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        Button refresh = new Button("🔄 Yenile"); refresh.getStyleClass().addAll("btn", "btn-ghost");
        refresh.setOnAction(e -> loadCurrency(cards, tbl, alarms, lastUpdateLbl));
        toolbar.getChildren().add(refresh);

        bottom.getChildren().addAll(tableBox, alarmBox);

        root.getChildren().addAll(title, lastUpdateLbl, toolbar, cards, bottom);

        loadCurrency(cards, tbl, alarms, lastUpdateLbl);
        return root;
    }

    private void loadCurrency(HBox cards, TableView<JsonNode> table, VBox alarms, Label lastUpdateLbl) {
        new Thread(() -> {
            try {
                ApiClient.post("/api/currency/rates/refresh", "{}");
                JsonNode[] rates = ApiClient.getJsonArray("/api/currency/rates");
                String alertsPath = ApiClient.currentUserId != null && !ApiClient.currentUserId.isBlank()
                        ? "/api/currency/alerts?userId=" + ApiClient.currentUserId
                        : null;
                JsonNode[] alerts = alertsPath != null ? ApiClient.getJsonArray(alertsPath) : new JsonNode[0];
                Platform.runLater(() -> {
                    cards.getChildren().clear();
                    LocalDateTime newest = null;
                    if (rates != null) {
                        for (JsonNode r : rates) {
                            String sym = r.get("symbol").asText();
                            double rate = r.get("rate").asDouble();
                            boolean crypto = r.has("type") && "CRYPTO".equals(r.get("type").asText());
                            double chg = r.has("changePercent24h") ? r.get("changePercent24h").asDouble() : 0;
                            boolean up = chg >= 0;
                            String icon = crypto ? "₿ " : "💱 ";
                            String priceText = crypto && rate >= 1000
                                    ? String.format("%,.0f ₺", rate)
                                    : String.format("%,.2f ₺", rate);
                            String chgText = crypto || chg != 0
                                    ? String.format("%s%.2f%%", chg >= 0 ? "+" : "", chg)
                                    : "Spot";
                            cards.getChildren().add(currencyCard(icon + sym + "/TRY", priceText, chgText, up || !crypto));
                            if (r.has("lastUpdated") && !r.get("lastUpdated").isNull()) {
                                try {
                                    LocalDateTime lu = LocalDateTime.parse(r.get("lastUpdated").asText().substring(0, 19));
                                    if (newest == null || lu.isAfter(newest)) newest = lu;
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                    table.getItems().setAll(rates != null ? Arrays.asList(rates) : new ArrayList<>());
                    alarms.getChildren().clear();
                    if (alerts != null) {
                        for (JsonNode a : alerts) {
                            String text = a.get("symbol").asText() + " " + a.get("condition").asText() + " " + a.get("targetPrice").asText();
                            boolean active = a.get("active").asBoolean();
                            alarms.getChildren().add(alarmRow(text, active ? "🟢 Aktif" : "🔴 Pasif", a.get("id").asText()));
                        }
                    }
                    if (lastUpdateLbl != null) {
                        if (newest != null) {
                            lastUpdateLbl.setText("Son güncelleme: " + newest.format(
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " · Binance & open.er-api.com");
                        } else if (rates != null && rates.length > 0) {
                            lastUpdateLbl.setText("Canlı kurlar yüklendi");
                        } else {
                            lastUpdateLbl.setText("Kur verisi alınamadı. Backend ve internet bağlantısını kontrol edin.");
                        }
                    }
                });
            } catch (Exception e) {
                showApiError("Döviz/kripto kurları yüklenemedi", e);
            }
        }).start();
    }

    private void showAlarmDialog() {
        Stage d = dialogStage("Yeni Fiyat Alarmı");
        VBox root = new VBox(16); root.getStyleClass().add("dialog-card"); root.setPadding(new Insets(24));
        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        TextField symbol = new TextField(); symbol.setPromptText("Sembol (örn. BTC)"); symbol.getStyleClass().add("text-field");
        TextField target = new TextField(); target.setPromptText("Hedef fiyat"); target.getStyleClass().add("text-field");
        ComboBox<String> cond = new ComboBox<>(FXCollections.observableArrayList("ABOVE", "BELOW")); cond.setValue("ABOVE"); cond.getStyleClass().add("combo-box");
        g.add(newLabel("Sembol"), 0, 0); g.add(symbol, 1, 0);
        g.add(newLabel("Hedef Fiyat"), 0, 1); g.add(target, 1, 1);
        g.add(newLabel("Koşul"), 0, 2); g.add(cond, 1, 2);
        Button save = new Button("💾 Kaydet"); save.getStyleClass().addAll("btn", "btn-primary");
        save.setOnAction(e -> {
            String uid = requireCurrentUserId();
            if (uid == null) return;
            try {
                String json = String.format("{\"userId\":\"%s\",\"symbol\":\"%s\",\"targetPrice\":%s,\"condition\":\"%s\"}",
                    uid, symbol.getText(), target.getText(), cond.getValue());
                ApiClient.post("/api/currency/alerts", json); d.close(); showPage("currency");
            } catch (Exception ex) {}
        });
        Button cancel = new Button("İptal"); cancel.getStyleClass().addAll("btn", "btn-ghost"); cancel.setOnAction(e -> d.close());
        root.getChildren().addAll(newLabel("Alarm Bilgileri"), g, new HBox(10, save, cancel));
        d.setScene(new Scene(root, 400, 320)); d.show();
    }

    private VBox currencyCard(String name, String price, String change, boolean up) {
        VBox c = new VBox(8); c.getStyleClass().add("card"); c.setPrefWidth(160);
        Label n = new Label(name); n.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        Label p = new Label(price); p.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label ch = new Label(change); ch.getStyleClass().addAll("card-change", up ? "positive" : "negative");
        c.getChildren().addAll(n, p, ch);
        return c;
    }

    private HBox alarmRow(String text, String status, String alertId) {
        HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(8));
        h.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8;");
        Label t = new Label(text); t.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;"); t.setPrefWidth(220);
        Label s = new Label(status); s.setStyle("-fx-text-fill: " + (status.contains("Aktif") ? "#10b981" : "#4f46e5") + "; -fx-font-size: 12px;");
        Button toggle = new Button("🔄"); toggle.setStyle("-fx-font-size: 10px; -fx-background-color: transparent;");
        toggle.setOnAction(e -> {
            new Thread(() -> { try { ApiClient.put("/api/currency/alerts/" + alertId + "/toggle"); Platform.runLater(() -> showPage("currency")); } catch (Exception ex) { showApiError("Alarm güncellenemedi", ex); } }).start();
        });
        Button del = new Button("🗑️"); del.setStyle("-fx-font-size: 10px; -fx-background-color: transparent;");
        del.setOnAction(e -> confirmDelete("Bu alarmı silmek istiyor musunuz?", "/api/currency/alerts/", alertId, () -> showPage("currency")));
        h.getChildren().addAll(t, s, toggle, del);
        return h;
    }

    // ================ NOTIFICATIONS ================
    private VBox buildNotifications() {
        VBox root = new VBox(16);
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        Button markAll = new Button("✓ Tümünü Okundu İşaretle"); markAll.getStyleClass().addAll("btn", "btn-primary");
        Button refresh = new Button("🔄 Yenile"); refresh.getStyleClass().addAll("btn", "btn-ghost");
        bar.getChildren().addAll(markAll, refresh);

        TableView<JsonNode> table = new TableView<>(); table.getStyleClass().add("table-view");
        table.setItems(notifList); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(col("Tarih","createdAt",140), col("Tip","type",90), col("Mesaj","message",350), col("Durum","read",90));

        // Detay paneli
        VBox detailPanel = new VBox(10);
        detailPanel.getStyleClass().add("card");
        detailPanel.setPadding(new Insets(16));
        detailPanel.setMaxWidth(600);
        detailPanel.setVisible(false);
        Label detailTitle = new Label("🔔 Bildirim Detayı"); detailTitle.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label detailType = new Label(""); detailType.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        Label detailMsg = new Label(""); detailMsg.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px; -fx-wrap-text: true;"); detailMsg.setPrefWidth(560);
        Label detailDate = new Label(""); detailDate.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        detailPanel.getChildren().addAll(detailTitle, detailType, detailMsg, detailDate);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) {
                detailPanel.setVisible(true);
                detailType.setText("Tip: " + (val.has("type") ? val.get("type").asText() : ""));
                detailMsg.setText(val.has("message") ? val.get("message").asText() : "");
                detailDate.setText("Tarih: " + (val.has("createdAt") ? val.get("createdAt").asText() : ""));
            } else {
                detailPanel.setVisible(false);
            }
        });

        markAll.setOnAction(e -> new Thread(() -> { try {
            JsonNode[] d = ApiClient.getJsonArray(userListPath("notifications"));
            if(d != null) for(JsonNode n : d) if(!n.get("read").asBoolean()) ApiClient.patch("/api/notifications/" + n.get("id").asText() + "/read");
            Platform.runLater(this::loadNotifications);
        } catch(Exception ex){ showApiError("Bildirimler güncellenemedi", ex); } }).start());

        refresh.setOnAction(e -> loadNotifications());
        loadNotifications();

        root.getChildren().addAll(bar, table, detailPanel);
        return root;
    }

    private void loadNotifications() {
        new Thread(() -> {
            try {
                JsonNode[] d = ApiClient.getJsonArray(userListPath("notifications"));
                Platform.runLater(() -> { notifList.setAll(d != null ? d : new JsonNode[0]); });
                long unread = d != null ? Arrays.stream(d).filter(n -> !n.get("read").asBoolean()).count() : 0;
                Platform.runLater(() -> {
                    unreadBadge.setText(String.valueOf(unread));
                    unreadBadge.setVisible(unread > 0);
                    if (sidebarNotifBadge != null) {
                        sidebarNotifBadge.setText(String.valueOf(unread));
                        sidebarNotifBadge.setVisible(unread > 0);
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    // ================ SETTINGS ================
    private ScrollPane buildSettings() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(10,0,20,0));

        Label title = new Label("⚙️ Ayarlar");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Profile
        VBox profile = new VBox(12); profile.getStyleClass().add("card"); profile.setMaxWidth(700);
        Label pTitle = new Label("👤 Profil Bilgileri"); pTitle.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-font-size: 16px;");
        GridPane pg = new GridPane(); pg.setHgap(12); pg.setVgap(12);
        TextField pName = new TextField(); pName.getStyleClass().add("text-field");
        TextField pEmail = new TextField(); pEmail.getStyleClass().add("text-field");
        TextField pPhone = new TextField(); pPhone.getStyleClass().add("text-field");
        pg.add(newLabel("Ad Soyad"), 0, 0); pg.add(pName, 1, 0);
        pg.add(newLabel("E-posta"), 0, 1); pg.add(pEmail, 1, 1);
        pg.add(newLabel("Telefon"), 0, 2); pg.add(pPhone, 1, 2);
        Button pSave = new Button("💾 Kaydet"); pSave.getStyleClass().addAll("btn", "btn-primary");
        pSave.setOnAction(e -> {
            try {
                String json = String.format("{\"userId\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}",
                    ApiClient.currentUserId, pName.getText(), pEmail.getText(), pPhone.getText());
                ApiClient.put("/api/settings/profile", json);
                new Alert(Alert.AlertType.INFORMATION, "Profil kaydedildi.").show();
            } catch (Exception ex) {}
        });
        profile.getChildren().addAll(pTitle, pg, pSave);

        // App settings
        VBox app = new VBox(12); app.getStyleClass().add("card"); app.setMaxWidth(700);
        Label aTitle = new Label("🔧 Uygulama Ayarları"); aTitle.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-font-size: 16px;");
        VBox opts = new VBox(10);
        app.getChildren().addAll(aTitle, opts);

        // Security
        VBox sec = new VBox(12); sec.getStyleClass().add("card"); sec.setMaxWidth(700);
        Label sTitle = new Label("🔐 Güvenlik"); sTitle.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-font-size: 16px;");
        GridPane sg = new GridPane(); sg.setHgap(12); sg.setVgap(12);
        PasswordField oldPass = new PasswordField(); oldPass.setPromptText("Mevcut şifre"); oldPass.getStyleClass().add("text-field");
        PasswordField newPass = new PasswordField(); newPass.setPromptText("Yeni şifre"); newPass.getStyleClass().add("text-field");
        PasswordField confPass = new PasswordField(); confPass.setPromptText("Yeni şifre tekrar"); confPass.getStyleClass().add("text-field");
        sg.add(newLabel("Mevcut Şifre"), 0, 0); sg.add(oldPass, 1, 0);
        sg.add(newLabel("Yeni Şifre"), 0, 1); sg.add(newPass, 1, 1);
        sg.add(newLabel("Tekrar"), 0, 2); sg.add(confPass, 1, 2);
        Button changePass = new Button("🔑 Şifreyi Değiştir"); changePass.getStyleClass().addAll("btn", "btn-primary");
        changePass.setOnAction(e -> {
            if (!newPass.getText().equals(confPass.getText())) {
                new Alert(Alert.AlertType.ERROR, "Yeni şifreler eşleşmiyor.").show(); return;
            }
            try {
                String json = String.format("{\"userId\":\"%s\",\"oldPassword\":\"%s\",\"newPassword\":\"%s\"}",
                        ApiClient.currentUserId, oldPass.getText(), newPass.getText());
                ApiClient.post("/api/settings/change-password", json);
                new Alert(Alert.AlertType.INFORMATION, "Şifre değiştirildi.").show();
            } catch (Exception ex) {}
        });
        sec.getChildren().addAll(sTitle, sg, changePass);

        // Data management
        VBox data = new VBox(12); data.getStyleClass().add("card"); data.setMaxWidth(700);
        Label dTitle = new Label("💾 Veri Yönetimi"); dTitle.setStyle("-fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox dataBtns = new HBox(10);

        Button exportData = new Button("📤 Verimi Dışa Aktar"); exportData.getStyleClass().addAll("btn", "btn-ghost");
        exportData.setOnAction(e -> {
            new Thread(() -> {
                try {
                    JsonNode json = ApiClient.get("/api/settings/export?userId=" + ApiClient.currentUserId, JsonNode.class);
                    String filename = "CepteFinans_Yedek_" + java.time.LocalDate.now() + ".json";
                    java.nio.file.Files.writeString(java.nio.file.Path.of(filename), json.toPrettyString());
                    Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Yedek kaydedildi: " + filename).show());
                } catch (Exception ex) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Dışa aktarma başarısız: " + ex.getMessage()).show());
                }
            }).start();
        });

        Button importData = new Button("📥 Veri İçe Aktar"); importData.getStyleClass().addAll("btn", "btn-ghost");
        importData.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Yedek Dosyası Seç");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Dosyaları", "*.json"));
            File file = fc.showOpenDialog(null);
            if (file != null) {
                new Thread(() -> {
                    try {
                        String content = java.nio.file.Files.readString(file.toPath());
                        ApiClient.post("/api/settings/import", content);
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.INFORMATION, "Veriler içe aktarıldı.").show();
                            loadSettings(pName, pEmail, pPhone, opts);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "İçe aktarma başarısız: " + ex.getMessage()).show());
                    }
                }).start();
            }
        });

        Button deleteData = new Button("🗑️ Tüm Verileri Sil"); deleteData.getStyleClass().addAll("btn", "btn-danger");
        deleteData.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Onay");
            confirm.setHeaderText("Tüm ayar ve profil verileriniz silinecek!");
            confirm.setContentText("Bu işlem geri alınamaz. Devam etmek istiyor musunuz?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    new Thread(() -> {
                        try {
                            ApiClient.delete("/api/settings/data?userId=" + ApiClient.currentUserId);
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.INFORMATION, "Tüm veriler silindi.").show();
                                loadSettings(pName, pEmail, pPhone, opts);
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Silme başarısız: " + ex.getMessage()).show());
                        }
                    }).start();
                }
            });
        });

        dataBtns.getChildren().addAll(exportData, importData, deleteData);
        data.getChildren().addAll(dTitle, dataBtns);

        root.getChildren().addAll(title, profile, app, sec, data);
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true); sp.getStyleClass().add("scroll-pane");

        // Load settings
        loadSettings(pName, pEmail, pPhone, opts);
        return sp;
    }

    private void loadSettings(TextField pName, TextField pEmail, TextField pPhone, VBox opts) {
        new Thread(() -> {
            try {
                JsonNode profile = ApiClient.get("/api/settings/profile?userId=" + ApiClient.currentUserId, JsonNode.class);
                JsonNode settings = ApiClient.get("/api/settings?userId=" + ApiClient.currentUserId, JsonNode.class);
                Platform.runLater(() -> {
                    if (profile != null) {
                        if (profile.has("fullName")) pName.setText(profile.get("fullName").asText());
                        if (profile.has("email")) pEmail.setText(profile.get("email").asText());
                        if (profile.has("phone")) pPhone.setText(profile.get("phone").asText());
                    }
                    if (settings != null) {
                        opts.getChildren().clear();
                        opts.getChildren().addAll(
                            settingRowApi("🌙 Koyu Tema", settings.has("darkMode") && settings.get("darkMode").asBoolean(), "darkMode", settings),
                            settingRowApi("🔔 Bildirimler", settings.has("notificationsEnabled") && settings.get("notificationsEnabled").asBoolean(), "notificationsEnabled", settings),
                            settingRowApi("📧 E-posta Özeti", settings.has("emailSummary") && settings.get("emailSummary").asBoolean(), "emailSummary", settings),
                            settingRowApi("🔒 İki Faktörlü Doğrulama", settings.has("twoFactorEnabled") && settings.get("twoFactorEnabled").asBoolean(), "twoFactorEnabled", settings),
                            settingRowApi("💱 Döviz Alarmları", settings.has("currencyAlertsEnabled") && settings.get("currencyAlertsEnabled").asBoolean(), "currencyAlertsEnabled", settings),
                            settingRowApi("📊 Haftalık Rapor", settings.has("weeklyReportEnabled") && settings.get("weeklyReportEnabled").asBoolean(), "weeklyReportEnabled", settings)
                        );
                    }
                });
            } catch (Exception e) {}
        }).start();
    }

    private HBox settingRowApi(String label, boolean initial, String field, JsonNode settings) {
        HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(8,0,8,0));
        h.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");
        Label l = new Label(label); l.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px;"); l.setPrefWidth(300);
        ToggleButton tb = new ToggleButton(initial ? "Açık" : "Kapalı");
        tb.setSelected(initial);
        tb.setStyle(initial ? "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 6 14; -fx-font-weight: bold;" : "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-background-radius: 10; -fx-padding: 6 14;");
        tb.setOnAction(e -> {
            boolean sel = tb.isSelected();
            tb.setText(sel ? "Açık" : "Kapalı");
            tb.setStyle(sel ? "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 6 14; -fx-font-weight: bold;" : "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-background-radius: 10; -fx-padding: 6 14;");
            // Save to API
            new Thread(() -> {
                try {
                    ObjectNode updated = settings.deepCopy();
                    updated.put(field, sel);
                    ApiClient.put("/api/settings", mapper.writeValueAsString(updated));
                } catch (Exception ex) { showApiError("Ayar kaydedilemedi", ex); }
            }).start();
        });
        h.getChildren().addAll(l, tb);
        return h;
    }

    private HBox settingRow(String label, boolean initial) {
        HBox h = new HBox(10); h.setAlignment(Pos.CENTER_LEFT); h.setPadding(new Insets(8,0,8,0));
        h.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");
        Label l = new Label(label); l.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px;"); l.setPrefWidth(300);
        ToggleButton tb = new ToggleButton(initial ? "Açık" : "Kapalı");
        tb.setSelected(initial);
        tb.setStyle(initial ? "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 6 14; -fx-font-weight: bold;" : "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-background-radius: 10; -fx-padding: 6 14;");
        tb.setOnAction(e -> {
            boolean sel = tb.isSelected();
            tb.setText(sel ? "Açık" : "Kapalı");
            tb.setStyle(sel ? "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 6 14; -fx-font-weight: bold;" : "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-background-radius: 10; -fx-padding: 6 14;");
        });
        h.getChildren().addAll(l, tb);
        return h;
    }

    // ================ HELPERS ================
    private TableColumn<JsonNode, String> col(String name, String prop, int w) {
        TableColumn<JsonNode, String> c = new TableColumn<>(name);
        c.setPrefWidth(w);
        c.setCellValueFactory(data -> {
            JsonNode n = data.getValue().get(prop);
            if(n == null || n.isNull()) return new javafx.beans.property.SimpleStringProperty("");
            if(n.isNumber()) {
                double v = n.asDouble();
                String pl = prop.toLowerCase();
                if (pl.contains("price") || pl.contains("amount") || pl.equals("rate")
                        || pl.contains("24h") || pl.contains("balance")) {
                    return new javafx.beans.property.SimpleStringProperty(tl.format(v));
                }
                return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", v));
            }
            return new javafx.beans.property.SimpleStringProperty(n.asText());
        });
        return c;
    }

    private Label newLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        return l;
    }

    private Stage dialogStage(String title) {
        Stage s = new Stage();
        s.setTitle(title);
        return s;
    }

    public static void main(String[] args) { launch(args); }
}


