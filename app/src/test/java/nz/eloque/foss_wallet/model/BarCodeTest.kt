package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets

class BarCodeTest {

    @Test
    fun testIs1dForQRCode() {
        val barcode = BarCode(
            BarcodeFormat.QR_CODE,
            "test message",
            StandardCharsets.UTF_8,
            "alt text"
        )
        assertFalse("QR Code should be 2D", barcode.is1d())
    }

    @Test
    fun testIs1dForCode128() {
        val barcode = BarCode(
            BarcodeFormat.CODE_128,
            "test message",
            StandardCharsets.UTF_8,
            "alt text"
        )
        assertTrue("CODE_128 should be 1D", barcode.is1d())
    }

    @Test
    fun testIs1dForAztec() {
        val barcode = BarCode(
            BarcodeFormat.AZTEC,
            "test message",
            StandardCharsets.UTF_8,
            null
        )
        assertFalse("AZTEC should be 2D", barcode.is1d())
    }

    @Test
    fun testIs1dForEAN13() {
        val barcode = BarCode(
            BarcodeFormat.EAN_13,
            "1234567890123",
            StandardCharsets.UTF_8,
            null
        )
        assertTrue("EAN_13 should be 1D", barcode.is1d())
    }

    @Test
    fun testIs1dForPDF417() {
        val barcode = BarCode(
            BarcodeFormat.PDF_417,
            "test message",
            StandardCharsets.UTF_8,
            null
        )
        assertTrue("PDF_417 should be 1D", barcode.is1d())
    }

    @Test
    fun testIs1dForDataMatrix() {
        val barcode = BarCode(
            BarcodeFormat.DATA_MATRIX,
            "test message",
            StandardCharsets.UTF_8,
            null
        )
        assertFalse("DATA_MATRIX should be 2D", barcode.is1d())
    }

    @Test
    fun testToJsonWithAltText() {
        val barcode = BarCode(
            BarcodeFormat.QR_CODE,
            "test message",
            StandardCharsets.UTF_8,
            "alternative text"
        )
        
        val json = barcode.toJson()
        
        assertEquals("QR_CODE", json.getString("format"))
        assertEquals("test message", json.getString("message"))
        assertEquals("alternative text", json.getString("altText"))
    }

    @Test
    fun testToJsonWithoutAltText() {
        val barcode = BarCode(
            BarcodeFormat.CODE_128,
            "12345",
            StandardCharsets.UTF_8,
            null
        )
        
        val json = barcode.toJson()
        
        assertEquals("CODE_128", json.getString("format"))
        assertEquals("12345", json.getString("message"))
        assertNull(json.opt("altText"))
    }

    @Test
    fun testFromJsonWithAltText() {
        val json = JSONObject().apply {
            put("format", "QR_CODE")
            put("message", "test message")
            put("messageEncoding", "UTF-8")
            put("altText", "alt")
        }
        
        val barcode = BarCode.fromJson(json)
        
        assertEquals(BarcodeFormat.QR_CODE, barcode.javaClass.getDeclaredField("format").let {
            it.isAccessible = true
            it.get(barcode)
        })
        assertEquals("alt", barcode.altText)
    }

    @Test
    fun testFromJsonWithoutAltText() {
        val json = JSONObject().apply {
            put("format", "CODE_128")
            put("message", "test")
            put("messageEncoding", "UTF-8")
        }
        
        val barcode = BarCode.fromJson(json)
        
        assertNull(barcode.altText)
    }

    @Test
    fun testFromJsonWithDefaultEncoding() {
        val json = JSONObject().apply {
            put("format", "QR_CODE")
            put("message", "test")
        }
        
        val barcode = BarCode.fromJson(json)
        
        // Should use fallback charset
        assertNotNull(barcode)
    }

    @Test
    fun testFormatFromStringPDF417() {
        val format = BarCode.formatFromString("PKBarcodeFormatPDF417")
        assertEquals(BarcodeFormat.PDF_417, format)
    }

    @Test
    fun testFormatFromStringAztec() {
        val format = BarCode.formatFromString("PKBarcodeFormatAztec")
        assertEquals(BarcodeFormat.AZTEC, format)
    }

    @Test
    fun testFormatFromStringCode128() {
        val format = BarCode.formatFromString("PKBarcodeFormatCode128")
        assertEquals(BarcodeFormat.CODE_128, format)
    }

    @Test
    fun testFormatFromStringCode39() {
        val format = BarCode.formatFromString("PKBarcodeFormatCode39")
        assertEquals(BarcodeFormat.CODE_39, format)
    }

    @Test
    fun testFormatFromStringCode93() {
        val format = BarCode.formatFromString("PKBarcodeFormatCode93")
        assertEquals(BarcodeFormat.CODE_93, format)
    }

    @Test
    fun testFormatFromStringUnknown() {
        val format = BarCode.formatFromString("UnknownFormat")
        assertEquals(BarcodeFormat.QR_CODE, format)
    }

    @Test
    fun testFormatFromStringEmpty() {
        val format = BarCode.formatFromString("")
        assertEquals(BarcodeFormat.QR_CODE, format)
    }

    @Test
    fun testEqualsWithSameValues() {
        val barcode1 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        val barcode2 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        
        assertEquals(barcode1, barcode2)
    }

    @Test
    fun testEqualsWithDifferentMessages() {
        val barcode1 = BarCode(
            BarcodeFormat.QR_CODE,
            "message1",
            StandardCharsets.UTF_8,
            "alt"
        )
        val barcode2 = BarCode(
            BarcodeFormat.QR_CODE,
            "message2",
            StandardCharsets.UTF_8,
            "alt"
        )
        
        assertNotEquals(barcode1, barcode2)
    }

    @Test
    fun testEqualsWithDifferentFormats() {
        val barcode1 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        val barcode2 = BarCode(
            BarcodeFormat.CODE_128,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        
        assertNotEquals(barcode1, barcode2)
    }

    @Test
    fun testEqualsWithDifferentAltText() {
        val barcode1 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt1"
        )
        val barcode2 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt2"
        )
        
        assertNotEquals(barcode1, barcode2)
    }

    @Test
    fun testEqualsWithNullAltText() {
        val barcode1 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            null
        )
        val barcode2 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            null
        )
        
        assertEquals(barcode1, barcode2)
    }

    @Test
    fun testHashCodeConsistency() {
        val barcode = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        
        val hash1 = barcode.hashCode()
        val hash2 = barcode.hashCode()
        
        assertEquals(hash1, hash2)
    }

    @Test
    fun testHashCodeForEqualObjects() {
        val barcode1 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        val barcode2 = BarCode(
            BarcodeFormat.QR_CODE,
            "message",
            StandardCharsets.UTF_8,
            "alt"
        )
        
        assertEquals(barcode1.hashCode(), barcode2.hashCode())
    }

    @Test
    fun testEncodeAsBitmapProducesValidBitmap() {
        val barcode = BarCode(
            BarcodeFormat.QR_CODE,
            "test",
            StandardCharsets.UTF_8,
            null
        )
        
        val bitmap = barcode.encodeAsBitmap(100, 100, false)
        
        assertNotNull(bitmap)
        assertEquals(100, bitmap.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun testEncodeAsBitmapWithDifferentSizes() {
        val barcode = BarCode(
            BarcodeFormat.QR_CODE,
            "test",
            StandardCharsets.UTF_8,
            null
        )
        
        val bitmap1 = barcode.encodeAsBitmap(50, 50, false)
        val bitmap2 = barcode.encodeAsBitmap(200, 200, false)
        
        assertEquals(50, bitmap1.width)
        assertEquals(50, bitmap1.height)
        assertEquals(200, bitmap2.width)
        assertEquals(200, bitmap2.height)
    }
}