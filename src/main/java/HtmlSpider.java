import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author namcooper
 * @create 2020-03-25 09:46
 */
public class HtmlSpider {
    private static HtmlSpider sInstance;
    private JTextField fieldHtml;
    private JTextArea log;
    private JButton btnParse;
    private List<String> emails = new ArrayList<>();

    private HtmlSpider() {
    }

    public static HtmlSpider getInstance() {
        if (sInstance == null) {
            sInstance = new HtmlSpider();
        }
        return sInstance;
    }

    public void htmlSpider() {
        JFrame frame = new JFrame("Vena's html spider");
        frame.setLayout(new VFlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.getColor("#F4F5F6"));
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new VFlowLayout());
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(Color.getColor("#F4F5F6"));
        area.setText("请输入网址：");
        fieldHtml = new JTextField();
        log = new JTextArea();
        log.setEditable(false);
        log.setBackground(Color.getColor("#F4F5F6"));

        panel.add(area);
        panel.add(fieldHtml);
        panel.add(log);
        frame.add(panel);

        JPanel btnParsePannel = new JPanel();
        btnParse = new JButton("解析网址");
        btnParse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = fieldHtml.getText();
                if (url != null && url.length() > 0) {
                    parseRootHtml(url);
                } else {
                    log.setText("请输入正确的网址！");
                }
            }
        });
        btnParse.setMargin(new Insets(10, 10, 10, 10));
        btnParsePannel.add(btnParse);
        frame.add(btnParsePannel);

        JPanel btnBuildPannel = new JPanel();
        JButton buildExcel = new JButton("生成文件");
        buildExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 2020/3/25 生成文件
                if (emails.isEmpty()) {
                    log.setText("无解析完成的数据~");
                } else {
                    buildExcel();
                }
            }
        });
        buildExcel.setMargin(new Insets(10, 10, 10, 10));
        btnBuildPannel.add(buildExcel);
        frame.add(btnBuildPannel);

        frame.pack();
        frame.setVisible(true);
        frame.setBounds(600, 400, 500, 400);
    }

    private void buildExcel() {
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet();
            for (int i = 0; i < emails.size(); i++) {
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellValue(emails.get(i));
            }
            File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
            String desktopPath = desktopDir.getAbsolutePath() + "/desktop";
            FileOutputStream out = new FileOutputStream(new File(desktopPath, "html_emails.xlsx"));
            //最后要写入输出流
            wb.write(out);
            log.setText("excel文件生成完成，请在桌面上查看html_emails.xlsx文件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseRootHtml(String url) {
        btnParse.setEnabled(false);
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    log.setText("正在解析" + url);
                    URL uUrl = new URL(url);
                    Document doc = Jsoup.connect(url).get();
                    Elements aTagList = doc.body().getElementsByTag("a");
                    List<String> urlList = new ArrayList<>();
                    for (Element element : aTagList) {
                        String tUrl = element.attr("href");
                        if (tUrl == null || tUrl.isEmpty()) {
                            continue;
                        }
                        System.out.println(tUrl);
                        if (!tUrl.startsWith("http")) {
                            if (tUrl.startsWith("../")) {
                                //上级目录地址
                                String subUrl = url.substring(0, url.lastIndexOf("/"));
                                int i = subUrl.lastIndexOf("/");
                                tUrl = subUrl.substring(0, i)
                                        + tUrl.replace("../", "/");
                            } else if (tUrl.startsWith("/")) {
                                //标识根目录开始
                                tUrl = uUrl.getProtocol() + "://" + uUrl.getHost() + tUrl;
                            } else {
                                //同级目录地址
                                String subUrl = url.substring(0, url.lastIndexOf("/"));
                                if (tUrl.startsWith("./")) {
                                    tUrl = subUrl + tUrl.replace("./", "/");
                                } else {
                                    tUrl = subUrl + "/" + tUrl;
                                }
                            }
                        }
                        //去掉重复的url以及可能存在的当前rootUrl
                        if (!url.equals(tUrl) && !urlList.contains(tUrl)) {
                            urlList.add(tUrl);
                            System.out.println(tUrl);
                        }
                    }
                    //进一步爬取已获取的二级url地址
                    parseSecondUrl(urlList);

                } catch (Exception e1) {
                    log.setText("网址解析失败！");
                }
                btnParse.setEnabled(true);
            }
        });
        fixedThreadPool.shutdown();
    }

    private void parseSecondUrl(List<String> urlList) {
        for (String url : urlList) {
            log.setText("正在解析" + url);
            try {
                Document doc = Jsoup.connect(url).get();
                String html = doc.toString();
                //根据正则，获取邮箱地址
                matchEmail(emails, html);
                //根据正则，获取可能存在的隐藏邮箱地址
                matchHiddenEmail(emails, html);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        log.setText("解析完成");
    }

    private void matchEmail(List<String> emails, String html) {
        String check = "[a-zA-Z0-9_-]+[ ]*@[ ]*[a-zA-Z0-9_-]+([.][a-zA-Z0-9_-]+)+";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(html);
        while (matcher.find()) {
            String group = matcher.group(0).replace(" ", "");
            if (!emails.contains(group)) {
                emails.add(group);
                System.out.println("Found value: " + group);
            }
        }
    }

    private void matchHiddenEmail(List<String> emails, String html) {
        //qty ▇ whu.edu.cn
        String check = "[a-zA-Z0-9_-]+[ ]*[▇#][ ]*[a-zA-Z0-9_-]+([.][a-zA-Z0-9_-]+)+";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(html);
        while (matcher.find()) {
            String group = matcher.group(0).replace("▇", "@");
            group = group.replace("#", "@");
            group = group.replace(" ", "");
            if (!emails.contains(group)) {
                emails.add(group);
                System.out.println("Found value: " + group);
            }
        }
    }
}
