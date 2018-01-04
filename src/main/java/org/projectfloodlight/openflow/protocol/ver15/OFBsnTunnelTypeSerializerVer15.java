// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template const_set_serializer.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver15;

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
import org.projectfloodlight.openflow.protocol.OFBsnTunnelType;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;
import java.util.EnumSet;
import java.util.Collections;


public class OFBsnTunnelTypeSerializerVer15 {

    public final static long BSN_TUNNEL_L2GRE_VAL = 0x1L;

    public static Set<OFBsnTunnelType> readFrom(ByteBuf bb) throws OFParseError {
        try {
            return ofWireValue(bb.readLong());
        } catch (IllegalArgumentException e) {
            throw new OFParseError(e);
        }
    }

    public static void writeTo(ByteBuf bb, Set<OFBsnTunnelType> set) {
        bb.writeLong(toWireValue(set));
    }

    public static void putTo(Set<OFBsnTunnelType> set, PrimitiveSink sink) {
        sink.putLong(toWireValue(set));
    }


    public static Set<OFBsnTunnelType> ofWireValue(long val) {
        EnumSet<OFBsnTunnelType> set = EnumSet.noneOf(OFBsnTunnelType.class);

        if((val & BSN_TUNNEL_L2GRE_VAL) != 0)
            set.add(OFBsnTunnelType.BSN_TUNNEL_L2GRE);
        return Collections.unmodifiableSet(set);
    }

    public static long toWireValue(Set<OFBsnTunnelType> set) {
        long wireValue = 0;

        for(OFBsnTunnelType e: set) {
            switch(e) {
                case BSN_TUNNEL_L2GRE:
                    wireValue |= BSN_TUNNEL_L2GRE_VAL;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal enum value for type OFBsnTunnelType in version 1.5: " + e);
            }
        }
        return wireValue;
    }

}
