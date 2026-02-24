//package org.example.shopdemo.rabbitmq;
//
//import ch.qos.logback.classic.pattern.MessageConverter;
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.FanoutExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//@Component
//public class rabbitmq {
//    // 交换机（Exchange）是 RabbitMQ 中的核心组件之一，负责接收生产者发送的消息，
//    // 并根据特定的路由规则将消息分发到一个或多个队列中。
//    // 常见的交换机类型包括：
//    // 1. Direct Exchange（直连交换机）：根据 routing key 精确匹配队列。
//    // 2. Fanout Exchange（扇出交换机）：将消息广播到所有绑定的队列，忽略 routing key。
//    // 3. Topic Exchange（主题交换机）：根据通配符模式匹配 routing key，实现灵活的消息路由。
//    // 4. Headers Exchange（头交换机）：根据消息头部属性进行匹配（较少使用）。
//    // 交换机的作用是解耦生产者和消费者，使消息的路由更加灵活和高效。
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    String message="Hello World";
//    String queueName="hello";
//    // 演示点对点消息队列：发送消息到指定队列
//    public void send() {
//        rabbitTemplate.convertAndSend(queueName, message);
//        System.out.println("Sent message: " + message + " to queue: " + queueName);
//    }
//
//    // 演示点对点消息队列：接收并处理队列中的消息
//    @RabbitListener(queues = "hello")
//    public void receive(String receivedMessage) {
//        System.out.println("Received message: " + receivedMessage);
//    }
//
//    // 演示发布/订阅模式：向交换机发送消息，多个消费者可以监听同一个交换机
//    public void sendToExchange(String exchangeName, String routingKey, String message) {
//        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
//        System.out.println("Sent message to exchange: " + exchangeName +
//                          " with routing key: " + routingKey +
//                          ", message: " + message);
//    }
//
//    // 演示基于用户ID的路由：向特定用户发送消息
//    public void sendToUser(String userId, String message) {
//        String routingKey = "user." + userId;
//        rabbitTemplate.convertAndSend( routingKey, message);
//        System.out.println("Sent message to user: " + userId + ", content: " + message);
//    }
//
//    // 演示广播模式：向所有绑定到fanout交换机的队列发送消息
//    public void broadcastMessage(String message) {
//        rabbitTemplate.convertAndSend("fanoutExchange", "", message);
//        System.out.println("Broadcast message: " + message);
//    }
//
//    // 演示主题路由：根据主题模式发送消息
//    public void sendByTopic(String topic, String message) {
//        rabbitTemplate.convertAndSend("topicExchange", topic, message);
//        System.out.println("Sent message with topic: " + topic + ", content: " + message);
//    }
//
//    // 演示RPC模式：发送请求并等待响应
//    public String sendRpcRequest(String requestMessage) {
//        String response = (String) rabbitTemplate.convertSendAndReceive("rpcExchange", "rpc.queue", requestMessage);
//        System.out.println("RPC request sent: " + requestMessage + ", received response: " + response);
//        return response;
//    }
//
//
//    // 演示消费者接收消息：监听特定队列并处理消息
//    @RabbitListener(queues = "consumerQueue")
//    public void consumeMessage(String message) {
//        System.out.println("Consumer received message: " + message);
//        // 在这里可以添加具体的业务逻辑处理接收到的消息
//        processMessage(message);
//    }
//
//    // 处理接收到的消息的业务逻辑
//    private void processMessage(String message) {
//        // 示例：打印消息内容并模拟处理
//        System.out.println("Processing message: " + message);
//        // 可以在这里添加更复杂的业务逻辑，如数据库操作、调用其他服务等
//    }
//
//    //声明一个交换机
//    @Bean
//    public FanoutExchange fanoutExchange() {
//        return new FanoutExchange("fanoutExchange");
//    }
//    //声明一个队列
//    @Bean
//    public Queue consumerQueue() {
//        return new Queue("consumerQueue");
//    }
//    //将交换机和队列进行绑定
//    @Bean
//    public Binding binding(Queue consumerQueue, FanoutExchange fanoutExchange) {
//        return BindingBuilder.bind(consumerQueue).to(fanoutExchange);
//    }
//    @Bean
//    public Jackson2JsonMessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//}
