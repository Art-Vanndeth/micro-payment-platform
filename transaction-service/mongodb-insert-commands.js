// Raw MongoDB Insert Commands for Transaction Collection
// Use these commands in MongoDB Compass, MongoDB Shell, or any MongoDB client

// 1. Insert a CREDIT transaction (recipient receiving money)
db.transactions.insertOne({
    "_id": "txn_001_credit_2025",
    "transactionId": "txn_001_credit_2025",
    "paymentId": "pay_12345_001",
    "fromAccountId": "acc_sender_001",
    "toAccountId": "acc_recipient_001",
    "amount": NumberDecimal("150.00"),
    "currency": "USD",
    "transactionType": "CREDIT",
    "status": "COMPLETED",
    "description": "Payment received from sender",
    "createdAt": new Date("2025-07-20T10:30:00Z"),
    "updatedAt": new Date("2025-07-20T10:30:05Z"),
    "completedAt": new Date("2025-07-20T10:30:05Z"),
    "gatewayTransactionId": "gtw_txn_001_abc123",
    "gatewayResponse": "SUCCESS",
    "createdBy": "system",
    "lastModifiedBy": "system",
    "metadata": {
        "processingTime": "5000ms",
        "gatewayProvider": "stripe",
        "transactionFee": "2.50"
    }
});

// 2. Insert a DEBIT transaction (sender sending money)
db.transactions.insertOne({
    "_id": "txn_001_debit_2025",
    "transactionId": "txn_001_debit_2025",
    "paymentId": "pay_12345_001",
    "fromAccountId": "acc_sender_001",
    "toAccountId": "acc_recipient_001",
    "amount": NumberDecimal("152.50"),
    "currency": "USD",
    "transactionType": "DEBIT",
    "status": "COMPLETED",
    "description": "Payment sent to recipient",
    "createdAt": new Date("2025-07-20T10:30:00Z"),
    "updatedAt": new Date("2025-07-20T10:30:05Z"),
    "completedAt": new Date("2025-07-20T10:30:05Z"),
    "gatewayTransactionId": "gtw_txn_001_def456",
    "gatewayResponse": "SUCCESS",
    "createdBy": "user_123",
    "lastModifiedBy": "system",
    "metadata": {
        "processingTime": "5000ms",
        "gatewayProvider": "stripe",
        "includesFee": true
    }
});

// 3. Insert a FAILED transaction
db.transactions.insertOne({
    "_id": "txn_002_failed_2025",
    "transactionId": "txn_002_failed_2025",
    "paymentId": "pay_12346_002",
    "fromAccountId": "acc_sender_002",
    "toAccountId": "acc_recipient_002",
    "amount": NumberDecimal("500.00"),
    "currency": "EUR",
    "transactionType": "DEBIT",
    "status": "FAILED",
    "description": "International transfer attempt",
    "createdAt": new Date("2025-07-20T11:15:00Z"),
    "updatedAt": new Date("2025-07-20T11:15:30Z"),
    "gatewayTransactionId": "gtw_txn_002_xyz789",
    "gatewayResponse": "INSUFFICIENT_FUNDS",
    "failureReason": "Insufficient funds in source account",
    "createdBy": "user_456",
    "lastModifiedBy": "system",
    "metadata": {
        "attemptCount": 1,
        "gatewayProvider": "paypal",
        "errorCode": "2001"
    }
});

// 4. Insert a PENDING transaction
db.transactions.insertOne({
    "_id": "txn_003_pending_2025",
    "transactionId": "txn_003_pending_2025",
    "paymentId": "pay_12347_003",
    "fromAccountId": "acc_sender_003",
    "toAccountId": "acc_recipient_003",
    "amount": NumberDecimal("75.25"),
    "currency": "GBP",
    "transactionType": "CREDIT",
    "status": "PENDING",
    "description": "Cross-border payment processing",
    "createdAt": new Date("2025-07-20T12:00:00Z"),
    "updatedAt": new Date("2025-07-20T12:00:00Z"),
    "gatewayTransactionId": "gtw_txn_003_mno123",
    "gatewayResponse": "PROCESSING",
    "createdBy": "user_789",
    "lastModifiedBy": "user_789",
    "metadata": {
        "estimatedCompletionTime": "2025-07-21T12:00:00Z",
        "gatewayProvider": "wise",
        "requiresManualReview": false
    }
});

// 5. Insert multiple transactions using insertMany
db.transactions.insertMany([
    {
        "_id": "txn_004_batch_2025",
        "transactionId": "txn_004_batch_2025",
        "paymentId": "pay_batch_001",
        "fromAccountId": "acc_corporate_001",
        "toAccountId": "acc_employee_001",
        "amount": NumberDecimal("2500.00"),
        "currency": "USD",
        "transactionType": "CREDIT",
        "status": "COMPLETED",
        "description": "Salary payment",
        "createdAt": new Date("2025-07-20T09:00:00Z"),
        "updatedAt": new Date("2025-07-20T09:00:02Z"),
        "completedAt": new Date("2025-07-20T09:00:02Z"),
        "gatewayTransactionId": "gtw_salary_001",
        "gatewayResponse": "SUCCESS",
        "createdBy": "payroll_system",
        "lastModifiedBy": "payroll_system",
        "metadata": {
            "payrollBatch": "2025-07-20",
            "employeeId": "emp_001",
            "department": "Engineering"
        }
    },
    {
        "_id": "txn_005_batch_2025",
        "transactionId": "txn_005_batch_2025",
        "paymentId": "pay_batch_002",
        "fromAccountId": "acc_corporate_001",
        "toAccountId": "acc_employee_002",
        "amount": NumberDecimal("3000.00"),
        "currency": "USD",
        "transactionType": "CREDIT",
        "status": "COMPLETED",
        "description": "Salary payment",
        "createdAt": new Date("2025-07-20T09:00:00Z"),
        "updatedAt": new Date("2025-07-20T09:00:02Z"),
        "completedAt": new Date("2025-07-20T09:00:02Z"),
        "gatewayTransactionId": "gtw_salary_002",
        "gatewayResponse": "SUCCESS",
        "createdBy": "payroll_system",
        "lastModifiedBy": "payroll_system",
        "metadata": {
            "payrollBatch": "2025-07-20",
            "employeeId": "emp_002",
            "department": "Product"
        }
    }
]);

// 6. Create indexes for better query performance
db.transactions.createIndex({ "paymentId": 1 });
db.transactions.createIndex({ "fromAccountId": 1 });
db.transactions.createIndex({ "toAccountId": 1 });
db.transactions.createIndex({ "status": 1 });
db.transactions.createIndex({ "transactionType": 1 });
db.transactions.createIndex({ "createdAt": -1 });
db.transactions.createIndex({ "gatewayTransactionId": 1 }, { unique: true });
db.transactions.createIndex({ "fromAccountId": 1, "status": 1 });
db.transactions.createIndex({ "toAccountId": 1, "status": 1 });

// 7. Query examples to verify the data
// Find all transactions for a specific payment
db.transactions.find({ "paymentId": "pay_12345_001" });

// Find all completed transactions
db.transactions.find({ "status": "COMPLETED" });

// Find all transactions for a specific account (either sender or receiver)
db.transactions.find({
    $or: [
        { "fromAccountId": "acc_sender_001" },
        { "toAccountId": "acc_sender_001" }
    ]
});

// Find transactions within a date range
db.transactions.find({
    "createdAt": {
        $gte: new Date("2025-07-20T00:00:00Z"),
        $lte: new Date("2025-07-20T23:59:59Z")
    }
});

// Find failed transactions with failure reasons
db.transactions.find({
    "status": "FAILED",
    "failureReason": { $exists: true }
});
