// **********************************************************************
//
// Copyright (c) 2003-2004 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

public class HelloServiceI extends Ice.LocalObjectImpl implements IceBox.Service
{
    public void
    start(String name, Ice.Communicator communicator, String[] args)
        throws IceBox.FailureException
    {
	String id = communicator.getProperties().getProperty("Identity");

        _adapter = communicator.createObjectAdapter(name);
        Ice.Object object = new HelloI();
        _adapter.add(object, Ice.Util.stringToIdentity(id));
        _adapter.activate();
    }

    public void
    stop()
    {
        _adapter.deactivate();
    }

    private Ice.ObjectAdapter _adapter;
}
