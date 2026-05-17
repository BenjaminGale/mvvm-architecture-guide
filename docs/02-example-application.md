# 2. Example Application

This repository contains a sample order management application that demonstrates the patterns described in the guide. It spans three feature areas — orders, customers, and stock — touching every architectural layer described in this guide.

> Items marked **[planned]** are not yet implemented but are included here to describe the intended scope of the example application.

**Requirements:** Java 21 or later.

```bash
cd example && ./gradlew run
```

---

## Domain

### Customer

A customer represents a person or organisation that places orders.

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `name` | String | Display name |
| `email` | String | Contact email address |
| `status` | `CustomerStatus` | Whether the customer is active |

**CustomerStatus**

| Value | Description |
|---|---|
| `ACTIVE` | Customer can place orders |
| `INACTIVE` | Customer is no longer active; excluded from order entry |

**Domain operations on Customer**

| Operation | Type | Guard | Description |
|---|---|---|---|
| `DeleteCustomer` **[planned]** | Command | Customer has no orders | Permanently removes the customer |
| `DeactivateCustomer` **[planned]** | Command | Customer has orders | Marks the customer as `INACTIVE`; any `WIP` orders are cancelled; any `FULFILLED` orders are cancelled and their allocated stock is returned to inventory. The user is asked to confirm before the operation proceeds. `SHIPPED` orders are retained for historical record. |

---

### Product **[planned]**

A product is a stocked item that can be added to an order. It holds a snapshot of the current price and tracks stock levels.

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `name` | String | Display name |
| `unitPrice` | BigDecimal | Current selling price per unit |
| `quantityInStock` | int | Total units held in inventory |

`quantityAvailable` is derived as `quantityInStock` minus the sum of `quantityAllocated` across all line items referencing this product on `WIP` or `FULFILLED` orders.

**Domain operations on Product**

| Operation | Type | Guard | Description |
|---|---|---|---|
| `RestockProduct` | Command | Quantity must be positive | Adds units to `quantityInStock` |

---

### Order

An order represents a purchase placed by a customer, consisting of one or more line items.

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `customerId` **[planned]** | UUID | The customer who placed the order |
| `reference` | String | A short human-readable identifier |
| `date` | LocalDate | The date the order was created |
| `completionDate` **[planned]** | LocalDate | The date the order was shipped or cancelled; absent while the order is open |
| `status` **[planned]** | `OrderStatus` | The current lifecycle state of the order |
| `lineItems` | `List<LineItem>` | The items on the order |

An order is **valid** when it has a non-empty reference, an associated customer, and at least one line item. Its **total** is the sum of all line item totals.

**OrderStatus** **[planned]**

Orders follow a lifecycle driven by stock allocation. The transition between `WIP` and `FULFILLED` is automatic — it occurs whenever stock is allocated or returned, based on whether all line items are fully allocated.

```
WIP ⇌ FULFILLED → SHIPPED
 ↘       ↘
  CANCELLED
```

| Value | Description |
|---|---|
| `WIP` | Order is being prepared; stock may be partially allocated |
| `FULFILLED` | All line items are fully allocated; order is ready to ship |
| `SHIPPED` | Order has been dispatched; stock is finalised and cannot be returned |
| `CANCELLED` | Order was cancelled; any allocated stock has been returned to inventory |

An order is considered **overdue** when it has not been shipped within 30 days of its `date` and is neither `SHIPPED` nor `CANCELLED`.

**Domain operations on Order**

| Operation | Type | Guard | Description |
|---|---|---|---|
| `CopyOrder` | Command | Order must exist | Creates a new `WIP` order copied from an existing one, with a new ID, today's date, and a `COPY-` prefix on the reference; no stock is allocated |
| `AllocateStock` **[planned]** | Command | Order is `WIP` or `FULFILLED`; line item is not fully allocated; product has sufficient available stock | Increases `LineItem.quantityAllocated`; decreases `Product.quantityAvailable`; transitions the order to `FULFILLED` if all line items are now fully allocated |
| `ReturnStock` **[planned]** | Command | Order is `WIP` or `FULFILLED`; line item has allocated stock | Decreases `LineItem.quantityAllocated`; increases `Product.quantityAvailable`; transitions the order back to `WIP` if it was `FULFILLED` |
| `ShipOrder` **[planned]** | Command | Order is `FULFILLED` | Transitions the order to `SHIPPED`; reduces `Product.quantityInStock` by the allocated amounts; sets `completionDate` to today |
| `CancelOrder` **[planned]** | Command | Order is `WIP` or `FULFILLED` | Returns all allocated stock to inventory; transitions the order to `CANCELLED`; sets `completionDate` to today |

---

### LineItem

A line item records a product added to an order. The product name and unit price are captured as a snapshot at the time the line item is created, so the order reflects what the customer was charged even if the product details change later.

| Property | Type | Description |
|---|---|---|
| `productId` **[planned]** | UUID | Reference to the product |
| `description` | String | Product name at the time of order entry |
| `quantity` | int | How many units ordered |
| `quantityAllocated` **[planned]** | int | How many units of stock have been allocated |
| `unitPrice` | BigDecimal | Price per unit at the time of order entry |

Its **total** is `quantity × unitPrice`. A line item is **fully allocated** when `quantityAllocated == quantity`.

---

## Features

### Orders Explorer

The orders explorer is the main list screen for browsing all orders.

- Display all orders in a table, sorted by date descending
- Columns: Reference, Customer, Date, Total, Status **[planned]** (`WIP` / `FULFILLED` / `SHIPPED` / `CANCELLED`), Overdue indicator
- Refresh the list
- Open an order in the editor by selecting it
- Show the total number of orders and the number of overdue orders in the status bar
- Filter orders by status **[planned]**

---

### Order Editor

The order editor opens when a user selects an existing order or creates a new one. It is the primary workspace for order entry and lifecycle management.

**Header**

- Edit the order reference
- Edit the order date
- Select a customer from a list of active customers **[planned]** *(currently a free-text field)*

**Line Items**

- View all line items in a table, with a running total
- Columns: Product, Quantity, Allocated **[planned]**, Unit Price, Total
- Add a new line item by selecting a product **[planned]** *(currently free-text entry)*
- Edit the quantity of an existing line item via a dialog
- Remove a line item
- Allocate stock to a line item **[planned]**
- Return allocated stock from a line item **[planned]**
- Line items are read-only once the order is `FULFILLED` or `SHIPPED` **[planned]**

**Toolbar actions**

| Action | Guard | Description |
|---|---|---|
| Save | Order is `WIP` and valid | Persists the current state of the order |
| Ship **[planned]** | Order is `FULFILLED` | Marks the order as shipped; stock is finalised |
| Cancel **[planned]** | Order is `WIP` or `FULFILLED` | Cancels the order; allocated stock is returned |
| Copy | — | Creates a new `WIP` order copied from this one |
| Delete | — | Permanently removes the order |

---

### Customers Explorer

The customers explorer lists all active customers.

- Display active customers in a table, sorted by name
- Columns: Name, Email, Order count **[planned]**, Total spend **[planned]**
- Open a customer in the editor by selecting it
- Add a new customer

---

### Customer Editor

The customer editor opens as a dialog when adding or editing a customer.

- Edit the customer's name and email address
- Set the customer's status (Active / Inactive)
- Save changes or cancel without saving
- View the customer's order history **[planned]**

**Toolbar actions** **[planned]**

| Action | Guard | Description |
|---|---|---|
| Delete | Customer has no orders | Permanently removes the customer |
| Deactivate | Customer has orders | Cancels open orders (returning stock for `FULFILLED` ones), marks customer as `INACTIVE`; prompts the user to confirm before proceeding |

---

### Stock Explorer **[planned]**

The stock explorer lists all products and their current inventory levels.

- Display all products in a table, sorted by name
- Columns: Product name, Unit price, In stock, Allocated, Available
- Open a product in the editor by selecting it
- Add a new product

---

### Product Editor **[planned]**

The product editor opens as a dialog when adding or editing a product.

- Edit the product name and unit price
- Save changes or cancel without saving
- Add stock by entering a quantity to restock
