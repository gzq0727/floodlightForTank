package tank.sdnos.monitor.web;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import tank.sdnos.monitor.BandwidthMonitor.LinkSpeed;
import tank.sdnos.monitor.BandwidthMonitor.LinkUsage;
import tank.sdnos.monitor.CommonUse.NoDirectLink;
import tank.sdnos.monitor.DelayMonitor.LatencyLet;
import tank.sdnos.monitor.DelayMonitor.LinkDelay;
import tank.sdnos.monitor.PacketLossMonitor.LinkLoss;

public class Serializer {
    /**
     * use to convert array LinkSpeed[] to json for rest response in
     * {@link tank.sdnos.monitor.web.TopNNoDirectLinkSpeedResource}
     */
    @JsonSerialize(using = NoDirectLinkSpeedArraySerializer.class)
    public static class NoDirectLinkSpeedArray {
        private LinkSpeed[] noDirectLinkSpeedList;

        public NoDirectLinkSpeedArray(LinkSpeed[] noDirectLinkSpeedList) {
            this.noDirectLinkSpeedList = noDirectLinkSpeedList;

        }

        public LinkSpeed[] getArray() {
            return this.noDirectLinkSpeedList;
        }
    }

    public static class NoDirectLinkSpeedArraySerializer extends JsonSerializer<NoDirectLinkSpeedArray> {

        @Override
        public void serialize(NoDirectLinkSpeedArray em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getArray() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link speed statistics data was found now.");
                jGen.writeEndObject();
                return;
            }
            LinkSpeed[] noDirectLinkSpeedArray = em.getArray();

            jGen.writeStartArray();
            if (noDirectLinkSpeedArray[0] != null) {
                for (LinkSpeed linkSpeed : noDirectLinkSpeedArray) {
                    if (linkSpeed != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", linkSpeed.getLink().getSrc().toString());
                        jGen.writeStringField("src-port", linkSpeed.getLink().getSrcPort().toString());
                        jGen.writeStringField("dst-switch", linkSpeed.getLink().getDst().toString());
                        jGen.writeStringField("dst-port", linkSpeed.getLink().getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkSpeed", linkSpeed.getLinkSpeed());
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert array LinkUsage[] to json for rest response in
     * {@link tank.sdnos.monitor.web.TopNNoDirectLinkUsageResource}
     */
    @JsonSerialize(using = NoDirectLinkUsageArraySerializer.class)
    public static class NoDirectLinkUsageArray {
        private LinkUsage[] noDirectLinkUsageArray;

        public NoDirectLinkUsageArray(LinkUsage[] noDirectLinkUsageArray) {
            this.noDirectLinkUsageArray = noDirectLinkUsageArray;

        }

        public LinkUsage[] getArray() {
            return this.noDirectLinkUsageArray;
        }
    }

    public static class NoDirectLinkUsageArraySerializer extends JsonSerializer<NoDirectLinkUsageArray> {

        @Override
        public void serialize(NoDirectLinkUsageArray em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getArray() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link usage statistics data was found now.");
                jGen.writeEndObject();
                return;
            }
            LinkUsage[] noDirectLinkUsageArray = em.getArray();

            jGen.writeStartArray();
            if (noDirectLinkUsageArray[0] != null) {
                for (LinkUsage linkUsage : noDirectLinkUsageArray) {
                    if (linkUsage != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", linkUsage.getLink().getSrc().toString());
                        jGen.writeStringField("src-port", linkUsage.getLink().getSrcPort().toString());
                        jGen.writeStringField("dst-switch", linkUsage.getLink().getDst().toString());
                        jGen.writeStringField("dst-port", linkUsage.getLink().getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkUsage", linkUsage.getLinkUsage());
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert map Map<NoDirectLink,Long> to json for rest response in
     * {@link tank.sdnos.monitor.web.AllNoDirectLinkSpeedResource}
     */
    @JsonSerialize(using = NoDirectLinkSpeedMapSerializer.class)
    public static class NoDirectLinkSpeedMap {
        private Map<NoDirectLink, Long> noDirectLinkSpeed;

        public NoDirectLinkSpeedMap(Map<NoDirectLink, Long> noDirectLinkSpeed) {
            this.noDirectLinkSpeed = noDirectLinkSpeed;

        }

        public Map<NoDirectLink, Long> getMap() {
            return this.noDirectLinkSpeed;
        }
    }

    public static class NoDirectLinkSpeedMapSerializer extends JsonSerializer<NoDirectLinkSpeedMap> {

        @Override
        public void serialize(NoDirectLinkSpeedMap em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getMap() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link speed statistics data was found now.");
                jGen.writeEndObject();
                return;
            }

            Map<NoDirectLink, Long> theMap = em.getMap();
            jGen.writeStartArray();
            if (theMap.keySet() != null) {
                for (NoDirectLink noDirectLink : theMap.keySet()) {
                    if (theMap.get(noDirectLink) != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", noDirectLink.getSrc().toString());
                        jGen.writeStringField("src-port", noDirectLink.getSrcPort().toString());
                        jGen.writeStringField("dst-switch", noDirectLink.getDst().toString());
                        jGen.writeStringField("dst-port", noDirectLink.getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkSpeed", theMap.get(noDirectLink));
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert map Map<NoDirectLink, Float> to json for rest response in
     * {@link tank.sdnos.monitor.web.AllNoDirectLinkUsageResource}
     */
    @JsonSerialize(using = NoDirectLinkUsageMapSerializer.class)
    public static class NoDirectLinkUsageMap {
        private Map<NoDirectLink, Float> noDirectLinkUsage;

        public NoDirectLinkUsageMap(Map<NoDirectLink, Float> noDirectLinkUsage) {
            this.noDirectLinkUsage = noDirectLinkUsage;

        }

        public Map<NoDirectLink, Float> getMap() {
            return this.noDirectLinkUsage;
        }
    }

    public static class NoDirectLinkUsageMapSerializer extends JsonSerializer<NoDirectLinkUsageMap> {

        @Override
        public void serialize(NoDirectLinkUsageMap em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getMap() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link usage statistics data was found now.");
                jGen.writeEndObject();
                return;
            }

            Map<NoDirectLink, Float> theMap = em.getMap();
            jGen.writeStartArray();
            if (theMap.keySet() != null) {
                for (NoDirectLink noDirectLink : theMap.keySet()) {
                    if (theMap.get(noDirectLink) != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", noDirectLink.getSrc().toString());
                        jGen.writeStringField("src-port", noDirectLink.getSrcPort().toString());
                        jGen.writeStringField("dst-switch", noDirectLink.getDst().toString());
                        jGen.writeStringField("dst-port", noDirectLink.getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkUsage", theMap.get(noDirectLink));
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert array LinkDelay[] to json for rest response in
     * {@link tank.sdnos.monitor.web.TopNNoDirectLinkDelayResource}
     */
    @JsonSerialize(using = NoDirectLinkDelayArraySerializer.class)
    public static class NoDirectLinkDelayArray {
        private LinkDelay[] noDirectLinkDelayArray;

        public NoDirectLinkDelayArray(LinkDelay[] noDirectLinkDelayArray) {
            this.noDirectLinkDelayArray = noDirectLinkDelayArray;

        }

        public LinkDelay[] getArray() {
            return this.noDirectLinkDelayArray;
        }
    }

    public static class NoDirectLinkDelayArraySerializer extends JsonSerializer<NoDirectLinkDelayArray> {

        @Override
        public void serialize(NoDirectLinkDelayArray em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getArray() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link delay statistics data was found now.");
                jGen.writeEndObject();
                return;
            }
            LinkDelay[] noDirectLinkDelayArray = em.getArray();

            jGen.writeStartArray();
            if (noDirectLinkDelayArray[0] != null) {
                for (LinkDelay linkDelay : noDirectLinkDelayArray) {
                    if (linkDelay != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", linkDelay.getLink().getSrc().toString());
                        jGen.writeStringField("src-port", linkDelay.getLink().getSrcPort().toString());
                        jGen.writeStringField("dst-switch", linkDelay.getLink().getDst().toString());
                        jGen.writeStringField("dst-port", linkDelay.getLink().getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkDelay", linkDelay.getDelay());
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert map Map<NoDirectLink, LatencyLet> to json for rest
     * response in {@link tank.sdnos.monitor.web.AllNoDirectLinkUsageResource}
     */
    @JsonSerialize(using = NoDirectLinkDelayMapSerializer.class)
    public static class NoDirectLinkDelayMap {
        private Map<NoDirectLink, LatencyLet> noDirectLinkDelay;

        public NoDirectLinkDelayMap(Map<NoDirectLink, LatencyLet> noDirectLinkDelay) {
            this.noDirectLinkDelay = noDirectLinkDelay;

        }

        public Map<NoDirectLink, LatencyLet> getMap() {
            return this.noDirectLinkDelay;
        }
    }

    private static class NoDirectLinkDelayMapSerializer extends JsonSerializer<NoDirectLinkDelayMap> {

        @Override
        public void serialize(NoDirectLinkDelayMap em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getMap() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link delay statistics data was found now.");
                jGen.writeEndObject();
                return;
            }

            Map<NoDirectLink, LatencyLet> theMap = em.getMap();
            jGen.writeStartArray();
            if (theMap.keySet() != null) {
                for (NoDirectLink noDirectLink : theMap.keySet()) {
                    if (theMap.get(noDirectLink) != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", noDirectLink.getSrc().toString());
                        jGen.writeStringField("src-port", noDirectLink.getSrcPort().toString());
                        jGen.writeStringField("dst-switch", noDirectLink.getDst().toString());
                        jGen.writeStringField("dst-port", noDirectLink.getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkDelay", theMap.get(noDirectLink).getLatency());
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert array LinkLoss[] to json for rest response in
     * {@link tank.sdnos.monitor.web.TopNNoDirectLinkDelayResource}
     */
    @JsonSerialize(using = NoDirectLinkLossArraySerializer.class)
    public static class NoDirectLinkLossArray {
        private LinkLoss[] noDirectLinkLossArray;

        public NoDirectLinkLossArray(LinkLoss[] noDirectLinkLossArray) {
            this.noDirectLinkLossArray = noDirectLinkLossArray;

        }

        public LinkLoss[] getArray() {
            return this.noDirectLinkLossArray;
        }
    }

    private static class NoDirectLinkLossArraySerializer extends JsonSerializer<NoDirectLinkLossArray> {

        @Override
        public void serialize(NoDirectLinkLossArray em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getArray() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link loss statistics data was found now.");
                jGen.writeEndObject();
                return;
            }
            LinkLoss[] noDirectLinkLossArray = em.getArray();

            jGen.writeStartArray();
            if (noDirectLinkLossArray[0] != null) {
                for (LinkLoss linkLoss : noDirectLinkLossArray) {
                    if (linkLoss != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", linkLoss.getLink().getSrc().toString());
                        jGen.writeStringField("src-port", linkLoss.getLink().getSrcPort().toString());
                        jGen.writeStringField("dst-switch", linkLoss.getLink().getDst().toString());
                        jGen.writeStringField("dst-port", linkLoss.getLink().getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkLoss", linkLoss.getLossRate());
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

    /**
     * use to convert map Map<NoDirectLink, Long> to json for rest response in
     * {@link tank.sdnos.monitor.web.AllNoDirectLinkLossResource}
     */
    @JsonSerialize(using = NoDirectLinkLossMapSerializer.class)
    public static class NoDirectLinkLossMap {
        private Map<NoDirectLink, Long> noDirectLinkLoss;

        public NoDirectLinkLossMap(Map<NoDirectLink, Long> noDirectLinkLoss) {
            this.noDirectLinkLoss = noDirectLinkLoss;

        }

        public Map<NoDirectLink, Long> getMap() {
            return this.noDirectLinkLoss;
        }
    }

    private static class NoDirectLinkLossMapSerializer extends JsonSerializer<NoDirectLinkLossMap> {

        @Override
        public void serialize(NoDirectLinkLossMap em, JsonGenerator jGen, SerializerProvider serializer)
                throws IOException, JsonProcessingException {
            // TODO Auto-generated method stub

            jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

            if (em.getMap() == null) {
                jGen.writeStartObject();
                jGen.writeString("No link loss statistics data was found now.");
                jGen.writeEndObject();
                return;
            }

            Map<NoDirectLink, Long> theMap = em.getMap();
            jGen.writeStartArray();
            if (theMap.keySet() != null) {
                for (NoDirectLink noDirectLink : theMap.keySet()) {
                    if (theMap.get(noDirectLink) != null) {
                        jGen.writeStartObject();
                        jGen.writeObjectFieldStart("link");
                        jGen.writeStringField("src-switch", noDirectLink.getSrc().toString());
                        jGen.writeStringField("src-port", noDirectLink.getSrcPort().toString());
                        jGen.writeStringField("dst-switch", noDirectLink.getDst().toString());
                        jGen.writeStringField("dst-port", noDirectLink.getDstPort().toString());
                        jGen.writeEndObject();
                        jGen.writeNumberField("linkLoss", theMap.get(noDirectLink));
                        jGen.writeEndObject();
                    }
                }
            }
            jGen.writeEndArray();

        }

    }

}
