// **********************************************************************
//
// Copyright (c) 2003-2008 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package IceInternal;

final class UdpTransceiver implements Transceiver
{
    public java.nio.channels.SelectableChannel
    fd()
    {
        assert(_fd != null);
        return _fd;
    }

    public SocketStatus
    initialize()
    {
        //
        // Nothing to do.
        //
        return SocketStatus.Finished;
    }

    public void
    close()
    {
        assert(_fd != null);
        
        if(_traceLevels.network >= 1)
        {
            String s = "closing udp connection\n" + toString();
            _logger.trace(_traceLevels.networkCat, s);
        }
        
        try
        {
            _fd.close();
        }
        catch(java.io.IOException ex)
        {
        }
        _fd = null;
    }

    public boolean
    write(Buffer buf)
    {
        assert(buf.b.position() == 0);
        final int packetSize = java.lang.Math.min(_maxPacketSize, _sndSize - _udpOverhead);
        if(packetSize < buf.size())
        {
            //
            // We don't log a warning here because the client gets an exception anyway.
            //
            throw new Ice.DatagramLimitException();
        }

        while(buf.b.hasRemaining())
        {
            try
            {
                assert(_fd != null);

                int ret = _fd.write(buf.b);
                
                if(ret == 0)
                {
                    return false;
                }

                if(_traceLevels.network >= 3)
                {
                    String s = "sent " + ret + " bytes via udp\n" + toString();
                    _logger.trace(_traceLevels.networkCat, s);
                }

                if(_stats != null)
                {
                    _stats.bytesSent(type(), ret);
                }

                assert(ret == buf.b.limit());
                break;
            }
            catch(java.nio.channels.AsynchronousCloseException ex)
            {
                Ice.ConnectionLostException se = new Ice.ConnectionLostException();
                se.initCause(ex);
                throw se;
            }
            catch(java.net.PortUnreachableException ex)
            {
                Ice.ConnectionLostException se = new Ice.ConnectionLostException();
                se.initCause(ex);
                throw se;
            }
            catch(java.io.InterruptedIOException ex)
            {
                continue;
            }
            catch(java.io.IOException ex)
            {
                Ice.SocketException se = new Ice.SocketException();
                se.initCause(ex);
                throw se;
            }
        }

        return true;
    }

    public boolean
    read(Buffer buf, Ice.BooleanHolder moreData)
    {
        assert(buf.b.position() == 0);
        moreData.value = false;

        final int packetSize = java.lang.Math.min(_maxPacketSize, _rcvSize - _udpOverhead);
        if(packetSize < buf.size())
        {
            //
            // We log a warning here because this is the server side -- without the
            // the warning, there would only be silence.
            //
            if(_warn)
            {
                _logger.warning("DatagramLimitException: maximum size of " + packetSize + " exceeded");
            }
            throw new Ice.DatagramLimitException();
        }
        buf.resize(packetSize, true);
        buf.b.position(0);

        int ret = 0;
        while(true)
        {
            try
            {
                java.net.InetSocketAddress sender = (java.net.InetSocketAddress)_fd.receive(buf.b);

                if(sender == null || buf.b.position() == 0)
                {
                    return false;
                }

                ret = buf.b.position();

                if(_connect)
                {
                    //
                    // If we must connect, then we connect to the first peer that
                    // sends us a packet.
                    //
                    Network.doConnect(_fd, sender);
                    _connect = false; // We're connected now

                    if(_traceLevels.network >= 1)
                    {
                        String s = "connected udp socket\n" + toString();
                        _logger.trace(_traceLevels.networkCat, s);
                    }
                }

                break;
            }
            catch(java.nio.channels.AsynchronousCloseException ex)
            {
                Ice.ConnectionLostException se = new Ice.ConnectionLostException();
                se.initCause(ex);
                throw se;
            }
            catch(java.net.PortUnreachableException ex)
            {
                Ice.ConnectionLostException se = new Ice.ConnectionLostException();
                se.initCause(ex);
                throw se;
            }
            catch(java.io.InterruptedIOException ex)
            {
                continue;
            }
            catch(java.io.IOException ex)
            {
                if(Network.connectionLost(ex))
                {
                    Ice.ConnectionLostException se = new Ice.ConnectionLostException();
                    se.initCause(ex);
                    throw se;
                }

                Ice.SocketException se = new Ice.SocketException();
                se.initCause(ex);
                throw se;
            }
        }

        if(_traceLevels.network >= 3)
        {
            String s = "received " + ret + " bytes via udp\n" + toString();
            _logger.trace(_traceLevels.networkCat, s);
        }

        if(_stats != null)
        {
            _stats.bytesReceived(type(), ret);
        }

        buf.resize(ret, true);
        buf.b.position(ret);

        return true;
    }

    public String
    type()
    {
        return "udp";
    }

    public String
    toString()
    {
        if(_mcastAddr != null && _fd != null)
        {
            return Network.fdToString(_fd) + "\nmulticast address = " + Network.addrToString(_mcastAddr);
        }
        else
        {
            return Network.fdToString(_fd);
        }
    }

    public void
    checkSendSize(Buffer buf, int messageSizeMax)
    {
        if(buf.size() > messageSizeMax)
        {
            throw new Ice.MemoryLimitException();
        }
        final int packetSize = java.lang.Math.min(_maxPacketSize, _sndSize - _udpOverhead);
        if(packetSize < buf.size())
        {
            throw new Ice.DatagramLimitException();
        }
    }

    public final int
    effectivePort()
    {
        return _addr.getPort();
    }

    //
    // Only for use by UdpEndpoint
    //
    UdpTransceiver(Instance instance, java.net.InetSocketAddress addr, String mcastInterface, int mcastTtl)
    {
        _traceLevels = instance.traceLevels();
        _logger = instance.initializationData().logger;
        _stats = instance.initializationData().stats;
        _incoming = false;
        _connect = true;
        _warn = instance.initializationData().properties.getPropertyAsInt("Ice.Warn.Datagrams") > 0;
        _addr = addr;

        try
        {
            _fd = Network.createUdpSocket();
            setBufSize(instance);
            Network.setBlock(_fd, false);
            Network.doConnect(_fd, _addr);
            _connect = false; // We're connected now
            if(_addr.getAddress().isMulticastAddress())
            {
                configureMulticast(null, mcastInterface, mcastTtl);
            }

            if(_traceLevels.network >= 1)
            {
                String s = "starting to send udp packets\n" + toString();
                _logger.trace(_traceLevels.networkCat, s);
            }
        }
        catch(Ice.LocalException ex)
        {
            _fd = null;
            throw ex;
        }
    }

    //
    // Only for use by UdpEndpoint
    //
    UdpTransceiver(Instance instance, String host, int port, String mcastInterface, boolean connect)
    {
        _traceLevels = instance.traceLevels();
        _logger = instance.initializationData().logger;
        _stats = instance.initializationData().stats;
        _incoming = true;
        _connect = connect;
        _warn = instance.initializationData().properties.getPropertyAsInt("Ice.Warn.Datagrams") > 0;

        try
        {
            _fd = Network.createUdpSocket();
            setBufSize(instance);
            Network.setBlock(_fd, false);
            _addr = Network.getAddressForServer(host, port, instance.protocolSupport());
            if(_traceLevels.network >= 2)
            {
                String s = "attempting to bind to udp socket " + Network.addrToString(_addr);
                _logger.trace(_traceLevels.networkCat, s);
            }
            if(_addr.getAddress().isMulticastAddress())
            {
                Network.setReuseAddress(_fd, true);
                _mcastAddr = _addr;
                _addr = Network.doBind(_fd, Network.getAddress("0.0.0.0", port, Network.EnableIPv4));
                configureMulticast(_mcastAddr, mcastInterface, -1);
            }
            else
            {
                if(!System.getProperty("os.name").startsWith("Windows"))
                {
                    //
                    // Enable SO_REUSEADDR on Unix platforms to allow
                    // re-using the socket even if it's in the TIME_WAIT
                    // state. On Windows, this doesn't appear to be
                    // necessary and enabling SO_REUSEADDR would actually
                    // not be a good thing since it allows a second
                    // process to bind to an address even it's already
                    // bound by another process.
                    //
                    // TODO: using SO_EXCLUSIVEADDRUSE on Windows would
                    // probably be better but it's only supported by recent
                    // Windows versions (XP SP2, Windows Server 2003).
                    //
                    Network.setReuseAddress(_fd, true);
                }
                _addr = Network.doBind(_fd, _addr);
            }

            if(_traceLevels.network >= 1)
            {
                String s = "starting to receive udp packets\n" + toString();
                _logger.trace(_traceLevels.networkCat, s);
            }
        }
        catch(Ice.LocalException ex)
        {
            _fd = null;
            throw ex;
        }
    }

    private synchronized void
    setBufSize(Instance instance)
    {
        assert(_fd != null);

        for(int i = 0; i < 2; ++i)
        {
            String direction;
            String prop;
            int dfltSize;
            if(i == 0)
            {
                direction = "receive";
                prop = "Ice.UDP.RcvSize";
                dfltSize = Network.getRecvBufferSize(_fd);
                _rcvSize = dfltSize;
            }
            else
            {
                direction = "send";
                prop = "Ice.UDP.SndSize";
                dfltSize = Network.getSendBufferSize(_fd);
                _sndSize = dfltSize;
            }

            //
            // Get property for buffer size and check for sanity.
            //
            int sizeRequested = instance.initializationData().properties.getPropertyAsIntWithDefault(prop, dfltSize);
            if(sizeRequested < _udpOverhead)
            {
                _logger.warning("Invalid " + prop + " value of " + sizeRequested + " adjusted to " + dfltSize);
                sizeRequested = dfltSize;
            }
                
            if(sizeRequested != dfltSize)
            {
                //
                // Try to set the buffer size. The kernel will silently adjust
                // the size to an acceptable value. Then read the size back to
                // get the size that was actually set.
                //
                int sizeSet;
                if(i == 0)
                {
                    Network.setRecvBufferSize(_fd, sizeRequested);
                    _rcvSize = Network.getRecvBufferSize(_fd);
                    sizeSet = _rcvSize;
                }
                else
                {
                    Network.setSendBufferSize(_fd, sizeRequested);
                    _sndSize = Network.getSendBufferSize(_fd);
                    sizeSet = _sndSize;
                }

                //
                // Warn if the size that was set is less than the requested size.
                //
                if(sizeSet < sizeRequested)
                {
                    _logger.warning("UDP " + direction + " buffer size: requested size of "
                                    + sizeRequested + " adjusted to " + sizeSet);
                }
            }
        }
    }

    //
    // The NIO classes do not support multicast, at least not directly. This method works around
    // that limitation by using reflection to configure the file descriptor of a DatagramChannel for
    // multicast operation. Specifically, an instance of java.net.PlainDatagramSocketImpl is used
    // to (temporarily) wrap the channel's file descriptor.
    //
    private void
    configureMulticast(java.net.SocketAddress group, String interfaceAddr, int ttl)
    {
        try
        {
            java.lang.reflect.Constructor c =
                Class.forName("java.net.PlainDatagramSocketImpl").getDeclaredConstructor((Class[])null);
            c.setAccessible(true);
            java.net.DatagramSocketImpl socketImpl = (java.net.DatagramSocketImpl)c.newInstance((Object[])null);

            //
            // We have to invoke the protected create() method on the PlainDatagramSocketImpl object so
            // that this hack works properly when IPv6 is enabled on Windows.
            //
            java.lang.reflect.Method m =
                Class.forName("java.net.PlainDatagramSocketImpl").getDeclaredMethod("create", (Class[])null);
            m.setAccessible(true);
            m.invoke(socketImpl);

            java.lang.reflect.Field channelFd = 
                Class.forName("sun.nio.ch.DatagramChannelImpl").getDeclaredField("fd");
            channelFd.setAccessible(true);

            java.lang.reflect.Field socketFd = java.net.DatagramSocketImpl.class.getDeclaredField("fd");
            socketFd.setAccessible(true);
            socketFd.set(socketImpl, channelFd.get(_fd));

            try 
            {
                java.net.NetworkInterface intf = null;
                if(interfaceAddr.length() != 0)
                {
                    intf = java.net.NetworkInterface.getByName(interfaceAddr);
                    if(intf == null)
                    {
                        java.net.InetSocketAddress addr = Network.getAddress(interfaceAddr, 0, Network.EnableIPv4);
                        intf = java.net.NetworkInterface.getByInetAddress(addr.getAddress());
                    }
                }

                if(group != null)
                {
                    Class[] types = new Class[]{ java.net.SocketAddress.class, java.net.NetworkInterface.class };
                    m = socketImpl.getClass().getDeclaredMethod("joinGroup", types);
                    m.setAccessible(true);
                    Object[] args = new Object[]{ group, intf };
                    m.invoke(socketImpl, args);
                }
                else if(intf != null)
                {
                    Class[] types = new Class[]{ Integer.TYPE, Object.class };
                    m = socketImpl.getClass().getDeclaredMethod("setOption", types);
                    m.setAccessible(true);
                    Object[] args = new Object[]{ new Integer(java.net.SocketOptions.IP_MULTICAST_IF2), intf };
                    m.invoke(socketImpl, args);
                }

                if(ttl != -1)
                {
                    Class[] types = new Class[]{ Integer.TYPE };
                    m = java.net.DatagramSocketImpl.class.getDeclaredMethod("setTimeToLive", types);
                    m.setAccessible(true);
                    Object[] args = new Object[]{ new Integer(ttl) };
                    m.invoke(socketImpl, args);
                }
            }
            finally 
            {
                socketFd.set(socketImpl, null);
            }
        }
        catch(Exception ex)
        {
            Ice.SocketException se = new Ice.SocketException();
            se.initCause(ex);
            throw se;
        }
    }

    private void
    closeSocket()
    {
    }

    protected synchronized void
    finalize()
        throws Throwable
    {
        IceUtilInternal.Assert.FinalizerAssert(_fd == null);

        super.finalize();
    }

    private TraceLevels _traceLevels;
    private Ice.Logger _logger;
    private Ice.Stats _stats;
    private boolean _incoming;
    private boolean _connect;
    private final boolean _warn;
    private int _rcvSize;
    private int _sndSize;
    private java.nio.channels.DatagramChannel _fd;
    private java.net.InetSocketAddress _addr;
    private java.net.InetSocketAddress _mcastAddr = null;

    //
    // The maximum IP datagram size is 65535. Subtract 20 bytes for the IP header and 8 bytes for the UDP header
    // to get the maximum payload.
    //
    private final static int _udpOverhead = 20 + 8;
    private final static int _maxPacketSize = 65535 - _udpOverhead;
}
