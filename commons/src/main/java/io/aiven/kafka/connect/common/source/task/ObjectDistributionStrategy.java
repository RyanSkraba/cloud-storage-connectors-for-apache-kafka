/*
 * Copyright 2024 Aiven Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.aiven.kafka.connect.common.source.task;

/**
 * SourceObjectDistributionStrategy is a common interface which allows source connectors to determine what method of
 * distributing tasks amongst source connectors in distributed mode.
 */
public interface ObjectDistributionStrategy {

    boolean isPartOfTask(int taskId, String valueToBeEvaluated);

    /**
     * When a connector receives a reconfigure event this method should be called to ensure that the distribution
     * strategy is updated correctly.
     *
     * @param maxTasks
     *            The maximum number of tasks created for the Connector
     * @param expectedFormat
     *            The expected format, of files, path, table names or other ways to partition the tasks.
     */
    void reconfigureDistributionStrategy(int maxTasks, String expectedFormat);

    /**
     * Check if the task is responsible for this set of files by checking if the given task matches the partition id.
     *
     * @param taskId
     *            the current running task
     * @param partitionId
     *            The partitionId recovered from the file path.
     * @return true if this task is responsible for this partition. false if it is not responsible for this task.
     */
    default boolean taskMatchesPartition(final int taskId, final int partitionId) {
        // The partition id and task id are both expected to start at 0 but if the task id is changed to start at 1 this
        // will break.
        return taskId == partitionId;
    }

    /**
     * In the event of more partitions existing then tasks configured, the task will be required to take up additional
     * tasks that match.
     *
     * @param taskId
     *            the current running task.
     * @param maxTasks
     *            The maximum number of configured tasks allowed to run for this connector.
     * @param partitionId
     *            The partitionId recovered from the file path.
     * @return true if the task supplied should handle the supplied partition
     */
    default boolean taskMatchesModOfPartitionAndMaxTask(final int taskId, final int maxTasks, final int partitionId) {

        return taskMatchesPartition(taskId, partitionId % maxTasks);
    }

    default boolean toBeProcessedByThisTask(final int taskId, final int maxTasks, final int partitionId) {
        return partitionId < maxTasks
                ? taskMatchesPartition(taskId, partitionId)
                : taskMatchesModOfPartitionAndMaxTask(taskId, maxTasks, partitionId);

    }
}
