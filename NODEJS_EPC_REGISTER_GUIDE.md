# Node.js Express API Integration Guide: EPC Register

This document provides instructions on how to implement the backend endpoint for bulk linen registration in an Express.js application.

## 1. Endpoint Specification
* **Route:** `POST /api/linens/register-batch`
* **Content-Type:** `application/json`

### Request Payload (JSON)
```json
{
  "linen_id": "LN-XXXXXX",
  "linen_type": "Bed Sheet",
  "linen_height": 200,
  "linen_width": 150,
  "linen_length": 10,
  "linen_max_cycle": 120,
  "linen_description": "Standard cotton bed sheet",
  "linen_created_date": "2023-10-27T10:00:00.000Z",
  "linen_size_category": "MEDIUM",
  "linen_weight": 0.85,
  "linen_material": "Cotton",
  "linen_supplier": "Supplier A",
  "linen_budget_source": "Budget 2023",
  "operator_username": "admin",
  "epc_list": ["EPC001", "EPC002", "EPC003"]
}
```

## 2. Database Schema
Ensure your `linens` table has the following columns:

| Column Name | Data Type | Notes |
|-------------|-----------|-------|
| `linen_id` | `VARCHAR(50)` | Primary Key or Unique Identifier for the batch |
| `epc` | `VARCHAR(50)` | Primary Key (Individual Unique Tag) |
| `linen_type` | `VARCHAR(25)` | |
| `linen_height` | `INT4` | |
| `linen_width` | `INT4` | |
| `linen_length` | `INT4` | |
| `linen_max_cycle` | `INT4` | |
| `linen_description` | `VARCHAR(255)` | |
| `linen_created_date` | `TIMESTAMP` | |
| `linen_size_category` | `VARCHAR(20)` | |
| `linen_weight` | `NUMERIC(10, 2)` | |
| `linen_material` | `VARCHAR(100)` | |
| `linen_supplier` | `VARCHAR(100)` | |
| `linen_budget_source` | `VARCHAR(100)` | |
| `operator_username` | `VARCHAR(100)` | |

## 3. Implementation Example (Node.js + Express + pg)

```javascript
const express = require('express');
const router = express.Router();
const pool = require('./db'); // Your database connection pool

router.post('/api/linens/register-batch', async (req, res) => {
    const client = await pool.connect();
    try {
        const {
            linen_id,
            linen_type,
            linen_height,
            linen_width,
            linen_length,
            linen_max_cycle,
            linen_description,
            linen_created_date,
            linen_size_category,
            linen_weight,
            linen_material,
            linen_supplier,
            linen_budget_source,
            operator_username,
            epc_list
        } = req.body;

        await client.query('BEGIN');

        const insertedEpcs = [];
        const skippedExistingEpcs = [];

        for (const epc of epc_list) {
            // Check if EPC already exists
            const checkRes = await client.query('SELECT epc FROM linens WHERE epc = $1', [epc]);
            
            if (checkRes.rows.length === 0) {
                // Insert new linen
                await client.query(`
                    INSERT INTO linens (
                        epc, linen_id, linen_type, linen_height, linen_width, linen_length, 
                        linen_max_cycle, linen_description, linen_created_date, 
                        linen_size_category, linen_weight, linen_material, 
                        linen_supplier, linen_budget_source, operator_username
                    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
                `, [
                    epc, linen_id, linen_type, linen_height, linen_width, linen_length,
                    linen_max_cycle, linen_description, linen_created_date,
                    linen_size_category, linen_weight, linen_material,
                    linen_supplier, linen_budget_source, operator_username
                ]);
                insertedEpcs.push(epc);
            } else {
                skippedExistingEpcs.push(epc);
            }
        }

        await client.query('COMMIT');

        res.status(200).json({
            message: 'Batch registration completed',
            inserted_epcs: insertedEpcs,
            skipped_existing_epcs: skippedExistingEpcs
        });

    } catch (err) {
        await client.query('ROLLBACK');
        console.error(err);
        res.status(500).json({ error: 'Internal server error' });
    } finally {
        client.release();
    }
});

module.exports = router;
```

## 4. Key Logic Points
1. **Transaction Management:** Use `BEGIN`, `COMMIT`, and `ROLLBACK` to ensure data integrity during batch insertion.
2. **Deduplication:** The server should check if an EPC already exists before attempting an insert to avoid Primary Key violations. Alternatively, use `INSERT ... ON CONFLICT (epc) DO NOTHING`.
3. **Validation:** Ensure all numeric fields are correctly parsed and not null if required by your schema.
4. **Timestamps:** Handle the `linen_created_date` as a valid ISO timestamp for your database.
