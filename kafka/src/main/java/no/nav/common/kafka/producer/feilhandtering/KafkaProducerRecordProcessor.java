package no.nav.common.kafka.producer.feilhandtering;

import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.kafka.producer.util.ProducerUtils;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class KafkaProducerRecordProcessor {

    private final static long ERROR_TIMEOUT_MS = 5000;

    private final static long POLL_TIMEOUT_MS = 3000;

    private final static long WAITING_FOR_LEADER_TIMEOUT_MS = 10_000;

    private final static long RECORDS_OLDER_THAN_MS = 0;

    private final static int RECORDS_BATCH_SIZE = 100;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final KafkaProducerRepository producerRepository;

    private final Producer<byte[], byte[]> producer;

    private final LeaderElectionClient leaderElectionClient;

    private volatile boolean isRunning;

    private volatile boolean isClosed;

    public KafkaProducerRecordProcessor(
            KafkaProducerRepository producerRepository,
            Producer<byte[], byte[]> producerClient,
            LeaderElectionClient leaderElectionClient
    ) {
        this.producerRepository = producerRepository;
        this.producer = producerClient;
        this.leaderElectionClient = leaderElectionClient;

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public void start() {
        if (isClosed) {
            throw new IllegalStateException("Cannot start closed producer record processor");
        }

        if (!isRunning) {
            executorService.submit(this::recordHandlerLoop);
        }
    }

    public void close() {
        isRunning = false;
        isClosed = true;
    }

    private void recordHandlerLoop() {
        isRunning = true;

        try {
           while (isRunning) {
               try {
                   if (!leaderElectionClient.isLeader()) {
                       Thread.sleep(WAITING_FOR_LEADER_TIMEOUT_MS);
                       continue;
                   }

                   Instant recordsOlderThan = Instant.now().minusMillis(RECORDS_OLDER_THAN_MS);
                   List<StoredProducerRecord> records = producerRepository.getRecords(recordsOlderThan, RECORDS_BATCH_SIZE);

                   if (!records.isEmpty()) {
                       publishStoredRecordsBatch(records);
                   }

                   // If the number of records are less than the max batch size,
                   //   then most likely there are not many messages to process and we can wait a bit
                   if (records.size() < RECORDS_BATCH_SIZE) {
                       Thread.sleep(POLL_TIMEOUT_MS);
                   }
               } catch (Exception e) {
                   log.error("Failed to process kafka producer records", e);
                   Thread.sleep(ERROR_TIMEOUT_MS);
               }
           }
       } catch (Exception e) {
           log.error("Unexpected exception caught in record handler loop", e);
       } finally {
           log.info("Closing kafka producer record processor...");
           producer.close();
       }
    }

    private void publishStoredRecordsBatch(List<StoredProducerRecord> records) throws InterruptedException {
        // TODO: could be done inside a kafka transaction

        CountDownLatch latch = new CountDownLatch(records.size());

        records.forEach(record -> {
            producer.send(ProducerUtils.mapFromStoredRecord(record), (metadata, exception) -> {
                try {
                    if (exception != null) {
                        log.warn(format("Failed to resend failed message to topic %s", record.getTopic()), exception);
                    } else {
                        producerRepository.deleteRecord(record.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to send message to kafka", e);
                } finally {
                    latch.countDown();
                }
            });
        });

        producer.flush();

        latch.await();
    }

}
