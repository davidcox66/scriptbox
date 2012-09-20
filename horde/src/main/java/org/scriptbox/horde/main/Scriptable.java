package org.scriptbox.horde.main;

import java.util.List
import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Scriptable {

    private static final Logger LOGGER = LoggerFactory.getLogger( Scriptable );
   
    String name; 
    String scriptName;
    String scriptText;
    List args;
     
    private static AtomicInteger counter = new AtomicInteger();
    
    Scriptable( String name, String scriptText, List args ) {
        this.name = name;
        this.scriptText = scriptText;
        this.args = args;
        this.scriptName = generateScriptName();
    }
    
    void callInClassLoader( ClassLoader loader, Closure closure ) {
        if( closure ) {
            Thread thread = Thread.currentThread();
            ClassLoader current = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader( loader );
                closure.call();
            }
            finally {
               thread.setContextClassLoader( current );
            }
        }
    }    
    String getArgString() {    
       String argstr = "";
       if( args ) {
           argstr = args.join(',');
       }
       return argstr;
    }
    
    protected String generateScriptName() {
        return name + 'Script' + counter.addAndGet(1) + '.groovy';
    }
    
    
}
