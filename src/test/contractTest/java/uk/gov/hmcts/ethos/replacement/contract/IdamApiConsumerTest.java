package uk.gov.hmcts.ethos.replacement.contract;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.net.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest.getUserDetails;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@PactFolder("pacts")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "idamApi_oidc", port = "8893")
@ContextConfiguration(classes = {IdamApiConsumerApplication.class})
@TestPropertySource(properties = {"idam.api.url=http://localhost:8893"})
public class IdamApiConsumerTest {
    @Autowired
    IdamApi idamApi;
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";

    @Pact(provider = "idamApi_oidc", consumer = "ethos_replDocmosisService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        return builder
                .given("userinfo is requested")
                .uponReceiving("a request for user info")
                .path("/o/userinfo")
                .method("GET")
                .matchHeader(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .willRespondWith()
                .status(HttpStatus.SC_OK)
                .body(createUserInfoResponse())
                .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPactFragment() {
        UserDetails userDetails = idamApi.retrieveUserDetails(AUTH_TOKEN);
//        UserDetails userDetails = getUserDetails();
        Assertions.assertEquals("mail@mail.com", userDetails.getEmail());
    }

    private PactDslJsonBody createUserInfoResponse() {
        return new PactDslJsonBody()
            .stringType("uid", "1111-2222-3333-4567")
            .stringValue("sub", "mail@mail.com")
            .stringValue("givenName", "Mike")
            .stringValue("familyName", "Jordan")
            .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("role"), 1)
            .stringType("IDAM_ADMIN_USER");
    }

}

