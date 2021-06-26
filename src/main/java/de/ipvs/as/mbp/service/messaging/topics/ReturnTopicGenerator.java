package de.ipvs.as.mbp.service.messaging.topics;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * This component is responsible for generating return topics that can be transmitted within request messages
 * in order to indicate to the receiver under which topic the publish of corresponding reply messages is expected.
 * The topics of this component are generated in a consistent manner, for a specific user and with unique identifiers.
 * The ID of the user is included in the prefix of the topic in order to  ease the management of topic restrictions
 * and permission on the messaging broker.
 */
@Component
public class ReturnTopicGenerator {
    //Pattern for return topics
    private static final String PATTERN_RETURN_TOPIC = "r/{user-id}/{category}/{corr-id}";

    //User service to use
    private final UserService userService;

    /**
     * Creates and initializes the topic generator component from a given user service that allows to access
     * information about the users of the MBP.
     *
     * @param userService THe user service to use for accessing user information.
     */
    @Autowired
    public ReturnTopicGenerator(UserService userService) {
        //Set user service
        this.userService = userService;
    }

    /**
     * Creates a return topic from a given category name.
     *
     * @param category The category name to use
     * @return The resulting return topic
     */
    public String createReturnTopic(String category) {
        //Get current user
        User user = userService.getLoggedInUser();

        //Create return topic
        return createReturnTopic(user, category, generateCorrelationId());
    }

    /**
     * Creates a return topic from a given {@link User} and category name.
     *
     * @param user     The user to use
     * @param category The category name to use
     * @return The resulting return topic
     */
    public String createReturnTopic(User user, String category) {
        //Create return topic
        return createReturnTopic(user, category, generateCorrelationId());
    }

    /**
     * Creates a return topic from a given category name and correlation identifier.
     *
     * @param category      The category name to use
     * @param correlationId The correlation identifier to use
     * @return The resulting return topic
     */
    public String createReturnTopic(String category, String correlationId) {
        //Get current user
        User user = userService.getLoggedInUser();

        //Create return topic
        return createReturnTopic(user, category, correlationId);
    }

    /**
     * Creates a return topic from a given {@link User}, category name and correlation identifier.
     *
     * @param user          The user to use
     * @param category      The category name to use
     * @param correlationId The correlation identifier to use
     * @return The resulting return topic
     */
    public String createReturnTopic(User user, String category, String correlationId) {
        //Sanity checks for parameters
        if ((user == null) || (user.getId() == null) || (user.getId().isEmpty())) {
            throw new IllegalArgumentException("The user ID must not be null or empty.");
        } else if ((category == null) || (category.isEmpty())) {
            throw new IllegalArgumentException("The category must not be null or empty.");
        } else if ((correlationId == null) || (correlationId.isEmpty())) {
            throw new IllegalArgumentException("The correlation identifier must not be null or empty.");
        }

        //Fill in the pattern
        return PATTERN_RETURN_TOPIC
                .replaceAll("\\{user-id}", user.getId())
                .replaceAll("\\{category}", category)
                .replaceAll("\\{corr-id}", correlationId);
    }

    /**
     * Creates a new, random and unique correlation identifier.
     *
     * @return The resulting correlation identifier
     */
    private String generateCorrelationId() {
        //Generate UUID as correlation identifier
        return UUID.randomUUID().toString().replace("-", "");
    }
}
