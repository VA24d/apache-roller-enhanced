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

<p class="subtitle"><s:text name="communityPulse.subtitle" /></p>

<%-- Entry Selector Form --%>
<s:form action="communityPulse!analyze" theme="bootstrap" cssClass="form-inline">
    <s:hidden name="salt" />
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />

    <div class="form-group" style="margin-bottom: 15px;">
        <label for="entryId"><s:text name="communityPulse.selectEntry" />:</label>
        <s:select name="entryId" list="recentEntries" listKey="id" listValue="title"
                  headerKey="" headerValue="-- Select Entry --"
                  cssClass="form-control" style="max-width:400px; display:inline-block; margin:0 10px;" />

        <s:submit value="%{getText('communityPulse.analyze')}" cssClass="btn btn-primary" />
    </div>
</s:form>

<%-- Results (only shown after analysis) --%>
<s:if test="pulseResult != null">

    <h3>
        <s:property value="pulseResult.entry.title" />
        <%-- Manual Refresh Button --%>
        <s:url var="refreshUrl" action="communityPulse!analyze">
            <s:param name="weblog" value="actionWeblog.handle" />
            <s:param name="entryId" value="entryId" />
            <s:param name="strategy" value="pulseResult.breakdown.methodUsed" />
            <s:param name="refresh" value="true" />
        </s:url>
        <a href="<s:property value='#refreshUrl' />" class="btn btn-sm btn-default" style="margin-left:10px;"
           title="Force regenerate analysis (bypasses cache)">
            &#x21bb; Refresh
        </a>
    </h3>

    <%-- ============ 6A: Discussion Overview ============ --%>
    <h4 style="margin-top:20px;"><s:text name="communityPulse.overview" /></h4>

    <div class="row" style="display:flex; flex-wrap:wrap; margin-bottom:20px;">

        <%-- Activity Level --%>
        <s:set var="activity" value="pulseResult.indicators['activityLevel']" />
        <s:if test="#activity != null">
            <div class="col-md-4" style="margin-bottom:15px;">
                <div class="panel panel-default">
                    <div class="panel-heading"><strong><s:property value="#activity['_label']" /></strong></div>
                    <div class="panel-body" style="text-align:center;">
                        <span style="font-size:2em; font-weight:bold;">
                            <s:property value="#activity['level']" />
                        </span>
                        <br/>
                        <small>
                            <s:property value="#activity['totalComments']" /> comments,
                            <s:property value="#activity['commentsPerDay']" /> per day
                        </small>
                    </div>
                </div>
            </div>
        </s:if>

        <%-- Response Types --%>
        <s:set var="types" value="pulseResult.indicators['responseTypes']" />
        <s:if test="#types != null">
            <div class="col-md-4" style="margin-bottom:15px;">
                <div class="panel panel-default">
                    <div class="panel-heading"><strong><s:property value="#types['_label']" /></strong></div>
                    <div class="panel-body">
                        <table class="table table-condensed" style="margin:0;">
                            <tr><td>Questions</td><td><s:property value="#types['questions']" /> (<s:property value="#types['questionsPct']" />%)</td></tr>
                            <tr><td>Positive</td><td><s:property value="#types['positive']" /> (<s:property value="#types['positivePct']" />%)</td></tr>
                            <tr><td>Debate</td><td><s:property value="#types['debate']" /> (<s:property value="#types['debatePct']" />%)</td></tr>
                            <tr><td>General</td><td><s:property value="#types['general']" /> (<s:property value="#types['generalPct']" />%)</td></tr>
                        </table>
                    </div>
                </div>
            </div>
        </s:if>

        <%-- Unique Commenters --%>
        <s:set var="uc" value="pulseResult.indicators['uniqueCommenters']" />
        <s:if test="#uc != null">
            <div class="col-md-4" style="margin-bottom:15px;">
                <div class="panel panel-default">
                    <div class="panel-heading"><strong><s:property value="#uc['_label']" /></strong></div>
                    <div class="panel-body" style="text-align:center;">
                        <span style="font-size:2em; font-weight:bold;">
                            <s:property value="#uc['uniqueCount']" />
                        </span>
                        unique out of <s:property value="#uc['totalComments']" />
                        <br/>
                        <span class="label label-info">
                            <s:property value="#uc['diversityLabel']" />
                        </span>
                    </div>
                </div>
            </div>
        </s:if>

        <%-- Top Contributors --%>
        <s:set var="tc" value="pulseResult.indicators['topContributors']" />
        <s:if test="#tc != null">
            <div class="col-md-4" style="margin-bottom:15px;">
                <div class="panel panel-default">
                    <div class="panel-heading"><strong><s:property value="#tc['_label']" /></strong></div>
                    <div class="panel-body">
                        <s:iterator var="contrib" value="#tc['contributors']">
                            <span class="label label-default" style="font-size:0.9em; margin-right:5px;">
                                <s:property value="#contrib['name']" />
                                (<s:property value="#contrib['commentCount']" />)
                            </span>
                        </s:iterator>
                        <br/><small><s:property value="#tc['totalContributors']" /> total contributors</small>
                    </div>
                </div>
            </div>
        </s:if>

        <%-- Recurring Keywords --%>
        <s:set var="kw" value="pulseResult.indicators['recurringKeywords']" />
        <s:if test="#kw != null">
            <div class="col-md-4" style="margin-bottom:15px;">
                <div class="panel panel-default">
                    <div class="panel-heading"><strong><s:property value="#kw['_label']" /></strong></div>
                    <div class="panel-body">
                        <s:iterator var="keyword" value="#kw['keywords']">
                            <span class="label label-primary" style="font-size:0.9em; margin:2px;">
                                <s:property value="#keyword['word']" />
                                (<s:property value="#keyword['count']" />)
                            </span>
                        </s:iterator>
                    </div>
                </div>
            </div>
        </s:if>

    </div>

    <%-- ============ 6B: Conversation Breakdown ============ --%>
    <h4><s:text name="communityPulse.breakdown" /></h4>

    <p>
        <small>
            <s:text name="communityPulse.method" />:
            <strong><s:property value="pulseResult.breakdown.methodUsed" /></strong>
        </small>
    </p>

    <%-- Themes --%>
    <s:if test="pulseResult.breakdown.themes != null && !pulseResult.breakdown.themes.isEmpty()">
        <s:iterator var="theme" value="pulseResult.breakdown.themes" status="idx">
            <div class="panel panel-info" style="margin-bottom:10px;">
                <div class="panel-heading">
                    <strong>Theme <s:property value="#idx.count" />: <s:property value="#theme.label" /></strong>
                    <span class="badge pull-right"><s:property value="#theme.commentCount" /> comments</span>
                </div>
                <div class="panel-body">
                    <p>
                        <s:text name="communityPulse.keywords" />:
                        <s:iterator var="kw" value="#theme.keywords">
                            <span class="label label-default"><s:property value="#kw" /></span>
                        </s:iterator>
                    </p>

                    <s:if test="#theme.representativeComments != null && !#theme.representativeComments.isEmpty()">
                        <p><strong><s:text name="communityPulse.representativeComments" />:</strong></p>
                        <s:iterator var="rep" value="#theme.representativeComments">
                            <blockquote style="font-size:0.9em; border-left:3px solid #5bc0de; padding:5px 10px; margin:5px 0;">
                                <s:property value="#rep" />
                            </blockquote>
                        </s:iterator>
                    </s:if>
                </div>
            </div>
        </s:iterator>
    </s:if>
    <s:else>
        <p><s:text name="communityPulse.noComments" /></p>
    </s:else>

    <%-- Recap --%>
    <s:if test="pulseResult.breakdown.recap != null">
        <div class="well" style="margin-top:15px;">
            <h5><s:text name="communityPulse.recap" /></h5>
            <p><s:property value="pulseResult.breakdown.recap" /></p>
        </div>
    </s:if>

    <%-- Strategy Switcher --%>
    <s:if test="pulseResult.availableStrategies.length > 1">
        <div style="margin-top:15px; padding:10px; background:#f5f5f5; border-radius:4px;">
            <s:text name="communityPulse.switchMethod" />:
            <s:iterator var="strat" value="pulseResult.availableStrategies">
                <s:url var="switchUrl" action="communityPulse!analyze">
                    <s:param name="weblog" value="actionWeblog.handle" />
                    <s:param name="entryId" value="entryId" />
                    <s:param name="strategy" value="#strat.name" />
                </s:url>
                <a href="<s:property value='#switchUrl' />" class="btn btn-sm btn-default"
                   title="<s:property value='#strat.description' />">
                    <s:property value="#strat.name" />
                </a>
            </s:iterator>
        </div>
    </s:if>

</s:if>

<s:elseif test="entryId != null && entryId != ''">
    <div class="alert alert-warning">
        <s:text name="communityPulse.noComments" />
    </div>
</s:elseif>
