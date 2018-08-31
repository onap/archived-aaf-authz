/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.org;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;

public class JavaxMailer implements Mailer {
	private Session session;

	  public JavaxMailer() {
		  
			// Get the default Session object.
			session = Session.getDefaultInstance(System.getProperties());
	
	  }
	  
	  @Override
		public int sendEmail(AuthzTrans trans, boolean testMode, String mailFrom, List<String> to, List<String> cc, String subject, String body,
				Boolean urgent) throws OrganizationException {

			int status = 1;


			try {
				// Create a default MimeMessage object.
				MimeMessage message = new MimeMessage(session);

				// Set From: header field of the header.
				message.setFrom(new InternetAddress(mailFrom));

				if (!testMode) {
					// Set To: header field of the header. This is a required field
					// and calling module should make sure that it is not null or
					// blank
					message.addRecipients(Message.RecipientType.TO,getAddresses(to));

					// Set CC: header field of the header.
					if ((cc != null) && (cc.size() > 0)) {
						message.addRecipients(Message.RecipientType.CC,getAddresses(cc));
					}

					// Set Subject: header field
					message.setSubject(subject);

					if (urgent) {
						message.addHeader("X-Priority", "1");
					}

					// Now set the actual message
					message.setText(body);
				} else {

					// override recipients
					message.addRecipients(Message.RecipientType.TO,
							InternetAddress.parse(mailFrom));

					// Set Subject: header field
					message.setSubject("[TESTMODE] " + subject);

					if (urgent) {
						message.addHeader("X-Priority", "1");
					}

					ArrayList<String> newBody = new ArrayList<>();

					Address temp[] = getAddresses(to);
					String headerString = "TO:\t" + InternetAddress.toString(temp) + "\n";

					temp = getAddresses(cc);
					headerString += "CC:\t" + InternetAddress.toString(temp) + "\n";

					newBody.add(headerString);

					newBody.add("Text: \n");

					newBody.add(body);
					String outString = "";
					for (String s : newBody) {
						outString += s + "\n";
					}

					message.setText(outString);
				}
				// Send message
				Transport.send(message);
				status = 0;

			} catch (MessagingException mex) {
				System.out.println("Error messaging: "+ mex.getMessage());
				System.out.println("Error messaging: "+ mex.toString());
				throw new OrganizationException("Exception send email message "
						+ mex.getMessage());
			}

			return status;
		}

		/**
		 * Convert the delimiter String into Internet addresses with the default
		 * delimiter of ";"
		 * @param strAddress
		 * @return
		 */
		private Address[] getAddresses(List<String> strAddress) throws OrganizationException {
			return this.getAddresses(strAddress,";");
		}
		/**
		 * Convert the delimiter String into Internet addresses with the
		 * delimiter of provided
		 * @param strAddresses
		 * @param delimiter
		 * @return
		 */
		private Address[] getAddresses(List<String> strAddresses, String delimiter) throws OrganizationException {
			Address[] addressArray = new Address[strAddresses.size()];
			int count = 0;
			for (String addr : strAddresses)
			{
				try{
					addressArray[count] = new InternetAddress(addr);
					count++;
				}catch(Exception e){
					throw new OrganizationException("Failed to parse the email address "+ addr +": "+e.getMessage());
				}
			}
			return addressArray;
		}

}
