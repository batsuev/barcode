package barcode;

public class BarCode {
    public static native void Create(String code, String encoding, BarCodeResponse res);

    public static void load() {
        System.load(System.getProperty("barcode.so"));
    }
}
