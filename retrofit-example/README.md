# 🚀 Testing Guide: Retrofit API Swagger

This guide walks you through using the **Retrofit API Swagger** plugin to inspect, navigate, and execute the pre-configured `DummyJsonService` endpoints directly inside Android Studio / IntelliJ IDEA.

---

## ⚙️ Step 1: Configure the Server Environment

Before running API calls, configure the default Base URL and headers in the plugin settings:

1. Click the **Gear Icon (⚙️)** in the top right of the **Retrofit API Swagger** Tool Window (or navigate to `Settings / Preferences | Tools | Retrofit API Swagger`).
2. Set the **Base URL**:
   ```text
   [https://dummyjson.com](https://dummyjson.com)
   ```
3. *(Optional)* In the **Global Request Headers** table, click **`+`** to add test headers:

   | Header Name (Key) | Header Value |
         | :--- | :--- |
   | `Content-Type` | `application/json` |
   | `Authorization` | `Bearer test_token_123` |

4. Click **Apply** and **OK**.

---

## 🧪 Test Scenarios

### 1. Test Editor Navigation & Type Lookup
Open `DummyJsonService.kt` in the editor to test IDE integration features:

* **Gutter Icon Jump:** Click the web icon located in the editor gutter next to `@POST("products/add")`. The tool window will open and focus directly on the `addProduct` card.
* **Click-to-Navigate Type:** In the `addProduct` parameter card, hover over `Type: DummyProductRequest`. Click the hyperlink to jump straight to the source file (`DummyProductRequest.kt`).

---

### 2. Test GET Endpoints (Path & Query Parameters)

#### Test A: Path Parameters (`getProductById`)
1. In the plugin tool window, locate `GET products/{id}`.
2. Enter a test ID in the `id` field (e.g., `1`).
3. Click **Try It Out**.
4. **Expected Result:** Returns product details for item `#1` from `https://dummyjson.com/products/1`.

#### Test B: Query Parameters (`searchProducts`)
1. Locate `GET products/search`.
2. Enter a search query in the `q` field (e.g., `phone`).
3. Click **Try It Out**.
4. **Expected Result:** Returns a list of matching products from `https://dummyjson.com/products/search?q=phone`.

---

### 3. Test POST Endpoints & Body Schema Generation (`addProduct`)

1. Locate `POST products/add`.
2. Observe the **Example Value | Schema** text area. The plugin automatically reads `DummyProductRequest` properties and populates a mock body:
   ```json
   {
     "title": "string",
     "description": "string",
     "price": 0.0,
     "category": "string"
   }
   ```
3. Modify the JSON payload with custom values:
   ```json
   {
     "title": "Perfume Oil",
     "description": "Mega Impression Height",
     "price": 13.0,
     "category": "fragrances"
   }
   ```
4. Click **Try It Out**.
5. **Expected Result:** `DummyJSON` returns an HTTP 201/200 success response echoing back your newly created item with a generated `id` (e.g., `id: 195`).

---

### 4. Test PATCH Endpoints (`updateProduct`)

1. Locate `PATCH products/{id}`.
2. Fill in the path parameter `id` (e.g., `1`).
3. Modify the update body:
   ```json
   {
     "title": "Updated Laptop Title"
   }
   ```
4. Click **Try It Out**.
5. **Expected Result:** Returns the updated product object confirming the partial update.

---

## 🔍 Verification Checklist

| Feature Under Test | Action | Pass Criteria |
| :--- | :--- | :--- |
| **Base URL Prefixing** | Execute any endpoint without leading slashes. | Target URL evaluates cleanly to `https://dummyjson.com/...` |
| **Gutter Marker** | Click gutter icon next to `@GET("products/search")`. | Tool window activates and scrolls to `searchProducts`. |
| **PSI Class Lookup** | Click `DummyProductRequest` link. | IDE opens `DummyProductRequest.kt` editor tab. |
| **JSON Schema Generation** | View `addProduct` body section. | Auto-generates valid JSON based on `DummyProductRequest` fields. |
| **Global Headers** | Send any request. | Headers saved in Settings are attached to the outgoing request. |