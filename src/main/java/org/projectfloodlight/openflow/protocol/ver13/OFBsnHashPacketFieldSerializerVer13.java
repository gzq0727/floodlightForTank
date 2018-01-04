// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_set_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver13;

import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.actionid.*;
import org.projectfloodlight.openflow.protocol.bsntlv.*;
import org.projectfloodlight.openflow.protocol.errormsg.*;
import org.projectfloodlight.openflow.protocol.meterband.*;
import org.projectfloodlight.openflow.protocol.instruction.*;
import org.projectfloodlight.openflow.protocol.instructionid.*;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.protocol.stat.*;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.protocol.oxs.*;
import org.projectfloodlight.openflow.protocol.queueprop.*;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.util.*;
import org.projectfloodlight.openflow.exceptions.*;
import org.projectfloodlight.openflow.protocol.OFBsnHashPacketField;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;
import java.util.EnumSet;
import java.util.Collections;


public class OFBsnHashPacketFieldSerializerVer13 {

    public final static long BSN_HASH_FIELD_DST_MAC_VAL = 0x2L;
    public final static long BSN_HASH_FIELD_SRC_MAC_VAL = 0x4L;
    public final static long BSN_HASH_FIELD_ETH_TYPE_VAL = 0x8L;
    public final static long BSN_HASH_FIELD_VLAN_ID_VAL = 0x10L;
    public final static long BSN_HASH_FIELD_INNER_L2_VAL = 0x20L;
    public final static long BSN_HASH_FIELD_INNER_L3_VAL = 0x40L;
    public final static long BSN_HASH_FIELD_SRC_IP_VAL = 0x80L;
    public final static long BSN_HASH_FIELD_DST_IP_VAL = 0x100L;
    public final static long BSN_HASH_FIELD_IP_PROTO_VAL = 0x200L;
    public final static long BSN_HASH_FIELD_SRC_L4_PORT_VAL = 0x400L;
    public final static long BSN_HASH_FIELD_DST_L4_PORT_VAL = 0x800L;
    public final static long BSN_HASH_FIELD_MPLS_LABEL1_VAL = 0x1000L;
    public final static long BSN_HASH_FIELD_MPLS_LABEL2_VAL = 0x2000L;
    public final static long BSN_HASH_FIELD_MPLS_LABEL3_VAL = 0x4000L;
    public final static long BSN_HASH_FIELD_MPLS_LABEL_HI_BITS_VAL = 0x8000L;
    public final static long BSN_HASH_FIELD_MPLS_PAYLOAD_SRC_IP_VAL = 0x10000L;
    public final static long BSN_HASH_FIELD_MPLS_PAYLOAD_DST_IP_VAL = 0x20000L;
    public final static long BSN_HASH_FIELD_SYMMETRIC_VAL = 0x40000L;

    public static Set<OFBsnHashPacketField> readFrom(ByteBuf bb) throws OFParseError {
        try {
            return ofWireValue(bb.readLong());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ByteBuf bb, Set<OFBsnHashPacketField> set) {
        bb.writeLong(toWireValue(set));
    }

    public static void putTo(Set<OFBsnHashPacketField> set, PrimitiveSink sink) {
        sink.putLong(toWireValue(set));
    }


    public static Set<OFBsnHashPacketField> ofWireValue(long val) {
        EnumSet<OFBsnHashPacketField> set = EnumSet.noneOf(OFBsnHashPacketField.class);

        if((val & BSN_HASH_FIELD_DST_MAC_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_DST_MAC);
        if((val & BSN_HASH_FIELD_SRC_MAC_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_SRC_MAC);
        if((val & BSN_HASH_FIELD_ETH_TYPE_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_ETH_TYPE);
        if((val & BSN_HASH_FIELD_VLAN_ID_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_VLAN_ID);
        if((val & BSN_HASH_FIELD_INNER_L2_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_INNER_L2);
        if((val & BSN_HASH_FIELD_INNER_L3_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_INNER_L3);
        if((val & BSN_HASH_FIELD_SRC_IP_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_SRC_IP);
        if((val & BSN_HASH_FIELD_DST_IP_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_DST_IP);
        if((val & BSN_HASH_FIELD_IP_PROTO_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_IP_PROTO);
        if((val & BSN_HASH_FIELD_SRC_L4_PORT_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_SRC_L4_PORT);
        if((val & BSN_HASH_FIELD_DST_L4_PORT_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_DST_L4_PORT);
        if((val & BSN_HASH_FIELD_MPLS_LABEL1_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_MPLS_LABEL1);
        if((val & BSN_HASH_FIELD_MPLS_LABEL2_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_MPLS_LABEL2);
        if((val & BSN_HASH_FIELD_MPLS_LABEL3_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_MPLS_LABEL3);
        if((val & BSN_HASH_FIELD_MPLS_LABEL_HI_BITS_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_MPLS_LABEL_HI_BITS);
        if((val & BSN_HASH_FIELD_MPLS_PAYLOAD_SRC_IP_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_MPLS_PAYLOAD_SRC_IP);
        if((val & BSN_HASH_FIELD_MPLS_PAYLOAD_DST_IP_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_MPLS_PAYLOAD_DST_IP);
        if((val & BSN_HASH_FIELD_SYMMETRIC_VAL) != 0)
            set.add(OFBsnHashPacketField.BSN_HASH_FIELD_SYMMETRIC);
        return Collections.unmodifiableSet(set);
    }

    public static long toWireValue(Set<OFBsnHashPacketField> set) {
        long wireValue = 0;

        for(OFBsnHashPacketField e: set) {
            switch(e) {
                case BSN_HASH_FIELD_DST_MAC:
                    wireValue |= BSN_HASH_FIELD_DST_MAC_VAL;
                    break;
                case BSN_HASH_FIELD_SRC_MAC:
                    wireValue |= BSN_HASH_FIELD_SRC_MAC_VAL;
                    break;
                case BSN_HASH_FIELD_ETH_TYPE:
                    wireValue |= BSN_HASH_FIELD_ETH_TYPE_VAL;
                    break;
                case BSN_HASH_FIELD_VLAN_ID:
                    wireValue |= BSN_HASH_FIELD_VLAN_ID_VAL;
                    break;
                case BSN_HASH_FIELD_INNER_L2:
                    wireValue |= BSN_HASH_FIELD_INNER_L2_VAL;
                    break;
                case BSN_HASH_FIELD_INNER_L3:
                    wireValue |= BSN_HASH_FIELD_INNER_L3_VAL;
                    break;
                case BSN_HASH_FIELD_SRC_IP:
                    wireValue |= BSN_HASH_FIELD_SRC_IP_VAL;
                    break;
                case BSN_HASH_FIELD_DST_IP:
                    wireValue |= BSN_HASH_FIELD_DST_IP_VAL;
                    break;
                case BSN_HASH_FIELD_IP_PROTO:
                    wireValue |= BSN_HASH_FIELD_IP_PROTO_VAL;
                    break;
                case BSN_HASH_FIELD_SRC_L4_PORT:
                    wireValue |= BSN_HASH_FIELD_SRC_L4_PORT_VAL;
                    break;
                case BSN_HASH_FIELD_DST_L4_PORT:
                    wireValue |= BSN_HASH_FIELD_DST_L4_PORT_VAL;
                    break;
                case BSN_HASH_FIELD_MPLS_LABEL1:
                    wireValue |= BSN_HASH_FIELD_MPLS_LABEL1_VAL;
                    break;
                case BSN_HASH_FIELD_MPLS_LABEL2:
                    wireValue |= BSN_HASH_FIELD_MPLS_LABEL2_VAL;
                    break;
                case BSN_HASH_FIELD_MPLS_LABEL3:
                    wireValue |= BSN_HASH_FIELD_MPLS_LABEL3_VAL;
                    break;
                case BSN_HASH_FIELD_MPLS_LABEL_HI_BITS:
                    wireValue |= BSN_HASH_FIELD_MPLS_LABEL_HI_BITS_VAL;
                    break;
                case BSN_HASH_FIELD_MPLS_PAYLOAD_SRC_IP:
                    wireValue |= BSN_HASH_FIELD_MPLS_PAYLOAD_SRC_IP_VAL;
                    break;
                case BSN_HASH_FIELD_MPLS_PAYLOAD_DST_IP:
                    wireValue |= BSN_HASH_FIELD_MPLS_PAYLOAD_DST_IP_VAL;
                    break;
                case BSN_HASH_FIELD_SYMMETRIC:
                    wireValue |= BSN_HASH_FIELD_SYMMETRIC_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OFBsnHashPacketField in version 1.3: " + e);
            }
        }
        return wireValue;
    }

}
