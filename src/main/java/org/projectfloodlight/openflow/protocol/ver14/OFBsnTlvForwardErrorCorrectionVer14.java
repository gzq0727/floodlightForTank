// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
// Copyright (c) 2011, 2012 Open Networking Foundation
// Copyright (c) 2012, 2013 Big Switch Networks, Inc.
// This library was generated by the LoxiGen Compiler.
// See the file LICENSE.txt which should have been included in the source distribution

// Automatically generated by LOXI from template of_class.java
// Do not modify

package org.projectfloodlight.openflow.protocol.ver14;

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

class OFBsnTlvForwardErrorCorrectionVer14 implements OFBsnTlvForwardErrorCorrection {
    private static final Logger logger = LoggerFactory.getLogger(OFBsnTlvForwardErrorCorrectionVer14.class);
    // version: 1.4
    final static byte WIRE_VERSION = 5;
    final static int LENGTH = 5;


    // OF message fields
    private final OFBsnForwardErrorCorrectionType value;
//

    // package private constructor - used by readers, builders, and factory
    OFBsnTlvForwardErrorCorrectionVer14(OFBsnForwardErrorCorrectionType value) {
        if(value == null) {
            throw new NullPointerException("OFBsnTlvForwardErrorCorrectionVer14: property value cannot be null");
        }
        this.value = value;
    }

    // Accessors for OF message fields
    @Override
    public int getType() {
        return 0x95;
    }

    @Override
    public OFBsnForwardErrorCorrectionType getValue() {
        return value;
    }

    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }



    public OFBsnTlvForwardErrorCorrection.Builder createBuilder() {
        return new BuilderWithParent(this);
    }

    static class BuilderWithParent implements OFBsnTlvForwardErrorCorrection.Builder {
        final OFBsnTlvForwardErrorCorrectionVer14 parentMessage;

        // OF message fields
        private boolean valueSet;
        private OFBsnForwardErrorCorrectionType value;

        BuilderWithParent(OFBsnTlvForwardErrorCorrectionVer14 parentMessage) {
            this.parentMessage = parentMessage;
        }

    @Override
    public int getType() {
        return 0x95;
    }

    @Override
    public OFBsnForwardErrorCorrectionType getValue() {
        return value;
    }

    @Override
    public OFBsnTlvForwardErrorCorrection.Builder setValue(OFBsnForwardErrorCorrectionType value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }



        @Override
        public OFBsnTlvForwardErrorCorrection build() {
                OFBsnForwardErrorCorrectionType value = this.valueSet ? this.value : parentMessage.value;
                if(value == null)
                    throw new NullPointerException("Property value must not be null");

                //
                return new OFBsnTlvForwardErrorCorrectionVer14(
                    value
                );
        }

    }

    static class Builder implements OFBsnTlvForwardErrorCorrection.Builder {
        // OF message fields
        private boolean valueSet;
        private OFBsnForwardErrorCorrectionType value;

    @Override
    public int getType() {
        return 0x95;
    }

    @Override
    public OFBsnForwardErrorCorrectionType getValue() {
        return value;
    }

    @Override
    public OFBsnTlvForwardErrorCorrection.Builder setValue(OFBsnForwardErrorCorrectionType value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    @Override
    public OFVersion getVersion() {
        return OFVersion.OF_14;
    }

//
        @Override
        public OFBsnTlvForwardErrorCorrection build() {
            if(!this.valueSet)
                throw new IllegalStateException("Property value doesn't have default value -- must be set");
            if(value == null)
                throw new NullPointerException("Property value must not be null");


            return new OFBsnTlvForwardErrorCorrectionVer14(
                    value
                );
        }

    }


    final static Reader READER = new Reader();
    static class Reader implements OFMessageReader<OFBsnTlvForwardErrorCorrection> {
        @Override
        public OFBsnTlvForwardErrorCorrection readFrom(ByteBuf bb) throws OFParseError {
            int start = bb.readerIndex();
            // fixed value property type == 0x95
            short type = bb.readShort();
            if(type != (short) 0x95)
                throw new OFParseError("Wrong type: Expected=0x95(0x95), got="+type);
            int length = U16.f(bb.readShort());
            if(length != 5)
                throw new OFParseError("Wrong length: Expected=5(5), got="+length);
            if(bb.readableBytes() + (bb.readerIndex() - start) < length) {
                // Buffer does not have all data yet
                bb.readerIndex(start);
                return null;
            }
            if(logger.isTraceEnabled())
                logger.trace("readFrom - length={}", length);
            OFBsnForwardErrorCorrectionType value = OFBsnForwardErrorCorrectionTypeSerializerVer14.readFrom(bb);

            OFBsnTlvForwardErrorCorrectionVer14 bsnTlvForwardErrorCorrectionVer14 = new OFBsnTlvForwardErrorCorrectionVer14(
                    value
                    );
            if(logger.isTraceEnabled())
                logger.trace("readFrom - read={}", bsnTlvForwardErrorCorrectionVer14);
            return bsnTlvForwardErrorCorrectionVer14;
        }
    }

    public void putTo(PrimitiveSink sink) {
        FUNNEL.funnel(this, sink);
    }

    final static OFBsnTlvForwardErrorCorrectionVer14Funnel FUNNEL = new OFBsnTlvForwardErrorCorrectionVer14Funnel();
    static class OFBsnTlvForwardErrorCorrectionVer14Funnel implements Funnel<OFBsnTlvForwardErrorCorrectionVer14> {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(OFBsnTlvForwardErrorCorrectionVer14 message, PrimitiveSink sink) {
            // fixed value property type = 0x95
            sink.putShort((short) 0x95);
            // fixed value property length = 5
            sink.putShort((short) 0x5);
            OFBsnForwardErrorCorrectionTypeSerializerVer14.putTo(message.value, sink);
        }
    }


    public void writeTo(ByteBuf bb) {
        WRITER.write(bb, this);
    }

    final static Writer WRITER = new Writer();
    static class Writer implements OFMessageWriter<OFBsnTlvForwardErrorCorrectionVer14> {
        @Override
        public void write(ByteBuf bb, OFBsnTlvForwardErrorCorrectionVer14 message) {
            // fixed value property type = 0x95
            bb.writeShort((short) 0x95);
            // fixed value property length = 5
            bb.writeShort((short) 0x5);
            OFBsnForwardErrorCorrectionTypeSerializerVer14.writeTo(bb, message.value);


        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("OFBsnTlvForwardErrorCorrectionVer14(");
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
        OFBsnTlvForwardErrorCorrectionVer14 other = (OFBsnTlvForwardErrorCorrectionVer14) obj;

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
