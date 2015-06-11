package org.scriptbox.util.common.mail;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MailMessage {

	private boolean html;
	private Message message;
	private List<MimeBodyPart> parts = new ArrayList<MimeBodyPart>();

    public MailMessage() {
        this( System.getProperties() );
    }

    public MailMessage( Properties properties ) {
        this( Session.getInstance(properties, null) );
    }

	public MailMessage( Session session ) {
	    message = new MimeMessage(session);
	}
	
	public void setPriority( int priority ) {
		try {
			if( priority != 0 ) {
	            message.setHeader( "X-Priority", "" + priority );
			}
			else {
				message.removeHeader( "X-Priority" );
			}
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Error setting message priority: " + priority, ex );
		}
	}
	
	public void setFrom( String from ) {
		try {
			message.setFrom( getAddress(from) );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed setting FROM address: '" + from + "'", ex );
		}
	}

	public void addTo( String... tos ) {
		addRecipients( Message.RecipientType.TO, "TO", tos );
	}
	
	public void addCc( String... ccs ) {
		addRecipients( Message.RecipientType.CC, "CC", ccs );
	}
	
	public void addBcc( String... ccs ) {
		addRecipients( Message.RecipientType.BCC, "BCC", ccs );
	}
	
	public void setSubject( String subject ) {
		try {
			message.setSubject( subject );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed setting subject: '" + subject + "'", ex );
		}
	}
	
	public void addText( String text ) {
	    try {
	    	MimeBodyPart textpart = new MimeBodyPart();
	    	textpart.setText( text );
	    	parts.add( textpart );
	    }
	    catch( Exception ex ) {
	    	throw new RuntimeException( "Failed setting message text", ex );
	    }
	}
	
	public void addText( String description, String text ) {
	    try {
	    	MimeBodyPart textpart = new MimeBodyPart();
	    	textpart.setText( text );
	    	textpart.setDescription( description );
	    	parts.add( textpart );
	    }
	    catch( Exception ex ) {
	    	throw new RuntimeException( "Failed setting message text", ex );
	    }
	}
	public void addHtml( String html ) {
	    try {
	    	MimeBodyPart htmlpart = new MimeBodyPart();
            htmlpart.setContent( html, "text/html" );
            parts.add( htmlpart );
            this.html = true;
	    }
	    catch( Exception ex ) {
	    	throw new RuntimeException( "Failed setting message html", ex );
	    }
	}
	
	public void addFile( String file ) {
		try {
			MimeBodyPart mbp = new MimeBodyPart();
	        FileDataSource fds = new FileDataSource(file);
	        mbp.setDataHandler(new DataHandler(fds));
	        mbp.setFileName(fds.getName());
	        parts.add( mbp );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed adding file: '" + file + "'", ex );
		}
	}
	
	public void send() {
		try {
			Multipart mp = !html ? new MimeMultipart() : new MimeMultipart( "alternative" );
			for( MimeBodyPart mbp : parts ) {
				mp.addBodyPart( mbp );
			}
			message.setContent( mp );
			message.setSentDate( new Date() );
			Transport.send( message );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed sending email", ex );
		}
	
	}
	private void addRecipients( Message.RecipientType type, String name, String... addrs ) {
		for( String addr : addrs ) {
			try {
				message.addRecipient(type, new InternetAddress(addr) );
			}
			catch( Exception ex ) {
				throw new RuntimeException( "Failed adding " + name + " address: '" + addr + "'", ex );
			}
		}
	}
	private InternetAddress getAddress( String address ) {
		try {
			return new InternetAddress( address  );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Invalid address: '" + address + "'", ex );
		}
	}
}
