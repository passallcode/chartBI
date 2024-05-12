package com.yupi.springbootinit.achievemq;

import org.springframework.stereotype.Component;

@Component
public interface BIConstant {
    String BI_EXCHANGE = "bi_exchange";
    String BI_QUEUE = "bi_queue";
    String BI_ROUTING_KEY = "bi_routing_key";

}
