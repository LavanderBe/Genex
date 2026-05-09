package Genex.entities;

import java.time.LocalDateTime;

public class SponsorNote {

    private String        id;
    private String        sponsorId;
    private String        author;
    private String        note;
    private LocalDateTime createdAt;

    public SponsorNote() {}

    public String getId()                          { return id; }
    public void   setId(String id)                 { this.id = id; }

    public String getSponsorId()                   { return sponsorId; }
    public void   setSponsorId(String s)           { this.sponsorId = s; }

    public String getAuthor()                      { return author; }
    public void   setAuthor(String s)              { this.author = s; }

    public String getNote()                        { return note; }
    public void   setNote(String s)                { this.note = s; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void          setCreatedAt(LocalDateTime d) { this.createdAt = d; }

    public String getCreatedAtDisplay() {
        return createdAt != null ? createdAt.toLocalDate().toString() : "";
    }
}
