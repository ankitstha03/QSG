import java.sql.Timestamp;
import org.apache.commons.validator.routines.EmailValidator;
import java.util.*;
import org.sql2o.*;


/**
 * Represents an MCQ Question. Each question has 4 answers of which only one is
 * correct. Also, each question falls under a category and a difficulty level.
 * The difficulty levels are coded as follows:
 *     0: Easy
 *     1: Medium
 *     2: Hard
 *
 * @author Manish Munikar
 * @since 2017-08-12
 *
 * @see Answer
 * @see Category
 */
public class Questionlog extends Timestamped {

    // constants

    private Integer questionid;
    private Timestamp tim;
    private Integer userid;
    private String actio;

    // constructors

    public Questionlog(Integer questionid2, Integer userid2, String action) {
        this.setQuestionId(questionid2);
        this.setTime(new Timestamp(new Date().getTime()));
        this.setUserId(userid2);
        this.setAction(action);
    }
    // getters and setters
    public Integer getUserId() {
        return this.userid;
    }

    public Questionlog setUserId(Integer id) {
        this.userid = id;
        return this;
    }

    public Integer getQuestionId() {
        return this.questionid;
    }

    public Questionlog setQuestionId(Integer id) {
        this.questionid = id;
        return this;
    }

    public String getAction() {
        return this.actio;
    }

    public Questionlog setAction(String action) {
        this.actio = action;
        return this;
    }

    public Timestamp getTime() {
        return this.tim;
    }

    public Questionlog setTime(Timestamp time) {
        this.tim = time;
        return this;
    }

    public Questionlog setTime(String timeString) {
        this.tim = Timestamp.valueOf(timeString);
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        // If obj is not an instance of Question class, directly return false.
        if (! (obj instanceof Questionlog)) {
            return false;
        }
        Questionlog questionlog = (Questionlog) obj;
        return this.id.equals(questionlog.getId()) &&
            this.userid.equals(questionlog.getUserId()) &&
            this.questionid.equals(questionlog.getQuestionId()) &&
            this.tim.equals(questionlog.getTime()) &&
            this.actio.equals(questionlog.getAction());
    }

    // methods

    /**
     * Saves the Question instance to the database. Updates if already saved.
     *
     * @return Saved or updated instance.
     */
    public Questionlog save() {
        try (Connection con = DB.sql2o.open();) {
            String query;
            if (this.id != null && this.id > 0) {
                query = "UPDATE questionlog SET "
                    + "userid=:userId, questionid=:questionId, "
                    + "actio=:action WHERE id=:id";
                con.createQuery(query)
                    .bind(this)
                    .executeUpdate();
            } else {
                query = "INSERT INTO questionlog"
                    + " (questionid, userid, actio)"
                    + " VALUES (:questionId, :userId, :action)";
                    this.id = con.createQuery(query, true)
                                .bind(this)
                                .executeUpdate()
                                .getKey(Integer.class);

            }
            return Questionlog.findById(this.id);
        }
    }

    /**
     * Delets the Question instance from the database.
     */
    public void delete() {
        try (Connection con = DB.sql2o.open();) {
            String query = "DELETE FROM questionlog WHERE id=:id";
            con.createQuery(query)
                .bind(this)
                .executeUpdate();
            this.setId(null);
        }
    }

    // relations lookup

    /**
     * Gets the user who created the question.
     *
     * @return User instance related to this Question instance.
     */
    public User getUser() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM users WHERE id=:userId";
            return con.createQuery(query)
                    .bind(this)
                    .executeAndFetchFirst(User.class);
        }
    }

    /**
     * Gets the category in which this question belongs.
     *
     * @return Category instance associated with this Question instance.
     */
    public Question getQuestion() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM questions WHERE id=:questionId";
            return con.createQuery(query)
                    .bind(this)
                    .executeAndFetchFirst(Question.class);
        }
    }





    public static List<Questionlog> all() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM questionlog ORDER BY id DESC";
            return con.createQuery(query).executeAndFetch(Questionlog.class);
        }
    }

    public static List<Questionlog> byAction(String action) {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM questionlog WHERE actio=:action ORDER BY id DESC";
            return con.createQuery(query).addParameter("action", action).executeAndFetch(Questionlog.class);
        }
    }


    public static Questionlog findById(Integer id) {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM questionlog WHERE id=:id";
            return con.createQuery(query)
                .addParameter("id", id)
                .executeAndFetchFirst(Questionlog.class);
        }
    }

}
