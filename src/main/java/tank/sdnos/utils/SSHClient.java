package tank.sdnos.utils;

import com.jcraft.jsch.*;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

public class SSHClient {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SSHClient.class);
    private static int timeout = 3000;
    private static int sshPort = 22;

    public static Map<String, String> execRemoteCommand(String remoteHostIp, String userName, String password,
            String command) {

        Session session = null;
        ChannelExec exec = null;

        ByteArrayOutputStream outputResult = new ByteArrayOutputStream();
        ByteArrayOutputStream errResult = new ByteArrayOutputStream();
        Map<String, String> results = new HashMap<String, String>();

        JSch jsch = new JSch();
        try {
            session = jsch.getSession(userName, remoteHostIp, sshPort);
            session.setPassword(password);
            session.setUserInfo(new MyUserInfo());
            session.connect(timeout);

            exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand(command);
            exec.setInputStream(null);
            exec.setOutputStream(outputResult);
            exec.setErrStream(errResult);

            exec.connect(timeout);

            while (!exec.isClosed()) {
                Thread.sleep(500);
            }

            exec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("tank# can not ssh to remote host: {}", remoteHostIp);
        }

        if (outputResult.toString().equals("")) {
            results.put("stdout", null);
        } else {
            results.put("stdout", outputResult.toString());
        }

        if (errResult.toString().equals("")) {
            results.put("stderr", null);
        } else {
            results.put("stderr", errResult.toString());
            logger.error("tank# execute remote command {} error, error message: {}", command, errResult);
        }

        return results;
    }

    private static class MyUserInfo implements UserInfo {
        @Override
        public String getPassphrase() {
            System.out.println("getPassphrase");
            return null;
        }

        @Override
        public String getPassword() {
            System.out.println("getPassword");
            return null;
        }

        @Override
        public boolean promptPassword(String s) {
            System.out.println("promptPassword:" + s);
            return false;
        }

        @Override
        public boolean promptPassphrase(String s) {
            System.out.println("promptPassphrase:" + s);
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return true;// notice here!
        }

        @Override
        public void showMessage(String s) {
            System.out.println("showMessage:" + s);
        }
    }

    public static void main(String[] args) {
        Map<String, String> res = new HashMap<String, String>();
        res = execRemoteCommand("127.0.0.1", "gzq", "a", "ifconfix");
        if (res.get("stdout") != null) {
            System.out.println("output message:\n" + res.get("stdout"));
        }
        if (res.get("stderr") != null) {
            System.out.println("error message:\n" + res.get("stderr"));
        }
    }

}