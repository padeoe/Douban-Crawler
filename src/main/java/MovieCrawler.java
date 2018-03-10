import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class MovieCrawler {
    /**
     * 获取豆瓣的电影信息
     *
     * @param imdbId  imdb号，以tt开头
     * @param myProxy 代理
     * @return
     */
    public static Movie crawlDoubanMovie(String imdbId, MyProxy myProxy) {
        String urlPrefix = "https://movie.douban.com/j/subject_suggest?q=";
        String urlString = urlPrefix + imdbId;
        String headersString = "Accept: */*\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7,ja;q=0.6\n" +
                "Connection: keep-alive\n" +
//                "Cookie: ll=\"108090\"; bid=mOYQ3yCuPOM; ap=1\n" +
                "Host: movie.douban.com\n" +
//                "Referer: https://movie.douban.com/subject_search?search_text=tt0295700&cat=1002\n" +
//                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36\n" +
                "X-Requested-With: XMLHttpRequest";
        String[] headersArray = headersString.split("\n");
        Map<String, String> headers = Arrays.asList(headersArray).stream().map(
                s -> s.split(":", 2)
        ).collect(
                Collectors.toMap(array -> array[0], array -> array[1])
        );
        try {
            URL url = new URL(urlString);
            Proxy proxy = null;
            if (myProxy != null)
                proxy = myProxy.getProxy();
            HttpURLConnection connection = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
            if (proxy != null) {
                Authenticator.setDefault(new ProxyAuthenticator(myProxy.getProxyUser(), myProxy.getProxyPass()));
                connection.setRequestProperty("Proxy-Switch-Ip", "yes");
            }
            headers.forEach((k, v) -> connection.setRequestProperty(k, v));
            connection.setConnectTimeout(1000);
            connection.setRequestMethod("GET");
            connection.connect();

            try (
                    InputStream is = "gzip".equals(connection.getContentEncoding()) ?
                            new GZIPInputStream(connection.getInputStream()) :
                            connection.getInputStream()
            ) {
                byte[] bytes = is.readAllBytes();
                String result = new String(bytes, StandardCharsets.UTF_8);
                return Movie.fromJson(result, imdbId);
            } finally {
                connection.disconnect();
            }

        } catch (MalformedURLException e) {
            System.out.println(imdbId + " URL异常：" + urlString);
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            System.out.println(imdbId + " 连接错误");
            return null;
        } catch (IOException e) {
            System.out.println(imdbId + " 连接错误");
            return null;
        }

    }

    public static Movie crawlDoubanMovie(String imdbId) {
        return crawlDoubanMovie(imdbId, null);
    }
}
