package spring.boot.template.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import spring.boot.template.client.coder.OrderFrameDecoder;
import spring.boot.template.client.coder.OrderFrameEncoder;
import spring.boot.template.client.coder.OrderprotocolDecoder;
import spring.boot.template.client.coder.OrderprotocolEncoder;
import spring.boot.template.client.dispatcher.OperationResultFuture;
import spring.boot.template.client.dispatcher.RequestPendingCenter;
import spring.boot.template.client.dispatcher.ResponseDispatcherHandler;
import spring.boot.template.common.OperationResult;
import spring.boot.template.common.RequestMessage;
import spring.boot.template.order.OrderOperation;
import spring.boot.template.util.IdUtil;

import javax.xml.bind.SchemaOutputResolver;

public class ClientV2 {


    public static void main(String[] args) throws Exception {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());

        //超时调整
        bootstrap.option(NioChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000);
        RequestPendingCenter requestPendingCenter = new RequestPendingCenter();
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderprotocolEncoder());

                pipeline.addLast(new OrderprotocolDecoder());
                //响应分发处理
                pipeline.addLast(new ResponseDispatcherHandler(requestPendingCenter));
                //日志打印
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture sync = bootstrap.connect("127.0.0.1",8050).sync();

        long streamId = IdUtil.nextId();
        OperationResultFuture operationResultFuture = new OperationResultFuture();
        RequestMessage requestMessage = new RequestMessage(streamId, new OrderOperation(3, "stream-client"));
        ChannelFuture channelFuture = sync.channel().writeAndFlush(requestMessage);
        requestPendingCenter.add(streamId,operationResultFuture);

        //进过handler的处理并设值之后，我们在这里阻塞等待
        OperationResult operationResult = operationResultFuture.get();
        System.out.println("====>获取到了结果：" + operationResult);


        sync.channel().closeFuture().get();

    }
}
