package org.jabref.model.database;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.jabref.model.metadata.FileDirectoryPreferences;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BibDatabaseContextTest {

    private Path currentWorkingDir;

    // Store the minimal preferences for the
    // BibDatabaseContext.getFileDirectories(File,
    // FileDirectoryPreferences) incocation:
    private FileDirectoryPreferences fileDirPrefs;

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        fileDirPrefs = mock(FileDirectoryPreferences.class);
        currentWorkingDir = Paths.get(System.getProperty("user.dir"));
        when(fileDirPrefs.isBibLocationAsPrimary()).thenReturn(true);
    }

    @Test
    public void getFileDirectoriesWithEmptyDbParent() {
        BibDatabaseContext dbContext = new BibDatabaseContext();
        dbContext.setDatabaseFile(Paths.get("biblio.bib").toFile());
        List<String> fileDirectories = dbContext.getFileDirectories("file", fileDirPrefs);
        assertEquals(Collections.singletonList(currentWorkingDir.toString()),
                fileDirectories);
    }
}
