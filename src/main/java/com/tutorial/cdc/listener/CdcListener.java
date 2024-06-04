package com.tutorial.cdc.listener;

import com.tutorial.cdc.elasticsearch.service.StudentService;
import io.debezium.config.Configuration;
import io.debezium.data.Envelope.Operation;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.debezium.data.Envelope.FieldName;
import static java.util.stream.Collectors.toMap;


/**
 * This class creates, starts and stops the EmbeddedEngine, which starts the Debezium engine. The engine also
 * loads and launches the connectors setup in the configuration.
 * <p>
 * The class uses @PostConstruct and @PreDestroy functions to perform needed operations.
 *
 * @author Sohan
 */
@Slf4j
@Service
public class CdcListener {

    /**
     * Single thread pool which will run the Debezium engine asynchronously.
     */
    private final Executor executor;

    /**
     * The Debezium engine which needs to be loaded with the configurations, Started and Stopped - for the
     * CDC to work.
     */
//    private final EmbeddedEngine engine;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> engine;

    /**
     * Handle to the Service layer, which interacts with ElasticSearch.
     */
    private final StudentService studentService;

    /**
     * Constructor which loads the configurations and sets a callback method 'handleEvent', which is invoked when
     * a DataBase transactional operation is performed.
     *
     * @param studentConnector: Student Connector
     * @param studentService:   Student Service
     */
    private CdcListener(Configuration studentConnector, StudentService studentService) {
        this.executor = Executors.newSingleThreadExecutor();

        this.engine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(studentConnector.asProperties())
                .notifying(this::handleChangeEvent)
                .build();

        this.studentService = studentService;
    }

    /**
     * The method is called after the Debezium engine is initialized and started asynchronously using the Executor.
     */
    @PostConstruct
    private void start() {
        this.executor.execute(engine);
    }

    /**
     * This method is called when the container is being destroyed. This stops the debezium, merging the Executor.
     */
    @PreDestroy
    private void stop() throws IOException {
        if (this.engine != null) {
            this.engine.close();
        }
    }

    /**
     * This method is invoked when a transactional action is performed on any of the tables that were configured.
     *
     * @param sourceRecordRecordChangeEvent: Source Record
     */
    private void handleChangeEvent(
            RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordValue = (Struct) sourceRecord.value();

        if (sourceRecordValue != null) {
            String oprStr = (String) sourceRecordValue.get(FieldName.OPERATION);
            Operation operation = Operation.forCode(oprStr);

            //Only if this is a transactional operation.
            // c:CREATE, r:READ, u:UPDATE, d:DELETE
            if ("crud".contains(operation.code())) {
                String record = operation == Operation.DELETE ? FieldName.BEFORE : FieldName.AFTER;
                Struct struct = (Struct) sourceRecordValue.get(record);

                Map<String, Object> message = struct.schema().fields().stream()
                        .map(Field::name)
                        .filter(fieldName -> struct.get(fieldName) != null)
                        .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                        .collect(toMap(Pair::getKey, Pair::getValue));

                //Call the service to handle the data change.
                this.studentService.maintainReadModel(message, operation);
                log.info("Data Changed: {} with Operation: {}", message, operation.name());

            }
        }
    }
}