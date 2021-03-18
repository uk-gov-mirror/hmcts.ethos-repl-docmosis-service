package uk.gov.hmcts.ethos.replacement.docmosis;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpHeaders;
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

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "idamApi_oidc", port = "5000")
@ContextConfiguration(classes = DocmosisApplication.class)
@TestPropertySource(
        locations = {"classpath:application.properties"},
        properties = {"idam.api.url=localhost:5000"}
)
public class IdamApiConsumerTest {

    @Autowired
    IdamApi idamApi;

    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";

    @Pact(provider = "idamApi_oidc", consumer = "ethos_replDocmosisService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        return builder
                .given("userinfo is requested")
                .uponReceiving("a request for a user")
                .path("/o/userinfo")
                .method("GET")
                .matchHeader(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .willRespondWith()
                .status(HttpStatus.SC_OK)
                .body(createUserDetailsResponse())
                .toPact();
    }

    private PactDslJsonBody createUserDetailsResponse() {
        return new PactDslJsonBody()
                .stringType("uid", "1111-2222-3333-4567")
                .stringValue("sub", "mail@mail.com")
                .stringValue("givenName", "Mike")
                .stringValue("familyName", "Jordan")
                .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("role"), 1)
                .stringType("IDAM_ADMIN_USER");
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPactResponse() {
        UserDetails details = idamApi.retrieveUserDetails(AUTH_TOKEN);
        Assertions.assertEquals("mail@mail.com", details.getEmail());
    }

//    @Test
//    @PactTestFor(pactMethod = "generatePactFragment")
//    public void verifyPactResponse() {
//        UserDetails details = getUserDetails();
//        Assertions.assertEquals("mail@mail.com", details.getEmail());
//    }
//
//    private static UserDetails getUserDetails() {
//        UserDetails userDetails = new UserDetails();
//        userDetails.setUid("id");
//        userDetails.setEmail("mail@mail.com");
//        userDetails.setFirstName("Mike");
//        userDetails.setLastName("Jordan");
//        userDetails.setRoles(Collections.singletonList("role"));
//        return userDetails;
//    }
}
