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

<p class="subtitle"><s:text name="trending.subtitle" /></p>

<%-- Trending Weblogs --%>
<h3><s:text name="trending.topWeblogs" /></h3>

<s:if test="trendingWeblogs.isEmpty">
    <p><s:text name="trending.noWeblogs" /></p>
</s:if>

<s:else>
    <table class="table table-striped table-bordered">
        <thead>
            <tr>
                <th>#</th>
                <th><s:text name="trending.weblogName" /></th>
                <th><s:text name="trending.starCount" /></th>
            </tr>
        </thead>
        <tbody>
            <s:iterator var="row" value="trendingWeblogs" status="idx">
                <tr>
                    <td><s:property value="#idx.count" /></td>
                    <td>
                        <a href='<s:property value="#row[0].absoluteURL" />'>
                            <s:property value="#row[0].name" />
                        </a>
                    </td>
                    <td>
                        <span class="badge"><s:property value="#row[1]" /></span>
                    </td>
                </tr>
            </s:iterator>
        </tbody>
    </table>
</s:else>


<%-- Trending Entries --%>
<h3><s:text name="trending.topEntries" /></h3>

<s:if test="trendingEntries.isEmpty">
    <p><s:text name="trending.noEntries" /></p>
</s:if>

<s:else>
    <table class="table table-striped table-bordered">
        <thead>
            <tr>
                <th>#</th>
                <th><s:text name="trending.entryTitle" /></th>
                <th><s:text name="trending.weblog" /></th>
                <th><s:text name="trending.starCount" /></th>
            </tr>
        </thead>
        <tbody>
            <s:iterator var="row" value="trendingEntries" status="idx">
                <tr>
                    <td><s:property value="#idx.count" /></td>
                    <td>
                        <a href='<s:property value="#row[0].permalink" />'>
                            <s:property value="#row[0].title" />
                        </a>
                    </td>
                    <td>
                        <a href='<s:property value="#row[0].website.absoluteURL" />'>
                            <s:property value="#row[0].website.name" />
                        </a>
                    </td>
                    <td>
                        <span class="badge"><s:property value="#row[1]" /></span>
                    </td>
                </tr>
            </s:iterator>
        </tbody>
    </table>
</s:else>
