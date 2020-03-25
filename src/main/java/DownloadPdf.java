
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadPdf {
    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void downLoadByUrl(String urlStr, String fileName,
                                     String savePath, OnDownloadListener listener) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 设置超时间为3秒
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestProperty("Accept-Encoding", "*");
        // 得到输入流
        InputStream inputStream = conn.getInputStream();
        // 获取自己数组
        long contentLengthLong = conn.getContentLengthLong();
        byte[] getData = readInputStream(inputStream, contentLengthLong, listener);
        // 文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        System.out.println("info:" + url + " download success");

    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream, long contentLenght, OnDownloadListener listener)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long sum = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
            sum += len;
            listener.onProgress(sum);
        }
        bos.close();
        return bos.toByteArray();
    }

    public interface OnDownloadListener {
        void onProgress(long sum);
    }
}

