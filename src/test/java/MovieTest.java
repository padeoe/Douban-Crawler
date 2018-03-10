import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovieTest {

    @Test
    void fromJson() {
        String movies = "[{\"episode\":\"\",\"img\":\"https://img3.doubanio.com\\/view\\/photo\\/s_ratio_poster\\/public\\/p1158743783.jpg\",\"title\":\"玩具总动员\",\"url\":\"https:\\/\\/movie.douban.com\\/subject\\/1291575\\/?suggest=tt0114709\",\"type\":\"movie\",\"year\":\"1995\",\"sub_title\":\"Toy Story\",\"id\":\"1291575\"}]";
        Movie movie1 = Movie.fromJson(movies, "tt0001");
        String expectedMovieJson = "{\"id\":\"1291575\",\"title\":\"玩具总动员\",\"sub_title\":\"Toy Story\",\"img\":\"https://img3.doubanio.com/view/photo/s_ratio_poster/public/p1158743783.jpg\",\"year\":\"1995\",\"type\":\"movie\",\"episode\":\"\"}";
        assertEquals(movie1.toString(), expectedMovieJson);

        String emptyMovies = "[]";
        Movie movie2 = Movie.fromJson(emptyMovies, "tt0001");
        assertEquals("{}", movie2.toString());


        String illegalJson = "[sasa]";
        Movie movie3 = Movie.fromJson(illegalJson, "tt0001");
        assertEquals(null, movie3);

        String illegalJson2= "dasdasd";
        Movie movie4 = Movie.fromJson(illegalJson, "tt0001");
        assertEquals(null, movie4);


        String emptyMovies2 = "[{}]";
        Movie movie5 = Movie.fromJson(emptyMovies, "tt0001");
        assertEquals("{}", movie5.toString());

        String emptyMoviesAndBadJson = "[{dasd}]";
        Movie movie6 = Movie.fromJson(emptyMoviesAndBadJson, "tt0001");
        assertEquals(null, movie6);

        String emptyMoviesContentWithoutId = "[{\"episode\":\"\"}]";
        Movie movie7 = Movie.fromJson(emptyMoviesContentWithoutId, "tt0001");
        assertEquals(null, movie7);


        String emptyMoviesContentWithId = "[{\"episode\":\"\",\"id\":\"123\"}]";
        Movie movie8 = Movie.fromJson(emptyMoviesContentWithId, "tt0001");
        assertEquals("{\"id\":\"123\",\"episode\":\"\"}", movie8.toString());

    }

    @Test
    public void toStringTest() {
        Movie movie = new Movie("s", "s", "s", "s", "http://a/b", "s", "s", "s");
        String json = movie.toString();
        assertEquals("{\"id\":\"s\",\"title\":\"s\",\"sub_title\":\"s\",\"img\":\"http://a/b\",\"year\":\"s\",\"type\":\"s\",\"episode\":\"s\"}",json);
    }
}