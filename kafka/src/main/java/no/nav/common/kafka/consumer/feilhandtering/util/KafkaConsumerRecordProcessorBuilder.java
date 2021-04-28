package no.nav.common.kafka.consumer.feilhandtering.util;

import net.javacrumbs.shedlock.core.LockProvider;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessorConfig;
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRepository;
import no.nav.common.kafka.consumer.feilhandtering.StoredRecordConsumer;

import java.time.Duration;
import java.util.Map;

public class KafkaConsumerRecordProcessorBuilder {

    private final static Duration DEFAULT_ERROR_TIMEOUT = Duration.ofMinutes(1);
    private final static Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(30);
    private final static int DEFAULT_RECORDS_BATCH_SIZE = 100;

    private KafkaConsumerRecordProcessorBuilder() { }

    private LockProvider lockProvider;

    private KafkaConsumerRepository kafkaConsumerRepository;

    private Map<String, StoredRecordConsumer> recordConsumers;

    private final KafkaConsumerRecordProcessorConfig config = new KafkaConsumerRecordProcessorConfig(
            DEFAULT_ERROR_TIMEOUT,
            DEFAULT_POLL_TIMEOUT,
            DEFAULT_RECORDS_BATCH_SIZE);

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

    public KafkaConsumerRecordProcessorBuilder withRecordConsumers(Map<String, StoredRecordConsumer> recordConsumers) {
        this.recordConsumers = recordConsumers;
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

    public KafkaConsumerRecordProcessor build() {
        if (lockProvider == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without lockProvider");
        }

        if (kafkaConsumerRepository == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without kafkaConsumerRepository");
        }

        if (recordConsumers == null) {
            throw new IllegalStateException("Cannot build kafka consumer record processor without recordConsumers");
        }

        return new KafkaConsumerRecordProcessor(
                lockProvider,
                kafkaConsumerRepository,
                recordConsumers,
                config);
    }
}
