package Genex.services;

import Genex.entities.SponsorTeam;
import Genex.entities.SponsorTournament;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.GRAY);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

    public void exportTeamContract(SponsorTeam st, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        addHeader(document, "CONTRAT DE SPONSORING - ÉQUIPE");
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);

        addTableRow(table, "Sponsor:", st.getSponsorName());
        addTableRow(table, "Équipe:", st.getTeamName());
        addTableRow(table, "Méthode:", st.getMethodLabel());
        addTableRow(table, "Montant / Valeur:", st.getBudgetAmount() + " TND");
        addTableRow(table, "Date Début:", st.getStartDate() != null ? st.getStartDate().toString() : "N/A");
        addTableRow(table, "Date Fin:", st.getEndDate() != null ? st.getEndDate().toString() : "N/A");
        addTableRow(table, "Notes:", st.getNotes() != null ? st.getNotes() : "—");

        document.add(table);
        addSignatures(document);
        
        document.close();
    }

    public void exportTournamentContract(SponsorTournament st, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        addHeader(document, "CONTRAT DE SPONSORING - TOURNOI");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);

        addTableRow(table, "Sponsor:", st.getSponsorName());
        addTableRow(table, "Tournoi:", st.getTournamentName());
        addTableRow(table, "Méthode:", st.getMethodLabel());
        addTableRow(table, "Montant / Valeur:", st.getBudgetAmount() + " TND");
        addTableRow(table, "Date Début:", st.getStartDate() != null ? st.getStartDate().toString() : "N/A");
        addTableRow(table, "Date Fin:", st.getEndDate() != null ? st.getEndDate().toString() : "N/A");
        addTableRow(table, "Notes:", st.getNotes() != null ? st.getNotes() : "—");

        document.add(table);
        addSignatures(document);

        document.close();
    }

    private void addHeader(Document document, String titleStr) throws DocumentException {
        Paragraph title = new Paragraph(titleStr, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph genex = new Paragraph("Généré par GENEX Management System", SUBTITLE_FONT);
        genex.setAlignment(Element.ALIGN_CENTER);
        genex.setSpacingAfter(20);
        document.add(genex);

        document.add(new Paragraph("______________________________________________________________________________"));
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, LABEL_FONT));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setPadding(8);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, NORMAL_FONT));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setPadding(8);
        table.addCell(cellValue);
    }

    private void addSignatures(Document document) throws DocumentException {
        Paragraph space = new Paragraph("\n\n\n\n");
        document.add(space);

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);

        PdfPCell cell1 = new PdfPCell(new Phrase("Signature du Sponsor\n\n_____________________", LABEL_FONT));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        sigTable.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase("Signature GENEX / Représentant\n\n_____________________", LABEL_FONT));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        sigTable.addCell(cell2);

        document.add(sigTable);
    }
}
