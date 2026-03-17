<%--
  Licensed under the Apache License, Version 2.0.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="siteSummary.subtitle" />
</p>
<p><s:text name="siteSummary.prompt" /></p>

<%-- View toggle buttons --%>
<div style="margin-bottom:16px;">
    <s:url var="fullUrl" action="siteSummary">
        <s:param name="view" value="'full'" />
    </s:url>
    <s:url var="minimalistUrl" action="siteSummary">
        <s:param name="view" value="'minimalist'" />
    </s:url>

    <s:if test="view == 'full'">
        <a href="<s:property value='#fullUrl' />" class="btn btn-primary btn-sm">
            Full View</a>
        <a href="<s:property value='#minimalistUrl' />" class="btn btn-default btn-sm">
            Minimalist View</a>
    </s:if>
    <s:else>
        <a href="<s:property value='#fullUrl' />" class="btn btn-default btn-sm">
            Full View</a>
        <a href="<s:property value='#minimalistUrl' />" class="btn btn-primary btn-sm">
            Minimalist View</a>
    </s:else>

    <span style="margin-left:12px;color:#777;">
        Currently viewing: <strong><s:property value="report.viewName" /></strong>
        (<s:property value="report.metricCount" /> metrics)
    </span>
</div>

<%-- Metrics display --%>
<s:if test="report != null && report.results.size() > 0">

    <div class="row">
        <s:iterator var="metric" value="report.results" status="idx">
            <div class="col-md-4" style="margin-bottom:16px;">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <s:property value="#metric.label" />
                        </h4>
                    </div>
                    <div class="panel-body" style="text-align:center;">
                        <h2 style="margin:4px 0;">
                            <s:property value="#metric.value" />
                        </h2>

                        <s:if test="#metric.details != null && #metric.details.size() > 0">
                            <ul class="list-unstyled" style="font-size:0.9em;color:#777;margin-top:8px;">
                                <s:iterator var="detail" value="#metric.details">
                                    <li><s:property value="#detail" /></li>
                                </s:iterator>
                            </ul>
                        </s:if>
                    </div>
                </div>
            </div>

            <%-- New row every 3 cards --%>
            <s:if test="(#idx.count % 3) == 0">
                </div><div class="row">
            </s:if>
        </s:iterator>
    </div>

</s:if>
<s:else>
    <p class="text-muted">No metrics available.</p>
</s:else>
