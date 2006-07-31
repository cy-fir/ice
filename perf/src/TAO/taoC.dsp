# Microsoft Developer Studio Project File - Name="taoC" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=taoC - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "taoC.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "taoC.mak" CFG="taoC - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "taoC - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE "taoC - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "taoC - Win32 Debug"

# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "."
# PROP Intermediate_Dir "Debug\taoC"
# PROP Target_Dir ""
MTL=midl.exe
# ADD MTL /D "_DEBUG" /nologo /mktyplib203 /win32
# ADD CPP /nologo /MDd /W3 /Gm /GR /GX /Zi /Gy /I "$(TAO_ROOT)\.." /I "$(TAO_ROOT)" /I "$(ICE_HOME)\include" /I "." /D "_DEBUG" /D "WIN32" /D "_CONSOLE" /FD /c
# SUBTRACT CPP /YX
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409 /i "$(TAO_ROOT)\.." /i "$(TAO_ROOT)" /d "_DEBUG"
BSC32=bscmake.exe
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 /machine:IX86
# ADD LINK32 advapi32.lib user32.lib TAO_Strategiesd.lib TAO_PortableServerd.lib TAOd.lib TAO_ValueTyped.lib TAO_Messagingd.lib ACEd.lib IceUtild.lib /nologo /subsystem:console /incremental:no /debug /machine:I386 /out:".\client.exe" /libpath:"$(TAO_ROOT)\..\lib" /libpath:"$(TAO_ROOT)\..\ace" /libpath:"$(TAO_ROOT)\tao" /libpath:"$(TAO_ROOT)\tao\Strategies" /libpath:"$(TAO_ROOT)\tao\Messaging" /libpath:"$(TAO_ROOT)\tao\ValueType" /libpath:"$(TAO_ROOT)\tao\PortableServer" /libpath:"$(ICE_HOME)\lib"
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "taoC - Win32 Release"

# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
MTL=midl.exe
# ADD MTL /D "NDEBUG" /nologo /mktyplib203 /win32
# ADD CPP /nologo /MD /W3 /GR /GX /O2 /I "$(TAO_ROOT)\.." /I "$(TAO_ROOT)" /I "$(ICE_HOME)\include" /I "." /D "NDEBUG" /D "WIN32" /D "_CONSOLE" /FD /c
# SUBTRACT CPP /YX
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409 /i "$(TAO_ROOT)\.." /i "$(TAO_ROOT)" /d "NDEBUG"
BSC32=bscmake.exe
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 /machine:IX86
# ADD LINK32 advapi32.lib user32.lib TAO_Strategies.lib TAO_PortableServer.lib TAO_ValueType.lib TAO_AnyTypeCode.lib TAO_Messaging.lib TAO.lib ACE.lib IceUtil.lib /nologo /subsystem:console /pdb:none /machine:I386 /out:"client.exe" /libpath:"$(TAO_ROOT)\..\lib" /libpath:"$(TAO_ROOT)\..\ace" /libpath:"$(TAO_ROOT)\tao" /libpath:"$(TAO_ROOT)\tao\Strategies" /libpath:"$(TAO_ROOT)\tao\Messaging" /libpath:"$(TAO_ROOT)\tao\ValueType" /libpath:"$(TAO_ROOT)\tao\PortableServer" /libpath:"$(ICE_HOME)\lib"

!ENDIF 

# Begin Target

# Name "taoC - Win32 Debug"
# Name "taoC - Win32 Release"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;cxx;c"
# Begin Source File

SOURCE="client.cpp"
# End Source File
# Begin Source File

SOURCE=.\Roundtrip_Handler.cpp
# End Source File
# Begin Source File

SOURCE="TestC.cpp"
# End Source File
# Begin Source File

SOURCE=.\TestS.cpp
# End Source File
# Begin Source File

SOURCE=.\WorkerThread.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hh"
# Begin Source File

SOURCE=.\Roundtrip_Handler.h
# End Source File
# Begin Source File

SOURCE="TestC.h"
# End Source File
# Begin Source File

SOURCE="TestS.h"
# End Source File
# Begin Source File

SOURCE="TestS_T.h"
# End Source File
# Begin Source File

SOURCE=.\WorkerThread.h
# End Source File
# End Group
# Begin Group "Inline Files"

# PROP Default_Filter "i;inl"
# Begin Source File

SOURCE="TestC.inl"
# End Source File
# Begin Source File

SOURCE="TestS.inl"
# End Source File
# Begin Source File

SOURCE="TestS_T.inl"
# End Source File
# End Group
# Begin Group "Template Files"

# PROP Default_Filter ""
# Begin Source File

SOURCE="TestS_T.cpp"
# PROP Exclude_From_Build 1
# End Source File
# End Group
# Begin Group "Documentation"

# PROP Default_Filter ""
# Begin Source File

SOURCE="README"
# End Source File
# End Group
# Begin Group "Idl Files"

# PROP Default_Filter "idl"
# Begin Source File

SOURCE="Test.idl"

!IF  "$(CFG)" == "taoC - Win32 Debug"

# PROP Ignore_Default_Tool 1
USERDEP__TEST_="$(ACE_ROOT)\bin\tao_idl.exe"	
# Begin Custom Build - Invoking $(TAO_ROOT)\..\bin\tao_idl on $(InputPath)
InputPath="Test.idl"

BuildCmds= \
	PATH=%PATH%;$(ACE_ROOT)\lib \
	$(ACE_ROOT)\bin\tao_idl -Ge 1 -GC -Wb,pre_include=ace\pre.h -Wb,post_include=ace\post.h -I$(TAO_ROOT) $(InputPath) \
	

"TestC.inl" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS.inl" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS_T.inl" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestC.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS_T.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestC.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS_T.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ELSEIF  "$(CFG)" == "taoC - Win32 Release"

# PROP Ignore_Default_Tool 1
USERDEP__TEST_="$(ACE_ROOT)\bin\tao_idl.exe"	
# Begin Custom Build - Invoking $(ACE_ROOT)\bin\tao_idl on $(InputPath)
InputPath="Test.idl"

BuildCmds= \
	PATH=%PATH%;$(ACE_ROOT)\lib \
	$(ACE_ROOT)\bin\tao_idl -Ge 1 -GC -Wb,pre_include=ace\pre.h -Wb,post_include=ace\post.h -I$(TAO_ROOT) $(InputPath) \
	

"TestC.inl" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS.inl" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS_T.inl" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestC.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS_T.h" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestC.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)

"TestS_T.cpp" : $(SOURCE) "$(INTDIR)" "$(OUTDIR)"
   $(BuildCmds)
# End Custom Build

!ENDIF 

# End Source File
# End Group
# End Target
# End Project
