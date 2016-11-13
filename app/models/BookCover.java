package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 *  Represents a ... book cover.
 */
@Entity
public class BookCover extends Model{

    @Id
    private long id;

    @Lob
    private byte[] picture;

    public static Model.Finder<Long, BookCover> find = new Model.Finder<Long,BookCover>(BookCover.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }
}
