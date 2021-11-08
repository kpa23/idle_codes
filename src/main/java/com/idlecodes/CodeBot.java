package com.idlecodes;

import com.sun.jna.Native;

import java.io.*;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import static com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_TM_SQDIFF_NORMED;


public class CodeBot {
    private int x;
    private int y;
    private int wh, ww, wx, wy;
    private Robot robot;
    private static final double THRESHOLD = 0.05;


    public void loadJarDll(String name) throws IOException {
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        try (FileOutputStream fos = new FileOutputStream(temp); InputStream in = this.getClass().getResourceAsStream(name)) {
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            System.load(temp.getAbsolutePath());
        }
    }

    public void openMenu() {
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_O);
        robot.delay(60);
        robot.keyRelease(KeyEvent.VK_O);
        robot.delay(900);
    }

    public boolean enterCode() throws AWTException, IOException {
        robot.mouseMove(x + wx + 10, y + wy + 10);
        robot.delay(200);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(40);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(1400);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.delay(50);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_V);
        robot.delay(500);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(1600);
        Core.MinMaxLocResult mmr;
        mmr = checkButton("ok");
        if (mmr.minVal < THRESHOLD) {
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_ENTER);
            robot.delay(900);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            robot.delay(700);
            return false;
        } else {
            robot.delay(2000);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(700);
            return true;
        }
    }


    public interface User32 extends StdCallLibrary {
        @Deprecated
        User32 instance = (User32) Native.loadLibrary("user32", User32.class, DEFAULT_OPTIONS);

        boolean ShowWindow(WinDef.HWND hWnd, int nCmdShow);

        boolean SetForegroundWindow(WinDef.HWND hWnd);

        WinDef.HWND FindWindow(String winClass, String title);

        int GetWindowRect(WinDef.HWND handle, int[] rect);

        int SW_SHOW = 1;
    }

    public CodeBot() throws AWTException {
        User32 user32 = User32.instance;
        WinDef.HWND hWnd = user32.FindWindow(null, "Idle Champions");
        user32.ShowWindow(hWnd, User32.SW_SHOW);
        user32.SetForegroundWindow(hWnd);
        int[] rect = new int[]{0, 0, 0, 0};
        int result = User32.instance.GetWindowRect(hWnd, rect);
        wx = rect[0];
        wy = rect[1];
        wh = rect[3] - rect[1];
        ww = rect[2] - rect[0];
        this.robot = new Robot();
    }

    public static Mat bufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
    }

    private Core.MinMaxLocResult checkButton(String buttonName) throws AWTException, IOException {
        robot.mouseMove(10, 10);
        robot.delay(500);
        Rectangle scr = new Rectangle(this.wx, this.wy, this.ww, this.wh);
        Mat templ = bufferedImage2Mat(ImageIO.read(this.getClass().getResourceAsStream("/" + buttonName + ".png")));
        BufferedImage image = new Robot().createScreenCapture(scr);
        Mat img = bufferedImage2Mat(image);
        // / Create the result matrix
        int resultCols = img.cols() - templ.cols() + 1;
        int resultRows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);
        // / Do the Matching and Normalize
        Imgproc.matchTemplate(img,
                templ,
                result,
                CV_TM_SQDIFF_NORMED);
        Imgproc.threshold(result, result, 0.1, 1.0, Imgproc.THRESH_TOZERO);
        return Core.minMaxLoc(result);
    }

    public boolean findButton() throws AWTException, IOException {
        loadJarDll("/opencv_java453.dll");

        Core.MinMaxLocResult mmr;
        mmr = checkButton("unlock");
        if (mmr.minVal > THRESHOLD) {
            openMenu();
            mmr = checkButton("unlock");
        }
        Point matchLoc = mmr.minLoc;
        if (mmr.minVal < THRESHOLD) {
            this.x = (int) matchLoc.x;
            this.y = (int) matchLoc.y;
            return true;
        } else {
            System.out.println("Can't find \"Unlock a Locked Chest\" button. Make sure the game is opened.");
            return false;
        }
    }


}
