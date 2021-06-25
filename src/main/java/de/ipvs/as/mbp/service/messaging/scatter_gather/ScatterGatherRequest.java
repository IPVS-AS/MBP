package de.ipvs.as.mbp.service.messaging.scatter_gather;

import de.ipvs.as.mbp.error.MBPException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScatterGatherRequest<T> {

    private CompletableFuture<Void> overallFuture;
    private Collection<CompletableFuture<List<T>>> individualFutures;

    protected ScatterGatherRequest(CompletableFuture<Void> overallFuture, Collection<CompletableFuture<List<T>>> individualFutures) {
        //Set overall and individual futures
        setOverallFuture(overallFuture);
        setIndividualFutures(individualFutures);
    }

    public List<T> execute() {
        //Sanity check for state
        if (this.wasExecuted()) {
            //Request has already been executed, thus just return the results again
            return mergeFutureResults();
        }

        //Try to execute the request
        try {
            //Execute completable future
            overallFuture.get();

            //Merge and return the results of the individual futures
            return mergeFutureResults();
        } catch (Exception e) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Execution of scatter gather request failed.");
        }
    }

    public boolean wasExecuted() {
        return overallFuture.isDone();
    }

    protected CompletableFuture<Void> getOverallFuture() {
        return overallFuture;
    }

    protected ScatterGatherRequest<T> setOverallFuture(CompletableFuture<Void> overallFuture) {
        //Sanity check
        if (overallFuture == null) {
            throw new IllegalArgumentException("The overall completable future must not be null.");
        }

        this.overallFuture = overallFuture;
        return this;
    }

    protected Collection<CompletableFuture<List<T>>> getIndividualFutures() {
        return individualFutures;
    }

    protected ScatterGatherRequest<T> setIndividualFutures(Collection<CompletableFuture<List<T>>> individualFutures) {
        //Sanity check
        if ((individualFutures == null) || (individualFutures.isEmpty())) {
            throw new IllegalArgumentException("The collection of individual completable futures must not be null or empty.");
        }

        this.individualFutures = individualFutures;
        return this;
    }

    private List<T> mergeFutureResults() {
        //Sanity check for state
        if (!this.wasExecuted()) {
            throw new IllegalStateException("The request must be executed first before the results can be accessed.");
        }

        //Create new result list
        List<T> resultList = new ArrayList<>();

        //Retrieve the results of all futures and add them to the list
        this.individualFutures.forEach(f -> {
            try {
                resultList.addAll(f.get());
            } catch (Exception ignored) {
            }
        });

        //Return result list
        return resultList;
    }
}
