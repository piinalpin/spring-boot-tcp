package com.piinalpin.tcpclientsample.service;

import com.piinalpin.tcpclientsample.dto.MessageDTO;
import com.piinalpin.tcpclientsample.gateway.TCPClientGateway;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class MessageService {

    private final TCPClientGateway clientGateway;

    public MessageService(TCPClientGateway clientGateway) {
        this.clientGateway = clientGateway;
    }

    @SneakyThrows
    public MessageDTO send(String message) {
        MessageDTO messageRequest = MessageDTO.builder()
                .message(message)
                .sender("tcp-client")
                .timestamp(LocalDateTime.now().toString())
                .build();

        Jackson2JsonObjectMapper mapper = new Jackson2JsonObjectMapper();
        String messageRequestStr = mapper.toJson(messageRequest);

        log.info("Sending message: {}", messageRequestStr);
        byte[] responseByte = clientGateway.send(messageRequestStr.getBytes());
        MessageDTO response = mapper.fromJson(new String(responseByte), MessageDTO.class);
        log.info("Receive message: {}, from: {}, at: {}", response.getMessage(), response.getSender(), response.getTimestamp());
        return response;
    }

}
