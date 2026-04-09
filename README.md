# Expiry Tracker

Expiry Tracker is a smart Android application designed to help users manage and track the expiration dates of their products. By reducing food waste and ensuring you use items before they expire, this app simplifies your pantry and fridge management.

## Features

- **Quick Add:** Easily add products with their name and expiry date.
- **Smart Scanning (v1.1):**
    - **Barcode Scanner:** Scan product barcodes to instantly identify items.
    - **Product Lookup:** Automatically fetches product names from the [Open Food Facts](https://world.openfoodfacts.org/) database.
    - **Expiry Date Recognition:** Uses AI-powered OCR to scan expiry dates directly from product packaging.
- **Automated Alerts:** Receive notifications before products expire. Customizable lead times (e.g., 2 days before).
- **Search & Filter:** Find products by name, expiry date, or date range.
- **Inventory Management:** View total products, expired items, and those expiring soon at a glance on the home dashboard.
- **Customizable Settings:**
    - Toggle alerts.
    - Set default reminder times.
    - Choose preferred date formats and sort orders.
    - Auto-delete expired items after a set period.

## Technologies Used

- **Language:** Kotlin
- **UI:** XML with View Binding and Material Design components.
- **Database:** SQLite (via `SQLiteOpenHelper`) for local data persistence.
- **AI/ML:** 
    - [Google ML Kit](https://developers.google.com/ml-kit) for Barcode Scanning and Text Recognition.
    - [CameraX](https://developer.android.com/training/camerax) for camera integration.
- **Networking:** [OkHttp](https://square.github.io/okhttp/) for fetching product data from the Open Food Facts API.
- **Architecture:** Clean and modular code structure for easy maintenance.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/pradeep2481/expirytracker.git
   ```
2. Open the project in **Android Studio**.
3. Build and run the app on your Android device (requires Camera permission for scanning features).

## Usage

1. **Add a Product:** Go to the 'Add' tab. You can manually enter details or use the camera icon to scan a barcode or an expiry date.
2. **Track:** Check the 'Home' tab for a summary of your inventory status.
3. **Search:** Use the 'Search' tab to find specific items in your list.
4. **Settings:** Customize notifications and display preferences in the 'Settings' tab.

---
Developed by [Pradeep Kumar](https://github.com/pradeep2481)
