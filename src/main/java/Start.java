import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Start {
    static MyProxy proxy = new MyProxy();
    static Connection conn = null;
    static String JDBCURL = "jdbc:mysql://localhost/?useSSL=false&"
            + "user=root&password=localserver&useUnicode=true&characterEncoding=UTF8";
    static String DBNAME = "broken_douban_java";
//    static String DBNAME = "test";


    public static void start() {
        try {
            conn = DriverManager.getConnection(JDBCURL);

            conn.setCatalog(DBNAME);
            //创建爬虫需要的表
            createCrawlerTable();
            crawlStart();
        } catch (SQLException ex) {
            System.err.println("数据库错误:" + ex.getMessage());
        }
    }

    public static void crawlStart() {
        int i = 1;
        while (true) {
            System.out.println("开始第" + i + "轮爬虫");

            List<String> imdbIds = getSomeImdbIdsWithoutDoubanInfo(300);
            if (imdbIds.size() == 0) {
                System.out.println("推出");
                break;
            } else {
                imdbIds.parallelStream().forEach(
                        imdbId -> crawlAndSave(imdbId)
                );
            }
            i++;
        }
    }

    public static List<String> getSomeImdbIdsWithoutDoubanInfo(int limit) {
        String query = "SELECT tconst FROM movie_pass WHERE url_done = FALSE ORDER BY  score DESC LIMIT " + limit + ";";
        try (Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(query);
            String imdbId;
            List<String> imdbIds = new ArrayList<>();
            if (resultSet == null) return null;
            while (resultSet.next()) {
                imdbId = resultSet.getString("tconst");
                imdbIds.add(imdbId);
            }
            return imdbIds;
        } catch (SQLException e) {
            System.out.println("数据库错误：" + e.getMessage());
            return null;
        }
    }

    public static int saveMovieToExtraInfoTable(Movie movie) throws SQLException {
        String addMovie = "INSERT INTO extra_info(douban_id, imdb_id, title, sub_title, img_url, year, type, episode) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement insertExtraInfo = conn.prepareStatement(addMovie, Statement.RETURN_GENERATED_KEYS)) {
            insertExtraInfo.setString(1, movie.getId());
            insertExtraInfo.setString(2, movie.getImdbId());
            insertExtraInfo.setString(3, movie.getTitle());
            insertExtraInfo.setString(4, movie.getSubTitle());
            insertExtraInfo.setString(5, movie.getImgUrl());
            insertExtraInfo.setString(6, movie.getYear());
            insertExtraInfo.setString(7, movie.getType());
            insertExtraInfo.setString(8, movie.getEpisode());
            insertExtraInfo.executeUpdate();
            int extraInfoId;
            ResultSet rs = insertExtraInfo.getGeneratedKeys();
            if (rs.next()) {
                extraInfoId = rs.getInt(1);
                return extraInfoId;
            } else {
                throw new SQLException("插入extra_info表失败");
            }
        }
    }

    public static void saveImdbToPassTable(String imdbId) throws SQLException {
        String update = "UPDATE movie_pass SET url_done=TRUE WHERE tconst=?";
        try (PreparedStatement savePassed = conn.prepareStatement(update)) {
            savePassed.setString(1, imdbId);
            savePassed.executeUpdate();
        }
    }

    public static void save_extra_info_id_to_movieinfo_table(String imdbId, int extraInfoId) throws SQLException {
        String update = "UPDATE movies SET extra_info_id = ? WHERE tconst=?";
        try (PreparedStatement saveExtraInfoId = conn.prepareStatement(update)) {
            saveExtraInfoId.setInt(1, extraInfoId);
            saveExtraInfoId.setString(2, imdbId);
            saveExtraInfoId.executeUpdate();
        }
    }

    public static void crawlAndSave(String imdbId) {

        Movie movie = MovieCrawler.crawlDoubanMovie(imdbId, new MyProxy());
        if (movie != null) {
            System.out.println(movie);
            try {
                int extraInfoId = saveMovieToExtraInfoTable(movie);
                saveImdbToPassTable(imdbId);
                save_extra_info_id_to_movieinfo_table(imdbId, extraInfoId);
            } catch (Exception e) {
                System.out.println(imdbId + " " + e.getMessage());
            }
        }
    }

    public static void createDatabaseAndTable() {
        Statement stmt;
        String createDatabase = "CREATE DATABASE IF NOT EXISTS " + DBNAME + ";";
        String ddlMovie = "CREATE TABLE movies\n" +
                "(\n" +
                "  movie_id       INT AUTO_INCREMENT\n" +
                "    PRIMARY KEY,\n" +
                "  tconst         CHAR(9)       NULL,\n" +
                "  primaryTitle   VARCHAR(1000) NULL,\n" +
                "  startYear      INT           NULL,\n" +
                "  type_id        INT           NULL,\n" +
                "  runtimeMinutes VARCHAR(156)  NULL,\n" +
                "  genres         VARCHAR(256)  NULL,\n" +
                "  region         MEDIUMTEXT    NULL,\n" +
                "  language       MEDIUMTEXT    NULL,\n" +
                "  directors      TEXT          NULL,\n" +
                "  actors         TEXT          NULL,\n" +
                "  extra_info_id  INT           NULL,\n" +
                "  CONSTRAINT movies_movie_id_uindex\n" +
                "  UNIQUE (movie_id)\n" +
                ")\n" +
                "  ENGINE = InnoDB\n" +
                "  CHARACTER SET utf8mb4;";

        String indexMovie1 = "CREATE INDEX movies_tconst_movie_id_index\n" +
                "  ON movies (tconst, movie_id);";

        String indexMovie2 = "CREATE INDEX movies_pic_id_index\n" +
                "  ON movies (extra_info_id);";

        String ddlRating = "CREATE TABLE rating\n" +
                "(\n" +
                "  tconst        CHAR(9) NULL,\n" +
                "  averageRating DOUBLE  NULL,\n" +
                "  numVotes      INT     NULL\n" +
                ")\n" +
                "  ENGINE = InnoDB\n" +
                "  CHARACTER SET utf8mb4;";
        String indexRating1 = "CREATE INDEX rating_tconst_index\n" +
                "  ON rating (tconst);";
        String indexRating2 = "CREATE INDEX rating_averageRating_index\n" +
                "  ON rating (averageRating);";
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(createDatabase);
            conn.setCatalog(DBNAME);
            stmt = conn.createStatement();
            stmt.executeUpdate(ddlMovie);
            stmt.executeUpdate(indexMovie1);
            stmt.executeUpdate(indexMovie2);
            stmt.executeUpdate(ddlRating);
            stmt.executeUpdate(indexRating1);
            stmt.executeUpdate(indexRating2);

        } catch (SQLException e) {
            System.out.println("初始化数据库和表失败：" + e.getMessage());
        }


    }

    public static void createCrawlerTable() {
        String ddlExtrInfo = "CREATE TABLE extra_info (\n" +
                "  id        INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  douban_id CHAR(9)      NULL,\n" +
                "  imdb_id   CHAR(9)      NULL,\n" +
                "  title     VARCHAR(255) NULL,\n" +
                "  sub_title VARCHAR(255) NULL,\n" +
                "  img_url   VARCHAR(255) NULL,\n" +
                "  year      CHAR(8)      NULL,\n" +
                "  type      VARCHAR(50)  NULL,\n" +
                "  episode   VARCHAR(50)  NULL\n" +
                ")\n" +
                "  CHARACTER SET utf8mb4\n" +
                "  ENGINE = InnoDB;";

        String ddlMoviePass = "CREATE TABLE movie_pass (\n" +
                "  tconst   CHAR(9),\n" +
                "  score    DOUBLE,\n" +
                "  url_done BOOL\n" +
                ")\n" +
                "  CHARACTER SET utf8mb4\n" +
                "  ENGINE = InnoDB;";

        String indexExtraInfo = "CREATE INDEX extra_info_imdb_id_index\n" +
                "  ON extra_info (imdb_id);";
        String indexMoviePass1 = "CREATE INDEX movie_pass_url_done_index\n" +
                "  ON movie_pass (url_done);";
        String indexMoviePass2 = "CREATE INDEX movie_pass_tconst_score_index\n" +
                "  ON movie_pass (tconst);";
        String indexMoviePass3 = "CREATE INDEX movie_pass_score_index\n" +
                "  ON movie_pass (score);";
        String insertMoviePass = "INSERT INTO movie_pass\n" +
                "  SELECT\n" +
                "    A.tconst,\n" +
                "    rating.averageRating * rating.numVotes AS score,\n" +
                "    FALSE\n" +
                "  FROM movies A LEFT JOIN rating ON A.tconst = rating.tconst\n" +
                "  ORDER BY score DESC;";


        Statement stmt = null;
        String[] ddlSQLs = new String[]{ddlExtrInfo, ddlMoviePass};
        String[][] others = new String[][]{{indexExtraInfo}, {indexMoviePass1, indexMoviePass2, indexMoviePass3, insertMoviePass}};
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            System.out.println("数据库错误：" + e.getMessage());
            return;
        }
        for (int i = 0; i < ddlSQLs.length; i++) {
            try {
                excuteSQLWithOutput(ddlSQLs[i], stmt);
                for (int j = 0; j < others[i].length; j++) {
                    excuteSQLWithOutput(others[i][j], stmt);
                }
            } catch (SQLException e) {
                if (e.getSQLState().equals("42S01")) {
                    System.out.println("表已经存在，跳过：" + e.getMessage());
                    continue;
                }
            }
        }
    }

    public static void excuteSQLWithOutput(String sql, Statement stmt) throws SQLException {
        System.out.println("执行语句：\n" + sql);
        long startTime = System.currentTimeMillis();
        stmt.executeUpdate(sql);
        long endTime = System.currentTimeMillis();
        System.out.println("执行结束，" + (endTime - startTime) / 1000.0 + "秒");
    }


    public static void main(String[] args) {
        start();
    }
}