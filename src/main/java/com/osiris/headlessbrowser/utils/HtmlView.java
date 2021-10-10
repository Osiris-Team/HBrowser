package com.osiris.headlessbrowser.utils;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class HtmlView {

    public JFrame getFrame(String url, String html) {
        // create jeditorpane
        JEditorPane jEditorPane = new JEditorPane();

        // make it read-only
        jEditorPane.setEditable(false);

        // create a scrollpane; modify its attributes as desired
        JScrollPane scrollPane = new JScrollPane(jEditorPane);

        // add an html editor kit
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);

        // create a document, set it on the jeditorpane, then add the html
        javax.swing.text.Document doc = kit.createDefaultDocument();
        jEditorPane.setDocument(doc);
        jEditorPane.setText(html);

        // now add it all to a frame
        JFrame j = new JFrame(url);
        j.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // make it easy to close the application
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // display the frame
        j.setSize(new Dimension(600, 400));

        // pack it, if you prefer
        //j.pack();
        return j;
    }

}
