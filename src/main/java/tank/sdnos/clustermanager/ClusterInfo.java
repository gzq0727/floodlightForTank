package tank.sdnos.clustermanager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterInfo {
    private static String propertiesFilePath = "src/main/resources/floodlightdefault.properties";
    private static Logger log = LoggerFactory.getLogger(ClusterInfo.class);
    private static Map<Short, Controller> clusterInfo = new HashMap<Short, Controller>();

    static {
        getClusterInfo();
    }

    public static void getClusterInfo() {
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(propertiesFilePath));
            pps.load(in);
        } catch (IOException e) {
            log.warn("floodlightdefault.properties is not found");
            e.printStackTrace();
        }

        String tmp = null;
        tmp = pps.getProperty("org.sdnplatform.sync.internal.SyncManager.nodes");
        List<Controller> controllers = null;

        if (tmp != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                controllers = mapper.readValue(tmp, new TypeReference<List<Controller>>() {
                });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            log.info("can not find the property");
        }

        for (Controller controller : controllers) {
            clusterInfo.put(controller.getNodeId(), controller);
        }

    }

    public static Map<Short, Controller> getClusterNodes() {
        return clusterInfo;
    }

    public static Controller getClusterNode(Short nodeId) {

        return clusterInfo.get(nodeId);
    }

    public static String getControllerIp(Short nodeId) {

        return clusterInfo.get(nodeId).getHostname();
    }

    static class Controller {
        private Short nodeId;
        private Short domainId;
        private String hostname;
        private int port;

        public Short getNodeId() {
            return nodeId;
        }

        public void setNodeId(Short nodeId) {
            this.nodeId = nodeId;
        }

        public Short getDomainId() {
            return domainId;
        }

        public void setDomainId(Short domainId) {
            this.domainId = domainId;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

    public static void main(String[] args) {
        System.out.println(clusterInfo.toString());
    }

}
