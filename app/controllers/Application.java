package controllers;

import models.Book;
import models.User;
import models.utils.AppException;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

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

    /**
     * Display the list of books on the DB.
     *
     * @return list of books
     */
    public Result index() {

        Form<BookForm> bookForm = form(BookForm.class);
        return ok(index.render(bookForm));
    }

    public Result listBooks() {
        return ok(Json.toJson(getAllBooks()));
    }

    public List<Book> getAllBooks() {
        return Book.find.all();
    }

    public Result insertBook() {
        return ok();
    }

    public Result runInsertBook() {
        Form<BookForm> bookForm = form(BookForm.class).bindFromRequest();

        //TODO validate

        Book book = new Book();
        book.setTitle(bookForm.get().title);
        book.setAuthor(bookForm.get().author);
        book.setPages(bookForm.get().pages);
        book.setDescription(bookForm.get().description);
        book.save();

        return redirect(routes.Application.index());
    }

    public static class BookForm {

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
            return badRequest(index.render(null));
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