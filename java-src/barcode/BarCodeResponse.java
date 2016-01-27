package barcode;

public class BarCodeResponse {
    private String encoding;
    private String textInfo;
    private String partial;

    public BarCodeResponse(String encoding, String textInfo, String partial) {
        this.encoding = encoding;
        this.textInfo = textInfo;
        this.partial = partial;
    }

    public String getEncoding() { return this.encoding; }
    public String getTextInfo() { return this.textInfo; }
    public String getPartial() { return this.partial; }
}
