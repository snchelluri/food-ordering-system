package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.Serializable;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topic, K key, V message, ListenableFutureCallback<SendResult<K, V>> callback) {
        log.info("Sending message={} to the topic={}", message, topic);
        try {
            ListenableFuture<SendResult<K, V>> kafkaFutureResult =
                    (ListenableFuture<SendResult<K, V>>) kafkaTemplate.send(topic, key, message);
            kafkaFutureResult.addCallback(callback);
        } catch (KafkaException e) {
            log.error("Error on kafka producer with key: {}, message: {} and exception: {} ", key,
                    message, e.getMessage());
            throw new KafkaProducerException("Error on kafka producer with key:" + key + "and message: " + message);
        }
    }

    @PreDestroy
    public void close(){
        if(kafkaTemplate != null){
            log.info("Closing kafka producer");
            kafkaTemplate.destroy();
        }
    }

}
