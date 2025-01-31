package no.nav.common.kafka.consumer.feilhandtering.util;

import net.javacrumbs.shedlock.core.LockProvider;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessorConfig;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.backoff.LinearBackoffStrategy;
import no.nav.common.kafka.consumer.feilhandtering.backoff.BackoffStrategy;
import no.nav.common.kafka.consumer.util.TopicConsumerConfig;

import java.time.Duration;
import java.util.List;

import static no.nav.common.kafka.consumer.util.ConsumerUtils.createTopicConsumers;

public class KafkaConsumerRecordProcessorBuilder {

    private final static Duration DEFAULT_ERROR_TIMEOUT = Duration.ofMinutes(5);
    private final static Duration DEFAULT_POLL_TIMEOUT = Duration.ofMinutes(1);
    private final static int DEFAULT_RECORDS_BATCH_SIZE = 100;
    private final static BackoffStrategy DEFAULT_RETRY_BACKOFF_STRATEGY = new LinearBackoffStrategy(
            0,
            12 * 60 * 60, // Max 12 hours backoff
            144 // Each failed retry will add 5 additional minutes before retrying again
    );

    private KafkaConsumerRecordProcessorBuilder() { }

    private LockProvider lockProvider;

    private KafkaConsumerRepository kafkaConsumerRepository;

    private List<TopicConsumerConfig<?, ?>> topicConsumerConfigs;

    private final KafkaConsumerRecordProcessorConfig config = new KafkaConsumerRecordProcessorConfig(
            DEFAULT_ERROR_TIMEOUT,
            DEFAULT_POLL_TIMEOUT,
            DEFAULT_RECORDS_BATCH_SIZE,
            DEFAULT_RETRY_BACKOFF_STRATEGY
    );

    public static KafkaConsumerRecordProcessorBuilder builder() {
        return new KafkaConsumerRecordProcessorBuilder();
    }

    public KafkaConsumerRecordProcessorBuilder withLockProvider(LockProvider lockProvider) {
        this.lockProvider = lockProvider;
        return this;
    }

    public KafkaConsumerRecordProcessorBuilder withKafkaConsumerRepository(KafkaConsumerRepository kafkaConsumerRepository) {
        this.kafkaConsumerRepository = kafkaConsumerRepository;
        return this;
    }

    public KafkaConsumerRecordProcessorBuilder withConsumerConfigs(List<TopicConsumerConfig<?, ?>> topicConsumerConfigs) {
        this.topicConsumerConfigs = topicConsumerConfigs;
        return this;
    }

    public KafkaConsumerRecordProcessorBuilder withErrorTimeout(Duration errorTimeout) {
        this.config.setErrorTimeout(errorTimeout);
        return this;
    }

    public KafkaConsumerRecordProcessorBuilder withPollTimeout(Duration pollTimeout) {
        this.config.setPollTimeout(pollTimeout);
        return this;
    }

    public KafkaConsumerRecordProcessorBuilder withRecordBatchSize(int recordBatchSize) {
        this.config.setRecordBatchSize(recordBatchSize);
        return this;
    }

    public KafkaConsumerRecordProcessorBuilder withBackoffStrategy(BackoffStrategy backoffStrategy) {
        this.config.setBackoffStrategy(backoffStrategy);
        return this;
    }

    public KafkaConsumerRecordProcessor build() {
        if (lockProvider == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without lockProvider");
        }

        if (kafkaConsumerRepository == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without kafkaConsumerRepository");
        }

        if (topicConsumerConfigs == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without recordConsumers");
        }

        if (config.getErrorTimeout() == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without config.errorTimeout");
        }

        if (config.getPollTimeout() == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without config.pollTimeout");
        }

        if (config.getBackoffStrategy() == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without config.retryBackoffStrategy");
        }

        return new KafkaConsumerRecordProcessor(
                lockProvider,
                kafkaConsumerRepository,
                createTopicConsumers(topicConsumerConfigs),
                config
        );
    }
}
