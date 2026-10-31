# Zayed SalesForce Enterprise

Enterprise Route-to-Market, Inventory, Multi-Mode Commission & Regional Order Booking System.

**Zayed SalesForce** is a production-grade Android enterprise system built with modern Kotlin and Jetpack Compose (Material 3). Designed for distributors, wholesale enterprises, logistics fleets, and multi-tier commercial sales teams, it seamlessly coordinates operations across Administrators, Field Sales Representatives, Retail/Commercial Customers, and Regional Suppliers.

---

## 👥 Operational Accounts & Clean Slate Architecture

The system launches directly into the **Executive Admin Console** with zero splash screen delays, zero login barriers, and zero sign-out obstructions.

- **Pre-Configured Default Administrator**: Al Yasir Bin Zayed (`yasirzayedco@gmail.com`), System Owner & Executive Administrator with unrestricted root access to all modules, dispatches, audits, and settings.
- **Clean Slate Database**: All demo starter inventory and mock customer listings have been cleared. Enterprise users start with a clean production database to author their own products, customer directories, dispatches, and supplier partnerships.
- **Master Security PIN**: `22026` for administrative authorization and security overrides.

---

## 🚀 Key Modules & Capabilities

### 1. Executive Admin Console & 9-Module Hub
- **Sales Analytics Suite**: Interactive Vico Cartesian Charts showing rolling 7-Day sales revenue trends (column and line visualizations) and 24-hour hourly dispatch distribution timelines.
- **Staff Management & Privilege Control**: Role-based access control (RBAC) to toggle individual module permissions (Inventory, Orders, Customers) and issue temporary expiring credentials.
- **Inventory Catalog**: Unlimited (∞) and finite inventory policies, SKU management, unit pricing, dynamic price ranges, and real-time stock updating.
- **3-Layer Stock Alert Widget**: Real-time triage classifying warehouse SKUs into **Low Stock**, **Critical (<5 units)**, and **Out of Stock** with one-tap restocking.
- **Order Fulfillment & Logistics Dispatch**: Route scheduling, confirmed delivery timestamps, invoice previews, and payment approval workflows.
- **Customer Directory & Archival Governance**: Safe customer archival protecting historical order integrity, deletion approval queues, and wholesale registration.
- **Supplier Performance Hub**: Supplier volume breakdown, transit monitoring, and commission margin metrics.
- **Multi-Mode Commission Ledger**: Calculates both percentage-based (e.g., 3.5%–5%) and flat fixed fees (e.g., $25/order) with downloadable official PDF payout slips.
- **Security Audit Logs**: Comprehensive immutable log recording administrative interventions, status changes, permission modifications, and security PIN updates.

### 2. Global Currency System
- Centralized system currency selector configurable via **Settings**.
- Live formatting across all reports, invoices, carts, commission slips, and dashboard KPIs.
- Supported currencies: **USD ($)**, **KES (KSh)**, **AED (AED)**, **EUR (€)**, **GBP (£)**, **ZAR (R)**, **NGN (₦)**.

### 3. Biometric Security & Master PIN Layer
- Hardware-backed biometric authentication (Fingerprint / Face Unlock via Android BiometricPrompt).
- Secondary 5-digit PIN authorization for high-risk operations with master administrative bypass (`22026`).

### 4. Lightweight & Battery-Conscious Sync Layer
- **Ultra-Light Footprint**: Minimal installable size (<5MB).
- **GZIP Sync Compression**: Payloads are compressed with GZIP and Base64 encoded before transit to Supabase, guaranteeing transmission size <1MB per sync cycle.
- **Battery-Aware WorkManager**: Configured with `setRequiresBatteryNotLow(true)` and `NetworkType.CONNECTED` constraints to pause non-critical background synchronization during low battery states.

---

## 📸 Dashboard Screenshots

### 1. Executive Admin Dashboard
Manage the entire system with high-level KPIs, 7-day revenue reports, and automated inventory alerts.
![Admin Dashboard](./app/src/main/res/drawable/admin_dashboard_screenshot_1793355095907.jpg)

### 2. Sales Representative Dashboard
Track your sales pipeline, view earned commissions, and generate official commission slips.
![Sales Rep Dashboard](./app/src/main/res/drawable/sales_rep_dashboard_screenshot_1793355111752.jpg)

### 3. Customer Account Portal
Track order status in real-time, view pricing, and contact assigned representatives.
![Customer Account](./app/src/main/res/drawable/customer_account_screenshot_1793355125449.jpg)

### 4. Supplier Logistics Hub
Manage unprocessed orders, track transit status, and optimize delivery performance.
![Supplier Dashboard](./app/src/main/res/drawable/supplier_dashboard_screenshot_1793355138392.jpg)

---

## 🛠 Tech Stack

- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Local Persistence**: Room Database (SQLite) with 10 tables
- **Cloud Backend**: Supabase BaaS (PostgreSQL, Realtime Outbox, Session Auth)
- **Background Engine**: AndroidX WorkManager with Power & Network Constraints
- **Visualization**: Vico Cartesian Charting Library
- **Security**: AndroidX Biometric & Master PIN Verification
- **Reporting**: Android Canvas-to-PDF Printing (`AdminReportPdfPrinter` & `CommissionPdfPrinter`)
