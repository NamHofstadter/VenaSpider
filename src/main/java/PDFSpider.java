import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.helper.StringUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author namcooper
 * @create 2020-03-25 09:46
 */
public class PDFSpider {

    private JTextArea textFilePath;
    private JButton btnParse;
    private JTextArea textProgress;
    private JTextArea logArea;
    private String filePath;

    public void pdfSpider() {
        JFrame frame = new JFrame("Vena's pdf spider");
        frame.setLayout(new VFlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setBackground(Color.getColor("#F4F5F6"));
        frame.setResizable(false);

        //todo 增加按钮元素
        JPanel btnPanel = new JPanel();
        JButton btn = new JButton("选择文件");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 2020/3/21 选择文件
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                FileFilter excelFilter = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        String name = f.getName();
                        return f.isDirectory() || name.toLowerCase().endsWith(".xls")
                                || name.toLowerCase().endsWith(".xlsx");
                    }

                    @Override
                    public String getDescription() {
                        return "*.xls;*.xlsx";
                    }
                };
                chooser.addChoosableFileFilter(excelFilter);
                chooser.setFileFilter(excelFilter);
                chooser.showDialog(new JLabel(), "选择");
                File file = chooser.getSelectedFile();
                filePath = file.getAbsolutePath();
                textFilePath.setText(filePath);
                btnParse.setEnabled(filePath != null && file.length() > 0);
            }
        });
        btn.setMargin(new Insets(10, 10, 10, 10));
        btnPanel.add(btn);
        frame.add(btnPanel);

        //todo 增加文件地址选择元素
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new VFlowLayout());
        JTextArea textArea = new JTextArea();
        textArea.setBackground(Color.getColor("#F4F5F6"));
        textArea.setText("当前选择文件地址：");
        filePanel.add(textArea);
        textFilePath = new JTextArea();
        textFilePath.setBackground(Color.getColor("#F4F5F6"));
        textFilePath.setText("");
        filePanel.add(textFilePath);
        textProgress = new JTextArea();
        textProgress.setBackground(Color.getColor("#F4F5F6"));
        textProgress.setText("当前处理进度：0/0");
        filePanel.add(textProgress);
        logArea = new JTextArea();
        logArea.setBackground(Color.getColor("#F4F5F6"));
        logArea.setText("");
        filePanel.add(logArea);

        frame.add(filePanel);

        //todo 增加开始解析按钮
        JPanel parseBtnPanel = new JPanel();
        btnParse = new JButton("开始解析");
        btnParse.setEnabled(false);
        btnParse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        btn.setEnabled(false);
                        btnParse.setEnabled(false);
                        // 解析excel文件
                        logArea.setText("开始解析excel文件");
                        java.util.List<Map<String, String>> list = parseExcel(filePath);
                        logArea.setText("excel文件解析完成，Spider启动");
                        textProgress.setText("当前处理进度：0/" + list.size());
                        // 读取link地址，并解析pdf,生成新的集合数据
                        java.util.List<Map<String, String>> newList = parseLink(list);
                        logArea.setText("正在向excel中写入email信息");
                        //向原excel中写入新的列
                        writeEmail2Excel(filePath, newList);
                        logArea.setText("email信息写入完成，请重新打开原excel文件查看");

                        btnParse.setEnabled(true);
                        btn.setEnabled(true);

                    }
                });
                fixedThreadPool.shutdown();

            }
        });
        btnParse.setMargin(new Insets(10, 10, 10, 10));
        parseBtnPanel.add(btnParse);
        frame.add(parseBtnPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
        frame.setBounds(600, 400, 500, 400);
    }

    private void writeEmail2Excel(String excelFilePath, java.util.List<Map<String, String>> newList) {
        try {
            File file = new File(excelFilePath);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            Workbook wb = new XSSFWorkbook(in);//获取excel
            //获取第一个sheet
            Sheet sheet = wb.getSheetAt(0);
            //获取最大行数
            int rownum = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rownum; i++) {
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(4);
                if (cell == null) {
                    cell = row.createCell(4);
                }
                cell.setCellType(CellType.STRING);
                cell.setCellValue(newList.get(i - 1).get("email"));
            }
            FileOutputStream out = new FileOutputStream(excelFilePath);
            wb.write(out);//最后要写入输出流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private java.util.List<Map<String, String>> parseLink(java.util.List<Map<String, String>> list) {
        java.util.List<Map<String, String>> newList = new ArrayList<>();

        File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath() + "/desktop/pdfs";
        File pdfsDir = new File(desktopPath);
        if (!pdfsDir.exists()) {
            pdfsDir.mkdir();
        }
        System.out.println(desktopPath);
        for (int i = 0; i < list.size(); i++) {
            textProgress.setText("当前处理进度：" + (i + 1) + "/" + list.size());
            Map<String, String> map = list.get(i);
            String link = map.get("link");
            if (StringUtil.isBlank(link)) {
                map.put("email", "");
                newList.add(map);
                continue;
            }
            String fileName = link.substring(link.lastIndexOf("/"));
            try {
                logArea.setText("开始下载pdf==>" + link);
                DownloadPdf.downLoadByUrl(link, fileName, desktopPath, new DownloadPdf.OnDownloadListener() {
                    @Override
                    public void onProgress(long sum) {
                        logArea.setText("开始下载pdf==" + FileSizeUtils.getFileSize(sum) + "==>" + link);
                    }
                });
                String pdfPath = desktopPath + "/" + fileName;
                // 解析pdf得到想要的字符串
                logArea.setText("开始解析pdf==>" + fileName);
                String emailList = parsePdf(pdfPath);
                // 得到邮箱列表之后,写入集合
                map.put("email", emailList);
                newList.add(map);
                //删除本地文件
                File file = new File(pdfPath);
                file.delete();
            } catch (Exception e) {
                map.put("email", "");
                newList.add(map);
                e.printStackTrace();
            }
        }
        return newList;
    }

    private String parsePdf(String pdfPath) {
        try {
            PDDocument document = PDDocument.load(pdfPath);
            // 获取页码
            int pages = document.getNumberOfPages();
            // 读文本内容
            PDFTextStripper stripper = new PDFTextStripper();
            // 设置按顺序输出
            stripper.setSortByPosition(true);
            stripper.setStartPage(0);
            stripper.setEndPage(pages > 5 ? 5 : pages);
//            stripper.setEndPage(1);
            //获取内容
            String content = stripper.getText(document);
            document.close();
            //根据正则，获取邮箱地址
            String check = "([a-zA-Z0-9_-]+[.])*[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+([.][a-zA-Z0-9_-]+)+";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(content);
            String emailList = null;
            while (matcher.find()) {
                System.out.println("Found value: " + matcher.group(0));
                if (emailList == null) {
                    emailList = matcher.group(0);
                } else {
                    emailList = emailList + ";" + matcher.group(0);
                }
            }
            return emailList;
        } catch (Exception e) {
            return "";
        }
    }


    private java.util.List<Map<String, String>> parseExcel(String excelFilePath) {
        List<Map<String, String>> list = null;
        String columns[] = {"name", "title", "link"};
        Workbook wb = readExcel(excelFilePath);
        if (wb != null) {
            //用来存放表中数据
            list = new ArrayList<Map<String, String>>();
            //获取第一个sheet
            Sheet sheet = wb.getSheetAt(0);
            //获取最大行数
            int rownum = sheet.getPhysicalNumberOfRows();
            //获取第一行
            Row row = sheet.getRow(0);
            //获取最大列数
            int colnum = row.getPhysicalNumberOfCells();
            for (int i = 1; i < rownum; i++) {
                Map<String, String> map = new LinkedHashMap<String, String>();
                row = sheet.getRow(i);
                if (row != null) {
                    for (int j = 0; j < colnum; j++) {
                        String cellData = (String) getCellFormatValue(row.getCell(j));
                        map.put(columns[j], cellData);
                    }
                } else {
                    break;
                }
                list.add(map);
            }
        }
        return list;
    }

    //读取excel
    private static Workbook readExcel(String filePath) {
        Workbook wb = null;
        if (filePath == null) {
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                return wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                return wb = new XSSFWorkbook(is);
            } else {
                return wb = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    private Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            //判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    //判断cell是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        //转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    } else {
                        //数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }
}
