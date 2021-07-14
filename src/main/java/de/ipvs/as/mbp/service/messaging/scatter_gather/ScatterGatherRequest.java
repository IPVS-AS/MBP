package de.ipvs.as.mbp.service.messaging.scatter_gather;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.config.RequestStageConfig;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Objects of this class represent scatter gather requests that were built by the {@link ScatterGatherRequestBuilder}
 * and can be executed synchronously at any time in order to retrieve and collect the reply messages of all recipients.
 * The {@link ScatterGatherRequest}s are able to deal with different types of reply messages (string, JSON or
 * {@link DomainMessage} objects) which are provided as type parameter during the request creation. Furthermore,
 * they can consist out of multiple request stages where each stage is described by one {@link RequestStageConfig}.
 * A scatter gather request terminates as soon as all request stages concluded successfully.
 *
 * @param <T> The desired type of the reply messages
 */
public class ScatterGatherRequest<T> {

    //Overall future that combines the execution of the individual futures
    private CompletableFuture<Void> overallFuture;

    //Individual futures of the request stages
    private Collection<CompletableFuture<List<T>>> individualFutures;

    /**
     * Creates a new scatter gather request from a given overall completable future that combines the various request
     * stages and a collection of individual futures which represent the request stages that were created from the
     * provided {@link RequestStageConfig}s and can be inspected after the execution of the request in order to
     * retrieve the reply messages.
     *
     * @param overallFuture     The overall completable future
     * @param individualFutures Collection of the individual completable futures
     */
    protected ScatterGatherRequest(CompletableFuture<Void> overallFuture, Collection<CompletableFuture<List<T>>> individualFutures) {
        //Set overall and individual futures
        setOverallFuture(overallFuture);
        setIndividualFutures(individualFutures);
    }

    /**
     * Executes the scatter gather request synchronously and returns a list of the received reply messages as result.
     *
     * @return The resulting list of reply messages
     */
    public List<T> execute() {
        //Sanity check for state
        if (this.wasExecuted()) {
            //Request has already been executed, thus just return the results again
            return mergeFutureResults();
        }

        //Try to execute the request
        try {
            //Block for results of completable future
            overallFuture.get();

            //Merge and return the results of the individual futures
            return mergeFutureResults();
        } catch (Exception e) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "Execution of scatter gather request failed.");
        }
    }

    /**
     * Cancels the execution of the request.
     */
    public void cancel() {
        this.overallFuture.cancel(true);
    }

    /**
     * Checks and returns whether the scatter gather request was already executed. This may be helpful to know, since
     * each scatter gather request can only be executed once.
     *
     * @return True, if the request was already executed; false otherwise
     */
    public boolean wasExecuted() {
        return overallFuture.isDone();
    }

    /**
     * Checks and returns whether the scatter gather request was cancelled during its execution.
     *
     * @return True, if the request was cancelled; false otherwise
     */
    public boolean wasCancelled() {
        return overallFuture.isCancelled();
    }

    /**
     * Returns the overall completable future that combines the various request stages of the scatter gather request.
     *
     * @return The overall completable future
     */
    protected CompletableFuture<Void> getOverallFuture() {
        return overallFuture;
    }

    /**
     * Sets the overall completable future that combines the various request stages of the scatter gather request.
     *
     * @param overallFuture The overall future to set
     */
    protected void setOverallFuture(CompletableFuture<Void> overallFuture) {
        //Sanity check
        if (overallFuture == null) {
            throw new IllegalArgumentException("The overall completable future must not be null.");
        }

        //Set field
        this.overallFuture = overallFuture;
    }

    /**
     * Returns the collection of individual futures which represent the request stages that were created from the
     * provided {@link RequestStageConfig}s and can be inspected after the execution of the request in order to
     * retrieve the reply messages.
     *
     * @return The collection of individual futures
     */
    protected Collection<CompletableFuture<List<T>>> getIndividualFutures() {
        return individualFutures;
    }

    /**
     * Sets the collection of individual futures which represent the request stages that were created from the
     * provided {@link RequestStageConfig}s and can be inspected after the execution of the request in order to
     * retrieve the reply messages.
     *
     * @param individualFutures The collection of individual futures to set
     */
    protected void setIndividualFutures(Collection<CompletableFuture<List<T>>> individualFutures) {
        //Sanity check
        if ((individualFutures == null) || (individualFutures.isEmpty())) {
            throw new IllegalArgumentException("The collection of individual completable futures must not be null or empty.");
        }

        this.individualFutures = individualFutures;
    }

    /**
     * After the request was executed, this method inspects the collection of individual futures in order to retrieve
     * the received reply messages and merges the results into a common list.
     *
     * @return The common result list of received replies
     */
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
