package spring.boot.template.client.dispatcher;

import spring.boot.template.common.OperationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestPendingCenter {

    private Map<Long, OperationResultFuture> map = new ConcurrentHashMap<>();

    public void add(Long streamId,OperationResultFuture future) {
        System.out.println("===>client-put-"+ streamId);
        this.map.put(streamId, future);
    }

    public void set(Long streamId, OperationResult result) {
        OperationResultFuture future = this.map.get(streamId);
        if (future != null) {
            System.out.println("===>client-set-"+ streamId);
            future.setSuccess(result);
            //防止map内存溢出
            System.out.println("===>client-remove-"+ streamId);
            this.map.remove(streamId);
        }
    }



}
