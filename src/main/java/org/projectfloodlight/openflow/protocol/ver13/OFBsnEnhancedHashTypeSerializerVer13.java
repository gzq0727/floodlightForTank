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
import org.projectfloodlight.openflow.protocol.OFBsnEnhancedHashType;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;
import java.util.EnumSet;
import java.util.Collections;


public class OFBsnEnhancedHashTypeSerializerVer13 {

    public final static long BSN_ENHANCED_HASH_L2_VAL = 0x1L;
    public final static long BSN_ENHANCED_HASH_L3_VAL = 0x2L;
    public final static long BSN_ENHANCED_HASH_L2GRE_VAL = 0x4L;
    public final static long BSN_ENHANCED_HASH_MPLS_VAL = 0x8L;
    public final static long BSN_ENHANCED_HASH_GTP_VAL = 0x10L;
    public final static long BSN_ENHANCED_HASH_SYMMETRIC_VAL = 0x20L;

    public static Set<OFBsnEnhancedHashType> readFrom(ByteBuf bb) throws OFParseError {
        try {
            return ofWireValue(bb.readLong());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ByteBuf bb, Set<OFBsnEnhancedHashType> set) {
        bb.writeLong(toWireValue(set));
    }

    public static void putTo(Set<OFBsnEnhancedHashType> set, PrimitiveSink sink) {
        sink.putLong(toWireValue(set));
    }


    public static Set<OFBsnEnhancedHashType> ofWireValue(long val) {
        EnumSet<OFBsnEnhancedHashType> set = EnumSet.noneOf(OFBsnEnhancedHashType.class);

        if((val & BSN_ENHANCED_HASH_L2_VAL) != 0)
            set.add(OFBsnEnhancedHashType.BSN_ENHANCED_HASH_L2);
        if((val & BSN_ENHANCED_HASH_L3_VAL) != 0)
            set.add(OFBsnEnhancedHashType.BSN_ENHANCED_HASH_L3);
        if((val & BSN_ENHANCED_HASH_L2GRE_VAL) != 0)
            set.add(OFBsnEnhancedHashType.BSN_ENHANCED_HASH_L2GRE);
        if((val & BSN_ENHANCED_HASH_MPLS_VAL) != 0)
            set.add(OFBsnEnhancedHashType.BSN_ENHANCED_HASH_MPLS);
        if((val & BSN_ENHANCED_HASH_GTP_VAL) != 0)
            set.add(OFBsnEnhancedHashType.BSN_ENHANCED_HASH_GTP);
        if((val & BSN_ENHANCED_HASH_SYMMETRIC_VAL) != 0)
            set.add(OFBsnEnhancedHashType.BSN_ENHANCED_HASH_SYMMETRIC);
        return Collections.unmodifiableSet(set);
    }

    public static long toWireValue(Set<OFBsnEnhancedHashType> set) {
        long wireValue = 0;

        for(OFBsnEnhancedHashType e: set) {
            switch(e) {
                case BSN_ENHANCED_HASH_L2:
                    wireValue |= BSN_ENHANCED_HASH_L2_VAL;
                    break;
                case BSN_ENHANCED_HASH_L3:
                    wireValue |= BSN_ENHANCED_HASH_L3_VAL;
                    break;
                case BSN_ENHANCED_HASH_L2GRE:
                    wireValue |= BSN_ENHANCED_HASH_L2GRE_VAL;
                    break;
                case BSN_ENHANCED_HASH_MPLS:
                    wireValue |= BSN_ENHANCED_HASH_MPLS_VAL;
                    break;
                case BSN_ENHANCED_HASH_GTP:
                    wireValue |= BSN_ENHANCED_HASH_GTP_VAL;
                    break;
                case BSN_ENHANCED_HASH_SYMMETRIC:
                    wireValue |= BSN_ENHANCED_HASH_SYMMETRIC_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OFBsnEnhancedHashType in version 1.3: " + e);
            }
        }
        return wireValue;
    }

}
