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
    <s:text name="bugReportForm.editSubtitle"/>
</p>

<s:form action="bugReportAdd!save" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="bean.id"/>

    <s:textfield name="bean.title" label="%{getText('bugReportForm.titleLabel')}"
                 size="50" maxlength="255" cssClass="form-control"/>

    <s:textarea name="bean.description" label="%{getText('bugReportForm.description')}"
                rows="5" cols="50" cssClass="form-control"/>

    <s:select name="bean.reportType" label="%{getText('bugReportForm.type')}"
              list="{'BROKEN_BUTTON','BROKEN_LINK','UI_ISSUE','OTHER'}"
              cssClass="form-control"/>

    <s:select name="bean.severity" label="%{getText('bugReportForm.severity')}"
              list="{'LOW','MEDIUM','HIGH'}"
              cssClass="form-control"/>

    <s:textfield name="bean.pageUrl" label="%{getText('bugReportForm.pageUrl')}"
                 size="50" maxlength="512" cssClass="form-control"/>

    <s:textarea name="bean.stepsToReproduce" label="%{getText('bugReportForm.stepsToReproduce')}"
                rows="4" cols="50" cssClass="form-control"/>

    <s:textarea name="bean.expectedBehavior" label="%{getText('bugReportForm.expectedBehavior')}"
                rows="3" cols="50" cssClass="form-control"/>

    <s:textarea name="bean.actualBehavior" label="%{getText('bugReportForm.actualBehavior')}"
                rows="3" cols="50" cssClass="form-control"/>

    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-9">
            <s:submit value="%{getText('generic.save')}" cssClass="btn btn-primary"/>

            <s:url var="cancelUrl" action="bugReports">
                <s:param name="weblog" value="weblog"/>
            </s:url>
            <s:a href="%{cancelUrl}" cssClass="btn btn-default">
                <s:text name="generic.cancel"/>
            </s:a>
        </div>
    </div>
</s:form>
