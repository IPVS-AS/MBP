package org.citopt.connde.service.stats;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.repository.projection.ValueLogProjection;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jan
 */
@Service
public class ValueLogStatsService {

    @Autowired
    private ValueLogRepository valueLogRepository;

    public ValueLogStats calculateValueLogStats(Component component) {
        ValueLogStats stats = new ValueLogStats();

        String componentId = component.getId();
        List<ValueLogProjection> valueLogList = valueLogRepository.findProjectionByIdref(componentId);

        if (valueLogList.isEmpty()) {
            return stats;
        }

        stats.setNumberLogs(valueLogList.size());
        stats.setFirstLog(valueLogList.get(0));
        stats.setLastLog(valueLogList.get(valueLogList.size() - 1));

        ValueLogProjection minLog = valueLogList.get(0);
        ValueLogProjection maxLog = valueLogList.get(0);

        float averageAccumulator = 0;

        for (ValueLogProjection log : valueLogList) {
            float logValue = Float.parseFloat(log.getValue());

            if (logValue < Float.parseFloat(minLog.getValue())) {
                minLog = log;
            }
            if (logValue > Float.parseFloat(maxLog.getValue())) {
                maxLog = log;
            }

            averageAccumulator += logValue;
        }

        stats.setMinimumLog(minLog);
        stats.setMaximumLog(maxLog);

        float average = averageAccumulator / ((float) valueLogList.size());
        stats.setAverage(average);

        float varianceAccumulator = 0;

        for (ValueLogProjection log : valueLogList) {
            float logValue = Float.parseFloat(log.getValue());

            varianceAccumulator += Math.pow(logValue - average, 2);
        }

        float variance = varianceAccumulator / ((float) valueLogList.size());
        stats.setVariance(variance);

        float standardDeviation = (float) Math.sqrt(variance);
        stats.setStandardDeviation(standardDeviation);

        return stats;
    }
}
