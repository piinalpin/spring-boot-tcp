package com.piinalpin.tcpserversample.service;

import com.piinalpin.tcpserversample.dto.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MessageService {

    public String process(byte[] message) throws IOException {
        String messageJson = new String(message);
        log.info("Receive message as JSON: {}", messageJson);

        Jackson2JsonObjectMapper mapper = new Jackson2JsonObjectMapper();
        MessageDTO messageDTO = mapper.fromJson(messageJson, MessageDTO.class);
        log.info("Message: {}, from: {}, at: {}", messageDTO.getMessage(), messageDTO.getSender(), messageDTO.getTimestamp());

        MessageDTO response = MessageDTO.builder()
                .message("Hello this message from TCP server!")
                .timestamp(LocalDateTime.now().toString())
                .sender("tcp-server")
                .build();
        return mapper.toJson(response);
    }

}
