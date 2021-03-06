package com.dianping.mocksocks.transport;

import com.dianping.mocksocks.transport.monitor.StatusFormatter;
import com.dianping.mocksocks.transport.monitor.config.Configs;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yihua.huang@dianping.com
 */
public class Connection {

	private long startTime;

	private long endTime;

	private int bytesSend;

	private int bytesReceive;

	private Channel channel;

	private String status;

	private List<Transmit> messages = new ArrayList<Transmit>();

	private Transmit current;

	public static final String SUCCESS = "success";

	public static final String FAIL = "fail";

	public static final String CONNECTED = "connected";

	private String protocol;

	public Connection(Channel channel) {
		this.channel = channel;
	}

	public Connection() {
	}

	public long getStartTime() {
		return startTime;
	}

	public Connection start() {
		this.startTime = System.currentTimeMillis();
		this.status = CONNECTED;
		return this;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getBytesSend() {
		return bytesSend;
	}

	public synchronized void setBytesSend(int bytesSend) {
		this.bytesSend = bytesSend;
	}

	public synchronized void addBytesSend(long bytesSend) {
		this.bytesSend += bytesSend;
	}

	public int getBytesReceive() {
		return bytesReceive;
	}

	public synchronized void setBytesReceive(int bytesReceive) {
		this.bytesReceive = bytesReceive;
	}

	public synchronized void request(ChannelBuffer channelBuffer) {
		if (!Configs.getInstance().isRecord()) {
			return;
		}
		this.bytesSend += channelBuffer.readableBytes();
		if (current == null) {
			current = new Transmit();
            messages.add(current);
			current.setRequest(new Message(channelBuffer, Message.MessageType.Request, protocol));
		} else if (current.getRequest() == null) {
			current.setRequest(new Message(channelBuffer, Message.MessageType.Request, protocol));
		} else if (current.getRequest() != null && current.getResponse() == null) {
			current.getRequest().addChannelBuffer(channelBuffer);
		} else if (current.getResponse() != null) {
			messages.add(current);
		}
	}

	public synchronized void response(ChannelBuffer channelBuffer) {
		if (!Configs.getInstance().isRecord()) {
			return;
		}
		this.bytesReceive += channelBuffer.readableBytes();
		if (current == null || current.getRequest() == null) {
		} else if (current.getRequest() != null && current.getResponse() == null) {
			current.setResponse(new Message(channelBuffer, Message.MessageType.Response, protocol));
		} else if (current.getResponse() != null) {
			current.getResponse().addChannelBuffer(channelBuffer);
		}
	}

	public synchronized void addBytesReceive(long bytesReceive) {
		this.bytesReceive += bytesReceive;
	}

	public String getStatus() {
		return status;
	}

	public List<Transmit> getMessages() {
		return messages;
	}

	public InetSocketAddress getAddress() {
		return (InetSocketAddress) channel.getRemoteAddress();
	}

	public Connection setStatus(String status) {
		this.status = status;
		return this;
	}

	public Connection close() {
		this.status = "closed";
		endTime = System.currentTimeMillis();
		return this;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String toString() {
		return "Connection{remote=" + channel.getRemoteAddress() + ",startTime="
				+ StatusFormatter.formatTimeStamp(startTime) + ", endTime=" + StatusFormatter.formatTimeStamp(endTime)
				+ ", timeCost=" + (endTime > 0 ? StatusFormatter.formatTimePeriod(endTime - startTime) : 0)
				+ ", bytesSend=" + StatusFormatter.formatBytes(bytesSend) + ", bytesReceive="
				+ StatusFormatter.formatBytes(bytesReceive) + ", status=" + status + '}';
	}
}
