package rocks.crimp.crimp.service;

import android.content.Context;

import com.squareup.tape.InMemoryObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RestRequestTaskQueue extends TaskQueue<RestRequestTask> {
    private RestRequestTaskQueue(ObjectQueue<RestRequestTask> delegate) {
        super(delegate);
    }

    public static RestRequestTaskQueue create() {
        InMemoryObjectQueue<RestRequestTask> delegate;
        delegate = new InMemoryObjectQueue<>();
        return new RestRequestTaskQueue(delegate);
    }
}