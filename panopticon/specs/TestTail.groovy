
mailer.host = "Mailhost.odc.vzwcorp.com";

def converter = objectify(
    ~/(?s)^(\d\d:\d\d:\d\d\.\d\d\d)\s+<([^:]*):([^:]*):([^>]*)>\s+(\w+)\s+([^\s]+)\s+(.*?)\n(.*)/,
    ["time","user","request","thread", "level", "location", "message", "data" ],
    {
		it.time = totime( 'HH:mm:ss', it.time )
		
		raise( 'SERVER_LOG', it )
		if( 'ERROR' == it.level ) {
			raise( 'SERVER_LOG_ERROR', it )
		}
    },
    {
		raise( 'SERVER_LOG_REJECT', it )
    }
);

def header = ~/^\d\d:\d\d:\d\d\.\d\d\d\s.*/
def combined = coalesce(header,converter);

tail(10,['/tmp/dummy'],combined);

boolean sent = false;
observe( 'SERVER_LOG_ERROR' ) { 
    if( !sent ) {
        println "Preparing email";
        sent = true;
        def msg = mailer.message();
        msg.from = "test@verizonwireless.com";
        msg.subject = "Got an ${it.level} message";
        msg.addTo( "David.Cox2@VerizonWireless.com" );
        msg.addText( "Message: ${it.message}" );
        msg.addText( "${it.data}" );
        println "Sending email";
        msg.send(); 
        println "Email sent";
    }
}

def fl = flow(); 

fl.filter( blackout("20:00", "07:00" ) { msg ->
	return msg.priority < 5;
} );

fl.filter( suspend(10) { msg ->
	return msg.priority < 5;
} );

fl.filter( collate{ a, b ->
	
} );

fl.receiver{ msgs ->
}


observe( 'SERVER_LOG_ERROR' ) { ev, record ->
	fl.send( record ) 
}
