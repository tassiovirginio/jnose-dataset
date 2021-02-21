package io.ebeaninternal.server.expression;

import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class JsonPathExpressionTest extends BaseExpressionTest {

  private JsonPathExpression exp(String propertyName, String path, Op operator, Object value) {
    return new JsonPathExpression(propertyName, path, operator, value);
  }

  @Test
  public void isSameByPlan_when_same() {

    same(exp("a", "path", Op.EQ, 10), exp("a", "path", Op.EQ, 10));
  }
}
