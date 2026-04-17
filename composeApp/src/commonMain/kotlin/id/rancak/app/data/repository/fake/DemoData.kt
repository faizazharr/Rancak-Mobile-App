package id.rancak.app.data.repository.fake

import id.rancak.app.domain.model.*

// ─────────────────────────────────────────────────────────────────────────────
// Demo credentials
//   Email   : demo@rancak.id  (atau email apapun — semua diterima)
//   Password: demo123
// ─────────────────────────────────────────────────────────────────────────────
const val DEMO_PASSWORD = "demo123"

val demoUser = User(
    uuid = "user-demo-001",
    name = "Admin Demo",
    email = "demo@rancak.id",
    tenants = listOf(
        Tenant(uuid = "tenant-001", name = "Warung Rancak"),
        Tenant(uuid = "tenant-002", name = "Cafe Sederhana")
    )
)

val demoLoginResult = LoginResult(
    tokens = AuthTokens(
        accessToken  = "demo-access-token",
        refreshToken = "demo-refresh-token",
        expiresIn    = 86400L
    ),
    user = demoUser
)

// ── Categories ────────────────────────────────────────────────────────────────

val catMakanan = Category(uuid = "cat-1", name = "Makanan Berat", description = null)
val catMinuman = Category(uuid = "cat-2", name = "Minuman",       description = null)
val catSnack   = Category(uuid = "cat-3", name = "Snack",         description = null)
val catDessert = Category(uuid = "cat-4", name = "Dessert",       description = null)

val demoCategories: List<Category> = listOf(catMakanan, catMinuman, catSnack, catDessert)

// ── Products (20 item) ────────────────────────────────────────────────────────

val demoProducts: List<Product> = listOf(
    // Makanan Berat
    Product("prd-01", "SKU-001", "8001001", "Nasi Goreng Spesial",    "Nasi goreng telur, ayam, sayuran pilihan", catMakanan, 25_000L, 100.0, "porsi", null, true,  false, "2025-01-01"),
    Product("prd-02", "SKU-002", "8001002", "Nasi Ayam Bakar",        "Nasi dengan ayam bakar bumbu kecap",       catMakanan, 32_000L, 100.0, "porsi", null, true,  false, "2025-01-01"),
    Product("prd-03", "SKU-003", "8001003", "Mie Goreng Seafood",     "Mie goreng udang, cumi, sayuran",          catMakanan, 28_000L, 100.0, "porsi", null, true,  false, "2025-01-01"),
    Product("prd-04", "SKU-004", "8001004", "Ayam Penyet Sambal Ijo", "Ayam goreng geprek sambal hijau",          catMakanan, 35_000L, 100.0, "porsi", null, true,  false, "2025-01-01"),
    Product("prd-05", "SKU-005", "8001005", "Bebek Goreng Crispy",    "Bebek goreng dengan sambal korek",         catMakanan, 45_000L, 100.0, "porsi", null, true,  false, "2025-01-01"),
    Product("prd-06", "SKU-006", "8001006", "Bakso Kuah Spesial",     "Bakso sapi mie tahu kuah kaldu",           catMakanan, 22_000L, 100.0, "porsi", null, true,  false, "2025-01-01"),
    // Minuman
    Product("prd-07", "SKU-101", "8002001", "Es Teh Manis",           "Teh manis segar es batu",                  catMinuman,  5_000L, 200.0, "gelas", null, true,  false, "2025-01-01"),
    Product("prd-08", "SKU-102", "8002002", "Es Jeruk Segar",         "Jeruk peras segar dengan es",              catMinuman,  8_000L, 200.0, "gelas", null, true,  false, "2025-01-01"),
    Product("prd-09", "SKU-103", "8002003", "Kopi Hitam",             "Kopi robusta tubruk lokal",                catMinuman,  8_000L, 200.0, "gelas", null, true,  false, "2025-01-01"),
    Product("prd-10", "SKU-104", "8002004", "Kopi Susu Gula Aren",    "Kopi susu dengan gula aren asli",          catMinuman, 18_000L, 200.0, "gelas", null, true,  false, "2025-01-01"),
    Product("prd-11", "SKU-105", "8002005", "Jus Alpukat",            "Jus alpukat segar tanpa susu kental",      catMinuman, 15_000L, 200.0, "gelas", null, true,  false, "2025-01-01"),
    Product("prd-12", "SKU-106", "8002006", "Air Mineral",            "Air mineral 600ml",                        catMinuman,  4_000L, 200.0, "botol", null, true,  false, "2025-01-01"),
    Product("prd-13", "SKU-107", "8002007", "Es Lemon Tea",           "Teh lemon segar es batu",                  catMinuman, 10_000L, 0.0,   "gelas", null, false, false, "2025-01-01"),
    // Snack
    Product("prd-14", "SKU-201", "8003001", "Keripik Singkong",       "Keripik singkong crispy original",         catSnack,   12_000L, 50.0, "bungkus", null, true, false, "2025-01-01"),
    Product("prd-15", "SKU-202", "8003002", "Kentang Goreng",         "Kentang goreng crispy dengan saus",        catSnack,   15_000L, 50.0, "porsi",   null, true, false, "2025-01-01"),
    Product("prd-16", "SKU-203", "8003003", "Bakwan Goreng (3pcs)",   "Bakwan sayuran goreng garing",             catSnack,   10_000L, 50.0, "porsi",   null, true, false, "2025-01-01"),
    Product("prd-17", "SKU-204", "8003004", "Lumpia Goreng (3pcs)",   "Lumpia isi ayam dan sayuran",              catSnack,   12_000L, 50.0, "porsi",   null, true, false, "2025-01-01"),
    // Dessert
    Product("prd-18", "SKU-301", "8004001", "Es Krim Vanilla",        "Satu scoop es krim vanilla premium",       catDessert, 18_000L, 50.0, "scoop", null, true, false, "2025-01-01"),
    Product("prd-19", "SKU-302", "8004002", "Pudding Cokelat",        "Pudding lembut rasa cokelat",              catDessert, 12_000L, 50.0, "cup",   null, true, false, "2025-01-01"),
    Product("prd-20", "SKU-303", "8004003", "Pisang Goreng Crispy",   "Pisang goreng tepung renyah keju",         catDessert, 15_000L, 50.0, "porsi", null, true, false, "2025-01-01"),
)

// ── Sales History (12 transaksi) ──────────────────────────────────────────────

val demoSales: MutableList<Sale> = mutableListOf(
    Sale("sale-001", "INV-2025-0001", OrderType.DINE_IN, 1, SaleStatus.PAID,
        null, 57_000L, 0L, 0L, tax = 5_700L, total = 62_700L,
        paymentMethod = PaymentMethod.CASH, paidAmount = 70_000L, changeAmount = 7_300L,
        items = listOf(
            SaleItem("si-001a", null, "Nasi Goreng Spesial", "2", 25_000L, 0L, 50_000L, null, null),
            SaleItem("si-001b", null, "Es Teh Manis",        "2",  5_000L, 0L, 10_000L, null, null),
        ), createdAt = "2025-06-14 12:30:00"),

    Sale("sale-002", "INV-2025-0002", OrderType.TAKEAWAY, 2, SaleStatus.PAID,
        null, 45_000L, 0L, 0L, tax = 4_500L, total = 49_500L,
        paymentMethod = PaymentMethod.QRIS, paidAmount = 49_500L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-002a", null, "Ayam Penyet Sambal Ijo", "1", 35_000L, 0L, 35_000L, null, null),
            SaleItem("si-002b", null, "Es Jeruk Segar",         "1",  8_000L, 0L,  8_000L, null, null),
            SaleItem("si-002c", null, "Air Mineral",            "1",  4_000L, 0L,  4_000L, null, null),
        ), createdAt = "2025-06-14 12:45:00"),

    Sale("sale-003", "INV-2025-0003", OrderType.DINE_IN, 3, SaleStatus.PAID,
        null, 103_000L, 10_000L, 0L, tax = 9_300L, total = 102_300L,
        paymentMethod = PaymentMethod.CARD, paidAmount = 102_300L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-003a", null, "Bebek Goreng Crispy",  "2", 45_000L, 0L,  90_000L, null, null),
            SaleItem("si-003b", null, "Jus Alpukat",          "1", 15_000L, 0L,  15_000L, null, null),
            SaleItem("si-003c", null, "Pudding Cokelat",      "2", 12_000L, 0L,  24_000L, null, null),
        ), createdAt = "2025-06-14 13:00:00"),

    Sale("sale-004", "INV-2025-0004", OrderType.TAKEAWAY, 4, SaleStatus.PAID,
        null, 55_000L, 0L, 0L, tax = 5_500L, total = 60_500L,
        paymentMethod = PaymentMethod.CASH, paidAmount = 70_000L, changeAmount = 9_500L,
        items = listOf(
            SaleItem("si-004a", null, "Mie Goreng Seafood",   "1", 28_000L, 0L, 28_000L, null, null),
            SaleItem("si-004b", null, "Es Teh Manis",         "1",  5_000L, 0L,  5_000L, null, null),
            SaleItem("si-004c", null, "Keripik Singkong",     "2", 12_000L, 0L, 24_000L, null, null),
        ), createdAt = "2025-06-14 13:30:00"),

    Sale("sale-005", "INV-2025-0005", OrderType.DINE_IN, 5, SaleStatus.PAID,
        null, 115_000L, 5_000L, 0L, tax = 11_000L, total = 121_000L,
        paymentMethod = PaymentMethod.QRIS, paidAmount = 121_000L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-005a", null, "Nasi Ayam Bakar",      "2", 32_000L, 0L,  64_000L, null, null),
            SaleItem("si-005b", null, "Kopi Susu Gula Aren",  "2", 18_000L, 0L,  36_000L, null, null),
            SaleItem("si-005c", null, "Pisang Goreng Crispy", "1", 15_000L, 0L,  15_000L, null, null),
        ), createdAt = "2025-06-14 14:00:00"),

    Sale("sale-006", "INV-2025-0006", OrderType.DELIVERY, null, SaleStatus.PAID,
        null, 55_000L, 0L, 5_000L, tax = 6_000L, total = 66_000L,
        paymentMethod = PaymentMethod.TRANSFER, paidAmount = 66_000L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-006a", null, "Bakso Kuah Spesial", "2", 22_000L, 0L, 44_000L, null, null),
            SaleItem("si-006b", null, "Kentang Goreng",     "1", 15_000L, 0L, 15_000L, null, null),
        ), createdAt = "2025-06-14 14:30:00"),

    Sale("sale-007", "INV-2025-0007", OrderType.DINE_IN, 7, SaleStatus.PAID,
        null, 44_000L, 0L, 0L, tax = 4_400L, total = 48_400L,
        paymentMethod = PaymentMethod.CASH, paidAmount = 50_000L, changeAmount = 1_600L,
        items = listOf(
            SaleItem("si-007a", null, "Mie Goreng Seafood", "1", 28_000L, 0L, 28_000L, null, null),
            SaleItem("si-007b", null, "Kopi Hitam",         "2",  8_000L, 0L, 16_000L, null, null),
        ), createdAt = "2025-06-13 11:00:00"),

    Sale("sale-008", "INV-2025-0008", OrderType.DINE_IN, 8, SaleStatus.PAID,
        null, 160_000L, 20_000L, 0L, tax = 14_000L, total = 154_000L,
        paymentMethod = PaymentMethod.CARD, paidAmount = 154_000L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-008a", null, "Bebek Goreng Crispy",    "2", 45_000L, 0L,  90_000L, null, null),
            SaleItem("si-008b", null, "Ayam Penyet Sambal Ijo", "1", 35_000L, 0L,  35_000L, null, null),
            SaleItem("si-008c", null, "Jus Alpukat",            "2", 15_000L, 0L,  30_000L, null, null),
        ), createdAt = "2025-06-13 12:00:00"),

    Sale("sale-009", "INV-2025-0009", OrderType.TAKEAWAY, 9, SaleStatus.PAID,
        null, 27_000L, 0L, 0L, tax = 2_700L, total = 29_700L,
        paymentMethod = PaymentMethod.CASH, paidAmount = 30_000L, changeAmount = 300L,
        items = listOf(
            SaleItem("si-009a", null, "Bakso Kuah Spesial", "1", 22_000L, 0L, 22_000L, null, null),
            SaleItem("si-009b", null, "Es Teh Manis",       "1",  5_000L, 0L,  5_000L, null, null),
        ), createdAt = "2025-06-13 13:00:00"),

    Sale("sale-010", "INV-2025-0010", OrderType.DINE_IN, 10, SaleStatus.VOID,
        null, 50_000L, 0L, 0L, tax = 5_000L, total = 55_000L,
        paymentMethod = null, paidAmount = 0L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-010a", null, "Nasi Goreng Spesial", "2", 25_000L, 0L, 50_000L, null, null),
        ), createdAt = "2025-06-13 14:00:00"),

    Sale("sale-011", "INV-2025-0011", OrderType.DELIVERY, null, SaleStatus.PAID,
        null, 84_000L, 0L, 8_000L, tax = 9_200L, total = 101_200L,
        paymentMethod = PaymentMethod.QRIS, paidAmount = 101_200L, changeAmount = 0L,
        items = listOf(
            SaleItem("si-011a", null, "Nasi Ayam Bakar",      "2", 32_000L, 0L, 64_000L, null, null),
            SaleItem("si-011b", null, "Lumpia Goreng (3pcs)", "1", 12_000L, 0L, 12_000L, null, null),
            SaleItem("si-011c", null, "Air Mineral",          "2",  4_000L, 0L,  8_000L, null, null),
        ), createdAt = "2025-06-13 17:00:00"),

    Sale("sale-012", "INV-2025-0012", OrderType.DINE_IN, 12, SaleStatus.PAID,
        null, 71_000L, 0L, 0L, tax = 7_100L, total = 78_100L,
        paymentMethod = PaymentMethod.CASH, paidAmount = 100_000L, changeAmount = 21_900L,
        items = listOf(
            SaleItem("si-012a", null, "Ayam Penyet Sambal Ijo", "1", 35_000L, 0L, 35_000L, null, null),
            SaleItem("si-012b", null, "Kopi Susu Gula Aren",   "2", 18_000L, 0L, 36_000L, null, null),
            SaleItem("si-012c", null, "Es Krim Vanilla",       "1", 18_000L, 0L, 18_000L, null, null),
        ), createdAt = "2025-06-13 19:00:00"),
)

// ── Tables (8 meja) ───────────────────────────────────────────────────────────

val demoTables: MutableList<Table> = mutableListOf(
    Table("tbl-01", "M1", "Dalam", 4, TableStatus.AVAILABLE,   true, 1, null),
    Table("tbl-02", "M2", "Dalam", 4, TableStatus.OCCUPIED,    true, 2, "sale-003"),
    Table("tbl-03", "M3", "Dalam", 6, TableStatus.AVAILABLE,   true, 3, null),
    Table("tbl-04", "M4", "Dalam", 2, TableStatus.INACTIVE,   true, 4, null),
    Table("tbl-05", "L1", "Luar",  4, TableStatus.AVAILABLE,   true, 5, null),
    Table("tbl-06", "L2", "Luar",  4, TableStatus.AVAILABLE,   true, 6, null),
    Table("tbl-07", "L3", "Luar",  6, TableStatus.OCCUPIED,    true, 7, "sale-005"),
    Table("tbl-08", "L4", "Luar",  8, TableStatus.INACTIVE,  true, 8, null),
)

// ── Shift ─────────────────────────────────────────────────────────────────────

var demoShift: Shift? = Shift(
    uuid              = "shift-001",
    openedAt          = "2025-06-14 08:00:00",
    closedAt          = null,
    status            = ShiftStatus.OPEN,
    openingCash       = "500000",
    closingCash       = null,
    expectedCash      = null,
    cashDifference    = null,
    cashierName       = "Admin Demo",
    totalSales        = 571_600L,
    totalTransactions = 11,
    totalExpenses     = 85_000L,
    totalCashIn       = 750_000L
)

// ── KDS Orders (4 order aktif) ────────────────────────────────────────────────

val demoKdsOrders: MutableList<KdsOrder> = mutableListOf(
    KdsOrder("kds-001", "INV-2025-0013", OrderType.DINE_IN,   "M2",  13, null, null, KdsStatus.NEW,
        listOf(
            KdsItem("ki-001a", "Nasi Goreng Spesial", "2", null, null,         KdsItemStatus.PENDING),
            KdsItem("ki-001b", "Es Teh Manis",        "2", null, null,         KdsItemStatus.PENDING),
        ), "2025-06-14 15:00:00"),
    KdsOrder("kds-002", "INV-2025-0014", OrderType.TAKEAWAY,  null,  14, null, null, KdsStatus.COOKING,
        listOf(
            KdsItem("ki-002a", "Ayam Penyet Sambal Ijo", "1", null, "Tidak pedas", KdsItemStatus.COOKING),
            KdsItem("ki-002b", "Bebek Goreng Crispy",    "1", null, null,          KdsItemStatus.PENDING),
        ), "2025-06-14 15:05:00"),
    KdsOrder("kds-003", "INV-2025-0015", OrderType.DINE_IN,   "L3",  15, null, null, KdsStatus.COOKING,
        listOf(
            KdsItem("ki-003a", "Mie Goreng Seafood",   "2", null, null,           KdsItemStatus.COOKING),
            KdsItem("ki-003b", "Kopi Susu Gula Aren",  "2", null, "Gula sedikit", KdsItemStatus.READY),
        ), "2025-06-14 15:10:00"),
    KdsOrder("kds-004", "INV-2025-0016", OrderType.TAKEAWAY,  null,  16, null, null, KdsStatus.READY,
        listOf(
            KdsItem("ki-004a", "Bakso Kuah Spesial", "1", null, null, KdsItemStatus.READY),
            KdsItem("ki-004b", "Kentang Goreng",     "1", null, null, KdsItemStatus.READY),
            KdsItem("ki-004c", "Es Jeruk Segar",     "2", null, null, KdsItemStatus.READY),
        ), "2025-06-14 14:50:00"),
)

// ── Cash In ───────────────────────────────────────────────────────────────────

val demoCashIns: MutableList<CashIn> = mutableListOf(
    CashIn("ci-001", 500_000L, "Modal",   "Kas pembukaan shift",              null, null, null, null, null, "2025-06-14 08:00:00"),
    CashIn("ci-002", 200_000L, "Setoran", "Tambahan kas dari owner",          null, null, null, null, null, "2025-06-14 10:00:00"),
    CashIn("ci-003",  50_000L, "Lainnya", "Pengembalian uang makan karyawan", null, null, null, null, null, "2025-06-13 09:00:00"),
)

// ── Expenses ──────────────────────────────────────────────────────────────────

val demoExpenses: MutableList<Expense> = mutableListOf(
    Expense("exp-001", 45_000L, "Bahan makanan",   "Beli ayam dan sayuran",       null, null, null, null, "2025-06-14", "2025-06-14 07:00:00", null),
    Expense("exp-002", 20_000L, "Gas memasak",     "Isi ulang gas LPG 3kg",       null, null, null, null, "2025-06-14", "2025-06-14 07:30:00", null),
    Expense("exp-003", 15_000L, "Bahan minuman",   "Beli jeruk dan alpukat",      null, null, null, null, "2025-06-14", "2025-06-14 08:00:00", null),
    Expense("exp-004", 30_000L, "Kebersihan",      "Sabun, lap, kantong plastik", null, null, null, null, "2025-06-13", "2025-06-13 08:00:00", null),
    Expense("exp-005", 25_000L, "Parkir & angkut", "Ongkos angkut belanjaan",     null, null, null, null, "2025-06-13", "2025-06-13 09:00:00", null),
)

// ── Report Summary ────────────────────────────────────────────────────────────

val demoShiftSummary = ShiftSummary(
    uuid              = "shift-001",
    openedAt          = "2025-06-14 08:00:00",
    closedAt          = null,
    status            = "open",
    openingCash       = "500000",
    closingCash       = null,
    expectedCash      = null,
    cashDifference    = null,
    cashierName       = "Admin Demo",
    totalSales        = 811_200L,
    totalTransactions = 11,
    totalExpenses     = 85_000L,
    totalCashIn       = 750_000L,
    paymentSummary = listOf(
        PaymentMethodReport("cash",      238_700L, 4),
        PaymentMethodReport("qris",      221_700L, 3),
        PaymentMethodReport("card",      256_300L, 2),
        PaymentMethodReport("transfer",   66_000L, 1),
    )
)
