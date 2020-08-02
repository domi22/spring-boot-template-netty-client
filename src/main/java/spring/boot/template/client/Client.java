package spring.boot.template.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import spring.boot.template.client.coder.OrderFrameDecoder;
import spring.boot.template.client.coder.OrderFrameEncoder;
import spring.boot.template.client.coder.OrderprotocolDecoder;
import spring.boot.template.client.coder.OrderprotocolEncoder;
import spring.boot.template.common.RequestMessage;
import spring.boot.template.order.OrderOperation;
import spring.boot.template.util.IdUtil;

public class Client {


    public static void main(String[] args) throws Exception {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderprotocolEncoder());
                pipeline.addLast(new OrderprotocolDecoder());
                //日志打印
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture sync = bootstrap.connect("127.0.0.1",8050).sync();

        RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(), new OrderOperation(3, "stream-client"));
        ChannelFuture channelFuture = sync.channel().writeAndFlush(requestMessage);

        sync.channel().closeFuture().get();

    }
}
