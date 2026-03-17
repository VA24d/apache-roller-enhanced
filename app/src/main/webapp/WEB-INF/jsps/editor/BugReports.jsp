<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="bugReportForm.subtitle"/>
</p>
<p class="pagetip">
    <s:text name="bugReportForm.tip"/>
</p>

<%-- List of user's bug reports --%>
<table class="rollertable table table-striped" width="100%">
    <tr class="rollertable">
        <th width="25%"><s:text name="bugReportForm.titleLabel"/></th>
        <th width="15%"><s:text name="bugReportForm.type"/></th>
        <th width="10%"><s:text name="bugReportForm.severity"/></th>
        <th width="10%"><s:text name="bugReportForm.status"/></th>
        <th width="20%"><s:text name="bugReportForm.createdAt"/></th>
        <th width="10%"><s:text name="generic.edit"/></th>
        <th width="10%"><s:text name="generic.delete"/></th>
    </tr>

    <s:if test="reports != null && !reports.isEmpty()">
        <s:iterator var="report" value="reports" status="rowstatus">
            <tr>
                <td><s:property value="#report.title"/></td>
                <td><s:property value="#report.reportType"/></td>
                <td><s:property value="#report.severity"/></td>
                <td>
                    <s:if test="#report.status.name() == 'OPEN'">
                        <span class="label label-danger"><s:property value="#report.status"/></span>
                    </s:if>
                    <s:elseif test="#report.status.name() == 'TRIAGED'">
                        <span class="label label-warning"><s:property value="#report.status"/></span>
                    </s:elseif>
                    <s:else>
                        <span class="label label-success"><s:property value="#report.status"/></span>
                    </s:else>
                </td>
                <td><s:property value="#report.createdAt"/></td>
                <td align="center">
                    <s:url var="editUrl" action="bugReportEdit">
                        <s:param name="weblog" value="weblog"/>
                        <s:param name="bean.id" value="#report.id"/>
                    </s:url>
                    <s:a href="%{editUrl}">
                        <span class="glyphicon glyphicon-edit"></span>
                    </s:a>
                </td>
                <td align="center">
                    <s:url var="deleteUrl" action="bugReports!delete">
                        <s:param name="weblog" value="weblog"/>
                        <s:param name="reportId" value="#report.id"/>
                    </s:url>
                    <s:a href="%{deleteUrl}" onclick="return confirm('%{getText('generic.confirmDelete')}')">
                        <span class="glyphicon glyphicon-trash"></span>
                    </s:a>
                </td>
            </tr>
        </s:iterator>
    </s:if>
    <s:else>
        <tr>
            <td colspan="7"><s:text name="bugReportForm.noneFound"/></td>
        </tr>
    </s:else>
</table>

<s:url var="addUrl" action="bugReportAdd">
    <s:param name="weblog" value="weblog"/>
</s:url>
<s:a href="%{addUrl}" cssClass="btn btn-success">
    <span class="glyphicon glyphicon-plus"></span> <s:text name="bugReportForm.addNew"/>
</s:a>
