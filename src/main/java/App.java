
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;
import java.util.*;
import org.sql2o.*;
import java.sql.Timestamp;
import org.apache.commons.lang.StringUtils;


/**
 * The controller class for the web application. It handles all the routes.
 * @author Manish Munikar, Sushil Shakya, Aakash Shrestha, Rojina Deuja
 * @since 2017-08-12
 */
public class App {

    /** Number of questions per page to show in question list page */
    public static final Integer QUESTIONS_PER_PAGE = 10;
    public static String msg = "";
    public static String msg2 = "";
    public static void main(String[] args)
    {
        // The sets the folder in the 'resources' folder where the static files
        // are to be searched.
        staticFileLocation("/public");

        // Default layout template.
        String layout = "templates/layout.vtl";

        // Layout template for login/signup pages.
        String layout_signinup = "templates/layout_signinup.vtl";

        // Home page. Redirects to login page if not already logged in.
        get("/", (request, response) -> {
            Map<String,Object> model = new HashMap<String,Object>();
            if (request.session().attribute("userId") == null) {
                response.redirect("/login");
            } else {
                response.redirect("/admin");
            }
            model.put("template", "templates/index.vtl");
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // Admin dashboard. Currenlty, this page is not implemented.
        get("/admin", (request, response) -> {
			if (request.session().attribute("userId") == null || request.session().attribute("userId") == "") {
                response.redirect("/login");
            }
            Map<String,Object> model = new HashMap<String,Object>();
            if (request.session().attribute("userId") != null) {
            model.put("template", "templates/admin.vtl");
            model.put("titlepage", "Admin-LIS QSG");
            List<Questionlog> crted = Questionlog.byAction("Created");
            List<Questionlog> upted = Questionlog.byAction("Updated");
            List<Questionlog> delted = Questionlog.byAction("Deleted");
            model.put("crted", crted);
            model.put("upted", upted);
            model.put("delted", delted);
            model.put("categories", Category.all());
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);

          }else{
			response.redirect("/login");
		  }

            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // This is a temporary route to display some message using GET
        // parameters. This should only be used for debugging.
        //
        // For example, if we want to display "404 NOT FOUND" message, we should
        // redirect to '/message?m=404+NOT+FOUND'
        // get("/message", (request, response) -> {
        //     Map<String,Object> model = new HashMap<String,Object>();
        //     model.put("template", "templates/message.vtl");
        //     model.put("message", request.queryParams("m"));
        //     return new ModelAndView(model, layout);
        // }, new VelocityTemplateEngine());

        // The registration form.
        // NOTE: This is only for development phase. In
        // production, any user would need to be added by an admin.
        // Self-registration should not be allowed in production environment.
        get("/register", (request, response) -> {
            List<User> us=User.all();
            for(User user:us){
              if(user.isAdmin()){
                response.redirect("/login");
              }
            }
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/register.vtl");
            model.put("show", 1);
            if(request.queryParams("m")==null){
              model.put("message", "");
            }else{
              model.put("message", request.queryParams("m"));
            }
            return new ModelAndView(model, layout_signinup);
        }, new VelocityTemplateEngine());

        // The registration form handler. It creates a new user and Redirects
        // to the login page.
        // NOTE: Again, self-registration is only for development
        // phase!
        post("/register", (request, response) -> {
          List<User> us=User.all();
          for(User user:us){
            if(user.isAdmin()){
              response.redirect("/login");
            }
          }
            String name = request.queryParams("name");
            String email = request.queryParams("email");
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            User u = User.findByUsername(username);
            if (u == null) {
                User u2 = new User(email, username, password, name, 2).save();
                User u3 = User.findByUsername(username);
                request.session().attribute("userId", u3.getId());
                response.redirect("/admin");
            }
            else{
                response.redirect("/register");
            }
            return 0;
        });

        // The login form.
        // It is used to handle user login.
        // Added by Rojina Deuja
        get("/login", (request, response) -> {
          Integer show=1;
          if (request.session().attribute("userId") != null) {
              response.redirect("/admin");
          }

          List<User> us=User.all();
          for(User user:us){
            if(user.isAdmin()){
              show=0;
            }
          }
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/login.vtl");
            model.put("show", show);
            if(msg==""){
              model.put("message", "");
            }else{
              model.put("message", msg);
            }
            return new ModelAndView(model, layout_signinup);
        }, new VelocityTemplateEngine());

        // Login form handler.
        // Verify the username and password and only access for authenticated
        post("/login", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            User u = User.findByUsername(username);

            if (u == null) {
                msg="Username not Found";
                response.redirect("/login");
            }

            if (u.checkPassword(password)) {
                request.session().attribute("userId", u.getId());
                response.redirect("/admin");
            } else {
                // TODO
                msg="Login Failed";
                response.redirect("/login");
            }

            return 0;
        });

        // The logout handler.
        // NOTE: Logout should be handled by a POST method. We've used GET for
        // ease of implementation in the development phase.

        get("/logout", (request, response) -> {

                request.session().removeAttribute("userId");
                response.redirect("/login");

            return 0;
        });
        // End of Rojina part

        // Paginated list of all questions in the database. This is useful for
        // deleting or updating existing questions, or adding a new one in the
        // database.
        // Added by Manish Munikar
        get("/questions", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/question_list.vtl");
            model.put("titlepage", "Questions-LIS QSG");
            Integer page = 1;   // default page number
            if (request.queryParams("page") != null) {
                page = Integer.parseInt(request.queryParams("page"));
            }
            Integer start = (page - 1) * QUESTIONS_PER_PAGE;
            List<Question> questions = Question.limit(start, QUESTIONS_PER_PAGE);
            List<Category> categories = Category.all();

            model.put("questions", questions);
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("currentPage", page);
            model.put("categories", categories);
            model.put("prevPage", page-1);
            model.put("nextPage", page+1);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());


        get("/log", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/question_log.vtl");
            model.put("titlepage", "Log-LIS QSG");
            List<Questionlog> crted = Questionlog.byAction("Created");
            List<Questionlog> upted = Questionlog.byAction("Updated");
            List<Questionlog> delted = Questionlog.byAction("Deleted");
            model.put("crted", crted);
            model.put("upted", upted);
            model.put("delted", delted);
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());
        // New question form.
        get("/questions/add", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/question_add_form.vtl");
            model.put("titlepage", "Add Question-LIS QSG");
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("categories", Category.all());
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // New question form handler. Adds the new question in the database
        // and then redirects to question list page.
        post("/questions", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Integer categoryId = Integer.parseInt(request.queryParams("category"));
            String question = request.queryParams("question");
            String answer1 = request.queryParams("answer1");
            String answer2 = request.queryParams("answer2");
            String answer3 = request.queryParams("answer3");
            String answer4 = request.queryParams("answer4");
            Integer difficulty = Integer.parseInt(request.queryParams("difficulty"));

            // userId is 0 = NULL for now
            Question q = new Question(0, categoryId, question, difficulty).save();
            Integer userr=request.session().attribute("userId");
            Integer quesid=q.getId();
            Questionlog ql=new Questionlog(quesid, userr, "Created").save();
            Answer a;
            a = new Answer(q, answer1, true).save();
            a = new Answer(q, answer2, false).save();
            a = new Answer(q, answer3, false).save();
            a = new Answer(q, answer4, false).save();

            response.redirect("/questions");
            return 0;
        });

        // Question delete handler. Data modifiers (add, update, delete) should
        // should always be handled by POST method.
        post("/questions/:qid/delete", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Integer id = Integer.parseInt(request.params(":qid"));

            Question q = Question.findById(id);
            q.delete();
            Integer userr=request.session().attribute("userId");
            Integer quesid=q.getId();
            Questionlog ql=new Questionlog(quesid, userr, "Deleted").save();

            response.redirect("/questions");
            return 0;
        });

        post("/users/:uid/delete", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
          if (!User.findById(request.session().attribute("userId")).isAdmin()) {
                response.redirect("/users");
            }
            Integer id = Integer.parseInt(request.params(":uid"));
            User u = User.findById(id);
            u.delete();
            response.redirect("/users");
            return 0;
        });

        // Question edit form.
        get("/questions/:qid/edit", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Question question = Question
                .findById(Integer.parseInt(request.params(":qid")));

            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/question_edit_form.vtl");
            model.put("titlepage", "Edit Question-LIS QSG");
            model.put("categories", Category.all());
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("question", question);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());
        // Question edit form handler.

        get("/users/:uid/edit", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }

            User user = User
                .findById(Integer.parseInt(request.params(":uid")));

            Map<String,Object> model = new HashMap<String,Object>();
            if (!User.findById(request.session().attribute("userId")).isAdmin()) {
                  model.put("template", "templates/404.vtl");
                  User defuser=User.findById(request.session().attribute("userId"));
                  model.put("defuser",defuser);
                  return new ModelAndView(model, layout);
              }
            model.put("template", "templates/user_edit_form.vtl");
            model.put("titlepage", "Edit User-LIS QSG");
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("user",user);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        post("/questions/add", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }

          Integer categoryId = Integer.parseInt(request.queryParams("category"));
          String question = request.queryParams("question");
          String answer1 = request.queryParams("answer1");
          String answer2 = request.queryParams("answer2");
          String answer3 = request.queryParams("answer3");
          String answer4 = request.queryParams("answer4");
          Integer difficulty = Integer.parseInt(request.queryParams("difficulty"));

          // userId is 0 = NULL for now
          Question q = new Question(0, categoryId, question, difficulty).save();
          Integer userr=request.session().attribute("userId");
          Integer quesid=q.getId();
          Questionlog ql=new Questionlog(quesid, userr, "Created").save();
          Answer a;
          a = new Answer(q, answer1, true).save();
          a = new Answer(q, answer2, false).save();
          a = new Answer(q, answer3, false).save();
          a = new Answer(q, answer4, false).save();

          response.redirect("/questions");
          return 0;
        });

        post("/questions/:qid/edit", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Question q = Question.findById(Integer.parseInt(request.params(":qid")));

            Integer categoryId = Integer.parseInt(request.queryParams("category"));
            String text = request.queryParams("question");
            String answer1 = request.queryParams("answer1");
            String answer2 = request.queryParams("answer2");
            String answer3 = request.queryParams("answer3");
            String answer4 = request.queryParams("answer4");
            Integer difficulty = Integer.parseInt(request.queryParams("difficulty"));

            String sql;
            try (Connection con = DB.sql2o.open();) {
                sql = "UPDATE questions SET categoryId=:categoryId, text=:text, difficulty=:difficulty WHERE id=:id";
                con.createQuery(sql)
                    .addParameter("categoryId", categoryId)
                    .addParameter("text", text)
                    .addParameter("difficulty", difficulty)
                    .addParameter("id", q.getId())
                    .executeUpdate();
            }

            // delete old answers and add new answers
            for (Answer a : q.getAnswers()) {
                a.delete();
            }
            q.addAnswer(answer1, true);
            q.addAnswer(answer2, false);
            q.addAnswer(answer3, false);
            q.addAnswer(answer4, false);
            Integer userr=request.session().attribute("userId");
            Integer quesid=q.getId();
            Questionlog ql=new Questionlog(quesid, userr, "Updated").save();
            response.redirect("/questions/" + q.getId() + "/edit");
            return 0;
        });
        // End of Manish Munikar part

        // List of categories.
        // Displays the list of catgories currently available in the database.
        // Added by Sushil Shakya
        get("/categories", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();Category.all();
            model.put("template", "templates/category_list.vtl");
            model.put("titlepage", "Categories-LIS QSG");
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("categories", Category.all());
            // System.out.println(categories_list);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // Category form handler. Saves the category in the database and
        // redirects to list of categories.
        post("/categories",(request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            String category_name = request.queryParams("category_name");
            Category c = new Category(category_name).save();
            response.redirect("/categories");
            return "Success";
        });

        // Category delete handler.
        // Can be used to delete the unwanted category from the database.
        post("/categories/:cId/delete", (request, response) ->{
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Category category = Category.findById(Integer.parseInt(request.params(":cId")));
            category.delete();
            response.redirect("/categories");
            return "Success";
        });

        get("/categories/:cId", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Category category = Category.findById(Integer.parseInt(request.params("cId")));
            Integer page = 1;   // default page number
            if (request.queryParams("page") != null) {
                page = Integer.parseInt(request.queryParams("page"));
            }
            Integer start = (page - 1) * QUESTIONS_PER_PAGE;
            List<Question> questions = Question.findByCategory(Integer.parseInt(request.params("cId")),start, QUESTIONS_PER_PAGE);
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/question_list.vtl");
            model.put("titlepage", category.getName()+"-LIS QSG");
            model.put("category", category);
            model.put("categories", Category.all());
            model.put("questions", questions);
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("currentPage", page);
            model.put("prevPage", page-1);
            model.put("nextPage", page+1);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());
        // Part of Sushil Shakya ends here


        // User add form. This should be only available for admin users.
        get("/users/add", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            if (!User.findById(request.session().attribute("userId")).isAdmin()) {
                  model.put("template", "templates/404.vtl");

                  User defuser=User.findById(request.session().attribute("userId"));
                  model.put("defuser",defuser);

                  return new ModelAndView(model, layout);
              }
            // Get the current active user ID
            Integer userId = request.session().attribute("userId");



            model.put("template", "templates/user_add_form.vtl");
            model.put("titlepage", "Add User-LIS QSG");
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // User add form handler. This is also available for admin users.
        // Added by Aakash Shrestha
        post("/users", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }

            Map<String,Object> model = new HashMap<String,Object>();

            // Check if current active user is admin.
            if (!User.findById(request.session().attribute("userId")).isAdmin()) {
                  model.put("template", "templates/404.vtl");
                  return new ModelAndView(model, layout);
              }
            Integer role = Integer.parseInt(request.queryParams("role"));
            String name = request.queryParams("name");
            String username = request.queryParams("username");
            String email = request.queryParams("email");
            String password = request.queryParams("password");

            try {
                User u = new User(email, username, password, name, role).save();
                response.redirect("/users");
            } catch (Exception e) {
                model.put("template", "templates/message.vtl");
                model.put("message", "ERROR CREATING USER");
                e.printStackTrace();
                return new ModelAndView(model, layout);
            }

            return 0;
        });
        post("/users/:uid/edit", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }

            Map<String,Object> model = new HashMap<String,Object>();

            // Check if current active user is admin.
            if (!User.findById(request.session().attribute("userId")).isAdmin()) {
                  model.put("template", "templates/404.vtl");
                  return new ModelAndView(model, layout);
              }
            Integer role = Integer.parseInt(request.queryParams("role"));
            String name = request.queryParams("name");
            String username = request.queryParams("username");
            String email = request.queryParams("email");
            String password = request.queryParams("password");

            try {
                User user = User
                  .findById(Integer.parseInt(request.params(":uid")));
                user.setName(name);
                user.setEmail(email);
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(role);
                user.save();
                response.redirect("/users");
            } catch (Exception e) {
                model.put("template", "templates/message.vtl");
                model.put("message", "ERROR CREATING USER");
                e.printStackTrace();
                return new ModelAndView(model, layout);
            }

            return 0;
        });

        get("/change", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
                  // Get the current active user ID
            Integer userId = request.session().attribute("userId");



            model.put("template", "templates/user_change_form.vtl");
            model.put("titlepage", "Change Password-LIS QSG");
            if(msg2==""){
              model.put("message", "");
            }else{
              model.put("message", msg2);
            }
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());


		        post("/change", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }



            // Check if current active user is admin
            String oldpassword = request.queryParams("oldpassword");
            String password = request.queryParams("password");

            try {
                User user=User.findById(request.session().attribute("userId"));
				if(user.checkPassword(oldpassword)){
					user.setPassword(password);
          user.save();
          response.redirect("/admin");
				}
        else{
        msg2="Wrong password";
        response.redirect("/change");
      }

            } catch (Exception e) {
                msg2="Coudnt change password";
                response.redirect("/change");
                e.printStackTrace();
                return 0;
            }

            return 0;
        });
        // List of users. Only available for admin users.
        get("/users", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }


            Map<String,Object> model = new HashMap<String,Object>();
            if (!User.findById(request.session().attribute("userId")).isAdmin()) {
                  model.put("template", "templates/404.vtl");
                  User defuser=User.findById(request.session().attribute("userId"));
                  model.put("defuser",defuser);
                  return new ModelAndView(model, layout);
              }
            // Check if current active user is admin
            List<User> userss=User.all();
            model.put("template", "templates/user_list.vtl");
            model.put("users", userss);
            model.put("titlepage", "Users-LIS QSG");
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/users/add", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            Integer userId = request.session().attribute("userId");
            // if (userId == null || !User.findById(userId).isExaminer()) {
            //     response.redirect("/message?m=ACCESS+DENIED");
            // }
            model.put("template", "templates/user_add_form.vtl");
            model.put("titlepage", "Add User");
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

         get("/users/:id", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();

            Integer id = Integer.parseInt(request.params("id"));
            User usr=User.findById(id);
            model.put("template", "templates/profile.vtl");
            model.put("titlepage", usr.getName()+"'s Profile");
            model.put("user", User.findById(id));
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // Exam delete handler. Also deletes all associated sets and
        // set-question relations.
        post("/exams/:id/delete", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Integer id = Integer.parseInt(request.params("id"));
            Exam exam = Exam.findById(id);
            exam.delete();
            response.redirect("/exams");
            return 0;
        });
        // List of exams.
        get("/exams", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("template", "templates/exam_list.vtl");
            model.put("titlepage", "Exams-LIS QSG");
            model.put("exams", Exam.all());
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // New exam form.
        // NOTE: Should only be avaiable for Examiner users.
        get("/exams/add", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            Integer userId = request.session().attribute("userId");
            // if (userId == null || !User.findById(userId).isExaminer()) {
            //     response.redirect("/message?m=ACCESS+DENIED");
            // }
            model.put("template", "templates/exam_add_form.vtl");
            model.put("titlepage", "Add Exam-LIS QSG");
            model.put("categories", Category.all());
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // Exam form handler. It not only creates an Exam instance, but also
        // creates 3 Set instances and also adds appropriate questions to those
        // sets automatically.
        //
        // NOTE: Should only be available for Examiner users.
        post("/exams", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Integer userId = request.session().attribute("userId");
            // if (userId == null || !User.findById(userId).isExaminer()) {
            //     response.redirect("/message?m=ACCESS+DENIED");
            // }

            String title = request.queryParams("title");
            String timeString = request.queryParams("time").replace("T", " ");
            // make sure the seconds are set before parsing
            if (StringUtils.countMatches(timeString, ":") == 1) {
                timeString += ":00";
            }
            Integer duration = Integer.parseInt(request.queryParams("duration"));
            Integer setNumber = Integer.parseInt(request.queryParams("SetNumber"));
            Integer difficulty = Integer.parseInt(request.queryParams("difficulty"));
            String[] categories = request.queryParamsValues("categories");

            // create exam

            Exam exam = new Exam();
            exam.setTitle(title);
            exam.setTime(timeString);
            exam.setDuration(duration);
            exam.setDifficulty(difficulty);
            exam.setUserId(userId);
            exam.save();

            Set[] sets = new Set[setNumber];
            // Create 3 sets.
            for(Integer i = 0;i<setNumber;i++){
               sets[i] = new Set(exam, i).save();
            }


            // Calculate question counts. We have assumed that each question is
            // allocated 3 minutes in average. Also, each set contains 50%
            // questions of the specified difficulty and 25% each of the other
            // two difficulty levels.
            Integer countPerSet = duration / 3;
            Integer countSelected = countPerSet / 2;
            Integer countOther1 = countPerSet / 4;
            Integer countOther2 = countPerSet - countSelected - countOther1;

            // Extract questions of various difficulty levels
            String query = "SELECT * FROM questions WHERE "
                + "difficulty=:difficulty AND "
                + "(FIND_IN_SET(questions.categoryId, :categoryIds) > 0) "
                + "ORDER BY RAND() LIMIT 0, :count";
            List<Question> questionsSelected;
            List<Question> questionsOther1;
            List<Question> questionsOther2;
            try (Connection con = DB.sql2o.open();) {
                questionsSelected = con.createQuery(query)
                        .addParameter("difficulty", difficulty)
                        .addParameter("categoryIds", String.join(",", categories))
                        .addParameter("count", countSelected * setNumber)
                        .executeAndFetch(Question.class);
                questionsOther1 = con.createQuery(query)
                        .addParameter("difficulty", (difficulty + 1) % 3)
                        .addParameter("categoryIds", String.join(",", categories))
                        .addParameter("count", countOther1 * setNumber)
                        .executeAndFetch(Question.class);
                questionsOther2 = con.createQuery(query)
                        .addParameter("difficulty", (difficulty + 2) % 3)
                        .addParameter("categoryIds", String.join(",", categories))
                        .addParameter("count", countOther2 * setNumber)
                        .executeAndFetch(Question.class);
            }

            // separate the questions for each set

            // List<Question> set1Questions = new ArrayList<Question>();
            // List<Question> set2Questions = new ArrayList<Question>();
            // List<Question> set3Questions = new ArrayList<Question>();
            List<List<Question>> setList = new ArrayList<List<Question>>();
            for (int i = 0; i < setNumber; i++) {
                List<Question> setQuestions = new ArrayList<>();
                setList.add(setQuestions);
            }


            for (Integer i = 0; i < questionsSelected.size(); i++) {
                Question q = questionsSelected.get(i);
                Integer test = i / countSelected;
                for(Integer j = 0;j<setNumber;j++){
                  if (test == 0) {
                    setList.get(j).add(q);
                  }
                }

            }

            for (Integer i = 0; i < questionsOther1.size(); i++) {
                Question q = questionsOther1.get(i);
                Integer test = i / countOther1;
                for(Integer j = 0;j<setNumber;j++){
                  if (test == 1) {
                    setList.get(j).add(q);
                  }
                }
            }

            for (Integer i = 0; i < questionsOther2.size(); i++) {
                Question q = questionsOther2.get(i);
                Integer test = i / countOther2;
                for(Integer j = 0;j<setNumber;j++){
                  if (test == 2) {
                    setList.get(j).add(q);
                  }
                }
            }

            for(Integer j = 0;j<setNumber;j++){
                  assert setList.get(j).size() == countPerSet;
                }
            // Check if all sets have correct question counts
            // assert set1Questions.size() == countPerSet;
            // assert set2Questions.size() == countPerSet;
            // assert set3Questions.size() == countPerSet;

            // Collections.shuffle(set1Questions);
            // Collections.shuffle(set2Questions);
            // Collections.shuffle(set3Questions);
            for(Integer j = 0;j<setNumber;j++){
                  Collections.shuffle(setList.get(j));
                }
            Random random = new Random();
            for (Integer i = 1; i <= countPerSet; i++) {
                // set1.addQuestion(set1Questions.get(i-1), i, random.nextInt(4));
                // set2.addQuestion(set2Questions.get(i-1), i, random.nextInt(4));
                // set3.addQuestion(set3Questions.get(i-1), i, random.nextInt(4));
                for(Integer j = 0;j<setNumber;j++){
                  sets[j].addQuestion(setList.get(j).get(i-1), i, random.nextInt(4));
                }
            }

            // Redirect to the newly generated exam sets page
            response.redirect("/exams/" + exam.getId());
            return 0;
        });

        // Exam detail page. Shows links to the 3 question sets.
        get("/exams/:id", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();

            Integer id = Integer.parseInt(request.params("id"));
            Exam exa=Exam.findById(id);
            model.put("template", "templates/exam_detail.vtl");
            model.put("titlepage", exa.getTitle()+"-LIS QSG");
            model.put("exam", Exam.findById(id));
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // Exam delete handler. Also deletes all associated sets and
        // set-question relations.
        post("/exams/:id/delete", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Integer id = Integer.parseInt(request.params("id"));
            Exam exam = Exam.findById(id);
            exam.delete();
            response.redirect("/exams");
            return 0;
        });

        // Question set. Displays the list of questions and 4 options for each
        // question. The questions and answers have pre-defined order for a
        // specified set.
        get("/exams/:id/:set", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            Integer examId = Integer.parseInt(request.params("id"));
            Exam exam = Exam.findById(examId);
            // Validate set number. Must be one of {1,2,3}.
            Integer setNumber = Integer.parseInt(request.params("set"));
            if (setNumber < 0 || setNumber > exam.getSets().size()-1) {
                response.redirect("/message?m=INVALID+SET+NUMBER");
            }

            Set set = exam.getSets().get(setNumber);

            model.put("template", "templates/question_set.vtl");
            model.put("titlepage", exam.getTitle()+"-LIS QSG");
            model.put("set", set);
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("exam", exam);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        // Answer sheet for a particular set. It displays the question number
        // and its corresponding index of correct answer, for each question in
        // the set.
        get("/exams/:id/:set/solution", (request, response) -> {
          if (request.session().attribute("userId") == null) {
              response.redirect("/login");
          }
            Map<String,Object> model = new HashMap<String,Object>();
            Integer examId = Integer.parseInt(request.params("id"));
            Integer setNumber = Integer.parseInt(request.params("set"));
            Exam exam = Exam.findById(examId);
            Set set = exam.getSets().get(setNumber);

            model.put("template", "templates/answer_sheet.vtl");
            model.put("titlepage", exam.getTitle()+" Solution-LIS QSG");
            model.put("set", set);
            User defuser=User.findById(request.session().attribute("userId"));
            model.put("defuser",defuser);
            model.put("exam", exam);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());
    }
}
