package id.rancak.app

import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.domain.repository.OperationsRepository
import id.rancak.app.presentation.viewmodel.ShiftViewModel
import id.rancak.app.presentation.viewmodel.TenantPickerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [TenantPickerViewModel] and [ShiftViewModel].
 *
 * Fakes are defined inline to avoid production code coupling.
 * Uses a [StandardTestDispatcher] so coroutine execution is fully controlled.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fake repositories
    // ─────────────────────────────────────────────────────────────────────────

    private fun authRepo(
        tenants: Resource<List<Tenant>> = Resource.Success(emptyList())
    ) = object : AuthRepository {
        override suspend fun login(email: String, password: String) = Resource.Error("unused")
        override suspend fun loginWithGoogle(idToken: String) = Resource.Error("unused")
        override suspend fun refreshToken() = Resource.Error("unused")
        override suspend fun logout() = Resource.Success(Unit)
        override suspend fun getMe() = Resource.Error("unused")
        override suspend fun getMyTenants() = tenants
        override suspend fun getTenantSettings() = Resource.Error("unused")
        override suspend fun getReceiptSettings() = Resource.Error("unused")
        override suspend fun changePassword(currentPassword: String, newPassword: String) = Resource.Error("unused")
        override suspend fun getSessions() = Resource.Error("unused")
        override suspend fun revokeSession(sessionId: String) = Resource.Error("unused")
        override fun isLoggedIn() = true
        override fun getCurrentTenantUuid(): String? = null
        override fun getCurrentTenantName(): String? = null
        override fun setTenant(uuid: String, name: String) {}
        override fun setUserRole(role: String) {}
    }

    private fun operationsRepo(
        currentShift: Resource<Shift?> = Resource.Success(null),
        openShift: Resource<Shift> = Resource.Error("unused"),
        closeShift: Resource<Shift> = Resource.Error("unused")
    ) = object : OperationsRepository {
        override suspend fun getTables() = Resource.Error("unused")
        override suspend fun getCurrentShift() = currentShift
        override suspend fun openShift(openingCash: String) = openShift
        override suspend fun closeShift(closingCash: String, note: String?) = closeShift
        override suspend fun getKdsOrders() = Resource.Error("unused")
        override suspend fun updateKdsStatus(kdsUuid: String, status: KdsStatus) = Resource.Error("unused")
        override suspend fun getSurcharges() = Resource.Error("unused")
        override suspend fun getTaxConfigs() = Resource.Error("unused")
        override suspend fun getDiscountRules() = Resource.Error("unused")
        override suspend fun validateVoucher(code: String, subtotal: Long) = Resource.Error("unused")
        override suspend fun previewDiscount(total: Long) = Resource.Error("unused")
        override suspend fun syncCatalog(updatedAfter: String?) = Resource.Error("unused")
        override suspend fun syncStatus() = Resource.Error("unused")
        override suspend fun getShiftSummaryById(shiftUuid: String) = Resource.Error("unused")
    }

    private fun fakeShift(uuid: String = "shift-1") = Shift(
        uuid              = uuid,
        openedAt          = "2026-04-22T08:00:00Z",
        closedAt          = null,
        status            = ShiftStatus.OPEN,
        openingCash       = "100000",
        closingCash       = null,
        expectedCash      = null,
        cashDifference    = null,
        cashierName       = "Budi",
        totalSales        = null,
        totalTransactions = null,
        totalExpenses     = null,
        totalCashIn       = null
    )

    private fun fakeTenant(uuid: String = "t-1", name: String = "Kafe Rancak") =
        Tenant(uuid = uuid, name = name)

    // ─────────────────────────────────────────────────────────────────────────
    // TenantPickerViewModel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `loadTenants - success with multiple tenants`() = runTest {
        val tenants = listOf(fakeTenant("a"), fakeTenant("b"))
        val vm = TenantPickerViewModel(authRepo(tenants = Resource.Success(tenants)))

        vm.loadTenants()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.tenants.size)
        assertFalse(state.isConfirmed)
    }

    @Test
    fun `loadTenants - single tenant auto-confirms`() = runTest {
        val only = fakeTenant("only")
        val vm = TenantPickerViewModel(authRepo(tenants = Resource.Success(listOf(only))))

        vm.loadTenants()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isConfirmed)
        assertEquals(only, state.selectedTenant)
    }

    @Test
    fun `loadTenants - empty list does not auto-confirm`() = runTest {
        val vm = TenantPickerViewModel(authRepo(tenants = Resource.Success(emptyList())))

        vm.loadTenants()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isConfirmed)
        assertEquals(0, state.tenants.size)
    }

    @Test
    fun `loadTenants - error propagates to state`() = runTest {
        val vm = TenantPickerViewModel(authRepo(
            tenants = Resource.Error("Koneksi gagal")
        ))

        vm.loadTenants()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Koneksi gagal", state.error)
        assertEquals(0, state.tenants.size)
    }

    @Test
    fun `selectTenant - updates selectedTenant`() = runTest {
        val vm = TenantPickerViewModel(authRepo())
        val tenant = fakeTenant("sel")

        vm.selectTenant(tenant)

        assertEquals(tenant, vm.uiState.value.selectedTenant)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ShiftViewModel
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `loadCurrentShift - success with open shift`() = runTest {
        val shift = fakeShift()
        val vm = ShiftViewModel(operationsRepo(currentShift = Resource.Success(shift)))

        vm.loadCurrentShift()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.currentShift)
        assertEquals("shift-1", state.currentShift?.uuid)
    }

    @Test
    fun `loadCurrentShift - success with no open shift`() = runTest {
        val vm = ShiftViewModel(operationsRepo(currentShift = Resource.Success(null)))

        vm.loadCurrentShift()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currentShift)
        assertNull(state.error)
    }

    @Test
    fun `loadCurrentShift - error propagates`() = runTest {
        val vm = ShiftViewModel(operationsRepo(
            currentShift = Resource.Error("Gagal memuat shift")
        ))

        vm.loadCurrentShift()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Gagal memuat shift", state.error)
    }

    @Test
    fun `openShift - success sets shift and shiftJustOpened flag`() = runTest {
        val shift = fakeShift("new-shift")
        val vm = ShiftViewModel(operationsRepo(openShift = Resource.Success(shift)))

        vm.onOpeningCashChange("200000")
        vm.openShift()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("new-shift", state.currentShift?.uuid)
        assertTrue(state.shiftJustOpened)
        assertEquals("", state.openingCash)
    }

    @Test
    fun `openShift - error propagates`() = runTest {
        val vm = ShiftViewModel(operationsRepo(
            openShift = Resource.Error("Shift sudah terbuka")
        ))

        vm.onOpeningCashChange("100000")
        vm.openShift()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Shift sudah terbuka", state.error)
        assertNull(state.currentShift)
    }

    @Test
    fun `closeShift - success clears currentShift and sets shiftJustClosed`() = runTest {
        val closedShift = fakeShift("closed-shift").copy(
            status    = ShiftStatus.CLOSED,
            closedAt  = "2026-04-22T18:00:00Z"
        )
        val vm = ShiftViewModel(operationsRepo(
            currentShift = Resource.Success(fakeShift()),
            closeShift   = Resource.Success(closedShift)
        ))
        vm.loadCurrentShift()
        advanceUntilIdle()

        vm.onClosingCashChange("95000")
        vm.onClosingNoteChange("Alhamdulillah ramai")
        vm.closeShift()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.currentShift)
        assertTrue(state.shiftJustClosed)
        assertEquals("", state.closingCash)
        assertEquals("", state.closingNote)
    }

    @Test
    fun `onOpeningCashChange - strips non-digit characters`() = runTest {
        val vm = ShiftViewModel(operationsRepo())
        vm.onOpeningCashChange("Rp 50.000")
        assertEquals("50000", vm.uiState.value.openingCash)
    }

    @Test
    fun `onClosingCashChange - strips non-digit characters`() = runTest {
        val vm = ShiftViewModel(operationsRepo())
        vm.onClosingCashChange("1.000.000")
        assertEquals("1000000", vm.uiState.value.closingCash)
    }
}
