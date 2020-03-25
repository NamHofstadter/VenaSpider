import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
public class HtmlSpider {
    private static HtmlSpider sInstance;

    private HtmlSpider() {
    }

    public static HtmlSpider getInstance() {
        if (sInstance == null) {
            sInstance = new HtmlSpider();
        }
        return sInstance;
    }

    public void htmlSpider() {
        JFrame frame = new JFrame("Vena's pdf spider");
        frame.setLayout(new VFlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.getColor("#F4F5F6"));
        frame.setResizable(false);

        frame.pack();
        frame.setVisible(true);
        frame.setBounds(600, 400, 500, 400);
    }
}
