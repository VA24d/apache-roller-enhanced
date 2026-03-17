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

<p class="subtitle"><s:text name="starredEntries.subtitle" /></p>

<s:if test="pager == null || pager.items.isEmpty">
    <p><s:text name="starredEntries.noneStarred" /></p>
</s:if>

<s:else>
    <table class="table table-striped table-bordered">
        <thead>
            <tr>
                <th><s:text name="starredEntries.entryTitle" /></th>
                <th><s:text name="starredEntries.weblog" /></th>
                <th><s:text name="starredEntries.starredDate" /></th>
                <th><s:text name="starredEntries.actions" /></th>
            </tr>
        </thead>
        <tbody>
            <s:iterator var="star" value="pager.items">
                <tr>
                    <td>
                        <a href='<s:property value="#star.entry.permalink" />'>
                            <s:property value="#star.entry.title" />
                        </a>
                    </td>
                    <td>
                        <a href='<s:property value="#star.entry.website.absoluteURL" />'>
                            <s:property value="#star.entry.website.name" />
                        </a>
                    </td>
                    <td>
                        <s:date name="#star.starredTime" format="yyyy-MM-dd HH:mm" />
                    </td>
                    <td>
                        <s:url action="starEntry!unstar" var="unstarUrl">
                            <s:param name="entryId" value="#star.entry.id" />
                        </s:url>
                        <a href='<s:property value="unstarUrl" />' class="btn btn-xs btn-warning">
                            <span class="glyphicon glyphicon-star" aria-hidden="true"></span>
                            <s:text name="starredEntries.unstar" />
                        </a>
                    </td>
                </tr>
            </s:iterator>
        </tbody>
    </table>

    <%-- Pagination --%>
    <nav>
        <ul class="pager">
            <s:if test="pager.prevLink != null">
                <li class="previous">
                    <a href='<s:property value="pager.prevLink" />'>
                        &larr; <s:text name="starredEntries.prev" />
                    </a>
                </li>
            </s:if>
            <s:if test="pager.nextLink != null">
                <li class="next">
                    <a href='<s:property value="pager.nextLink" />'>
                        <s:text name="starredEntries.next" /> &rarr;
                    </a>
                </li>
            </s:if>
        </ul>
    </nav>
</s:else>
