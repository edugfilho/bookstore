package controllers;

import models.Book;
import models.BookCover;
import models.User;
import models.utils.AppException;
import play.Routes;
import play.mvc.Http.MultipartFormData;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
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
 * Login and Logout.
 * User: yesnault
 */
public class Application extends Controller {

    public static Result GO_HOME = redirect(
            routes.Application.index()
    );

    public static Result GO_DASHBOARD = redirect(
            routes.Dashboard.index()
    );

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
            bookForm = bookForm.fill(formData);
        }
        return ok(bookModalContent.render(bookForm, create));
    }

    public Result runUpsertBook() {
        BookForm bookForm = form(BookForm.class).bindFromRequest().get();
        Book book = new Book();

        //TODO validate


        book.setTitle(bookForm.title);
        book.setAuthor(bookForm.author);
        book.setPages(bookForm.pages);
        book.setDescription(bookForm.description);
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
        MultipartFormData body = request().body().asMultipartFormData();
        MultipartFormData.FilePart picture = body.getFile("cover");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            BookCover cover = new BookCover();
            try {
                cover.setPicture(fileToByte(file));
                cover.save();
            } catch (IOException e) {
                return internalServerError(e.getMessage());
            }

            return ok(Json.parse("{\"id\": " + cover.getId() +"}"));
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    private byte[] fileToByte(File f) throws IOException {
        return Files.readAllBytes(f.toPath());
    }

    public Result fileDownload(Long id) {
        return ok();
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
        public String coverUrl;
    }

    /**
     * Login class used by Login Form.
     */
    public static class Login {

        @Constraints.Required
        public String email;
        @Constraints.Required
        public String password;

        /**
         * Validate the authentication.
         *
         * @return null if validation ok, string with details otherwise
         */
        public String validate() {

            User user = null;
            try {
                user = User.authenticate(email, password);
            } catch (AppException e) {
                return Messages.get("error.technical");
            }
            if (user == null) {
                return Messages.get("invalid.user.or.password");
            } else if (!user.validated) {
                return Messages.get("account.not.validated.check.mail");
            }
            return null;
        }

    }

    public static class Register {

        @Constraints.Required
        public String email;

        @Constraints.Required
        public String fullname;

        @Constraints.Required
        public String inputPassword;

        /**
         * Validate the authentication.
         *
         * @return null if validation ok, string with details otherwise
         */
        public String validate() {
            if (isBlank(email)) {
                return "Email is required";
            }

            if (isBlank(fullname)) {
                return "Full name is required";
            }

            if (isBlank(inputPassword)) {
                return "Password is required";
            }

            return null;
        }

        private boolean isBlank(String input) {
            return input == null || input.isEmpty() || input.trim().isEmpty();
        }
    }

    /**
     * Handle login form submission.
     *
     * @return Dashboard if auth OK or login form if auth KO
     */
    public Result authenticate() {
        Form<Login> loginForm = form(Login.class).bindFromRequest();

        Form<Register> registerForm = form(Register.class);

        if (loginForm.hasErrors()) {
            return badRequest(index.render());
        } else {
            session("email", loginForm.get().email);
            return GO_DASHBOARD;
        }
    }

    /**
     * Logout and clean the session.
     *
     * @return Index page
     */
    public Result logout() {
        session().clear();
        flash("success", Messages.get("youve.been.logged.out"));
        return GO_HOME;
    }

}