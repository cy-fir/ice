// **********************************************************************
//
// Copyright (c) 2003-2004 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

class CallbackClient extends Ice.Application
{
    private static void
    menu()
    {
        System.out.println(
            "usage:\n" +
            "t: send callback as twoway\n" +
            "o: send callback as oneway\n" +
            "O: send callback as batch oneway\n" +
            "d: send callback as datagram\n" +
            "D: send callback as batch datagram\n" +
            "f: flush all batch requests\n" +
	    "v: set/reset override context field\n" +
            "s: shutdown server\n" +
            "x: exit\n" +
            "?: help\n");
    }

    public int
    run(String[] args)
    {
        Ice.Properties properties = communicator().getProperties();
        final String proxyProperty = "Callback.Client.Callback";
        String proxy = properties.getProperty(proxyProperty);
        if(proxy.length() == 0)
        {
            System.err.println("property `" + proxyProperty + "' not set");
            return 1;
        }

        Ice.ObjectPrx base = communicator().stringToProxy(proxy);
        CallbackPrx twoway = CallbackPrxHelper.checkedCast(base.ice_twoway().ice_timeout(-1).ice_secure(false));
        if(twoway == null)
        {
            System.err.println("invalid proxy");
            return 1;
        }
        CallbackPrx oneway = CallbackPrxHelper.uncheckedCast(twoway.ice_oneway());
        CallbackPrx batchOneway = CallbackPrxHelper.uncheckedCast(twoway.ice_batchOneway());
        CallbackPrx datagram = CallbackPrxHelper.uncheckedCast(twoway.ice_datagram());
        CallbackPrx batchDatagram = CallbackPrxHelper.uncheckedCast(twoway.ice_batchDatagram());

        Ice.ObjectAdapter adapter = communicator().createObjectAdapter("Callback.Client");
        adapter.add(new CallbackReceiverI(), Ice.Util.stringToIdentity("callbackReceiver"));
        adapter.activate();

        CallbackReceiverPrx twowayR = 
	    CallbackReceiverPrxHelper.uncheckedCast(adapter.createProxy(
                Ice.Util.stringToIdentity("callbackReceiver")));
        CallbackReceiverPrx onewayR = CallbackReceiverPrxHelper.uncheckedCast(twowayR.ice_oneway());
        //CallbackReceiverPrx batchOnewayR =
        //    CallbackReceiverPrxHelper.uncheckedCast(twowayR.ice_batchOneway());
        CallbackReceiverPrx datagramR = CallbackReceiverPrxHelper.uncheckedCast(twowayR.ice_datagram());
        //CallbackReceiverPrx batchDatagramR =
        //    CallbackReceiverPrxHelper.uncheckedCast(twowayR.ice_batchDatagram());

	String overwrite = null;

        menu();

        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        String line = null;
        do
        {
            try
            {
                System.out.print("==> ");
                System.out.flush();
                line = in.readLine();
                if(line == null)
                {
                    break;
                }
                if(line.equals("t"))
                {
                    java.util.HashMap context = new java.util.HashMap();
                    context.put("_fwd", "t");
                    twoway.initiateCallback(twowayR, context);
                }
                else if(line.equals("o"))
                {
                    java.util.HashMap context = new java.util.HashMap();
                    context.put("_fwd", "o");
                    oneway.initiateCallback(onewayR, context);
                }
                else if(line.equals("O"))
                {
                    java.util.HashMap context = new java.util.HashMap();
                    context.put("_fwd", "O");
		    if(overwrite != null)
		    {
			context.put("_ovwt", overwrite);
		    }
                    batchOneway.initiateCallback(onewayR, context);
                }
                else if(line.equals("d"))
                {
                    java.util.HashMap context = new java.util.HashMap();
                    context.put("_fwd", "d");
                    datagram.initiateCallback(datagramR, context);
                }
                else if(line.equals("D"))
                {
                    java.util.HashMap context = new java.util.HashMap();
                    context.put("_fwd", "D");
		    if(overwrite != null)
		    {
			context.put("_ovwt", overwrite);
		    } 
		    batchDatagram.initiateCallback(datagramR, context);
                }
                else if(line.equals("f"))
                {
		    communicator().flushBatchRequests();
                }
		else if(line.equals("v"))
		{
		    if(overwrite == null)
		    {
			overwrite = "some_value";
			System.out.println("overwrite context field is now `" + overwrite + "'");
		    }
		    else
		    {
			overwrite = null;
			System.out.println("overwrite context field is empty");
		    }
		}
                else if(line.equals("s"))
                {
                    twoway.shutdown();
                }
                else if(line.equals("x"))
                {
                    // Nothing to do
                }
                else if(line.equals("?"))
                {
                    menu();
                }
                else
                {
                    System.out.println("unknown command `" + line + "'");
                    menu();
                }
            }
            catch(java.io.IOException ex)
            {
                ex.printStackTrace();
            }
            catch(Ice.LocalException ex)
            {
                ex.printStackTrace();
            }
        }
        while(!line.equals("x"));

        return 0;
    }
}
