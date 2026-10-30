package com.example.data.repository

import android.content.Context
import com.example.data.local.OrderDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SalesForceRepository private constructor(context: Context) {

    private val db = OrderDatabase.getDatabase(context)
    private val productDao = db.productDao()
    private val commissionDao = db.commissionDao()
    private val staffDao = db.staffDao()
    private val customerOrderDao = db.customerOrderDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    // Products
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    fun getLowStockProducts(): Flow<List<Product>> = kotlinx.coroutines.flow.flow {
        emit(productDao.getLowStockProducts())
    }
    suspend fun updateStock(productId: String, newStock: Int) = productDao.updateStockQty(productId, newStock)
    suspend fun addProduct(product: Product) = productDao.insertProduct(product)

    // Commissions
    fun getAllCommissions(): Flow<List<CommissionRecord>> = commissionDao.getAllCommissions()
    fun getCommissionsForRep(salesRepId: String): Flow<List<CommissionRecord>> = commissionDao.getCommissionsByRep(salesRepId)

    suspend fun recordCommission(
        salesRepId: String,
        salesRepName: String,
        orderId: String,
        orderAmount: Double,
        type: CommissionType,
        rate: Double
    ) {
        val record = CommissionRecord.calculate(
            salesRepId = salesRepId,
            salesRepName = salesRepName,
            orderId = orderId,
            orderAmount = orderAmount,
            type = type,
            rate = rate
        )
        commissionDao.insertCommission(record)
    }

    // Staff & Passwords
    fun getAllStaff(): Flow<List<StaffMember>> = staffDao.getAllStaff()
    suspend fun getStaffByEmail(email: String) = staffDao.findStaffByEmailOrPhone(email)

    suspend fun generateTemporaryPassword(email: String): String? {
        val staff = staffDao.findStaffByEmailOrPhone(email) ?: return null
        val tempPass = "TEMP${(100000..999999).random()}"
        staffDao.issueTemporaryPassword(staff.id, tempPass, System.currentTimeMillis() + (24 * 60 * 60 * 1000L))
        return tempPass
    }

    suspend fun updatePassword(staffId: String, newPass: String) {
        staffDao.updatePasswordAndClearTemp(staffId, newPass)
    }

    suspend fun updateStaffBiometric(staffId: String, enabled: Boolean) {
        val staff = staffDao.getStaffById(staffId) ?: return
        staffDao.updateStaff(staff.copy(isBiometricEnabled = enabled))
    }

    // Customer Orders
    fun getAllOrders(): Flow<List<CustomerOrder>> = customerOrderDao.getAllOrders()
    fun getOrdersForCustomer(email: String): Flow<List<CustomerOrder>> = customerOrderDao.getOrdersForCustomer(email)

    suspend fun placeOrder(order: CustomerOrder) {
        customerOrderDao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderDeliveryStatus) {
        customerOrderDao.updateDeliveryStatus(orderId, status)
    }

    suspend fun requestBackorder(productName: String, customerName: String, customerEmail: String) {
        val seq = (10000..99999).random()
        val backorder = CustomerOrder(
            id = "ord_bo_${System.currentTimeMillis()}",
            serializedOrderNumber = CustomerOrder.generateSerializedOrderNumber(seq),
            customerName = customerName,
            customerEmail = customerEmail,
            customerSerializedId = "CUST-${String.format("%05d", seq)}",
            itemsSummary = "BACKORDER REQUEST: $productName",
            totalPricing = 0.0,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            deliveryStatus = OrderDeliveryStatus.BACKORDERED,
            assignedSalesRepId = "rep_101",
            assignedSalesRepSerializedId = "AYBZ-001-SR",
            assignedSalesRepName = "Ahmad Al-Mansoor",
            supplierSerializedId = "SUPP-00101",
            isBackorderRequest = true
        )
        customerOrderDao.insertOrder(backorder)
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existingProducts = productDao.getAllProducts().first()
        if (existingProducts.isEmpty()) {
            productDao.insertAll(
                listOf(
                    Product("p1", "Solar Panel 400W", "Energy", 299.99, 4, lowStockThreshold = 10, criticalStockThreshold = 5),
                    Product("p2", "Lithium Battery 5kWh", "Energy", 1499.00, 0, lowStockThreshold = 5, criticalStockThreshold = 2),
                    Product("p3", "Smart Inverter 3KW", "Electronics", 450.00, 12, lowStockThreshold = 15, criticalStockThreshold = 5),
                    Product("p4", "Industrial Water Pump", "Hardware", 850.00, 25, lowStockThreshold = 10, criticalStockThreshold = 3),
                    Product("p5", "Fiber Optic Cable 100m", "Networking", 85.00, 3, lowStockThreshold = 10, criticalStockThreshold = 5)
                )
            )

            staffDao.insertAll(
                listOf(
                    StaffMember("admin_1", StaffMember.generateSerializedId(UserRole.ADMIN, 1), "Zayed Director", "admin@zayed.com", UserRole.ADMIN, secondaryPin = "1234"),
                    StaffMember("rep_101", StaffMember.generateSerializedId(UserRole.SALES_REP, 1), "Ahmad Al-Mansoor", "ahmad@zayed.com", UserRole.SALES_REP, secondaryPin = "5678"),
                    StaffMember("cust_1", StaffMember.generateSerializedId(UserRole.CUSTOMER, 1), "Fatima Hassan", "fatima@client.com", UserRole.CUSTOMER),
                    StaffMember("supp_1", StaffMember.generateSerializedId(UserRole.SUPPLIER, 1), "Global Supplier Ltd", "supplier@zayed.com", UserRole.SUPPLIER)
                )
            )

            commissionDao.insertAll(
                listOf(
                    CommissionRecord("c1", "rep_101", "Ahmad Al-Mansoor", "ord_901", 1200.0, CommissionType.PERCENTAGE_BASED, 5.0, 60.0, isPaidOut = true),
                    CommissionRecord("c2", "rep_101", "Ahmad Al-Mansoor", "ord_902", 450.0, CommissionType.FLAT_FIXED, 35.0, 35.0, isPaidOut = true),
                    CommissionRecord("c3", "rep_101", "Ahmad Al-Mansoor", "ord_903", 2200.0, CommissionType.TIERED_PERFORMANCE, 5.0, 154.0, isPaidOut = false)
                )
            )

            customerOrderDao.insertAll(
                listOf(
                    CustomerOrder(
                        id = "ord_901",
                        serializedOrderNumber = CustomerOrder.generateSerializedOrderNumber(901),
                        customerName = "Fatima Hassan",
                        customerEmail = "fatima@client.com",
                        customerSerializedId = "CUST-00901",
                        itemsSummary = "2x Solar Panel 400W",
                        totalPricing = 599.98,
                        paymentMethod = PaymentMethod.BANK_TRANSFER,
                        deliveryStatus = OrderDeliveryStatus.DISPATCHED_IN_TRANSIT,
                        assignedSalesRepId = "rep_101",
                        assignedSalesRepSerializedId = "AYBZ-001-SR",
                        assignedSalesRepName = "Ahmad Al-Mansoor",
                        supplierSerializedId = "SUPP-001",
                        hasCommissionApproved = true,
                        isCommissionPaidOut = true
                    ),
                    CustomerOrder(
                        id = "ord_902",
                        serializedOrderNumber = CustomerOrder.generateSerializedOrderNumber(902),
                        customerName = "Fatima Hassan",
                        customerEmail = "fatima@client.com",
                        customerSerializedId = "CUST-00901",
                        itemsSummary = "1x Smart Inverter 3KW",
                        totalPricing = 450.00,
                        paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
                        deliveryStatus = OrderDeliveryStatus.PROCESSED,
                        assignedSalesRepId = "rep_101",
                        assignedSalesRepSerializedId = "AYBZ-001-SR",
                        assignedSalesRepName = "Ahmad Al-Mansoor",
                        supplierSerializedId = "SUPP-001",
                        hasCommissionApproved = true,
                        isCommissionPaidOut = true
                    ),
                    CustomerOrder(
                        id = "ord_903",
                        serializedOrderNumber = CustomerOrder.generateSerializedOrderNumber(903),
                        customerName = "Fatima Hassan",
                        customerEmail = "fatima@client.com",
                        customerSerializedId = "CUST-00901",
                        itemsSummary = "1x Industrial Water Pump",
                        totalPricing = 850.00,
                        paymentMethod = PaymentMethod.CREDIT_CARD,
                        deliveryStatus = OrderDeliveryStatus.UNPROCESSED,
                        assignedSalesRepId = "rep_101",
                        assignedSalesRepSerializedId = "AYBZ-001-SR",
                        assignedSalesRepName = "Ahmad Al-Mansoor",
                        supplierSerializedId = "SUPP-001",
                        hasCommissionApproved = false,
                        isCommissionPaidOut = false
                    )
                )
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SalesForceRepository? = null

        fun getInstance(context: Context): SalesForceRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SalesForceRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
