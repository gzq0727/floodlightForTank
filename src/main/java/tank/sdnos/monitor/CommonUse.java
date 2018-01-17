package tank.sdnos.monitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import tank.sdnos.monitor.CommonUse.NoDirectLink;

public class CommonUse {

    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        return list;
    }

    public static class NoDirectLink extends Link {

        public NoDirectLink(DatapathId srcSw, OFPort srcPort, DatapathId dstSw, OFPort dstPort) {
            super();
            this.setSrc(srcSw);
            this.setSrcPort(srcPort);
            this.setDst(dstSw);
            this.setDstPort(dstPort);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long number1 = this.getSrc().getLong() * this.getDst().getLong();
            int number2 = this.getSrcPort().getPortNumber() * this.getDstPort().getPortNumber();
            result = prime * result + (int) (number1 ^ (number1 >>> 32));
            result = prime * result + number2;

            return result;

        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof NoDirectLink) {
                if (this.getSrc() == ((NoDirectLink) object).getSrc()
                        && this.getSrcPort() == ((NoDirectLink) object).getSrcPort()
                        && this.getDst() == ((NoDirectLink) object).getDst()
                        && this.getDstPort() == ((NoDirectLink) object).getDstPort()) {
                    return true;
                }
                if (this.getSrc() == ((NoDirectLink) object).getDst()
                        && this.getSrcPort() == ((NoDirectLink) object).getDstPort()
                        && this.getDst() == ((NoDirectLink) object).getSrc()
                        && this.getDstPort() == ((NoDirectLink) object).getSrcPort()) {
                    return true;
                }

                return false;
            }

            return false;
        }
    }

    public static Set<NoDirectLink> getNoDirectionLinksSet(Map<Link, LinkInfo> linksInfo) {
        Set<NoDirectLink> noDirectLinks = new HashSet<NoDirectLink>();
        for (Link link : linksInfo.keySet()) {
            NoDirectLink link1 = new NoDirectLink(link.getSrc(), link.getSrcPort(), link.getDst(), link.getDstPort());
            noDirectLinks.add(link1);
        }
        return noDirectLinks;
    }

    public static NoDirectLink getNoDirectionLink(Link linkInfo) {
        NoDirectLink link = new NoDirectLink(linkInfo.getSrc(), linkInfo.getSrcPort(), linkInfo.getDst(),
                linkInfo.getDstPort());
        return link;
    }

}
