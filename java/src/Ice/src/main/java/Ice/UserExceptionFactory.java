// **********************************************************************
//
// Copyright (c) 2003-2016 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

package Ice;

/**
 * Instantiates user exceptions.
 *
 * @see InputStream#throwException
 **/
public interface UserExceptionFactory
{
    /**
     * Instantiate a user exception with the given Slice type id (such as <code>::Module::MyException</code>)
     * and throw it. If the implementation does not throw an exception, the Ice run time will fall back
     * to using its default behavior for instantiating the user exception.
     **/
    void createAndThrow(String typeId)
        throws UserException;
}
