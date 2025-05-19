package testScripts;
import java.lang.SecurityManager; // Not necessary


public class SecurityTest {
    public static void main(String[] args) {
        var alwaysFalse = true;
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        } else if (alwaysFalse) {
            System.setSecurityManager(new SecurityManager());
        }
    }
}
