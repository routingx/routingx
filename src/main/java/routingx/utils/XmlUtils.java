package routingx.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlUtils {

	public static String format(String xml) {
		return format(xml.getBytes(StandardCharsets.UTF_8));
	}

	public static String format(byte[] xmlBuffer) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new DOMSource(from(xmlBuffer)), new StreamResult(baos));
			return baos.toString();
		} catch (Throwable ex) {
			log.warn(ex.getMessage());
			return TextUtils.toString(xmlBuffer);
		} finally {
			try {
				baos.close();
			} catch (Throwable e) {
			}
		}
	}

	public static Document from(String xml) throws Exception {
		return from(xml.getBytes(StandardCharsets.UTF_8));
	}

	public static Document from(byte[] xmlBuffer) throws Exception {
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.parse(new InputSource(new ByteArrayInputStream(xmlBuffer)));
		return doc;
	}
}
