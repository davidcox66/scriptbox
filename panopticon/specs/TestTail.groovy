import groovy.text.SimpleTemplateEngine;

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
tail(2,[getContextPropertyEx('server.log')],processor);

def mbox = inbox();
mbox.blackout("20:00", "07:00" ) { msg ->
	msg.priority >= 5
}
mbox.throttle( 3 ) { msg ->
	msg.priority >= 5
}

mbox.throttle( 1 ) { msg, box ->
	msg.priority > box.previousDelivery.maxPriority;
} 

/*
mbox.predicate{ msg ->
}
*/

Random rand = new Random();
observe( 'SERVER_LOG_ERROR' ) { ev, record ->
	// int priority = Math.abs(rand.nextInt() % 10);
	int priority = 3;
	println "Priority: ${priority}";
	mbox.send( record.location, priority, record );
}

String text = '''
	Received ${msgs.size()} messages

<% msgs.each{ msg -> 
	def rec = msg.data;
%>
	${msg.priority} : ${rec.time} ${rec.user} ${rec.request} ${rec.level} ${rec.location} ${rec.message} 
	${rec.data}
<% } %>
''';

def engine = new SimpleTemplateEngine()
def template = engine.createTemplate(text);

mbox.deliver{ msgs ->
    println "Preparing email";
    def msg = mailer.message();
    msg.from = "test@verizonwireless.com";
    msg.subject = "Received ${msgs.size()} messages";
    getContextPropertyEx('mail.addresses').each{ 
        msg.addTo( it );
    }
	
    msg.addText( template.make([msgs: msgs]).toString() );
    // msg.addText( "error.txt", "${record.data}" );
    println "Sending email";
    msg.send(); 
    println "Email sent";
}

