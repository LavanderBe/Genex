package Genex.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class QRCodeService {

    public WritableImage createQrImage(String content, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
            WritableImage image = new WritableImage(size, size);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    image.getPixelWriter().setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return image;
        } catch (WriterException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
