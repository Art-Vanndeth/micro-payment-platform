//package com.pipay.gateway.config;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class GatewayConfig {
//
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("account-service", r -> r.path("/api/accounts/**")
//                        .filters(f -> f.rewritePath("/api/accounts/(?<segment>.*)", "/api/accounts/${segment}"))
//                        .uri("lb://ACCOUNT-SERVICE"))
//                .route("payment-service", r -> r.path("/api/payments/**")
//                        .filters(f -> f.rewritePath("/api/payments/(?<segment>.*)", "/${segment}"))
//                        .uri("lb://PAYMENT-SERVICE"))
//                .route("transaction-service", r -> r.path("/api/transactions/**")
//                        .filters(f -> f.rewritePath("/api/transactions/(?<segment>.*)", "/${segment}"))
//                        .uri("lb://TRANSACTION-SERVICE"))
//                .route("notification-service", r -> r.path("/api/notifications/**")
//                        .filters(f -> f.rewritePath("/api/notifications/(?<segment>.*)", "/${segment}"))
//                        .uri("lb://NOTIFICATION-SERVICE"))
//                .build();
//    }
//}