package no.nav.common.kafka.consumer.feilhandtering;

import lombok.SneakyThrows;
import org.apache.kafka.common.TopicPartition;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static java.lang.String.format;
import static no.nav.common.kafka.util.DatabaseConstants.*;
import static no.nav.common.kafka.util.DatabaseUtils.*;

public class OracleConsumerRepository implements KafkaConsumerRepository {

    private final DataSource dataSource;

    public OracleConsumerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    public long storeRecord(KafkaConsumerRecord record) {
        String sql = format(
                "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                CONSUMER_RECORD_TABLE, ID, TOPIC, PARTITION, RECORD_OFFSET, KEY, VALUE, HEADERS_JSON
        );

        long id = incrementAndGetOracleSequence(dataSource, CONSUMER_RECORD_ID_SEQ);

        try (PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setLong(1, id);
            statement.setString(2, record.getTopic());
            statement.setInt(3, record.getPartition());
            statement.setLong(4, record.getOffset());
            statement.setBytes(5, record.getKey());
            statement.setBytes(6, record.getValue());
            statement.setString(7, record.getHeadersJson());
            statement.executeUpdate();

            return id;
        } catch (SQLIntegrityConstraintViolationException e) {
            return -1;
        }
    }

    @SneakyThrows
    @Override
    public void deleteRecords(List<Long> ids) {
        String sql = format("DELETE FROM %s WHERE %s = ANY(?)", CONSUMER_RECORD_TABLE, ID);
        try (PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            Array array = dataSource.getConnection().createArrayOf("INTEGER", ids.toArray());
            statement.setArray(1, array);
            statement.executeUpdate();
        }
    }

    @SneakyThrows
    @Override
    public boolean hasRecordWithKey(String topic, int partition, byte[] key) {
        String sql = format(
                "SELECT %s FROM %s WHERE %s = ? AND %s = ? AND %s = ? FETCH NEXT 1 ROWS ONLY",
                ID, CONSUMER_RECORD_TABLE, TOPIC, PARTITION, KEY
        );

        try (PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setString(1, topic);
            statement.setInt(2, partition);
            statement.setBytes(3, key);

            return statement.executeQuery().next();
        }
    }

    @SneakyThrows
    @Override
    public List<KafkaConsumerRecord> getRecords(String topic, int partition, int maxRecords) {
        String sql = format(
                "SELECT * FROM %s WHERE %s = ? AND %s = ? ORDER BY %s FETCH NEXT %d ROWS ONLY",
                CONSUMER_RECORD_TABLE, TOPIC, PARTITION, ID, maxRecords
        );

        try (PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setString(1, topic);
            statement.setInt(2, partition);
            return fetchConsumerRecords(statement.executeQuery());
        }
    }

    @SneakyThrows
    @Override
    public void incrementRetries(long id) {
        String sql = format(
                "UPDATE %s SET %s = %s + 1, %s = CURRENT_TIMESTAMP WHERE %s = ?",
                CONSUMER_RECORD_TABLE, RETRIES, RETRIES, LAST_RETRY, ID
        );

        try (PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            statement.setLong(1, id);
            statement.execute();
        }
    }

    @SneakyThrows
    @Override
    public List<TopicPartition> getTopicPartitions(List<String> topics) {
        String sql = format(
                "SELECT DISTINCT %s, %s FROM %s WHERE %s = ANY(?)",
                TOPIC, PARTITION, CONSUMER_RECORD_TABLE, TOPIC
        );

        try (PreparedStatement statement = createPreparedStatement(dataSource, sql)) {
            Array array = dataSource.getConnection().createArrayOf("VARCHAR", topics.toArray());
            statement.setArray(1, array);
            return fetchTopicPartitions(statement.executeQuery());
        }
    }

}
