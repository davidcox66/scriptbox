
mailer.host = getContextPropertyEx('mail.host')

def converter = objectify(
    ~/(?s)^(\d\d:\d\d:\d\d\.\d\d\d)\s+<([^:]*):([^:]*):([^>]*)>\s+(\w+)\s+([^\s]+)\s+([^\n]*)\n?(.*)/,
    ["time","user","request","thread", "level", "location", "message", "data" ],
    {
		// it.time = totime( 'HH:mm:ss', it.time )
		
		raise( 'SERVER_LOG', it )
		if( 'ERROR' == it.level ) {
			raise( 'SERVER_LOG_ERROR', it )
		}
    },
    {
		raise( 'SERVER_LOG_REJECT', it )
    }
);

def processor = coalesce(~/^\d\d:\d\d:\d\d\.\d\d\d\s.*/,converter);
tail(10,[getContextPropertyEx('server.log')],processor);

/*
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
*/

boolean sent = false;
observe( 'SERVER_LOG_ERROR' ) { ev, record ->
    if( !sent ) {
        println "Preparing email";
        sent = true;
        def msg = mailer.message();
        msg.from = "test@verizonwireless.com";
        msg.subject = "Got an ${record.level} message";
        getContextPropertyEx('mail.addresses').each{ 
            msg.addTo( it );
        }
        msg.addText( "Message: ${record.message}" );
        msg.addText( "error.txt", "${record.data}" );
        println "Sending email";
        msg.send(); 
        println "Email sent";
    }
}

