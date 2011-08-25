/*
 *
 * Copyright (C) 2009-2010 IPB Halle, Sebastian Wolf
 *
 * Contact: swolf@ipb-halle.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.ipbhalle.metfusion.wrapper;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.io.*;
import java.util.Properties;

public class MailClient {

	public void sendMail(String mailServer, String from, String to,
			String subject, String messageBody, String[] attachments)
			throws MessagingException, AddressException {
		// Setup mail server
		Properties props = System.getProperties();
		props.put("mail.smtp.host", mailServer);

		// Get a mail session
		Session session = Session.getDefaultInstance(props, null);

		// Define a new mail message
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);

		// Create a message part to represent the body text
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(messageBody);

		// use a MimeMultipart as we need to handle the file attachments
		Multipart multipart = new MimeMultipart();

		// add the message body to the mime message
		multipart.addBodyPart(messageBodyPart);

		// add any file attachments to the message
		addAtachments(attachments, multipart);

		// Put all message parts in the message
		message.setContent(multipart);

		// Send the message
		Transport.send(message);

	}

	public void sendMailWithoutAttach(String mailServer, String from,
			String to, String subject, String messageBody)
			throws MessagingException, AddressException,
			UnsupportedEncodingException {

		// Setup mail server
		Properties props = System.getProperties();
		props.put("mail.smtp.host", mailServer);

		// Get a mail session
		Session session = Session.getDefaultInstance(props, null);

		// Define a new mail message
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		// message.setSubject(subject);
		// message.setText(messageBody);

		String encoding = "8bit";
		String charset = "iso-8859-1";
		String contentType = "text/plain";
		String subjectEncoding = "Q";
		message.setSubject(MimeUtility.encodeText(subject, charset,
				subjectEncoding));
		message.setContent(messageBody, "text/html;CHARSET=iso-8859-1");
		// set Type and Charset in Headerfield 'Content-Type'
		message.setHeader("Content-Type", contentType + "; charset=" + charset);
		// set Transfer-Encoding for printing umlauts
		message.setHeader("Content-Transfer-Encoding", encoding);
		message.saveChanges();

		// Send the message
		Transport.send(message);

	}

	protected void addAtachments(String[] attachments, Multipart multipart)
			throws MessagingException, AddressException {
		for (int i = 0; i <= attachments.length - 1; i++) {
			String filename = attachments[i];
			MimeBodyPart attachmentBodyPart = new MimeBodyPart();

			// use a JAF FileDataSource as it does MIME type detection
			DataSource source = new FileDataSource(filename);
			attachmentBodyPart.setDataHandler(new DataHandler(source));

			// assume that the filename you want to send is the same as the
			// actual file name - could alter this to remove the file path
			attachmentBodyPart.setFileName(filename);

			// add the attachment
			multipart.addBodyPart(attachmentBodyPart);
		}
	}
}
