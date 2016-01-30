package barcode;

public class BarCodeResponse {
    private String encoding;
    private String textInfo;
    private String partial;

    public BarCodeResponse() {}

    public void setEncoding(String val) { this.encoding = new String(val); }
    public void setTextInfo(String val) { this.textInfo = new String(val); }
    public void setPartial(String val) { this.partial = new String(val); }

    public String getEncoding() { return this.encoding; }
    public String getTextInfo() { return this.textInfo; }
    public String getPartial() { return this.partial; }
}
