<%--
  Licensed under the Apache License, Version 2.0.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="siteSummary.subtitle" />
</p>
<p><s:text name="siteSummary.prompt" /></p>

<%-- View toggle --%>
<s:url var="fullUrl" action="siteSummary">
    <s:param name="view" value="'full'" />
</s:url>
<s:url var="minimalistUrl" action="siteSummary">
    <s:param name="view" value="'minimalist'" />
</s:url>

<p>
    <s:if test="view == 'full'">
        <b>Full View</b> |
        <a href="<s:property value='#minimalistUrl' />">Minimalist View</a>
    </s:if>
    <s:else>
        <a href="<s:property value='#fullUrl' />">Full View</a> |
        <b>Minimalist View</b>
    </s:else>
    &mdash;
    <s:property value="report.viewName" /> view
    (<s:property value="report.metricCount" /> metrics)
</p>

<%-- Metrics table --%>
<s:if test="report != null && report.metricCount > 0">

    <table class="rollertable table table-striped">
        <tr class="rollertable">
            <th class="rollertable" width="30%">Metric</th>
            <th class="rollertable" width="25%">Value</th>
            <th class="rollertable" width="45%">Details</th>
        </tr>

        <s:iterator var="metric" value="report.results">
            <tr>
                <td class="rollertable"><s:property value="#metric.label" /></td>
                <td class="rollertable"><b><s:property value="#metric.value" /></b></td>
                <td class="rollertable">
                    <s:if test="#metric.details != null && !#metric.details.isEmpty">
                        <s:iterator var="detail" value="#metric.details" status="dIdx">
                            <s:property value="#detail" />
                            <s:if test="!#dIdx.last"> &middot; </s:if>
                        </s:iterator>
                    </s:if>
                    <s:else>&mdash;</s:else>
                </td>
            </tr>
        </s:iterator>
    </table>

</s:if>
<s:else>
    <p>No metrics available.</p>
</s:else>
