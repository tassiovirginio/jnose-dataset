/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.schemaupdate;

import java.io.File;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import org.hibernate.testing.DialectChecks;
import org.hibernate.testing.RequiresDialectFeature;
import org.hibernate.testing.ServiceRegistryBuilder;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Gail Badner
 */
public class SchemaExportTest extends BaseUnitTestCase {


    @Test
    public void testGenerateDdlToFile() {
		final SchemaExport schemaExport = new SchemaExport();

        java.io.File outFile = new java.io.File("schema.ddl");
        schemaExport.setOutputFile( outFile.getPath() );

        // do not script to console or export to database
        schemaExport.execute( EnumSet.of( TargetType.SCRIPT ), SchemaExport.Action.DROP, metadata );
        if ( doesDialectSupportDropTableIfExist() && schemaExport.getExceptions().size() > 0 ) {
            assertEquals( 2, schemaExport.getExceptions().size() );
        }
        assertTrue( outFile.exists() );

        //check file is not empty
        assertTrue( outFile.length() > 0 );
        outFile.delete();
    }

 
}
