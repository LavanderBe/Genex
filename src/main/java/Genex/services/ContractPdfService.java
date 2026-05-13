package Genex.services;

import Genex.entities.SponsorTeam;
import Genex.entities.SponsorTournament;
import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class ContractPdfService {

    private static final String DEST_DIR = "contracts";
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(139, 13, 13));
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

    public ContractPdfService() {
        File dir = new File(DEST_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public void exportTeamContract(SponsorTeam st) throws Exception {
        String fileName = "Contract_Team_" + st.getSponsorName().replaceAll("\\s+", "_") + "_" + st.getId() + ".pdf";
        File file = new File(DEST_DIR, fileName);
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();
        buildHeader(document, "CONTRAT DE SPONSORING — ÉQUIPE");
        
        PdfPTable table = createInfoTable();
        addTableRow(table, "ID CONTRAT", st.getId());
        addTableRow(table, "SPONSOR", st.getSponsorName());
        addTableRow(table, "ÉQUIPE", st.getTeamName());
        addTableRow(table, "MÉTHODE", st.getMethodLabel());
        addTableRow(table, "BUDGET ENGAGÉ", st.getBudgetAmount() + " TND");
        addTableRow(table, "DATE DE DÉBUT", st.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addTableRow(table, "DATE DE FIN", st.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        document.add(table);

        addNotes(document, st.getNotes());
        addSignatures(document);

        document.close();
        openFile(file);
    }

    public void exportTournamentContract(SponsorTournament st) throws Exception {
        String fileName = "Contract_Tournoi_" + st.getSponsorName().replaceAll("\\s+", "_") + "_" + st.getId() + ".pdf";
        File file = new File(DEST_DIR, fileName);
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();
        buildHeader(document, "CONTRAT DE SPONSORING — TOURNOI");

        PdfPTable table = createInfoTable();
        addTableRow(table, "ID CONTRAT", st.getId());
        addTableRow(table, "SPONSOR", st.getSponsorName());
        addTableRow(table, "TOURNOI", st.getTournamentName());
        addTableRow(table, "MÉTHODE", st.getMethodLabel());
        addTableRow(table, "BUDGET ENGAGÉ", st.getBudgetAmount() + " TND");
        addTableRow(table, "DATE DE DÉBUT", st.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addTableRow(table, "DATE DE FIN", st.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        document.add(table);

        addNotes(document, st.getNotes());
        addSignatures(document);

        document.close();
        openFile(file);
    }

    private void buildHeader(Document doc, String titleText) throws Exception {
        Paragraph p = new Paragraph("GENEX ESPORTS", FONT_SUBTITLE);
        p.setAlignment(Element.ALIGN_RIGHT);
        doc.add(p);
        
        doc.add(new Paragraph("\n"));
        Paragraph title = new Paragraph(titleText, FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph("\n\n"));
    }

    private PdfPTable createInfoTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        return table;
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, FONT_LABEL));
        cellLabel.setBackgroundColor(new Color(245, 245, 245));
        cellLabel.setPadding(8f);
        cellLabel.setBorderColor(Color.LIGHT_GRAY);
        
        PdfPCell cellValue = new PdfPCell(new Phrase(value != null ? value : "—", FONT_VALUE));
        cellValue.setPadding(8f);
        cellValue.setBorderColor(Color.LIGHT_GRAY);
        
        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    private void addNotes(Document doc, String notes) throws Exception {
        doc.add(new Paragraph("\nCLAUSES ET NOTES PARTICULIÈRES :", FONT_LABEL));
        Paragraph p = new Paragraph(notes != null && !notes.isBlank() ? notes : "Aucune note particulière spécifiée pour ce contrat.", FONT_VALUE);
        p.setSpacingBefore(5f);
        doc.add(p);
    }

    private void addSignatures(Document doc) throws Exception {
        doc.add(new Paragraph("\n\n\n\n"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        
        PdfPCell s1 = new PdfPCell(new Phrase("SIGNATURE SPONSOR\n(Précédé de 'Bon pour accord')", FONT_LABEL));
        s1.setBorder(Rectangle.NO_BORDER);
        s1.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        PdfPCell s2 = new PdfPCell(new Phrase("SIGNATURE GENEX ADMINISTRATION\n(Cachet et Signature)", FONT_LABEL));
        s2.setBorder(Rectangle.NO_BORDER);
        s2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        table.addCell(s1);
        table.addCell(s2);
        doc.add(table);
    }

    private void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Desktop non supporté, impossible d'ouvrir le fichier : " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
