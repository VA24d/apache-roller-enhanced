/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.business.search.lucene;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

/**
 * Abstraction that provides index operations with the context they need
 * from the index manager, without coupling them to the concrete
 * {@link LuceneIndexManager} class.
 *
 * <p>This interface follows the GRASP Indirection principle and the
 * Dependency Inversion Principle (DIP) to break the cyclic dependency
 * between the manager and its operations. It exposes only the five
 * methods that operations actually call (Interface Segregation Principle).</p>
 *
 * @see LuceneIndexManager
 * @see IndexOperation
 */
public interface IndexOperationContext {

    /**
     * Returns the configured Lucene analyzer for text processing.
     *
     * @return Analyzer to be used in manipulating the index.
     */
    Analyzer getAnalyzer();

    /**
     * Returns the Lucene directory where the index is stored.
     *
     * @return Directory containing the index, or null if error.
     */
    Directory getIndexDirectory();

    /**
     * Returns the read-write lock used to coordinate concurrent
     * access to the index.
     *
     * @return ReadWriteLock for index access synchronization.
     */
    ReadWriteLock getReadWriteLock();

    /**
     * Resets the shared index reader so it will be reopened on
     * the next read, picking up any recent writes.
     */
    void resetSharedReader();

    /**
     * Returns a shared index reader for search operations.
     * The reader is lazily opened and cached.
     *
     * @return IndexReader for reading the index.
     */
    IndexReader getSharedIndexReader();
}
