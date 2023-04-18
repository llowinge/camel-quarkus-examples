package org.acme.cxf.soap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.xml.ws.Service;
import org.acme.cxf.soap.service.Address;
import org.acme.cxf.soap.service.Contact;
import org.acme.cxf.soap.service.ContactService;
import org.acme.cxf.soap.service.ContactType;
import org.acme.cxf.soap.service.NoSuchContactException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PojoClientTest extends BaseTest {

    protected ContactService createCXFClient() {
        try {
            final URL serviceUrl = new URL(getServerUrl() + "/cxf/services/contact?wsdl");
            final QName qName = new QName(ContactService.TARGET_NS, ContactService.class.getSimpleName());
            final Service service = Service.create(serviceUrl, qName);
            return service.getPort(ContactService.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Contact createContact() {
        Contact contact = new Contact();
        contact.setName("Croway");
        contact.setType(ContactType.OTHER);
        Address address = new Address();
        address.setCity("Rome");
        address.setStreet("Test Street");
        contact.setAddress(address);

        return contact;
    }

    @Test
    public void testBasic() throws NoSuchContactException {
        ContactService cxfClient = createCXFClient();

        String servedWsdl = RestAssured.given()
                .get("/cxf/services/contact?wsdl")
                .then()
                .statusCode(200)
                .extract().body().asString();
        System.out.println(" ====== wsdl = \n\n" + servedWsdl);

        cxfClient.addContact(createContact());
        Assertions.assertSame(1, cxfClient.getContacts().getContacts().size(), "We should have one contact.");

        Assertions.assertNotNull(cxfClient.getContact("Croway"), "We haven't found contact.");

        Assertions.assertThrows(NoSuchContactException.class, () -> cxfClient.getContact("Non existent"));
    }
}
