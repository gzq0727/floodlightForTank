package tank.sdnos.clustermanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tank.sdnos.utils.SSHClient;
import tank.sdnos.utils.ShellUtil;

public class ControllerRoleManager {
    private static String controllerUserName = "tank";
    private static String controllerUserPassword = "tanklab";
    private static String controllerConfigFilePath = "";

    static {
        controllerConfigFilePath = getControllerConfigFilePath();
    }

    private static String getControllerConfigFilePath() {
        StringBuilder result = new StringBuilder();
        Map<String, String> commandResult = new HashMap<String, String>();
        commandResult = ShellUtil.executeShellCommand("pwd");

        if (commandResult.get("stderr") == null) {
            return null;
        } else {
            result.append(commandResult.get("stdout"));
            if (result.toString() != "") {
                result.append("/src/main/resources/floodlightdefault.properties");
                return result.toString();
            } else {
                return null;
            }
        }

    }

    public static boolean changeSwitchRoleToSlaveInConfigFile(String controllerIp, String dpid) {
        /*
         * sed -i
         * 's/"00:00:00:00:00:00:00:01":"ROLE_MASTER"/"00:00:00:00:00:00:00:01":
         * "ROLE_SLAVE"/g'
         * /home/tank/floodlight/src/main/resources/floodlightdefault.
         * propertiess
         */

        StringBuilder command = new StringBuilder("sed -i 's/");
        command.append("\"" + dpid + "\":\"ROLE_MASTER\"" + "/\"" + dpid + "\":\"ROLE_SLAVE\"" + "/g' "
                + controllerConfigFilePath);

        System.out.println(command.toString());

        Map<String, String> commandResult = new HashMap<String, String>();

        commandResult = SSHClient.execRemoteCommand(controllerIp, controllerUserName, controllerUserPassword,
                command.toString());
        if (commandResult.get("stderr") != null) {
            return false;
        } else {
            /*
             * if the command result is what we expected, return true;or return
             * false
             */
            if (commandResult.get("stdout") == "") {
                return true;
            } else {
                return false;
            }
        }

    }

    public static boolean changeSwitchRoleToMasterInConfigFile(String controllerIp, String dpid) {
        /*
         * sed -i
         * 's/"00:00:00:00:00:00:00:01":"ROLE_MASTER"/"00:00:00:00:00:00:00:01":
         * "ROLE_SLAVE"/g'
         * /home/tank/floodlight/src/main/resources/floodlightdefault.
         * propertiess
         */

        StringBuilder command = new StringBuilder("sed -i 's/");
        command.append("\"" + dpid + "\":\"ROLE_SLAVE\"" + "/\"" + dpid + "\":\"ROLE_MASTER\"" + "/g' "
                + controllerConfigFilePath);

        System.out.println(command.toString());

        Map<String, String> commandResult = new HashMap<String, String>();

        commandResult = SSHClient.execRemoteCommand(controllerIp, controllerUserName, controllerUserPassword,
                command.toString());
        if (commandResult.get("stderr") != null) {
            return false;
        } else {
            /*
             * if the command result is what we expected, return true;or return
             * false
             */
            if (commandResult.get("stdout") == "") {
                return true;
            } else {
                return false;
            }
        }

    }

    public static void main(String[] args) {
        System.out.println(controllerConfigFilePath);
        changeSwitchRoleToMaster("172.18.16.123", "00:00:00:00:00:00:00:01");
    }

}
