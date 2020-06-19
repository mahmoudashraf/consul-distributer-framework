package com.consul.leader.elections.test;

import org.springframework.context.annotation.ComponentScan;


// @Configuration
/*
 * @ComponentScan("pl.piomin.services.customer.leader," +
 * "pl.piomin.services.customer.leader.annotation," + "pl.piomin.services.customer.event," +
 * "pl.piomin.services.customer.dto," + "pl.piomin.services.customer.exception," +
 * "pl.piomin.services.customer.services," + "pl.piomin.services.customer.example," +
 * "pl.piomin.services.customer.test")
 *
 */
@ComponentScan("com.consul.leader.elections.resources,com.consul.leader.elections.test")

public class TestConfig {
}
