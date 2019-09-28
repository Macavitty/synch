package mav;

public class User {
    private String userid;
    private String postedvideo;
    private String mergedvideo;
    private Integer score;

    public User(String userid) {
        this.userid = userid;
    }

    public User(String userid, String postedvideo) {
        this.userid = userid;
        this.postedvideo = postedvideo;
    }

    public User(String userid, String postedvideo, Integer score) {
        this.userid = userid;
        this.postedvideo = postedvideo;
        this.score = score;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPostedvideo() {
        return postedvideo;
    }

    public void setPostedvideo(String postedvideo) {
        this.postedvideo = postedvideo;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getMergedvideo() {
        return mergedvideo;
    }

    public void setMergedvideo(String mergedvideo) {
        this.mergedvideo = mergedvideo;
    }
}
