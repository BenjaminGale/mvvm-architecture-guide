# 2. Example Application

This repository contains a sample order management application that demonstrates the patterns described in the guide. It spans three feature areas — orders, customers, and stock — touching every architectural layer described in this guide.

> Items marked **[planned]** are not yet implemented but are included here to describe the intended scope of the example application.

**Requirements:** Java 25 or later.

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
| `DeactivateCustomer` **[planned]** | Command | Customer has orders | Marks the customer as `INACTIVE`; any `IN_PROGRESS` orders are cancelled, and any stock allocated to their line items is returned to inventory. The user is asked to confirm before the operation proceeds. `SHIPPED` orders are retained for historical record. |

---

### Product

A product is a stocked item that can be added to an order. It holds a snapshot of the current price and tracks stock levels.

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `name` | String | Display name |
| `unitPrice` | BigDecimal | Current selling price per unit |
| `quantityInStock` | int | Total units held in inventory |

`quantityAvailable` **[planned]** is a derived property, not a stored field: `quantityInStock` minus the sum of `quantityAllocated` across all line items referencing this product on open orders. It is not yet tracked — there is no allocation concept in the domain yet.

**Domain operations on Product**

| Operation | Type | Guard | Description |
|---|---|---|---|
| `RestockProduct` **[planned]** | Command | Quantity must be positive | Adds units to `quantityInStock` |

---

### Order

An order represents a purchase placed by a customer, consisting of one or more line items.

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `customerId` | UUID | The customer who placed the order |
| `reference` | String | A short human-readable identifier |
| `createdDate` | LocalDate | The date the order was created; set automatically and read-only |
| `plannedShipDate` | LocalDate | The date the order is expected to ship; set by the user |
| `completionDate` | LocalDate | The date the order was shipped or cancelled; absent while the order is open. The field exists but nothing currently sets it — `ShipOrder`/`CancelOrder` are not yet implemented **[planned]** |
| `status` | `OrderStatus` | The current lifecycle state of the order |
| `lineItems` | `List<LineItem>` | The items on the order |

An order is **valid** when it has a non-empty reference, an associated customer, and at least one line item. Its **total** is the sum of all line item totals.

**OrderStatus**

```
IN_PROGRESS → SHIPPED
 ↘
   CANCELLED
```

| Value | Description |
|---|---|
| `IN_PROGRESS` | Order is open; not yet shipped or cancelled |
| `SHIPPED` | Order has been dispatched **[planned]** — no command currently transitions an order to this state |
| `CANCELLED` | Order was cancelled **[planned]** — no command currently transitions an order to this state |

An order is considered **overdue** when its `plannedShipDate` is in the past and its status is `IN_PROGRESS`.

**Domain operations on Order**

| Operation | Type | Guard | Description |
|---|---|---|---|
| `CopyOrder` | Command | Order must exist | Creates a new `IN_PROGRESS` order copied from an existing one, with a new ID, today's `createdDate`, no `plannedShipDate`, and a `COPY-` prefix on the reference |
| `AllocateStock` **[planned]** | Command | Order is `IN_PROGRESS`; line item is not fully allocated; product has sufficient available stock | Increases `LineItem.quantityAllocated`. `Product.quantityAvailable` decreases as a side effect of being derived from it. |
| `ReturnStock` **[planned]** | Command | Order is `IN_PROGRESS`; line item has allocated stock | Decreases `LineItem.quantityAllocated`. `Product.quantityAvailable` increases as a side effect of being derived from it. |
| `ShipOrder` **[planned]** | Command | Order is `IN_PROGRESS`<br>Has at least one line item<br>Every line item is fully allocated (`quantityAllocated == quantity`) | Transitions the order to `SHIPPED`; reduces `Product.quantityInStock` by each line item's allocated amount; sets `completionDate` to today |
| `CancelOrder` **[planned]** | Command | Order is `IN_PROGRESS` | Returns all allocated stock to inventory (zeroes `quantityAllocated` on each line item); transitions the order to `CANCELLED`; sets `completionDate` to today |

---

### LineItem

A line item records a product added to an order. The product name and unit price are captured as a snapshot at the time the line item is created, so the order reflects what the customer was charged even if the product details change later.

| Property | Type | Description |
|---|---|---|
| `productId` | UUID | Reference to the product |
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
- Columns: Reference, Customer, Created, Ship By, Status (`IN_PROGRESS` / `SHIPPED` / `CANCELLED`), Total
- Overdue orders are highlighted in the table
- Refresh the list
- Open an order in the editor by selecting it
- Show the total number of orders and the number of overdue orders in the status bar
- Filter orders by status **[planned]**

---

### Order Editor

The order editor opens when a user selects an existing order or creates a new one. It is the primary workspace for order entry and lifecycle management.

**Header**

- Edit the order reference
- Display the `createdDate` (read-only)
- Edit the `plannedShipDate` via a date picker
- Select a customer from a list of active customers via a dialog

**Line Items**

- View all line items in a table, with a running total
- Columns: Product, Quantity, Unit Price, Total
- Add a new line item by selecting a product via a dialog
- Edit the quantity of an existing line item via a dialog
- Remove a line item
- Allocate stock to a line item **[planned]**
- Return allocated stock from a line item **[planned]**
- Line items are read-only once the order is `SHIPPED` or `CANCELLED` **[planned]**

**Toolbar actions**

| Action | Guard | Description |
|---|---|---|
| Save | Order is valid | Persists the current state of the order |
| Ship **[planned]** | See `ShipOrder` guard under [Order](#order) | Marks the order as shipped; stock is finalised |
| Cancel **[planned]** | See `CancelOrder` guard under [Order](#order) | Cancels the order |
| Copy | — | Creates a new `IN_PROGRESS` order copied from this one |
| Delete | — | Permanently removes the order |

---

### Customers Explorer

The customers explorer lists all active customers.

- Display active customers in a table, sorted by name
- Columns: Name, Email, Status, Order count **[planned]**, Total spend **[planned]**
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
| Delete | See `DeleteCustomer` guard under [Customer](#customer) | Permanently removes the customer |
| Deactivate | See `DeactivateCustomer` guard under [Customer](#customer) | Marks the customer as `INACTIVE` |

---

### Stock Explorer

The stock explorer lists all products and their current inventory levels. It is currently read-only: adding, editing, and deleting products are all disabled pending the Product Editor below.

- Display all products in a table, sorted by name
- Columns: Product name, Unit price, In stock
- Allocated, Available columns **[planned]** — depend on the allocation concept described under [Product](#product)
- Open a product in the editor by selecting it **[planned]**
- Add a new product **[planned]**

---

### Product Editor **[planned]**

The product editor opens as a dialog when adding or editing a product.

- Edit the product name and unit price
- Save changes or cancel without saving
- Add stock by entering a quantity to restock
