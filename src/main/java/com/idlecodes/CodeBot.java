package com.idlecodes;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS;

public class CodeBot {
    private int x;
    private int y;


    private boolean debug = false;
    private int wh, ww, wx, wy;
    private Robot robot;
    // assumes the current class is called MyLogger
    private final static Logger logger = Logger.getLogger(CodeBot.class.getName());

    public void loadJarDll(String name) throws IOException {
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        try (FileOutputStream fos = new FileOutputStream(temp); InputStream in = this.getClass().getResourceAsStream(name)) {
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        System.load(temp.getAbsolutePath());
    }

    public void openMenu() {
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_O);
        robot.delay(60);
        robot.keyRelease(KeyEvent.VK_O);
        robot.delay(900);
    }

    public boolean enterCode() throws AWTException, IOException, OCRException.OCRError {
        robot.mouseMove(x, y);
        robot.delay(200);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(40);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(1100);

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
        try {
            detectCorrespondingZone("ok", false);
        } catch (OCRException.ImageNotFound e) {
            robot.delay(2000);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(150);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(1000);
            robot.keyPress(KeyEvent.VK_SPACE);
            robot.delay(50);
            robot.keyRelease(KeyEvent.VK_SPACE);
            robot.delay(1700);
            return true;
        }
        robot.delay(500);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(900);
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
        robot.delay(700);
        return false;
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

    public CodeBot(boolean debug) throws AWTException, OCRException.GameNotFound {
        this.debug = debug;
        User32 user32 = User32.instance;
        WinDef.HWND hWnd = user32.FindWindow(null, "Idle Champions");
        user32.ShowWindow(hWnd, User32.SW_SHOW);
        user32.SetForegroundWindow(hWnd);
        int[] rect = new int[]{0, 0, 0, 0};
        if (User32.instance.GetWindowRect(hWnd, rect) == 0) {
            System.out.println("Can't find Idle Champions window. Make sure the game is running and try again later");
            throw new OCRException.GameNotFound("No Idle Champions window found");
        }
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

    public void writeComparisonPictureToFile(String filePath, Mat img) {
        if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".png")) {
            Imgcodecs.imwrite(filePath, img);
        } else {
            return;
        }
    }

    public void detectCorrespondingZone(String buttonName) throws AWTException, IOException, OCRException.ImageNotFound, OCRException.OCRError {
        detectCorrespondingZone(buttonName, false);
    }

    public void detectCorrespondingZone(String buttonName, boolean savePos) throws AWTException, IOException, OCRException.ImageNotFound, OCRException.OCRError {
        Rectangle scr = new Rectangle(this.wx, this.wy + wh / 2, this.ww / 2 + this.ww / 4, this.wh / 2);
        robot.mouseMove(1, 1);
        Mat objectImageMat = bufferedImage2Mat(ImageIO.read(this.getClass().getResourceAsStream("/" + buttonName + ".png")));
        BufferedImage image = new Robot().createScreenCapture(scr);
        Mat sceneImageMat = bufferedImage2Mat(image);
        // 8000, (float) 1.089,50, 16, 3, 3, ORB.HARRIS_SCORE, 11, 16
        ORB surf = ORB.create(2000, (float) 1.1, 20, 50, 3, 3, ORB.HARRIS_SCORE, 50, 50);
        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        Mat objectDescriptor = new Mat();
        Mat sceneDescriptor = new Mat();
        surf.detectAndCompute(objectImageMat, new Mat(), objectKeyPoints, objectDescriptor);
        surf.detectAndCompute(sceneImageMat, new Mat(), sceneKeyPoints, sceneDescriptor);
        // http://stackoverflow.com/questions/29828849/flann-for-opencv-java
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch matches = new MatOfDMatch();

        if (objectKeyPoints.toList().isEmpty()) {
            throw new OCRException.OCRError("No keypoints in object");
        }
        if (sceneKeyPoints.toList().isEmpty()) {
            throw new OCRException.OCRError("No keypoints in scene");
        }
        matcher.match(objectDescriptor, sceneDescriptor, matches);

        double maxDist = 0;
        double minDist = 1000;

        for (int i = 0; i < objectDescriptor.rows(); i++) {
            double dist = matches.toList().get(i).distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        if (debug) {
            logger.warning("-- Max dist : " + maxDist);
            logger.warning(MessageFormat.format("-- Min dist : {0}", minDist));
        }
        LinkedList<DMatch> goodMatches = new LinkedList<>();
        MatOfDMatch gm = new MatOfDMatch();
        for (int i = 0; i < objectDescriptor.rows(); i++) {
            if (matches.toList().get(i).distance < 30) { //16
                goodMatches.addLast(matches.toList().get(i));
            }
        }
        gm.fromList(goodMatches);
        // DRAWING OUTPUT
        Mat imgMatch = new Mat();
        Features2d.drawMatches(objectImageMat, objectKeyPoints, sceneImageMat, sceneKeyPoints,
                gm, imgMatch, Scalar.all(-1), Scalar.all(0), new MatOfByte(), Features2d.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS);

        if (goodMatches.isEmpty()) {
            if (debug)
                logger.severe("Cannot find matching zone");
            throw new OCRException.ImageNotFound("Cannot find matching zone");
        }

        LinkedList<Point> objList = new LinkedList<>();
        LinkedList<Point> sceneList = new LinkedList<>();
        List<KeyPoint> objectKeyPointsList = objectKeyPoints.toList();
        List<KeyPoint> sceneKeyPointsList = sceneKeyPoints.toList();
        for (int i = 0; i < goodMatches.size(); i++) {
            objList.addLast(objectKeyPointsList.get(goodMatches.get(i).queryIdx).pt);
            sceneList.addLast(sceneKeyPointsList.get(goodMatches.get(i).trainIdx).pt);
        }
        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);
        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);

        // Calib3d.RANSAC could be used instead of 0
        try {
            Mat hg = Calib3d.findHomography(obj, scene, 0, 5);

            Mat objectCorners = new Mat(4, 1, CvType.CV_32FC2);
            Mat sceneCorners = new Mat(4, 1, CvType.CV_32FC2);
            objectCorners.put(0, 0, new double[]{0, 0});
            objectCorners.put(1, 0, new double[]{objectImageMat.cols(), 0});
            objectCorners.put(2, 0, new double[]{objectImageMat.cols(), objectImageMat.rows()});
            objectCorners.put(3, 0, new double[]{0, objectImageMat.rows()});

            Core.perspectiveTransform(objectCorners, sceneCorners, hg);

            // points of object
            Point po1 = new Point(objectCorners.get(0, 0));
            Point po2 = new Point(objectCorners.get(1, 0));
            Point po3 = new Point(objectCorners.get(2, 0));
            Point po4 = new Point(objectCorners.get(3, 0));

            // point of object in scene
            Point p1 = new Point(sceneCorners.get(0, 0)); // top left
            Point p2 = new Point(sceneCorners.get(1, 0)); // top right
            Point p3 = new Point(sceneCorners.get(2, 0)); // bottom right
            Point p4 = new Point(sceneCorners.get(3, 0)); // bottom left
            if (debug) {
                logger.info(po1.toString());
                logger.info(po2.toString());
                logger.info(po3.toString());
                logger.info(po4.toString());
                logger.info(p1.toString()); // top left
                logger.info(p2.toString()); // top right
                logger.info(p3.toString()); // bottom right
                logger.info(p4.toString()); // bottom left
            }

            if (savePos) {
                this.x = (int) ((p1.x + p2.x + p3.x + p4.x) / 4 - 40) + this.wx;
                this.y = (int) ((p1.y + p2.y + p3.y + p4.y) / 4 - 40 + this.wh / 2 + this.wy);
                if (debug)
                    logger.info("x=" + x + "; y=" + y);
            }
            if (debug) {
                try {
                    // translate corners
                    p1.set(new double[]{p1.x + objectImageMat.cols(), p1.y});
                    p2.set(new double[]{p2.x + objectImageMat.cols(), p2.y});
                    p3.set(new double[]{p3.x + objectImageMat.cols(), p3.y});
                    p4.set(new double[]{p4.x + objectImageMat.cols(), p4.y});

                    Imgproc.line(imgMatch, p1, p2, new Scalar(0, 255, 0), 1);
                    Imgproc.line(imgMatch, p2, p3, new Scalar(0, 255, 0), 1);
                    Imgproc.line(imgMatch, p3, p4, new Scalar(0, 255, 0), 1);
                    Imgproc.line(imgMatch, p4, p1, new Scalar(0, 255, 0), 1);

                    imshow(imgMatch);
                } catch (Exception e) {
                    logger.severe(e.toString());
                }
            }
        } catch (Exception e) {
            throw new OCRException.ImageNotFound("Image not found");
        }
    }

    public void imshow(Mat src) {
        BufferedImage bufImage = null;
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", src, matOfByte);
            byte[] byteArray = matOfByte.toArray();
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);

            JFrame frame = new JFrame("Image");
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean findButton() throws AWTException, IOException, OCRException.OCRError {
        loadJarDll("/opencv_java454.dll");
        String buttonName = "unlock";
        try {
            detectCorrespondingZone(buttonName, true);
        } catch (OCRException.ImageNotFound e) {
            openMenu();
            try {
                detectCorrespondingZone(buttonName, true);
            } catch (OCRException.ImageNotFound ex) {
                System.out.println("Can't find \"Unlock a Locked Chest\" button. Make sure the game is opened.");
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }


}
