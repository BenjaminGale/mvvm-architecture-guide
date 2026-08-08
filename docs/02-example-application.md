# 2. Example Application

This repository contains a sample order management application that demonstrates the patterns described in the guide. It spans three feature areas — orders, customers, and stock — touching every architectural layer described in this guide.

> Items marked **[planned]** are not yet implemented but are included here to describe the intended scope of the example application.

**Requirements:** Java 25 or later.

```bash
cd example && ./gradlew run
```

---

## Application Shell

The application window is built from chrome shared across every feature: a **sidebar**, a **toolbar**, a tabbed content area, and a **status bar**.

```
┌────────────┬───────────────────────────────────────────┐
│ Sidebar    │ Toolbar                                   │
│            ├───────────────────────────────────────────│
│ Orders     │ Tabs: [Explorer] [Editor]                 │
│ Customers  │┌─────────────────────────────────────────┐│
│ Stock      ││ selected tab's content                  ││
│            ││ (an explorer or an editor)              ││
│            ││                                         ││
│            │└─────────────────────────────────────────┘│
├────────────┴───────────────────────────────────────────┤
│ Status bar                                             │
└────────────────────────────────────────────────────────┘
```

**Sidebar** — A fixed navigation panel on the left with one entry per workspace: Orders, Customers, Stock. Selecting an entry switches the active workspace; exactly one is selected at a time.

**Workspaces** — A workspace is one of the app's top-level domain areas. Each owns its own tabs independently of the others, so switching away and back leaves everything exactly as it was. A workspace opens with its explorer tab already showing; further tabs are opened by drilling into individual records.

**Tabs** — Each workspace has its own tabbed content area.
- *Explorer tabs* are unclosable and open automatically as soon as the workspace exists — one per workspace.
- *Editor tabs* are closable and open on demand, when a record is created or selected from its explorer.
- Opening a record that's already open re-selects its existing tab instead of opening a duplicate.

**Toolbar** — A single toolbar above the tab content, rebuilt for whichever tab is currently selected. It combines actions scoped to the whole workspace (e.g. "Add" on an explorer) with actions scoped to the selected tab (e.g. "Save", "Copy", "Delete" on an order editor), so what appears depends on both which workspace and which tab is active.

**Status bar** — A row along the bottom of the window showing information contextual to the selected tab — for example, total and overdue order counts on the Orders Explorer. It updates as the selected tab changes, and is empty for tabs that don't publish anything.

**Explorers and editors** — Every workspace follows the same pattern:
- An **explorer** is the entry point: a table listing all records of that type, opened as the workspace's initial, unclosable tab.
- An **editor** opens when a record is created or selected. Orders open their editor as a closable tab within the workspace; Customers and Stock open theirs as a dialog.

The Features section below describes each workspace's explorer and editor in detail.

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

A product is a stocked item that can be added to an order. It holds a snapshot of the current selling price; stock itself is tracked separately, in batches (see [StockBatch](#stockbatch-planned)).

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `name` | String | Display name |
| `unitPrice` | BigDecimal | Current selling price per unit |
| `quantityInStock` | int | Total units physically on hand |

`quantityInStock`, `quantityAvailable` **[planned]**, and `inventoryValue` **[planned]** are all derived properties, summed across the product's stock batches, rather than stored fields: `quantityInStock` is the sum of each batch's `quantityRemaining`, `quantityAvailable` is the sum of each batch's `quantityAvailable`, and `inventoryValue` is the sum of each batch's `value`. Until batching is implemented, `quantityInStock` remains a plain stored field with no batch-level detail, and `quantityAvailable`/`inventoryValue` are not tracked at all.

---

### StockBatch **[planned]**

A stock batch records a quantity of a product booked into inventory at a point in time, together with the cost paid to acquire it. Stock is allocated to order line items from specific batches, via Allocation (see [LineItem](#lineitem)): a batch of 10 can supply 5 units to one order and 3 to another, leaving 2 available, and a single line item can likewise draw from more than one batch of the same product to satisfy its full quantity.

| Property | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `productId` | UUID | The product this batch is stock for |
| `receivedDate` | LocalDate | The date the batch was booked in |
| `quantityReceived` | int | Units originally booked into this batch |
| `unitCost` | BigDecimal | Cost paid per unit, snapshotted at receipt so later changes to the product's selling price don't affect it |

`quantityRemaining` is derived, not stored: `quantityReceived` minus the sum of allocated quantities against this batch belonging to `SHIPPED` orders — only shipping physically depletes a batch. `quantityAvailable` is derived further: `quantityRemaining` minus the sum of allocated quantities belonging to `IN_PROGRESS` orders — stock held for a pending order is on hand but not free to allocate elsewhere. `value` is derived as `quantityRemaining × unitCost`.

**Domain operations on StockBatch**

| Operation | Type | Guard | Description |
|---|---|---|---|
| `ReceiveStockBatch` **[planned]** | Command | Quantity must be positive; cost must not be negative | Creates a new `StockBatch` for a product with today's `receivedDate` |

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

An order is **valid** when it has a non-empty reference, an associated customer, and at least one line item. Its **total** is the sum of all line item totals, and its **margin** **[planned]** is the sum of all line item margins.

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
| `AllocateStock` **[planned]** | Command | Order is `IN_PROGRESS`; line item is not fully allocated; product has sufficient available stock across its batches | Creates one or more `Allocation` records against the line item, drawing from the product's `StockBatch`es until the requested quantity is satisfied. Which batch(es) are drawn from is an allocation strategy decision that isn't settled yet (e.g. FIFO), and may end up user-configurable. |
| `ReturnStock` **[planned]** | Command | Order is `IN_PROGRESS`; line item has allocated stock | Removes or reduces the line item's `Allocation` record(s), returning quantity to the originating batch(es) |
| `ShipOrder` **[planned]** | Command | Order is `IN_PROGRESS`<br>Has at least one line item<br>Every line item is fully allocated (sum of its allocation quantities equals `quantity`) | Transitions the order to `SHIPPED`; sets `completionDate` to today. No change to `Allocation` records is needed — each batch's `quantityRemaining` is derived from allocations belonging to `SHIPPED` orders, so it depletes automatically once the order's status changes. |
| `CancelOrder` **[planned]** | Command | Order is `IN_PROGRESS` | Deletes all of the order's `Allocation` records, returning quantity to the originating batches; transitions the order to `CANCELLED`; sets `completionDate` to today |

---

### LineItem

A line item records a product added to an order. The product name and unit price are captured as a snapshot at the time the line item is created, so the order reflects what the customer was charged even if the product details change later.

| Property | Type | Description |
|---|---|---|
| `productId` | UUID | Reference to the product |
| `description` | String | Product name at the time of order entry |
| `quantity` | int | How many units ordered |
| `unitPrice` | BigDecimal | Price per unit at the time of order entry |

Its **total** is `quantity × unitPrice`. `quantityAllocated` **[planned]**, `costOfGoodsSold` **[planned]**, and `margin` **[planned]** are all derived, not stored. `quantityAllocated` is the sum of the line item's Allocation quantities, and the line item is **fully allocated** when `quantityAllocated == quantity`. `costOfGoodsSold` is the sum of `allocation.quantity × batch.unitCost` across those same allocations — the actual cost of the specific batches consumed, not an average. `margin` is `total − costOfGoodsSold`.

**Allocation** **[planned]**

An allocation records that some quantity of a line item's ordered units has been drawn from a specific stock batch. A line item can have several allocations — including more than one against the same batch, or against different batches of the same product — if that's what it takes to satisfy its quantity.

| Property | Type | Description |
|---|---|---|
| `lineItemId` | UUID | The line item this allocation is for |
| `batchId` | UUID | The batch this allocation draws from |
| `quantity` | int | Units allocated from this batch to this line item |

---

## Features

### Shell

The chrome shared across every workspace — see [Application Shell](#application-shell) above for how the pieces fit together.

- Navigate between workspaces (Orders, Customers, Stock) via the sidebar
- Switch tabs within the active workspace; open tabs persist when switching away to another workspace and back
- Close an editor tab
- Reuse an already-open editor tab instead of opening a duplicate when the same record is selected again
- Run the toolbar action for the selected tab
- View the status bar content for the selected tab
- Confirm before closing a tab with unsaved changes **[planned]** — `TabViewModel.closable` already accepts a `canClose` veto hook, but no editor supplies one yet, so closing a tab with unsaved changes still discards them silently
- Open a tab in one workspace from another (e.g. an order editor opening a customer detail tab) **[planned]**
- Visual indication of how many editable tabs are open in a workspace **[planned]**
- Visual indication that a workspace has unsaved edits **[planned]**

---

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
- Columns: Name, Email, Status, Order count **[planned]**, Total spend **[planned]**, Total margin **[planned]**
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
- Allocated, Available, Inventory value columns **[planned]** — depend on the batch/allocation model described under [Product](#product) and [StockBatch](#stockbatch-planned)
- Open a product in the editor by selecting it **[planned]**
- Add a new product **[planned]**

---

### Product Editor **[planned]**

The product editor opens as a dialog when adding or editing a product.

- Edit the product name and unit price
- Save changes or cancel without saving
- View the product's stock batches in a table, sorted by received date descending
  - Columns: Received, Quantity received, Remaining, Unit cost, Value
- Receive stock by entering a quantity and unit cost, creating a new stock batch (`ReceiveStockBatch`)
