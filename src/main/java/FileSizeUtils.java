import java.text.DecimalFormat;

/**
 * @author namcooper
 * @create 2020-03-21 17:09
 */
public class FileSizeUtils {
    private FileSizeUtils() {
    }

    private static final long KB = 1024;
    private static final long MB = KB * 1024;

    public static String getFileSize(long bit) {
        if (bit < KB) {
            return bit + "B";
        }
        if (bit < MB) {
            return getDecimalTwoNum(bit * 1f / KB) + "KB";
        }
        return getDecimalTwoNum(bit * 1f / MB) + "MB";
    }

    /**
     * 将传入的double数保留两位小数后返回
     */
    private static String getDecimalTwoNum(double num) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(num);
    }
}
