package com.idlecodes;

public class OCRException {
    public static class ImageNotFound extends BaseException {
        private static final long serialVersionUID = 1555714415375055302L;

        public ImageNotFound(String msg) {
            super(msg);
        }
    }
    public static class OCRError extends BaseException {
        private static final long serialVersionUID = 2555714415375055302L;

        public OCRError(String msg) {
            super(msg);
        }
    }
    public static class GameNotFound extends BaseException {
        private static final long serialVersionUID = 3555714415375055302L;

        public GameNotFound(String msg) {
            super(msg);
        }
    }

}
