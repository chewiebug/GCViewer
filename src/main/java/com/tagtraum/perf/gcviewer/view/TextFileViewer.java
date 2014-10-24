package com.tagtraum.perf.gcviewer.view;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import com.tagtraum.perf.gcviewer.view.util.UrlDisplayHelper;

/**
 * Dialog to display a text file. Hyperlinks are made clickable. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.04.2013</p>
 */
public class TextFileViewer extends ScreenCenteredDialog {

    public TextFileViewer(Frame f, String fileName) {
        super(f, fileName);
        
        initComponents(fileName);
    }

    private String addBlanksAtBeginningOfLine(String text) {
        if (text.length() == 0) {
            return text;
        }
        
        StringBuilder sb = new StringBuilder(text);
        int index = 0;
        while (index < sb.length() && sb.charAt(index) == ' ') {
            sb.replace(index, index + 1, "&nbsp;");
            index += "&nbsp;".length();
        }
        
        return sb.toString();
    }
    
    private String addHtmlTags(String text) {
        text = addBlanksAtBeginningOfLine(text);
        text = replaceSpecialCharacters(text);
        
        int startIndex = text.indexOf("http://");
        if (startIndex < 0) {
            startIndex = text.indexOf("https://");
        }
        if (startIndex >= 0) {
            int endIndex = findEndOfHyperlink(text, startIndex);
            
            text = text.substring(0, startIndex) 
                    + "<a href='" + text.substring(startIndex, endIndex) + "'>"
                    + text.substring(startIndex, endIndex)
                    + "</a>"
                    + text.substring(endIndex); 
        }
        
        return text;
    }
    
    private int findEndOfHyperlink(String text, int startIndex) {
        int endIndex = text.indexOf(" ", startIndex);
        if (endIndex < 0) {
            endIndex = text.length();
        }
        while (text.charAt(endIndex-1) == '.'
                || text.charAt(endIndex-1) == ')'
                || text.charAt(endIndex-1) == '>') {
            
            endIndex--;
        }
        
        return endIndex;
    }
    
    private void initComponents(String fileName) {
        super.initComponents();
        
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.addHyperlinkListener(new HyperlinkAdapter(this));
        
        try {
            textPane.setText(readFile(fileName));
            textPane.setCaretPosition(0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        JScrollPane scrollPane = new JScrollPane(
                textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        getContentPane().add("Center", scrollPane);
        pack();
    }

    private String readFile(String fileName) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {

            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
                    
                    StringBuilder text = new StringBuilder();
                    while (reader.ready()) {
                        text.append(addHtmlTags(reader.readLine())).append("<br/>");
                    }
                    
                    return text.toString();
                }
            }
            else {
                return "'" + fileName + "' not found";
            }
        }
    }
    
    private String replaceSpecialCharacters(String text) {
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        return text;
    }
    
    /**
     * Listener for clicks on hyperlinks.
     * 
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 25.04.2013</p>
     */
    private class HyperlinkAdapter implements HyperlinkListener {

        private Component parent;
        
        public HyperlinkAdapter(Component parent) {
            super();
            
            this.parent = parent;
        }
        
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == EventType.ACTIVATED) {
                UrlDisplayHelper.displayUrl(parent, e.getURL());
            }
        }
        
    }
}
