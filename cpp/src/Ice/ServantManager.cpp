// **********************************************************************
//
// Copyright (c) 2003-2004 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

#include <Ice/ServantManager.h>
#include <Ice/ServantLocator.h>
#include <Ice/LocalException.h>
#include <Ice/IdentityUtil.h>
#include <Ice/LoggerUtil.h>
#include <Ice/Instance.h>
#include <IceUtil/StringUtil.h>

using namespace std;
using namespace Ice;
using namespace IceInternal;

void IceInternal::incRef(ServantManager* p) { p->__incRef(); }
void IceInternal::decRef(ServantManager* p) { p->__decRef(); }

void
IceInternal::ServantManager::addServant(const ObjectPtr& object, const Identity& ident, const string& facet)
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    ServantMapMap::iterator p = _servantMapMapHint;

    if(p == _servantMapMap.end() || p->first != ident)
    {
	p = _servantMapMap.find(ident);
    }

    if(p == _servantMapMap.end())
    {
	p = _servantMapMap.insert(_servantMapMapHint, pair<const Identity, FacetMap>(ident, FacetMap()));
    }
    else
    {
	if(p->second.find(facet) != p->second.end())
	{
	    AlreadyRegisteredException ex(__FILE__, __LINE__);
	    ex.kindOfObject = "servant";
	    ex.id = identityToString(ident);
	    if(!facet.empty())
	    {
		ex.id += " -f " + IceUtil::escapeString(facet, "");
	    }
	    throw ex;
	}
    }

    _servantMapMapHint = p;

    p->second.insert(pair<const string, ObjectPtr>(facet, object));
}

void
IceInternal::ServantManager::removeServant(const Identity& ident, const string& facet)
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    ServantMapMap::iterator p = _servantMapMapHint;
    FacetMap::iterator q;

    if(p == _servantMapMap.end() || p->first != ident)
    {
	p = _servantMapMap.find(ident);
    }
    
    if(p == _servantMapMap.end() || (q = p->second.find(facet)) == p->second.end())
    {
	NotRegisteredException ex(__FILE__, __LINE__);
	ex.kindOfObject = "servant";
	ex.id = identityToString(ident);
	if(!facet.empty())
	{
	    ex.id += " -f " + IceUtil::escapeString(facet, "");
	}
	throw ex;
    }

    p->second.erase(q);

    if(p->second.empty())
    {
	if(p == _servantMapMapHint)
	{
	    _servantMapMap.erase(p++);
	    _servantMapMapHint = p;
	}
	else
	{
	    _servantMapMap.erase(p);
	}
    }
}

FacetMap
IceInternal::ServantManager::removeAllFacets(const Identity& ident)
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    ServantMapMap::iterator p = _servantMapMapHint;

    if(p == _servantMapMap.end() || p->first != ident)
    {
	p = _servantMapMap.find(ident);
    }
    
    if(p == _servantMapMap.end())
    {
	NotRegisteredException ex(__FILE__, __LINE__);
	ex.kindOfObject = "servant";
	ex.id = identityToString(ident);
	throw ex;
    }

    FacetMap result = p->second;

    if(p == _servantMapMapHint)
    {
	_servantMapMap.erase(p++);
	_servantMapMapHint = p;
    }
    else
    {
	_servantMapMap.erase(p);
    }

    return result;
}

ObjectPtr
IceInternal::ServantManager::findServant(const Identity& ident, const string& facet) const
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    ServantMapMap::iterator p = _servantMapMapHint;
    FacetMap::iterator q;
    
    ServantMapMap& servantMapMap = const_cast<ServantMapMap&>(_servantMapMap);

    if(p == servantMapMap.end() || p->first != ident)
    {
	p = servantMapMap.find(ident);
    }
    
    if(p == servantMapMap.end() || (q = p->second.find(facet)) == p->second.end())
    {
	return 0;
    }
    else
    {
	_servantMapMapHint = p;
	return q->second;
    }
}

bool
IceInternal::ServantManager::hasServant(const Identity& ident) const
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    ServantMapMap::iterator p = _servantMapMapHint;
    ServantMapMap& servantMapMap = const_cast<ServantMapMap&>(_servantMapMap);

    if(p == servantMapMap.end() || p->first != ident)
    {
	p = servantMapMap.find(ident);
    }
    
    if(p == servantMapMap.end())
    {
	return false;
    }
    else
    {
	_servantMapMapHint = p;
	assert(!p->second.empty());
	return true;
    }
}

void
IceInternal::ServantManager::addServantLocator(const ServantLocatorPtr& locator, const string& category)
{
    IceUtil::Mutex::Lock sync(*this);

    assert(_instance); // Must not be called after destruction.

    if((_locatorMapHint != _locatorMap.end() && _locatorMapHint->first == category)
       || _locatorMap.find(category) != _locatorMap.end())
    {
	AlreadyRegisteredException ex(__FILE__, __LINE__);
	ex.kindOfObject = "servant locator";
	ex.id = IceUtil::escapeString(category, "");
	throw ex;
    }
    
    _locatorMapHint = _locatorMap.insert(_locatorMapHint, pair<const string, ServantLocatorPtr>(category, locator));
}

ServantLocatorPtr
IceInternal::ServantManager::findServantLocator(const string& category) const
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    map<string, ServantLocatorPtr>& locatorMap =
	const_cast<map<string, ServantLocatorPtr>&>(_locatorMap);

    map<string, ServantLocatorPtr>::iterator p = locatorMap.end();
    if(_locatorMapHint != locatorMap.end())
    {
	if(_locatorMapHint->first == category)
	{
	    p = _locatorMapHint;
	}
    }
    
    if(p == locatorMap.end())
    {
	p = locatorMap.find(category);
    }
    
    if(p != locatorMap.end())
    {
	_locatorMapHint = p;
	return p->second;
    }
    else
    {
	return 0;
    }
}

IceInternal::ServantManager::ServantManager(const InstancePtr& instance, const string& adapterName)
    : _instance(instance),
      _adapterName(adapterName),
      _servantMapMapHint(_servantMapMap.end()),
      _locatorMapHint(_locatorMap.end())
{
}

IceInternal::ServantManager::~ServantManager()
{
    //
    // Don't check whether destroy() has been called. It might have
    // not been called if the associated object adapter was not
    // properly deactivated.
    //
    //assert(!_instance);
}

void
IceInternal::ServantManager::destroy()
{
    IceUtil::Mutex::Lock sync(*this);
    
    assert(_instance); // Must not be called after destruction.

    _servantMapMap.clear();
    _servantMapMapHint = _servantMapMap.end();

    for(map<string, ServantLocatorPtr>::const_iterator p = _locatorMap.begin(); p != _locatorMap.end(); ++p)
    {
	try
	{
	    p->second->deactivate(p->first);
	}
	catch(const Exception& ex)
	{
	    Error out(_instance->logger());
	    out << "exception during locator deactivation:\n"
		<< "object adapter: `" << _adapterName << "'\n"
		<< "locator category: `" << p->first << "'\n"
		<< ex;
	}
	catch(...)
	{
	    Error out(_instance->logger());
	    out << "unknown exception during locator deactivation:\n"
		<< "object adapter: `" << _adapterName << "'\n"
		<< "locator category: `" << p->first << "'";
	}
    }

    _locatorMap.clear();
    _locatorMapHint = _locatorMap.end();

    _instance = 0;
}
