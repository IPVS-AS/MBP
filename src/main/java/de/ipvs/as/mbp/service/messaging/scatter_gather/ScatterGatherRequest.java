package de.ipvs.as.mbp.service.messaging.scatter_gather;

import de.ipvs.as.mbp.error.MBPException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScatterGatherRequest<T> {

    private CompletableFuture<List<T>> future;

    protected ScatterGatherRequest(CompletableFuture<List<T>> future) {
        //Set future
        setFuture(future);
    }

    public List<T> execute() {
        try {
            //Execute completable future synchronously
            return future.get();
        } catch (Exception e) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Execution of scatter gather request failed.");
        }
    }

    public boolean wasExecuted() {
        return future.isDone();
    }

    protected CompletableFuture<List<T>> getFuture() {
        return this.future;
    }

    protected ScatterGatherRequest<T> setFuture(CompletableFuture<List<T>> future) {
        //Sanity check
        if (future == null) {
            throw new IllegalArgumentException("Completable future must not be null.");
        }

        //Set future
        this.future = future;
        return this;
    }
}
