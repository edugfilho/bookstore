package controllers;

import controllers.utils.ThumbnailGen;
import models.Book;
import models.BookCover;
import play.Routes;
import play.mvc.Http.MultipartFormData;
import play.data.Form;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.bookModalContent;
import views.html.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static play.data.Form.form;

/**
 * Bookstore application
 */
public class Application extends Controller {

    public Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        routes.javascript.Application.listBooks(),
                        routes.javascript.Application.upsertBook(),
                        routes.javascript.Application.runUpsertBook(),
                        routes.javascript.Application.runDeleteBook(),
                        routes.javascript.Application.fileUpload(),
                        routes.javascript.Application.fileDownload()
                )
        );
    }

    /**
     * Display the list of books on the DB.
     *
     * @return list of books
     */
    public Result index() {

        Form<BookForm> bookForm = form(BookForm.class);
        return ok(index.render());
    }

    public Result listBooks() {
        return ok(Json.toJson(getAllBooks()));
    }

    public List<Book> getAllBooks() {
        return Book.find.all();
    }

    /**
     * Renders a modal page to insert a new book if create is true,
     * otherwise to update the existing book, referenced by id.
     * @param id the book id
     * @return
     */
    public Result upsertBook(Boolean create, Long id) {
        Form<BookForm> bookForm = form(BookForm.class);

        if(!create) {
            BookForm formData = new BookForm();
            Book book = Book.find.byId(id);
            formData.id = id;
            formData.author = book.getAuthor();
            formData.description = book.getDescription();
            formData.pages = book.getPages();
            formData.title = book.getTitle();
            formData.coverId = book.getCoverId();
            bookForm = bookForm.fill(formData);
        }
        return ok(bookModalContent.render(bookForm, create));
    }

    private byte[] getFileBytesFromRequest(Http.Request request, String fieldName) throws IOException {
        MultipartFormData body = request.body().asMultipartFormData();
        MultipartFormData.FilePart picture = body.getFile(fieldName);
        if (picture != null) {
            File file = picture.getFile();
            BookCover cover = new BookCover();
            return fileToByte(file);
        } else {
            return null;
        }
    }

    public Result runUpsertBook() {
        BookForm bookForm = form(BookForm.class).bindFromRequest().get();
        Book book = new Book();

        //TODO validate


        book.setTitle(bookForm.title);
        book.setAuthor(bookForm.author);
        book.setPages(bookForm.pages);
        book.setDescription(bookForm.description);
        book.setCoverId(bookForm.coverId);

        byte[] cover = BookCover.find.byId(book.getCoverId()).getPicture();
        try {
            book.setThumbnail(ThumbnailGen.scaleImage(cover, 30, 30));
        } catch (IOException e) {
            return internalServerError(e.getMessage());
        }
        if(bookForm.id != null) { //Edit
            book.setId(bookForm.id);
            book.update();
        } else {
            book.save();
        }

        return redirect(routes.Application.index());
    }

    public Result runDeleteBook(Long id) {
        if(id != null) {
            Book.find.byId(id).delete();
        }
        //I tried to escape from this but found no other way =P
        //Simply returns "redirect":"\"
        return ok(Json.parse("{\"redirect\":\"\\\\\"}"));
    }

    public Result fileUpload() {
        byte[] fileBytes = null;
        try {
            fileBytes = getFileBytesFromRequest(request(), "cover");
        } catch (IOException e) {
            return internalServerError(e.getMessage());
        }
        if(fileBytes != null) {
            BookCover cover = new BookCover();
            cover.setPicture(fileBytes);
            cover.save();
            return ok(Json.parse("{\"id\": " + cover.getId() + "}"));
        }else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    private byte[] fileToByte(File f) throws IOException {
        return Files.readAllBytes(f.toPath());
    }

    public Result fileDownload(Long id) {
        byte[] pic = BookCover.find.byId(id).getPicture();
        return ok(pic).as("image/jpeg");
    }

    public static class BookForm {

        public Long id;

        @Constraints.MaxLength(150)
        @Constraints.Required
        public String title;

        @Constraints.MaxLength(150)
        @Constraints.Required
        public String author;

        @Constraints.Min(1)
        @Constraints.Max(9999)
        @Constraints.Required
        public int pages;

        @Constraints.MaxLength(500)
        public String description;

        //TODO: @Constraints.Required
        public Long coverId;
    }
}