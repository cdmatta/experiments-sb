package com.github.cdmatta.experiment.saf.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.security.*;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

@Configuration
@EnableConfigurationProperties(SafProperties.class)
@EnableJms
@Slf4j
public class BrokerConfiguration {
    private static final String ADMIN_GROUP = "admin";
    private static final String PRODUCER_GROUP = "producer";

    @Bean(destroyMethod = "stop")
    BrokerService brokerService(SafProperties safProperties) throws Exception {
        BrokerService broker = new BrokerService();

        broker.setBrokerName("saf-broker");
        broker.setUseJmx(false);

        broker.setSystemUsage(systemUsage(safProperties));
        broker.setAdvisorySupport(safProperties.isAdvisorySupportEnabled());
        broker.setPlugins(setupPlugins(broker, safProperties));
        broker.addConnector(safProperties.getBrokerUrl());

        if (safProperties.isPersistenceEnabled()) {
            broker.setDataDirectory(safProperties.getKahaDbPath());
        } else {
            log.warn("SAF broker configured with in-memory storage.");
            broker.setPersistent(false);
        }

        broker.start();
        return broker;
    }

    private SystemUsage systemUsage(SafProperties safProperties) {
        MemoryUsage memoryUsage = new MemoryUsage();
        memoryUsage.setPercentOfJvmHeap(safProperties.getBrokerMemoryPercentage());

        SystemUsage systemUsage = new SystemUsage();
        systemUsage.setMemoryUsage(memoryUsage);

        return systemUsage;
    }

    private BrokerPlugin[] setupPlugins(BrokerService broker, SafProperties safProperties) throws Exception {
        ArrayList<BrokerPlugin> plugins = new ArrayList<>(asList(nullToEmpty(broker.getPlugins(), BrokerPlugin[].class)));

        plugins.add(authenticationPlugin(safProperties));
        plugins.add(authorizationPlugin(safProperties));

        return plugins.toArray(new BrokerPlugin[]{});
    }

    private SimpleAuthenticationPlugin authenticationPlugin(SafProperties safProperties) {
        SimpleAuthenticationPlugin plugin = new SimpleAuthenticationPlugin();
        List<AuthenticationUser> users = new ArrayList<>();

        users.add(new AuthenticationUser(
                safProperties.getUser().getUsername(),
                safProperties.getUser().getPassword(),
                PRODUCER_GROUP
        ));

        users.add(new AuthenticationUser(
                safProperties.getAdmin().getUsername(),
                safProperties.getAdmin().getPassword(),
                ADMIN_GROUP
        ));

        plugin.setUsers(users);
        plugin.setAnonymousAccessAllowed(false);
        return plugin;
    }

    private AuthorizationPlugin authorizationPlugin(SafProperties safProperties) throws Exception {
        List<DestinationMapEntry> authorizationEntries = new ArrayList<>();

        AuthorizationEntry safQueueEntry = new AuthorizationEntry();
        safQueueEntry.setQueue(SafProperties.DATA_QUEUE_NAME);
        safQueueEntry.setRead(ADMIN_GROUP);
        safQueueEntry.setWrite(PRODUCER_GROUP + "," + ADMIN_GROUP); // Anyone can dispatch a message
        safQueueEntry.setAdmin(ADMIN_GROUP);
        authorizationEntries.add(safQueueEntry);

        AuthorizationEntry dlqEntry = new AuthorizationEntry();
        dlqEntry.setQueue(SafProperties.DLQ_NAME);
        dlqEntry.setRead(ADMIN_GROUP);
        dlqEntry.setWrite(ADMIN_GROUP);
        dlqEntry.setAdmin(ADMIN_GROUP);
        authorizationEntries.add(dlqEntry);

        AuthorizationEntry advisoryEntry = new AuthorizationEntry();
        advisoryEntry.setTopic("ActiveMQ.Advisory.>");
        advisoryEntry.setRead(PRODUCER_GROUP + "," + ADMIN_GROUP); // Advisory messages can be read by anyone
        advisoryEntry.setWrite(ADMIN_GROUP);
        advisoryEntry.setAdmin(ADMIN_GROUP);
        authorizationEntries.add(advisoryEntry);

        return new AuthorizationPlugin(new DefaultAuthorizationMap(authorizationEntries));
    }

    @Bean(destroyMethod = "stop")
    public PooledConnectionFactory pooledConnectionFactory(SafProperties safProperties) {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(connectionFactory(safProperties));
        SafProperties.Pool pool = safProperties.getPool();
        pooledConnectionFactory.setMaxConnections(pool.getMaxConnections());
        pooledConnectionFactory.setIdleTimeout((int) pool.getIdleTimeout().toMillis());
        pooledConnectionFactory.setExpiryTimeout(pool.getExpiryInMillisOrZero());
        return pooledConnectionFactory;
    }

    private ActiveMQConnectionFactory connectionFactory(SafProperties safProperties) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                safProperties.getAdmin().getUsername(),
                safProperties.getAdmin().getPassword(),
                safProperties.getBrokerUrl()
        );
        connectionFactory.setRedeliveryPolicy(redeliveryPolicy(safProperties.getRedeliveryPolicy()));
        connectionFactory.setNonBlockingRedelivery(safProperties.getRedeliveryPolicy().isNonBlockingRedeliveryEnabled());
        return connectionFactory;
    }

    private RedeliveryPolicy redeliveryPolicy(SafProperties.RedeliveryPolicy policy) {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setInitialRedeliveryDelay(policy.getInitialRedeliveryDelay().toMillis());
        redeliveryPolicy.setMaximumRedeliveryDelay(policy.getMaxRedeliveryDelay().toMillis());
        redeliveryPolicy.setMaximumRedeliveries(policy.getMaximumRedeliveries());
        redeliveryPolicy.setUseExponentialBackOff(policy.isUseExponentialBackoff());
        redeliveryPolicy.setBackOffMultiplier(policy.getBackOffMultiplier());
        redeliveryPolicy.setUseCollisionAvoidance(policy.isUseCollisionAvoidance());
        redeliveryPolicy.setCollisionAvoidancePercent(policy.getCollisionAvoidancePercent());
        return redeliveryPolicy;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                      DefaultJmsListenerContainerFactoryConfigurer configurer,
                                                                      SafProperties safProperties) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(t -> log.warn("Failed to handle event", t));
        factory.setSessionTransacted(true);
        factory.setConcurrency("1-" + safProperties.getListenerMaxConcurrency());
        factory.setCacheLevelName("CACHE_CONSUMER"); //DefaultMessageListenerContainer.CACHE_CONSUMER
        return factory;
    }
}
