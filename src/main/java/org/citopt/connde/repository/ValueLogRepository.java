package org.citopt.connde.repository;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.citopt.connde.MongoConfiguration;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Represents a repository for persisting and querying value logs, powered by the MongoDB database. Since value
 * logs are time series data, a special way of storing the values is needed in order to preserve efficiency.
 * The approach implemented within this repository is described and recommended by the MongoDB blog post
 * "Time Series Data and MongoDB".
 * (https://www.mongodb.com/blog/post/time-series-data-and-mongodb-part-1-introduction)
 */
@Component
public class ValueLogRepository {
    //Name of the database to use for the value logs
    private static final String DATABASE_NAME = MongoConfiguration.DB_NAME;

    //Name of the collection to use for the value logs
    private static final String COLLECTION_NAME = "mongoValueLogs";

    //MongoDB bean to use
    private MongoClient mongoClient;

    //Value log database and collection of the MongoDB
    private MongoDatabase valueLogDatabase;
    private MongoCollection<ValueLog> valueLogCollection;

    /**
     * Instantiates the repository by passing a reference to the MongoDB bean that is supposed to be used (auto-wired).
     *
     * @param mongoClient The MongoDB bean to use
     */
    @Autowired
    private ValueLogRepository(MongoClient mongoClient) {
        //Store reference to MongoDB bean
        this.mongoClient = mongoClient;

        //Fetch coded registry for mapping value log objects from and to BSON documents
        CodecRegistry codecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        //Get value log database and collection with codec registry
        this.valueLogDatabase = mongoClient.getDatabase(DATABASE_NAME).withCodecRegistry(codecRegistry);
        this.valueLogCollection = valueLogDatabase.getCollection(COLLECTION_NAME, ValueLog.class);
    }

    /**
     * Writes a given value log object into the repository.
     *
     * @param valueLog The value log to write
     */
    public void write(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        //Get time from value log
        ZonedDateTime valueLogTime = valueLog.getTime().atZone(ZoneId.systemDefault());

        //Calculate absolute day from value log
        int absoluteDay = valueLogTime.getYear() * 366 + valueLogTime.getDayOfYear();

        //Get epoch seconds from value log
        long epochSeconds = valueLog.getTime().getEpochSecond();

        Document filterDocument = new Document("idref", valueLog.getIdref());
        filterDocument.append("nvalues", new Document("$lt", 80));
        filterDocument.append("day", absoluteDay);

        Document updateDocument = new Document("$push", new Document("values", valueLog));
        updateDocument.append("$min", new Document("first", epochSeconds));
        updateDocument.append("$max", new Document("last", epochSeconds));
        updateDocument.append("$inc", new Document("nvalues", 1));

        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.upsert(true);

        this.valueLogCollection.updateOne(filterDocument, updateDocument, updateOptions);
    }

    /**
     * Finds and returns a list of value logs that match a certain id reference of a component.
     *
     * @param idref The idref to match
     * @return The requested list of value logs
     */
    public List<ValueLog> findAllByIdRef(String idref) {
        //Sanity check
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        /*
        [{$match: {
  "idref": "5f1ddc446d51821c984a6c96"
}}, {$group: {
  _id: "$idref",
  "values": {$addToSet: "$values"}
}}, {$project: {
  "values": {  "$reduce":
  {
        "input": "$values",
        "initialValue": [],
        "in": { "$setUnion": [ "$$value", "$$this" ] }
      }}
}}, {$unwind: {
  path: "$values"
}}, {$replaceRoot: {
  newRoot: "$values"
}}]
//TODO Project and group not needed probably
         */


        Document matchStage = new Document("$match", new Document("idref", idref));
        Document unwindStage = new Document("$unwind", new Document("path", "$values"));
        Document replaceRootStage = new Document("$replaceRoot", new Document("newRoot", "$values"));
        
        AggregateIterable<ValueLog> result = this.valueLogCollection.aggregate(Arrays.asList(matchStage, unwindStage, replaceRootStage), ValueLog.class);
        for(ValueLog log : result){
            System.out.println(log);
        }

        return new ArrayList<>();
    }

    /**
     * Finds and returns a page of value logs that match a certain id reference of a component.
     *
     * @param idref    The idref to match
     * @param pageable The pageable describing the desired page of value logs
     * @return The requested page of value logs
     */
    public Page<ValueLog> findAllByIdRef(String idref, Pageable pageable) {
        //Sanity check
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        //Get limit and offset from pageable
        int limit = pageable.getPageSize();
        int offset = pageable.getOffset();

        //Get desired sort option from pageable
        Sort sort = pageable.getSort();

        //Iterate over all specified sort properties
        for (Sort.Order order : sort) {
            String property = order.getProperty();

            //Only sorting for time property is supported, thus ignore the other ones
            if (!property.equals("time")) {
                continue;
            }

            //Extend query for ordering with the chosen direction
            if (order.isAscending()) {
                //TODO
            } else {
                //TODO
            }

            //Only ordering for time is supported, so no need to consider other properties
            break;
        }

        //Add limit and offset if meaningful
        if ((offset > 0) && (limit > 0)) {
            //TODO
        } else if (limit > 0) {
            //TODO
        }

        //Add where clause in order to filter for idref
        //Query query = selectQuery.where("idref='" + idref + "'");

        //Execute query
        List<ValueLog> valueLogs = new ArrayList<>();

        //Return value logs as page
        return new PageImpl<>(valueLogs, pageable, valueLogs.size());
    }

    public void deleteByIdRef(String idref) {
        //TODO Does not work
        //Sanity check
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        //TODO
    }
}
