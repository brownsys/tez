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

import TableController from './table';
import ColumnDefinition from 'em-table/utils/column-definition';
import TableDefinition from 'em-table/utils/table-definition';

export default TableController.extend({

  queryParams: ["dagName", "dagID", "submitter", "status", "appID", "callerID", "pageNo"],
  dagName: "",
  dagID: "",
  submitter: "",
  status: "",
  appID: "",
  callerID: "",
  pageNo: 1,

  breadcrumbs: [],

  headerComponentNames: ['dags-page-search', 'table-controls', 'dags-pagination-ui'],

  definition: Ember.computed("dagName", "dagID", "submitter", "status", "appID", "callerID", "pageNo", function () {
    return TableDefinition.create({
      dagName: this.get("dagName"),
      dagID: this.get("dagID"),
      submitter: this.get("submitter"),
      status: this.get("status"),
      appID: this.get("appID"),
      callerID: this.get("callerID"),

      pageNum: this.get("pageNo"),
      rowCountOptions: [5, 10, 25, 50, 100, 250, 500]
    });
  }),

  columns: ColumnDefinition.make([{
    id: 'name',
    headerTitle: 'Dag Name',
    contentPath: 'name',
    cellComponentName: 'em-table-linked-cell',
    getCellContent: function (row) {
      return {
        routeName: "dag",
        model: row.get("entityID"),
        text: row.get("name")
      };
    }
  },{
    id: 'entityID',
    headerTitle: 'Id',
    contentPath: 'entityID'
  },{
    id: 'submitter',
    headerTitle: 'Submitter',
    contentPath: 'submitter'
  },{
    id: 'status',
    headerTitle: 'Status',
    contentPath: 'status',
    cellComponentName: 'em-table-status-cell',
    observePath: true
  },{
    id: 'progress',
    headerTitle: 'Progress',
    contentPath: 'progress',
    cellComponentName: 'em-table-progress-cell',
    observePath: true
  },{
    id: 'startTime',
    headerTitle: 'Start Time',
    contentPath: 'startTime',
    cellComponentName: 'date-formatter',
  },{
    id: 'endTime',
    headerTitle: 'End Time',
    contentPath: 'endTime',
    cellComponentName: 'date-formatter',
  },{
    id: 'duration',
    headerTitle: 'Duration',
    contentPath: 'duration',
    cellDefinition: {
      type: 'duration'
    }
  },{
    id: 'appID',
    headerTitle: 'Application Id',
    contentPath: 'appID',
    cellComponentName: 'em-table-linked-cell',
    getCellContent: function (row) {
      return {
        routeName: "app",
        model: row.get("appID"),
        text: row.get("appID")
      };
    }
  },{
    id: 'queue',
    headerTitle: 'Queue',
    contentPath: 'queue'
  },{
    id: 'callerID',
    headerTitle: 'Caller ID',
    contentPath: 'callerID'
  },{
    id: 'callerType',
    headerTitle: 'Caller Type',
    contentPath: 'callerType'
  },{
    id: 'logs',
    headerTitle: 'Logs',
    contentPath: 'containerLogs',
    cellComponentName: "em-table-linked-cell",
    cellDefinition: {
      target: "_blank"
    }
  }]),

  getCounterColumns: function () {
    return this._super().concat(this.get('env.app.tables.defaultColumns.dagCounters'));
  },

  actions: {
    searchChanged: function (propertyName, value) {
      this.set(propertyName, value);
    },
  }

});
