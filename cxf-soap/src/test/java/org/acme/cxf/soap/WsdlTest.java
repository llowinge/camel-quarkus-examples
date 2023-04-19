package org.acme.cxf.soap;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WsdlTest {

    @Test
    public void wsdlCustomers() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        getAndSaveServed("customers");
    }

    @Test
    public void wsdlContact() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        getAndSaveServed("contact");
    }

    protected String getAndSaveServed(String service) throws IOException {
        String servedWsdl = RestAssured.given()
                .get("/cxf/services/" + service + "?wsdl")
                .then()
                .statusCode(200)
                .extract().body().asString();
        servedWsdl = normalizeNsPrefixes(servedWsdl);
        String mode = this.getClass().getSimpleName().endsWith("IT") ? "native" : "jvm";
        Path servedPath = Paths.get("target/WsdlTest/" + service + "-served-" + mode + ".wsdl")
                .toAbsolutePath();
        Files.createDirectories(servedPath.getParent());
        Files.write(servedPath, servedWsdl.getBytes(StandardCharsets.UTF_8));
        return servedWsdl;
    }

    protected String normalizeNsPrefixes(String servedWsdl) {
        return servedWsdl.replace("xmlns:ns1=\"http://schemas.xmlsoap.org/soap/http\"", "");
    }

    static void save(Document doc, Path path) throws TransformerException, IOException {
        Files.createDirectories(path.getParent());
        Transformer t = TransformerFactory.newDefaultInstance().newTransformer();
        t.transform(new DOMSource(doc), new StreamResult(path.toFile()));
    }

    static Document parse(DocumentBuilder db, String wsdlDoc) throws SAXException, IOException {
        Document doc = db.parse(new InputSource(new StringReader(wsdlDoc)));

        /*
         * There is some default :9090 location in the generated WSDL so we remove the whole address node from both
         */
        NodeList adrNodes = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/soap/", "address");
        List<Node> adrNodesList = new ArrayList<>();
        for (int i = 0; i < adrNodes.getLength(); i++) {
            adrNodesList.add(adrNodes.item(i));
        }
        for (Node node : adrNodesList) {
            node.getParentNode().removeChild(node);
        }
        doc.normalizeDocument();
        return doc;
    }

}
