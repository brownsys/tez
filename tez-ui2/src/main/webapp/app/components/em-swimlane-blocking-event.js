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

import Ember from 'ember';

export default Ember.Component.extend({

  process: null,
  blocking: null,
  events: null,

  classNames: ["em-swimlane-blocking-event"],

  blockingEvent: Ember.computed("events.length", "process.blockingEventName", function () {
    var events = this.get("events"),
        blockingEventName = this.get("process.blockingEventName");

    return events.find(function (event) {
      return event.name === blockingEventName;
    });
  }),

  didInsertElement: Ember.observer("blockingEvent", function () {
    var blockerEventHeight = (this.get("blocking.index") - this.get("process.index")) * 30;

    this.$().css({
      "left": this.get("blockingEvent.pos") + "%"
    });
    this.$(".event-line").css({
      "height": `${blockerEventHeight}px`,
      "border-color": this.get("process").getColor()
    });
  }),

  sendMouseAction: function (name, mouseEvent) {
    this.sendAction(name, "blocking-event", this.get("process"), {
      mouseEvent: mouseEvent,
      blocking: this.get("blocking"),
      blockingEvent: this.get("blockingEvent")
    });
  },

  mouseEnter: function (mouseEvent) {
    this.sendMouseAction("showTooltip", mouseEvent);
  },

  mouseLeave: function (mouseEvent) {
    this.sendMouseAction("hideTooltip", mouseEvent);
  },

});
