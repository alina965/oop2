package common.responses;

public class XmlResponse extends ChatResponse {
    private final String xml;

    public XmlResponse(String xml) {
        super(null);
        this.xml = xml;
    }

    public String getXml() {
        return xml;
    }
}