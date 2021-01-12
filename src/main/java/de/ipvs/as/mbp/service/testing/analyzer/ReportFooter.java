package de.ipvs.as.mbp.service.testing.analyzer;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

/**
 * Creating a Footer with the page information for the Test-Report.
 */
public class ReportFooter implements IEventHandler{
    protected PdfFormXObject placeholder;
    protected float side = 20;
    protected float x = 300;
    protected float y = 25;
    protected float space = 4.5f;
    protected float descent = 3;

    final Style footerStyle = new Style().setFontSize(8).setFontColor(ColorConstants.BLACK);

    public ReportFooter() {
        placeholder = new PdfFormXObject(new Rectangle(0, 0, side, side));
    }

    /**
     * Handles the End Page event and adds "currentPageNumber / " to the footer of the report.
     *
     * @param event to handle
     */
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        int pageNumber = pdf.getPageNumber(page);
        Rectangle pageSize = page.getPageSize();
        PdfCanvas pdfCanvas = new PdfCanvas(
                page.getLastContentStream(), page.getResources(), pdf);
        Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
        Paragraph p = new Paragraph().add(String.valueOf(pageNumber)).add("  /").addStyle(footerStyle);
        canvas.showTextAligned(p, x, y, TextAlignment.RIGHT).setFontSize(8);
        pdfCanvas.addXObject(placeholder, x + space, y - descent);
        pdfCanvas.release();
    }

    /**
     * Adds the total number of pages next to the "currentPageNumber / " to the footer.
     *
     * @param pdf document to be manipulated
     */
    public void writeTotal(PdfDocument pdf) {
        Canvas canvas = new Canvas(placeholder, pdf);
        Paragraph pages = new Paragraph().add(String.valueOf(pdf.getNumberOfPages())).addStyle(footerStyle);
        canvas.setFontSize(8).showTextAligned(pages,
                0, descent, TextAlignment.LEFT);
    }
}
