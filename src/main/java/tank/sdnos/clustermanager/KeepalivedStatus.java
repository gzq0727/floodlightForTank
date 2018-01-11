package tank.sdnos.clustermanager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tank.sdnos.utils.ShellUtil;

public class KeepalivedStatus {
    public static boolean keepalivedIsActive() {
        String command = "service keepalived status";
        Map<String, String> resultsMap = new HashMap<String, String>();
        resultsMap = ShellUtil.executeShellCommand(command);

        String result = resultsMap.get("stdout");

        if (result.contains("Active: active (running)")) {
            return true;
        } else {

            return false;
        }
    }

    /* if keepalived is start, return true else flase */
    public static boolean startKeepalived() {
        if (keepalivedIsActive()) {
            return true;
        } else {
            String command = "service keepalived restart";
            ShellUtil.executeShellCommand(command);
            try {
                /* promise the command is excuted */
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return keepalivedIsActive();
        }
    }

    public static void main(String[] args) {
        System.out.println(keepalivedIsActive());
        System.out.println(startKeepalived());
    }
}
