package models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Represents a Book.
 * Created by Eduardo.
 */
@Entity
public class Book extends Model {

    @Id
    private long id;


    @Column
    @NotNull
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    @NotNull
    private String author;

    @Column
    @NotNull
    private int pages;

    @Column
    //TODO @NotNull
    private String coverUrl;

    public static Finder<Long, Book> find = new Finder<Long,Book>(Book.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
