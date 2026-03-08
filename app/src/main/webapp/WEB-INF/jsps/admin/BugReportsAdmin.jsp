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

<p class="subtitle"><s:text name="bugReportsAdmin.subtitle"/></p>
<p><s:text name="bugReportsAdmin.explanation"/></p>

<%-- Filter bar --%>
<s:form action="bugReportsAdmin" method="GET" theme="simple" cssClass="form-inline" style="margin-bottom:15px;">
    <s:select name="filterStatus" headerKey="" headerValue="%{getText('bugReportsAdmin.allStatuses')}"
              list="{'OPEN','TRIAGED','RESOLVED'}" cssClass="form-control"/>
    <s:submit value="%{getText('bugReportsAdmin.filter')}" cssClass="btn btn-default"/>
</s:form>

<%-- Bug reports table --%>
<table class="rollertable table table-striped" width="100%">
    <tr class="rollertable">
        <th width="20%"><s:text name="bugReportForm.titleLabel"/></th>
        <th width="10%"><s:text name="bugReportForm.type"/></th>
        <th width="8%"><s:text name="bugReportForm.severity"/></th>
        <th width="8%"><s:text name="bugReportForm.status"/></th>
        <th width="12%"><s:text name="bugReportsAdmin.reporter"/></th>
        <th width="12%"><s:text name="bugReportForm.createdAt"/></th>
        <th width="15%"><s:text name="bugReportsAdmin.actions"/></th>
        <th width="5%"><s:text name="generic.delete"/></th>
    </tr>

    <s:if test="reports != null && !reports.isEmpty()">
        <s:iterator var="report" value="reports" status="rowstatus">
            <tr>
                <td>
                    <a href="#" onclick="showDetailModal('<s:property value="#report.id"/>')">
                        <s:property value="#report.title"/>
                    </a>
                </td>
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
                <td><s:property value="#report.reporterUserName"/></td>
                <td><s:property value="#report.createdAt"/></td>
                <td>
                    <s:if test="#report.status.name() == 'OPEN'">
                        <s:url var="triageUrl" action="bugReportsAdmin!changeStatus">
                            <s:param name="reportId" value="#report.id"/>
                            <s:param name="newStatus">TRIAGED</s:param>
                        </s:url>
                        <s:a href="%{triageUrl}" cssClass="btn btn-xs btn-warning">
                            <s:text name="bugReportsAdmin.triage"/>
                        </s:a>
                        <s:url var="resolveUrl" action="bugReportsAdmin!changeStatus">
                            <s:param name="reportId" value="#report.id"/>
                            <s:param name="newStatus">RESOLVED</s:param>
                        </s:url>
                        <s:a href="%{resolveUrl}" cssClass="btn btn-xs btn-success">
                            <s:text name="bugReportsAdmin.resolve"/>
                        </s:a>
                    </s:if>
                    <s:elseif test="#report.status.name() == 'TRIAGED'">
                        <s:url var="resolveUrl" action="bugReportsAdmin!changeStatus">
                            <s:param name="reportId" value="#report.id"/>
                            <s:param name="newStatus">RESOLVED</s:param>
                        </s:url>
                        <s:a href="%{resolveUrl}" cssClass="btn btn-xs btn-success">
                            <s:text name="bugReportsAdmin.resolve"/>
                        </s:a>
                        <s:url var="reopenUrl" action="bugReportsAdmin!changeStatus">
                            <s:param name="reportId" value="#report.id"/>
                            <s:param name="newStatus">OPEN</s:param>
                        </s:url>
                        <s:a href="%{reopenUrl}" cssClass="btn btn-xs btn-default">
                            <s:text name="bugReportsAdmin.reopen"/>
                        </s:a>
                    </s:elseif>
                    <s:else>
                        <s:url var="reopenUrl" action="bugReportsAdmin!changeStatus">
                            <s:param name="reportId" value="#report.id"/>
                            <s:param name="newStatus">TRIAGED</s:param>
                        </s:url>
                        <s:a href="%{reopenUrl}" cssClass="btn btn-xs btn-warning">
                            <s:text name="bugReportsAdmin.reopen"/>
                        </s:a>
                    </s:else>
                </td>
                <td align="center">
                    <s:url var="deleteUrl" action="bugReportsAdmin!delete">
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
            <td colspan="8"><s:text name="bugReportsAdmin.noneFound"/></td>
        </tr>
    </s:else>
</table>
