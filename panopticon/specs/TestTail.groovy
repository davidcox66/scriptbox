
mailer.host = "Mailhost.odc.vzwcorp.com";

boolean sent = false;
def header = ~/^\d\d:\d\d:\d\d\.\d\d\d\s.*/

def processor = { String line ->
    println "Line: ${line}";
    if( !sent ) {
        println "Preparing email";
        sent = true;
        def msg = mailer.message();
        msg.from = "test@verizonwireless.com";
        msg.subject = "Ooops";
        msg.addTo( "David.Cox2@VerizonWireless.com" );
        msg.addText( "Something went wrong" );
        msg.addText( line );
        println "Sending email";
        msg.send(); 
        println "Email sent";
    }
}

def converter = objectify(
    ~/(?s)^(\d\d:\d\d:\d\d\.\d\d\d)\s+<([^:]*):([^:]*):([^>]*)>\s+(\w+)\s+([^\s]+)\s+(.*?)\n(.*)/,
    // ~/(?m)^(\d\d:\d\d:\d\d\.\d\d\d)\s+<([^:]*):([^:]*):([^>]*)>\s+(\w+)\s+([^\s]+)\s+(.*?)\n?(.*)/,
    ["time","user","request","thread", "level", "location", "message", "data" ],
    {
        println "obj: ${it}"
    },
    {
        println "rejected"
    }
);

def combined = coalesce(header,converter);
 

tail(10,['/tmp/dummy'],combined);
// tail(10,['/tmp/dummy'],processor);


