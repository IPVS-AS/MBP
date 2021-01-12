package de.ipvs.as.mbp.service.testing.analyzer;


import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Creating a Header with the MBP-Icon in it for the Test-Report.
 */
public class ReportHeader implements IEventHandler {
    String header;
    String iconPath;

    public ReportHeader(String header, String iconPath) {
        this.header = header;
        this.iconPath = iconPath;
    }


    /**
     * Adds the MBP icon and short description to each page of the report when a new page is added.
     *
     * @param event to handle
     */
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        try {
            Rectangle pageSize = page.getPageSize();

            // Create a canvas component w specific font size/color
            PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
            Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
            canvas.setFontSize(8f);
            canvas.setFontColor(new DeviceRgb(0, 191, 255));

            // Load the mbp icon out of the github repository
            URL mbp = new URL(iconPath);
            mbp.openConnection();
            BufferedImage image = ImageIO.read(mbp);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            Image mbpIcon = new Image(ImageDataFactory.create(baos.toByteArray()));

            // Set the scale and fixed position of the icon on the page
            mbpIcon.scaleAbsolute(25, 25);
            mbpIcon.setFixedPosition(((pageSize.getWidth() - mbpIcon.getImageScaledWidth()) / 30) - 10, (pageSize.getHeight() - mbpIcon.getImageScaledHeight()) - 6);

            // Add the MBP icon
            canvas.add(mbpIcon);

            // Add the MBP short description
            Paragraph paragraph = new Paragraph(header).setFontColor(new DeviceRgb(0, 191, 255));
            paragraph.setFontSize(12);
            canvas.showTextAligned(header, (pageSize.getWidth() / 6) + 12, pageSize.getTop() - 24, TextAlignment.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
