/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tez.dag.history.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.tez.common.counters.TezCounters;
import org.apache.tez.dag.api.DagTypeConverters;
import org.apache.tez.dag.api.oldrecords.TaskAttemptState;
import org.apache.tez.dag.app.dag.impl.TaskAttemptImpl.DataEventDependencyInfo;
import org.apache.tez.dag.history.HistoryEvent;
import org.apache.tez.dag.history.HistoryEventType;
import org.apache.tez.dag.history.utils.TezEventUtils;
import org.apache.tez.dag.records.TaskAttemptTerminationCause;
import org.apache.tez.dag.records.TezTaskAttemptID;
import org.apache.tez.dag.recovery.records.RecoveryProtos.DataEventDependencyInfoProto;
import org.apache.tez.dag.recovery.records.RecoveryProtos.TaskAttemptFinishedProto;
import org.apache.tez.dag.recovery.records.RecoveryProtos.TezEventProto;
import org.apache.tez.runtime.api.impl.TezEvent;

public class TaskAttemptFinishedEvent implements HistoryEvent {

  private static final Logger LOG = LoggerFactory.getLogger(TaskAttemptFinishedEvent.class);

  private TezTaskAttemptID taskAttemptId;
  private String vertexName;
  private long creationTime;
  private long allocationTime;
  private long startTime;
  private long finishTime;
  private TezTaskAttemptID creationCausalTA;
  private TaskAttemptState state;
  private String diagnostics;
  private TezCounters tezCounters;
  private TaskAttemptTerminationCause error;
  private List<DataEventDependencyInfo> dataEvents;
  private List<TezEvent> taGeneratedEvents;
  private ContainerId containerId;
  private NodeId nodeId;
  private String inProgressLogsUrl;
  private String completedLogsUrl;
  private String nodeHttpAddress;

  public TaskAttemptFinishedEvent(TezTaskAttemptID taId,
      String vertexName,
      long startTime,
      long finishTime,
      TaskAttemptState state,
      TaskAttemptTerminationCause error,
      String diagnostics, TezCounters counters, 
      List<DataEventDependencyInfo> dataEvents,
      List<TezEvent> taGeneratedEvents,
      long creationTime, 
      TezTaskAttemptID creationCausalTA, 
      long allocationTime,
      ContainerId containerId,
      NodeId nodeId,
      String inProgressLogsUrl,
      String completedLogsUrl,
      String nodeHttpAddress) {
    this.taskAttemptId = taId;
    this.vertexName = vertexName;
    this.creationCausalTA = creationCausalTA;
    this.creationTime = creationTime;
    this.allocationTime = allocationTime;
    this.startTime = startTime;
    this.finishTime = finishTime;
    this.state = state;
    this.diagnostics = diagnostics;
    this.tezCounters = counters;
    this.error = error;
    this.dataEvents = dataEvents;
    this.taGeneratedEvents = taGeneratedEvents;
    this.containerId = containerId;
    this.nodeId = nodeId;
    this.inProgressLogsUrl = inProgressLogsUrl;
    this.completedLogsUrl = completedLogsUrl;
    this.nodeHttpAddress = nodeHttpAddress;
  }

  public TaskAttemptFinishedEvent() {
  }

  @Override
  public HistoryEventType getEventType() {
    return HistoryEventType.TASK_ATTEMPT_FINISHED;
  }

  @Override
  public boolean isRecoveryEvent() {
    return true;
  }

  @Override
  public boolean isHistoryEvent() {
    return true;
  }
  
  public List<DataEventDependencyInfo> getDataEvents() {
    return dataEvents;
  }
  
  public TaskAttemptFinishedProto toProto() throws IOException {
    TaskAttemptFinishedProto.Builder builder =
        TaskAttemptFinishedProto.newBuilder();
    builder.setTaskAttemptId(taskAttemptId.toString())
        .setState(state.ordinal())
        .setCreationTime(creationTime)
        .setAllocationTime(allocationTime)
        .setStartTime(startTime)
        .setFinishTime(finishTime);
    if (creationCausalTA != null) {
      builder.setCreationCausalTA(creationCausalTA.toString());
    }
    if (diagnostics != null) {
      builder.setDiagnostics(diagnostics);
    }
    if (error != null) {
      builder.setErrorEnum(error.name());
    }
    if (tezCounters != null) {
      builder.setCounters(DagTypeConverters.convertTezCountersToProto(tezCounters));
    }
    if (dataEvents != null && !dataEvents.isEmpty()) {
      for (DataEventDependencyInfo info : dataEvents) {
        builder.addDataEvents(DataEventDependencyInfo.toProto(info));
      }
    }
    if (taGeneratedEvents != null && !taGeneratedEvents.isEmpty()) {
      for (TezEvent event : taGeneratedEvents) {
        builder.addTaGeneratedEvents(TezEventUtils.toProto(event));
      }
    }
    if (containerId != null) {
      builder.setContainerId(containerId.toString());
    }
    if (nodeId != null) {
      builder.setNodeId(nodeId.toString());
    }
    if (nodeHttpAddress != null) {
      builder.setNodeHttpAddress(nodeHttpAddress);
    }
    return builder.build();
  }

  public void fromProto(TaskAttemptFinishedProto proto) throws IOException {
    this.taskAttemptId = TezTaskAttemptID.fromString(proto.getTaskAttemptId());
    this.state = TaskAttemptState.values()[proto.getState()];
    this.creationTime = proto.getCreationTime();
    this.allocationTime = proto.getAllocationTime();
    this.startTime = proto.getStartTime();
    this.finishTime = proto.getFinishTime();
    if (proto.hasCreationCausalTA()) {
      this.creationCausalTA = TezTaskAttemptID.fromString(proto.getCreationCausalTA());
    }
    if (proto.hasDiagnostics()) {
      this.diagnostics = proto.getDiagnostics();
    }
    if (proto.hasErrorEnum()) {
      this.error = TaskAttemptTerminationCause.valueOf(proto.getErrorEnum());
    }
    if (proto.hasCounters()) {
      this.tezCounters = DagTypeConverters.convertTezCountersFromProto(
        proto.getCounters());
    }
    if (proto.getDataEventsCount() > 0) {
      this.dataEvents = Lists.newArrayListWithCapacity(proto.getDataEventsCount());
      for (DataEventDependencyInfoProto protoEvent : proto.getDataEventsList()) {
        this.dataEvents.add(DataEventDependencyInfo.fromProto(protoEvent));
      }
    }
    if (proto.getTaGeneratedEventsCount() > 0) {
      this.taGeneratedEvents = Lists.newArrayListWithCapacity(proto.getTaGeneratedEventsCount());
      for (TezEventProto eventProto : proto.getTaGeneratedEventsList()) {
        this.taGeneratedEvents.add(TezEventUtils.fromProto(eventProto));
      }
    }
    if (proto.hasContainerId()) {
      this.containerId = ConverterUtils.toContainerId(proto.getContainerId());
    }
    if (proto.hasNodeId()) {
      this.nodeId = ConverterUtils.toNodeId(proto.getNodeId());
    }
    if (proto.hasNodeHttpAddress()) {
      this.nodeHttpAddress = proto.getNodeHttpAddress();
    }
  }

  @Override
  public void toProtoStream(OutputStream outputStream) throws IOException {
    toProto().writeDelimitedTo(outputStream);
  }

  @Override
  public void fromProtoStream(InputStream inputStream) throws IOException {
    TaskAttemptFinishedProto proto =
        TaskAttemptFinishedProto.parseDelimitedFrom(inputStream);
    if (proto == null) {
      throw new IOException("No data found in stream");
    }
    fromProto(proto);
  }

  @Override
  public String toString() {
    String counterStr = "";
    if (state != TaskAttemptState.SUCCEEDED) {
      counterStr = ", counters=" + ( tezCounters == null ? "null" :
        tezCounters.toString()
        .replaceAll("\\n", ", ").replaceAll("\\s+", " "));
    }
    return "vertexName=" + vertexName
        + ", taskAttemptId=" + taskAttemptId
        + ", creationTime=" + creationTime
        + ", allocationTime=" + allocationTime
        + ", startTime=" + startTime
        + ", finishTime=" + finishTime
        + ", timeTaken=" + (finishTime - startTime)
        + ", status=" + state.name()
        + ", errorEnum=" + (error != null ? error.name() : "")
        + ", diagnostics=" + diagnostics
        + ", containerId=" + (containerId != null ? containerId.toString() : "")
        + ", nodeId=" + (nodeId != null ? nodeId.toString() : "")
        + ", nodeHttpAddress=" + (nodeHttpAddress != null ? nodeHttpAddress : "")
        + counterStr;
  }

  public TezTaskAttemptID getTaskAttemptID() {
    return taskAttemptId;
  }

  public TezCounters getCounters() {
    return tezCounters;
  }

  public String getDiagnostics() {
    return diagnostics;
  }
  
  public TaskAttemptTerminationCause getTaskAttemptError() {
    return error;
  }

  public long getFinishTime() {
    return finishTime;
  }

  public TaskAttemptState getState() {
    return state;
  }

  public long getStartTime() {
    return startTime;
  }
  
  public long getCreationTime() {
    return creationTime;
  }
  
  public long getAllocationTime() {
    return allocationTime;
  }
  
  public TezTaskAttemptID getCreationCausalTA() {
    return creationCausalTA;
  }

  public List<TezEvent> getTAGeneratedEvents() {
    return taGeneratedEvents;
  }

  public ContainerId getContainerId() {
    return containerId;
  }

  public NodeId getNodeId() {
    return nodeId;
  }

  public String getInProgressLogsUrl() {
    return inProgressLogsUrl;
  }

  public String getCompletedLogsUrl() {
    return completedLogsUrl;
  }

  public String getNodeHttpAddress() {
    return nodeHttpAddress;
  }
}
