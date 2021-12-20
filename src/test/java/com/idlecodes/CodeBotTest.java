package com.idlecodes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CodeBotTest {

    @org.junit.jupiter.api.Test
    void detectCorrespondingZoneOK1() throws IOException, AWTException, OCRException.OCRError, OCRException.ImageNotFound {
        IdleCodes.loadJarDll("/opencv_java454.dll");
        BufferedImage screen0 = ImageIO.read(new File(".//test//findok0.png"));
        BufferedImage screen1 = ImageIO.read(new File(".//test//findok1.png"));
        BufferedImage screenu0 = ImageIO.read(new File(".//test//findunlock0.png"));
        BufferedImage screenu2 = ImageIO.read(new File(".//test//findunlock2.png"));
        BufferedImage screenu1 = ImageIO.read(new File(".//test//findunlock1.png"));
        BufferedImage okButton = ImageIO.read(this.getClass().getResourceAsStream("/ok.png"));
        BufferedImage unlockButton = ImageIO.read(this.getClass().getResourceAsStream("/unlock.png"));
        CodeBot cb = new CodeBot(false);
        //ok
        cb.detectCorrespondingZone("ok", okButton, screen1, true);
        assertTrue(cb.x > 100 && cb.y > 100, "Button OK found");
        try {
            cb.detectCorrespondingZone("ok", okButton, screen0, false);
        } catch (Exception e) {
            assertTrue("Cannot find matching zone".equals(e.getMessage()) || "Image not found".equals(e.getMessage()));
        }
        // Unlock
        try {
            cb.detectCorrespondingZone("unlock", unlockButton, screenu0, true);
        } catch (Exception e) {
            assertTrue("Cannot find matching zone".equals(e.getMessage()) || "Image not found".equals(e.getMessage()));
        }
        try {
            cb.detectCorrespondingZone("unlock", unlockButton, screenu2, true);
        } catch (Exception e) {
            assertTrue("Cannot find matching zone".equals(e.getMessage()) || "Image not found".equals(e.getMessage()));
        }
        cb.detectCorrespondingZone("unlock", unlockButton, screenu1, true);
        assertTrue(cb.x > 10 && cb.y > 100, "Button Unlock found");
    }
}