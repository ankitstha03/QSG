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
public class Exportlog extends Timestamped {

    // constants

    private Integer examid;
    private Integer setid;
    private Timestamp tim;
    private Integer userid;
    private String actio;

    // constructors

    public Exportlog(Integer examid2, Integer userid2, String action, Integer setid2) {
        this.setExamId(examid2);
        this.setTime(new Timestamp(new Date().getTime()));
        this.setUserId(userid2);
        if(action!="Exported"){
          this.setSetId(null);
        }
        else{
          this.setSetId(setid2);
        }
        this.setAction(action);
    }
    // getters and setters
    public Integer getUserId() {
        return this.userid;
    }

    public Exportlog setUserId(Integer id) {
        this.userid = id;
        return this;
    }

    public Integer getExamId() {
        return this.examid;
    }

    public Exportlog setExamId(Integer id) {
        this.examid = id;
        return this;
    }

    public String getAction() {
        return this.actio;
    }

    public Exportlog setAction(String action) {
        this.actio = action;
        return this;
    }

    public Integer getSetId() {
        return this.setid;
    }

    public Exportlog setSetId(Integer id) {
        this.setid = id;
        return this;
    }


    public Timestamp getTime() {
        return this.tim;
    }

    public Exportlog setTime(Timestamp time) {
        this.tim = time;
        return this;
    }

    public Exportlog setTime(String timeString) {
        this.tim = Timestamp.valueOf(timeString);
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        // If obj is not an instance of Question class, directly return false.
        if (! (obj instanceof Exportlog)) {
            return false;
        }
        Exportlog exportlog = (Exportlog) obj;
        return this.id.equals(exportlog.getId()) &&
            this.userid.equals(exportlog.getUserId()) &&
            this.examid.equals(exportlog.getExamId()) &&
            this.tim.equals(exportlog.getTime()) &&
            this.actio.equals(exportlog.getAction()) &&
            this.setid.equals(exportlog.getSetId());
    }

    // methods

    /**
     * Saves the Question instance to the database. Updates if already saved.
     *
     * @return Saved or updated instance.
     */
    public Exportlog save() {
        try (Connection con = DB.sql2o.open();) {
            String query;
            if (this.id != null && this.id > 0) {
                query = "UPDATE exportlog SET "
                    + "userid=:userId, examid=:examId, "
                    + "setid=:setId, actio=:action WHERE id=:id";
                con.createQuery(query)
                    .bind(this)
                    .executeUpdate();
            } else {
                query = "INSERT INTO exportlog"
                    + " (examid, userid, setid, actio)"
                    + " VALUES (:examId, :userId, :setId, :action)";
                    this.id = con.createQuery(query, true)
                                .bind(this)
                                .executeUpdate()
                                .getKey(Integer.class);

            }
            return Exportlog.findById(this.id);
        }
    }

    /**
     * Delets the Question instance from the database.
     */
    public void delete() {
        try (Connection con = DB.sql2o.open();) {
            String query = "DELETE FROM exportlog WHERE id=:id";
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
    public Exam getExam() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM exams WHERE id=:examId";
            return con.createQuery(query)
                    .bind(this)
                    .executeAndFetchFirst(Exam.class);
        }
    }


    public Set getSet() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM sets WHERE id=:setId";
            return con.createQuery(query)
                    .bind(this)
                    .executeAndFetchFirst(Set.class);
        }
    }

    public char getSetName() {
      char name;
        if(this.setid==null){
          name = (char) (45);
        }
        else{
          name = this.getSet().getSetName();
        }
        return name;
    }

    public static List<Exportlog> all() {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM exportlog ORDER BY id DESC";
            return con.createQuery(query).executeAndFetch(Exportlog.class);
        }
    }


    public static Exportlog findById(Integer id) {
        try (Connection con = DB.sql2o.open();) {
            String query = "SELECT * FROM exportlog WHERE id=:id";
            return con.createQuery(query)
                .addParameter("id", id)
                .executeAndFetchFirst(Exportlog.class);
        }
    }

}
