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
public class Categorylog extends Timestamped {

    // constants

    private Integer categoryid;
    private String actio;
    private Timestamp tim;
    private Integer userid;

    // constructors

    public Categorylog(Integer categoryid2, Integer userid2, String actio) {
        this.setCategoryId(categoryid2);
        this.setTime(new Timestamp(new Date().getTime()));
        this.setUserId(userid2);
        this.setAction(actio);
    }
    // getters and setters
    public Integer getUserId() {
        return this.userid;
    }

    public Categorylog setUserId(Integer id) {
        this.userid = id;
        return this;
    }

    public Integer getCategoryId() {
        return this.categoryid;
    }

    public Categorylog setCategoryId(Integer id) {
        this.categoryid = id;
        return this;
    }

    public String getAction() {
        return this.actio;
    }

    public Categorylog setAction(String action) {
        this.actio = action;
        return this;
    }


    public Timestamp getTime() {
        return this.tim;
    }

    public Categorylog setTime(Timestamp time) {
        this.tim = time;
        return this;
    }

    public Categorylog setTime(String timeString) {
        this.tim = Timestamp.valueOf(timeString);
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        // If obj is not an instance of Question class, directly return false.
        if (! (obj instanceof Categorylog)) {
            return false;
        }
        Categorylog categorylog = (Categorylog) obj;
        return this.id.equals(categorylog.getId()) &&
            this.userid.equals(categorylog.getUserId()) &&
            this.categoryid.equals(categorylog.getCategoryId()) &&
            this.tim.equals(categorylog.getTime()) &&
            this.actio.equals(categorylog.getAction());
    }

    // methods

    /**
     * Saves the Question instance to the database. Updates if already saved.
     *
     * @return Saved or updated instance.
     */
    public Categorylog save() {
        try (Connection con = DB.sql2o.open();) {
            String query;
            if (this.id != null && this.id > 0) {
                query = "UPDATE categorylog SET "
                    + "userid=:userId, categoryid=:categoryId, "
                    + "actio=:action WHERE id=:id";
                con.createQuery(query)
                    .bind(this)
                    .executeUpdate();
            } else {
                query = "INSERT INTO exportlog"
                    + " (categoryid, userid, actio)"
                    + " VALUES (:categoryId, :userId, :action";
                    this.id = con.createQuery(query, true)
                                .bind(this)
                                .executeUpdate()
                                .getKey(Integer.class);

            }
            return Categorylog.findById(this.id);
        }
    }

    /**
     * Delets the Question instance from the database.
     */
    public void delete() {
        try (Connection con = DB.sql2o.open();) {
            String query = "DELETE FROM categorylog WHERE id=:id";
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
    public Category getCategory() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM categories WHERE id=:examId";
            return con.createQuery(query)
                    .bind(this)
                    .executeAndFetchFirst(Category.class);
        }
    }




    public static List<Categorylog> all() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM categorylog ORDER BY id DESC";
            return con.createQuery(query).executeAndFetch(Categorylog.class);
        }
    }


    public static Categorylog findById(Integer id) {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM categorylog WHERE id=:id";
            return con.createQuery(query)
                .addParameter("id", id)
                .executeAndFetchFirst(Categorylog.class);
        }
    }

}
