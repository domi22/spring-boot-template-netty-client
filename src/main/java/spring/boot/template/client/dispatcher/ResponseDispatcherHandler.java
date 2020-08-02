package spring.boot.template.client.dispatcher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import spring.boot.template.common.OperationResult;
import spring.boot.template.common.ResponseMessage;


public class ResponseDispatcherHandler extends SimpleChannelInboundHandler<ResponseMessage> {


    private RequestPendingCenter requestPendingCenter;

    public ResponseDispatcherHandler() {
    }

    public ResponseDispatcherHandler(RequestPendingCenter requestPendingCenter) {
        this.requestPendingCenter = requestPendingCenter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseMessage responseMessage) throws Exception {
        System.out.println("====>client-handler-ResponseDispatcherHandler");
        long streamId = responseMessage.getMessageHeader().getStreamId();
        OperationResult messageBody = responseMessage.getMessageBody();
        requestPendingCenter.set(streamId,messageBody);
    }


}
