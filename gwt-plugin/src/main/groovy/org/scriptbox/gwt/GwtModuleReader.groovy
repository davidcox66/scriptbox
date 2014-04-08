package org.scriptbox.gwt;

import java.util.List;

public interface GwtModuleReader
{
    GwtModule readModule( String name );
    List<String> getGwtModules();
}
