package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import net.jcip.annotations.NotThreadSafe;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbDropAllCommandTest extends AbstractMigrationTest {

    private DbDropAllCommand<TestMigrationConfiguration> dropAllCommand = new DbDropAllCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml");

    @Test
    public void testRun() throws Exception {
        final String databaseUrl = getDatabaseUrl();
        final TestMigrationConfiguration conf = createConfiguration(databaseUrl);

        // Create some data
        new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations.xml")
            .run(null, new Namespace(ImmutableMap.of()), conf);

        // Drop it
        dropAllCommand.run(null, new Namespace(ImmutableMap.of()), conf);

        // After we dropped data and schema, we should be able to create the "persons" table again
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            handle.execute("create table persons(id int, name varchar(255))");
        }
    }
}
