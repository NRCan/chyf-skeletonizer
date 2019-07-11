@ECHO OFF
                                                                                
if NOT DEFINED JAVA_HOME (
  ECHO ERROR: JAVA_HOME not set. Please set to root of your java installation.
  GOTO END
)

set JAVA=%JAVA_HOME%/bin/java

set CP=".;../lib/utilities-1.0.jar;../lib/graph-1.0.jar;../lib/bsh-2.0b1.jar;../lib/jts-1.5.0.jar;../lib/jump-1.1.3RC5.jar;../lib/xercesImpl-2.6.0.jar;../lib/xml-apis-2.0.0.jar;../lib/junit-3.8.1.jar;../lib/jama-1.0.1.jar;../lib/jdom-1.0b8.jar;../lib/xalan-2.3.1.jar;../lib/skeletonizer-1.0.jar"

%JAVA% -Xmx500M -cp %CP% net.refractions.skeletons.SkeletonizeDatasetPlugin %1 %2 %3 %4 %5

