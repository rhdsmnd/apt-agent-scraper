package com.rhdes.aptagent.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Jsoup.class })
public class ScraperTests {

    @Mock
    Connection mockCrlConn;

    HashMap<String, Document> urlToParsedDocsMap;

    public static final String CP_PREFIX = "/saved-cl-files/";
    public static final String MOCK_URL = "https://localhost/";

    @BeforeAll
    public void init() throws IOException {
        initMockClPages();
        mockStatic(Jsoup.class);
        for (String url : urlToParsedDocsMap.keySet()) {
            Connection mockConn = mock(Connection.class);
            when(mockConn.get()).thenReturn(urlToParsedDocsMap.get(url));
            when(Jsoup.connect(url)).thenReturn(mockConn);
        }
    }

    @BeforeEach
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testJsoupMock() throws IOException {
        //HashMap<String, >
        //when(Jsoup.connect("http://localhost/main.html")).thenReturn(mockCrlConn);
        //when(mockCrlConn.get()).thenReturn(load)
        //System.out.println(System.getProperty("java.class.path"));
        //when(mockCrlConn.get()).thenReturn()
        if (urlToParsedDocsMap == null) {
            init();
        }
    }

    public void initMockClPages() {
        urlToParsedDocsMap = new HashMap<String, Document>();
        try {
            Files.walkFileTree(Paths.get(this.getClass().getResource(CP_PREFIX).toURI()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filepath, BasicFileAttributes attrs) {
                if (!filepath.toFile().isDirectory()) {
                    mockFile(filepath.toFile());
                }
                return FileVisitResult.CONTINUE;
                }
            });
        } catch (URISyntaxException |IOException e) {
            System.err.println("Invalid uri: " + e);
        }
    }

    public void mockFile(File fileObj) {
        try {
            Document parsedDoc = Jsoup.parse(fileObj, null);
            String filepath = fileObj.getAbsolutePath().replace(File.separatorChar, '/');
            urlToParsedDocsMap.put(MOCK_URL + filepath.substring(filepath.indexOf(CP_PREFIX) + CP_PREFIX.length()),
                                    parsedDoc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
