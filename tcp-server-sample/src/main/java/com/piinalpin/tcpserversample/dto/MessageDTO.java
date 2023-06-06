package com.piinalpin.tcpserversample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2780348667790818215L;

    private String message;
    private String sender;
    private String timestamp;

}
