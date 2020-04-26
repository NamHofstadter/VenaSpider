import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author namcooper
 * @create 2020-03-21 10:01
 */
public class Spider {

    private static Spider sInstance;

    private Spider() {
    }

    public static Spider getInstance() {
        if (sInstance == null) {
            sInstance = new Spider();
        }
        return sInstance;
    }

    public void startFrame() {
        JFrame frame = new JFrame("Vena's email spider");
        frame.setLayout(new VFlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBackground(Color.getColor("#F4F5F6"));
        frame.setResizable(false);

        JPanel btnPdfPanel = new JPanel();
        JButton btnPdf = new JButton("PDF Spider");
        btnPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pdf爬虫
                new PDFSpider().pdfSpider();
            }
        });
        btnPdf.setMargin(new Insets(10, 10, 10, 10));
        btnPdfPanel.add(btnPdf);
        frame.add(btnPdfPanel);

        JPanel btnHtmlPanel = new JPanel();
        JButton btnHtml = new JButton("Html Spider");
        btnHtml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 网页爬虫
                new HtmlSpider().htmlSpider();
            }
        });
        btnHtml.setMargin(new Insets(10, 10, 10, 10));
        btnHtmlPanel.add(btnHtml);
        frame.add(btnHtmlPanel);

        JPanel scienceDirectHtmlPanel = new JPanel();
        JButton scienceDirectHtmlBtn = new JButton("ScienceDirect Spider");
        scienceDirectHtmlBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ScienceDirect爬虫
                new ScienceDirectSpider().htmlSpider();
            }
        });
        scienceDirectHtmlBtn.setMargin(new Insets(10, 10, 10, 10));
        scienceDirectHtmlPanel.add(scienceDirectHtmlBtn);
        frame.add(scienceDirectHtmlPanel);

        frame.pack();
        frame.setVisible(true);
        frame.setBounds(600, 400, 500, 400);
    }

}
