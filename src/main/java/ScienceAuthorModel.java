/**
 * @author namcooper
 * @create 2020-04-26 09:37
 */
public class ScienceAuthorModel {
    private String title = "";
    private String author = "";
    private String email = "";
    private String hIndex = "";
    private String citations = "";

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void sethIndex(String hIndex) {
        this.hIndex = hIndex;
    }

    public void setCitations(String citations) {
        this.citations = citations;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public String gethIndex() {
        return hIndex;
    }

    public String getCitations() {
        return citations;
    }

    public boolean hasUniqueInfo() {
        return author != null && author.length() != 0;
    }
}
