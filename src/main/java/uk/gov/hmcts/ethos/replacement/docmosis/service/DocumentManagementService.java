package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.DocumentManagementException;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import java.io.File;
import java.net.URI;

import static java.util.Collections.singletonList;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementService {

    private static final String FILES_NAME = "files";
    private static final String APPLICATION_DOCX_VALUE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final Logger log = LoggerFactory.getLogger(DocumentManagementService.class);
    private DocumentUploadClientApi documentUploadClient;
    private AuthTokenGenerator authTokenGenerator;
    private UserService userService;

    @Autowired
    public DocumentManagementService(DocumentUploadClientApi documentUploadClient, AuthTokenGenerator authTokenGenerator,
                                     UserService userService) {
        this.documentUploadClient = documentUploadClient;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
    }

    URI uploadDocument(String authorisation, File doc) {
        try {
            MultipartFile file = new InMemoryMultipartFile(FILES_NAME, doc.getName(), APPLICATION_DOCX_VALUE, FileCopyUtils.copyToByteArray(doc));
            UploadResponse response = documentUploadClient.upload(
                    authorisation,
                    authTokenGenerator.generate(),
                    userService.getUserDetails(authorisation).getId(),
                    singletonList(file)
            );
            Document document = response.getEmbedded().getDocuments().stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new DocumentManagementException("Document management failed uploading file" + doc.getName()));

            log.info("Uploaded document successful");
            return URI.create(document.links.self.href);
        } catch (Exception ex) {
            throw new DocumentManagementException(String.format("Unable to upload document %s to document management",
                    doc.getName()), ex);
        }
    }

    private String generateDownloadableURL(URI documentSelf) {
        return documentSelf.getScheme() + "://localhost:3453" + documentSelf.getRawPath() + "/binary";
        //return documentSelf.getScheme() + "://" + documentSelf.getAuthority() + documentSelf.getRawPath() + "/binary";
    }

    String generateMarkupDocument(URI documentSelf) {
        return "<a target=\"_blank\" href=\"" + generateDownloadableURL(documentSelf) + "\">Document</a>";
    }

}
