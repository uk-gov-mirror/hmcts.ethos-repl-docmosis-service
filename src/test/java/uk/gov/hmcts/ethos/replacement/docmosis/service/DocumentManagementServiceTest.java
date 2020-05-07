package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentManagementServiceTest {

    @Mock
    private DocumentUploadClientApi documentUploadClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @InjectMocks
    private DocumentManagementService documentManagementService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private File file;
    private String markup;

    @Before
    public void setUp() {
        file = createTestFile();
        markup = "<a target=\"_blank\" href=\"null/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary\">Document</a>";
        when(authTokenGenerator.generate()).thenReturn("authString");
        documentManagementService = new DocumentManagementService(documentUploadClient, authTokenGenerator, userService);
    }

    @Test
    public void shouldUploadToDocumentManagement() throws IOException, URISyntaxException {
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(successfulDocumentManagementUploadResponse());
        URI documentSelfPath = documentManagementService.uploadDocument("authString", Files.readAllBytes(file.toPath()));
        String documentDownloadableURL = documentManagementService.generateDownloadableURL(documentSelfPath);
        assertEquals(documentManagementService.generateMarkupDocument(documentDownloadableURL), markup);
        assertNotNull(documentSelfPath);
        assertEquals("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4", documentSelfPath.getPath());
    }

    @Test
    public void uploadDocumentToDocumentManagementThrowsException() throws IOException, URISyntaxException {
        expectedException.expect(DocumentManagementException.class);
        expectedException.expectMessage("Unable to upload document document.docx to document management");
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(unsuccessfulDocumentManagementUploadResponse());
        documentManagementService.uploadDocument("authString", Files.readAllBytes(file.toPath()));
    }

    private File createTestFile() {
        Path path = Paths.get(OUTPUT_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("Hello World !!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path.toFile();
    }
}