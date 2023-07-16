package com.clangenginner.book.web.rest

import com.clangenginner.book.IntegrationTest
import com.clangenginner.book.config.EmbeddedKafka
import com.clangenginner.book.config.KafkaSseConsumer
import com.clangenginner.book.config.KafkaSseProducer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.http.MediaType
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.GenericMessage
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.MimeTypeUtils
import java.time.Duration

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@EmbeddedKafka
class KhBookKafkaResourceIT {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    @Qualifier(KafkaSseProducer.CHANNELNAME)
    private lateinit var output: MessageChannel

    @Autowired
    @Qualifier(KafkaSseConsumer.CHANNELNAME)
    private lateinit var input: MessageChannel

    @Autowired
    private lateinit var collector: MessageCollector

    @Test
    @Throws(InterruptedException::class)
    fun producesMessages() {
        client.post().uri("/api/kh-book-kafka/publish?message=value-produce")
            .exchange()
            .expectStatus()
            .isNoContent

        val messages = collector.forChannel(output)
        val payload = messages.take() as (GenericMessage<String>)
        assertThat(payload.payload).isEqualTo("value-produce")
    }

    @Test
    fun consumesMessages() {
        val map = hashMapOf<String, Any>()
        map[MessageHeaders.CONTENT_TYPE] = MimeTypeUtils.TEXT_PLAIN_VALUE
        val headers = MessageHeaders(map)
        val testMessage = GenericMessage<String>("value-consume", headers)
        input.send(testMessage)
        val value = client
            .get()
            .uri("/api/kh-book-kafka/consume")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .returnResult(String::class.java)
            .responseBody
            .blockFirst(Duration.ofSeconds(10))
        assertThat(value).isEqualTo("value-consume")
    }
}
