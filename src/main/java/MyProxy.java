import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public class MyProxy {


    // 代理隧道验证信息

    static final String proxyUser = "***";
    static final String proxyPass = "***";

    // 代理服务器

    static String proxyServer = "***.com";

    static int proxyPort = 9020;
    // 创建代理服务器地址对象
    static InetSocketAddress addr = new InetSocketAddress(proxyServer, proxyPort);
    // 创建HTTP类型代理对象

    static Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);


    public MyProxy() {
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPass() {
        return proxyPass;
    }
}

class ProxyAuthenticator extends Authenticator {
    private String user, password;

    public ProxyAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password.toCharArray());
    }
}