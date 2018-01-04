// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver12;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import io.netty.buffer.ByteBuf;
import com.google.common.hash.PrimitiveSink;
import com.google.common.hash.Funnel;

class OFOxmConnTrackingNwProtoVer12 implements OFOxmConnTrackingNwProto {
    private static final Logger logger = LoggerFactory.getLogger(OFOxmConnTrackingNwProtoVer12.class);
    // version: 1.2
    final static byte WIRE_VERSION = 3;
    final static int LENGTH = 5;

        private final static U8 DEFAULT_VALUE = U8.ZERO;

    // OF message fields
    private final U8 value;
//
    // Immutable default instance
    final static OFOxmConnTrackingNwProtoVer12 DEFAULT = new OFOxmConnTrackingNwProtoVer12(
        DEFAULT_VALUE
    );

    // package private constructor - used by readers, builders, and factory
    OFOxmConnTrackingNwProtoVer12(U8 value) {
        if(value == null) {
            throw new NullPointerException("OFOxmConnTrackingNwProtoVer12: property value cannot be null");
        }
        this.value = value;
    }

    // Accessors for OF message fields
    @Override
    public long getTypeLen() {
        return 0x1ee01L;
    }

    @Override
    public U8 getValue() {
        return value;
    }

    @Override
    public MatchField<U8> getMatchField() {
        return MatchField.CONN_TRACKING_NW_PROTO;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    public OFOxm<U8> getCanonical() {
        // exact match OXM is always canonical
        return this;
    }

    @Override
    public U8 getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.2");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_12;
    }



    public OFOxmConnTrackingNwProto.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFOxmConnTrackingNwProto.Builder {
        final OFOxmConnTrackingNwProtoVer12 parentMessage;

        // OF message fields
        private boolean valueSet;
        private U8 value;

        BuilderWithParent(OFOxmConnTrackingNwProtoVer12 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public long getTypeLen() {
        return 0x1ee01L;
    }

    @Override
    public U8 getValue() {
        return value;
    }

    @Override
    public OFOxmConnTrackingNwProto.Builder setValue(U8 value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public MatchField<U8> getMatchField() {
        return MatchField.CONN_TRACKING_NW_PROTO;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    @Override
    public OFOxm<U8> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.2");
    }

    @Override
    public U8 getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.2");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_12;
    }



        @Override
        public OFOxmConnTrackingNwProto build() {
                U8 value = this.valueSet ? this.value : parentMessage.value;
                if(value == null)
                    throw new NullPointerException("Property value must not be null");

                //
                return new OFOxmConnTrackingNwProtoVer12(
                    value
                );
        }

    }

    static class Builder implements OFOxmConnTrackingNwProto.Builder {
        // OF message fields
        private boolean valueSet;
        private U8 value;

    @Override
    public long getTypeLen() {
        return 0x1ee01L;
    }

    @Override
    public U8 getValue() {
        return value;
    }

    @Override
    public OFOxmConnTrackingNwProto.Builder setValue(U8 value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public MatchField<U8> getMatchField() {
        return MatchField.CONN_TRACKING_NW_PROTO;
    }

    @Override
    public boolean isMasked() {
        return false;
    }

    @Override
    public OFOxm<U8> getCanonical()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property canonical not supported in version 1.2");
    }

    @Override
    public U8 getMask()throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Property mask not supported in version 1.2");
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_12;
    }

//
        @Override
        public OFOxmConnTrackingNwProto build() {
            U8 value = this.valueSet ? this.value : DEFAULT_VALUE;
            if(value == null)
                throw new NullPointerException("Property value must not be null");


            return new OFOxmConnTrackingNwProtoVer12(
                    value
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFOxmConnTrackingNwProto> {
        @Override
        public OFOxmConnTrackingNwProto readFrom(ByteBuf bb) throws OFParseError {
            // fixed value property typeLen == 0x1ee01L
            int typeLen = bb.readInt();
            if(typeLen != 0x1ee01)
                throw new OFParseError("Wrong typeLen: Expected=0x1ee01L(0x1ee01L), got="+typeLen);
            U8 value = U8.of(bb.readByte());

            OFOxmConnTrackingNwProtoVer12 oxmConnTrackingNwProtoVer12 = new OFOxmConnTrackingNwProtoVer12(
                    value
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", oxmConnTrackingNwProtoVer12);
            return oxmConnTrackingNwProtoVer12;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFOxmConnTrackingNwProtoVer12Funnel FUNNEL = new OFOxmConnTrackingNwProtoVer12Funnel();
    static class OFOxmConnTrackingNwProtoVer12Funnel implements Funnel<OFOxmConnTrackingNwProtoVer12> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFOxmConnTrackingNwProtoVer12 message, PrimitiveSink sink) {
            // fixed value property typeLen = 0x1ee01L
            sink.putInt(0x1ee01);
            message.value.putTo(sink);
        }
    }


    public void writeTo(ByteBuf bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFOxmConnTrackingNwProtoVer12> {
        @Override
        public void write(ByteBuf bb, OFOxmConnTrackingNwProtoVer12 message) {
            // fixed value property typeLen = 0x1ee01L
            bb.writeInt(0x1ee01);
            bb.writeByte(message.value.getRaw());


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFOxmConnTrackingNwProtoVer12(");
        b.append("value=").append(value);
        b.append(")");
        return b.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFOxmConnTrackingNwProtoVer12 other = (OFOxmConnTrackingNwProtoVer12) obj;

        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

}
