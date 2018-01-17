package tank.sdnos.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Properties;

import org.projectfloodlight.openflow.protocol.OFBarrierRequest;
import org.projectfloodlight.openflow.protocol.OFBucket;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFFlowDeleteStrict;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFFlowModify;
import org.projectfloodlight.openflow.protocol.OFFlowModifyStrict;
import org.projectfloodlight.openflow.protocol.OFGroupAdd;
import org.projectfloodlight.openflow.protocol.OFGroupBucket;
import org.projectfloodlight.openflow.protocol.OFGroupDelete;
import org.projectfloodlight.openflow.protocol.OFGroupModify;
import org.projectfloodlight.openflow.protocol.OFGroupType;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionEnqueue;
import org.projectfloodlight.openflow.protocol.action.OFActionGroup;
import org.projectfloodlight.openflow.protocol.action.OFActionMeter;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionPopMpls;
import org.projectfloodlight.openflow.protocol.action.OFActionPopVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionPushMpls;
import org.projectfloodlight.openflow.protocol.action.OFActionPushVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetMplsLabel;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwTtl;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpSrc;
import org.projectfloodlight.openflow.protocol.action.OFActionSetVlanVid;
import org.projectfloodlight.openflow.protocol.action.OFActionStripVlan;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.match.Prerequisite;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.ClassId;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.ICMPv4Code;
import org.projectfloodlight.openflow.types.ICMPv4Type;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IPv6FlowLabel;
import org.projectfloodlight.openflow.types.IpDscp;
import org.projectfloodlight.openflow.types.IpEcn;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.LagId;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBitMask128;
import org.projectfloodlight.openflow.types.OFBitMask512;
import org.projectfloodlight.openflow.types.OFBooleanValue;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFMetadata;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.PacketType;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U128;
import org.projectfloodlight.openflow.types.U16;
import org.projectfloodlight.openflow.types.U32;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.U8;
import org.projectfloodlight.openflow.types.UDF;
import org.projectfloodlight.openflow.types.VFI;
import org.projectfloodlight.openflow.types.VRF;
import org.projectfloodlight.openflow.types.VlanPcp;
import org.projectfloodlight.openflow.types.VlanVid;
import org.projectfloodlight.openflow.types.VxlanNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.util.FlowModUtils;
import net.floodlightcontroller.util.MatchUtils;

public class FlowUtils {
    protected static Logger log = LoggerFactory.getLogger(FlowUtils.class);

    private String propertiesFilePath = "src/main/resources/floodlightdefault.properties";

    public static int FLOWMOD_DEFAULT_IDLE_TIMEOUT = 0; // in seconds
    public static int FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
    public static int FLOWMOD_DEFAULT_PRIORITY = 1; // 0 is the default
                                                    // table-miss flow in
                                                    // OF1.3+, so we need to use
                                                    // 1
    protected static short FLOWMOD_DEFAULT_TABLE_ID = 0;
    protected static boolean FLOWMOD_DEFAULT_SET_SEND_FLOW_REM_FLAG = false;

    public FlowUtils() {
        /* get default parameter from floodlightdefault.properties */
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(propertiesFilePath));
            pps.load(in);
        } catch (IOException e) {
            log.warn("floodlightdefault.properties is not found");
            e.printStackTrace();
        }

        String tmp = null;
        tmp = pps.getProperty("tank.sdnos.flowutils.tank_idel_timeout");
        if (tmp != null) {
            FLOWMOD_DEFAULT_IDLE_TIMEOUT = Integer.parseInt(tmp);
        } else {
            log.info("tank:default idel timeout is not set in floodlightdefault.properties, using {}",
                    FLOWMOD_DEFAULT_IDLE_TIMEOUT);
        }
        tmp = pps.getProperty("tank.sdnos.flowutils.tank_hard_timeout");
        if (tmp != null) {
            FLOWMOD_DEFAULT_HARD_TIMEOUT = Integer.parseInt(tmp);
        } else {
            log.info("tank:default hard timeout is not set in floodlightdefault.properties, using {}",
                    FLOWMOD_DEFAULT_HARD_TIMEOUT);
        }
        tmp = pps.getProperty("tank.sdnos.flowutils.tank_priority");
        if (tmp != null) {
            FLOWMOD_DEFAULT_PRIORITY = Integer.parseInt(tmp);
        } else {
            log.info("tank:default priority is not set in floodlightdefault.properties, using {}",
                    FLOWMOD_DEFAULT_PRIORITY);
        }
        tmp = pps.getProperty("tank.sdnos.flowutils.tank_defaut_table_id");
        if (tmp != null) {
            FLOWMOD_DEFAULT_TABLE_ID = Short.parseShort(tmp);
        } else {
            log.info("tank:default table id is not set in floodlightdefault.properties, using {}",
                    FLOWMOD_DEFAULT_TABLE_ID);
        }
        tmp = pps.getProperty("tank.sdnos.flowutils.tank_send_flow_rem_flag");
        if (tmp != null) {
            if (tmp.equals("false")) {
                FLOWMOD_DEFAULT_SET_SEND_FLOW_REM_FLAG = false;
            } else if (tmp.equals("true")) {
                FLOWMOD_DEFAULT_SET_SEND_FLOW_REM_FLAG = true;
            }
        } else {
            log.info("tank:default idel timeout is not set in floodlightdefault.properties, using {}",
                    FLOWMOD_DEFAULT_SET_SEND_FLOW_REM_FLAG);
        }
    }

    /**
     * build match from String
     *
     * @param match
     *            an String representation of match, such
     *            as:inport=1,ethtype=0x0800
     * @param sw
     *            the IOFSwitch you want to send flowmod message
     * @return Match org.projectfloodlight.openflow.protocol.match.Match
     *
     *
     */
    public static Match buildMatch(IOFSwitch sw, String match) {
        OFVersion ofVersion = sw.getOFFactory().getVersion();

        return MatchUtils.fromString(match, ofVersion);

    }

    /* build actions from String */
    public static List<OFAction> buildActions(IOFSwitch sw, String actions) {
        List<OFAction> actionList = new ArrayList<OFAction>();
        OFActions ofActions = sw.getOFFactory().actions();

        /*
         * org.projectfloodlight.openflow.protocol.action
         *
         * resolve actions from String actions
         *
         */

        // Split into pairs of key=value
        String[] tokens = actions.split("[\\[,\\]]");

        // Split up key=value pairs into [key, value], and insert into
        // array-deque
        int i;
        String[] tmp;
        ArrayDeque<String[]> llValues = new ArrayDeque<String[]>();
        for (i = 0; i < tokens.length; i++) {
            tmp = tokens[i].split("=");
            if (tmp.length != 2) {
                throw new IllegalArgumentException(
                        "tank:Token " + tokens[i] + " does not have form 'key=value' parsing " + actions);
            }
            tmp[0] = tmp[0].toLowerCase(); // try to make key parsing case
                                           // insensitive
            llValues.add(tmp); // llValues contains [key, value] pairs. Create a
                               // queue of pairs to process.
        }

        while (!llValues.isEmpty()) {
            String[] key_value = llValues.pollFirst(); // pop off the first
                                                       // element; this
                                                       // completely removes it
                                                       // from the queue.
            switch (key_value[0]) {
            case "en_queue": /*
                              * en_queue=1:1 the first parameter means port and
                              * the second paremeter means queue id
                              */
                OFActionEnqueue.Builder ab0 = ofActions.buildEnqueue();
                ab0.setPort(OFPort.of(Integer.parseInt(key_value[1].split(":")[1])));
                ab0.setQueueId(Long.parseLong(key_value[1].split(":")[1]));
                actionList.add(ab0.build());
                break;
            case "group":
                OFActionGroup.Builder ab1 = ofActions.buildGroup();
                ab1.setGroup(OFGroup.of(Integer.parseInt(key_value[1])));
                actionList.add(ab1.build());
                break;
            case "meter":
                OFActionMeter.Builder ab2 = ofActions.buildMeter();
                break;
            case "output":
                OFActionOutput.Builder ab3 = ofActions.buildOutput();
                OFPort output_port = MatchUtils.portFromString(key_value[1]);
                ab3.setPort(output_port);
                actionList.add(ab3.build());
                break;
            case "pop_mpls":
                OFActionPopMpls.Builder ab4 = ofActions.buildPopMpls();
                actionList.add(ab4.build());
                break;
            case "pop_pbb":
                break;
            case "pop_vlan":
                OFActionPopVlan ab6 = ofActions.popVlan();
                actionList.add(ab6);
                break;
            case "push_mpls":
                OFActionPushMpls.Builder ab7 = ofActions.buildPushMpls();
                ab7.setEthertype(EthType.of(0x8847));
                actionList.add(ab7.build());
                break;
            case "push_pbb":
                break;
            case "push_vlan":
                OFActionPushVlan.Builder ab9 = ofActions.buildPushVlan();
                ab9.setEthertype(EthType.of(0X8100));
                actionList.add(ab9.build());
                break;
            case "set_dl_dst":
                OFActionSetDlDst.Builder ab10 = ofActions.buildSetDlDst();
                ab10.setDlAddr(MacAddress.of(key_value[1]));
                actionList.add(ab10.build());
                break;
            case "set_dl_src":
                OFActionSetDlSrc.Builder ab11 = ofActions.buildSetDlSrc();
                ab11.setDlAddr(MacAddress.of(key_value[1]));
                actionList.add(ab11.build());
                break;
            case "set_field":
                break;
            case "setMplsLabel":
                OFActionSetMplsLabel.Builder ab13 = ofActions.buildSetMplsLabel();
                ab13.setMplsLabel(Long.parseLong(key_value[1]));
                actionList.add(ab13.build());
                break;
            case "set_mpls_tc":
                break;
            case "set_mpls_ttl":
                break;
            case "set_nw_dst":
                OFActionSetNwDst.Builder ab16 = ofActions.buildSetNwDst();
                ab16.setNwAddr(IPv4Address.of(key_value[1]));
                actionList.add(ab16.build());
                break;
            case "set_nw_ecn":
                break;
            case "set_nw_src":
                OFActionSetNwSrc.Builder ab18 = ofActions.buildSetNwSrc();
                ab18.setNwAddr(IPv4Address.of(key_value[1]));
                actionList.add(ab18.build());
                break;
            case "set_nw_ttl":
                OFActionSetNwTtl.Builder ab19 = ofActions.buildSetNwTtl();
                ab19.setNwTtl(Short.parseShort(key_value[1]));
                actionList.add(ab19.build());
                break;
            case "set_queue":
                break;
            case "set_tp_dst":
                OFActionSetTpDst.Builder ab21 = ofActions.buildSetTpDst();
                ab21.setTpPort(TransportPort.of(Integer.parseInt(key_value[1])));
                actionList.add(ab21.build());
                break;
            case "set_tp_src":
                OFActionSetTpSrc.Builder ab22 = ofActions.buildSetTpSrc();
                ab22.setTpPort(TransportPort.of(Integer.parseInt(key_value[1])));
                actionList.add(ab22.build());
                break;
            case "set_vlan_pcp":
                break;
            case "set_vlan_vid":
                OFActionSetVlanVid.Builder ab24 = ofActions.buildSetVlanVid();
                ab24.setVlanVid(VlanVid.ofVlan(Integer.parseInt(key_value[1])));
                actionList.add(ab24.build());
                break;
            case "strip_vlan":
                OFActionStripVlan ab25 = ofActions.stripVlan();
                actionList.add(ab25);
                break;
            default:
                throw new IllegalArgumentException("tank:unknown token " + key_value + " parsing " + actions);
            }
        }

        return actionList;
    }

    /* flow related */
    static OFFlowMod.Builder makeAddFlow(IOFSwitch sw, String match, String actions) {
        OFFlowMod.Builder fmb;
        fmb = sw.getOFFactory().buildFlowAdd();

        Match ofMatch = buildMatch(sw, match);
        Match.Builder mb = MatchUtils.convertToVersion(ofMatch, sw.getOFFactory().getVersion());
        /* match */
        fmb.setMatch(mb.build());

        /* action */
        List<OFAction> ofActions = buildActions(sw, actions);
        FlowModUtils.setActions(fmb, ofActions, sw);

        return fmb;
    }

    /**
     * add flow using default parameter
     */
    public static boolean addFlow(IOFSwitch sw, String match, String actions) {
        OFFlowMod.Builder fmb = makeAddFlow(sw, match, actions);

        fmb.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT).setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
                .setBufferId(OFBufferId.NO_BUFFER).setPriority(FLOWMOD_DEFAULT_PRIORITY)
                .setTableId(TableId.of(FLOWMOD_DEFAULT_TABLE_ID));

        /* flowmod flags */
        Set<OFFlowModFlags> flags = new HashSet<>();
        flags.add(OFFlowModFlags.SEND_FLOW_REM);
        fmb.setFlags(flags);

        sw.write(fmb.build());

        return true;
    }

    /* add flow using your parameter */
    public static boolean addFlow(IOFSwitch sw, String match, String actions, int idel_timeout, int hard_timeout,
            int priority) {
        OFFlowMod.Builder fmb = makeAddFlow(sw, match, actions);

        fmb.setIdleTimeout(idel_timeout).setHardTimeout(hard_timeout).setBufferId(OFBufferId.NO_BUFFER)
                .setPriority(priority).setTableId(TableId.of(FLOWMOD_DEFAULT_TABLE_ID));

        /* flowmod flags */
        Set<OFFlowModFlags> flags = new HashSet<>();
        if (FLOWMOD_DEFAULT_SET_SEND_FLOW_REM_FLAG) {
            flags.add(OFFlowModFlags.SEND_FLOW_REM);
            fmb.setFlags(flags);
        }

        sw.write(fmb.build());

        return true;
    }

    /* add flow using your parameter */
    public static boolean addFlow(IOFSwitch sw, String match, String actions, int idel_timeout, int hard_timeout,
            int priority, short tableId) {
        OFFlowMod.Builder fmb = makeAddFlow(sw, match, actions);

        fmb.setIdleTimeout(idel_timeout).setHardTimeout(hard_timeout).setBufferId(OFBufferId.NO_BUFFER)
                .setPriority(priority).setTableId(TableId.of(tableId));

        /* flowmod flags */
        Set<OFFlowModFlags> flags = new HashSet<>();
        if (FLOWMOD_DEFAULT_SET_SEND_FLOW_REM_FLAG) {
            flags.add(OFFlowModFlags.SEND_FLOW_REM);
            fmb.setFlags(flags);
        }

        sw.write(fmb.build());

        return true;
    }

    /* add flow using your parameter */
    public static boolean addFlow(IOFSwitch sw, String match, String actions, int idel_timeout, int hard_timeout,
            int priority, short tableId, boolean setRemFlag) {
        OFFlowMod.Builder fmb = makeAddFlow(sw, match, actions);

        fmb.setIdleTimeout(idel_timeout).setHardTimeout(hard_timeout).setBufferId(OFBufferId.NO_BUFFER)
                .setPriority(priority).setTableId(TableId.of(tableId));

        /* flowmod flags */
        Set<OFFlowModFlags> flags = new HashSet<>();
        if (setRemFlag) {
            flags.add(OFFlowModFlags.SEND_FLOW_REM);
            fmb.setFlags(flags);
        }

        sw.write(fmb.build());

        return true;
    }

    /* add flow using your parameter */
    public static boolean addFlow(IOFSwitch sw, String match, String actions, int idel_timeout, int hard_timeout,
            int priority, short tableId, boolean setRemFlag, U64 cookie) {
        OFFlowMod.Builder fmb = makeAddFlow(sw, match, actions);

        fmb.setIdleTimeout(idel_timeout).setHardTimeout(hard_timeout).setBufferId(OFBufferId.NO_BUFFER)
                .setPriority(priority).setTableId(TableId.of(tableId));

        /* flowmod flags */
        Set<OFFlowModFlags> flags = new HashSet<>();
        if (setRemFlag) {
            flags.add(OFFlowModFlags.SEND_FLOW_REM);
            fmb.setFlags(flags);
        }
        fmb.setCookie(cookie);

        sw.write(fmb.build());

        return true;
    }

    @SuppressWarnings("uncheckd")
    static OFFlowMod.Builder makeDel_gen(OFFlowMod.Builder fm, IOFSwitch sw, String fieldMatch, String otherMatch) {
        if (fieldMatch != null) {
            /* set filed match */
            Match match = MatchUtils.fromString(fieldMatch, sw.getOFFactory().getVersion());
            fm.setMatch(match);
        }

        if (otherMatch == null) {
            return fm;
        }
        /* set other match */
        String[] tokens = otherMatch.split("[\\[,\\]]"); // Split into pairs of
                                                         // key=value

        // Split up key=value pairs into [key, value], and insert into
        // array-deque
        int i;
        String[] tmp;
        ArrayDeque<String[]> llValues = new ArrayDeque<String[]>();
        for (i = 0; i < tokens.length; i++) {
            tmp = tokens[i].split("=");
            if (tmp.length != 2) {
                throw new IllegalArgumentException(
                        "tank:Token " + tokens[i] + " does not have form 'key=value' parsing " + otherMatch);
            }
            tmp[0] = tmp[0].toLowerCase(); // try to make key parsing case
                                           // insensitive
            llValues.add(tmp); // llValues contains [key, value] pairs. Create a
                               // queue of pairs to process.
        }

        while (!llValues.isEmpty()) {
            String[] key_value = llValues.pollFirst(); // pop off the first
                                                       // element; this
                                                       // completely removes it
                                                       // from the queue.
            switch (key_value[0]) {
            case "xid":
                fm.setXid(Long.parseLong(key_value[1]));
                break;
            case "cookie":
                break;
            case "cookie_mask":
                break;
            case "table_id":
                fm.setTableId(TableId.of(Short.parseShort(key_value[1])));
                break;
            case "idel_timeout":
                fm.setIdleTimeout(Integer.parseInt(key_value[1]));
                break;
            case "hard_timeout":
                fm.setHardTimeout(Integer.parseInt(key_value[1]));
                break;
            case "priority":
                fm.setPriority(Integer.parseInt(key_value[1]));
                break;
            case "buffer_id":
                break;
            case "output_port":
                OFPort output_port = MatchUtils.portFromString(key_value[1]);
                fm.setOutPort(output_port);
                break;
            case "out_group":
                fm.setOutGroup(OFGroup.of(Integer.parseInt(key_value[1])));
                break;
            case "flags":
                break;
            case "Instructions":
                break;
            case "actions":
                break;
            case "import":
                break;
            default:
                throw new IllegalArgumentException("tank:unknown token " + key_value + " parsing " + otherMatch);
            }
        }

        return fm;
    }

    static OFFlowDelete makeDelFlow(IOFSwitch sw, String fieldMatch, String otherMatch) {
        OFFlowDelete.Builder deleteFlow = sw.getOFFactory().buildFlowDelete();
        deleteFlow = (OFFlowDelete.Builder) makeDel_gen(deleteFlow, sw, fieldMatch, otherMatch);

        return deleteFlow.build();
    }

    /*
     * FieldMatch means the openflow1.3 match fields, otherMatch means
     * idel_timeout/priority etc.
     */
    static OFFlowDeleteStrict makeDelStrictFlow(IOFSwitch sw, String fieldMatch, String otherMatch) {
        OFFlowDeleteStrict.Builder deleteFlow = sw.getOFFactory().buildFlowDeleteStrict();
        deleteFlow = (OFFlowDeleteStrict.Builder) makeDel_gen(deleteFlow, sw, fieldMatch, otherMatch);

        return deleteFlow.build();
    }

    public static boolean modifyFlow(IOFSwitch sw, String fieldMatch, String otherMatch, String actions) {
        OFFlowModify.Builder modb = sw.getOFFactory().buildFlowModify();
        modb = (OFFlowModify.Builder) makeDel_gen(modb, sw, fieldMatch, otherMatch);
        modb.setActions(buildActions(sw, actions));
        sw.write(modb.build());

        return true;
    }

    public static boolean modifyStrictFlow(IOFSwitch sw, String fieldMatch, String otherMatch, String actions) {
        OFFlowModifyStrict.Builder modb = sw.getOFFactory().buildFlowModifyStrict();
        modb = (OFFlowModifyStrict.Builder) makeDel_gen(modb, sw, fieldMatch, otherMatch);
        modb.setActions(buildActions(sw, actions));
        sw.write(modb.build());

        return true;
    }

    public static boolean delFlow(IOFSwitch sw, String fieldMatch, String otherMatch) {
        OFFlowDelete flowDelete = makeDelFlow(sw, fieldMatch, otherMatch);
        sw.write(flowDelete);
        return true;
    }

    /* FieldMatch means the openflow1.3 match fields */
    public static boolean delFlow(IOFSwitch sw, String match, boolean isFieldMatch) {
        OFFlowDelete flowDelete;
        if (isFieldMatch) {
            flowDelete = makeDelFlow(sw, match, null);
        } else {
            flowDelete = makeDelFlow(sw, null, match);
        }
        sw.write(flowDelete);
        return true;
    }

    public static boolean delStrictFlow(IOFSwitch sw, String fieldMatch, String otherMatch) {
        OFFlowDeleteStrict flowDeleteStrict = makeDelStrictFlow(sw, fieldMatch, otherMatch);
        sw.write(flowDeleteStrict);
        return true;
    }

    /* FieldMatch means the openflow1.3 match fields */
    public static boolean delStrictFlow(IOFSwitch sw, String match, boolean isFieldMatch) {
        OFFlowDeleteStrict flowDeleteStrict;
        if (isFieldMatch) {
            flowDeleteStrict = makeDelStrictFlow(sw, match, null);
        } else {
            flowDeleteStrict = makeDelStrictFlow(sw, null, match);
        }
        sw.write(flowDeleteStrict);
        return true;
    }

    public static boolean delAllFlows(IOFSwitch sw) {

        OFFlowDelete deleteFlows = sw.getOFFactory().buildFlowDelete().setTableId(TableId.ALL).build();
        sw.write(deleteFlows);

        return true;
    }

    /* group related */
    static OFGroupAdd.Builder makeGroupFlow(IOFSwitch sw, int groupNumber, OFGroupType groupType,
            List<OFBucket> buckets) {
        OFGroupAdd.Builder gb;
        gb = sw.getOFFactory().buildGroupAdd();
        gb.setGroup(OFGroup.of(groupNumber));
        gb.setGroupType(groupType);
        gb.setBuckets(buckets);

        return gb;
    }

    public static boolean addAllGroup(IOFSwitch sw, int groupNumber, List<String> actionBuckets) {
        OFGroupType gType = OFGroupType.ALL;
        List<OFBucket> buckets = new ArrayList<OFBucket>();

        for (String actionBucket : actionBuckets) {
            OFBucket.Builder ofBucketBuilder = null;
            ofBucketBuilder = sw.getOFFactory().buildBucket().setActions(buildActions(sw, actionBucket));
            ofBucketBuilder.setWatchGroup(OFGroup.ANY);
            ofBucketBuilder.setWatchPort(OFPort.ANY);
            buckets.add(ofBucketBuilder.build());
        }
        sw.write(makeGroupFlow(sw, groupNumber, gType, buckets).build());

        return true;
    }

    /*
     * Map<String,String> actionBuckets : the key is action and value is weight
     */
    public static boolean addSelectGroup(IOFSwitch sw, int groupNumber, Map<String, String> actionBuckets) {
        OFGroupType gType = OFGroupType.SELECT;
        List<OFBucket> buckets = new ArrayList<OFBucket>();

        for (String action : actionBuckets.keySet()) {
            OFBucket.Builder ofBucketBuilder = null;
            ofBucketBuilder = sw.getOFFactory().buildBucket().setActions(buildActions(sw, action));
            ofBucketBuilder.setWatchGroup(OFGroup.ANY);
            ofBucketBuilder.setWatchPort(OFPort.ANY);
            int weight = Integer.parseInt(actionBuckets.get(action));
            ofBucketBuilder.setWeight(weight);
            buckets.add(ofBucketBuilder.build());
        }
        sw.write(makeGroupFlow(sw, groupNumber, gType, buckets).build());

        return true;
    }

    public static boolean addIndirectGroup(IOFSwitch sw, int groupNumber, List<String> actionBuckets) {
        OFGroupType gType = OFGroupType.INDIRECT;
        List<OFBucket> buckets = new ArrayList<OFBucket>();

        for (String actionBucket : actionBuckets) {
            OFBucket.Builder ofBucketBuilder = null;
            ofBucketBuilder = sw.getOFFactory().buildBucket().setActions(buildActions(sw, actionBucket));
            ofBucketBuilder.setWatchGroup(OFGroup.ANY);
            ofBucketBuilder.setWatchPort(OFPort.ANY);
            buckets.add(ofBucketBuilder.build());
        }
        sw.write(makeGroupFlow(sw, groupNumber, gType, buckets).build());

        return true;
    }

    /*
     * Map<String,String> actionBuckets : the key is action and value is a
     * string for watch port and group, but the format is such as
     * watchPort=1,watchGroup=1
     */
    public static boolean addFfGroup(IOFSwitch sw, int groupNumber, Map<String, String> actionBuckets) {
        OFGroupType gType = OFGroupType.FF;
        List<OFBucket> buckets = new ArrayList<OFBucket>();

        for (String action : actionBuckets.keySet()) {
            OFBucket.Builder ofBucketBuilder = null;
            ofBucketBuilder = sw.getOFFactory().buildBucket().setActions(buildActions(sw, action));
            String watch = actionBuckets.get(action);
            List<String> watchs = Arrays.asList(watch.split(","));
            for (String watchDetail : watchs) {
                String[] detail = watchDetail.split("=");
                if (detail[0].trim() == "watchPort") {
                    if (detail[1].trim() == "any") {
                        ofBucketBuilder.setWatchPort(OFPort.ANY);
                    } else {
                        ofBucketBuilder.setWatchPort(OFPort.of(Integer.parseInt(detail[1])));
                    }
                } else if (detail[0].trim() == "watchGroup") {
                    if (detail[1].trim() == "any") {
                        ofBucketBuilder.setWatchGroup(OFGroup.ANY);
                    } else {
                        ofBucketBuilder.setWatchGroup(OFGroup.of(Integer.parseInt(detail[1])));
                    }
                }
            }
            buckets.add(ofBucketBuilder.build());
        }

        sw.write(makeGroupFlow(sw, groupNumber, gType, buckets).build());

        return true;
    }

    static OFGroupDelete makeDelGroup(IOFSwitch sw, int groupNumber) {
        OFGroupDelete.Builder delGroup = sw.getOFFactory().buildGroupDelete();
        delGroup.setGroup(OFGroup.of(groupNumber));

        return delGroup.build();
    }

    public static boolean modifyGroup(IOFSwitch sw, int groupNumber, String actionBucket) {
        OFGroupModify.Builder modGroup = sw.getOFFactory().buildGroupModify();
        modGroup.setGroup(OFGroup.of(groupNumber));
        modGroup.setGroupType(OFGroupType.ALL);

        List<OFBucket> buckets = new ArrayList<OFBucket>();
        List<OFAction> actions = buildActions(sw, actionBucket);
        buckets.add(sw.getOFFactory().buildBucket().setActions(actions).setWatchGroup(OFGroup.ANY)
                .setWatchPort(OFPort.ANY).build());

        modGroup.setBuckets(buckets);
        sw.write(modGroup.build());

        return true;
    }

    public static boolean delGroup(IOFSwitch sw, int groupNumber) {
        OFGroupDelete delGroup = makeDelGroup(sw, groupNumber);
        sw.write(delGroup);

        return true;
    }

    public static boolean delAllGroups(IOFSwitch sw) {

        OFGroupDelete.Builder delgroup = sw.getOFFactory().buildGroupDelete().setGroup(OFGroup.ALL)
                .setGroupType(OFGroupType.ALL);

        sw.write(delgroup.build());

        /*
         * UNSUPPORT GROUP TYPE FOR RUIJIE SWITCH
         * delgroup.setGroupType(OFGroupType.FF);
         * this.sw.write(delgroup.build());
         */

        /*
         * INVALID GROUP FOR INDIRECT GROUP TYPE WHEN DELETE INDIRECT GROUP FOR
         * RUIJIE SWITCH delgroup.setGroupType(OFGroupType.INDIRECT);
         * this.sw.write(delgroup.build());
         */

        delgroup.setGroupType(OFGroupType.SELECT);
        sw.write(delgroup.build());

        return true;
    }

    /* meter related */

    public static boolean delAllmeter(IOFSwitch sw) {
        return false;

    }









    public static void main(String[] args) {
        FlowUtils flowUtils = new FlowUtils();
    }

}
