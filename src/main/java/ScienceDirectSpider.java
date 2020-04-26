import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class ScienceDirectSpider {
    private JTextField fieldHtml;
    private JTextArea log;
    private JButton btnParse;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0";


    private List<ScienceAuthorModel> authorList = new ArrayList<>();

    public void htmlSpider() {
        JFrame frame = new JFrame("Vena's html spider");
        frame.setLayout(new VFlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
                // 生成文件
                if (authorList.isEmpty()) {
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
            Row rowTitle = sheet.createRow(0);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("title");
            Cell cellAuthor = rowTitle.createCell(1);
            cellAuthor.setCellValue("author");
            Cell cellEmail = rowTitle.createCell(2);
            cellEmail.setCellValue("email");
            Cell cellHIndex = rowTitle.createCell(3);
            cellHIndex.setCellValue("h-index");
            Cell cellCitation = rowTitle.createCell(4);
            cellCitation.setCellValue("citations");
            for (int i = 1; i < authorList.size() + 1; i++) {
                ScienceAuthorModel model = authorList.get(i - 1);
                Row row = sheet.createRow(i);
                Cell dataTitle = row.createCell(0);
                dataTitle.setCellValue(model.getTitle());
                Cell dataAuthor = row.createCell(1);
                dataAuthor.setCellValue(model.getAuthor());
                Cell dataEmail = row.createCell(2);
                dataEmail.setCellValue(model.getEmail());
                Cell dataHIndex = row.createCell(3);
                dataHIndex.setCellValue(model.gethIndex());
                Cell dataCitation = row.createCell(4);
                dataCitation.setCellValue(model.getCitations());
            }
            File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
            String desktopPath = desktopDir.getAbsolutePath() + "/desktop";
            FileOutputStream out = new FileOutputStream(new File(desktopPath, "science_direct_emails.xlsx"));
            //最后要写入输出流
            wb.write(out);
            log.setText("excel文件生成完成，请在桌面上查看science_direct_emails.xlsx");
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
                    Document doc = Jsoup.connect(url)
                            .userAgent(USER_AGENT)
                            .get();
                    Elements aLiList = doc.body().getElementsByTag("li");
                    List<String> urlList = new ArrayList<>();
                    for (Element li : aLiList) {
                        Element a = null;
                        try {
                            a = li.getElementsByTag("dl").get(0)
                                    .getElementsByTag("dt").get(0)
                                    .getElementsByTag("h3").get(0)
                                    .getElementsByTag("a").get(0);
                        } catch (Exception e) {
                            continue;
                        }
                        if (a == null) {
                            continue;
                        }
                        String tUrl = a.attr("href");
                        if (tUrl == null || tUrl.isEmpty()) {
                            continue;
                        }
                        System.out.println(tUrl);
                        if (!tUrl.startsWith("http")) {
                            if (tUrl.startsWith("../../")) {
                                //上级目录地址
                                String subUrl = url.substring(0, url.lastIndexOf("/"));
                                int i = subUrl.lastIndexOf("/");
                                subUrl = subUrl.substring(0, i);
                                int j = subUrl.lastIndexOf("/");
                                tUrl = subUrl.substring(0, j)
                                        + tUrl.replace("../../", "/");
                            } else if (tUrl.startsWith("../")) {
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
                        //去掉重复的url
                        if (!urlList.contains(tUrl)) {
                            urlList.add(tUrl);
                            System.out.println(tUrl);
                        }
                    }
                    //进一步爬取已获取的二级url地址
                    parseSecondUrl(urlList);

                } catch (Exception e1) {
                    log.setText("网址解析失败！");
                    System.out.println(e1.getMessage());
                }
                btnParse.setEnabled(true);
            }
        });
        fixedThreadPool.shutdown();
    }

    private void parseSecondUrl(List<String> urlList) {
        int pro = 0;
        for (String url : urlList) {
            pro++;
            log.setText("正在解析,进度" + pro + "/" + urlList.size());
            try {

                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .get();

                //获取文章id
                String articleId = null;
                Elements metas = doc.head().getElementsByTag("meta");
                for (int j = 0; j < metas.size(); j++) {
                    Element meta = metas.get(j);
                    if ("citation_pii".equals(meta.attr("name"))) {
                        articleId = meta.attr("content");
                        break;
                    }
                }
                //获取文章标题
                Element article = doc.body().getElementsByTag("article").get(0);
                Element h1 = article.getElementsByTag("h1").get(0);
                Element span = h1.getElementsByTag("span").get(0);
                String title = span.text();
                System.out.println(title);
                Elements scripts = doc.getElementsByTag("script");
                Element script = null;
                for (Element s : scripts) {
                    if ("application/json".equals(s.attr("type"))) {
                        script = s;
                        break;
                    }
                }
                if (script != null) {
                    String authorJsonStr = script.html();
                    JSONObject objectJson = JSONObject.parseObject(authorJsonStr);
                    JSONArray jsonArrayContent = objectJson.getJSONObject("authors").getJSONArray("content");
                    if (jsonArrayContent.isEmpty()) {
                        throw new Exception("该文章无作者信息");
                    }
                    JSONObject content0 = jsonArrayContent.getJSONObject(0);
                    JSONArray authorsArray = content0.getJSONArray("$$");
                    //获取作者和邮箱
                    for (int i = 0; i < authorsArray.size(); i++) {
                        JSONObject authorObject = authorsArray.getJSONObject(i);
                        JSONArray authorInfoArray = authorObject.getJSONArray("$$");
                        StringBuilder authorName = new StringBuilder();
                        String authorEmail = "";
                        for (int j = 0; j < authorInfoArray.size(); j++) {
                            JSONObject authorInfo = authorInfoArray.getJSONObject(j);
                            String innerName = authorInfo.getString("#name");
                            if ("given-name".equals(innerName)) {
                                authorName.append(authorInfo.getString("_"));
                            }
                            if ("surname".equals(innerName)) {
                                authorName.append(" ").append(authorInfo.getString("_"));
                            }
                            if ("e-address".equals(innerName)) {
                                authorEmail = authorInfo.getJSONObject("$").getString("href").replace("mailto:", "");
                            }
                        }
                        ScienceAuthorModel model = new ScienceAuthorModel();
                        model.setTitle(title);
                        String authorNameStr = authorName.toString();
                        System.out.println(authorNameStr);
                        model.setAuthor(authorNameStr);
                        System.out.println(authorEmail);
                        model.setEmail(authorEmail);

                        //请求articles获取authorId
                        try {
                            String articlesUrl = "https://www.sciencedirect.com/sdfe/arp/pii/" + articleId + "/author/" + (i + 1) + "/articles";
                            Connection.Response res = Jsoup.connect(articlesUrl)
                                    .header("Content-Type", "application/json;charset=UTF-8")
                                    .ignoreContentType(true)
                                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")
                                    .execute();
                            String jsonArticles = res.body();
                            System.out.println(jsonArticles);
                            JSONObject jsonObject = JSONObject.parseObject(jsonArticles);
                            String authorId = jsonObject.getString("scopusAuthorId");
                            if (authorId != null && authorId.length() > 0) {
                                //有authorId,根据authorId去获取h-index、Citations
                                String hIndexUrl = "https://www.mendeley.com/profiles/" + authorId + "/preview/?sd=1&border=0";
                                Document docHIndex = Jsoup.connect(hIndexUrl)
                                        .userAgent(USER_AGENT)
                                        .get();
                                Elements lis = docHIndex.body().getElementsByTag("li");
                                if (lis.isEmpty()) {
                                    throw new Exception("该作者无h-index等信息");
                                }
                                String hIndex = lis.get(0).getElementsByTag("data").get(0).text();
                                if (hIndex.contains(",")) {
                                    hIndex = hIndex.replace(",", "");
                                }
                                System.out.println("h-index==>" + hIndex);
                                model.sethIndex(hIndex);
                                String citation = lis.get(1).getElementsByTag("data").get(0).text();
                                if (citation.contains(",")) {
                                    citation = citation.replace(",", "");
                                }
                                System.out.println("citation==>" + citation);
                                model.setCitations(citation);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (model.hasUniqueInfo()) {
                                authorList.add(model);
                            }
                            continue;
                        }
                        if (model.hasUniqueInfo()) {
                            authorList.add(model);
                        }
                    }
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        log.setText("解析完成");
    }

}
