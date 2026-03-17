/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.StarManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserEntryStar;
import org.apache.roller.weblogger.pojos.UserWeblogStar;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * JPA implementation of StarManager.
 */
@com.google.inject.Singleton
public class JPAStarManagerImpl implements StarManager {

    private static final Log LOG = LogFactory.getLog(JPAStarManagerImpl.class);

    private final JPAPersistenceStrategy strategy;


    @com.google.inject.Inject
    protected JPAStarManagerImpl(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
        LOG.debug("JPAStarManagerImpl created");
    }


    // ---- Weblog stars ----

    @Override
    public void saveWeblogStar(UserWeblogStar star) throws WebloggerException {
        this.strategy.store(star);
    }

    @Override
    public void removeWeblogStar(UserWeblogStar star) throws WebloggerException {
        this.strategy.remove(star);
    }

    @Override
    public UserWeblogStar getWeblogStar(String id) throws WebloggerException {
        return (UserWeblogStar) this.strategy.load(UserWeblogStar.class, id);
    }

    @Override
    public UserWeblogStar getWeblogStarByUserAndWeblog(User user, Weblog weblog) throws WebloggerException {
        try {
            TypedQuery<UserWeblogStar> query = strategy.getNamedQuery(
                    "UserWeblogStar.getByUserAndWeblog", UserWeblogStar.class);
            query.setParameter(1, user);
            query.setParameter(2, weblog);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<UserWeblogStar> getStarredWeblogs(User user, int offset, int length) throws WebloggerException {
        TypedQuery<UserWeblogStar> query = strategy.getNamedQuery(
                "UserWeblogStar.getStarredWeblogsByUser", UserWeblogStar.class);
        query.setParameter(1, user);
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (length > 0) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }


    // ---- Entry stars ----

    @Override
    public void saveEntryStar(UserEntryStar star) throws WebloggerException {
        this.strategy.store(star);
    }

    @Override
    public void removeEntryStar(UserEntryStar star) throws WebloggerException {
        this.strategy.remove(star);
    }

    @Override
    public UserEntryStar getEntryStar(String id) throws WebloggerException {
        return (UserEntryStar) this.strategy.load(UserEntryStar.class, id);
    }

    @Override
    public UserEntryStar getEntryStarByUserAndEntry(User user, WeblogEntry entry) throws WebloggerException {
        try {
            TypedQuery<UserEntryStar> query = strategy.getNamedQuery(
                    "UserEntryStar.getByUserAndEntry", UserEntryStar.class);
            query.setParameter(1, user);
            query.setParameter(2, entry);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<UserEntryStar> getStarredEntries(User user, int offset, int length) throws WebloggerException {
        TypedQuery<UserEntryStar> query = strategy.getNamedQuery(
                "UserEntryStar.getStarredEntriesByUser", UserEntryStar.class);
        query.setParameter(1, user);
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (length > 0) {
            query.setMaxResults(length);
        }
        return query.getResultList();
    }


    // ---- Trending (aggregate queries — no iteration) ----

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getTrendingWeblogs(int limit) throws WebloggerException {
        Query query = strategy.getNamedQuery("UserWeblogStar.getTrendingWeblogs");
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<Object[]> rows = query.getResultList();
        // Resolve weblog IDs to entities: query returns [weblogId, count]
        for (Object[] row : rows) {
            row[0] = this.strategy.load(Weblog.class, (String) row[0]);
        }
        return rows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getTrendingEntries(int limit) throws WebloggerException {
        Query query = strategy.getNamedQuery("UserEntryStar.getTrendingEntries");
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        List<Object[]> rows = query.getResultList();
        // Resolve entry IDs to entities: query returns [entryId, count]
        for (Object[] row : rows) {
            row[0] = this.strategy.load(WeblogEntry.class, (String) row[0]);
        }
        return rows;
    }


    // ---- Counts ----

    @Override
    public long getWeblogStarCount(Weblog weblog) throws WebloggerException {
        Query query = strategy.getNamedQuery("UserWeblogStar.getCountByWeblog");
        query.setParameter(1, weblog);
        return (Long) query.getSingleResult();
    }

    @Override
    public long getEntryStarCount(WeblogEntry entry) throws WebloggerException {
        Query query = strategy.getNamedQuery("UserEntryStar.getCountByEntry");
        query.setParameter(1, entry);
        return (Long) query.getSingleResult();
    }


    @Override
    public void release() {
        // nothing to release
    }
}
