/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.lisp.ctl;

import io.netty.channel.Channel;
import org.onlab.packet.IpAddress;
import org.onosproject.lisp.msg.protocols.LispMessage;
import org.onosproject.net.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * An abstract representation of a LISP router.
 * This class can be extended by others to serve as a base for their vendor
 * specific representation of a router.
 */
public abstract class AbstractLispRouter implements LispRouter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String DROP_MESSAGE_WARN =
                    "Drop message {} destined to router {} as channel is closed.";

    private Channel channel;
    private String channelId;

    private boolean connected;
    private boolean subscribed;
    private LispRouterId routerId;
    private LispRouterAgent agent;

    /**
     * A default constructor.
     *
     * @param routerId router identifier
     */
    AbstractLispRouter(LispRouterId routerId) {
        this.routerId = routerId;
    }

    @Override
    public final String channelId() {
        return channelId;
    }

    @Override
    public final IpAddress routerId() {
        return routerId.id();
    }

    @Override
    public final String stringId() {
        return routerId.toString();
    }

    @Override
    public final void setChannel(Channel channel) {
        this.channel = channel;
        final SocketAddress address = channel.remoteAddress();
        if (address instanceof InetSocketAddress) {
            final InetSocketAddress inetAddress = (InetSocketAddress) address;
            final IpAddress ipAddress = IpAddress.valueOf(inetAddress.getAddress());
            if (ipAddress.isIp4()) {
                channelId = ipAddress.toString() + ':' + inetAddress.getPort();
            } else {
                channelId = '[' + ipAddress.toString() + "]:" + inetAddress.getPort();
            }
        }
    }

    @Override
    public final void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public final boolean isConnected() {
        return connected;
    }


    @Override
    public final boolean isSubscribed() {
        return subscribed;
    }

    @Override
    public final void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public final void setAgent(LispRouterAgent agent) {
        // we never assign the agent more than one time
        if (this.agent == null) {
            this.agent = agent;
        }
    }

    @Override
    public final Device.Type deviceType() {
        return Device.Type.ROUTER;
    }

    @Override
    public void sendMessage(LispMessage message) {
        if (channel.isOpen()) {
            // TODO: need to consider to use writeAndFlush if possible
            channel.write(message);
            agent.processDownstreamMessage(routerId, message);
        } else {
            log.warn(DROP_MESSAGE_WARN, message, routerId);
        }
    }

    @Override
    public void handleMessage(LispMessage message) {
        this.agent.processUpstreamMessage(routerId, message);
    }

    @Override
    public final boolean connectRouter() {
        return this.agent.addConnectedRouter(routerId, this);
    }

    @Override
    public final void disconnectRouter() {
        setConnected(false);
        channel.close();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());

        String address = (channel != null) ? channel.remoteAddress().toString() : "?";
        String routerId = (stringId() != null) ? stringId() : "?";

        sb.append(" [");
        sb.append(address);
        sb.append(" routerId[");
        sb.append(routerId);
        sb.append("]]");

        return sb.toString();
    }
}
