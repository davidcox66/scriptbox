package org.scriptbox.gwt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;


class GwtModule {
    private Xpp3Dom xml;
    private String name;
    private Set<GwtModule> inherits;
    private GwtModuleReader reader;
    private File sourceFile;

    public GwtModule( String name, Xpp3Dom xml, GwtModuleReader reader ) {
        this.name = name;
        this.xml = xml;
        this.reader = reader;
    }

    private String getRenameTo() {
        return xml.getAttribute( "rename-to" );
    }

    public String getPublic() {
        Xpp3Dom node = xml.getChild( "public" );
        return ( node == null ? "public" : node.getAttribute( "path" ) );
    }

    public String[] getSuperSources() {
        Xpp3Dom nodes[] = xml.getChildren( "super-source" );
        if ( nodes == null ) {
            return new String[0];
        }
        String[] superSources = new String[nodes.length];
        int i = 0;
        for ( Xpp3Dom node : nodes ) {
            String path = node.getAttribute( "path" );
            if ( path == null ) {
                path = "";
            }
            superSources[i++] = path;
        }
        return superSources;
    }

    public String[] getSources() {
        Xpp3Dom nodes[] = xml.getChildren( "source" );
        if ( nodes == null || nodes.length == 0 ) {
            return [ "client" ] as String[];
        }
        String[] sources = new String[nodes.length];
        int i = 0;
        for ( Xpp3Dom node : nodes ) {
            sources[i++] = node.getAttribute( "path" );
        }
        return sources;
    }

    public List<String> getEntryPoints() {
        List<String> entryPoints = new ArrayList<String>();
        entryPoints.addAll( getLocalEntryPoints() );
        for ( GwtModule module : getInherits() ) {
            entryPoints.addAll( module.getLocalEntryPoints() );
        }
        return entryPoints;
    }

    private List<String> getLocalEntryPoints() {
        Xpp3Dom nodes[] = xml.getChildren( "entry-point" );
        if ( nodes == null ) {
            return Collections.emptyList();
        }
        List<String> entryPoints = new ArrayList<String>( nodes.length );
        for ( Xpp3Dom node : nodes ) {
            entryPoints.add( node.getAttribute( "class" ) );
        }
        return entryPoints;
    }

    /**
     * Build the set of inhertied modules. Due to xml inheritence mecanism, there may be cicles in the inheritence
     * graph, so we build a set of inherited modules
     */
    public Set<GwtModule> getInherits() {
        if ( inherits != null ) {
            return inherits;
        }

        inherits = new HashSet<GwtModule>();
        addInheritedModules( inherits, getLocalInherits() );

        return inherits;
    }

    /**
     * 
     * @param set
     * @param modules
     * @throws MojoExecutionException
     */
    private void addInheritedModules( Set<GwtModule> set, Set<GwtModule> modules ) {
        for ( GwtModule module : modules ) {
            if ( set.add( module ) ) {
                // if module is allready in the set, don't re-parse it's inherits
                addInheritedModules( set, module.getLocalInherits() );
            }
        }
    }

    private Set<GwtModule> getLocalInherits() {
        Xpp3Dom nodes[] = xml.getChildren( "inherits" );
        if ( nodes == null ) {
            return Collections.emptySet();
        }
        Set<GwtModule> modules = new HashSet<GwtModule>();
        for ( Xpp3Dom node : nodes ) {
            String moduleName = node.getAttribute( "name" );
            // exclude modules from gwt-dev/gwt-user
            if ( !moduleName.startsWith( "com.google.gwt." ) ) {
                modules.add( reader.readModule( moduleName ) );
            }
        }
        return modules;
    }

    public Map<String, String> getServlets() {
        return getServlets( getPath() );
    }

    public Map<String, String> getServlets( String path ) {
        Map<String, String> servlets = getLocalServlets( path );
        for ( GwtModule module : getInherits() ) {
            servlets.putAll( module.getLocalServlets( path ) );
        }
        return servlets;
    }

    private Map<String, String> getLocalServlets( String path ) {
        Map<String, String> servlets = new HashMap<String, String>();
        Xpp3Dom nodes[] = xml.getChildren( "servlet" );
        if ( nodes != null ) {
            for ( Xpp3Dom node : nodes ) {
                servlets.put( StringUtils.isBlank( path ) ? node.getAttribute( "path" ) : path + node.getAttribute( "path" ),
                              node.getAttribute( "class" ) );
            }
        }
        return servlets;
    }

    public String getName() {
        return name;
    }

    public String getPackage() {
        int index = name.lastIndexOf( '.' );
        return ( index < 0 ) ? "" : name.substring( 0, index );
    }

    public String getPath() {
        if ( getRenameTo() != null ) {
            return getRenameTo();
        }
        return name;
    }

    public File getSourceFile() {
    	return sourceFile;
    }
    
    public void setSourceFile(File file) {
		this.sourceFile = file;
	}
	
    @Override
    public boolean equals( Object obj ) {
        return name.equals( ( (GwtModule) obj ).name );
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
