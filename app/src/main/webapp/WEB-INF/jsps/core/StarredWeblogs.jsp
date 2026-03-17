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

<p class="subtitle"><s:text name="starredWeblogs.subtitle" /></p>

<s:if test="starredWeblogs.isEmpty">
    <p><s:text name="starredWeblogs.noneStarred" /></p>
</s:if>

<s:else>
    <table class="table table-striped table-bordered">
        <thead>
            <tr>
                <th><s:text name="starredWeblogs.weblogName" /></th>
                <th><s:text name="starredWeblogs.lastPost" /></th>
                <th><s:text name="starredWeblogs.actions" /></th>
            </tr>
        </thead>
        <tbody>
            <s:iterator var="star" value="starredWeblogs">
                <tr>
                    <td>
                        <a href='<s:property value="#star.weblog.absoluteURL" />'>
                            <s:property value="#star.weblog.name" />
                        </a>
                    </td>
                    <td>
                        <s:if test="#star.weblog.lastModified != null">
                            <s:date name="#star.weblog.lastModified" format="yyyy-MM-dd HH:mm" />
                        </s:if>
                        <s:else>—</s:else>
                    </td>
                    <td>
                        <s:url action="starWeblog!unstar" var="unstarUrl">
                            <s:param name="weblogId" value="#star.weblog.id" />
                        </s:url>
                        <a href='<s:property value="unstarUrl" />' class="btn btn-xs btn-warning">
                            <span class="glyphicon glyphicon-star" aria-hidden="true"></span>
                            <s:text name="starredWeblogs.unstar" />
                        </a>
                    </td>
                </tr>
            </s:iterator>
        </tbody>
    </table>
</s:else>
