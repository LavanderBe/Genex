package Genex.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a sponsorship contract between a Sponsor and a Tournament.
 *
 * Sponsorship method (what the sponsor provides):
 *   MONEY | PCS | DRINKS | FOOD | ELECTRONIC_GADGETS | MINI_SHOPS | EVENT_ANIMATORS
 */
public class SponsorTournament {

    public enum SponsorMethod {
        MONEY("Argent"),
        PCS("PCs / Matériel"),
        DRINKS("Boissons"),
        FOOD("Nourriture"),
        ELECTRONIC_GADGETS("Gadgets Électroniques"),
        MINI_SHOPS("Mini Boutiques"),
        EVENT_ANIMATORS("Animateurs / Événements");

        private final String label;
        SponsorMethod(String label) { this.label = label; }
        public String getLabel()    { return label; }

        @Override
        public String toString()    { return label; }
    }

    private String         id;
    private String         sponsorId;
    private Sponsor        sponsor;        // populated on read
    private String         tournamentId;
    private Tounament      tournament;     // populated on read
    private SponsorMethod  method;
    private BigDecimal     budgetAmount;   // monetary value (0 if non-cash)
    private LocalDate      startDate;
    private LocalDate      endDate;
    private String         notes;

    public SponsorTournament() {}

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getId()                        { return id; }
    public void   setId(String id)               { this.id = id; }

    public String getSponsorId()                 { return sponsorId; }
    public void   setSponsorId(String s)         { this.sponsorId = s; }

    public Sponsor getSponsor()                  { return sponsor; }
    public void    setSponsor(Sponsor sponsor)   {
        this.sponsor   = sponsor;
        this.sponsorId = sponsor != null ? sponsor.getId() : null;
    }

    public String getTournamentId()              { return tournamentId; }
    public void   setTournamentId(String t)      { this.tournamentId = t; }

    public Tounament getTournament()                       { return tournament; }
    public void      setTournament(Tounament tournament)   {
        this.tournament   = tournament;
        this.tournamentId = tournament != null ? tournament.getTournamentId() : null;
    }

    public SponsorMethod getMethod()                     { return method; }
    public void          setMethod(SponsorMethod method) { this.method = method; }

    public BigDecimal getBudgetAmount()                        { return budgetAmount; }
    public void       setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public LocalDate getStartDate()              { return startDate; }
    public void      setStartDate(LocalDate d)   { this.startDate = d; }

    public LocalDate getEndDate()                { return endDate; }
    public void      setEndDate(LocalDate d)     { this.endDate = d; }

    public String getNotes()                     { return notes; }
    public void   setNotes(String notes)         { this.notes = notes; }

    // ── Convenience display helpers ────────────────────────────────────────

    public String getSponsorName() {
        return sponsor != null ? sponsor.getName() : sponsorId;
    }

    public String getTournamentName() {
        return tournament != null ? tournament.getTournamentName() : "Tournament #" + tournamentId;
    }

    public String getMethodLabel() {
        return method != null ? method.getLabel() : "";
    }
}
