package io.ebean.json;

import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareMap;
import io.ebeaninternal.json.ModifyAwareOwner;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class EJsonTests {

  private static final Logger log = LoggerFactory.getLogger(EJsonTests.class);

  @Test
  public void test_map_simple() throws IOException {

    JsonFactory factory = new JsonFactory();

    String jsonInput = "{\"name\":\"rob\",\"age\":12}";

    JsonParser jsonParser = factory.createParser(jsonInput);

    Object result = EJson.parse(jsonParser);

    assertTrue(result instanceof Map);
    Map<?, ?> map = (Map<?, ?>) result;
    assertEquals("rob", map.get("name"));
    assertEquals(12L, map.get("age"));

    String jsonOutput = EJson.write(result);
    assertEquals(jsonInput, jsonOutput);
  }

  @Test
  public void write_withWriter_expect_writerNotClosed() throws IOException {

    File temp = Files.createTempFile("some", ".json").toFile();
    FileWriter writer = new FileWriter(temp);
    Map<String,Object> map = new LinkedHashMap<>();
    map.put("foo", "bar");
    EJson.write(map, writer);
    writer.write("The end.");
    writer.flush();
    writer.close();

    log.info("write to file {}", temp.getAbsolutePath());
  }
}
