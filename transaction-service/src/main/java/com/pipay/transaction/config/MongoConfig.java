package com.pipay.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@Configuration
@EnableReactiveMongoAuditing
public class MongoConfig {
    // MongoDB reactive configuration for auditing fields like @CreatedDate, @LastModifiedDate
}
