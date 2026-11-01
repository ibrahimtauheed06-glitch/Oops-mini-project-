import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * FoodWasteApplet - Professional Java AWT Applet for the Food Waste Management System.
 *
 * Architecture: BorderLayout (Header | Sidebar | CardLayout Content | StatusBar)
 * Compatible: Java 8 AWT Applet / AppletViewer
 *
 * Screens:
 *   1. Executive Dashboard (KPI analytics + expiry-aware stats)
 *   2. Food Inventory     (available food only, expiry-filtered)
 *   3. Add Donation       (full validation, provider link)
 *   4. Edit / Delete      (safe audit checks)
 *   5. Search & Filter    (keyword, category, status, provider)
 *   6. Providers          (register, update, remove)
 *   7. Collections        (transactional distribution log)
 *   8. Waste Records      (spoilage audit log)
 *   9. DB Settings        (live credential configuration)
 */
public class FoodWasteApplet extends Applet implements ActionListener, ItemListener {

    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // DESIGN TOKENS  (Green-themed SaaS palette)
    // -------------------------------------------------------------------------
    private static final Color CLR_DARK_GREEN  = new Color(0x14, 0x53, 0x2D); // #14532D sidebar bg
    private static final Color CLR_GREEN       = new Color(0x16, 0xA3, 0x4A); // #16A34A primary action
    private static final Color CLR_LIGHT_GREEN = new Color(0xDC, 0xFC, 0xE7); // #DCFCE7 active sidebar item
    private static final Color CLR_BG          = new Color(0xF8, 0xFA, 0xFC); // #F8FAFC page background
    private static final Color CLR_WHITE       = Color.WHITE;
    private static final Color CLR_TEXT        = new Color(0x1E, 0x29, 0x3B); // #1E293B headings
    private static final Color CLR_SECONDARY   = new Color(0x64, 0x74, 0x8B); // #64748B secondary text
    private static final Color CLR_BORDER      = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0 card border
    private static final Color CLR_WARNING     = new Color(0xF5, 0x9E, 0x0B); // #F59E0B warning / amber
    private static final Color CLR_DANGER      = new Color(0xDC, 0x26, 0x26); // #DC2626 danger / red

    // Sidebar highlight - active item background
    private static final Color CLR_SIDEBAR_ACTIVE = new Color(0x00, 0x64, 0x2C); // darker green

    // Header gradient top
    private static final Color CLR_HEADER_TOP  = new Color(0x0F, 0x3D, 0x21);
    private static final Color CLR_HEADER_BOT  = new Color(0x14, 0x53, 0x2D);

    // KPI accent colours (icon strip left edge)
    private static final Color CLR_KPI_TOTAL   = new Color(0x06, 0x96, 0x48); // emerald
    private static final Color CLR_KPI_AVAIL   = new Color(0x16, 0xA3, 0x4A); // green
    private static final Color CLR_KPI_QTY     = new Color(0x0E, 0x86, 0xD4); // blue
    private static final Color CLR_KPI_PROV    = new Color(0x7C, 0x3A, 0xED); // purple
    private static final Color CLR_KPI_COLL    = new Color(0x0E, 0x74, 0xB3); // teal-blue
    private static final Color CLR_KPI_COLLQTY = new Color(0x05, 0x82, 0x70); // teal
    private static final Color CLR_KPI_EXPIRED = new Color(0xF5, 0x9E, 0x0B); // amber
    private static final Color CLR_KPI_WASTE   = new Color(0xDC, 0x26, 0x26); // red

    // -------------------------------------------------------------------------
    // CARD IDENTIFIERS
    // -------------------------------------------------------------------------
    private static final String CARD_DASHBOARD   = "DASHBOARD";
    private static final String CARD_AVAILABLE   = "AVAILABLE";
    private static final String CARD_ADD_FOOD    = "ADD_FOOD";
    private static final String CARD_EDIT_FOOD   = "EDIT_FOOD";
    private static final String CARD_SEARCH      = "SEARCH";
    private static final String CARD_PROVIDERS   = "PROVIDERS";
    private static final String CARD_COLLECTIONS = "COLLECTIONS";
    private static final String CARD_WASTE       = "WASTE";
    private static final String CARD_SETTINGS    = "SETTINGS";

    // -------------------------------------------------------------------------
    // DATA ACCESS OBJECTS
    // -------------------------------------------------------------------------
    private final FoodDAO       foodDAO       = new FoodDAO();
    private final ProviderDAO   providerDAO   = new ProviderDAO();
    private final CollectionDAO collectionDAO = new CollectionDAO();
    private final WasteDAO      wasteDAO      = new WasteDAO();
    private final DashboardDAO  dashboardDAO  = new DashboardDAO();

    // -------------------------------------------------------------------------
    // LAYOUT COMPONENTS
    // -------------------------------------------------------------------------
    private CardLayout cardLayout;
    private Panel      cardsPanel;

    // Sidebar nav buttons (stored for highlight toggling)
    private Button btnNavDashboard, btnNavAvailable, btnNavAddFood, btnNavEditFood;
    private Button btnNavSearch, btnNavProviders, btnNavCollections, btnNavWaste, btnNavSettings;
    private Panel sidebarPanel; // the west sidebar panel

    // Status bar
    private Label lblStatus;
    private Label lblDbDot; // connection indicator dot

    // ========================== DASHBOARD ====================================
    private Label lblDashTotalDonations, lblDashAvailableCount, lblDashAvailableQty;
    private Label lblDashProvidersCount, lblDashCollectionsCount, lblDashCollectedQty;
    private Label lblDashExpiredCount, lblDashWasteQty;
    private TextArea txtDashRecent;
    private Button   btnDashRefresh;

    // ========================== AVAILABLE FOOD ===============================
    private TextArea txtAvailableTable;
    private Button   btnAvailRefresh, btnAvailGoAdd, btnAvailGoCollect;

    // ========================== ADD FOOD =====================================
    private TextField txtAddFoodName, txtAddQuantity, txtAddPrepDate, txtAddExpiryDate;
    private Choice    chAddCategory, chAddUnit, chAddProvider;
    private Button    btnAddSubmit, btnAddClear;

    // ========================== EDIT / DELETE FOOD ===========================
    private Choice    chEditSelectFood;
    private TextField txtEditFoodName, txtEditQuantity, txtEditPrepDate, txtEditExpiryDate;
    private Choice    chEditCategory, chEditUnit, chEditStatus, chEditProvider;
    private Button    btnEditLoad, btnEditUpdate, btnEditDelete, btnEditClear;

    // ========================== SEARCH & FILTER ==============================
    private TextField txtSearchKeyword;
    private Choice    chSearchCategory, chSearchStatus, chSearchProvider;
    private Button    btnSearchExecute, btnSearchReset;
    private TextArea  txtSearchResults;
    private Label     lblSearchSummary;

    // ========================== PROVIDERS ====================================
    private TextArea  txtProvidersTable;
    private Choice    chProvSelect;
    private TextField txtProvName, txtProvPhone, txtProvAddress;
    private Button    btnProvAdd, btnProvUpdate, btnProvDelete, btnProvRefresh, btnProvClear;

    // ========================== COLLECTIONS ==================================
    private Choice    chCollFoodItem;
    private TextField txtCollCollectorName, txtCollQuantity;
    private Label     lblCollAvailableInfo;
    private Button    btnCollSubmit, btnCollRefresh;
    private TextArea  txtCollTable;

    // ========================== WASTE RECORDS ================================
    private Choice    chWasteFoodItem;
    private TextField txtWasteQuantity, txtWasteCustomReason;
    private Choice    chWasteReason;
    private Label     lblWasteAvailableInfo;
    private Button    btnWasteSubmit, btnWasteRefresh;
    private TextArea  txtWasteTable;

    // ========================== DB SETTINGS ==================================
    private TextField txtDbHost, txtDbPort, txtDbName, txtDbUser, txtDbPass;
    private Label     lblDbUrlDisplay;
    private Button    btnDbSaveTest;

    // Cached data for dropdowns
    private List<Provider>      cachedProviders;
    private List<FoodItemModel> cachedAvailableFood;
    private List<FoodItemModel> cachedAllFood;

    // =========================================================================
    // APPLET LIFECYCLE
    // =========================================================================

    @Override
    public void init() {
        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);

        // Center: CardLayout content area
        cardLayout  = new CardLayout();
        cardsPanel  = new Panel(cardLayout);
        cardsPanel.setBackground(CLR_BG);

        cardsPanel.add(createDashboardPanel(),   CARD_DASHBOARD);
        cardsPanel.add(createAvailableFoodPanel(), CARD_AVAILABLE);
        cardsPanel.add(createAddFoodPanel(),     CARD_ADD_FOOD);
        cardsPanel.add(createEditFoodPanel(),    CARD_EDIT_FOOD);
        cardsPanel.add(createSearchPanel(),      CARD_SEARCH);
        cardsPanel.add(createProvidersPanel(),   CARD_PROVIDERS);
        cardsPanel.add(createCollectionsPanel(), CARD_COLLECTIONS);
        cardsPanel.add(createWastePanel(),       CARD_WASTE);
        cardsPanel.add(createSettingsPanel(),    CARD_SETTINGS);

        add(cardsPanel, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        // Load initial data
        try {
            refreshAllData();
        } catch (Throwable t) {
            System.err.println("[Applet Init Warning] " + t.getMessage());
            setStatus("Init note: " + t.getMessage(), true);
        }
        showCard(CARD_DASHBOARD);
    }

    // =========================================================================
    // HEADER
    // =========================================================================

    private Panel createHeader() {
        // Two-tone header: gradient-like via nested panels
        Panel header = new Panel(new BorderLayout(0, 0));
        header.setBackground(CLR_HEADER_TOP);

        Panel leftZone = new Panel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftZone.setBackground(CLR_HEADER_TOP);

        // Green square "logo" placeholder drawn as a Label with solid background
        Label logoBox = new Label("FW", Label.CENTER);
        logoBox.setFont(new Font("SansSerif", Font.BOLD, 15));
        logoBox.setForeground(CLR_WHITE);
        logoBox.setBackground(CLR_GREEN);
        leftZone.add(logoBox);

        // Title + subtitle stacked
        Panel titleStack = new Panel(new GridLayout(2, 1, 0, 0));
        titleStack.setBackground(CLR_HEADER_TOP);

        Label lblTitle = new Label("Food Waste Management System");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(CLR_WHITE);

        Label lblSub = new Label("Smart Food Donation & Distribution Platform");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(new Color(0xA7, 0xF3, 0xD0)); // mint green

        titleStack.add(lblTitle);
        titleStack.add(lblSub);
        leftZone.add(titleStack);

        header.add(leftZone, BorderLayout.WEST);

        // Right: DB connection status indicator
        Panel rightZone = new Panel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        rightZone.setBackground(CLR_HEADER_TOP);

        lblDbDot = new Label("  Connected  ", Label.CENTER);
        lblDbDot.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblDbDot.setForeground(CLR_WHITE);
        lblDbDot.setBackground(CLR_GREEN);

        Label lblDbInfo = new Label("food_waste_db @ localhost");
        lblDbInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblDbInfo.setForeground(new Color(0xBB, 0xF7, 0xD0));

        rightZone.add(lblDbDot);
        rightZone.add(lblDbInfo);
        header.add(rightZone, BorderLayout.EAST);

        return header;
    }

    // =========================================================================
    // SIDEBAR NAVIGATION
    // =========================================================================

    private Panel createSidebar() {
        Panel sidebarOuter = new Panel(new BorderLayout(0, 0));
        sidebarOuter.setBackground(CLR_DARK_GREEN);
        sidebarOuter.setPreferredSize(new Dimension(190, 680));

        sidebarPanel = new Panel();
        sidebarPanel.setLayout(new GridLayout(11, 1, 0, 6)); // 6px vertical spacing between buttons
        sidebarPanel.setBackground(CLR_DARK_GREEN);

        // Section label
        Label lblNav = new Label("  NAVIGATION", Label.LEFT);
        lblNav.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblNav.setForeground(new Color(0x86, 0xEF, 0xAC)); // light mint green
        lblNav.setBackground(CLR_DARK_GREEN);
        sidebarPanel.add(lblNav);

        btnNavDashboard   = makeSidebarButton("[#]  Dashboard");
        btnNavAvailable   = makeSidebarButton("[*]  Food Inventory");
        btnNavAddFood     = makeSidebarButton("[+]  Add Donation");
        btnNavEditFood    = makeSidebarButton("[/]  Edit / Delete");
        btnNavSearch      = makeSidebarButton("[?]  Search & Filter");
        btnNavProviders   = makeSidebarButton("[@]  Providers");
        btnNavCollections = makeSidebarButton("[^]  Collections");
        btnNavWaste       = makeSidebarButton("[!]  Waste Records");
        btnNavSettings    = makeSidebarButton("[-]  DB Settings");

        sidebarPanel.add(btnNavDashboard);
        sidebarPanel.add(btnNavAvailable);
        sidebarPanel.add(btnNavAddFood);
        sidebarPanel.add(btnNavEditFood);
        sidebarPanel.add(btnNavSearch);
        sidebarPanel.add(btnNavProviders);
        sidebarPanel.add(btnNavCollections);
        sidebarPanel.add(btnNavWaste);
        sidebarPanel.add(btnNavSettings);

        // Filler panel at bottom to push buttons to top
        Panel filler = new Panel();
        filler.setBackground(CLR_DARK_GREEN);
        sidebarPanel.add(filler);

        sidebarOuter.add(sidebarPanel, BorderLayout.CENTER);
        return sidebarOuter;
    }

    private Button makeSidebarButton(String label) {
        Button b = new Button("   " + label);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setBackground(CLR_DARK_GREEN);
        b.setForeground(new Color(0xD1, 0xFA, 0xE5)); // green-100
        b.addActionListener(this);
        return b;
    }

    // =========================================================================
    // STATUS BAR
    // =========================================================================

    private Panel createStatusBar() {
        Panel bar = new Panel(new BorderLayout(0, 0));
        bar.setBackground(CLR_BORDER);

        lblStatus = new Label("  Ready  |  Connected to MySQL [food_waste_db]");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(CLR_TEXT);
        bar.add(lblStatus, BorderLayout.WEST);

        Label lblCopy = new Label("Food Waste Management System  |  Java 8 AWT Applet  ");
        lblCopy.setFont(new Font("SansSerif", Font.ITALIC, 10));
        lblCopy.setForeground(CLR_SECONDARY);
        bar.add(lblCopy, BorderLayout.EAST);

        return bar;
    }

    // =========================================================================
    // DASHBOARD PANEL
    // =========================================================================

    private Panel createDashboardPanel() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(CLR_BG);

        outer.add(makeSectionHeader("Executive Dashboard", "Live analytics from food_waste_db"), BorderLayout.NORTH);

        Panel centerZone = new Panel(new BorderLayout(8, 8));
        centerZone.setBackground(CLR_BG);

        // 8 KPI cards in 2 rows x 4 cols with uniform spacing and consistent height
        Panel kpiGrid = new Panel(new GridLayout(2, 4, 12, 12));
        kpiGrid.setBackground(CLR_BG);

        lblDashTotalDonations  = makeKpiCard(kpiGrid, "Total Donations",   "0 items",      CLR_KPI_TOTAL);
        lblDashAvailableCount  = makeKpiCard(kpiGrid, "Available Items",    "0 active",     CLR_KPI_AVAIL);
        lblDashAvailableQty    = makeKpiCard(kpiGrid, "Available Quantity", "0.0 total",    CLR_KPI_QTY);
        lblDashProvidersCount  = makeKpiCard(kpiGrid, "Registered Donors",  "0 providers",  CLR_KPI_PROV);
        lblDashCollectionsCount= makeKpiCard(kpiGrid, "Collections",        "0 fulfilled",  CLR_KPI_COLL);
        lblDashCollectedQty    = makeKpiCard(kpiGrid, "Food Distributed",   "0.0 units",    CLR_KPI_COLLQTY);
        lblDashExpiredCount    = makeKpiCard(kpiGrid, "Past Expiry",        "0 items",      CLR_KPI_EXPIRED);
        lblDashWasteQty        = makeKpiCard(kpiGrid, "Total Wasted",       "0.0 units",    CLR_KPI_WASTE);

        centerZone.add(kpiGrid, BorderLayout.NORTH);

        // Recent Activity log
        Panel recentBox = new Panel(new BorderLayout(4, 4));
        recentBox.setBackground(CLR_WHITE);

        Panel recentHeader = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        recentHeader.setBackground(CLR_LIGHT_GREEN);
        Label lblRecent = new Label("Recent Food Inventory  (latest 8 records)");
        lblRecent.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblRecent.setForeground(CLR_DARK_GREEN);
        recentHeader.add(lblRecent);
        recentBox.add(recentHeader, BorderLayout.NORTH);

        // Ensure horizontal scrolling works when needed via SCROLLBARS_BOTH
        txtDashRecent = new TextArea("", 10, 100, TextArea.SCROLLBARS_BOTH);
        txtDashRecent.setEditable(false);
        txtDashRecent.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDashRecent.setBackground(CLR_WHITE);
        txtDashRecent.setForeground(CLR_TEXT);
        recentBox.add(txtDashRecent, BorderLayout.CENTER);

        centerZone.add(recentBox, BorderLayout.CENTER);
        outer.add(centerZone, BorderLayout.CENTER);

        Panel bottom = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottom.setBackground(CLR_BG);
        btnDashRefresh = makeStyledButton("Refresh Dashboard", CLR_GREEN, CLR_WHITE);
        bottom.add(btnDashRefresh);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    /**
     * Creates a KPI card panel and returns the value Label for later update.
     * Features a 6px colored left-edge accent strip, uppercase sub-caption,
     * and a prominent 18pt bold metric display.
     */
    private Label makeKpiCard(Panel parent, String title, String defaultVal, Color accentColor) {
        // Outer card wrapper with consistent border/white background
        Panel wrapper = new Panel(new BorderLayout(0, 0));
        wrapper.setBackground(CLR_WHITE);

        // Accent strip (6px wide)
        Panel strip = new Panel();
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(6, 1));
        wrapper.add(strip, BorderLayout.WEST);

        // Card body: 2-row grid with title and prominent statistic
        Panel card = new Panel(new GridLayout(2, 1, 0, 4));
        card.setBackground(CLR_WHITE);

        Label lblTitle = new Label("  " + title.toUpperCase());
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTitle.setForeground(CLR_SECONDARY);
        lblTitle.setBackground(CLR_WHITE);

        Label lblVal = new Label("  " + defaultVal);
        lblVal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblVal.setForeground(CLR_TEXT);
        lblVal.setBackground(CLR_WHITE);

        card.add(lblTitle);
        card.add(lblVal);

        wrapper.add(card, BorderLayout.CENTER);
        parent.add(wrapper);
        return lblVal;
    }

    // =========================================================================
    // AVAILABLE FOOD PANEL
    // =========================================================================

    private Panel createAvailableFoodPanel() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Food Inventory", "Currently available items (expiry-filtered)"), BorderLayout.NORTH);

        Panel tableBox = new Panel(new BorderLayout(0, 0));
        tableBox.setBackground(CLR_WHITE);

        // Column header bar
        Panel colHeader = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        colHeader.setBackground(CLR_DARK_GREEN);
        Label colLbl = new Label("   ID   | Food Name                | Category         | Available Qty    | Expiry Date  | Provider               | Status");
        colLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        colLbl.setForeground(CLR_WHITE);
        colHeader.add(colLbl);
        tableBox.add(colHeader, BorderLayout.NORTH);

        txtAvailableTable = new TextArea("", 16, 100, TextArea.SCROLLBARS_BOTH);
        txtAvailableTable.setEditable(false);
        txtAvailableTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAvailableTable.setBackground(CLR_WHITE);
        txtAvailableTable.setForeground(CLR_TEXT);
        tableBox.add(txtAvailableTable, BorderLayout.CENTER);

        outer.add(tableBox, BorderLayout.CENTER);

        Panel bottom = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottom.setBackground(CLR_BG);
        btnAvailRefresh   = makeStyledButton("Refresh", CLR_SECONDARY, CLR_WHITE);
        btnAvailGoAdd     = makeStyledButton("+ Add Donation", CLR_GREEN, CLR_WHITE);
        btnAvailGoCollect = makeStyledButton("Record Collection", CLR_KPI_COLL, CLR_WHITE);
        btnAvailRefresh.addActionListener(this);
        btnAvailGoAdd.addActionListener(this);
        btnAvailGoCollect.addActionListener(this);
        bottom.add(btnAvailRefresh);
        bottom.add(btnAvailGoAdd);
        bottom.add(btnAvailGoCollect);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    // =========================================================================
    // ADD FOOD DONATION PANEL
    // =========================================================================

    private Panel createAddFoodPanel() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Add Food Donation", "Register surplus food for distribution"), BorderLayout.NORTH);

        Panel formCard = makeCardPanel();
        GridBagConstraints gbc = defaultGbc();
        int row = 0;

        row = addFormRow(formCard, gbc, row, "Food Item Name  *", txtAddFoodName = new TextField(30));
        row = addFormRowChoice(formCard, gbc, row, "Category  *", chAddCategory = makeCategoryChoice());
        row = addFormRowQtyUnit(formCard, gbc, row, "Quantity & Unit  *",
                txtAddQuantity = new TextField(10), chAddUnit = makeUnitChoice());
        row = addFormRow(formCard, gbc, row, "Preparation Date  (YYYY-MM-DD)", txtAddPrepDate = new TextField(15));
        txtAddPrepDate.setText(todayDateString());
        row = addFormRow(formCard, gbc, row, "Expiry Date  (YYYY-MM-DD)  *", txtAddExpiryDate = new TextField(15));
        txtAddExpiryDate.setText(tomorrowDateString());
        row = addFormRowChoice(formCard, gbc, row, "Food Provider  *", chAddProvider = new Choice());

        outer.add(formCard, BorderLayout.CENTER);

        Panel btns = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btns.setBackground(CLR_BG);
        btnAddSubmit = makeStyledButton("Submit Donation", CLR_GREEN, CLR_WHITE);
        btnAddClear  = makeStyledButton("Clear Form", CLR_SECONDARY, CLR_WHITE);
        btnAddSubmit.addActionListener(this);
        btnAddClear.addActionListener(this);
        btns.add(btnAddSubmit);
        btns.add(btnAddClear);
        outer.add(btns, BorderLayout.SOUTH);

        return outer;
    }

    // =========================================================================
    // EDIT / DELETE FOOD PANEL
    // =========================================================================

    private Panel createEditFoodPanel() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Edit / Delete Food Records", "Modify or remove existing food entries"), BorderLayout.NORTH);

        Panel formCard = makeCardPanel();
        GridBagConstraints gbc = defaultGbc();
        int row = 0;

        // Select + Load button on same row
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formCard.add(makeFieldLabel("Select Food Record :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        Panel selRow = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        selRow.setBackground(CLR_WHITE);
        chEditSelectFood = new Choice();
        chEditSelectFood.addItemListener(this);
        btnEditLoad = makeStyledButton("Load", CLR_SECONDARY, CLR_WHITE);
        btnEditLoad.addActionListener(this);
        selRow.add(chEditSelectFood);
        selRow.add(btnEditLoad);
        formCard.add(selRow, gbc);
        row++;

        row = addFormRow(formCard, gbc, row, "Food Name", txtEditFoodName = new TextField(30));
        row = addFormRowChoice(formCard, gbc, row, "Category", chEditCategory = makeCategoryChoice());
        row = addFormRowQtyUnit(formCard, gbc, row, "Quantity & Unit",
                txtEditQuantity = new TextField(10), chEditUnit = makeUnitChoice());
        row = addFormRow(formCard, gbc, row, "Prep Date  (YYYY-MM-DD)", txtEditPrepDate = new TextField(15));
        row = addFormRow(formCard, gbc, row, "Expiry Date  (YYYY-MM-DD)", txtEditExpiryDate = new TextField(15));
        row = addFormRowChoice(formCard, gbc, row, "Status", chEditStatus = makeStatusChoice());
        row = addFormRowChoice(formCard, gbc, row, "Provider", chEditProvider = new Choice());

        outer.add(formCard, BorderLayout.CENTER);

        Panel btns = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btns.setBackground(CLR_BG);
        btnEditUpdate = makeStyledButton("Save Changes", CLR_GREEN, CLR_WHITE);
        btnEditDelete = makeStyledButton("Delete Record", CLR_DANGER, CLR_WHITE);
        btnEditClear  = makeStyledButton("Clear", CLR_SECONDARY, CLR_WHITE);
        btnEditUpdate.addActionListener(this);
        btnEditDelete.addActionListener(this);
        btnEditClear.addActionListener(this);
        btns.add(btnEditUpdate);
        btns.add(btnEditDelete);
        btns.add(btnEditClear);
        outer.add(btns, BorderLayout.SOUTH);

        return outer;
    }

    // =========================================================================
    // SEARCH & FILTER PANEL
    // =========================================================================

    private Panel createSearchPanel() {
        Panel outer = new Panel(new BorderLayout(6, 6));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Search & Filter", "Find food records by keyword, category, status, or provider"), BorderLayout.NORTH);

        // Filter bar card
        Panel filterCard = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(CLR_WHITE);

        filterCard.add(makeFieldLabel("Keyword:"));
        txtSearchKeyword = new TextField(12);
        filterCard.add(txtSearchKeyword);

        filterCard.add(makeFieldLabel("Category:"));
        chSearchCategory = makeCategoryChoiceWithAll();
        filterCard.add(chSearchCategory);

        filterCard.add(makeFieldLabel("Status:"));
        chSearchStatus = makeStatusChoiceWithAll();
        filterCard.add(chSearchStatus);

        filterCard.add(makeFieldLabel("Provider:"));
        chSearchProvider = new Choice();
        filterCard.add(chSearchProvider);

        btnSearchExecute = makeStyledButton("Search", CLR_GREEN, CLR_WHITE);
        btnSearchReset   = makeStyledButton("Reset", CLR_SECONDARY, CLR_WHITE);
        btnSearchExecute.addActionListener(this);
        btnSearchReset.addActionListener(this);
        filterCard.add(btnSearchExecute);
        filterCard.add(btnSearchReset);

        outer.add(filterCard, BorderLayout.NORTH);

        // Results area
        Panel resultsBox = new Panel(new BorderLayout(4, 4));
        resultsBox.setBackground(CLR_WHITE);

        Panel resultsHeader = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        resultsHeader.setBackground(CLR_LIGHT_GREEN);
        lblSearchSummary = new Label("Showing all records.");
        lblSearchSummary.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblSearchSummary.setForeground(CLR_DARK_GREEN);
        resultsHeader.add(lblSearchSummary);
        resultsBox.add(resultsHeader, BorderLayout.NORTH);

        txtSearchResults = new TextArea("", 14, 100, TextArea.SCROLLBARS_BOTH);
        txtSearchResults.setEditable(false);
        txtSearchResults.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtSearchResults.setBackground(CLR_WHITE);
        txtSearchResults.setForeground(CLR_TEXT);
        resultsBox.add(txtSearchResults, BorderLayout.CENTER);

        outer.add(resultsBox, BorderLayout.CENTER);
        return outer;
    }

    // =========================================================================
    // PROVIDERS PANEL
    // =========================================================================

    private Panel createProvidersPanel() {
        Panel outer = new Panel(new BorderLayout(6, 6));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Food Providers", "Manage registered donors and food suppliers"), BorderLayout.NORTH);

        // Providers table
        Panel tableBox = new Panel(new BorderLayout(0, 0));
        tableBox.setBackground(CLR_WHITE);

        Panel colHdr = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        colHdr.setBackground(CLR_DARK_GREEN);
        Label colLbl = new Label("   ID   | Provider / Donor Name      | Contact Phone      | Address                          | Items");
        colLbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        colLbl.setForeground(CLR_WHITE);
        colHdr.add(colLbl);
        tableBox.add(colHdr, BorderLayout.NORTH);

        txtProvidersTable = new TextArea("", 8, 100, TextArea.SCROLLBARS_BOTH);
        txtProvidersTable.setEditable(false);
        txtProvidersTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtProvidersTable.setBackground(CLR_WHITE);
        txtProvidersTable.setForeground(CLR_TEXT);
        tableBox.add(txtProvidersTable, BorderLayout.CENTER);

        outer.add(tableBox, BorderLayout.CENTER);

        // Bottom: Provider form
        Panel formCard = new Panel(new BorderLayout(4, 4));
        formCard.setBackground(CLR_WHITE);

        Panel formTitle = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formTitle.setBackground(CLR_LIGHT_GREEN);
        Label fl = new Label("Add / Edit Provider");
        fl.setFont(new Font("SansSerif", Font.BOLD, 11));
        fl.setForeground(CLR_DARK_GREEN);
        formTitle.add(fl);
        formCard.add(formTitle, BorderLayout.NORTH);

        Panel form = new Panel(new GridLayout(4, 2, 8, 6));
        form.setBackground(CLR_WHITE);
        form.add(makeFieldLabel("Select to Edit / Delete:"));
        chProvSelect = new Choice();
        chProvSelect.addItemListener(this);
        form.add(chProvSelect);
        form.add(makeFieldLabel("Provider / Donor Name  *:"));
        txtProvName = new TextField(25);
        form.add(txtProvName);
        form.add(makeFieldLabel("Contact Phone:"));
        txtProvPhone = new TextField(20);
        form.add(txtProvPhone);
        form.add(makeFieldLabel("Physical Address:"));
        txtProvAddress = new TextField(30);
        form.add(txtProvAddress);
        formCard.add(form, BorderLayout.CENTER);

        Panel btnBar = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        btnBar.setBackground(CLR_WHITE);
        btnProvAdd     = makeStyledButton("+ Add",    CLR_GREEN,     CLR_WHITE);
        btnProvUpdate  = makeStyledButton("Update",   new Color(0x0E, 0x74, 0xB3), CLR_WHITE);
        btnProvDelete  = makeStyledButton("Delete",   CLR_DANGER,    CLR_WHITE);
        btnProvRefresh = makeStyledButton("Refresh",  CLR_SECONDARY, CLR_WHITE);
        btnProvClear   = makeStyledButton("Clear",    CLR_SECONDARY, CLR_WHITE);
        btnProvAdd.addActionListener(this);
        btnProvUpdate.addActionListener(this);
        btnProvDelete.addActionListener(this);
        btnProvRefresh.addActionListener(this);
        btnProvClear.addActionListener(this);
        btnBar.add(btnProvAdd);
        btnBar.add(btnProvUpdate);
        btnBar.add(btnProvDelete);
        btnBar.add(btnProvRefresh);
        btnBar.add(btnProvClear);
        formCard.add(btnBar, BorderLayout.SOUTH);

        outer.add(formCard, BorderLayout.SOUTH);
        return outer;
    }

    // =========================================================================
    // COLLECTIONS PANEL
    // =========================================================================

    private Panel createCollectionsPanel() {
        Panel outer = new Panel(new BorderLayout(6, 6));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Collections", "Log food distributions to NGOs, shelters & volunteers"), BorderLayout.NORTH);

        // Form card
        Panel formCard = new Panel(new BorderLayout(4, 4));
        formCard.setBackground(CLR_WHITE);

        Panel formTitle = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formTitle.setBackground(CLR_LIGHT_GREEN);
        Label ft = new Label("Record a New Collection");
        ft.setFont(new Font("SansSerif", Font.BOLD, 11));
        ft.setForeground(CLR_DARK_GREEN);
        formTitle.add(ft);
        formCard.add(formTitle, BorderLayout.NORTH);

        Panel form = new Panel(new GridLayout(3, 2, 8, 8));
        form.setBackground(CLR_WHITE);

        form.add(makeFieldLabel("Food Item  *:"));
        chCollFoodItem = new Choice();
        chCollFoodItem.addItemListener(this);
        form.add(chCollFoodItem);

        form.add(makeFieldLabel("Collector / NGO  *:"));
        txtCollCollectorName = new TextField(25);
        form.add(txtCollCollectorName);

        form.add(makeFieldLabel("Quantity to Collect  *:"));
        Panel qtyRow = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        qtyRow.setBackground(CLR_WHITE);
        txtCollQuantity = new TextField(8);
        lblCollAvailableInfo = new Label("(select food item above)");
        lblCollAvailableInfo.setForeground(CLR_SECONDARY);
        lblCollAvailableInfo.setFont(new Font("SansSerif", Font.ITALIC, 11));
        qtyRow.add(txtCollQuantity);
        qtyRow.add(lblCollAvailableInfo);
        form.add(qtyRow);

        formCard.add(form, BorderLayout.CENTER);

        Panel btnBar = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        btnBar.setBackground(CLR_WHITE);
        btnCollSubmit  = makeStyledButton("Confirm Collection", CLR_KPI_COLL, CLR_WHITE);
        btnCollRefresh = makeStyledButton("Refresh History",    CLR_SECONDARY, CLR_WHITE);
        btnCollSubmit.addActionListener(this);
        btnCollRefresh.addActionListener(this);
        btnBar.add(btnCollSubmit);
        btnBar.add(btnCollRefresh);
        formCard.add(btnBar, BorderLayout.SOUTH);

        outer.add(formCard, BorderLayout.NORTH);

        // History log
        Panel histBox = new Panel(new BorderLayout(0, 0));
        histBox.setBackground(CLR_WHITE);

        Panel histHdr = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        histHdr.setBackground(CLR_DARK_GREEN);
        Label hl = new Label("   ID   | Food Item                  | Collector / Organization   | Qty Collected    | Collection Date");
        hl.setFont(new Font("Monospaced", Font.BOLD, 12));
        hl.setForeground(CLR_WHITE);
        histHdr.add(hl);
        histBox.add(histHdr, BorderLayout.NORTH);

        txtCollTable = new TextArea("", 10, 100, TextArea.SCROLLBARS_BOTH);
        txtCollTable.setEditable(false);
        txtCollTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtCollTable.setBackground(CLR_WHITE);
        txtCollTable.setForeground(CLR_TEXT);
        histBox.add(txtCollTable, BorderLayout.CENTER);

        outer.add(histBox, BorderLayout.CENTER);
        return outer;
    }

    // =========================================================================
    // WASTE RECORDS PANEL
    // =========================================================================

    private Panel createWastePanel() {
        Panel outer = new Panel(new BorderLayout(6, 6));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Waste Records", "Log and audit food spoilage events"), BorderLayout.NORTH);

        // Form card
        Panel formCard = new Panel(new BorderLayout(4, 4));
        formCard.setBackground(CLR_WHITE);

        Panel formTitle = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formTitle.setBackground(new Color(0xFF, 0xED, 0xED)); // light red
        Label ft = new Label("Log a Spoilage / Waste Event");
        ft.setFont(new Font("SansSerif", Font.BOLD, 11));
        ft.setForeground(CLR_DANGER);
        formTitle.add(ft);
        formCard.add(formTitle, BorderLayout.NORTH);

        Panel form = new Panel(new GridLayout(3, 2, 8, 8));
        form.setBackground(CLR_WHITE);

        form.add(makeFieldLabel("Food Item  *:"));
        chWasteFoodItem = new Choice();
        chWasteFoodItem.addItemListener(this);
        form.add(chWasteFoodItem);

        form.add(makeFieldLabel("Quantity Wasted  *:"));
        Panel qtyRow = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        qtyRow.setBackground(CLR_WHITE);
        txtWasteQuantity = new TextField(8);
        lblWasteAvailableInfo = new Label("(select food item above)");
        lblWasteAvailableInfo.setForeground(CLR_SECONDARY);
        lblWasteAvailableInfo.setFont(new Font("SansSerif", Font.ITALIC, 11));
        qtyRow.add(txtWasteQuantity);
        qtyRow.add(lblWasteAvailableInfo);
        form.add(qtyRow);

        form.add(makeFieldLabel("Reason:"));
        Panel reasonRow = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        reasonRow.setBackground(CLR_WHITE);
        chWasteReason = new Choice();
        chWasteReason.add("Expired");
        chWasteReason.add("Spoiled / Sour");
        chWasteReason.add("Contaminated");
        chWasteReason.add("Packaging Damaged");
        chWasteReason.add("Storage Malfunction");
        chWasteReason.add("Overcooked / Unfit");
        chWasteReason.add("Other");
        txtWasteCustomReason = new TextField(16);
        reasonRow.add(chWasteReason);
        reasonRow.add(new Label("or custom:"));
        reasonRow.add(txtWasteCustomReason);
        form.add(reasonRow);

        formCard.add(form, BorderLayout.CENTER);

        Panel btnBar = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        btnBar.setBackground(CLR_WHITE);
        btnWasteSubmit  = makeStyledButton("Record Waste Event", CLR_DANGER, CLR_WHITE);
        btnWasteRefresh = makeStyledButton("Refresh Records",    CLR_SECONDARY, CLR_WHITE);
        btnWasteSubmit.addActionListener(this);
        btnWasteRefresh.addActionListener(this);
        btnBar.add(btnWasteSubmit);
        btnBar.add(btnWasteRefresh);
        formCard.add(btnBar, BorderLayout.SOUTH);

        outer.add(formCard, BorderLayout.NORTH);

        // Audit log table
        Panel histBox = new Panel(new BorderLayout(0, 0));
        histBox.setBackground(CLR_WHITE);

        Panel histHdr = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        histHdr.setBackground(new Color(0x7F, 0x1D, 0x1D)); // dark red
        Label hl = new Label("   ID   | Food Item                  | Qty Wasted       | Reason for Spoilage / Waste    | Date Logged");
        hl.setFont(new Font("Monospaced", Font.BOLD, 12));
        hl.setForeground(CLR_WHITE);
        histHdr.add(hl);
        histBox.add(histHdr, BorderLayout.NORTH);

        txtWasteTable = new TextArea("", 10, 100, TextArea.SCROLLBARS_BOTH);
        txtWasteTable.setEditable(false);
        txtWasteTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtWasteTable.setBackground(CLR_WHITE);
        txtWasteTable.setForeground(CLR_TEXT);
        histBox.add(txtWasteTable, BorderLayout.CENTER);

        outer.add(histBox, BorderLayout.CENTER);
        return outer;
    }

    // =========================================================================
    // DATABASE SETTINGS PANEL
    // =========================================================================

    private Panel createSettingsPanel() {
        Panel outer = new Panel(new BorderLayout(8, 8));
        outer.setBackground(CLR_BG);
        outer.add(makeSectionHeader("Database Settings", "Live connection configuration"), BorderLayout.NORTH);

        Panel formCard = makeCardPanel();
        GridBagConstraints gbc = defaultGbc();
        int row = 0;

        row = addFormRow(formCard, gbc, row, "Database Host:", txtDbHost = new TextField(AppletDB.getHost(), 22));
        row = addFormRow(formCard, gbc, row, "Database Port:", txtDbPort = new TextField(AppletDB.getPort(), 10));
        row = addFormRow(formCard, gbc, row, "Database Name:", txtDbName = new TextField(AppletDB.getDbName(), 22));
        row = addFormRow(formCard, gbc, row, "Database User:", txtDbUser = new TextField(AppletDB.getUser(), 22));

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.25;
        formCard.add(makeFieldLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.75;
        txtDbPass = new TextField(22);
        txtDbPass.setEchoChar('*');
        txtDbPass.setText(AppletDB.getPassword());
        formCard.add(txtDbPass, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(makeFieldLabel("Current JDBC URL:"), gbc);
        gbc.gridx = 1;
        lblDbUrlDisplay = new Label(AppletDB.getJdbcUrl());
        lblDbUrlDisplay.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblDbUrlDisplay.setForeground(CLR_SECONDARY);
        formCard.add(lblDbUrlDisplay, gbc);

        outer.add(formCard, BorderLayout.CENTER);

        Panel bottom = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.setBackground(CLR_BG);
        btnDbSaveTest = makeStyledButton("Apply & Test Connection", CLR_GREEN, CLR_WHITE);
        btnDbSaveTest.addActionListener(this);
        bottom.add(btnDbSaveTest);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    // =========================================================================
    // UI BUILDER HELPERS
    // =========================================================================

    /** Two-line section header strip used at top of each card. */
    private Panel makeSectionHeader(String title, String subtitle) {
        Panel p = new Panel(new GridLayout(2, 1, 0, 0));
        p.setBackground(CLR_DARK_GREEN);

        Label lblT = new Label("  " + title);
        lblT.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblT.setForeground(CLR_WHITE);
        lblT.setBackground(CLR_DARK_GREEN);

        Label lblS = new Label("  " + subtitle);
        lblS.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblS.setForeground(new Color(0xBB, 0xF7, 0xD0)); // green-200
        lblS.setBackground(CLR_DARK_GREEN);

        p.add(lblT);
        p.add(lblS);
        return p;
    }

    /** White card-style panel with GridBagLayout. */
    private Panel makeCardPanel() {
        Panel p = new Panel(new GridBagLayout());
        p.setBackground(CLR_WHITE);
        return p;
    }

    /** Default GridBagConstraints for form rows. */
    private GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    /** Adds a label + textfield row to a GridBagLayout panel. Returns next row index. */
    private int addFormRow(Panel p, GridBagConstraints gbc, int row, String labelText, TextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(makeFieldLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(field, gbc);
        return row + 1;
    }

    /** Adds a label + Choice row. */
    private int addFormRowChoice(Panel p, GridBagConstraints gbc, int row, String labelText, Choice choice) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(makeFieldLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(choice, gbc);
        return row + 1;
    }

    /** Adds a label + qty field + unit choice row. */
    private int addFormRowQtyUnit(Panel p, GridBagConstraints gbc, int row, String labelText, TextField qField, Choice uChoice) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(makeFieldLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        Panel rowPnl = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rowPnl.setBackground(CLR_WHITE);
        rowPnl.add(qField);
        rowPnl.add(uChoice);
        p.add(rowPnl, gbc);
        return row + 1;
    }

    private Label makeFieldLabel(String text) {
        Label l = new Label(text, Label.RIGHT);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(CLR_TEXT);
        l.setBackground(CLR_WHITE);
        return l;
    }

    /** Styled action button with custom background and foreground. */
    private Button makeStyledButton(String label, Color bg, Color fg) {
        Button b = new Button(label);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setBackground(bg);
        b.setForeground(fg);
        return b;
    }

    private Choice makeCategoryChoice() {
        Choice c = new Choice();
        c.add("Cooked Meals");
        c.add("Bakery");
        c.add("Produce (Fruits/Vegetables)");
        c.add("Dairy");
        c.add("Canned Goods");
        c.add("Packaged Snacks");
        c.add("Beverages");
        c.add("Other");
        return c;
    }

    private Choice makeCategoryChoiceWithAll() {
        Choice c = new Choice();
        c.add("ALL");
        c.add("Cooked Meals");
        c.add("Bakery");
        c.add("Produce (Fruits/Vegetables)");
        c.add("Dairy");
        c.add("Canned Goods");
        c.add("Packaged Snacks");
        c.add("Beverages");
        c.add("Other");
        return c;
    }

    private Choice makeUnitChoice() {
        Choice c = new Choice();
        c.add("servings");
        c.add("loaves");
        c.add("kg");
        c.add("packets");
        c.add("liters");
        c.add("boxes");
        return c;
    }

    private Choice makeStatusChoice() {
        Choice c = new Choice();
        c.add("AVAILABLE");
        c.add("PARTIALLY_COLLECTED");
        c.add("COLLECTED");
        c.add("EXPIRED");
        c.add("WASTED");
        return c;
    }

    private Choice makeStatusChoiceWithAll() {
        Choice c = new Choice();
        c.add("ALL");
        c.add("AVAILABLE");
        c.add("PARTIALLY_COLLECTED");
        c.add("COLLECTED");
        c.add("EXPIRED");
        c.add("WASTED");
        return c;
    }

    // =========================================================================
    // EVENT HANDLING & CARD SWITCHING
    // =========================================================================

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // Sidebar Navigation
        if      (src == btnNavDashboard)   { showCard(CARD_DASHBOARD);   loadDashboard(); }
        else if (src == btnNavAvailable)   { showCard(CARD_AVAILABLE);   loadAvailableFood(); }
        else if (src == btnNavAddFood)     { showCard(CARD_ADD_FOOD); }
        else if (src == btnNavEditFood)    { showCard(CARD_EDIT_FOOD);   loadAllFoodIntoEditChoice(); }
        else if (src == btnNavSearch)      { showCard(CARD_SEARCH);      executeSearch(); }
        else if (src == btnNavProviders)   { showCard(CARD_PROVIDERS);   loadProviders(); }
        else if (src == btnNavCollections) { showCard(CARD_COLLECTIONS); loadCollections(); }
        else if (src == btnNavWaste)       { showCard(CARD_WASTE);       loadWasteRecords(); }
        else if (src == btnNavSettings)    { showCard(CARD_SETTINGS); }

        // Dashboard
        else if (src == btnDashRefresh)    { loadDashboard(); }

        // Available Food
        else if (src == btnAvailRefresh)   { loadAvailableFood(); }
        else if (src == btnAvailGoAdd)     { showCard(CARD_ADD_FOOD); }
        else if (src == btnAvailGoCollect) { showCard(CARD_COLLECTIONS); loadCollections(); }

        // Add Food
        else if (src == btnAddSubmit)      { handleAddFoodSubmit(); }
        else if (src == btnAddClear)       { clearAddFoodForm(); }

        // Edit Food
        else if (src == btnEditLoad)       { handleLoadEditFood(); }
        else if (src == btnEditUpdate)     { handleUpdateFood(); }
        else if (src == btnEditDelete)     { handleDeleteFood(); }
        else if (src == btnEditClear)      { clearEditFoodForm(); }

        // Search
        else if (src == btnSearchExecute)  { executeSearch(); }
        else if (src == btnSearchReset)    { resetSearch(); }

        // Providers
        else if (src == btnProvAdd)        { handleAddProvider(); }
        else if (src == btnProvUpdate)     { handleUpdateProvider(); }
        else if (src == btnProvDelete)     { handleDeleteProvider(); }
        else if (src == btnProvRefresh)    { loadProviders(); }
        else if (src == btnProvClear)      { clearProviderForm(); }

        // Collections
        else if (src == btnCollSubmit)     { handleRecordCollection(); }
        else if (src == btnCollRefresh)    { loadCollections(); }

        // Waste
        else if (src == btnWasteSubmit)    { handleRecordWaste(); }
        else if (src == btnWasteRefresh)   { loadWasteRecords(); }

        // DB Settings
        else if (src == btnDbSaveTest)     { handleSaveAndTestDb(); }
    }

    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        if      (src == chEditSelectFood) { handleLoadEditFood(); }
        else if (src == chProvSelect)     { handleLoadProviderDetails(); }
        else if (src == chCollFoodItem)   { updateCollQuantityHint(); }
        else if (src == chWasteFoodItem)  { updateWasteQuantityHint(); }
    }

    private void showCard(String cardName) {
        cardLayout.show(cardsPanel, cardName);
        highlightSidebarButton(cardName);
    }

    /** Applies active / inactive styling to all sidebar nav buttons. */
    private void highlightSidebarButton(String activeCard) {
        Button[] btns  = { btnNavDashboard, btnNavAvailable, btnNavAddFood, btnNavEditFood,
                           btnNavSearch, btnNavProviders, btnNavCollections, btnNavWaste, btnNavSettings };
        String[] cards = { CARD_DASHBOARD, CARD_AVAILABLE, CARD_ADD_FOOD, CARD_EDIT_FOOD,
                           CARD_SEARCH, CARD_PROVIDERS, CARD_COLLECTIONS, CARD_WASTE, CARD_SETTINGS };
        String[] baseLabels = {
            "[#]  Dashboard",
            "[*]  Food Inventory",
            "[+]  Add Donation",
            "[/]  Edit / Delete",
            "[?]  Search & Filter",
            "[@]  Providers",
            "[^]  Collections",
            "[!]  Waste Records",
            "[-]  DB Settings"
        };

        for (int i = 0; i < btns.length; i++) {
            if (cards[i].equals(activeCard)) {
                btns[i].setBackground(CLR_GREEN); // Vibrant green highlight #16A34A
                btns[i].setForeground(CLR_WHITE);
                btns[i].setFont(new Font("SansSerif", Font.BOLD, 12));
                btns[i].setLabel(" > " + baseLabels[i]);
            } else {
                btns[i].setBackground(CLR_DARK_GREEN);
                btns[i].setForeground(new Color(0xD1, 0xFA, 0xE5)); // Soft mint #D1FAE5
                btns[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
                btns[i].setLabel("   " + baseLabels[i]);
            }
        }
    }

    private void setStatus(String message, boolean isError) {
        if (lblStatus != null) {
            lblStatus.setText("  " + message);
            lblStatus.setForeground(isError ? CLR_DANGER : CLR_GREEN);
        }
    }

    // =========================================================================
    // BUSINESS LOGIC & DATA OPERATIONS
    // =========================================================================

    private void refreshAllData() {
        try {
            loadProviders();
            loadDashboard();
            loadAvailableFood();
            populateProviderDropdowns();
        } catch (Throwable t) {
            System.err.println("[Applet Data Load Notice] " + t.getMessage());
            setStatus("Database: " + t.getMessage(), true);
        }
    }

    // -------------------------------------------------------------------------
    // 1. Dashboard
    // -------------------------------------------------------------------------
    private void loadDashboard() {
        try {
            DashboardStats stats = dashboardDAO.getStats();

            lblDashTotalDonations.setText("  " + stats.getTotalFoodItems() + " items");
            lblDashAvailableCount.setText("  " + stats.getAvailableFoodItems() + " active");
            lblDashAvailableQty.setText(String.format("  %.1f total", stats.getTotalAvailableQuantity()));
            lblDashProvidersCount.setText("  " + stats.getTotalProviders() + " registered");

            lblDashCollectionsCount.setText("  " + stats.getTotalCollections() + " fulfilled");
            lblDashCollectedQty.setText(String.format("  %.1f units", stats.getTotalQuantityCollected()));
            lblDashExpiredCount.setText("  " + stats.getExpiredFoodItems() + " items");
            lblDashWasteQty.setText(String.format("  %.1f units", stats.getTotalQuantityWasted()));

            // Color the "Past Expiry" KPI label text in amber when > 0
            lblDashExpiredCount.setForeground(stats.getExpiredFoodItems() > 0 ? CLR_WARNING : CLR_TEXT);

            // Recent activity log with full un-truncated quantity & units and dynamic EXPIRED status
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-24s | %-16s | %-18s | %-12s | %-12s | %-22s\n",
                "ID", "Food Name", "Category", "Quantity & Unit", "Status", "Expiry Date", "Provider"));
            sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");

            List<FoodItemModel> all = foodDAO.getAllFoodItems();
            int count = 0;
            for (FoodItemModel f : all) {
                if (count++ >= 8) break;
                String qtyUnit = String.format("%.1f %s", f.getQuantity(), f.getUnit());
                String displayStatus = getDisplayStatus(f);
                sb.append(String.format("%-6d | %-24s | %-16s | %-18s | %-12s | %-12s | %-22s\n",
                    f.getFoodId(), truncate(f.getFoodName(), 24), truncate(f.getCategory(), 16),
                    qtyUnit, displayStatus,
                    (f.getExpiryDate() != null ? f.getExpiryDate() : "N/A"),
                    truncate(f.getProviderName(), 22)));
            }

            txtDashRecent.setText(sb.toString());
            setStatus("Dashboard refreshed  |  Available: " + stats.getAvailableFoodItems()
                + "  |  Expired: " + stats.getExpiredFoodItems(), false);
        } catch (SQLException e) {
            handleSqlError("loading dashboard", e);
        }
    }

    // -------------------------------------------------------------------------
    // 2. Available Food
    // -------------------------------------------------------------------------
    private void loadAvailableFood() {
        try {
            cachedAvailableFood = foodDAO.getAvailableFoodItems();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-24s | %-16s | %-18s | %-12s | %-22s | %-12s\n",
                "ID", "Food Name", "Category", "Available Qty", "Expiry Date", "Provider", "Status"));
            sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");

            if (cachedAvailableFood.isEmpty()) {
                sb.append("  No surplus food items currently available for collection (all may be expired or collected).\n");
            } else {
                for (FoodItemModel f : cachedAvailableFood) {
                    String qtyUnit = String.format("%.1f %s", f.getQuantity(), f.getUnit());
                    sb.append(String.format("%-6d | %-24s | %-16s | %-18s | %-12s | %-22s | %-12s\n",
                        f.getFoodId(), truncate(f.getFoodName(), 24), truncate(f.getCategory(), 16),
                        qtyUnit,
                        (f.getExpiryDate() != null ? f.getExpiryDate() : "N/A"),
                        truncate(f.getProviderName(), 22), f.getStatus()));
                }
            }

            txtAvailableTable.setText(sb.toString());
            setStatus("Loaded " + cachedAvailableFood.size() + " available food item(s)  (expiry-filtered).", false);
            updateAvailableDropdowns();
        } catch (SQLException e) {
            handleSqlError("loading available food", e);
        }
    }

    // -------------------------------------------------------------------------
    // 3. Add Food Donation
    // -------------------------------------------------------------------------
    private void handleAddFoodSubmit() {
        String name   = txtAddFoodName.getText().trim();
        String cat    = chAddCategory.getSelectedItem();
        String qtyStr = txtAddQuantity.getText().trim();
        String unit   = chAddUnit.getSelectedItem();
        String prep   = txtAddPrepDate.getText().trim();
        String expiry = txtAddExpiryDate.getText().trim();

        if (name.isEmpty()) {
            setStatus("Validation: Food item name cannot be blank.", true); return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) { setStatus("Validation: Quantity must be > 0.", true); return; }
        } catch (NumberFormatException ex) {
            setStatus("Validation: Enter a valid numeric quantity.", true); return;
        }
        if (expiry.isEmpty() || !isValidDate(expiry)) {
            setStatus("Validation: Enter a valid expiry date (YYYY-MM-DD).", true); return;
        }
        int providerId = getSelectedProviderId(chAddProvider);
        if (providerId <= 0) {
            setStatus("Validation: Please select a valid food provider.", true); return;
        }

        FoodItemModel item = new FoodItemModel();
        item.setFoodName(name);
        item.setCategory(cat);
        item.setQuantity(qty);
        item.setUnit(unit);
        item.setPreparationDate(prep.isEmpty() ? null : prep);
        item.setExpiryDate(expiry);
        item.setStatus("AVAILABLE");
        item.setProviderId(providerId);

        try {
            if (foodDAO.addFoodItem(item)) {
                setStatus("Donation '" + name + "' added successfully!", false);
                clearAddFoodForm();
                loadAvailableFood();
                loadDashboard();
            } else {
                setStatus("Could not add food donation. Check inputs.", true);
            }
        } catch (SQLException e) {
            handleSqlError("adding food donation", e);
        }
    }

    private void clearAddFoodForm() {
        txtAddFoodName.setText("");
        txtAddQuantity.setText("");
        txtAddPrepDate.setText(todayDateString());
        txtAddExpiryDate.setText(tomorrowDateString());
        if (chAddCategory.getItemCount() > 0) chAddCategory.select(0);
        if (chAddUnit.getItemCount()     > 0) chAddUnit.select(0);
    }

    // -------------------------------------------------------------------------
    // 4. Update / Delete Food
    // -------------------------------------------------------------------------
    private void loadAllFoodIntoEditChoice() {
        try {
            cachedAllFood = foodDAO.getAllFoodItems();
            chEditSelectFood.removeAll();
            if (cachedAllFood.isEmpty()) {
                chEditSelectFood.add("-- No food items found --");
            } else {
                for (FoodItemModel f : cachedAllFood) {
                    chEditSelectFood.add(String.format("#%d - %s (%.1f %s)",
                        f.getFoodId(), f.getFoodName(), f.getQuantity(), f.getUnit()));
                }
            }
            handleLoadEditFood();
        } catch (SQLException e) {
            handleSqlError("loading food for edit", e);
        }
    }

    private void handleLoadEditFood() {
        if (cachedAllFood == null || cachedAllFood.isEmpty() || chEditSelectFood.getSelectedIndex() < 0) return;
        int idx = chEditSelectFood.getSelectedIndex();
        if (idx < cachedAllFood.size()) {
            FoodItemModel f = cachedAllFood.get(idx);
            txtEditFoodName.setText(f.getFoodName());
            txtEditQuantity.setText(String.valueOf(f.getQuantity()));
            txtEditPrepDate.setText(f.getPreparationDate() != null ? f.getPreparationDate() : "");
            txtEditExpiryDate.setText(f.getExpiryDate() != null ? f.getExpiryDate() : "");
            selectChoiceByPrefix(chEditCategory, f.getCategory());
            selectChoiceByPrefix(chEditUnit, f.getUnit());
            selectChoiceByPrefix(chEditStatus, f.getStatus());
            selectProviderChoiceById(chEditProvider, f.getProviderId());
        }
    }

    private void handleUpdateFood() {
        if (cachedAllFood == null || cachedAllFood.isEmpty() || chEditSelectFood.getSelectedIndex() < 0) {
            setStatus("No food item selected.", true); return;
        }
        FoodItemModel f = cachedAllFood.get(chEditSelectFood.getSelectedIndex());
        String name   = txtEditFoodName.getText().trim();
        String qtyStr = txtEditQuantity.getText().trim();
        String expiry = txtEditExpiryDate.getText().trim();

        if (name.isEmpty()) { setStatus("Validation: Food name cannot be empty.", true); return; }
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty < 0) { setStatus("Validation: Quantity cannot be negative.", true); return; }
        } catch (NumberFormatException ex) {
            setStatus("Validation: Invalid quantity number.", true); return;
        }
        if (expiry.isEmpty() || !isValidDate(expiry)) {
            setStatus("Validation: Expiry date must be in YYYY-MM-DD format.", true); return;
        }

        int providerId = getSelectedProviderId(chEditProvider);
        if (providerId <= 0) providerId = f.getProviderId();

        f.setFoodName(name);
        f.setCategory(chEditCategory.getSelectedItem());
        f.setQuantity(qty);
        f.setUnit(chEditUnit.getSelectedItem());
        f.setPreparationDate(txtEditPrepDate.getText().trim().isEmpty() ? null : txtEditPrepDate.getText().trim());
        f.setExpiryDate(expiry);
        f.setStatus(chEditStatus.getSelectedItem());
        f.setProviderId(providerId);

        try {
            if (foodDAO.updateFoodItem(f)) {
                setStatus("Updated food item #" + f.getFoodId() + " successfully!", false);
                loadAllFoodIntoEditChoice();
                loadAvailableFood();
                loadDashboard();
            } else {
                setStatus("Update failed.", true);
            }
        } catch (SQLException e) {
            handleSqlError("updating food item", e);
        }
    }

    private void handleDeleteFood() {
        if (cachedAllFood == null || cachedAllFood.isEmpty() || chEditSelectFood.getSelectedIndex() < 0) {
            setStatus("No food item selected for deletion.", true); return;
        }
        FoodItemModel f = cachedAllFood.get(chEditSelectFood.getSelectedIndex());
        try {
            if (foodDAO.deleteFoodItem(f.getFoodId())) {
                setStatus("Deleted food item #" + f.getFoodId() + " ('" + f.getFoodName() + "').", false);
                clearEditFoodForm();
                loadAllFoodIntoEditChoice();
                loadAvailableFood();
                loadDashboard();
            } else {
                setStatus("Could not delete food item.", true);
            }
        } catch (SQLException e) {
            handleSqlError("deleting food item", e);
        }
    }

    private void clearEditFoodForm() {
        txtEditFoodName.setText("");
        txtEditQuantity.setText("");
        txtEditPrepDate.setText("");
        txtEditExpiryDate.setText("");
    }

    // -------------------------------------------------------------------------
    // 5. Search & Filter
    // -------------------------------------------------------------------------
    private void executeSearch() {
        String kw    = txtSearchKeyword.getText().trim();
        String cat   = chSearchCategory.getSelectedItem();
        String st    = chSearchStatus.getSelectedItem();
        int provId   = getSelectedProviderId(chSearchProvider);

        try {
            List<FoodItemModel> results = foodDAO.searchAndFilter(kw, cat, st, provId);
            lblSearchSummary.setText("Found " + results.size() + " matching record(s).");

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-24s | %-16s | %-18s | %-12s | %-12s | %-22s\n",
                "ID", "Food Name", "Category", "Quantity & Unit", "Status", "Expiry Date", "Provider"));
            sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");

            for (FoodItemModel f : results) {
                String qtyUnit = String.format("%.1f %s", f.getQuantity(), f.getUnit());
                String displayStatus = getDisplayStatus(f);
                sb.append(String.format("%-6d | %-24s | %-16s | %-18s | %-12s | %-12s | %-22s\n",
                    f.getFoodId(), truncate(f.getFoodName(), 24), truncate(f.getCategory(), 16),
                    qtyUnit, displayStatus,
                    (f.getExpiryDate() != null ? f.getExpiryDate() : "N/A"),
                    truncate(f.getProviderName(), 22)));
            }

            txtSearchResults.setText(sb.toString());
            setStatus("Search completed: " + results.size() + " record(s) found.", false);
        } catch (SQLException e) {
            handleSqlError("executing search", e);
        }
    }

    private void resetSearch() {
        txtSearchKeyword.setText("");
        chSearchCategory.select(0);
        chSearchStatus.select(0);
        chSearchProvider.select(0);
        executeSearch();
    }

    // -------------------------------------------------------------------------
    // 6. Providers
    // -------------------------------------------------------------------------
    private void loadProviders() {
        try {
            cachedProviders = providerDAO.getAllProviders();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-28s | %-18s | %-32s | %-8s\n",
                "ID", "Provider / Donor Name", "Contact Phone", "Address", "Items"));
            sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");

            for (Provider p : cachedProviders) {
                sb.append(String.format("%-6d | %-28s | %-18s | %-32s | %-8d\n",
                    p.getProviderId(), truncate(p.getProviderName(), 28),
                    (p.getPhone() != null ? p.getPhone() : "N/A"),
                    (p.getAddress() != null ? truncate(p.getAddress(), 32) : "N/A"),
                    p.getItemsCount()));
            }

            txtProvidersTable.setText(sb.toString());
            populateProviderDropdowns();
            setStatus("Loaded " + cachedProviders.size() + " food provider(s).", false);
        } catch (SQLException e) {
            handleSqlError("loading providers", e);
        }
    }

    private void populateProviderDropdowns() {
        if (cachedProviders == null) return;
        chProvSelect.removeAll();
        chAddProvider.removeAll();
        chEditProvider.removeAll();
        chSearchProvider.removeAll();
        chSearchProvider.add("ALL Providers");

        for (Provider p : cachedProviders) {
            String item = String.format("#%d - %s", p.getProviderId(), p.getProviderName());
            chProvSelect.add(item);
            chAddProvider.add(item);
            chEditProvider.add(item);
            chSearchProvider.add(item);
        }
    }

    private void handleLoadProviderDetails() {
        if (cachedProviders == null || cachedProviders.isEmpty() || chProvSelect.getSelectedIndex() < 0) return;
        int idx = chProvSelect.getSelectedIndex();
        if (idx < cachedProviders.size()) {
            Provider p = cachedProviders.get(idx);
            txtProvName.setText(p.getProviderName());
            txtProvPhone.setText(p.getPhone() != null ? p.getPhone() : "");
            txtProvAddress.setText(p.getAddress() != null ? p.getAddress() : "");
        }
    }

    private void handleAddProvider() {
        String name  = txtProvName.getText().trim();
        String phone = txtProvPhone.getText().trim();
        String addr  = txtProvAddress.getText().trim();
        if (name.isEmpty()) { setStatus("Validation: Provider name cannot be blank.", true); return; }
        try {
            if (providerDAO.addProvider(name, phone, addr)) {
                setStatus("Added provider '" + name + "' successfully!", false);
                clearProviderForm();
                loadProviders();
                loadDashboard();
            }
        } catch (SQLException e) {
            handleSqlError("adding provider", e);
        }
    }

    private void handleUpdateProvider() {
        if (cachedProviders == null || cachedProviders.isEmpty() || chProvSelect.getSelectedIndex() < 0) {
            setStatus("Select a provider to update.", true); return;
        }
        Provider p   = cachedProviders.get(chProvSelect.getSelectedIndex());
        String name  = txtProvName.getText().trim();
        String phone = txtProvPhone.getText().trim();
        String addr  = txtProvAddress.getText().trim();
        if (name.isEmpty()) { setStatus("Validation: Provider name cannot be empty.", true); return; }
        try {
            if (providerDAO.updateProvider(p.getProviderId(), name, phone, addr)) {
                setStatus("Updated provider #" + p.getProviderId() + " successfully!", false);
                loadProviders();
                loadDashboard();
            }
        } catch (SQLException e) {
            handleSqlError("updating provider", e);
        }
    }

    private void handleDeleteProvider() {
        if (cachedProviders == null || cachedProviders.isEmpty() || chProvSelect.getSelectedIndex() < 0) {
            setStatus("Select a provider to delete.", true); return;
        }
        Provider p = cachedProviders.get(chProvSelect.getSelectedIndex());
        try {
            if (providerDAO.deleteProvider(p.getProviderId())) {
                setStatus("Deleted provider #" + p.getProviderId() + " ('" + p.getProviderName() + "').", false);
                clearProviderForm();
                loadProviders();
                loadDashboard();
            }
        } catch (SQLException e) {
            handleSqlError("deleting provider", e);
        }
    }

    private void clearProviderForm() {
        txtProvName.setText("");
        txtProvPhone.setText("");
        txtProvAddress.setText("");
    }

    // -------------------------------------------------------------------------
    // 7. Collections
    // -------------------------------------------------------------------------
    private void updateAvailableDropdowns() {
        chCollFoodItem.removeAll();
        chWasteFoodItem.removeAll();

        if (cachedAvailableFood == null || cachedAvailableFood.isEmpty()) {
            chCollFoodItem.add("-- No Available Food --");
            chWasteFoodItem.add("-- No Available Food --");
            lblCollAvailableInfo.setText("(0 available)");
            lblWasteAvailableInfo.setText("(0 available)");
            return;
        }

        for (FoodItemModel f : cachedAvailableFood) {
            String item = String.format("#%d - %s (%.1f %s avail)",
                f.getFoodId(), f.getFoodName(), f.getQuantity(), f.getUnit());
            chCollFoodItem.add(item);
            chWasteFoodItem.add(item);
        }
        updateCollQuantityHint();
        updateWasteQuantityHint();
    }

    private void updateCollQuantityHint() {
        if (cachedAvailableFood != null && !cachedAvailableFood.isEmpty()
                && chCollFoodItem.getSelectedIndex() < cachedAvailableFood.size()) {
            FoodItemModel f = cachedAvailableFood.get(chCollFoodItem.getSelectedIndex());
            lblCollAvailableInfo.setText(String.format("(Max: %.1f %s)", f.getQuantity(), f.getUnit()));
            txtCollQuantity.setText(String.valueOf(f.getQuantity()));
        }
    }

    private void handleRecordCollection() {
        if (cachedAvailableFood == null || cachedAvailableFood.isEmpty() || chCollFoodItem.getSelectedIndex() < 0) {
            setStatus("No available food item selected.", true); return;
        }
        FoodItemModel f = cachedAvailableFood.get(chCollFoodItem.getSelectedIndex());
        String collector = txtCollCollectorName.getText().trim();
        String qtyStr    = txtCollQuantity.getText().trim();

        if (collector.isEmpty()) { setStatus("Validation: Collector / NGO name cannot be blank.", true); return; }
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) { setStatus("Validation: Quantity collected must be > 0.", true); return; }
        } catch (NumberFormatException ex) {
            setStatus("Validation: Invalid quantity number.", true); return;
        }

        try {
            if (collectionDAO.recordCollection(f.getFoodId(), collector, qty)) {
                setStatus(String.format("Recorded: %.1f %s of '%s' collected by '%s'.",
                    qty, f.getUnit(), f.getFoodName(), collector), false);
                txtCollCollectorName.setText("");
                txtCollQuantity.setText("");
                loadCollections();
                loadAvailableFood();
                loadDashboard();
            }
        } catch (SQLException e) {
            handleSqlError("recording collection", e);
        }
    }

    private void loadCollections() {
        try {
            List<CollectionRecord> list = collectionDAO.getAllCollections();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-26s | %-28s | %-18s | %-18s\n",
                "ID", "Food Item", "Collector / Organization", "Qty Collected", "Collection Date"));
            sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");

            if (list.isEmpty()) {
                sb.append("  No collection records logged yet.\n");
            } else {
                for (CollectionRecord c : list) {
                    String qtyUnit = String.format("%.1f %s", c.getQuantityCollected(), c.getUnit());
                    sb.append(String.format("%-6d | %-26s | %-28s | %-18s | %-18s\n",
                        c.getCollectionId(), truncate(c.getFoodName(), 26),
                        truncate(c.getCollectorName(), 28),
                        qtyUnit, c.getCollectionDate()));
                }
            }

            txtCollTable.setText(sb.toString());
            setStatus("Loaded " + list.size() + " collection record(s).", false);
        } catch (SQLException e) {
            handleSqlError("loading collections", e);
        }
    }

    // -------------------------------------------------------------------------
    // 8. Waste Records
    // -------------------------------------------------------------------------
    private void updateWasteQuantityHint() {
        if (cachedAvailableFood != null && !cachedAvailableFood.isEmpty()
                && chWasteFoodItem.getSelectedIndex() < cachedAvailableFood.size()) {
            FoodItemModel f = cachedAvailableFood.get(chWasteFoodItem.getSelectedIndex());
            lblWasteAvailableInfo.setText(String.format("(In stock: %.1f %s)", f.getQuantity(), f.getUnit()));
            txtWasteQuantity.setText(String.valueOf(f.getQuantity()));
        }
    }

    private void handleRecordWaste() {
        if (cachedAvailableFood == null || cachedAvailableFood.isEmpty() || chWasteFoodItem.getSelectedIndex() < 0) {
            setStatus("No food item selected to log as waste.", true); return;
        }
        FoodItemModel f = cachedAvailableFood.get(chWasteFoodItem.getSelectedIndex());
        String qtyStr      = txtWasteQuantity.getText().trim();
        String reasonSel   = chWasteReason.getSelectedItem();
        String custom      = txtWasteCustomReason.getText().trim();
        String finalReason = custom.isEmpty() ? reasonSel : (reasonSel + " - " + custom);

        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) { setStatus("Validation: Quantity wasted must be > 0.", true); return; }
        } catch (NumberFormatException ex) {
            setStatus("Validation: Invalid quantity number.", true); return;
        }

        try {
            if (wasteDAO.recordWaste(f.getFoodId(), qty, finalReason)) {
                setStatus(String.format("Logged: %.1f %s of '%s' wasted (%s).",
                    qty, f.getUnit(), f.getFoodName(), finalReason), false);
                txtWasteQuantity.setText("");
                txtWasteCustomReason.setText("");
                loadWasteRecords();
                loadAvailableFood();
                loadDashboard();
            }
        } catch (SQLException e) {
            handleSqlError("recording waste", e);
        }
    }

    private void loadWasteRecords() {
        try {
            List<WasteRecord> list = wasteDAO.getAllWasteRecords();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s | %-26s | %-18s | %-32s | %-18s\n",
                "ID", "Food Item", "Qty Wasted", "Reason for Spoilage / Waste", "Date Logged"));
            sb.append("---------------------------------------------------------------------------------------------------------------------------------\n");

            if (list.isEmpty()) {
                sb.append("  No waste records found in audit log.\n");
            } else {
                for (WasteRecord w : list) {
                    String qtyUnit = String.format("%.1f %s", w.getQuantityWasted(), w.getUnit());
                    sb.append(String.format("%-6d | %-26s | %-18s | %-32s | %-18s\n",
                        w.getWasteId(), truncate(w.getFoodName(), 26),
                        qtyUnit,
                        truncate(w.getReason(), 32), w.getWasteDate()));
                }
            }

            txtWasteTable.setText(sb.toString());
            setStatus("Loaded " + list.size() + " waste record(s).", false);
        } catch (SQLException e) {
            handleSqlError("loading waste records", e);
        }
    }

    // -------------------------------------------------------------------------
    // 9. DB Settings
    // -------------------------------------------------------------------------
    private void handleSaveAndTestDb() {
        String host = txtDbHost.getText().trim();
        String port = txtDbPort.getText().trim();
        String name = txtDbName.getText().trim();
        String user = txtDbUser.getText().trim();
        String pass = txtDbPass.getText();

        AppletDB.setCredentials(host, port, name, user, pass);
        lblDbUrlDisplay.setText(AppletDB.getJdbcUrl());

        try {
            java.sql.Connection conn = AppletDB.getConnection();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                setStatus("Connection TEST SUCCESSFUL! Connected to " + name + " @ " + host, false);
                if (lblDbDot != null) {
                    lblDbDot.setText("  Connected  ");
                    lblDbDot.setBackground(CLR_GREEN);
                }
                refreshAllData();
            }
        } catch (SQLException e) {
            setStatus("Connection FAILED: " + e.getMessage(), true);
            if (lblDbDot != null) {
                lblDbDot.setText("  Offline  ");
                lblDbDot.setBackground(CLR_DANGER);
            }
        }
    }

    // =========================================================================
    // UTILITY HELPERS
    // =========================================================================

    private void handleSqlError(String operation, Exception e) {
        String msg = "[DB Error in " + operation + "] " + e.getMessage();
        System.err.println(msg);
        setStatus(msg, true);
    }

    private int getSelectedProviderId(Choice choice) {
        if (choice == null || choice.getSelectedIndex() < 0) return -1;
        String text = choice.getSelectedItem();
        if (text != null && text.startsWith("#")) {
            try {
                int end = text.indexOf(" ");
                if (end > 1) return Integer.parseInt(text.substring(1, end));
            } catch (Exception ignored) {}
        }
        return -1;
    }

    private void selectProviderChoiceById(Choice choice, int providerId) {
        if (choice == null) return;
        String prefix = "#" + providerId + " ";
        for (int i = 0; i < choice.getItemCount(); i++) {
            if (choice.getItem(i).startsWith(prefix)) { choice.select(i); return; }
        }
    }

    private void selectChoiceByPrefix(Choice choice, String value) {
        if (choice == null || value == null) return;
        for (int i = 0; i < choice.getItemCount(); i++) {
            if (choice.getItem(i).equalsIgnoreCase(value.trim())) { choice.select(i); return; }
        }
    }

    private static String todayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private static String tomorrowDateString() {
        long tomorrow = System.currentTimeMillis() + (24L * 60 * 60 * 1000);
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(tomorrow));
    }

    private static boolean isValidDate(String d) {
        if (d == null || d.length() != 10) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(d);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return (s.length() <= maxLen) ? s : s.substring(0, maxLen - 2) + "..";
    }

    /**
     * Determines whether a YYYY-MM-DD date string represents a date strictly earlier than today (CURRENT_DATE).
     */
    private static boolean isPastExpiry(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date expiry = sdf.parse(expiryDate.trim());
            Date today = sdf.parse(sdf.format(new Date()));
            return expiry.before(today);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dynamically displays EXPIRED whenever the expiry date is strictly earlier than CURRENT_DATE,
     * without modifying the underlying database record.
     */
    private static String getDisplayStatus(FoodItemModel f) {
        if (f == null) return "";
        String st = f.getStatus();
        if (isPastExpiry(f.getExpiryDate())) {
            if ("AVAILABLE".equalsIgnoreCase(st) || "PARTIALLY_COLLECTED".equalsIgnoreCase(st)) {
                return "EXPIRED";
            }
        }
        return st != null ? st : "";
    }
}
