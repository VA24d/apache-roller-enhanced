/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.jpa;

import java.util.List;
import jakarta.persistence.TypedQuery;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BugReportManager;
import org.apache.roller.weblogger.pojos.BugReport;

@com.google.inject.Singleton
public class JPABugReportManagerImpl implements BugReportManager {

    private final JPAPersistenceStrategy strategy;

    @com.google.inject.Inject
    protected JPABugReportManagerImpl(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void saveBugReport(BugReport bugReport) throws WebloggerException {
        strategy.store(bugReport);
    }

    @Override
    public void removeBugReport(BugReport bugReport) throws WebloggerException {
        strategy.remove(bugReport);
    }

    @Override
    public BugReport getBugReport(String id) throws WebloggerException {
        return (BugReport) strategy.load(BugReport.class, id);
    }

    @Override
    public List<BugReport> getBugReports() throws WebloggerException {
        TypedQuery<BugReport> q = strategy.getNamedQuery("BugReport.getAllOrderByUpdatedDesc", BugReport.class);
        return q.getResultList();
    }

    @Override
    public List<BugReport> getBugReportsByReporter(String reporterUserName) throws WebloggerException {
        TypedQuery<BugReport> q = strategy.getNamedQuery("BugReport.getByReporterOrderByUpdatedDesc", BugReport.class);
        q.setParameter(1, reporterUserName);
        return q.getResultList();
    }

    @Override
    public List<BugReport> getBugReportsByStatus(BugReport.Status status) throws WebloggerException {
        TypedQuery<BugReport> q = strategy.getNamedQuery("BugReport.getByStatusOrderByUpdatedDesc", BugReport.class);
        q.setParameter(1, status);
        return q.getResultList();
    }

    @Override
    public void release() {
        // no-op
    }
}
