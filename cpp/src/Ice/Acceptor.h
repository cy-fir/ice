// **********************************************************************
//
// Copyright (c) 2003-2004 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

#ifndef ICE_ACCEPTOR_H
#define ICE_ACCEPTOR_H

#include <IceUtil/Shared.h>
#include <Ice/AcceptorF.h>
#include <Ice/TransceiverF.h>

#ifdef _WIN32
typedef int ssize_t;
#else
#   define SOCKET int
#endif

namespace IceInternal
{

class ICE_PROTOCOL_API Acceptor : public ::IceUtil::Shared
{
public:

    virtual SOCKET fd() = 0;
    virtual void close() = 0;
    virtual void listen() = 0;
    virtual TransceiverPtr accept(int) = 0;
    virtual std::string toString() const = 0;
};

}

#endif
