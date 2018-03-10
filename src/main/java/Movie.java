import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Movie {

    String id = "";
    String title = "";
    @SerializedName("sub_title")
    String subTitle = "";
    @SerializedName("img")
    String imgUrl = "";
    String year = "";
    String type = "";
    String episode = "";
    private transient String imdbId = "";

    public Movie(String imdbId, String id, String title, String subTitle, String imgUrl, String year, String type, String episode) {
        this.imdbId = imdbId;
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.imgUrl = imgUrl;
        this.year = year;
        this.type = type;
        this.episode = episode;
    }

    public Movie() {
    }

    public Movie(String imdbId) {
        this.imdbId = imdbId;
    }

    public static Movie fromJson(String json, String imdbId) {
        Type listType = new TypeToken<ArrayList<Movie>>() {
        }.getType();
        try {
            List<Movie> movies = new Gson().fromJson(json, listType);
            if (movies == null)
                return null;
            else if (movies.size() > 0) {
                Movie movie = movies.get(0);
/*                if (movie.id == null || movie.id.trim().equals(""))
                    return null;*/
                movie.setImdbId(imdbId);
                return movie;
            } else
                return new Movie(imdbId);
        } catch (JsonSyntaxException jsonSyntaxException) {
            return null;
        }

    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    @Override
    public String toString() {
//        return new Gson().toJson(this);
        return this.getImdbId() + " " + this.getTitle() + " " + this.getImgUrl() + " " + this.getId();
    }
}

