package org.acme.cxf.soap;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.cxf.soap.service.Address;
import org.acme.cxf.soap.service.Contact;
import org.acme.cxf.soap.service.ContactService;
import org.acme.cxf.soap.service.ContactType;
import org.acme.cxf.soap.service.NoSuchContactException;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PojoClientTest extends BaseTest {

    protected ContactService createCXFClient() {
        String URL = getServerUrl() + "/cxf/services/contact";

        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
        factory.setServiceClass(ContactService.class);
        factory.setAddress(URL);

        return (ContactService) factory.create();
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

        cxfClient.addContact(createContact());
        Assertions.assertSame(1, cxfClient.getContacts().getContacts().size(), "We should have one contact.");

        Assertions.assertNotNull(cxfClient.getContact("Croway"), "We haven't found contact.");

        Assertions.assertThrows(NoSuchContactException.class, () -> cxfClient.getContact("Non existent"));
    }
}
