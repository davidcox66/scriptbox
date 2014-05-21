
mailer.host = "Mailhost.odc.vzwcorp.com";

boolean sent = false;
def header = ~/^\d\d:\d\d:\d\d\.\d\d\d\s.*/

def processor = { record ->
    println "record: ${record}"
    if( !sent ) {
        println "Preparing email";
        sent = true;
        def msg = mailer.message();
        msg.from = "test@verizonwireless.com";
        msg.subject = "Received an ${record.level} message";
        msg.addTo( "David.Cox2@VerizonWireless.com" );
        msg.addText( "Got this message: ${record.message}" );
        println "Sending email";
        msg.send(); 
        println "Email sent";
    }
}

def converter = objectify(
    ~/(?s)^(\d\d:\d\d:\d\d\.\d\d\d)\s+<([^:]*):([^:]*):([^>]*)>\s+(\w+)\s+([^\s]+)\s+([^\n]*)\n?(.*)/,
    ["time","user","request","thread", "level", "location", "message", "data" ],
    processor, 
    {
        // rejected
    }
);

def combined = coalesce(header,converter);

tail(10,['/tmp/dummy'],combined);


