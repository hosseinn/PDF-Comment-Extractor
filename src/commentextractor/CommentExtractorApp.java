/*
 *
 * Extracts comments from PDF files using Itext PDF library.
 * This code extracts the comments from the PDF file using
 * the itext and print them on the console.
 *
 *
 * Copyright (c) 2011 Hossein Noorikhah
 * Copyright (c) 2009 Ali Vajahat
 *
 * Distributed under the GNU GPL v3. For full terms see the file docs/COPYING.
 * See: <http://www.gnu.org/licenses/gpl-3.0-standalone.html>
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package commentextractor;

import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ListIterator;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class CommentExtractorApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new CommentExtractorView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of CommentExtractorApp
     */
    public static CommentExtractorApp getApplication() {
        return Application.getInstance(CommentExtractorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        try {
            int first = 1, last = -1;

            if (args.length == 0) {
                launch(CommentExtractorApp.class, args);
                return;
            } else if (args.length == 1) {
                printUsage();
                return;
            } else if (args.length == 2) {
                last = -1;
            } else if (args.length == 3) {
                first = Integer.parseInt(args[2]);
                last = -1;
            } else if (args.length == 4) {
                first = Integer.parseInt(args[2]);
                last = Integer.parseInt(args[3]);
            } else if (args.length > 3) {
                printUsage();
                return;
            }

            PrintStream out = null;
            try {
                out = new PrintStream(System.out, true, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CommentExtractorApp.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.println(extractComments(args[1], first, last));

        } catch (NumberFormatException e) {
            printUsage();
        }

    }

    static void printUsage() {
        System.err.println("Usage: java -cp .;itextpdf-5.1.2.jar"
                + " CommentsTest filename.pdf [firstPage] [lastPage]");

    }

    static String extractComments(String filename, int first, int last) {
        StringBuffer output = null;
        try {
            PdfReader reader = new PdfReader(filename);

            if (last >= reader.getNumberOfPages() || (last == -1)) {
                last = reader.getNumberOfPages();
            }

            output = new StringBuffer(1024);

            for (int i = first; i <= last; i++) {

                PdfDictionary page = reader.getPageN(i);
                PdfArray annotsArray = null;

                if (page.getAsArray(PdfName.ANNOTS) == null) {
                    continue;
                }

                annotsArray = page.getAsArray(PdfName.ANNOTS);
                for (ListIterator<PdfObject> iter = annotsArray.listIterator(); iter.hasNext();) {
                    PdfDictionary annot = (PdfDictionary) PdfReader.getPdfObject(iter.next());
                    PdfString content = (PdfString) PdfReader.getPdfObject(annot.get(PdfName.CONTENTS));
                    if (content != null) {
                        output.append("----------\n");
                        output.append("Page " + i);
                        output.append("\n");
                        output.append(content.toUnicodeString().replaceAll("\r", "\r\n"));
                        output.append("\n");
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CommentExtractorApp.class.getName()).log(Level.SEVERE, null, e);
        }
        return new String(output);
    }
}
