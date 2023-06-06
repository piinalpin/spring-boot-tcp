# Spring Boot TCP Integration
**Transmission Control Protocol (TCP)** is a communications standard that enables application programs and computing devices to exchange messages over a network. It is designed to send packets across the internet and ensure the successful delivery of data and messages over networks.

TCP is one of the basic standards that define the rules of the internet and is included within the standards defined by the Internet Engineering Task Force (IETF). It is one of the most commonly used protocols within digital network communications and ensures end-to-end data delivery.

TCP organizes data so that it can be transmitted between a server and a client. It guarantees the integrity of the data being communicated over a network. Before it transmits data, TCP establishes a connection between a source and its destination, which it ensures remains live until communication begins. It then breaks large amounts of data into smaller packets, while ensuring data integrity is in place throughout the process.

As a result, high-level protocols that need to transmit data all use TCP Protocol.  Examples include peer-to-peer sharing methods like File Transfer Protocol (FTP), Secure Shell (SSH), and Telnet. It is also used to send and receive email through Internet Message Access Protocol (IMAP), Post Office Protocol (POP), and Simple Mail Transfer Protocol (SMTP), and for web access through the Hypertext Transfer Protocol (HTTP).

## How TCP works?
![TCP Works](images/tcp-works.png)

- The server is in a **“passive open”** state: Passive open is a network communication setting where a server process is waiting to establish a connection with a client. It is “listening” for a connection without establishing it.
- The client must initiate an **“active open:”** Once the server is in a “passive open” state, the client must establish a connection by sending a TCP synchronization or TCP SYN message. The server then expends resources to accept the connection.
- A reliable connection is established through a three-way handshake: The three-way handshake is one of the central features of TCP. It makes sure that the connection is set up securely and reliably by ensuring it follows three steps:
- **SYN**: The client sends a synchronization message to the server, essentially a unique numerical value.
- **SYN-ACK**: The server sends back a synchronization acknowledgment (or SYN-ACK) message, which comprises two parts – the SYN value +1 and an ACK message, which is also a numerical value. The client receives the SYN-ACK.
- **ACK**: The client responds with an acknowledgment of its own, which is the ACK value + 1. This step in the three-way handshake establishes the client-to-server connection. Applications hosted on the client can now communicate with server-hosted applications by leveraging the connection. 
- **Timeout**: Timeout is the maximum interval allowed to pass between the data origination and receipt. It ensures that the connection does not remain open too long and minimizes exposure to online threats and bad actors. 
- **Acknowledgment**: The server and the client exchange ACK values for data transmission validation. If a data stream is not acknowledged, then the protocol tries retransmission. Also, if three consecutive ACK values are the same, then TCP initiates retransmission.

These rules make TCP among the most reliable communication protocols available across local area networks (LAN) and wide area networks (WAN). However, it does have a few vulnerabilities. The three-way handshake (SYN, SYN-ACK, and ACK) process takes time, and there is a stipulated interval allotted for a timeout. As a result, TCP connections have a greater latency and may slow down data transmission when heavy packets need to be delivered.

## Spring Integration IP

Spring Integration provides channel adapters for receiving and sending messages over internet protocols. Both UDP (User Datagram Protocol) and TCP (Transmission Control Protocol) adapters are provided. Each adapter provides for one-way communication over the underlying protocol. In addition, Spring Integration provides simple inbound and outbound TCP gateways. These are used when two-way communication is needed.

Two flavors each of UDP inbound and outbound channel adapters are provided:

- `UnicastSendingMessageHandler` sends a datagram packet to a single destination.
- `UnicastReceivingChannelAdapter` receives incoming datagram packets.
- `MulticastSendingMessageHandler` sends (broadcasts) datagram packets to a multicast address.
- `MulticastReceivingChannelAdapter` receives incoming datagram packets by joining to a multicast address.

TCP inbound and outbound channel adapters are provided:
- `TcpSendingMessageHandler` sends messages over TCP.
- `TcpReceivingChannelAdapter` receives messages over TCP.

**Prerequisities**
- JDK 17 or later
- Spring Boot 3.1.x or later

## Implement the TCP Server
Before we implement the client, we need a TCP server. TCP server can be created using any language that supports TCP, but we will create it with Spring Boot as well. The server will listen the port `10001` on `localhost`, and will send message to the client.

**Dependency**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.integration</groupId>
  <artifactId>spring-integration-ip</artifactId>
</dependency>
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <optional>true</optional>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-core</artifactId>
  <version>2.14.2</version>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.14.2</version>
</dependency>
```

Add the port listen `tcp.server.port=10001` on `application.properties`

**Configuration** <br>
Create Bean Configuration called `TCPServerConfiguration`

```java
@Configuration
public class TCPServerConfiguration {

    @Value("${tcp.server.port}")
    private int port;

    public static final String TCP_DEFAULT_CHANNEL = "tcp-channel-sample";

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNioServerConnectionFactory tcpNioServerConnectionFactory = new TcpNioServerConnectionFactory(port);
        tcpNioServerConnectionFactory.setUsingDirectBuffers(true);
        return tcpNioServerConnectionFactory;
    }

    @Bean(name = TCP_DEFAULT_CHANNEL)
    public MessageChannel messageChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpInboundGateway tcpInboundGateway(AbstractServerConnectionFactory serverConnectionFactory,
                                               @Qualifier(value = "tcp-channel-sample") MessageChannel messageChannel) {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(serverConnectionFactory);
        gateway.setRequestChannel(messageChannel);
        return gateway;
    }

}
```

**Data Transfer Object (DTO)** <br>
Add the DTO `MessageDTO` to send response to client.
```java
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
```

**Service** <br>
A service class to process message from client and generate response which is will be send to client.

```java
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
```

**TCP Endpoint** <br>
FInally, we need to create Message Endpoint which will be register to the input channel on `ServiceActivator`

```java
@Slf4j
@MessageEndpoint
public class TCPMessageEndpoint {

    private final MessageService messageService;

    public TCPMessageEndpoint(MessageService messageService) {
        this.messageService = messageService;
    }

    @ServiceActivator(inputChannel = TCPServerConfiguration.TCP_DEFAULT_CHANNEL)
    public byte[] process(byte[] message) throws IOException {
        String response = messageService.process(message);
        log.info("Send message to client");
        return response.getBytes();
    }

}
```

## Implement the TCP Client
After we have TCP server, we can create our TCP Client using Spring Boot that listen the port of TCP server. We will create web service REST to try the client. 

**Dependency**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.integration</groupId>
  <artifactId>spring-integration-ip</artifactId>
</dependency>

<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <optional>true</optional>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.14.2</version>
</dependency>
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-core</artifactId>
  <version>2.14.2</version>
</dependency>
```

**Properties** <br>
Update the `application.properties` like below.

```bash
spring.application.name=tcp-client-sample
server.port=8080
server.servlet.context-path=/api
server.error.include-message=always

tcp.server.host=localhost
tcp.server.port=10001
tcp.client.connection.poolSize=50
```

**Configuration** <br>
Create Bean Configuration called `TCPClientConfiguration`

```java
@Configuration
public class TCPClientConfiguration {

    @Value("${tcp.server.host}")
    private String host;

    @Value("${tcp.server.port}")
    private int port;

    @Value("${tcp.client.connection.poolSize}")
    private int connectionPoolSize;

    public static final String TCP_DEFAULT_CHANNEL = "tcp-channel-sample";

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        TcpNioClientConnectionFactory tcpNioClientConnectionFactory = new TcpNioClientConnectionFactory(host, port);
        tcpNioClientConnectionFactory.setUsingDirectBuffers(true);
        return new CachingClientConnectionFactory(tcpNioClientConnectionFactory, connectionPoolSize);
    }

    @Bean(name = TCP_DEFAULT_CHANNEL)
    public MessageChannel messageChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = TCP_DEFAULT_CHANNEL)
    public MessageHandler messageHandler(AbstractClientConnectionFactory clientConnectionFactory) {
        TcpOutboundGateway tcpOutboundGateway = new TcpOutboundGateway();
        tcpOutboundGateway.setConnectionFactory(clientConnectionFactory);
        return tcpOutboundGateway;
    }

}
```

**Data Transfer Object (DTO)** <br>
Add the DTO `MessageDTO` like DTO on our tcp server to send message to server.
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 497439089321548030L;

    private String message;
    private String sender;
    private String timestamp;

}
```

**The Gateway** <br>
Create a TCP outbound gateway as gateway spring integration ip to the server.

```java
@Component
@MessagingGateway(defaultRequestChannel = TCPClientConfiguration.TCP_DEFAULT_CHANNEL)
public interface TCPClientGateway {

    byte[] send(byte[] message);

}
```

**Service** <br>
A service class to send message to server and mapping response our from TCP server.

```java
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
```

**Controller** <br>
The controller as REST controller that we used to receive request from API.

```java
@RestController
@RequestMapping(value = "")
public class TcpClientController {

    private final MessageService messageService;

    public TcpClientController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping(value = "/send")
    public MessageDTO send(@RequestParam("message") String message) {
        return messageService.send(message);
    }

}
```

**Test the integration** <br>

Test using `cURL` or you can test using browser or postman

```bash
curl --location 'http://localhost:8080/api/send?message=Hello%20there'
```

**Log the client**
```log
2023-06-05T15:30:22.227+07:00  INFO 21852 --- [nio-8080-exec-1] c.p.t.service.MessageService             : Sending message: {"message":"Hello there","sender":"tcp-client","timestamp":"2023-06-05T15:30:22.198153100"}
2023-06-05T15:30:22.240+07:00  WARN 21852 --- [nio-8080-exec-1] o.s.i.i.tcp.connection.TcpNioConnection  : No publisher available to publish TcpConnectionOpenEvent [source=TcpNioConnection:127.0.0.1:10001:57665:73fbea17-47c5-4c94-b3cb-ce75b94c6402], [factory=unknown, connectionId=127.0.0.1:10001:57665:73fbea17-47c5-4c94-b3cb-ce75b94c6402] **OPENED**
2023-06-05T15:30:22.314+07:00  INFO 21852 --- [nio-8080-exec-1] c.p.t.service.MessageService             : Receive message: Hello this message from TCP server!, from: tcp-server, at: 2023-06-05T15:30:22.286923800
```

**Log the server**
```log
2023-06-05T15:30:22.261+07:00  INFO 20196 --- [pool-2-thread-3] c.p.t.service.MessageService             : Receive message as JSON: {"message":"Hello there","sender":"tcp-client","timestamp":"2023-06-05T15:30:22.198153100"}
2023-06-05T15:30:22.286+07:00  INFO 20196 --- [pool-2-thread-3] c.p.t.service.MessageService             : Message: Hello there, from: tcp-client, at: 2023-06-05T15:30:22.198153100
2023-06-05T15:30:22.296+07:00  INFO 20196 --- [pool-2-thread-3] c.p.t.endpoint.TCPMessageEndpoint        : Send message to client
```

## References
- [What is Transmission Control Protocol TCP/IP?](https://www.fortinet.com/resources/cyberglossary/tcp-ip)
- [What Is TCP (Transmission Control Protocol)?](https://www.spiceworks.com/tech/networking/articles/what-is-tcp/)
- [Spring TCP and UDP Support](https://docs.spring.io/spring-integration/reference/html/ip.html)
- [Spring Boot TCP Client Sample](https://github.com/zhwxp/spring-boot-tcp-client-sample)
- [Spring Boot TCP Server Sample](https://github.com/zhwxp/spring-boot-tcp-server-sample)