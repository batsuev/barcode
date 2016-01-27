package barcode;

public class BarCode {
    public static native BarCodeResponse Create(String code, String encoding);

    static {
        System.load(System.getProperty("barcode.so"));
    }
}
