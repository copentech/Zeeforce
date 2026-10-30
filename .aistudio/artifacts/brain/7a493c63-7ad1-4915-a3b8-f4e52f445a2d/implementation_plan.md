# Implementation Plan - Zayed SalesForce Finalization (Revised)

This revised plan incorporates the multi-model commission structure (fixed amount + percentage), the 24-hour expiring temporary password workflow with mandatory password reset, lightweight APK (<5MB) & installed size (<20MB) optimization, interactive Compose Canvas sales and low-stock dashboards, individual sales rep KPI dashboards, branded commission slips, stealth Admin auth, staff account governance, customer archival, and Supabase offline sync.

---

## 1. Core Architecture & Data Model (Room Database v5)

We will update Room entities and bump `OrderDatabase` to **Version 5** (with automatic migration):

### A. Flexible Commission Structure in `OrderEntity` & `StaffMember`
- **Commission Modes**:
  - `PERCENTAGE`: e.g., 3.5% of order subtotal.
  - `FIXED`: e.g., KES 1,500 flat per delivered order.
- **Fields in `StaffMember`**:
  - `commissionType: String = "PERCENTAGE"` (`"PERCENTAGE"` or `"FIXED"`)
  - `commissionRate: Double = 0.035` (for percentage mode)
  - `fixedCommissionAmount: Double = 1500.0` (for fixed amount mode)
  - `accountStatus: String = "ACTIVE"` (`"ACTIVE"`, `"SUSPENDED"`, `"TERMINATED"`)
  - `passwordResetRequested: Boolean = false`
  - `tempPassword: String = ""`
  - `tempPasswordExpiresAt: Long = 0L` (Epoch millis; valid for 24 hours from creation)
  - `isTemporaryPassword: Boolean = false`
- **Fields in `OrderEntity`**:
  - `commissionType: String = "PERCENTAGE"`
  - `commissionRate: Double = 0.035`
  - `commissionAmount: Double = 0.0` (Auto-calculated via fixed or % rule; editable by Admin)
  - `commissionStatus: String = "PENDING"` (`"PENDING"`, `"APPROVED"`, `"PAID"`)
  - `commissionPaymentCode: String = ""` (Payment ref / M-Pesa / Bank code entered by Admin)
  - `commissionPaidAt: Long = 0L`
  - `isPaid: Boolean = false`

### B. Customer Archival Support in `Customer`
- `isArchived: Boolean = false` (Replaces destructive deletion; preserves sales history).
- Filter queries in `CustomerDao` to separate active and archived customer records with instant restore.

---

## 2. 24-Hour Temporary Password & Password Reset Flow

1. **User Password Reset Request**:
   - On the `LoginScreen`, a staff member can click **"Forgot Password?"**.
   - Prompts for their registered email or phone, flagging `passwordResetRequested = true` in Room DB.
2. **Admin Issuance of 24h Temporary Password**:
   - In the Admin Console under Staff Management, Admin sees pending reset requests or can select any staff member.
   - Admin taps **"Issue 24-Hour Temporary Password"**.
   - System generates an 8-character temporary password (e.g. `TMP-7491`) and sets:
     - `tempPassword = generated`
     - `tempPasswordExpiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)`
     - `isTemporaryPassword = true`
     - `passwordResetRequested = false`
   - Admin copies or transmits the temporary password to the staff member.
3. **Expiring Login & Mandatory Password Change**:
   - User signs in using their temporary password.
   - The auth engine validates the password and checks whether `System.currentTimeMillis() <= tempPasswordExpiresAt`.
   - If expired (> 24 hours), login is blocked with an alert: *"Temporary password expired after 24 hours. Please request a new code from your Administrator."*
   - If valid, user is immediately presented with a mandatory **"Change Your Password"** dialog.
   - Upon saving their new password, `password = newPassword`, `isTemporaryPassword = false`, and `tempPassword = ""` are persisted, granting normal access.

---

## 3. APK & Installed Size Optimization (< 5MB APK / < 20MB Installed)

To ensure the APK is **under 5MB** and installed size is **under 20MB**:
1. **Dependency Hygiene (`app/build.gradle.kts`)**:
   - Comment out heavy unused Firebase modules (`firebase-ai`, `firebase-appcheck`, etc.) that are not required for offline-first order booking.
   - Rely strictly on lightweight core libraries: Jetpack Compose, Room SQLite, Moshi, and Kotlin Coroutines.
2. **Native Canvas Visualization (Zero External Chart SDKs)**:
   - Build custom high-performance charts using Android's native `androidx.compose.foundation.Canvas`. This eliminates 5–10MB of third-party charting libraries while delivering 60 FPS touch-interactive graphs.
3. **Asset & Resource Optimization**:
   - Use vector drawables for UI icons and compressed WebP/PNG for emblems.
   - Enable code minification and resource shrinking for release configurations.

---

## 4. Admin Visual Analytics & Low-Stock Suite (`AdminSalesCharts.kt`)

1. **Total Daily Sales Visualizer**:
   - **7-Day Revenue Trend**: Bar chart with daily comparisons, baseline average, and touch tooltips.
   - **Hourly Sales Timeline**: Volume progression across 24 hours.
   - **Revenue vs Volume Split**: Side-by-side comparative visualizer for delivered orders and gross revenue.
   - **Interactive Switcher**: Toggle seamlessly between all 3 visual layouts.
2. **Three-Tier Low-Stock Alert System**:
   - **Badge Counter Card**: Real-time counter of depleted products (`<= 10 units`, respecting infinite stock items where `stockQty = -1`).
   - **Critical Stock Filter**: Dedicated tab to isolate all low-stock items.
   - **One-Tap Quick Restock**: Modal dialog allowing Admin to increment inventory with one tap directly from the dashboard.
3. **Executive KPI Overview**:
   - Total Gross Revenue, Active Booking Pipeline, Delivery Completion Rate, and Total Commission Payable.

---

## 5. Sales Rep Dashboard & Branded Commission Payout Slips

1. **Individual Sales Rep KPI Dashboard (`SalesRepDashboardScreen.kt`)**:
   - Reps see their personal metrics: My Daily Sales, Monthly Sales, Booked vs Delivered Orders, Commission Earned vs Received.
2. **Commission Approval & Payout Workflow**:
   - Admin marks order as **DELIVERED** and **PAID**.
   - Admin opens the **Commission Approval Dialog**, which calculates commission using either:
     - **Percentage mode** (e.g., 3.5% × order total)
     - **Fixed amount mode** (e.g., KES 1,500 flat)
   - Admin pastes the payment reference code (e.g. M-Pesa code or Bank transaction ID).
   - Order commission status updates to `PAID`.
3. **Branded Printable Commission Slip**:
   - Rep views and downloads/shares a formal **Al Yasir Bin Zayed Commission Slip** with company emblem, order details, calculated commission, Admin payment reference code, and official verification stamp.

---

## 6. Stealth Admin Auth & Full Staff Lifecycle Governance

1. **Public Login Form Hardening**:
   - Remove all public admin setup buttons, admin quick-pick chips, and sensitive labels from the public sign-in page.
   - Generic corporate login (Email/Phone + Password).
   - If an Admin logs in, it silently triggers the dedicated **4-8 Digit PIN Verification Screen** (`AdminPinScreen.kt`).
   - Blocks suspended or terminated accounts.
2. **Admin Staff Account Controls**:
   - Admin can edit staff contact details, reset passwords, change PINs, configure commission type (Fixed vs %), and set account status to **Active**, **Suspended**, or **Terminated**, or delete accounts.

---

## 7. Customer Profile Archival & Restoration

1. **Safe Archival**:
   - Staff and Admin can "Archive" customers rather than delete, ensuring historical orders remain linked.
2. **Directory Filter & Admin Restore**:
   - View "Active Customers" vs "Archived Customers".
   - Admin can restore archived customers or permanently purge records.

---

## 8. Verification & Testing

1. **Unit & Logic Tests**:
   - Validate fixed vs percentage commission calculation.
   - Test temporary password expiration (verifying access is denied after 24h).
   - Verify customer archival filtering and staff suspension blocks.
2. **Build & Compilation Check**:
   - Verify app compiles cleanly with `compile_applet`.
