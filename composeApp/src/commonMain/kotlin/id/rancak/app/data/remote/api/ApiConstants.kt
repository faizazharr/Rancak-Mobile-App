package id.rancak.app.data.remote.api

object ApiConstants {
    const val BASE_URL = "https://be-rancak.up.railway.app"

    /**
     * App-level API key — platform-specific via expect/actual [rancakApiKey].
     * Android: injected from local.properties via BuildConfig.
     * iOS: read from Info.plist key "RancakApiKey".
     * Never hardcode this value here.
     */
    val API_KEY: String get() = id.rancak.app.data.remote.rancakApiKey

    // Auth
    const val LOGIN = "/auth/login"
    const val GOOGLE_LOGIN = "/auth/google"
    const val REFRESH = "/auth/refresh"
    const val LOGOUT = "/auth/logout"
    const val ME = "/auth/me"
    const val CHANGE_PASSWORD = "/auth/change-password"
    const val FORGOT_PASSWORD = "/auth/forgot-password"
    const val RESET_PASSWORD = "/auth/reset-password"
    const val SESSIONS = "/auth/sessions"

    // Tenant-scoped (append tenantPath(tenantUuid) before these)
    const val PRODUCTS = "/products"
    const val CATEGORIES = "/categories"
    const val SALES = "/sales"
    const val SHIFTS = "/shifts"
    const val TABLES = "/tables"
    const val KDS = "/kds"
    const val ORDER_BOARD = "/order-board"
    const val BUNDLES = "/bundles"
    const val MODIFIERS = "/modifiers"
    const val SURCHARGES = "/surcharges"
    const val TAX_CONFIGS = "/tax-configs"
    const val VOUCHERS = "/vouchers"
    const val DISCOUNT_RULES = "/discount-rules"
    const val CASH_INS = "/cash-ins"
    const val EXPENSES = "/expenses"
    const val REPORTS = "/reports"
    const val SYNC_CATALOG = "/sync/catalog"
    const val SYNC_STATUS = "/sync/status"
    const val DEVICE_CONFIG = "/device-config"

    // Receipt endpoints (return raw ESC/POS bytes)
    fun qrPayment(saleUuid: String)   = "$SALES/$saleUuid/qr-payment"

    fun receiptEscpos(saleUuid: String) = "$SALES/$saleUuid/receipt/escpos"
    fun receiptKitchen(saleUuid: String) = "$SALES/$saleUuid/receipt/kitchen"
    fun receiptCombined(saleUuid: String) = "$SALES/$saleUuid/receipt/combined"

    fun tenantPath(tenantUuid: String) = "/tenants/$tenantUuid"
}
