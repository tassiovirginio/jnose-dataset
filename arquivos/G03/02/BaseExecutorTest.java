/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.Proxy;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseExecutorTest extends BaseDataTest {
  protected final Configuration config;
  protected static DataSource ds;

  @BeforeClass
  public static void setup() throws Exception {
    ds = createBlogDataSource();
  }

  public BaseExecutorTest() {
    config = new Configuration();
    config.setLazyLoadingEnabled(true);
    config.setUseGeneratedKeys(false);
    config.setMultipleResultSetsEnabled(true);
    config.setUseColumnLabel(true);
    config.setDefaultStatementTimeout(5000);
    config.setDefaultFetchSize(100);
  }

  @Test
  public void shouldInsertNewAuthorWithBeforeAutoKey() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(-1, "someone", "******", "someone@apache.org", null, Section.NEWS);
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatementWithBeforeAutoKey(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(insertStatement, author);
      assertTrue(rows > 0 || rows == BatchExecutor.BATCH_UPDATE_RETURN_VALUE);
      if (rows == BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        executor.flushStatements();
      }
      assertEquals(123456, author.getId());
      if (author.getId() != BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        List<Author> authors = executor.query(selectStatement, author.getId(), RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        executor.rollback(true);
        assertEquals(1, authors.size());
        assertEquals(author.toString(), authors.get(0).toString());
        assertTrue(author.getId() >= 10000);
      }
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

}
