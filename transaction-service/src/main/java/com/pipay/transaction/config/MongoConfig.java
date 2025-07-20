//package com.pipay.transaction.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.domain.ReactiveAuditorAware;
//import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
//import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
//import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
//import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
//import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
//import org.springframework.data.mongodb.core.validation.Validator;
//import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
//import reactor.core.publisher.Mono;
//
//@Configuration
//@EnableReactiveMongoAuditing
//public class MongoConfig {
//
//    @Bean
//    public ValidatingMongoEventListener validatingMongoEventListener(LocalValidatorFactoryBean factory) {
//        return new ValidatingMongoEventListener(factory);
//    }
//
//    @Bean
//    public LocalValidatorFactoryBean validator() {
//        return new LocalValidatorFactoryBean();
//    }
//
//    @Bean
//    public ReactiveAuditorAware<String> auditorProvider() {
//        return () -> Mono.just("system"); // You can implement user context here
//    }
//
//    // Optional: Custom configuration to remove _class field from documents
//    @Bean
//    public MappingMongoConverter mappingMongoConverter(ReactiveMongoTemplate mongoTemplate) {
//        MappingMongoConverter converter = (MappingMongoConverter) mongoTemplate.getConverter();
//        converter.setTypeMapper(null); // Remove _class field
//        return converter;
//    }
//}
