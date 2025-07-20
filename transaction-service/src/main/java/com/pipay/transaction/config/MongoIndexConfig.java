//package com.pipay.transaction.config;
//
//import com.pipay.transaction.entity.Transaction;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
//import org.springframework.data.mongodb.core.index.Index;
//import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class MongoIndexConfig {
//
//    private final ReactiveMongoTemplate mongoTemplate;
//
//    @PostConstruct
//    public void initIndexes() {
//        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(Transaction.class);
//
//        // Create indexes for better query performance
//        indexOps.indexExists("fromAccountId_1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index().on("fromAccountId", Sort.Direction.ASC)))
//            .subscribe();
//
//        indexOps.indexExists("toAccountId_1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index().on("toAccountId", Sort.Direction.ASC)))
//            .subscribe();
//
//        indexOps.indexExists("paymentId_1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index().on("paymentId", Sort.Direction.ASC)))
//            .subscribe();
//
//        indexOps.indexExists("status_1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index().on("status", Sort.Direction.ASC)))
//            .subscribe();
//
//        indexOps.indexExists("transactionType_1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index().on("transactionType", Sort.Direction.ASC)))
//            .subscribe();
//
//        indexOps.indexExists("createdAt_-1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index().on("createdAt", Sort.Direction.DESC)))
//            .subscribe();
//
//        // Compound indexes for common queries
//        indexOps.indexExists("fromAccountId_1_status_1_createdAt_-1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index()
//                .on("fromAccountId", Sort.Direction.ASC)
//                .on("status", Sort.Direction.ASC)
//                .on("createdAt", Sort.Direction.DESC)))
//            .subscribe();
//
//        indexOps.indexExists("toAccountId_1_status_1_createdAt_-1")
//            .filter(exists -> !exists)
//            .flatMap(notExists -> indexOps.createIndex(new Index()
//                .on("toAccountId", Sort.Direction.ASC)
//                .on("status", Sort.Direction.ASC)
//                .on("createdAt", Sort.Direction.DESC)))
//            .subscribe();
//
//        log.info("MongoDB indexes initialization completed for Transaction collection");
//    }
//}
